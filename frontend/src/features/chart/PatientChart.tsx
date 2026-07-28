import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { ApiClient } from '../../lib/api/client';
import type { CurrentUser, PatientSummary } from '../../lib/api/types';
import { can } from '../../lib/roles';
import { Tabs, type TabDefinition } from '../../design/Tabs';
import { Storyboard } from '../patients/Storyboard';
import { Snapshot } from '../patients/Snapshot';
import { ClinicalContextBar, ClinicalContextProvider } from './ClinicalContext';
import { EncountersPanel } from './EncountersPanel';
import { ProblemsPanel } from './ProblemsPanel';
import { AllergiesPanel } from './AllergiesPanel';
import { MedicationsPanel } from './MedicationsPanel';
import { VitalsPanel } from './VitalsPanel';
import { OrdersPanel } from './OrdersPanel';
import { NotesPanel } from './NotesPanel';

type ChartTab = 'snapshot' | 'encounters' | 'problems' | 'allergies' | 'medications' | 'vitals' | 'orders' | 'notes';

/**
 * The patient chart: a persistent storyboard beside the active workspace.
 *
 * Which tabs exist depends on the user's role, matching what the backend will
 * authorize. A pharmacist never reaches this screen because they cannot read
 * the patient directory at all.
 */
export function PatientChart({ client, patient, user, onClear }: {
  client: ApiClient; patient: PatientSummary; user?: CurrentUser; onClear: () => void;
}) {
  const { t } = useTranslation();
  const [tab, setTab] = useState<ChartTab>('snapshot');

  const canChart = can(user, 'chart');
  const canOrders = can(user, 'orders');
  const canMedications = can(user, 'medications');
  const canPrescribe = can(user, 'prescribe');
  const canDispense = can(user, 'dispense');

  const tabs: TabDefinition<ChartTab>[] = [
    { id: 'snapshot', label: t('chart.snapshot') },
    ...(canChart ? [
      { id: 'encounters' as const, label: t('chart.encounters') },
      { id: 'problems' as const, label: t('chart.problems') },
      { id: 'allergies' as const, label: t('chart.allergies') },
    ] : []),
    ...(canMedications ? [{ id: 'medications' as const, label: t('chart.medications') }] : []),
    ...(canChart ? [{ id: 'vitals' as const, label: t('chart.vitals') }] : []),
    ...(canOrders ? [{ id: 'orders' as const, label: t('chart.orders') }] : []),
    ...(canChart ? [{ id: 'notes' as const, label: t('chart.notes') }] : []),
  ];

  return (
    <ClinicalContextProvider>
      <div className="chart-layout">
        <Storyboard client={client} patient={patient} onClear={onClear}
          canSeeChart={canChart} canSeeScheduling={can(user, 'scheduling')} />
        <div>
          {canChart ? <ClinicalContextBar client={client} patientId={patient.id} /> : null}
          <Tabs label={t('chart.tablist')} tabs={tabs} active={tab} onChange={setTab}>
            {tab === 'snapshot' ? <Snapshot client={client} patient={patient} canPrescribe={canMedications} /> : null}
            {tab === 'encounters' ? <EncountersPanel client={client} patientId={patient.id} /> : null}
            {tab === 'problems' ? <ProblemsPanel client={client} patientId={patient.id} /> : null}
            {tab === 'allergies' ? <AllergiesPanel client={client} patientId={patient.id} /> : null}
            {tab === 'medications' ? (
              <MedicationsPanel client={client} patientId={patient.id}
                canPrescribe={canPrescribe} canDispense={canDispense} />
            ) : null}
            {tab === 'vitals' ? <VitalsPanel client={client} patientId={patient.id} /> : null}
            {tab === 'orders' ? <OrdersPanel client={client} patientId={patient.id} /> : null}
            {tab === 'notes' ? <NotesPanel client={client} patientId={patient.id} /> : null}
          </Tabs>
        </div>
      </div>
    </ClinicalContextProvider>
  );
}
