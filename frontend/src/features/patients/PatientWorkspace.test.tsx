import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { PatientWorkspace } from './PatientWorkspace';
import { MAX_OPEN_PATIENTS } from '../../app/useOpenCharts';
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
const everyone = [amara, bram, chiara];

function api() {
  return fakeClient({
    get: vi.fn().mockImplementation((path: string) => {
      const single = /\/api\/v1\/patients\/([^?/]+)$/.exec(path);
      if (single) return Promise.resolve(everyone.find(value => value.id === single[1]));
      return Promise.resolve(everyone);
    }),
  });
}

/** Shows the current URL so deep linking and refusal can be asserted. */
function Location() {
  return <span data-testid="path">{useLocation().pathname}</span>;
}

function renderAt(initial: string) {
  const { client } = api();
  return renderWithQuery(
    <MemoryRouter initialEntries={[initial]}>
      <Location />
      <Routes>
        <Route path="/patients/:patientId/:section?" element={<PatientWorkspace client={client} user={physician} />} />
        <Route path="/patients" element={<PatientWorkspace client={client} user={physician} />} />
      </Routes>
    </MemoryRouter>,
  );
}

const tabs = () => screen.getByRole('navigation', { name: /Open patient charts/ });

describe('open patient charts', () => {
  it('caps the number of charts a clinician can hold open at two', () => {
    expect(MAX_OPEN_PATIENTS).toBe(2);
  });

  it('shows the registry when no chart is open', async () => {
    renderAt('/patients');
    expect(await screen.findByRole('heading', { name: 'Patient registry' })).toBeInTheDocument();
    expect(screen.queryByRole('navigation', { name: /Open patient charts/ })).not.toBeInTheDocument();
  });

  it('opens a chart from a deep link, without prior shell state', async () => {
    renderAt('/patients/p-2');
    // A fresh window knows only the id, so the patient is fetched before the chart renders.
    expect(await screen.findByRole('button', { name: /^Bram de Vries/ })).toHaveAttribute('aria-current', 'page');
  });

  it('honours the chart section in the URL', async () => {
    renderAt('/patients/p-1/vitals');
    expect(await screen.findByRole('tab', { name: 'Vitals' })).toHaveAttribute('aria-selected', 'true');
  });

  it('falls back to the snapshot for an unknown section rather than rendering nothing', async () => {
    renderAt('/patients/p-1/not-a-section');
    expect(await screen.findByRole('tab', { name: 'Snapshot' })).toHaveAttribute('aria-selected', 'true');
  });

  it('keeps both charts open and switches between them', async () => {
    const user = userEvent.setup();
    renderAt('/patients/p-1');
    await screen.findByRole('button', { name: /^Amara Okafor/ });

    await user.click(screen.getByRole('button', { name: 'Find patient' }));
    await user.click(await screen.findByRole('button', { name: /^Bram de Vries/ }));

    expect(within(tabs()).getByRole('button', { name: /^Bram de Vries/ })).toHaveAttribute('aria-current', 'page');
    // The first chart is still open, only the visible one changed.
    expect(within(tabs()).getByRole('button', { name: /^Amara Okafor/ })).toBeInTheDocument();
    expect(screen.getByTestId('path')).toHaveTextContent('/patients/p-2');
  });

  it('refuses a third chart instead of silently closing one', async () => {
    const user = userEvent.setup();
    renderAt('/patients/p-1');
    await screen.findByRole('button', { name: /^Amara Okafor/ });

    await user.click(screen.getByRole('button', { name: 'Find patient' }));
    await user.click(await screen.findByRole('button', { name: /^Bram de Vries/ }));
    await user.click(screen.getByRole('button', { name: 'Find patient' }));
    await user.click(await screen.findByRole('button', { name: /^Chiara Rossi/ }));

    expect(screen.getByTestId('path')).toHaveTextContent('/patients');
    expect(within(tabs()).queryByRole('button', { name: /^Chiara Rossi/ })).not.toBeInTheDocument();
    expect(within(tabs()).getByRole('button', { name: /^Amara Okafor/ })).toBeInTheDocument();
    expect(within(tabs()).getByRole('button', { name: /^Bram de Vries/ })).toBeInTheDocument();
  });

  it('frees a slot when a chart is closed', async () => {
    const user = userEvent.setup();
    renderAt('/patients/p-1');
    await screen.findByRole('button', { name: /^Amara Okafor/ });

    await user.click(screen.getByRole('button', { name: 'Find patient' }));
    await user.click(await screen.findByRole('button', { name: /^Bram de Vries/ }));

    // At the limit, so closing one has to make room for another.
    await user.click(within(tabs()).getByRole('button', { name: /Close chart for Amara Okafor/ }));
    expect(within(tabs()).queryByRole('button', { name: /^Amara Okafor/ })).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Find patient' }));
    await user.click(await screen.findByRole('button', { name: /^Chiara Rossi/ }));

    expect(within(tabs()).getByRole('button', { name: /^Chiara Rossi/ })).toBeInTheDocument();
    expect(screen.getByTestId('path')).toHaveTextContent('/patients/p-3');
  });
});
