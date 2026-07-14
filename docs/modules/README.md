# Zantrix Module Vision

This document defines how Zantrix is divided into capabilities and how those capabilities fit together. It replaces the earlier flat list of numbered modules with a coherent, layered architecture.

The guiding idea is simple. A small number of **horizontal platform capabilities** provide identity, data, terminology, workflow, and interoperability. Every clinical and operational capability is built on top of that platform and composes it, rather than reinventing it. A cardiology module is not a silo with its own patient store. It is a focused experience over the shared Patient, Encounter, Observation, and Order capabilities, plus a small amount of cardiology specific content.

## How to read this document

Each capability lists:

- **Purpose.** What clinical or operational need it serves.
- **Primary FHIR resources.** The canonical resources it reads and writes. Zantrix is FHIR native, so these resources are the real storage and API surface, not an export format.
- **Depends on.** The platform or clinical capabilities it builds on.
- **Status.** One of `Planned`, `In design`, `In progress`, `Beta`, or `Stable`. At the time of writing every capability is `Planned`, because the project is rebuilding its foundation. Status is tracked for real in the [roadmap](../roadmap.md), and no capability is marked further along than the code supports.

## Status legend

| Status | Meaning |
|---|---|
| `Planned` | Agreed as in scope. Not yet designed in detail. |
| `In design` | Detailed specification and data model being written. |
| `In progress` | Implementation underway, not feature complete. |
| `Beta` | Feature complete, under testing and hardening. |
| `Stable` | Production ready, covered by tests and documentation. |

## Design principles for every capability

1. **FHIR native.** State is stored as FHIR resources in the FHIR platform. Capabilities do not create parallel private tables for clinical data that FHIR already models.
2. **Composed, not copied.** A capability reuses platform capabilities. It does not re-implement patients, identity, audit, or terminology.
3. **Standards over local invention.** Coded data uses SNOMED CT, LOINC, ICD, RxNorm, and similar. Free local codes are a last resort and are always mapped.
4. **Region neutral core, regional adapters.** Country specific behaviour is never hardcoded into the core. It is provided by adapter packs (see the Interoperability and Localization capability).
5. **Off by default.** Capabilities can be enabled per deployment. A clinic runs a handful. A hospital runs many. The core is the same.
6. **Secure and audited by construction.** Every read and write flows through the access control and audit capabilities.

---

## Layer 0: Platform

The horizontal foundation. Everything else depends on this layer. This is where the majority of early effort goes, because the quality of the platform sets the ceiling for every capability above it.

### P1. FHIR Data Platform
- **Purpose.** The canonical clinical data store and REST API. Hosts the HAPI FHIR R4 JPA server, resource validation against profiles, search, history, transactions, and FHIR operations.
- **Primary FHIR resources.** All resource types. CapabilityStatement, StructureDefinition, OperationDefinition.
- **Depends on.** PostgreSQL, Elasticsearch.
- **Status.** `Planned`.

### P2. Identity and Access Management
- **Purpose.** Authentication, authorization, sessions, and the practitioner and organization directory. Role based and attribute based access, SMART on FHIR scopes, and single sign on.
- **Primary FHIR resources.** Practitioner, PractitionerRole, Organization, Location, Group, Person.
- **Depends on.** Keycloak, FHIR Data Platform.
- **Status.** `Planned`.

### P3. Consent and Privacy
- **Purpose.** Patient consent, sensitive record flags, and GDPR data subject workflows including access, export, and erasure requests. Enforces consent aware filtering of clinical data.
- **Primary FHIR resources.** Consent, Flag.
- **Depends on.** IAM, Audit, FHIR Data Platform.
- **Status.** `Planned`.

### P4. Audit and Compliance
- **Purpose.** A tamper evident record of every access and change, recorded as FHIR AuditEvent, with a verifiable hash chain and reporting for privacy officers. Includes break the glass review.
- **Primary FHIR resources.** AuditEvent, Provenance.
- **Depends on.** IAM.
- **Status.** `Planned`.

### P5. Terminology and Ontology
- **Purpose.** Terminology services for SNOMED CT, LOINC, ICD-10 and ICD-11, RxNorm, and local value sets. Supports lookup, validate-code, translate, and value set expansion, backed by fast search.
- **Primary FHIR resources.** CodeSystem, ValueSet, ConceptMap, NamingSystem.
- **Depends on.** FHIR Data Platform, Elasticsearch.
- **Status.** `Planned`.

### P6. Workflow and Rules Engine
- **Purpose.** Cross capability tasks, protocols, and automation. Drives worklists, handovers, and reminders. Hosts the CDS Hooks service used by clinical decision support.
- **Primary FHIR resources.** Task, PlanDefinition, ActivityDefinition, CarePlan.
- **Depends on.** FHIR Data Platform, IAM.
- **Status.** `Planned`.

