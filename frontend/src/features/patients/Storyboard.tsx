import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { AllergySummary, AppointmentSummary, PatientSummary, ProblemSummary } from '../../lib/api/types';
import { age, formatDate, formatDateTime, sexMarker } from '../../lib/format';
import { Badge } from '../../design/Badge';
import { Button } from '../../design/Button';

/**
 * Persistent patient banner, always visible while a patient is in context.
 *
 * It carries the identity a clinician verifies before acting, and the two
 * safety facts that must never be hidden behind a tab: active allergies and the
 * active problem list.
 */
export function Storyboard({ client, patient, onClear, canSeeChart, canSeeScheduling }: {
  client: ApiClient;
  patient: PatientSummary;
  onClear: () => void;
  canSeeChart: boolean;
  canSeeScheduling: boolean;
}) {
  const { t, i18n } = useTranslation();
  const allergies = useQuery({
    enabled: canSeeChart, queryKey: ['allergies', patient.id],
    queryFn: ({ signal }) => client.get<AllergySummary[]>(`/api/v1/allergies?patientId=${encodeURIComponent(patient.id)}`, signal),
  });
  const problems = useQuery({
    enabled: canSeeChart, queryKey: ['problems', patient.id],
    queryFn: ({ signal }) => client.get<ProblemSummary[]>(`/api/v1/problems?patientId=${encodeURIComponent(patient.id)}`, signal),
  });
  const appointments = useQuery({
    enabled: canSeeScheduling, queryKey: ['appointments', patient.id],
    queryFn: ({ signal }) => client.get<AppointmentSummary[]>(`/api/v1/appointments?patientId=${encodeURIComponent(patient.id)}`, signal),
  });

  const years = age(patient.birthDate);
  const initials = patient.displayName.split(/\s+/).map(part => part.charAt(0)).slice(0, 2).join('').toUpperCase();
  // Captured once so the "upcoming" cutoff stays stable across re-renders.
  const [now] = useState(() => Date.now());
  const upcoming = (appointments.data ?? [])
    .filter(appointment => appointment.start && new Date(appointment.start).getTime() >= now
      && appointment.status !== 'cancelled' && appointment.status !== 'noshow')
    .sort((a, b) => String(a.start).localeCompare(String(b.start)))[0];

  return (
    <aside className="storyboard" aria-label={t('patientContext.label')}>
      <div className="storyboard-identity">
        <span className="avatar" aria-hidden="true">{initials}</span>
        <div>
          <h2>{patient.displayName}</h2>
          <p>{years === null ? '' : t('patient.ageYears', { count: years })} {sexMarker(patient.administrativeGender)}</p>
        </div>
      </div>
      <p className="muted-line">{t('patient.born')} {formatDate(patient.birthDate, i18n.language)}</p>
      <p className="muted-line">{t('patient.mrn')} {patient.identifier ?? patient.id}</p>
      {!patient.active ? <Badge tone="warning">{t('patient.inactive')}</Badge> : null}

      {canSeeChart ? (
        <section>
          <h3>{t('chart.allergies')}</h3>
          {allergies.isLoading ? <p className="muted-line">{t('common.loading')}</p>
            : allergies.data?.length ? (
              <ul>{allergies.data.map(allergy =>
                <li key={allergy.id}>
                  {allergy.criticality === 'high' ? <Badge tone="danger">{allergy.criticality}</Badge> : null} {allergy.substance}
                </li>)}
              </ul>
            ) : <p className="muted-line">{t('patient.noKnownAllergies')}</p>}
        </section>
      ) : null}

      {canSeeChart ? (
        <section>
          <h3>{t('patient.activeProblems')}</h3>
          {problems.data?.length
            ? <ul>{problems.data.slice(0, 6).map(problem => <li key={problem.id}>{problem.display}</li>)}</ul>
            : <p className="muted-line">{t('chart.empty')}</p>}
        </section>
      ) : null}

      {canSeeScheduling ? (
        <section>
          <h3>{t('patient.nextAppointment')}</h3>
          {upcoming
            ? <p className="muted-line">{formatDateTime(upcoming.start, i18n.language)}<br />{upcoming.service ?? ''}</p>
            : <p className="muted-line">{t('patient.noUpcoming')}</p>}
        </section>
      ) : null}

      <Button className="quiet-button" onClick={onClear}>{t('patients.change')}</Button>
    </aside>
  );
}
