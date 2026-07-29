import { useState, type FormEvent } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { SlotSummary, TermConcept } from '../../lib/api/types';
import { formatTime } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';
import { CodePicker } from '../terminology/CodePicker';
import { buildSlots } from './slots';

/**
 * Creating a practitioner's bookable time.
 *
 * A schedule is defined once and divided into slots of a fixed length. The
 * generated slots are shown before anything is created, because a wrong day or
 * interval would otherwise only become visible after it reached the store.
 */
export function ScheduleManager({ client }: { client: ApiClient }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const [service, setService] = useState<TermConcept>();
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [from, setFrom] = useState('09:00');
  const [to, setTo] = useState('12:00');
  const [minutes, setMinutes] = useState(15);
  const [created, setCreated] = useState<SlotSummary[]>();

  const preview = buildSlots(date, from, to, minutes);

  const create = useMutation({
    mutationFn: (body: unknown) => client.post<SlotSummary[]>('/api/v1/appointments/schedules', body),
    onSuccess: slots => {
      setCreated(slots);
      void queryClient.invalidateQueries({ queryKey: ['availability'] });
    },
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!service || preview.length === 0) return;
    const form = new FormData(event.currentTarget);
    create.mutate({
      practitionerId: String(form.get('practitionerId') ?? ''),
      locationId: String(form.get('locationId') ?? '') || null,
      serviceCode: service.code,
      serviceDisplay: service.display,
      slots: preview,
    });
  }

  return (
    <div className="stack">
      <Panel title={t('schedule.manageTitle')} subtitle={t('schedule.manageSubtitle')}>
        <form onSubmit={submit}>
          <div className="form-grid">
            <Field label={t('clinical.practitionerId')} required>
              {id => <input id={id} name="practitionerId" required />}
            </Field>
            <Field label={t('schedule.location')}>{id => <input id={id} name="locationId" />}</Field>
            <Field label={t('schedule.day')} required>
              {id => <input id={id} type="date" required value={date} onChange={e => setDate(e.target.value)} />}
            </Field>
            <Field label={t('schedule.from')} required>
              {id => <input id={id} type="time" required value={from} onChange={e => setFrom(e.target.value)} />}
            </Field>
            <Field label={t('schedule.to')} required>
              {id => <input id={id} type="time" required value={to} onChange={e => setTo(e.target.value)} />}
            </Field>
            <Field label={t('schedule.slotMinutes')} required>
              {id => <input id={id} type="number" min="5" max="240" step="5" required value={minutes}
                onChange={e => setMinutes(Number(e.target.value))} />}
            </Field>
          </div>

          <CodePicker client={client} domain="procedure" label={t('schedule.service')}
            value={service} onChange={setService} />

          {preview.length > 0
            ? <Notice tone="info">{t('schedule.previewCount', {
                count: preview.length,
                first: formatTime(preview[0].start, i18n.language),
                last: formatTime(preview[preview.length - 1].end, i18n.language),
              })}</Notice>
            : <Notice tone="warning">{t('schedule.previewEmpty')}</Notice>}

          <div className="form-actions">
            <Button type="submit" disabled={!service || preview.length === 0 || create.isPending}>
              {t('schedule.createSlots')}
            </Button>
          </div>
          {!service ? <p className="field-hint">{t('schedule.serviceRequired')}</p> : null}
        </form>
        <ErrorNotice error={create.error} />
      </Panel>

      {created ? (
        <Panel title={t('schedule.createdTitle')}>
          <DataTable caption={t('schedule.createdTitle')} rows={created} rowKey={row => row.id}
            empty={t('chart.empty')}
            columns={[
              { header: t('schedule.start'), cell: row => (
                <span className="primary-cell">{formatTime(row.start, i18n.language)}</span>) },
              { header: t('schedule.end'), cell: row => formatTime(row.end, i18n.language) },
              { header: t('schedule.service'), cell: row => row.serviceDisplay ?? '' },
              { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            ]} />
        </Panel>
      ) : null}
    </div>
  );
}