### P7. Interoperability and Localization
- **Purpose.** Inbound and outbound integration, and the home for regional adapter packs. Translates HL7 v2 and other formats to and from FHIR, hosts FHIR Subscriptions, and provides the plug points for country specific systems. The Dutch pack (BSN and SBV-Z verification, national exchange, VECOZO eligibility, DBC coding) is the first adapter pack and is disabled by default.
- **Primary FHIR resources.** MessageHeader, Subscription, Bundle, plus adapter specific mappings.
- **Depends on.** FHIR Data Platform, Apache Camel.
- **Status.** `Planned`.

### P8. Notifications and Messaging
- **Purpose.** Secure in application messaging, notifications, and outbound email or push for staff and patients.
- **Primary FHIR resources.** Communication, CommunicationRequest.
- **Depends on.** IAM, Workflow.
- **Status.** `Planned`.

### P9. Administration and Configuration
- **Purpose.** Tenant, organization, and location administration, feature flags for enabling capabilities, and system settings. The control plane for running Zantrix.
- **Primary FHIR resources.** Organization, Location, HealthcareService, Endpoint.
- **Depends on.** IAM.
- **Status.** `Planned`.

### P10. Analytics and Reporting Platform
- **Purpose.** FHIR Bulk Data export, an analytics friendly data store, and the query layer that dashboards, population health, and research build on. Keeps analytics load off the operational store.
- **Primary FHIR resources.** All resource types via Bulk Data, MeasureReport.
- **Depends on.** FHIR Data Platform.
- **Status.** `Planned`.

---

## Layer 1: Patient Administration

Who the patient is, where they are, and when they are seen.

### A1. Patient and Master Patient Index
- **Purpose.** The single source of truth for patient demographics. Probabilistic duplicate detection, safe merge and unmerge that re-point every referencing resource, VIP and sensitive flags.
- **Primary FHIR resources.** Patient, RelatedPerson, Person, Linkage.
- **Depends on.** IAM, Consent, Terminology.
- **Status.** `Planned`.

### A2. Encounters and ADT
- **Purpose.** The encounter lifecycle across outpatient, inpatient, and emergency. Admission, discharge, and transfer, with bed and census views for inpatient care.
- **Primary FHIR resources.** Encounter, EpisodeOfCare, Location.
- **Depends on.** Patient and MPI, Administration.
- **Status.** `Planned`.

### A3. Scheduling and Resource Management
- **Purpose.** Calendars for practitioners, rooms, and equipment. Appointment booking with conflict detection, waitlists, and slot management.
- **Primary FHIR resources.** Appointment, Slot, Schedule, ServiceRequest.
- **Depends on.** Patient and MPI, IAM, Encounters.
- **Status.** `Planned`.

### A4. Registration, Check-in, and Kiosk
- **Purpose.** Front desk registration and patient self service check in, including identity capture and questionnaire completion.
- **Primary FHIR resources.** Patient, Coverage, QuestionnaireResponse.
- **Depends on.** Patient and MPI, Scheduling.
- **Status.** `Planned`.

### A5. Coverage and Eligibility
- **Purpose.** Insurance and coverage records and eligibility checks. Eligibility calls to national payers are provided by regional adapter packs.
- **Primary FHIR resources.** Coverage, CoverageEligibilityRequest, CoverageEligibilityResponse.
- **Depends on.** Patient and MPI, Interoperability and Localization.
- **Status.** `Planned`.

---

## Layer 2: Clinical Core

The heart of the record. These capabilities are the narrow core that Zantrix makes excellent first.

### C1. Problems and Diagnoses
- **Purpose.** The active and resolved problem list, coded to SNOMED CT and ICD.
- **Primary FHIR resources.** Condition.
- **Depends on.** Patient and MPI, Terminology.
- **Status.** `Planned`.

### C2. Allergies and Intolerances
- **Purpose.** Coded allergies and intolerances with substances, reactions, and criticality, feeding decision support.
- **Primary FHIR resources.** AllergyIntolerance.
- **Depends on.** Patient and MPI, Terminology.
- **Status.** `Planned`.

### C3. Medications
- **Purpose.** The full medication lifecycle. Reconciliation, prescribing through computerized provider order entry, the administration record, and dispensing.
- **Primary FHIR resources.** MedicationRequest, MedicationStatement, MedicationAdministration, MedicationDispense, Medication.
- **Depends on.** Patient and MPI, Terminology, Clinical Decision Support, Orders.
- **Status.** `Planned`.

