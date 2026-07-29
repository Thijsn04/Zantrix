import { X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import type { PatientSummary } from '../../lib/api/types';
import { age, sexMarker } from '../../lib/format';

/**
 * Open patient charts.
 *
 * These are not decorative tabs: each one is a chart that stays mounted, so
 * switching between two patients preserves everything, including half-entered
 * forms. Each tab repeats enough identity to make a wrong-patient switch
 * obvious at a glance.
 */
export function PatientTabs({ patients, activeId, onActivate, onClose, onSearch }: {
  patients: PatientSummary[];
  activeId?: string;
  onActivate: (id: string) => void;
  onClose: (id: string) => void;
  onSearch: () => void;
}) {
  const { t } = useTranslation();
  return (
    <nav className="patient-tabs" aria-label={t('patients.openCharts')}>
      {patients.map(patient => {
        const years = age(patient.birthDate);
        const active = patient.id === activeId;
        return (
          <span key={patient.id} className={`patient-tab${active ? ' active' : ''}`}>
            <button type="button" aria-current={active ? 'page' : undefined} onClick={() => onActivate(patient.id)}>
              <strong>{patient.displayName}</strong>
              <span>
                {years === null ? '' : `${t('patient.ageYears', { count: years })} `}
                {sexMarker(patient.administrativeGender)} · {patient.identifier ?? patient.id}
              </span>
            </button>
            <button type="button" className="patient-tab-close"
              aria-label={t('patients.closeChart', { name: patient.displayName })}
              onClick={() => onClose(patient.id)}>
              <X size={13} aria-hidden="true" />
            </button>
          </span>
        );
      })}
      <button type="button" className={`patient-tab-search${activeId ? '' : ' active'}`}
        aria-current={activeId ? undefined : 'page'} onClick={onSearch}>
        {t('patients.findPatient')}
      </button>
    </nav>
  );
}
