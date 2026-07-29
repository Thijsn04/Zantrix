import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { CareTeamSummary, GoalSummary } from '../../lib/api/types';
import { formatDate } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice } from '../../design/Feedback';

const PRIORITIES = ['high-priority', 'medium-priority', 'low-priority'] as const;
const OUTCOMES = ['achieved', 'not-achieved', 'no-longer-desired'] as const;

/**
 * Care coordination: who is involved, and what the care is working towards.
 *
 * A goal is closed by recording what became of it rather than by removal,
 * because whether it was achieved is itself something a later clinician needs.
 */
export function CarePanel({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const [showClosed, setShowClosed] = useState(false);
  const [closing, setClosing] = useState<GoalSummary>();

  const goals = useQuery({
    queryKey: ['goals', patientId, showClosed],
    queryFn: ({ signal }) => client.get<GoalSummary[]>(
      `/api/v1/care/goals?patientId=${encodeURIComponent(patientId)}&includeClosed=${showClosed}`, signal),
  });
  const teams = useQuery({
    queryKey: ['care-teams', patientId],
    queryFn: ({ signal }) => client.get<CareTeamSummary[]>(
      `/api/v1/care/teams?patientId=${encodeURIComponent(patientId)}`, signal),
  });

  const invalidateGoals = () => void queryClient.invalidateQueries({ queryKey: ['goals'] });
  const invalidateTeams = () => void queryClient.invalidateQueries({ queryKey: ['care-teams'] });

  const addGoal = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/care/goals', body), onSuccess: invalidateGoals });
  const closeGoal = useMutation({
    mutationFn: ({ id, outcome }: { id: string; outcome: string }) => client.post(
      `/api/v1/care/goals/${encodeURIComponent(id)}/close`
      + `?patientId=${encodeURIComponent(patientId)}&outcome=${encodeURIComponent(outcome)}`),
    onSuccess: () => { invalidateGoals(); setClosing(undefined); } });
  const defineTeam = useMutation({
    mutationFn: (body: unknown) => client.post('/api/v1/care/teams', body), onSuccess: invalidateTeams });
  const standDown = useMutation({
    mutationFn: (id: string) => client.post(
      `/api/v1/care/teams/${encodeURIComponent(id)}/stand-down?patientId=${encodeURIComponent(patientId)}`),
    onSuccess: invalidateTeams });

  function submitGoal(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    addGoal.mutate({
      patientId,
      description: String(form.get('description') ?? ''),
      priority: String(form.get('priority') ?? '') || null,
      targetDate: String(form.get('targetDate') ?? '') || null,
      addressesConditionId: String(form.get('addressesConditionId') ?? '') || null,
      note: String(form.get('note') ?? '') || null,
    });
    event.currentTarget.reset();
  }

  function submitTeam(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    defineTeam.mutate({
      patientId,
      name: String(form.get('name') ?? ''),
      members: [{
        practitionerId: String(form.get('practitionerId') ?? ''),
        roleCode: String(form.get('roleCode') ?? ''),
        roleDisplay: String(form.get('roleDisplay') ?? ''),
      }],
    });
    event.currentTarget.reset();
  }

  return (
    <div className="stack">
      <Panel title={t('care.goalsTitle')}
        actions={<Button className="quiet-button" onClick={() => setShowClosed(!showClosed)}>
          {showClosed ? t('care.hideClosed') : t('care.showClosed')}</Button>}>
        <ErrorNotice error={goals.error} />
        <ErrorNotice error={closeGoal.error} />
        <DataTable caption={t('care.goalsTitle')} rows={goals.data ?? []} rowKey={row => row.id}
          empty={t('care.noGoals')}
          columns={[
            { header: t('care.goal'), cell: row => <span className="primary-cell">{row.description}</span> },
            { header: t('table.priority'), cell: row => row.priority
              ? t(`care.priority.${row.priority}`, { defaultValue: row.priority }) : '' },
            { header: t('care.target'), cell: row => formatDate(row.targetDate, i18n.language) },
            { header: t('table.status'), cell: row => <StatusBadge status={row.lifecycleStatus ?? ''} /> },
            { header: t('care.outcome'), cell: row => row.achievementStatus
              ? t(`care.outcomeValue.${row.achievementStatus}`, { defaultValue: row.achievementStatus }) : '' },
            { header: t('table.actions'), align: 'end', cell: row => row.lifecycleStatus === 'active'
              ? <Button className="quiet-button" onClick={() => setClosing(row)}>{t('care.close')}</Button>
              : null },
          ]} />

        {closing ? (
          <div className="merge-confirm">
            <h3 className="panel-title">{t('care.closeTitle', { goal: closing.description })}</h3>
            <p className="field-hint">{t('care.closeHint')}</p>
            <div className="button-row">
              {OUTCOMES.map(outcome => (
                <Button key={outcome} className="quiet-button" disabled={closeGoal.isPending}
                  onClick={() => closeGoal.mutate({ id: closing.id, outcome })}>
                  {t(`care.outcomeValue.${outcome}`)}
                </Button>
              ))}
              <Button className="quiet-button" onClick={() => setClosing(undefined)}>{t('common.cancel')}</Button>
            </div>
          </div>
        ) : null}
      </Panel>

      <Panel title={t('care.addGoalTitle')}>
        <form onSubmit={submitGoal}>
          <div className="form-grid">
            <Field label={t('care.goal')} required>{id => <input id={id} name="description" required />}</Field>
            <Field label={t('table.priority')}>
              {id => <select id={id} name="priority" defaultValue="">
                <option value="">{t('common.none')}</option>
                {PRIORITIES.map(value => <option key={value} value={value}>{t(`care.priority.${value}`)}</option>)}
              </select>}
            </Field>
            <Field label={t('care.target')} hint={t('care.targetHint')}>
              {(id, describedBy) => <input id={id} name="targetDate" type="date" aria-describedby={describedBy}
                min={new Date().toISOString().slice(0, 10)} />}
            </Field>
            <Field label={t('care.addresses')} hint={t('care.addressesHint')}>
              {(id, describedBy) => <input id={id} name="addressesConditionId" aria-describedby={describedBy} />}
            </Field>
            <Field label={t('clinical.note')}>{id => <input id={id} name="note" />}</Field>
          </div>
          <div className="form-actions">
            <Button type="submit" disabled={addGoal.isPending}>{t('care.addGoal')}</Button>
          </div>
        </form>
        <ErrorNotice error={addGoal.error} />
      </Panel>

      <Panel title={t('care.teamTitle')} subtitle={t('care.teamSubtitle')}>
        <ErrorNotice error={teams.error} />
        <ErrorNotice error={standDown.error} />
        <DataTable caption={t('care.teamTitle')} rows={teams.data ?? []} rowKey={row => row.id}
          empty={t('care.noTeams')}
          columns={[
            { header: t('table.name'), cell: row => <span className="primary-cell">{row.name}</span> },
            { header: t('care.members'), cell: row => row.members
              .map(member => `${member.practitionerId} (${member.roleDisplay ?? member.roleCode})`).join(', ') },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status ?? ''} /> },
            { header: t('table.actions'), align: 'end', cell: row => row.status === 'active'
              ? <Button className="quiet-button" disabled={standDown.isPending}
                  onClick={() => standDown.mutate(row.id)}>{t('care.standDown')}</Button>
              : null },
          ]} />

        <form onSubmit={submitTeam}>
          <div className="form-grid">
            <Field label={t('care.teamName')} required>{id => <input id={id} name="name" required />}</Field>
            <Field label={t('clinical.practitionerId')} required>
              {id => <input id={id} name="practitionerId" required />}
            </Field>
            <Field label={t('care.roleCode')} required>{id => <input id={id} name="roleCode" required />}</Field>
            <Field label={t('care.roleDisplay')} required>{id => <input id={id} name="roleDisplay" required />}</Field>
          </div>
          <div className="form-actions">
            <Button type="submit" disabled={defineTeam.isPending}>{t('care.defineTeam')}</Button>
          </div>
        </form>
        <ErrorNotice error={defineTeam.error} />
      </Panel>
    </div>
  );
}
