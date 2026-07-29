import { screen, within, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { MedicationsPanel } from './MedicationsPanel';
import { ClinicalContext } from './clinicalContext';
import type { PrescriptionSummary, SafetyAssessment } from '../../lib/api/types';
import { fakeClient, renderWithQuery } from '../../test/render';
import type { ReactNode } from 'react';

const READY_CONTEXT = {
  encounterId: 'e-1', practitionerId: 'pr-9',
  setEncounterId: () => undefined, setPractitionerId: () => undefined, ready: true,
};

function withContext(children: ReactNode) {
  return <ClinicalContext.Provider value={READY_CONTEXT}>{children}</ClinicalContext.Provider>;
}

const prescription: PrescriptionSummary = {
  id: 'm-1', patientId: 'p-1', rxNormIngredientCode: '6809', medication: 'Metformin',
  status: 'active', dosage: '500 mg twice daily', safety: null,
};

const clean: SafetyAssessment = {
  knowledgeBase: 'Zantrix high-priority interactions', version: '1.0.0',
  coverage: '15 high-priority interaction classes plus coded allergies', comprehensive: false, issues: [],
};

const critical: SafetyAssessment = {
  ...clean,
  issues: [{
    ruleId: 'DRUG-ALLERGY', severity: 'critical',
    summary: 'The patient has an active coded allergy to Penicillin.',
    existingMedicationId: null, source: 'FHIR AllergyIntolerance/a-1',
  }],
};

async function fillPrescription(user: ReturnType<typeof userEvent.setup>) {
  // Reported medication reuses these field names, so scope to the prescribing panel.
  const form = within(screen.getByRole('region', { name: 'Prescribe' }));
  const field = (name: string) => form.getByLabelText(/./, { selector: `[name="${name}"]` });
  await user.type(field('rxNormIngredientCode'), '6809');
  await user.type(field('medicationDisplay'), 'Metformin');
  await user.type(field('dosageText'), '500 mg twice daily');
  await user.type(field('doseValue'), '500');
  await user.type(field('doseUnit'), 'mg');
  await user.type(field('frequencyPerDay'), '2');
  await user.type(field('durationDays'), '30');
  await user.type(field('dispenseQuantity'), '60');
}

describe('medication safety', () => {
  it('runs a safety check before writing the prescription', async () => {
    const user = userEvent.setup();
    const get = vi.fn().mockImplementation((path: string) =>
      path.startsWith('/api/v1/medications/safety-check') ? Promise.resolve(clean) : Promise.resolve([]));
    const post = vi.fn().mockResolvedValue({});
    const { client } = fakeClient({ get, post });
    renderWithQuery(withContext(
      <MedicationsPanel client={client} patientId="p-1" canPrescribe canDispense={false} />));

    await fillPrescription(user);
    await user.click(screen.getByRole('button', { name: /Run safety check/ }));

    expect(await screen.findByText(/No interactions found/)).toBeInTheDocument();
    // Nothing has been prescribed yet.
    expect(post).not.toHaveBeenCalled();

    await user.click(screen.getByRole('button', { name: /^Prescribe$/ }));
    await waitFor(() => expect(post).toHaveBeenCalledWith('/api/v1/medications', expect.objectContaining({
      rxNormIngredientCode: '6809', safetyOverrideReason: null, requesterId: 'pr-9', encounterId: 'e-1',
    })));
  });

  it('blocks a critical interaction behind a documented override', async () => {
    const user = userEvent.setup();
    const get = vi.fn().mockImplementation((path: string) =>
      path.startsWith('/api/v1/medications/safety-check') ? Promise.resolve(critical) : Promise.resolve([]));
    const post = vi.fn().mockResolvedValue({});
    const { client } = fakeClient({ get, post });
    vi.spyOn(window, 'prompt').mockReturnValue('Benefit outweighs risk, patient monitored');
    renderWithQuery(withContext(
      <MedicationsPanel client={client} patientId="p-1" canPrescribe canDispense={false} />));

    await fillPrescription(user);
    await user.click(screen.getByRole('button', { name: /Run safety check/ }));

    expect(await screen.findByText(/active coded allergy/)).toBeInTheDocument();
    // The unqualified prescribe action is not offered for a critical issue.
    expect(screen.queryByRole('button', { name: /^Prescribe$/ })).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /Override and prescribe/ }));
    await waitFor(() => expect(post).toHaveBeenCalledWith('/api/v1/medications', expect.objectContaining({
      safetyOverrideReason: 'Benefit outweighs risk, patient monitored',
    })));
  });

  it('always states that the knowledge base is not comprehensive', async () => {
    const user = userEvent.setup();
    const get = vi.fn().mockImplementation((path: string) =>
      path.startsWith('/api/v1/medications/safety-check') ? Promise.resolve(clean) : Promise.resolve([]));
    const { client } = fakeClient({ get });
    renderWithQuery(withContext(
      <MedicationsPanel client={client} patientId="p-1" canPrescribe canDispense={false} />));

    await fillPrescription(user);
    await user.click(screen.getByRole('button', { name: /Run safety check/ }));
    expect(await screen.findByText(/not a comprehensive interaction database/)).toBeInTheDocument();
  });
});

describe('medication actions by role', () => {
  it('offers dispensing only to a pharmacist', async () => {
    const { client } = fakeClient({ get: vi.fn().mockResolvedValue([prescription]) });
    const { unmount } = renderWithQuery(withContext(
      <MedicationsPanel client={client} patientId="p-1" canPrescribe={false} canDispense={false} />));
    await screen.findByText('Metformin');
    expect(screen.queryByRole('button', { name: 'Dispense' })).not.toBeInTheDocument();
    unmount();

    renderWithQuery(withContext(
      <MedicationsPanel client={client} patientId="p-1" canPrescribe={false} canDispense />));
    expect(await screen.findByRole('button', { name: 'Dispense' })).toBeInTheDocument();
  });

  it('records an administration against the prescription', async () => {
    const user = userEvent.setup();
    const post = vi.fn().mockResolvedValue({});
    const { client } = fakeClient({ get: vi.fn().mockResolvedValue([prescription]), post });
    renderWithQuery(withContext(
      <MedicationsPanel client={client} patientId="p-1" canPrescribe={false} canDispense={false} />));

    await user.click(await screen.findByRole('button', { name: 'Administer' }));
    await waitFor(() => expect(post).toHaveBeenCalledWith('/api/v1/medications/administrations',
      expect.objectContaining({ patientId: 'p-1', prescriptionId: 'm-1', performerId: 'pr-9' })));
  });
});
