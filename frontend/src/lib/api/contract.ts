/**
 * Compile time conformance between the hand written transport types and the
 * contract the backend actually publishes.
 *
 * The generated schema cannot yet be used directly as the application's types:
 * springdoc has no nullability information for Java records, so it reports
 * almost every response field as optional. Adopting that verbatim would make
 * the workspace's types weaker than they are today, not stronger.
 *
 * What the generated schema does describe exactly is which fields exist. That
 * is the failure this file guards, and it is the one that actually bit: three
 * DTOs were transcribed with wrong field names and the compiler accepted all
 * three, because a wrong name is not a type error on the consuming side. Now a
 * backend rename or removal fails this build.
 *
 * Full aliasing onto the generated types waits on the backend annotating
 * nullable record components, which is tracked in the implementation plan.
 * This file contains type declarations only and emits no runtime code.
 */
import type { components } from './generated/schema';
import type {
  AllergySummary, AppointmentSummary, AuditRecord, AuditVerificationResult, ConsentSummary,
  CurrentUser, DirectorySummary, EmergencyAccessReview, EncounterSummary, FeatureFlagView,
  ImmunizationSummary,
  MedicationEventSummary, NoteSummary, OrderSummary, PatientMergeSummary, PatientRegistration,
  PatientSummary, PractitionerSummary, PrescriptionSummary, ProblemSummary, ResultSummary,
  SafetyAssessment, SafetyIssue, SlotSummary, TaskSummary, TermConcept, VitalSummary,
} from './types';

type Schemas = components['schemas'];

/** Resolves to `false` when `Local` names a field the published contract does not have. */
type FieldsExist<Local, Published> = keyof Local extends keyof Published ? true : false;

/** Fails to compile on anything but `true`, which is what turns drift into a build error. */
type Assert<T extends true> = T;

export type ContractConformance = [
  Assert<FieldsExist<CurrentUser, Schemas['CurrentUser']>>,
  Assert<FieldsExist<PatientSummary, Schemas['PatientSummary']>>,
  Assert<FieldsExist<PatientRegistration, Schemas['PatientRegistration']>>,
  Assert<FieldsExist<PatientMergeSummary, Schemas['PatientMergeSummary']>>,
  Assert<FieldsExist<EncounterSummary, Schemas['EncounterSummary']>>,
  Assert<FieldsExist<AppointmentSummary, Schemas['AppointmentSummary']>>,
  Assert<FieldsExist<SlotSummary, Schemas['SlotSummary']>>,
  Assert<FieldsExist<ProblemSummary, Schemas['ProblemSummary']>>,
  Assert<FieldsExist<AllergySummary, Schemas['AllergySummary']>>,
  Assert<FieldsExist<SafetyIssue, Schemas['SafetyIssue']>>,
  Assert<FieldsExist<SafetyAssessment, Schemas['SafetyAssessment']>>,
  Assert<FieldsExist<PrescriptionSummary, Schemas['PrescriptionSummary']>>,
  Assert<FieldsExist<MedicationEventSummary, Schemas['MedicationEventSummary']>>,
  Assert<FieldsExist<OrderSummary, Schemas['OrderSummary']>>,
  Assert<FieldsExist<ResultSummary, Schemas['ResultSummary']>>,
  Assert<FieldsExist<VitalSummary, Schemas['VitalSummary']>>,
  Assert<FieldsExist<NoteSummary, Schemas['NoteSummary']>>,
  Assert<FieldsExist<ImmunizationSummary, Schemas['ImmunizationSummary']>>,
  Assert<FieldsExist<TaskSummary, Schemas['TaskSummary']>>,
  Assert<FieldsExist<ConsentSummary, Schemas['ConsentSummary']>>,
  Assert<FieldsExist<EmergencyAccessReview, Schemas['EmergencyAccessReview']>>,
  Assert<FieldsExist<AuditRecord, Schemas['AuditRecord']>>,
  Assert<FieldsExist<AuditVerificationResult, Schemas['AuditVerificationResult']>>,
  Assert<FieldsExist<PractitionerSummary, Schemas['PractitionerSummary']>>,
  Assert<FieldsExist<DirectorySummary, Schemas['DirectorySummary']>>,
  Assert<FieldsExist<FeatureFlagView, Schemas['FeatureFlagView']>>,
  Assert<FieldsExist<TermConcept, Schemas['TermConcept']>>,
];
