import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { AppointmentDesk } from './AppointmentDesk';
import { ApiError } from '../../lib/api/client';
import type { AppointmentSummary, PatientSummary } from '../../lib/api/types';
import { fakeClient, renderWithQuery } from '../../test/render';

const patient: PatientSummary = {
  id: 'p-1', displayName: 'Amara Okafor', birthDate: '1984-03-12',
  administrativeGender: 'female', active: true, identifier: 'MRN-1', duplicateScore: 0,
};

function appointment(overrides: Partial<AppointmentSummary> = {}): AppointmentSummary {
  const start = new Date();
  start.setHours(10, 0, 0, 0);
  const end = new Date(start.getTime() + 30 * 60_000);
  return {
    id: 'ap-1', patientId: 'p-1', practitionerId: 'pr-9', locationId: null,
    status: 'booked', service: 'Diabetes review', start: start.toISOString(), end: end.toISOString(),
    ...overrides,
  };
}

describe('appointment desk', () => {
  it('checks in a booked appointment', async () => {
    const user = userEvent.setup();
    const post = vi.fn().mockResolvedValue({});
    const { client } = fakeClient({ get: vi.fn().mockResolvedValue([appointment()]), post });
    renderWithQuery(<AppointmentDesk client={client} patient={patient} canManageSchedules={false} />);

    const checkIn = await screen.findAllByRole('button', { name: 'Check in' });
    await user.click(checkIn[0]);
    await waitFor(() => expect(post).toHaveBeenCalledWith('/api/v1/appointments/ap-1/arrive?patientId=p-1'));
  });

  it('offers completion once the patient has arrived, and never check-in again', async () => {
    const { client } = fakeClient({ get: vi.fn().mockResolvedValue([appointment({ status: 'arrived' })]) });
    renderWithQuery(<AppointmentDesk client={client} patient={patient} canManageSchedules={false} />);

    expect((await screen.findAllByRole('button', { name: 'Mark complete' })).length).toBeGreaterThan(0);
    expect(screen.queryByRole('button', { name: 'Check in' })).not.toBeInTheDocument();
  });

  it('offers no transitions for a terminal appointment', async () => {
    const { client } = fakeClient({ get: vi.fn().mockResolvedValue([appointment({ status: 'fulfilled' })]) });
    renderWithQuery(<AppointmentDesk client={client} patient={patient} canManageSchedules={false} />);

    await screen.findAllByText('Diabetes review');
    expect(screen.queryByRole('button', { name: 'Check in' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Mark complete' })).not.toBeInTheDocument();
  });

  it('reports a booking conflict from the backend', async () => {
    const user = userEvent.setup();
    const post = vi.fn().mockRejectedValue(new ApiError(409, 'The practitioner already has an appointment'));
    const { client } = fakeClient({ get: vi.fn().mockResolvedValue([]), post });
    renderWithQuery(<AppointmentDesk client={client} patient={patient} canManageSchedules={false} />);

    const booking = screen.getByRole('button', { name: 'Book appointment' }).closest('form')!;
    const field = (name: string) => within(booking).getByLabelText(/./, { selector: `[name="${name}"]` });
    await user.type(field('practitionerId'), 'pr-9');
    await user.type(field('start'), '2026-08-04T10:00');
    await user.type(field('end'), '2026-08-04T10:30');
    await user.type(field('serviceCode'), '11429006');
    await user.type(field('serviceDisplay'), 'Consultation');
    await user.click(screen.getByRole('button', { name: 'Book appointment' }));

    expect(await screen.findByText(/already has an appointment/)).toBeInTheDocument();
  });
});
