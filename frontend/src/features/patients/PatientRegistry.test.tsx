import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { PatientRegistry } from './PatientRegistry';
import { ApiError } from '../../lib/api/client';
import type { PatientSummary } from '../../lib/api/types';
import { fakeClient, renderWithQuery } from '../../test/render';

const patient: PatientSummary = {
  id: 'p-1', displayName: 'Amara Okafor', birthDate: '1984-03-12',
  administrativeGender: 'female', active: true, identifier: 'MRN-1', duplicateScore: 0,
};

async function fillRequiredFields(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText(/Given name/), 'Amara');
  await user.type(screen.getByLabelText(/Family name/), 'Okafor');
  await user.type(screen.getByLabelText(/Birth date/), '1984-03-12');
  await user.selectOptions(screen.getByLabelText(/Administrative gender/), 'female');
}

describe('patient registry', () => {
  it('sends the search term to the patient endpoint', async () => {
    const user = userEvent.setup();
    const get = vi.fn().mockResolvedValue([]);
    const { client } = fakeClient({ get });
    renderWithQuery(<PatientRegistry client={client} onSelect={vi.fn()} />);

    await user.type(screen.getByLabelText(/Search by name/), 'Okafor');
    await waitFor(() => expect(get).toHaveBeenCalledWith('/api/v1/patients?query=Okafor', expect.anything()));
  });

  it('selects a patient from the results', async () => {
    const user = userEvent.setup();
    const onSelect = vi.fn();
    const { client } = fakeClient({ get: vi.fn().mockResolvedValue([patient]) });
    renderWithQuery(<PatientRegistry client={client} onSelect={onSelect} />);

    await user.click(await screen.findByRole('button', { name: /Amara Okafor/ }));
    expect(onSelect).toHaveBeenCalledWith(patient);
  });

  it('checks for duplicates before it will create a record', async () => {
    const user = userEvent.setup();
    const post = vi.fn().mockResolvedValue([]);
    const { client } = fakeClient({ post });
    renderWithQuery(<PatientRegistry client={client} onSelect={vi.fn()} />);

    await fillRequiredFields(user);
    await user.click(screen.getByRole('button', { name: /Check for duplicates/ }));

    await waitFor(() => expect(post).toHaveBeenCalledWith('/api/v1/patients/duplicates', expect.objectContaining({
      givenName: 'Amara', familyName: 'Okafor', birthDate: '1984-03-12', administrativeGender: 'female',
    })));
    // The registration itself must not have been sent yet.
    expect(post).toHaveBeenCalledTimes(1);
  });

  it('warns about likely duplicates and only registers after explicit confirmation', async () => {
    const user = userEvent.setup();
    const post = vi.fn()
      .mockResolvedValueOnce([{ ...patient, id: 'p-9', duplicateScore: 0.92 }])
      .mockResolvedValueOnce(patient);
    const { client } = fakeClient({ post });
    renderWithQuery(<PatientRegistry client={client} onSelect={vi.fn()} />);

    await fillRequiredFields(user);
    await user.click(screen.getByRole('button', { name: /Check for duplicates/ }));

    expect(await screen.findByText(/Found 1 possible existing record/)).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /Not a duplicate, register/ }));
    await waitFor(() => expect(post).toHaveBeenLastCalledWith('/api/v1/patients',
      expect.objectContaining({ confirmedUnique: true })));
  });

  it('surfaces a backend problem detail', async () => {
    const { client } = fakeClient({ get: vi.fn().mockRejectedValue(new ApiError(403, 'Consent denies access')) });
    renderWithQuery(<PatientRegistry client={client} onSelect={vi.fn()} />);
    expect(await screen.findByRole('alert')).toHaveTextContent('Consent denies access');
  });
});
