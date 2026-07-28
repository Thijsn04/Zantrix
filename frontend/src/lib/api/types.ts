/**
 * Transport types for the Zantrix application API.
 *
 * Each interface mirrors a backend DTO record exactly. Java `LocalDate` arrives
 * as an ISO date string and `Instant` as an ISO date-time string.
 */

export type Role = 'PHYSICIAN' | 'NURSE' | 'PHARMACIST' | 'ADMIN' | 'PRIVACY_OFFICER' | 'PATIENT';

export interface CurrentUser {
  subject: string;
  username: string | null;
  displayName: string | null;
  roles: string[];
  scopes: string[];
}

export interface ApiErrorBody {
  message?: string;
  detail?: string;
  title?: string;
}

export interface PatientSummary {
  id: string;
  displayName: string;
  birthDate: string;
  administrativeGender: string;
  active: boolean;
  identifier: string | null;
  duplicateScore: number;
}

export interface PatientRegistration {
  givenName: string;
  familyName: string;
  birthDate: string;
  administrativeGender: string;
  email: string | null;
  phone: string | null;
  identifierSystem: string | null;
  identifierValue: string | null;
  confirmedUnique: boolean;
}

export interface PatientMergeSummary {
  id: string;
  sourcePatientId: string;
  targetPatientId: string;
  status: string;
  resourcesRepointed: number;
  mergedAt: string | null;
  unmergedAt: string | null;
}

export interface EncounterSummary {
  id: string;
  patientId: string;
  practitionerId: string | null;
  status: string;
  reason: string | null;
  start: string | null;
  end: string | null;
}

export interface AppointmentSummary {
  id: string;
  patientId: string | null;
  practitionerId: string | null;
  locationId: string | null;
  status: string;
  service: string | null;
  start: string | null;
  end: string | null;
}

export interface SlotSummary {
  id: string;
  scheduleId: string;
  practitionerId: string | null;
  locationId: string | null;
  serviceDisplay: string | null;
  status: string;
  start: string;
  end: string;
}

export interface ProblemSummary {
  id: string;
  patientId: string;
  codeSystem: string;
  code: string;
  display: string;
  clinicalStatus: string;
  onsetDate: string | null;
  resolvedDate: string | null;
}

export interface AllergySummary {
  id: string;
  patientId: string;
  substanceCode: string;
  substance: string;
  clinicalStatus: string;
  verificationStatus: string;
  category: string | null;
  criticality: string | null;
  reaction: string | null;
  severity: string | null;
}

export interface SafetyIssue {
  ruleId: string;
  severity: string;
  summary: string;
  existingMedicationId: string | null;
  source: string | null;
}

export interface SafetyAssessment {
  knowledgeBase: string;
  version: string;
  coverage: string;
  comprehensive: boolean;
  issues: SafetyIssue[];
}

export interface PrescriptionSummary {
  id: string;
  patientId: string;
  rxNormIngredientCode: string;
  medication: string;
  status: string;
  dosage: string | null;
  safety: SafetyAssessment | null;
}

export interface MedicationEventSummary {
  id: string;
  resourceType: string;
  patientId: string;
  prescriptionId: string;
  status: string;
  occurredAt: string;
}

export interface OrderSummary {
  id: string;
  patientId: string;
  encounterId: string | null;
  category: string;
  code: string;
  display: string;
  status: string;
  priority: string | null;
  authoredOn: string | null;
  taskId: string | null;
}

export interface ResultSummary {
  id: string;
  orderId: string | null;
  patientId: string;
  status: string;
  conclusion: string | null;
  issuedAt: string | null;
  observationIds: string[];
}

export interface VitalSummary {
  id: string;
  loincCode: string;
  display: string;
  value: number;
  unit: string;
  observedAt: string | null;
}

export interface NoteSummary {
  id: string;
  patientId: string;
  encounterId: string | null;
  title: string;
  type: string | null;
  status: string;
  authorId: string | null;
  date: string | null;
  version: number;
}

export interface ImmunizationSummary {
  id: string;
  patientId: string;
  vaccineSystem: string;
  vaccineCode: string;
  vaccine: string;
  status: string | null;
  occurrenceDate: string | null;
  lotNumber: string | null;
  site: string | null;
  doseNumber: number | null;
  statusReason: string | null;
}

export interface TaskSummary {
  id: string;
  patientId: string;
  status: string;
  priority: string | null;
  description: string;
  focusReference: string | null;
  ownerReference: string | null;
  authoredOn: string | null;
  dueAt: string | null;
}

export interface ConsentSummary {
  id: string;
  patientId: string;
  status: string;
  type: string;
  resourceTypes: string[];
  start: string | null;
  end: string | null;
}

export interface EmergencyAccessReview {
  id: string;
  occurredAt: string | null;
  actor: string | null;
  reasonCode: string | null;
  patientId: string | null;
  resourceType: string | null;
  resourceId: string | null;
  taskId: string | null;
  status: string;
  reviewedAt: string | null;
  reviewedBy: string | null;
  outcomeCode: string | null;
}

export interface AuditRecord {
  id: number;
  recordedAt: string;
  actor: string | null;
  action: string;
  entityType: string | null;
  entityId: string | null;
  patientId: string | null;
  outcome: string;
  sourceIp: string | null;
  breakTheGlass: boolean;
  exportStatus: string | null;
  fhirAuditEventId: string | null;
}

export interface AuditVerificationResult {
  intact: boolean;
  checkedCount: number;
  brokenAtId: number | null;
}

export interface PractitionerSummary {
  id: string;
  identitySubject: string | null;
  displayName: string;
  roleCode: string | null;
  roleDisplay: string | null;
  organizationId: string | null;
  locationId: string | null;
  active: boolean;
}

export interface DirectorySummary {
  id: string;
  resourceType: string;
  name: string;
  active: boolean;
  managingOrganizationId: string | null;
}

export interface FeatureFlagView {
  name: string;
  enabled: boolean;
  updatedAt: string | null;
}

export interface TermConcept {
  system: string;
  version: string | null;
  code: string;
  display: string;
  inactive: boolean;
}

export interface SystemInfo {
  application: string;
  status: string;
}

/** FhirServerController returns a map; `fhirVersion` and `software` are absent when unreachable. */
export interface FhirServerStatus {
  baseUrl: string;
  reachable: boolean;
  fhirVersion?: string;
  software?: string;
}

export interface TerminologyStatus {
  reachable: boolean;
  fhirVersion: string;
  software: string;
  version: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}
