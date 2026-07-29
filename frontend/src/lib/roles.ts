import type { CurrentUser } from './api/types';

/**
 * Capability model for the workspace.
 *
 * Every entry mirrors a backend `@PreAuthorize` rule. The frontend is not the
 * security boundary, but showing a clinician an action the server will reject
 * is a defect, so the two must agree. When a backend rule changes, change it
 * here in the same commit.
 */
export const CAPABILITIES = {
  // PatientController: PHYSICIAN, NURSE, ADMIN
  patients: ['PHYSICIAN', 'NURSE', 'ADMIN'],
  // PatientController#merge: PHYSICIAN, ADMIN
  patientMerge: ['PHYSICIAN', 'ADMIN'],
  // PatientController#unmerge: ADMIN
  patientUnmerge: ['ADMIN'],
  // EncounterController, ProblemController, AllergyController, VitalsController,
  // DocumentationController: PHYSICIAN, NURSE
  chart: ['PHYSICIAN', 'NURSE'],
  // OrderController: PHYSICIAN, NURSE
  orders: ['PHYSICIAN', 'NURSE'],
  // MedicationController: PHYSICIAN, NURSE, PHARMACIST
  medications: ['PHYSICIAN', 'NURSE', 'PHARMACIST'],
  // MedicationController#prescribe: PHYSICIAN, PHARMACIST
  prescribe: ['PHYSICIAN', 'PHARMACIST'],
  // MedicationController#dispense: PHARMACIST
  dispense: ['PHARMACIST'],
  // SchedulingController: PHYSICIAN, NURSE, ADMIN
  scheduling: ['PHYSICIAN', 'NURSE', 'ADMIN'],
  // SchedulingController#schedule: ADMIN
  manageSchedules: ['ADMIN'],
  // WorkflowController: PHYSICIAN, NURSE, PHARMACIST, ADMIN, PRIVACY_OFFICER
  tasks: ['PHYSICIAN', 'NURSE', 'PHARMACIST', 'ADMIN', 'PRIVACY_OFFICER'],
  // CoverageController: PHYSICIAN, NURSE, ADMIN
  coverage: ['PHYSICIAN', 'NURSE', 'ADMIN'],
  // ConsentController: PHYSICIAN, PRIVACY_OFFICER
  consents: ['PHYSICIAN', 'PRIVACY_OFFICER'],
  // AuditController and EmergencyAccessReviewController: PRIVACY_OFFICER
  privacy: ['PRIVACY_OFFICER'],
  // AdministrationController: ADMIN
  administration: ['ADMIN'],
  // TerminologyController: PHYSICIAN, NURSE, PHARMACIST, ADMIN
  terminology: ['PHYSICIAN', 'NURSE', 'PHARMACIST', 'ADMIN'],
} as const satisfies Record<string, readonly string[]>;

export type Capability = keyof typeof CAPABILITIES;

export function can(user: CurrentUser | undefined, capability: Capability): boolean {
  if (!user) return false;
  return CAPABILITIES[capability].some(role => user.roles.includes(role));
}

/**
 * The workspace a user lands on. A pharmacist cannot search patients at all, so
 * their entry point is the worklist rather than the patient registry.
 */
export function primaryRole(user: CurrentUser | undefined): string {
  const ordered = ['PHYSICIAN', 'NURSE', 'PHARMACIST', 'PRIVACY_OFFICER', 'ADMIN'];
  return ordered.find(role => user?.roles.includes(role)) ?? 'PATIENT';
}
