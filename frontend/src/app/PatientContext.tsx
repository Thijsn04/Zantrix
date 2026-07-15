import { useTranslation } from 'react-i18next';
import type { PatientSummary } from '../lib/api/types';

/** Persistent shell location for patient context. No patient is inferred or fabricated. */
export function PatientContext({ patient }: { patient?: PatientSummary }) {
  const { t } = useTranslation();
  return <section className="patient-context" aria-label={t('patientContext.label')}>
    <strong>{patient?.displayName ?? t('patientContext.noneTitle')}</strong>
    <span>{patient ? `${patient.birthDate} · ${patient.identifier ?? patient.id}` : t('patientContext.noneDescription')}</span>
  </section>;
}
