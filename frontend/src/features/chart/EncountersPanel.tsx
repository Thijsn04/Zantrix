import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { EncounterSummary, TermConcept } from '../../lib/api/types';
import { formatDateTime } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice } from '../../design/Feedback';
import { CodePicker } from '../terminology/CodePicker';
import { useClinicalContext } from './clinicalContext';

const TERMINAL = ['finished', 'cancelled'];

export function EncountersPanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const { practitionerId, setEncounterId } = useClinicalContext();
  const [reason, setReason] = useState<TermConcept>();

  const encounters = useQuery({
    queryKey: ['encounters', patientId],
    queryFn: ({ signal }) => client.get<EncounterSummary[]>(
      `/api/v1/encounters?patientId=${encodeURIComponent(patientId)}`, signal),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['encounters'] });
  const create = useMutation({
    mutationFn: (body: unknown) => client.post<EncounterSummary>('/api/v1/encounters', body),
    onSuccess: encounter => { invalidate(); setReason(undefined); setEncounterId(encounter.id); },
  });
  const transition = useMutation({
    mutationFn: ({ id, verb }: { id: string; verb: 'start' | 'finish' | 'cancel' }) => client.post(
      `/api/v1/encounters/${encodeURIComponent(id)}/${verb}?patientId=${encodeURIComponent(patientId)}`),
    onSuccess: invalidate,
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!reason || !practitionerId) return;
    create.mutate({
      patientId, practitionerId, organizationId: null, appointmentId: null,
      reasonCode: reason.code, reasonDisplay: reason.display, plannedStart: new Date().toISOString(),
    });
  }

  return (
    <div className="stack">
      <Panel title={t('chart.encounters')}>
        <ErrorNotice error={encounters.error} />
        <ErrorNotice error={transition.error} />
        <DataTable caption={t('chart.encounters')} rows={encounters.data ?? []} rowKey={row => row.id} empty={t('chart.empty')}
          columns={[
            { header: t('table.reason'), cell: row => <span className="primary-cell">{row.reason ?? row.id}</span> },
            { header: t('table.start'), cell: row => formatDateTime(row.start, i18n.language) },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            { header: t('table.actions'), align: 'end', cell: row => TERMINAL.includes(row.status) ? null : (
              <div className="button-row">
                {row.status === 'planned' ? <Button className="quiet-button" disabled={transition.isPending}
                  onClick={() => transition.mutate({ id: row.id, verb: 'start' })}>{t('actions.start')}</Button> : null}
                {row.status === 'in-progress' ? <Button className="quiet-button" disabled={transition.isPending}
                  onClick={() => transition.mutate({ id: row.id, verb: 'finish' })}>{t('actions.finish')}</Button> : null}
                <Button className="quiet-button" onClick={() => setEncounterId(row.id)}>{t('encounters.useAsContext')}</Button>
                <Button className="quiet-button" disabled={transition.isPending}
                  onClick={() => transition.mutate({ id: row.id, verb: 'cancel' })}>{t('common.cancel')}</Button>
              </div>
            ) },
          ]} />
      </Panel>

      <Panel title={t('encounters.startTitle')} subtitle={t('encounters.startSubtitle')}>
        <form onSubmit={submit}>
          <CodePicker client={client} domain="procedure" label={t('encounters.reasonLabel')} value={reason} onChange={setReason} />
          <div className="form-actions">
            <Button type="submit" disabled={!reason || !practitionerId || create.isPending}>{t('encounters.create')}</Button>
          </div>
          {!practitionerId ? <p className="field-hint">{t('clinical.practitionerRequired')}</p> : null}
        </form>
        <ErrorNotice error={create.error} />
      </Panel>
    </div>
  );
}
