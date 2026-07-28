import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { CurrentUser, PatientSummary } from '../../lib/api/types';
import { can } from '../../lib/roles';
import { Tabs, type TabDefinition } from '../../design/Tabs';
import { Storyboard } from '../patients/Storyboard';
import { Snapshot } from '../patients/Snapshot';
import { AppointmentDesk } from '../schedule/AppointmentDesk';
import { ClinicalContextBar, ClinicalContextProvider } from './ClinicalContext';
import { EncountersPanel } from './EncountersPanel';
import { ProblemsPanel } from './ProblemsPanel';
import { AllergiesPanel } from './AllergiesPanel';
import { MedicationsPanel } from './MedicationsPanel';
import { VitalsPanel } from './VitalsPanel';
import { OrdersPanel } from './OrdersPanel';
import { ResultsPanel } from './ResultsPanel';
import { NotesPanel } from './NotesPanel';
import { ConsentPanel } from './ConsentPanel';
import { IdentityPanel } from './IdentityPanel';

type ChartTab =
  | 'snapshot' | 'appointments' | 'encounters' | 'problems' | 'allergies'
  | 'medications' | 'vitals' | 'orders' | 'results' | 'notes' | 'consent' | 'identity';

/**
 * The patient chart: a persistent storyboard beside the patient's sections.
 *
 * Everything about one patient lives here as a section of their chart,
 * including their appointments, rather than in a separate area that would
 * need the patient selected all over again.
 *
 * Which sections exist depends on the user's role, matching what the backend
 * will authorize.
 */
export function PatientChart({ client, patient, user, onClear }: {
  client: ApiClient; patient: PatientSummary; user?: CurrentUser; onClear: () => void;
}) {
  const { t } = useTranslation();
  const [tab, setTab] = useState<ChartTab>('snapshot');

  const canChart = can(user, 'chart');
  const canOrders = can(user, 'orders');
  const canMedications = can(user, 'medications');
  const canScheduling = can(user, 'scheduling');
  const canConsent = can(user, 'consents');
  const canMerge = can(user, 'patientMerge');
  const canUnmerge = can(user, 'patientUnmerge');

  const tabs: TabDefinition<ChartTab>[] = [
    { id: 'snapshot', label: t('chart.snapshot') },
    ...(canScheduling ? [{ id: 'appointments' as const, label: t('chart.appointments') }] : []),
    ...(canChart ? [
      { id: 'encounters' as const, label: t('chart.encounters') },
      { id: 'problems' as const, label: t('chart.problems') },
      { id: 'allergies' as const, label: t('chart.allergies') },
    ] : []),
    ...(canMedications ? [{ id: 'medications' as const, label: t('chart.medications') }] : []),
    ...(canChart ? [{ id: 'vitals' as const, label: t('chart.vitals') }] : []),
    ...(canOrders ? [
      { id: 'orders' as const, label: t('chart.orders') },
      { id: 'results' as const, label: t('chart.results') },
    ] : []),
    ...(canChart ? [{ id: 'notes' as const, label: t('chart.notes') }] : []),
    ...(canConsent ? [{ id: 'consent' as const, label: t('chart.consent') }] : []),
    ...(canMerge || canUnmerge ? [{ id: 'identity' as const, label: t('chart.identity') }] : []),
  ];

  // A role change can remove the selected section; fall back to the snapshot.
  const active = tabs.some(definition => definition.id === tab) ? tab : 'snapshot';

  return (
    <ClinicalContextProvider>
      <div className="chart-layout">
        <Storyboard client={client} patient={patient} onClear={onClear}
          canSeeChart={canChart} canSeeScheduling={canScheduling} />
        <div>
          {canChart ? <ClinicalContextBar client={client} patientId={patient.id} /> : null}
          <Tabs label={t('chart.tablist')} tabs={tabs} active={active} onChange={setTab}>
            {active === 'snapshot' ? <Snapshot client={client} patient={patient} canPrescribe={canMedications} /> : null}
            {active === 'appointments' ? (
              <AppointmentDesk client={client} patient={patient} canManageSchedules={can(user, 'manageSchedules')} />
            ) : null}
            {active === 'encounters' ? <EncountersPanel client={client} patientId={patient.id} /> : null}
            {active === 'problems' ? <ProblemsPanel client={client} patientId={patient.id} /> : null}
            {active === 'allergies' ? <AllergiesPanel client={client} patientId={patient.id} /> : null}
            {active === 'medications' ? (
              <MedicationsPanel client={client} patientId={patient.id}
                canPrescribe={can(user, 'prescribe')} canDispense={can(user, 'dispense')} />
            ) : null}
            {active === 'vitals' ? <VitalsPanel client={client} patientId={patient.id} /> : null}
            {active === 'orders' ? <OrdersPanel client={client} patientId={patient.id} /> : null}
            {active === 'results' ? <ResultsPanel client={client} patientId={patient.id} /> : null}
            {active === 'notes' ? <NotesPanel client={client} patientId={patient.id} /> : null}
            {active === 'consent' ? <ConsentPanel client={client} patientId={patient.id} /> : null}
            {active === 'identity' ? (
              <IdentityPanel client={client} patient={patient} canMerge={canMerge} canUnmerge={canUnmerge} />
            ) : null}
          </Tabs>
        </div>
      </div>
    </ClinicalContextProvider>
  );
}
