import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { ImmunizationSummary, TermConcept } from '../../lib/api/types';
import { formatDate } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';
import { CodePicker } from '../terminology/CodePicker';
import { useClinicalContext } from './clinicalContext';

const SNOMED = 'http://snomed.info/sct';

/**
 * Vaccination history.
 *
 * A recorded dose is never removed. Correcting a mistaken entry marks it
 * entered in error and leaves it visible, because a record that quietly loses a
 * dose is more dangerous than one that shows a correction.
 */
export function ImmunizationsPanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const { encounterId, practitionerId, ready } = useClinicalContext();
  const [vaccine, setVaccine] = useState<TermConcept>();
  const [showCorrected, setShowCorrected] = useState(false);

  const immunizations = useQuery({
    queryKey: ['immunizations', patientId, showCorrected],
    queryFn: ({ signal }) => client.get<ImmunizationSummary[]>(
      `/api/v1/immunizations?patientId=${encodeURIComponent(patientId)}&includeEnteredInError=${showCorrected}`,
      signal),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['immunizations'] });
  const record = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/immunizations', body),
    onSuccess: () => { invalidate(); setVaccine(undefined); },
  });
  const correct = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => client.post(
      `/api/v1/immunizations/${encodeURIComponent(id)}/entered-in-error`
      + `?patientId=${encodeURIComponent(patientId)}&reason=${encodeURIComponent(reason)}`),
    onSuccess: invalidate,
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!vaccine) return;
    const form = new FormData(event.currentTarget);
    const dose = String(form.get('doseNumber') ?? '').trim();
    record.mutate({
      patientId, encounterId, performerId: practitionerId,
      vaccineSystem: SNOMED, vaccineCode: vaccine.code, vaccineDisplay: vaccine.display,
      occurrenceDate: String(form.get('occurrenceDate') ?? ''),
      lotNumber: String(form.get('lotNumber') ?? '') || null,
      site: String(form.get('site') ?? '') || null,
      route: String(form.get('route') ?? '') || null,
      doseNumber: dose === '' ? null : Number(dose),
      note: String(form.get('note') ?? '') || null,
    });
    event.currentTarget.reset();
  }

  return (
    <div className="stack">
      <Panel title={t('chart.immunizations')}
        actions={<Button className="quiet-button" onClick={() => setShowCorrected(!showCorrected)}>
          {showCorrected ? t('immunizations.hideCorrected') : t('immunizations.showCorrected')}</Button>}>
        <ErrorNotice error={immunizations.error} />
        <ErrorNotice error={correct.error} />
        <DataTable caption={t('chart.immunizations')} rows={immunizations.data ?? []} rowKey={row => row.id}
          empty={t('immunizations.none')}
          columns={[
            { header: t('immunizations.vaccine'), cell: row => <span className="primary-cell">{row.vaccine}</span> },
            { header: t('immunizations.given'), cell: row => formatDate(row.occurrenceDate, i18n.language) },
            { header: t('immunizations.dose'), align: 'end', cell: row => row.doseNumber ?? '' },
            { header: t('immunizations.lot'), cell: row => row.lotNumber ?? '' },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status ?? ''} /> },
            { header: t('table.actions'), align: 'end', cell: row => row.status === 'completed' ? (
              <Button className="quiet-button" disabled={correct.isPending}
                onClick={() => {
                  const reason = window.prompt(t('immunizations.correctionPrompt'));
                  if (reason) correct.mutate({ id: row.id, reason });
                }}>{t('immunizations.markError')}</Button>
            ) : row.statusReason ? <span className="field-hint">{row.statusReason}</span> : null },
          ]} />
      </Panel>

      <Panel title={t('immunizations.recordTitle')} subtitle={t('immunizations.recordSubtitle')}>
        <form onSubmit={submit}>
          <CodePicker client={client} domain="procedure" label={t('immunizations.vaccine')}
            value={vaccine} onChange={setVaccine} />
          <div className="form-grid">
            <Field label={t('immunizations.given')} required>
              {id => <input id={id} name="occurrenceDate" type="date" required
                max={new Date().toISOString().slice(0, 10)} />}
            </Field>
            <Field label={t('immunizations.dose')}>{id => <input id={id} name="doseNumber" type="number" min="1" />}</Field>
            <Field label={t('immunizations.lot')}>{id => <input id={id} name="lotNumber" />}</Field>
            <Field label={t('immunizations.site')}>{id => <input id={id} name="site" />}</Field>
            <Field label={t('immunizations.route')}>{id => <input id={id} name="route" />}</Field>
            <Field label={t('clinical.note')}>{id => <input id={id} name="note" />}</Field>
          </div>
          <Notice tone="info">{t('immunizations.permanentNote')}</Notice>
          <div className="form-actions">
            <Button type="submit" disabled={!vaccine || !ready || record.isPending}>{t('immunizations.record')}</Button>
          </div>
          {!ready ? <p className="field-hint">{t('clinical.contextRequired')}</p> : null}
        </form>
        <ErrorNotice error={record.error} />
      </Panel>
    </div>
  );
}
