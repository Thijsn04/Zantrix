import { type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { VitalSummary } from '../../lib/api/types';
import { formatDateTime } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { ErrorNotice, Notice } from '../../design/Feedback';
import { useClinicalContext } from './clinicalContext';

/**
 * The Milestone 1 vital set, as a rooming form rather than one measurement at a
 * time. The backend accepts the whole set in one transaction and derives BMI
 * from height and weight, so the form mirrors that: fill in what was measured,
 * submit once.
 */
const VITALS = [
  { loincCode: '8480-6', unit: 'mm[Hg]', labelKey: 'vitals.systolic' },
  { loincCode: '8462-4', unit: 'mm[Hg]', labelKey: 'vitals.diastolic' },
  { loincCode: '8867-4', unit: '/min', labelKey: 'vitals.heartRate' },
  { loincCode: '9279-1', unit: '/min', labelKey: 'vitals.respiratoryRate' },
  { loincCode: '8310-5', unit: 'Cel', labelKey: 'vitals.temperature' },
  { loincCode: '2708-6', unit: '%', labelKey: 'vitals.oxygenSaturation' },
  { loincCode: '29463-7', unit: 'kg', labelKey: 'vitals.weight' },
  { loincCode: '8302-2', unit: 'cm', labelKey: 'vitals.height' },
] as const;

export function VitalsPanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const { encounterId, practitionerId, ready } = useClinicalContext();

  const vitals = useQuery({
    queryKey: ['vitals', patientId],
    queryFn: ({ signal }) => client.get<VitalSummary[]>(
      `/api/v1/vitals?patientId=${encodeURIComponent(patientId)}`, signal),
  });

  const record = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/vitals', body),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['vitals'] }),
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const measurements = VITALS
      .map(vital => ({ vital, raw: String(form.get(vital.loincCode) ?? '').trim() }))
      .filter(entry => entry.raw !== '')
      .map(entry => ({ loincCode: entry.vital.loincCode, value: Number(entry.raw), unit: entry.vital.unit, note: null }));
    if (!measurements.length) return;
    record.mutate({ patientId, encounterId, performerId: practitionerId, observedAt: new Date().toISOString(), measurements });
    event.currentTarget.reset();
  }

  return (
    <div className="stack">
      <Panel title={t('vitals.recordTitle')} subtitle={t('vitals.recordSubtitle')}>
        <form onSubmit={submit}>
          <div className="form-grid">
            {VITALS.map(vital => (
              <Field key={vital.loincCode} label={`${t(vital.labelKey)} (${vital.unit})`}>
                {id => <input id={id} name={vital.loincCode} type="number" step="any" inputMode="decimal" />}
              </Field>
            ))}
          </div>
          <div className="form-actions">
            <Button type="submit" disabled={!ready || record.isPending}>{t('vitals.record')}</Button>
          </div>
          {!ready ? <p className="field-hint">{t('clinical.contextRequired')}</p> : null}
        </form>
        <Notice tone="info">{t('vitals.bmiHint')}</Notice>
        <ErrorNotice error={record.error} />
      </Panel>

      <Panel title={t('chart.vitals')}>
        <ErrorNotice error={vitals.error} />
        <DataTable caption={t('chart.vitals')} rows={vitals.data ?? []} rowKey={row => row.id} empty={t('chart.empty')}
          columns={[
            { header: t('table.measurement'), cell: row => <span className="primary-cell">{row.display}</span> },
            { header: t('table.value'), align: 'end', cell: row => `${row.value} ${row.unit}` },
            { header: t('table.observed'), cell: row => formatDateTime(row.observedAt, i18n.language) },
          ]} />
      </Panel>
    </div>
  );
}
