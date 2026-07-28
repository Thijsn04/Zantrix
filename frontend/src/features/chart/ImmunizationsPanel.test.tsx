import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ImmunizationsPanel } from './ImmunizationsPanel';
import { ClinicalContextProvider } from './ClinicalContext';
import type { ImmunizationSummary } from '../../lib/api/types';
import { fakeClient, renderWithQuery } from '../../test/render';

const given: ImmunizationSummary = {
  id: 'im-1', patientId: 'p-1', vaccineSystem: 'http://snomed.info/sct', vaccineCode: '871875004',
  vaccine: 'Influenza vaccine', status: 'completed', occurrenceDate: '2026-03-02',
  lotNumber: 'FL-2291', site: 'Left deltoid', doseNumber: 1, statusReason: null,
};
const corrected: ImmunizationSummary = {
  ...given, id: 'im-2', status: 'entered-in-error', statusReason: 'Recorded on the wrong patient',
};

function render(rows: ImmunizationSummary[]) {
  const api = fakeClient({ get: vi.fn().mockResolvedValue(rows) });
  renderWithQuery(<ClinicalContextProvider>
    <ImmunizationsPanel client={api.client} patientId="p-1" />
  </ClinicalContextProvider>);
  return api;
}

describe('ImmunizationsPanel', () => {
  it('lists what was given', async () => {
    render([given]);
    expect(await screen.findByText('Influenza vaccine')).toBeInTheDocument();
    expect(screen.getByText('FL-2291')).toBeInTheDocument();
  });

  it('requires a documented reason before correcting an entry', async () => {
    const user = userEvent.setup();
    const api = render([given]);
    const prompt = vi.spyOn(window, 'prompt').mockReturnValue(null);

    await user.click(await screen.findByRole('button', { name: 'Entered in error' }));

    // Dismissing the prompt must not correct the record.
    expect(prompt).toHaveBeenCalled();
    expect(api.fake.post).not.toHaveBeenCalled();
    prompt.mockRestore();
  });

  it('sends the reason with the correction', async () => {
    const user = userEvent.setup();
    const api = render([given]);
    const prompt = vi.spyOn(window, 'prompt').mockReturnValue('Recorded on the wrong patient');

    await user.click(await screen.findByRole('button', { name: 'Entered in error' }));

    await waitFor(() => expect(api.fake.post).toHaveBeenCalledWith(
      expect.stringContaining('/api/v1/immunizations/im-1/entered-in-error')));
    expect(api.fake.post.mock.calls[0][0]).toContain('reason=Recorded%20on%20the%20wrong%20patient');
    prompt.mockRestore();
  });

  it('shows a corrected entry with its reason and offers no further correction', async () => {
    render([corrected]);
    expect(await screen.findByText('Recorded on the wrong patient')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Entered in error' })).not.toBeInTheDocument();
  });

  it('states that a recorded dose is permanent', async () => {
    render([]);
    expect(await screen.findByText(/never deleted/)).toBeInTheDocument();
  });
});
