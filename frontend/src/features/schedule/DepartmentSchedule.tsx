import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { ApiClient } from '../../lib/api/client';
import type { AppointmentSummary } from '../../lib/api/types';
import { formatTime } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice } from '../../design/Feedback';
import { dayWindow } from './slots';

/**
 * The front desk day view, across every patient.
 *
 * A patient's own appointments live in their chart. This is the other
 * question: who is coming to this clinic today. It cannot sit behind a patient
 * selection, because the answer is what tells the desk who the patients are.
 */
export function DepartmentSchedule({ client, canAdmit }: { client: ApiClient; canAdmit: boolean }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [practitionerId, setPractitionerId] = useState('');
  const [locationId, setLocationId] = useState('');

  const window = dayWindow(date);
  const parameters = new URLSearchParams({ from: window?.from ?? '', to: window?.to ?? '' });
  if (practitionerId.trim()) parameters.set('practitionerId', practitionerId.trim());
  else if (locationId.trim()) parameters.set('locationId', locationId.trim());

  const appointments = useQuery({
    enabled: Boolean(window),
    queryKey: ['day-schedule', parameters.toString()],
    queryFn: ({ signal }) => client.get<AppointmentSummary[]>(
      `/api/v1/appointments/day?${parameters.toString()}`, signal),
  });

  const transition = useMutation({
    mutationFn: ({ id, patientId, verb }: { id: string; patientId: string; verb: 'arrive' | 'fulfill' | 'cancel' }) =>
      client.post(`/api/v1/appointments/${encodeURIComponent(id)}/${verb}?patientId=${encodeURIComponent(patientId)}`),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['day-schedule'] }),
  });

  const rows = appointments.data ?? [];
  const waiting = rows.filter(row => row.status === 'arrived').length;

  return (
    <Panel title={t('schedule.departmentTitle')} level={1} subtitle={t('schedule.departmentSubtitle')}>
      <div className="form-grid">
        <Field label={t('schedule.day')} required>
          {id => <input id={id} type="date" value={date} onChange={event => setDate(event.target.value)} />}
        </Field>
        <Field label={t('clinical.practitionerId')} hint={t('schedule.filterHint')}>
          {(id, describedBy) => <input id={id} value={practitionerId} aria-describedby={describedBy}
            onChange={event => { setPractitionerId(event.target.value); setLocationId(''); }} />}
        </Field>
        <Field label={t('schedule.location')}>
          {id => <input id={id} value={locationId} disabled={Boolean(practitionerId.trim())}
            onChange={event => setLocationId(event.target.value)} />}
        </Field>
      </div>

      <p className="field-hint">{t('schedule.daySummary', { total: rows.length, waiting })}</p>
      <ErrorNotice error={appointments.error} />
      <ErrorNotice error={transition.error} />

      <DataTable caption={t('schedule.departmentTitle')} rows={rows} rowKey={row => row.id}
        empty={t('schedule.noneToday')}
        columns={[
          { header: t('schedule.start'), cell: row => (
            <span className="primary-cell">{formatTime(row.start, i18n.language)}</span>) },
          { header: t('table.patient'), cell: row => row.patientId ?? '' },
          { header: t('table.practitioner'), cell: row => row.practitionerId ?? '' },
          { header: t('table.service'), cell: row => row.service ?? '' },
          { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
          { header: t('table.actions'), align: 'end', cell: row => (
            <div className="button-row">
              {row.patientId ? (
                <Button className="quiet-button"
                  onClick={() => navigate(`/patients/${encodeURIComponent(row.patientId!)}`)}>
                  {t('schedule.openChart')}
                </Button>
              ) : null}
              {canAdmit && row.patientId && row.status === 'booked' ? (
                <Button className="quiet-button" disabled={transition.isPending}
                  onClick={() => transition.mutate({ id: row.id, patientId: row.patientId!, verb: 'arrive' })}>
                  {t('schedule.checkIn')}
                </Button>
              ) : null}
            </div>
          ) },
        ]} />
    </Panel>
  );
}
