import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { IdentityPanel } from './IdentityPanel';
import type { PatientSummary } from '../../lib/api/types';
import { fakeClient, renderWithQuery } from '../../test/render';

const surviving: PatientSummary = {
  id: 'p-1', displayName: 'Amara Okafor', birthDate: '1984-03-12',
  administrativeGender: 'female', active: true, identifier: 'MRN-100418', duplicateScore: 0,
};
const duplicate: PatientSummary = {
  id: 'p-9', displayName: 'Amara Okafor', birthDate: '1984-03-12',
  administrativeGender: 'female', active: true, identifier: 'MRN-100999', duplicateScore: 0.94,
};

function client(overrides: Parameters<typeof fakeClient>[0] = {}) {
  return fakeClient({
    get: vi.fn().mockImplementation((path: string) =>
      Promise.resolve(path.includes('/merges') ? [] : [surviving, duplicate])),
    ...overrides,
  });
}

describe('IdentityPanel', () => {
  it('requires an explicit confirmation before merging', async () => {
    const user = userEvent.setup();
    const { client: api, fake } = client();
    renderWithQuery(<IdentityPanel client={api} patient={surviving} canMerge canUnmerge={false} />);

    await user.type(screen.getByLabelText(/Find the duplicate record/), 'Okafor');
    await user.click(await screen.findByRole('button', { name: 'Select' }));

    // Selecting a record must not merge anything on its own.
    expect(fake.post).not.toHaveBeenCalled();
    expect(screen.getByText(/Confirm the merge direction/)).toBeInTheDocument();
    expect(screen.getByText(/This record is retired/)).toBeInTheDocument();
    expect(screen.getByText(/This record survives/)).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Merge these records' }));
    // The selected duplicate is the source, the open chart is the target.
    expect(fake.post).toHaveBeenCalledWith('/api/v1/patients/p-9/merge/p-1');
  });

  it('never offers the open record as its own merge candidate', async () => {
    const user = userEvent.setup();
    const { client: api } = client();
    renderWithQuery(<IdentityPanel client={api} patient={surviving} canMerge canUnmerge={false} />);

    await user.type(screen.getByLabelText(/Find the duplicate record/), 'Okafor');
    // Both patients share a name, so only the duplicate may be selectable.
    expect(await screen.findAllByRole('button', { name: 'Select' })).toHaveLength(1);
  });

  it('hides merging entirely from a user without the capability', () => {
    const { client: api } = client();
    renderWithQuery(<IdentityPanel client={api} patient={surviving} canMerge={false} canUnmerge={false} />);
    expect(screen.queryByText(/Merge a duplicate record/)).not.toBeInTheDocument();
  });

  it('offers reversal only to a user who may unmerge', async () => {
    const merged = [{
      id: 'm-1', sourcePatientId: 'p-9', targetPatientId: 'p-1', status: 'MERGED',
      resourcesRepointed: 12, mergedAt: '2026-07-20T09:00:00Z', unmergedAt: null,
    }];
    const api = fakeClient({
      get: vi.fn().mockImplementation((path: string) =>
        Promise.resolve(path.includes('/merges') ? merged : [])),
    });
    renderWithQuery(<IdentityPanel client={api.client} patient={surviving} canMerge={false} canUnmerge={false} />);
    expect(await screen.findByText(/Only an administrator can reverse a merge/)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Reverse merge' })).not.toBeInTheDocument();
  });
});