### C4. Orders and Results
- **Purpose.** Computerized provider order entry for labs, imaging, procedures, and referrals, and the results that come back, with acknowledgement and worklists.
- **Primary FHIR resources.** ServiceRequest, DiagnosticReport, Observation, Specimen, Task.
- **Depends on.** Patient and MPI, Terminology, Workflow.
- **Status.** `Planned`.

### C5. Clinical Documentation
- **Purpose.** Structured and narrative notes, templates and smart phrases, addenda, co-signing, and full version history.
- **Primary FHIR resources.** Composition, DocumentReference, ClinicalImpression.
- **Depends on.** Patient and MPI, Encounters, Terminology.
- **Status.** `Planned`.

### C6. Vitals and Flowsheets
- **Purpose.** Vital signs, intake and output, flowsheets, and early warning scores such as NEWS and MEWS.
- **Primary FHIR resources.** Observation.
- **Depends on.** Patient and MPI, Terminology.
- **Status.** `Planned`.

### C7. Clinical Decision Support
- **Purpose.** Drug to drug and drug to allergy interaction checks, dose range checks, and protocol guidance, delivered through CDS Hooks so alerts fire at the point of ordering.
- **Primary FHIR resources.** DetectedIssue, GuidanceResponse, PlanDefinition.
- **Depends on.** Workflow, Medications, Allergies, Terminology.
- **Status.** `Planned`.

### C8. Care Plans and Goals
- **Purpose.** Longitudinal care plans, goals, and care team coordination.
- **Primary FHIR resources.** CarePlan, Goal, CareTeam.
- **Depends on.** Patient and MPI, Workflow.
- **Status.** `Planned`.

### C9. Immunizations
- **Purpose.** Vaccination history and forecasting.
- **Primary FHIR resources.** Immunization, ImmunizationRecommendation.
- **Depends on.** Patient and MPI, Terminology.
- **Status.** `Planned`.

### C10. Results Review and Clinician Inbox
- **Purpose.** The clinician facing worklist and inbox that unifies results, tasks, messages, and items needing sign off.
- **Primary FHIR resources.** Task, Communication, DiagnosticReport.
- **Depends on.** Orders and Results, Workflow, Notifications.
- **Status.** `Planned`.

---

## Layer 3: Diagnostics and Ancillary Services

Departments that produce and manage diagnostic data. Each builds on Orders and Results rather than defining its own ordering flow.

| Capability | Purpose | Primary FHIR resources | Status |
|---|---|---|---|
| D1. Laboratory (LIS) | Chemistry and hematology, analyzer integration | ServiceRequest, Specimen, Observation, DiagnosticReport | `Planned` |
| D2. Microbiology | Cultures, sensitivities, resistance | Observation, DiagnosticReport | `Planned` |
| D3. Pathology | Tissue and biopsy workflow, macroscopy | ServiceRequest, DiagnosticReport, Specimen | `Planned` |
| D4. Radiology (RIS) | Imaging orders, protocolling, reporting | ServiceRequest, ImagingStudy, DiagnosticReport | `Planned` |
| D5. Imaging and PACS (DICOMweb) | Diagnostic image viewing inside the record | ImagingStudy, Media | `Planned` |
| D6. Pharmacy | Inpatient and outpatient dispensing and stock | MedicationDispense, SupplyRequest | `Planned` |
| D7. Blood Bank | Typing, issue, and transfusion records | Observation, BiologicallyDerivedProduct | `Planned` |
| D8. Nutrition and Dietetics | Diet orders, tube feeding, kitchen integration | NutritionOrder | `Planned` |

---

## Layer 4: Specialty Clinical

Specialty experiences composed from the Clinical Core plus specialty content. They add focused workflows and coded content, not parallel infrastructure.

| Capability | Focus | Status |
|---|---|---|
| S1. Emergency Department | Triage, trackboard, rapid ordering | `Planned` |
| S2. Perioperative and Operating Room | Case scheduling, counts, pre and post op | `Planned` |
| S3. Anesthesia | Intraoperative record | `Planned` |
| S4. Intensive Care | High density monitoring and device data | `Planned` |
| S5. Maternity and Obstetrics | Pregnancy record, partogram | `Planned` |
| S6. Neonatology | Growth and feeding for newborns | `Planned` |
| S7. Oncology | Multi day chemotherapy protocols and dosing | `Planned` |
| S8. Cardiology | ECG and echo integration | `Planned` |
| S9. Nephrology and Dialysis | Dialysis protocols | `Planned` |
| S10. Behavioral Health | Seclusion records, risk assessment, strict privacy | `Planned` |
| S11. Rehabilitation and Orthopedics | Functional scores, prosthesis registry | `Planned` |
| S12. Ophthalmology | Refraction and retinal documentation | `Planned` |
| S13. Dental and Maxillofacial | Odontogram | `Planned` |
| S14. Clinical Genetics | Pedigree and sequencing results | `Planned` |
| S15. Wound Care | Timeline with photo capture and measurements | `Planned` |
| S16. Infection Control | Multi resistant organism detection and contact tracing | `Planned` |
| S17. Transplant | Donor and recipient matching | `Planned` |
| S18. Pediatrics | Age based reference ranges and growth curves | `Planned` |
| S19. Endoscopy | Procedure workflow and image capture | `Planned` |

