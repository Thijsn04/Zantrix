import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { ProblemSummary, TermConcept } from '../../lib/api/types';
import { formatDate } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice } from '../../design/Feedback';
import { CodePicker } from '../terminology/CodePicker';
import { useClinicalContext } from './clinicalContext';

const SNOMED = 'http://snomed.info/sct';

export function ProblemsPanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const { encounterId, ready } = useClinicalContext();
  const [concept, setConcept] = useState<TermConcept>();
  const [showResolved, setShowResolved] = useState(false);

  const problems = useQuery({
    queryKey: ['problems', patientId, showResolved],
    queryFn: ({ signal }) => client.get<ProblemSummary[]>(
      `/api/v1/problems?patientId=${encodeURIComponent(patientId)}&includeResolved=${showResolved}`, signal),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['problems'] });
  const add = useMutation({ mutationFn: (body: unknown) => client.post('/api/v1/problems', body),
    onSuccess: () => { invalidate(); setConcept(undefined); } });
  const resolve = useMutation({
    mutationFn: (id: string) => client.post(`/api/v1/problems/${encodeURIComponent(id)}/resolve?patientId=${encodeURIComponent(patientId)}`),
    onSuccess: invalidate });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!concept) return;
    const form = new FormData(event.currentTarget);
    add.mutate({
      patientId, encounterId, codeSystem: SNOMED, code: concept.code, display: concept.display,
      onsetDate: String(form.get('onsetDate') ?? '') || null, note: String(form.get('note') ?? '') || null,
    });
  }

  return (
    <div className="stack">
      <Panel title={t('chart.problems')}
        actions={<Button className="quiet-button" onClick={() => setShowResolved(!showResolved)}>
          {showResolved ? t('chart.hideResolved') : t('chart.showResolved')}</Button>}>
        <ErrorNotice error={problems.error} />
        <ErrorNotice error={resolve.error} />
        <DataTable caption={t('chart.problems')} rows={problems.data ?? []} rowKey={row => row.id} empty={t('chart.empty')}
          columns={[
            { header: t('table.problem'), cell: row => <span className="primary-cell">{row.display}</span> },
            { header: t('table.code'), cell: row => row.code },
            { header: t('table.onset'), cell: row => formatDate(row.onsetDate, i18n.language) },
            { header: t('table.status'), cell: row => <StatusBadge status={row.clinicalStatus} /> },
            { header: t('table.actions'), align: 'end', cell: row => row.clinicalStatus === 'active'
              ? <Button className="quiet-button" disabled={resolve.isPending}
                  onClick={() => resolve.mutate(row.id)}>{t('actions.resolve')}</Button>
              : null },
          ]} />
      </Panel>

      <Panel title={t('problems.addTitle')}>
        <form onSubmit={submit}>
          <CodePicker client={client} domain="clinical-finding" label={t('problems.conceptLabel')}
            value={concept} onChange={setConcept} />
          <div className="form-grid">
            <Field label={t('problems.onset')}>{id => <input id={id} name="onsetDate" type="date" />}</Field>
            <Field label={t('clinical.note')}>{id => <input id={id} name="note" />}</Field>
          </div>
          <div className="form-actions">
            <Button type="submit" disabled={!concept || !ready || add.isPending}>{t('common.save')}</Button>
          </div>
          {!ready ? <p className="field-hint">{t('clinical.contextRequired')}</p> : null}
        </form>
        <ErrorNotice error={add.error} />
      </Panel>
    </div>
  );
}
