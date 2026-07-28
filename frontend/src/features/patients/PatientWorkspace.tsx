import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { CurrentUser, PatientSummary } from '../../lib/api/types';
import { PatientTabs } from './PatientTabs';
import { PatientRegistry } from './PatientRegistry';
import { PatientChart } from '../chart/PatientChart';
import { Notice } from '../../design/Feedback';

/** Two charts is the working limit: enough to compare or hand over, few enough to stay unambiguous. */
export const MAX_OPEN_PATIENTS = 2;

export function PatientWorkspace({
  client, user, openPatients, activeId, limitReached,
  onOpen, onActivate, onClose, onSearch,
}: {
  client: ApiClient;
  user: CurrentUser;
  openPatients: PatientSummary[];
  activeId?: string;
  limitReached: boolean;
  onOpen: (patient: PatientSummary) => void;
  onActivate: (id: string) => void;
  onClose: (id: string) => void;
  onSearch: () => void;
}) {
  const { t } = useTranslation();

  return (
    <div className="patient-workspace">
      {openPatients.length > 0 ? (
        <PatientTabs patients={openPatients} activeId={activeId}
          onActivate={onActivate} onClose={onClose} onSearch={onSearch} />
      ) : null}

      {/*
        Every open chart stays mounted and is hidden rather than unmounted, so
        switching patients keeps query caches, the selected chart section, and
        anything already typed into a form. `hidden` also removes the inactive
        chart from the accessibility tree.
      */}
      {openPatients.map(patient => (
        <div key={patient.id} hidden={patient.id !== activeId}>
          <PatientChart client={client} patient={patient} user={user} onClear={() => onClose(patient.id)} />
        </div>
      ))}

      {!activeId ? (
        <>
          {limitReached ? (
            <Notice tone="warning">{t('patients.limitReached', { count: MAX_OPEN_PATIENTS })}</Notice>
          ) : null}
          <PatientRegistry client={client} onSelect={onOpen} />
        </>
      ) : null}
    </div>
  );
}
