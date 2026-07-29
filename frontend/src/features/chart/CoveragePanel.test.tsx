import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { CoveragePanel } from './CoveragePanel';
import type { CoverageSummary } from '../../lib/api/types';
import { fakeClient, renderWithQuery } from '../../test/render';

const active: CoverageSummary = {
  id: 'cv-1', patientId: 'p-1', status: 'active', typeCode: 'EHCPOL', type: 'Health insurance',
  payor: 'Zorgverzekeraar Noord', payorOrganizationId: 'org-9', relationship: 'self',
  subscriberId: 'POL-88213', groupNumber: 'GRP-4', start: '2026-01-01', end: null,
};

function render(rows: CoverageSummary[]) {
  const api = fakeClient({ get: vi.fn().mockResolvedValue(rows) });
  renderWithQuery(<CoveragePanel client={api.client} patientId="p-1" />);
  return api;
}

describe('CoveragePanel', () => {
  it('lists what the patient holds', async () => {
    render([active]);
    expect(await screen.findByText('Zorgverzekeraar Noord')).toBeInTheDocument();
    expect(screen.getByText('POL-88213')).toBeInTheDocument();
    expect(screen.getByText('No end date')).toBeInTheDocument();
  });

  it('says plainly that this is not an eligibility check', async () => {
    render([]);
    // The distinction matters: nobody should read this as confirmation of payment.
    expect(await screen.findByText(/not an eligibility check/)).toBeInTheDocument();
  });

  it('ends a policy rather than deleting it', async () => {
    const user = userEvent.setup();
    const api = render([active]);

    await user.click(await screen.findByRole('button', { name: 'End coverage' }));

    await waitFor(() => expect(api.fake.post).toHaveBeenCalledWith(
      expect.stringContaining('/api/v1/coverage/cv-1/cancel')));
  });

  it('offers no ending action on coverage that already ended', async () => {
    render([{ ...active, status: 'cancelled', end: '2026-06-30' }]);
    expect(await screen.findByText('Zorgverzekeraar Noord')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'End coverage' })).not.toBeInTheDocument();
  });

  it('sends the recorded policy to the server', async () => {
    const user = userEvent.setup();
    const api = render([]);

    await user.type(await screen.findByLabelText(/^Payer \*/), 'Zorgverzekeraar Noord');
    await user.type(screen.getByLabelText(/Payer organization ID/), 'org-9');
    await user.type(screen.getByLabelText(/^Policy number/), 'POL-88213');
    await user.click(screen.getByRole('button', { name: 'Record coverage' }));

    await waitFor(() => expect(api.fake.post).toHaveBeenCalledWith('/api/v1/coverage',
      expect.objectContaining({
        patientId: 'p-1', payorDisplay: 'Zorgverzekeraar Noord',
        payorOrganizationId: 'org-9', subscriberId: 'POL-88213', relationship: 'self',
      })));
  });
});
