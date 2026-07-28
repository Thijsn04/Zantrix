import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { AllergySummary, TermConcept } from '../../lib/api/types';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice } from '../../design/Feedback';
import { CodePicker } from '../terminology/CodePicker';
import { useClinicalContext } from './clinicalContext';

const SNOMED = 'http://snomed.info/sct';

export function AllergiesPanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const { encounterId, ready } = useClinicalContext();
  const [substance, setSubstance] = useState<TermConcept>();
  const [manifestation, setManifestation] = useState<TermConcept>();
  const [includeInactive, setIncludeInactive] = useState(false);

  const allergies = useQuery({
    queryKey: ['allergies', patientId, includeInactive],
    queryFn: ({ signal }) => client.get<AllergySummary[]>(
      `/api/v1/allergies?patientId=${encodeURIComponent(patientId)}&includeInactive=${includeInactive}`, signal),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['allergies'] });
  const add = useMutation({ mutationFn: (body: unknown) => client.post('/api/v1/allergies', body),
    onSuccess: () => { invalidate(); setSubstance(undefined); setManifestation(undefined); } });
  const inactivate = useMutation({
    mutationFn: ({ id, enteredInError }: { id: string; enteredInError: boolean }) =>
      client.post(`/api/v1/allergies/${encodeURIComponent(id)}/inactivate`
        + `?patientId=${encodeURIComponent(patientId)}&enteredInError=${enteredInError}`),
    onSuccess: invalidate });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!substance) return;
    const form = new FormData(event.currentTarget);
    add.mutate({
      patientId, encounterId,
      substanceSystem: SNOMED, substanceCode: substance.code, substanceDisplay: substance.display,
      category: String(form.get('category') ?? 'medication'),
      criticality: String(form.get('criticality') ?? 'unable-to-assess'),
      manifestationSystem: manifestation ? SNOMED : null,
      manifestationCode: manifestation?.code ?? null,
      manifestationDisplay: manifestation?.display ?? null,
      reactionSeverity: String(form.get('reactionSeverity') ?? '') || null,
      note: String(form.get('note') ?? '') || null,
    });
  }

  return (
    <div className="stack">
      <Panel title={t('chart.allergies')}
        actions={<Button className="quiet-button" onClick={() => setIncludeInactive(!includeInactive)}>
          {includeInactive ? t('allergies.hideInactive') : t('allergies.showInactive')}</Button>}>
        <ErrorNotice error={allergies.error} />
        <ErrorNotice error={inactivate.error} />
        <DataTable caption={t('chart.allergies')} rows={allergies.data ?? []} rowKey={row => row.id}
          empty={t('patient.noKnownAllergies')}
          columns={[
            { header: t('table.substance'), cell: row => <span className="primary-cell">{row.substance}</span> },
            { header: t('table.criticality'), cell: row => <StatusBadge status={row.criticality} /> },
            { header: t('table.reaction'), cell: row => row.reaction ?? '' },
            { header: t('table.status'), cell: row => <StatusBadge status={row.clinicalStatus} /> },
            { header: t('table.actions'), align: 'end', cell: row => row.clinicalStatus === 'active' ? (
              <div className="button-row">
                <Button className="quiet-button" disabled={inactivate.isPending}
                  onClick={() => inactivate.mutate({ id: row.id, enteredInError: false })}>{t('allergies.inactivate')}</Button>
                <Button className="quiet-button" disabled={inactivate.isPending}
                  onClick={() => inactivate.mutate({ id: row.id, enteredInError: true })}>{t('allergies.enteredInError')}</Button>
              </div>
            ) : null },
          ]} />
      </Panel>

      <Panel title={t('allergies.addTitle')}>
        <form onSubmit={submit}>
          <CodePicker client={client} domain="substance" label={t('allergies.substanceLabel')}
            value={substance} onChange={setSubstance} />
          <div className="form-grid">
            <Field label={t('allergies.category')} required>
              {id => <select id={id} name="category" defaultValue="medication">
                <option value="medication">{t('allergies.categoryMedication')}</option>
                <option value="food">{t('allergies.categoryFood')}</option>
                <option value="environment">{t('allergies.categoryEnvironment')}</option>
                <option value="biologic">{t('allergies.categoryBiologic')}</option>
              </select>}
            </Field>
            <Field label={t('allergies.criticality')} required>
              {id => <select id={id} name="criticality" defaultValue="unable-to-assess">
                <option value="low">{t('allergies.criticalityLow')}</option>
                <option value="high">{t('allergies.criticalityHigh')}</option>
                <option value="unable-to-assess">{t('allergies.criticalityUnknown')}</option>
              </select>}
            </Field>
            <Field label={t('allergies.severity')}>
              {id => <select id={id} name="reactionSeverity" defaultValue="">
                <option value="">{t('common.none')}</option>
                <option value="mild">{t('allergies.severityMild')}</option>
                <option value="moderate">{t('allergies.severityModerate')}</option>
                <option value="severe">{t('allergies.severitySevere')}</option>
              </select>}
            </Field>
            <Field label={t('clinical.note')}>{id => <input id={id} name="note" />}</Field>
          </div>
          <CodePicker client={client} domain="clinical-finding" label={t('allergies.manifestationLabel')}
            value={manifestation} onChange={setManifestation} />
          <div className="form-actions">
            <Button type="submit" disabled={!substance || !ready || add.isPending}>{t('common.save')}</Button>
          </div>
          {!ready ? <p className="field-hint">{t('clinical.contextRequired')}</p> : null}
        </form>
        <ErrorNotice error={add.error} />
      </Panel>
    </div>
  );
}
