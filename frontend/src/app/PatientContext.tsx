import { useTranslation } from 'react-i18next';

/** Persistent shell location for patient context. No patient is inferred or fabricated. */
export function PatientContext() {
  const { t } = useTranslation();
  return <section className="patient-context" aria-label={t('patientContext.label')}>
    <strong>{t('patientContext.noneTitle')}</strong><span>{t('patientContext.noneDescription')}</span>
  </section>;
}
