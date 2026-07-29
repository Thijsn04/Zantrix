import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { AuditRecord, AuditVerificationResult, EmergencyAccessReview, Page } from '../../lib/api/types';
import { formatDateTime } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { Badge, StatusBadge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';
import { Tabs } from '../../design/Tabs';

type PrivacyTab = 'reviews' | 'audit' | 'integrity';

/**
 * The privacy officer workspace.
 *
 * This role does not do clinical work: it reviews who accessed what. The three
 * areas are the ones the backend actually supports, namely emergency access
 * reviews, a filtered audit trail, and hash-chain integrity verification.
 */
export function PrivacyOffice({ client }: { client: ApiClient }) {
  const { t } = useTranslation();
  const [tab, setTab] = useState<PrivacyTab>('reviews');
  return (
    <Panel title={t('privacy.title')} level={1} subtitle={t('privacy.subtitle')}>
      <Tabs label={t('privacy.title')} active={tab} onChange={setTab}
        tabs={[
          { id: 'reviews', label: t('privacy.emergency') },
          { id: 'audit', label: t('privacy.audit') },
          { id: 'integrity', label: t('privacy.integrity') },
        ]}>
        {tab === 'reviews' ? <EmergencyReviews client={client} /> : null}
        {tab === 'audit' ? <AuditTrail client={client} /> : null}
        {tab === 'integrity' ? <Integrity client={client} /> : null}
      </Tabs>
    </Panel>
  );
}

function EmergencyReviews({ client }: { client: ApiClient }) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const reviews = useQuery({
    queryKey: ['emergency-reviews'],
    queryFn: ({ signal }) => client.get<EmergencyAccessReview[]>('/api/v1/privacy/emergency-reviews', signal),
  });
  const decide = useMutation({
    mutationFn: ({ id, outcome }: { id: string; outcome: string }) => client.post(
      `/api/v1/privacy/emergency-reviews/${encodeURIComponent(id)}/review?outcome=${encodeURIComponent(outcome)}`),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['emergency-reviews'] }),
  });

  return (
    <>
      <Notice tone="info">{t('privacy.emergencyHint')}</Notice>
      <ErrorNotice error={reviews.error} />
      <ErrorNotice error={decide.error} />
      <DataTable caption={t('privacy.emergency')} rows={reviews.data ?? []} rowKey={row => row.id}
        empty={t('privacy.noReviews')}
        columns={[
          { header: t('table.actor'), cell: row => <span className="primary-cell">{row.actor ?? ''}</span> },
          { header: t('table.reason'), cell: row => <Badge tone="warning">{row.reasonCode ?? ''}</Badge> },
          { header: t('table.patient'), cell: row => row.patientId ?? '' },
          { header: t('table.resource'), cell: row => `${row.resourceType ?? ''} ${row.resourceId ?? ''}`.trim() },
          { header: t('table.when'), cell: row => formatDateTime(row.occurredAt, i18n.language) },
          { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
          { header: t('table.actions'), align: 'end', cell: row => row.status === 'open' ? (
            <div className="button-row">
              <Button className="quiet-button" disabled={decide.isPending}
                onClick={() => decide.mutate({ id: row.id, outcome: 'justified' })}>{t('privacy.justified')}</Button>
              <Button className="quiet-button" disabled={decide.isPending}
                onClick={() => decide.mutate({ id: row.id, outcome: 'not-justified' })}>{t('privacy.notJustified')}</Button>
            </div>
          ) : null },
        ]} />
    </>
  );
}

function AuditTrail({ client }: { client: ApiClient }) {
  const { t, i18n } = useTranslation();
  const [filters, setFilters] = useState({ patientId: '', actor: '', outcome: '', breakTheGlass: '' });

  const query = new URLSearchParams({ size: '100' });
  if (filters.patientId) query.set('patientId', filters.patientId);
  if (filters.actor) query.set('actor', filters.actor);
  if (filters.outcome) query.set('outcome', filters.outcome);
  if (filters.breakTheGlass) query.set('breakTheGlass', filters.breakTheGlass);

  const events = useQuery({
    queryKey: ['audit', filters],
    queryFn: ({ signal }) => client.get<Page<AuditRecord>>(`/api/v1/audit/events?${query.toString()}`, signal),
  });

  function apply(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setFilters({
      patientId: String(form.get('patientId') ?? ''), actor: String(form.get('actor') ?? ''),
      outcome: String(form.get('outcome') ?? ''), breakTheGlass: String(form.get('breakTheGlass') ?? ''),
    });
  }

  return (
    <>
      <form onSubmit={apply}>
        <div className="form-grid">
          <Field label={t('table.patient')}>{id => <input id={id} name="patientId" defaultValue={filters.patientId} />}</Field>
          <Field label={t('table.actor')}>{id => <input id={id} name="actor" defaultValue={filters.actor} />}</Field>
          <Field label={t('table.outcome')}>
            {id => <select id={id} name="outcome" defaultValue={filters.outcome}>
              <option value="">{t('common.any')}</option>
              <option value="SUCCESS">SUCCESS</option>
              <option value="DENIED">DENIED</option>
              <option value="FAILURE">FAILURE</option>
            </select>}
          </Field>
          <Field label={t('privacy.emergencyOnly')}>
            {id => <select id={id} name="breakTheGlass" defaultValue={filters.breakTheGlass}>
              <option value="">{t('common.any')}</option>
              <option value="true">{t('common.yes')}</option>
              <option value="false">{t('common.no')}</option>
            </select>}
          </Field>
        </div>
        <div className="form-actions"><Button type="submit">{t('common.filter')}</Button></div>
      </form>
      <ErrorNotice error={events.error} />
      <DataTable caption={t('privacy.audit')} rows={events.data?.content ?? []} rowKey={row => String(row.id)}
        empty={t('privacy.noEvents')}
        columns={[
          { header: t('table.when'), cell: row => formatDateTime(row.recordedAt, i18n.language) },
          { header: t('table.actor'), cell: row => <span className="primary-cell">{row.actor ?? ''}</span> },
          { header: t('table.action'), cell: row => `${row.action} ${row.entityType ?? ''}`.trim() },
          { header: t('table.patient'), cell: row => row.patientId ?? '' },
          { header: t('table.outcome'), cell: row => <StatusBadge status={row.outcome.toLowerCase()} /> },
          { header: t('privacy.emergencyOnly'), cell: row => row.breakTheGlass ? <Badge tone="danger">{t('common.yes')}</Badge> : '' },
        ]} />
    </>
  );
}

function Integrity({ client }: { client: ApiClient }) {
  const { t } = useTranslation();
  const verify = useQuery({
    queryKey: ['audit-integrity'],
    queryFn: ({ signal }) => client.get<AuditVerificationResult>('/api/v1/audit/integrity', signal),
  });

  return (
    <>
      <Notice tone="info">{t('privacy.integrityHint')}</Notice>
      <ErrorNotice error={verify.error} />
      {verify.data ? (
        <>
          <Notice tone={verify.data.intact ? 'info' : 'danger'}>
            {verify.data.intact ? t('privacy.chainIntact') : t('privacy.chainBroken', { id: verify.data.brokenAtId ?? '-' })}
          </Notice>
          <div className="metric-grid">
            <div className="metric">
              <strong>{verify.data.checkedCount}</strong><span>{t('privacy.entriesChecked')}</span>
            </div>
          </div>
        </>
      ) : null}
      <div className="form-actions">
        <Button onClick={() => void verify.refetch()} disabled={verify.isFetching}>{t('privacy.reverify')}</Button>
      </div>
    </>
  );
}