---

## Layer 5: Patient Engagement

Capabilities that reach the patient directly, mostly delivered as SMART on FHIR patient facing applications.

| Capability | Purpose | Primary FHIR resources | Status |
|---|---|---|---|
| E1. Patient Portal | Appointments, results, documents, messaging | Patient, Appointment, DiagnosticReport, Communication | `Planned` |
| E2. Telehealth | Video consultation alongside the record | Encounter, Appointment | `Planned` |
| E3. Questionnaires, PROMs and PREMs | Outcome and experience measures | Questionnaire, QuestionnaireResponse | `Planned` |
| E4. Remote Patient Monitoring | Device and wearable data ingestion | Observation, Device | `Planned` |
| E5. Outreach and Recall | Preventive care campaigns and reminders | CommunicationRequest, Group | `Planned` |

---

## Layer 6: Revenue Cycle

Financial capabilities. Regional billing rules are provided by adapter packs. The core models charges and claims in a standard way.

| Capability | Purpose | Primary FHIR resources | Status |
|---|---|---|---|
| R1. Coding | Diagnosis and procedure coding | Condition, Procedure, ChargeItem | `Planned` |
| R2. Charge Capture | Turning activity into billable items | ChargeItem, ChargeItemDefinition | `Planned` |
| R3. Claims and Billing | Claims, invoices, and patient statements | Claim, ClaimResponse, Invoice | `Planned` |
| R4. Contract Management | Payer price agreements and caps | Contract | `Planned` |
| R5. Cost Accounting | Cost versus revenue per treatment | ChargeItem, Invoice | `Planned` |

---

## Layer 7: Operations and Supply Chain

The logistics that keep a facility running.

| Capability | Purpose | Primary FHIR resources | Status |
|---|---|---|---|
| O1. Bed Management | Bed status and capacity | Location, Encounter | `Planned` |
| O2. Patient Transport | Moving patients within a facility | Task, Location | `Planned` |
| O3. Environmental Services | Cleaning and turnover of beds and rooms | Task, Location | `Planned` |
| O4. Supply Chain and Inventory | Procurement, stock, and implant registry | SupplyRequest, SupplyDelivery, Device | `Planned` |
| O5. Sterile Processing | Instrument sterilization tracking | Device, Task | `Planned` |
| O6. Medical Device Integration | Live data from monitors and pumps | Device, Observation | `Planned` |
| O7. Workforce and Rostering | Staff scheduling and capacity | PractitionerRole, Schedule | `Planned` |

---

## Layer 8: Data and Intelligence

Capabilities that turn the record into insight, all built on the Analytics and Reporting Platform.

| Capability | Purpose | Primary FHIR resources | Status |
|---|---|---|---|
| I1. Operational Dashboards | Real time KPIs such as occupancy and wait times | MeasureReport | `Planned` |
| I2. Population Health | Cohort identification and risk stratification | Group, Measure, MeasureReport | `Planned` |
| I3. Research and Clinical Trials | De-identified cohort data for research | ResearchStudy, ResearchSubject | `Planned` |
| I4. Predictive Models | Models such as sepsis risk and no show prediction | RiskAssessment | `Planned` |

---

## The narrow excellent core

Zantrix does not try to build all of the above at once. The first goal is a small set of capabilities, built to a genuinely production grade standard, that together form a usable EHR for an outpatient setting. That core is:

- **Platform:** P1 FHIR Data Platform, P2 IAM, P3 Consent and Privacy, P4 Audit, P5 Terminology, P6 Workflow, P9 Administration.
- **Patient Administration:** A1 Patient and MPI, A2 Encounters, A3 Scheduling.
- **Clinical Core:** C1 Problems, C2 Allergies, C3 Medications, C4 Orders and Results, C5 Clinical Documentation, C6 Vitals, C7 Clinical Decision Support.

Everything else in this document is deliberately deferred until the core is excellent. The sequencing and acceptance criteria live in the [roadmap](../roadmap.md).
