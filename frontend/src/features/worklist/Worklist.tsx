import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { CurrentUser, PatientSummary, TaskSummary } from '../../lib/api/types';
import { formatDateTime } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice } from '../../design/Feedback';
import { Tabs } from '../../design/Tabs';

type Scope = 'mine' | 'unassigned' | 'all';

/**
 * The clinician worklist.
 *
 * Tasks are grouped the way a working day is: what is assigned to me, what
 * nobody has picked up, and everything for the patient in context. Claiming a
 * task assigns it to the signed-in user through their token subject, which is
 * the same identity the backend records.
 */
export function Worklist({ client, user, patient, onSelectPatient }: {
  client: ApiClient;
  user?: CurrentUser;
  patient?: PatientSummary;
  onSelectPatient?: (patientId: string) => void;
}) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const [scope, setScope] = useState<Scope>('mine');
  const [includeCompleted, setIncludeCompleted] = useState(false);

  const owner = user ? `Practitioner/${user.subject}` : '';
  const query = new URLSearchParams();
  if (scope === 'mine' && owner) query.set('owner', owner);
  if (patient) query.set('patientId', patient.id);
  query.set('includeCompleted', String(includeCompleted));

  const tasks = useQuery({
    queryKey: ['tasks', scope, patient?.id, includeCompleted],
    queryFn: ({ signal }) => client.get<TaskSummary[]>(`/api/v1/tasks?${query.toString()}`, signal),
  });

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['tasks'] });
  const act = useMutation({
    mutationFn: ({ id, patientId, verb }: { id: string; patientId: string; verb: 'claim' | 'start' | 'complete' }) => {
      const base = `/api/v1/tasks/${encodeURIComponent(id)}/${verb}?patientId=${encodeURIComponent(patientId)}`;
      return client.post(verb === 'claim' ? `${base}&ownerReference=${encodeURIComponent(owner)}` : base);
    },
    onSuccess: invalidate,
  });

  const visible = (tasks.data ?? []).filter(task =>
    scope !== 'unassigned' || !task.ownerReference);

  return (
    <Panel title={t('tasks.title')} level={1} subtitle={patient ? t('tasks.filteredByPatient', { name: patient.displayName }) : undefined}
      actions={<Button className="quiet-button" onClick={() => setIncludeCompleted(!includeCompleted)}>
        {includeCompleted ? t('tasks.hideCompleted') : t('tasks.showCompleted')}</Button>}>
      <Tabs label={t('tasks.scopeLabel')} active={scope} onChange={setScope}
        tabs={[
          { id: 'mine', label: t('tasks.mine') },
          { id: 'unassigned', label: t('tasks.unassigned') },
          { id: 'all', label: t('tasks.all') },
        ]}>
        <ErrorNotice error={tasks.error} />
        <ErrorNotice error={act.error} />
        <DataTable caption={t('tasks.title')} rows={visible} rowKey={row => row.id} empty={t('tasks.empty')}
          columns={[
            { header: t('table.task'), cell: row => <span className="primary-cell">{row.description}</span> },
            { header: t('table.priority'), cell: row => <StatusBadge status={row.priority} /> },
            { header: t('table.due'), cell: row => formatDateTime(row.dueAt, i18n.language) },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            { header: t('table.actions'), align: 'end', cell: row => (
              <div className="button-row">
                {!row.ownerReference && owner ? (
                  <Button className="quiet-button" disabled={act.isPending}
                    onClick={() => act.mutate({ id: row.id, patientId: row.patientId, verb: 'claim' })}>
                    {t('tasks.claim')}</Button>
                ) : null}
                {row.status === 'requested' || row.status === 'accepted' ? (
                  <Button className="quiet-button" disabled={act.isPending}
                    onClick={() => act.mutate({ id: row.id, patientId: row.patientId, verb: 'start' })}>
                    {t('tasks.start')}</Button>
                ) : null}
                {row.status !== 'completed' ? (
                  <Button className="quiet-button" disabled={act.isPending}
                    onClick={() => act.mutate({ id: row.id, patientId: row.patientId, verb: 'complete' })}>
                    {t('tasks.complete')}</Button>
                ) : null}
                {onSelectPatient ? (
                  <Button className="quiet-button" onClick={() => onSelectPatient(row.patientId)}>
                    {t('tasks.openPatient')}</Button>
                ) : null}
              </div>
            ) },
          ]} />
      </Tabs>
    </Panel>
  );
}
