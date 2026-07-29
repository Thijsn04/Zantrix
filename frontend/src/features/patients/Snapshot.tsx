import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type {
  AllergySummary, EncounterSummary, NoteSummary, OrderSummary,
  PatientSummary, PrescriptionSummary, ProblemSummary, ResultSummary, VitalSummary,
} from '../../lib/api/types';
import { formatDate, formatDateTime } from '../../lib/format';
import { DataTable } from '../../design/DataTable';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice } from '../../design/Feedback';

/**
 * One-page clinical overview of a patient.
 *
 * This is the first thing a clinician opens: the active record at a glance,
 * rather than eight tabs that each have to be visited to build the picture.
 */
export function Snapshot({ client, patient, canPrescribe }: {
  client: ApiClient; patient: PatientSummary; canPrescribe: boolean;
}) {
  const { t, i18n } = useTranslation();
  const id = encodeURIComponent(patient.id);

  const problems = useQuery({ queryKey: ['problems', patient.id],
    queryFn: ({ signal }) => client.get<ProblemSummary[]>(`/api/v1/problems?patientId=${id}`, signal) });
  const allergies = useQuery({ queryKey: ['allergies', patient.id],
    queryFn: ({ signal }) => client.get<AllergySummary[]>(`/api/v1/allergies?patientId=${id}`, signal) });
  const medications = useQuery({ enabled: canPrescribe, queryKey: ['medications', patient.id],
    queryFn: ({ signal }) => client.get<PrescriptionSummary[]>(`/api/v1/medications?patientId=${id}`, signal) });
  const vitals = useQuery({ queryKey: ['vitals', patient.id],
    queryFn: ({ signal }) => client.get<VitalSummary[]>(`/api/v1/vitals?patientId=${id}`, signal) });
  const orders = useQuery({ queryKey: ['orders', patient.id],
    queryFn: ({ signal }) => client.get<OrderSummary[]>(`/api/v1/orders?patientId=${id}`, signal) });
  const results = useQuery({ queryKey: ['results', patient.id],
    queryFn: ({ signal }) => client.get<ResultSummary[]>(`/api/v1/orders/results?patientId=${id}`, signal) });
  const encounters = useQuery({ queryKey: ['encounters', patient.id],
    queryFn: ({ signal }) => client.get<EncounterSummary[]>(`/api/v1/encounters?patientId=${id}`, signal) });
  const notes = useQuery({ queryKey: ['notes', patient.id],
    queryFn: ({ signal }) => client.get<NoteSummary[]>(`/api/v1/notes?patientId=${id}`, signal) });

  const openOrders = (orders.data ?? []).filter(order => order.status !== 'completed' && order.status !== 'revoked');
  const unsignedNotes = (notes.data ?? []).filter(note => note.status === 'preliminary');
  const activeEncounter = (encounters.data ?? []).find(encounter => encounter.status === 'in-progress');

  return (
    <div className="stack">
      <div className="metric-grid">
        <div className="metric"><strong>{problems.data?.length ?? 0}</strong><span>{t('snapshot.activeProblems')}</span></div>
        <div className="metric"><strong>{allergies.data?.length ?? 0}</strong><span>{t('snapshot.allergies')}</span></div>
        <div className="metric"><strong>{openOrders.length}</strong><span>{t('snapshot.openOrders')}</span></div>
        <div className="metric"><strong>{unsignedNotes.length}</strong><span>{t('snapshot.unsignedNotes')}</span></div>
      </div>

      {activeEncounter ? (
        <Panel title={t('snapshot.activeEncounter')}
          subtitle={`${activeEncounter.reason ?? ''} ${formatDateTime(activeEncounter.start, i18n.language)}`}>
          <StatusBadge status={activeEncounter.status} />
        </Panel>
      ) : null}

      <div className="grid-2">
        <Panel title={t('snapshot.activeProblems')}>
          <ErrorNotice error={problems.error} />
          <DataTable caption={t('snapshot.activeProblems')} rows={problems.data ?? []} rowKey={row => row.id}
            empty={t('chart.empty')}
            columns={[
              { header: t('table.problem'), cell: row => <span className="primary-cell">{row.display}</span> },
              { header: t('table.onset'), cell: row => formatDate(row.onsetDate, i18n.language) },
              { header: t('table.status'), cell: row => <StatusBadge status={row.clinicalStatus} /> },
            ]} />
        </Panel>

        <Panel title={t('snapshot.allergies')}>
          <ErrorNotice error={allergies.error} />
          <DataTable caption={t('snapshot.allergies')} rows={allergies.data ?? []} rowKey={row => row.id}
            empty={t('patient.noKnownAllergies')}
            columns={[
              { header: t('table.substance'), cell: row => <span className="primary-cell">{row.substance}</span> },
              { header: t('table.criticality'), cell: row => <StatusBadge status={row.criticality} /> },
              { header: t('table.reaction'), cell: row => row.reaction ?? '' },
            ]} />
        </Panel>
      </div>

      <div className="grid-2">
        {canPrescribe ? (
          <Panel title={t('snapshot.medications')}>
            <ErrorNotice error={medications.error} />
            <DataTable caption={t('snapshot.medications')} rows={medications.data ?? []} rowKey={row => row.id}
              empty={t('chart.empty')}
              columns={[
                { header: t('table.medication'), cell: row => <span className="primary-cell">{row.medication}</span> },
                { header: t('table.dosage'), cell: row => row.dosage ?? '' },
                { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
              ]} />
          </Panel>
        ) : null}

        <Panel title={t('snapshot.recentVitals')}>
          <ErrorNotice error={vitals.error} />
          <DataTable caption={t('snapshot.recentVitals')} rows={(vitals.data ?? []).slice(0, 8)} rowKey={row => row.id}
            empty={t('chart.empty')}
            columns={[
              { header: t('table.measurement'), cell: row => <span className="primary-cell">{row.display}</span> },
              { header: t('table.value'), align: 'end', cell: row => `${row.value} ${row.unit}` },
              { header: t('table.observed'), cell: row => formatDateTime(row.observedAt, i18n.language) },
            ]} />
        </Panel>
      </div>

      <div className="grid-2">
        <Panel title={t('snapshot.openOrders')}>
          <ErrorNotice error={orders.error} />
          <DataTable caption={t('snapshot.openOrders')} rows={openOrders} rowKey={row => row.id} empty={t('chart.empty')}
            columns={[
              { header: t('table.order'), cell: row => <span className="primary-cell">{row.display}</span> },
              { header: t('table.priority'), cell: row => <StatusBadge status={row.priority} /> },
              { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            ]} />
        </Panel>

        <Panel title={t('snapshot.recentResults')}>
          <ErrorNotice error={results.error} />
          <DataTable caption={t('snapshot.recentResults')} rows={(results.data ?? []).slice(0, 8)} rowKey={row => row.id}
            empty={t('chart.empty')}
            columns={[
              { header: t('table.result'), cell: row => <span className="primary-cell">{row.conclusion ?? row.id}</span> },
              { header: t('table.issued'), cell: row => formatDateTime(row.issuedAt, i18n.language) },
              { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            ]} />
        </Panel>
      </div>
    </div>
  );
}
