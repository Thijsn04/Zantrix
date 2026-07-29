import { useMemo, useState, type PropsWithChildren } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { EncounterSummary } from '../../lib/api/types';
import { Field } from '../../design/Field';
import { formatDateTime } from '../../lib/format';
import { ClinicalContext, useClinicalContext } from './clinicalContext';

export function ClinicalContextProvider({ children }: PropsWithChildren) {
  const [encounterId, setEncounterId] = useState('');
  const [practitionerId, setPractitionerId] = useState('');
  const value = useMemo(() => ({
    encounterId, practitionerId, setEncounterId, setPractitionerId,
    ready: encounterId.trim().length > 0 && practitionerId.trim().length > 0,
  }), [encounterId, practitionerId]);
  return <ClinicalContext.Provider value={value}>{children}</ClinicalContext.Provider>;
}

/** Compact context bar shown above the chart tabs. */
export function ClinicalContextBar({ client, patientId }: { client: ApiClient; patientId: string }) {
  const { t, i18n } = useTranslation();
  const { encounterId, practitionerId, setEncounterId, setPractitionerId } = useClinicalContext();
  const encounters = useQuery({
    queryKey: ['encounters', patientId],
    queryFn: ({ signal }) => client.get<EncounterSummary[]>(
      `/api/v1/encounters?patientId=${encodeURIComponent(patientId)}`, signal),
  });
  const open = (encounters.data ?? []).filter(
    encounter => encounter.status === 'planned' || encounter.status === 'in-progress');

  return (
    <div className="context-bar">
      <Field label={t('clinical.encounter')} hint={t('clinical.encounterHint')}>
        {(id, describedBy) => (
          <select id={id} value={encounterId} aria-describedby={describedBy}
            onChange={event => setEncounterId(event.target.value)}>
            <option value="">{t('clinical.selectEncounter')}</option>
            {open.map(encounter => (
              <option key={encounter.id} value={encounter.id}>
                {`${encounter.reason ?? encounter.id} - ${formatDateTime(encounter.start, i18n.language)}`}
              </option>
            ))}
          </select>
        )}
      </Field>
      <Field label={t('clinical.practitionerId')} hint={t('clinical.practitionerHint')}>
        {(id, describedBy) => (
          <input id={id} value={practitionerId} aria-describedby={describedBy}
            onChange={event => setPractitionerId(event.target.value)} />
        )}
      </Field>
    </div>
  );
}
