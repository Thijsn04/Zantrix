import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { AuditVerificationResult, FhirServerStatus, SystemInfo, TerminologyStatus } from '../../lib/api/types';
import { Badge } from '../../design/Badge';
import { Panel } from '../../design/Panel';
import { ErrorNotice, Notice } from '../../design/Feedback';

/**
 * Whether the services the workspace depends on are actually reachable.
 *
 * Every value here is read from a service. Nothing is inferred, and a service
 * that cannot be reached says so rather than showing a comforting default. An
 * operator checking this screen during an incident has to be able to trust it.
 */
export function PlatformStatus({ client, canVerifyAudit }: { client: ApiClient; canVerifyAudit: boolean }) {
  const { t } = useTranslation();

  const system = useQuery({
    queryKey: ['system-info'],
    queryFn: ({ signal }) => client.get<SystemInfo>('/api/v1/system/info', signal),
  });
  const fhir = useQuery({
    queryKey: ['fhir-status'],
    queryFn: ({ signal }) => client.get<FhirServerStatus>('/api/v1/fhir/status', signal),
  });
  const terminology = useQuery({
    queryKey: ['terminology-status'],
    queryFn: ({ signal }) => client.get<TerminologyStatus>('/api/v1/terminology/status', signal),
  });
  const integrity = useQuery({
    enabled: canVerifyAudit,
    queryKey: ['audit-integrity'],
    queryFn: ({ signal }) => client.get<AuditVerificationResult>('/api/v1/audit/integrity', signal),
  });

  return (
    <div className="stack">
      <div className="status-grid">
        <StatusCard title={t('platform.application')} loading={system.isLoading} error={system.error}
          reachable={Boolean(system.data)}
          rows={system.data ? [
            [t('platform.name'), system.data.application],
            [t('table.status'), system.data.status],
          ] : []} />

        <StatusCard title={t('platform.fhirServer')} loading={fhir.isLoading} error={fhir.error}
          reachable={fhir.data?.reachable ?? false}
          rows={fhir.data ? [
            [t('platform.software'), fhir.data.software ?? t('common.none')],
            [t('platform.fhirVersion'), fhir.data.fhirVersion ?? t('common.none')],
            [t('platform.baseUrl'), fhir.data.baseUrl],
          ] : []} />

        <StatusCard title={t('platform.terminology')} loading={terminology.isLoading} error={terminology.error}
          reachable={terminology.data?.reachable ?? false}
          rows={terminology.data ? [
            [t('platform.software'), terminology.data.software],
            [t('platform.edition'), terminology.data.version],
            [t('platform.fhirVersion'), terminology.data.fhirVersion],
          ] : []} />

        {canVerifyAudit ? (
          <StatusCard title={t('platform.auditChain')} loading={integrity.isLoading} error={integrity.error}
            reachable={integrity.data?.intact ?? false}
            rows={integrity.data ? [
              [t('platform.entriesChecked'), String(integrity.data.checkedCount)],
              [t('platform.brokenAt'), integrity.data.brokenAtId === null
                ? t('common.none') : String(integrity.data.brokenAtId)],
            ] : []} />
        ) : null}
      </div>

      {terminology.isError ? <Notice tone="warning">{t('platform.terminologyUnavailable')}</Notice> : null}
      {integrity.data && !integrity.data.intact
        ? <Notice tone="danger">{t('platform.auditBroken', { id: integrity.data.brokenAtId })}</Notice> : null}
    </div>
  );
}

function StatusCard({ title, loading, error, reachable, rows }: {
  title: string; loading: boolean; error: unknown; reachable: boolean; rows: [string, string][];
}) {
  const { t } = useTranslation();
  const tone = loading ? 'neutral' : error || !reachable ? 'danger' : 'success';
  const label = loading ? t('common.loading') : error ? t('platform.unreachable')
    : reachable ? t('platform.reachable') : t('platform.degraded');

  return (
    <Panel title={title} actions={<Badge tone={tone}>{label}</Badge>}>
      <ErrorNotice error={error} />
      {rows.length > 0 ? (
        <dl className="status-list">
          {rows.map(([term, value]) => <div key={term}><dt>{term}</dt><dd>{value}</dd></div>)}
        </dl>
      ) : null}
    </Panel>
  );
}
