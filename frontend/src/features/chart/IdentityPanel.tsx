import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { PatientMergeSummary, PatientSummary } from '../../lib/api/types';
import { age, formatDate, formatDateTime, sexMarker } from '../../lib/format';
import { Button } from '../../design/Button';
import { DataTable } from '../../design/DataTable';
import { Field } from '../../design/Field';
import { Panel } from '../../design/Panel';
import { StatusBadge } from '../../design/Badge';
import { ErrorNotice, Notice } from '../../design/Feedback';

/**
 * Patient identity: the record this patient has been merged with, and the
 * ability to merge or reverse a merge.
 *
 * Merging moves every referencing resource from one record onto another. It is
 * the most consequential action in the workspace, so nothing here happens on a
 * single click: the clinician selects a record, reads back both identities in
 * full, and confirms the direction explicitly.
 */
export function IdentityPanel({ client, patient, canMerge, canUnmerge }: {
  client: ApiClient; patient: PatientSummary; canMerge: boolean; canUnmerge: boolean;
}) {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [candidate, setCandidate] = useState<PatientSummary>();

  const history = useQuery({
    queryKey: ['merges', patient.id],
    queryFn: ({ signal }) => client.get<PatientMergeSummary[]>(
      `/api/v1/patients/${encodeURIComponent(patient.id)}/merges`, signal),
  });

  const candidates = useQuery({
    enabled: query.trim().length > 1,
    queryKey: ['patients', query],
    queryFn: ({ signal }) => client.get<PatientSummary[]>(
      `/api/v1/patients?query=${encodeURIComponent(query.trim())}`, signal),
  });

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['merges'] });
    void queryClient.invalidateQueries({ queryKey: ['patients'] });
  };

  // The selected record is the source and is retired into the record in
  // context, so the chart the clinician is reading is the one that survives.
  const merge = useMutation({
    mutationFn: (sourceId: string) => client.post<PatientMergeSummary>(
      `/api/v1/patients/${encodeURIComponent(sourceId)}/merge/${encodeURIComponent(patient.id)}`),
    onSuccess: () => { invalidate(); setCandidate(undefined); setQuery(''); },
  });

  const unmerge = useMutation({
    mutationFn: (mergeId: string) => client.post<PatientMergeSummary>(
      `/api/v1/patients/merges/${encodeURIComponent(mergeId)}/unmerge`),
    onSuccess: invalidate,
  });

  const others = (candidates.data ?? []).filter(row => row.id !== patient.id);

  return (
    <div className="stack">
      <Panel title={t('identity.historyTitle')} subtitle={t('identity.historySubtitle')}>
        <ErrorNotice error={history.error} />
        <ErrorNotice error={unmerge.error} />
        <DataTable caption={t('identity.historyTitle')} rows={history.data ?? []} rowKey={row => row.id}
          empty={t('identity.noMerges')}
          columns={[
            { header: t('identity.direction'), cell: row => (
              <span className="primary-cell">{t('identity.mergedInto', {
                source: row.sourcePatientId, target: row.targetPatientId })}</span>) },
            { header: t('identity.repointed'), align: 'end', cell: row => row.resourcesRepointed },
            { header: t('table.status'), cell: row => <StatusBadge status={row.status} /> },
            { header: t('identity.mergedAt'), cell: row => formatDateTime(row.mergedAt, i18n.language) },
            { header: t('table.actions'), align: 'end', cell: row => canUnmerge && row.status === 'MERGED'
              ? <Button className="quiet-button" disabled={unmerge.isPending}
                  onClick={() => unmerge.mutate(row.id)}>{t('identity.unmerge')}</Button>
              : null },
          ]} />
        {history.data?.some(row => row.status === 'MERGED') && !canUnmerge
          ? <p className="field-hint">{t('identity.unmergeRestricted')}</p> : null}
      </Panel>

      {canMerge ? (
        <Panel title={t('identity.mergeTitle')} subtitle={t('identity.mergeSubtitle')}>
          <Notice tone="warning">{t('identity.mergeWarning')}</Notice>
          <Field label={t('identity.findDuplicate')} hint={t('identity.findDuplicateHint')}>
            {(id, describedBy) => (
              <input id={id} type="search" value={query} aria-describedby={describedBy}
                onChange={event => { setQuery(event.target.value); setCandidate(undefined); }} />
            )}
          </Field>

          <ErrorNotice error={candidates.error} />
          {query.trim().length > 1 ? (
            <DataTable caption={t('identity.findDuplicate')} rows={others} rowKey={row => row.id}
              empty={t('patients.noMatches')}
              columns={[
                { header: t('table.name'), cell: row => <span className="primary-cell">{row.displayName}</span> },
                { header: t('table.born'), cell: row => formatDate(row.birthDate, i18n.language) },
                { header: t('patient.mrn'), cell: row => row.identifier ?? row.id },
                { header: t('table.status'), cell: row => <StatusBadge status={row.active ? 'active' : 'inactive'} /> },
                { header: t('table.actions'), align: 'end', cell: row => row.active
                  ? <Button className="quiet-button" onClick={() => setCandidate(row)}>{t('identity.select')}</Button>
                  : <span className="field-hint">{t('identity.alreadyRetired')}</span> },
              ]} />
          ) : null}

          {candidate ? (
            <div className="merge-confirm">
              <h3 className="panel-title">{t('identity.confirmTitle')}</h3>
              <div className="grid-2">
                <IdentityCard label={t('identity.retiredRecord')} patient={candidate} tone="danger" />
                <IdentityCard label={t('identity.survivingRecord')} patient={patient} tone="success" />
              </div>
              {/* Duplicates share a name by definition, so the identifier is what tells them apart. */}
              <Notice tone="danger">{t('identity.confirmWarning', {
                source: candidate.identifier ?? candidate.id,
                target: patient.identifier ?? patient.id })}</Notice>
              <div className="form-actions">
                <Button disabled={merge.isPending} onClick={() => merge.mutate(candidate.id)}>
                  {t('identity.confirmMerge')}
                </Button>
                <Button className="quiet-button" onClick={() => setCandidate(undefined)}>{t('common.cancel')}</Button>
              </div>
              <ErrorNotice error={merge.error} />
            </div>
          ) : null}
        </Panel>
      ) : null}
    </div>
  );
}

function IdentityCard({ label, patient, tone }: {
  label: string; patient: PatientSummary; tone: 'danger' | 'success';
}) {
  const { t, i18n } = useTranslation();
  const years = age(patient.birthDate);
  return (
    <section className={`identity-card ${tone}`}>
      <p className="eyebrow">{label}</p>
      <strong>{patient.displayName}</strong>
      <dl>
        <div><dt>{t('table.born')}</dt><dd>{formatDate(patient.birthDate, i18n.language)}
          {years === null ? '' : ` (${t('patient.ageYears', { count: years })})`}</dd></div>
        <div><dt>{t('patients.gender')}</dt><dd>{sexMarker(patient.administrativeGender)}</dd></div>
        <div><dt>{t('patient.mrn')}</dt><dd>{patient.identifier ?? patient.id}</dd></div>
      </dl>
    </section>
  );
}
