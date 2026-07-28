import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { AppointmentSummary, PatientSummary, SlotSummary } from '../../lib/api/types';
import { formatDateTime, formatTime, isToday } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';

/**
 * Front-desk appointment workflow.
 *
 * The desk needs the arrival lifecycle, not just a list: an appointment moves
 * from booked to arrived at check-in, and to fulfilled when the visit is done.
 * Those transitions are enforced by the backend, so the desk only offers the
 * transition that is actually valid for the current status.
 */
export function AppointmentDesk({ client, patient, canManageSchedules }: {
  client: ApiClient; patient: PatientSummary; canManageSchedules: boolean;
}) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const [availability, setAvailability] = useState<SlotSummary[]>();
  const [selectedSlot, setSelectedSlot] = useState<SlotSummary>();

  const appointments = useQuery({
    queryKey: ['appointments', patient.id],
    queryFn: ({ signal }) => client.get<AppointmentSummary[]>(
      `/api/v1/appointments?patientId=${encodeURIComponent(patient.id)}`, signal),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['appointments'] });

  const transition = useMutation({
    mutationFn: ({ id, verb }: { id: string; verb: 'arrive' | 'fulfill' | 'cancel' }) =>
      client.post(`/api/v1/appointments/${encodeURIComponent(id)}/${verb}?patientId=${encodeURIComponent(patient.id)}`),
    onSuccess: invalidate,
  });

  const book = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/appointments', body),
    onSuccess: () => { invalidate(); setSelectedSlot(undefined); setAvailability(undefined); },
  });

  const lookup = useMutation({
    mutationFn: ({ practitionerId, start, end }: { practitionerId: string; start: string; end: string }) =>
      client.get<SlotSummary[]>(`/api/v1/appointments/availability?practitionerId=${encodeURIComponent(practitionerId)}`
        + `&start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`),
    onSuccess: setAvailability,
  });

  function searchAvailability(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const day = String(form.get('day') ?? '');
    if (!day) return;
    lookup.mutate({
      practitionerId: String(form.get('practitionerId') ?? ''),
      start: new Date(`${day}T00:00:00`).toISOString(),
      end: new Date(`${day}T23:59:59`).toISOString(),
    });
  }

  function bookAppointment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    book.mutate({
      patientId: patient.id,
      practitionerId: selectedSlot?.practitionerId ?? String(form.get('practitionerId') ?? ''),
      locationId: selectedSlot?.locationId ?? (String(form.get('locationId') ?? '') || null),
      slotId: selectedSlot?.id ?? null,
      start: selectedSlot ? selectedSlot.start : new Date(String(form.get('start'))).toISOString(),
      end: selectedSlot ? selectedSlot.end : new Date(String(form.get('end'))).toISOString(),
      serviceCode: String(form.get('serviceCode') ?? ''),
      serviceDisplay: String(form.get('serviceDisplay') ?? ''),
      comment: String(form.get('comment') ?? '') || null,
    });
  }

  const today = (appointments.data ?? []).filter(appointment => isToday(appointment.start));

  return (
    <div className="stack">
      <Panel title={t('schedule.todayTitle')} level={1} subtitle={t('schedule.todaySubtitle')}>
        <ErrorNotice error={appointments.error} />
        <ErrorNotice error={transition.error} />
        <DataTable caption={t('schedule.todayTitle')} rows={today} rowKey={row => row.id} empty={t('schedule.noneToday')}
          columns={[
            { header: t('table.time'), cell: row => formatTime(row.start, i18n.language) },
            { header: t('table.service'), cell: row => <span className="primary-cell">{row.service ?? ''}</span> },
            { header: t('table.practitioner'), cell: row => row.practitionerId ?? '' },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            { header: t('table.actions'), align: 'end', cell: row => <DeskActions row={row} transition={transition} /> },
          ]} />
      </Panel>

      <Panel title={t('schedule.upcomingTitle')}>
        <DataTable caption={t('schedule.upcomingTitle')} rows={appointments.data ?? []} rowKey={row => row.id}
          empty={t('chart.empty')}
          columns={[
            { header: t('table.when'), cell: row => formatDateTime(row.start, i18n.language) },
            { header: t('table.service'), cell: row => <span className="primary-cell">{row.service ?? ''}</span> },
            { header: t('table.location'), cell: row => row.locationId ?? '' },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            { header: t('table.actions'), align: 'end', cell: row => <DeskActions row={row} transition={transition} /> },
          ]} />
      </Panel>

      <div className="grid-2">
        <Panel title={t('schedule.availabilityTitle')} subtitle={t('schedule.availabilitySubtitle')}>
          <form onSubmit={searchAvailability}>
            <div className="form-grid">
              <Field label={t('clinical.practitionerId')} required>{id => <input id={id} name="practitionerId" required />}</Field>
              <Field label={t('schedule.day')} required>{id => <input id={id} name="day" type="date" required />}</Field>
            </div>
            <div className="form-actions"><Button type="submit" disabled={lookup.isPending}>{t('schedule.findSlots')}</Button></div>
          </form>
          <ErrorNotice error={lookup.error} />
          {availability ? (
            <DataTable caption={t('schedule.availabilityTitle')} rows={availability} rowKey={row => row.id}
              empty={t('schedule.noSlots')}
              columns={[
                { header: t('table.time'), cell: row => `${formatTime(row.start, i18n.language)} - ${formatTime(row.end, i18n.language)}` },
                { header: t('table.service'), cell: row => row.serviceDisplay ?? '' },
                { header: t('table.actions'), align: 'end', cell: row =>
                  <Button className="quiet-button" onClick={() => setSelectedSlot(row)}>{t('schedule.useSlot')}</Button> },
              ]} />
          ) : null}
        </Panel>

        <Panel title={t('schedule.bookTitle')}>
          {selectedSlot ? (
            <Notice tone="info">
              {t('schedule.slotSelected', {
                time: `${formatTime(selectedSlot.start, i18n.language)} - ${formatTime(selectedSlot.end, i18n.language)}`,
              })}
            </Notice>
          ) : null}
          <form onSubmit={bookAppointment}>
            <div className="form-grid">
              {selectedSlot ? null : (
                <>
                  <Field label={t('clinical.practitionerId')} required>{id => <input id={id} name="practitionerId" required />}</Field>
                  <Field label={t('schedule.location')}>{id => <input id={id} name="locationId" />}</Field>
                  <Field label={t('schedule.start')} required>{id => <input id={id} name="start" type="datetime-local" required />}</Field>
                  <Field label={t('schedule.end')} required>{id => <input id={id} name="end" type="datetime-local" required />}</Field>
                </>
              )}
              <Field label={t('clinical.code')} required hint={t('schedule.serviceCodeHint')}>
                {(id, describedBy) => <input id={id} name="serviceCode" required aria-describedby={describedBy} />}
              </Field>
              <Field label={t('schedule.service')} required>{id => <input id={id} name="serviceDisplay" required />}</Field>
              <Field label={t('clinical.note')}>{id => <input id={id} name="comment" />}</Field>
            </div>
            <div className="form-actions">
              <Button type="submit" disabled={book.isPending}>{t('schedule.book')}</Button>
              {selectedSlot ? <Button className="quiet-button" onClick={() => setSelectedSlot(undefined)}>{t('schedule.clearSlot')}</Button> : null}
            </div>
          </form>
          <ErrorNotice error={book.error} />
          {canManageSchedules ? <p className="field-hint">{t('schedule.adminHint')}</p> : null}
        </Panel>
      </div>
    </div>
  );
}

/** Only the transitions the backend accepts for the current status are offered. */
function DeskActions({ row, transition }: {
  row: AppointmentSummary;
  transition: { mutate: (input: { id: string; verb: 'arrive' | 'fulfill' | 'cancel' }) => void; isPending: boolean };
}) {
  const { t } = useTranslation();
  const terminal = ['fulfilled', 'cancelled', 'noshow'];
  if (terminal.includes(row.status)) return null;
  return (
    <div className="button-row">
      {row.status === 'booked' ? (
        <Button className="quiet-button" disabled={transition.isPending}
          onClick={() => transition.mutate({ id: row.id, verb: 'arrive' })}>{t('schedule.checkIn')}</Button>
      ) : null}
      {row.status === 'arrived' ? (
        <Button className="quiet-button" disabled={transition.isPending}
          onClick={() => transition.mutate({ id: row.id, verb: 'fulfill' })}>{t('schedule.complete')}</Button>
      ) : null}
      <Button className="quiet-button" disabled={transition.isPending}
        onClick={() => transition.mutate({ id: row.id, verb: 'cancel' })}>{t('common.cancel')}</Button>
    </div>
  );
}
