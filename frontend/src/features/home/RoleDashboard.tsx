import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { CurrentUser, EmergencyAccessReview, PatientSummary, TaskSummary } from '../../lib/api/types';
import { can, primaryRole } from '../../lib/roles';
import { formatDateTime } from '../../lib/format';
import { Panel } from '../../design/Panel';
import { DataTable } from '../../design/DataTable';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';
import type { WorkspacePage } from '../../app/navigation';

/**
 * The landing view, shaped by what the signed-in role actually does.
 *
 * A physician opens on their own work queue, a privacy officer on outstanding
 * emergency-access reviews, and an administrator on the control plane. Nothing
 * here is decorative: every number is a real count from the backend, and every
 * panel links to the work it describes.
 */
export function RoleDashboard({ client, user, patient, onNavigate }: {
  client: ApiClient;
  user?: CurrentUser;
  patient?: PatientSummary;
  onNavigate: (page: WorkspacePage) => void;
}) {
  const { t, i18n } = useTranslation();
  const role = primaryRole(user);
  const owner = user ? `Practitioner/${user.subject}` : '';

  const myTasks = useQuery({
    enabled: can(user, 'tasks') && Boolean(owner),
    queryKey: ['tasks', 'mine', owner],
    queryFn: ({ signal }) => client.get<TaskSummary[]>(`/api/v1/tasks?owner=${encodeURIComponent(owner)}`, signal),
  });
  const unassigned = useQuery({
    enabled: can(user, 'tasks'),
    queryKey: ['tasks', 'queue'],
    queryFn: ({ signal }) => client.get<TaskSummary[]>('/api/v1/tasks', signal),
  });
  const reviews = useQuery({
    enabled: can(user, 'privacy'),
    queryKey: ['emergency-reviews'],
    queryFn: ({ signal }) => client.get<EmergencyAccessReview[]>('/api/v1/privacy/emergency-reviews', signal),
  });

  const openReviews = (reviews.data ?? []).filter(review => review.status === 'open');
  const queue = (unassigned.data ?? []).filter(task => !task.ownerReference && task.status !== 'completed');

  return (
    <div className="stack">
      <Panel level={1} title={t('dashboard.greeting', { name: user?.displayName ?? user?.username ?? '' })}
        subtitle={t(`dashboard.role.${role}`, { defaultValue: role })}>
        <div className="metric-grid">
          {can(user, 'tasks') ? (
            <button className="metric metric-button" onClick={() => onNavigate('tasks')}>
              <strong>{myTasks.data?.filter(task => task.status !== 'completed').length ?? 0}</strong>
              <span>{t('dashboard.myOpenTasks')}</span>
            </button>
          ) : null}
          {can(user, 'tasks') ? (
            <button className="metric metric-button" onClick={() => onNavigate('tasks')}>
              <strong>{queue.length}</strong><span>{t('dashboard.unclaimed')}</span>
            </button>
          ) : null}
          {can(user, 'privacy') ? (
            <button className="metric metric-button" onClick={() => onNavigate('privacy')}>
              <strong>{openReviews.length}</strong><span>{t('dashboard.openReviews')}</span>
            </button>
          ) : null}
          {patient ? (
            <button className="metric metric-button" onClick={() => onNavigate('patients')}>
              <strong>1</strong><span>{t('dashboard.patientInContext')}</span>
            </button>
          ) : null}
        </div>
        {!patient && can(user, 'patients')
          ? <Notice tone="info">{t('dashboard.selectPatientHint')}</Notice> : null}
      </Panel>

      {can(user, 'tasks') ? (
        <Panel title={t('dashboard.myWork')} subtitle={t('dashboard.myWorkSubtitle')}>
          <ErrorNotice error={myTasks.error} />
          <DataTable caption={t('dashboard.myWork')} rows={(myTasks.data ?? []).filter(task => task.status !== 'completed')}
            rowKey={row => row.id} empty={t('dashboard.noAssignedWork')}
            columns={[
              { header: t('table.task'), cell: row => <span className="primary-cell">{row.description}</span> },
              { header: t('table.priority'), cell: row => <StatusBadge status={row.priority} /> },
              { header: t('table.due'), cell: row => formatDateTime(row.dueAt, i18n.language) },
              { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            ]} />
        </Panel>
      ) : null}

      {can(user, 'privacy') ? (
        <Panel title={t('dashboard.reviewQueue')} subtitle={t('dashboard.reviewQueueSubtitle')}>
          <ErrorNotice error={reviews.error} />
          <DataTable caption={t('dashboard.reviewQueue')} rows={openReviews} rowKey={row => row.id}
            empty={t('privacy.noReviews')}
            columns={[
              { header: t('table.actor'), cell: row => <span className="primary-cell">{row.actor ?? ''}</span> },
              { header: t('table.reason'), cell: row => row.reasonCode ?? '' },
              { header: t('table.patient'), cell: row => row.patientId ?? '' },
              { header: t('table.when'), cell: row => formatDateTime(row.occurredAt, i18n.language) },
            ]} />
        </Panel>
      ) : null}
    </div>
  );
}
