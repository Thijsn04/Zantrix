import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { useState } from 'react';
import { MAX_OPEN_PATIENTS, PatientWorkspace } from './PatientWorkspace';
import type { CurrentUser, PatientSummary } from '../../lib/api/types';
import { fakeClient, renderWithQuery } from '../../test/render';

const physician: CurrentUser = {
  subject: 's-1', username: 'physician_test', displayName: 'Dr. Test',
  roles: ['PHYSICIAN'], scopes: ['user/*.cruds'],
};

function patient(id: string, name: string): PatientSummary {
  return {
    id, displayName: name, birthDate: '1984-03-12', administrativeGender: 'female',
    active: true, identifier: `MRN-${id}`, duplicateScore: 0,
  };
}

const amara = patient('p-1', 'Amara Okafor');
const bram = patient('p-2', 'Bram de Vries');
const chiara = patient('p-3', 'Chiara Rossi');

/** Mirrors the open/close rules the shell applies, so the limit is exercised end to end. */
function Harness({ initial = [] as PatientSummary[] }) {
  const [open, setOpen] = useState<PatientSummary[]>(initial);
  const [activeId, setActiveId] = useState<string | undefined>(initial[0]?.id);
  const [limitReached, setLimitReached] = useState(false);
  const { client } = fakeClient({ get: vi.fn().mockResolvedValue([amara, bram, chiara]) });

  return (
    <PatientWorkspace
      client={client} user={physician} openPatients={open} activeId={activeId} limitReached={limitReached}
      onOpen={candidate => {
        if (open.some(value => value.id === candidate.id)) { setActiveId(candidate.id); return; }
        if (open.length >= MAX_OPEN_PATIENTS) { setActiveId(undefined); setLimitReached(true); return; }
        setOpen([...open, candidate]);
        setActiveId(candidate.id);
        setLimitReached(false);
      }}
      onActivate={id => setActiveId(id)}
      onClose={id => {
        const remaining = open.filter(value => value.id !== id);
        setOpen(remaining);
        setLimitReached(false);
        if (activeId === id) setActiveId(remaining[0]?.id);
      }}
      onSearch={() => { setActiveId(undefined); setLimitReached(false); }} />
  );
}

describe('open patient charts', () => {
  it('caps the number of charts a clinician can hold open at two', () => {
    expect(MAX_OPEN_PATIENTS).toBe(2);
  });

  it('keeps both charts open and switches between them', async () => {
    const user = userEvent.setup();
    renderWithQuery(<Harness initial={[amara, bram]} />);

    const tabs = screen.getByRole('navigation', { name: /Open patient charts/ });
    expect(within(tabs).getByRole('button', { name: /^Amara Okafor/ })).toHaveAttribute('aria-current', 'page');

    await user.click(within(tabs).getByRole('button', { name: /^Bram de Vries/ }));
    expect(within(tabs).getByRole('button', { name: /^Bram de Vries/ })).toHaveAttribute('aria-current', 'page');
    // Both charts remain open, only the visible one changed.
    expect(within(tabs).getByRole('button', { name: /^Amara Okafor/ })).toBeInTheDocument();
  });

  it('refuses a third chart instead of silently closing one', async () => {
    const user = userEvent.setup();
    renderWithQuery(<Harness initial={[amara, bram]} />);

    // Go to search, then try to open a third patient.
    await user.click(screen.getByRole('button', { name: 'Find patient' }));
    await user.click(await screen.findByRole('button', { name: /^Chiara Rossi/ }));

    expect(await screen.findByText(/You can have 2 charts open at once/)).toBeInTheDocument();
    const tabs = screen.getByRole('navigation', { name: /Open patient charts/ });
    expect(within(tabs).queryByRole('button', { name: /^Chiara Rossi/ })).not.toBeInTheDocument();
    expect(within(tabs).getByRole('button', { name: /^Amara Okafor/ })).toBeInTheDocument();
    expect(within(tabs).getByRole('button', { name: /^Bram de Vries/ })).toBeInTheDocument();
  });

  it('frees a slot when a chart is closed', async () => {
    const user = userEvent.setup();
    renderWithQuery(<Harness initial={[amara, bram]} />);

    const tabs = screen.getByRole('navigation', { name: /Open patient charts/ });
    await user.click(within(tabs).getByRole('button', { name: /Close chart for Amara Okafor/ }));
    expect(within(tabs).queryByRole('button', { name: /^Amara Okafor/ })).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Find patient' }));
    await user.click(await screen.findByRole('button', { name: /^Chiara Rossi/ }));

    expect(screen.queryByText(/You can have 2 charts open at once/)).not.toBeInTheDocument();
    expect(within(tabs).getByRole('button', { name: /^Chiara Rossi/ })).toBeInTheDocument();
  });

  it('shows the registry when no chart is open', () => {
    renderWithQuery(<Harness />);
    expect(screen.getByRole('heading', { name: 'Patient registry' })).toBeInTheDocument();
    expect(screen.queryByRole('navigation', { name: /Open patient charts/ })).not.toBeInTheDocument();
  });
});
