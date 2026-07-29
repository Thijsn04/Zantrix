# Roadmap

This is the honest status of Zantrix. The [module vision](modules/README.md) describes the full intended scope. This document describes what is actually being built, in what order, and against what acceptance criteria. If a capability is not listed as underway here, treat it as not started, regardless of how it is described elsewhere.

## Where the project is now

Zantrix is being rebuilt from the foundation up. An earlier prototype existed with partial modules, but it had significant issues: duplicated modules, a hybrid data model that was neither fully custom nor fully FHIR, schema managed by two tools at once, an audit mechanism that did not scale, fabricated integration responses, and a frontend with cosmetic chrome but no design system. The decision was made to rebuild the foundation properly rather than extend that base.

The current phase is **Milestone 1 beta hardening**. Milestone 0 is complete and the Milestone 1 outpatient slice is feature complete and tested. `Beta` here means the documented workflow is implemented and under hardening; it does not mean the system is certified or ready for unsupervised production use.

### Implemented baseline

The following is present in this repository:

- Docker Compose starts PostgreSQL 16, Keycloak 24.0.4, HAPI FHIR 8.10, Snowstorm 10.11.2, Elasticsearch 8.11.1, the backend, and the frontend. PostgreSQL provisions separate application, identity, and FHIR databases.
- The Java 21 / Spring Boot 3.3 backend is a stateless OAuth2 resource server. It maps Keycloak realm roles and SMART scopes, separates the externally validated issuer from private JWK retrieval, and applies central authorization.
- `FhirAccessGateway` is the only application path to HAPI. It supports capabilities, CRUD, search with bounded pagination, history, and transaction bundles. The public `/fhir/R4` facade provides these operations for the supported M0/M1 resources.
- Base R4 resources and every declared profile are validated before mutation. Clinical requests are scope checked, patient-context checked, consent evaluated, and audited.
- The Flyway-owned audit chain supports filtered privacy-officer queries, integrity verification, emergency-access review, and scheduled FHIR AuditEvent export. A durable operation journal reconciles the cross-service FHIR/audit failure window.
- Snowstorm provides SNOMED CT search, expansion, and validation. Licensed RF2 content is loaded by each operator and is never distributed by Zantrix. RxNorm ingredient codes are validated against the NLM API without sending patient data.
- The Milestone 1 modules cover patient/MPI merge and unmerge, outpatient encounters, schedules/slots/appointments, problems, allergies, medication reconciliation/prescribing/administration/dispensing, orders/results, signed notes/addenda, vitals/BMI, high-priority medication safety, Task worklists, consent/privacy, and administration directories/feature flags.
- The React workspace exposes the delivered clinical paths with patient context, role-aware navigation, internationalized copy, responsive themes, explicit error states, and browser-tested OIDC login and patient registration.
- CI builds and tests the backend, verifies Modulith boundaries, runs real PostgreSQL and HAPI integration tests, lints/tests/builds the frontend, runs the complete Compose browser flow, audits production npm dependencies, reviews pull-request dependencies, and enforces house style.

The frontend delivery sequence is planned in the [frontend delivery plan](architecture/frontend-plan.md), and the [implementation plan](implementation-plan.md) breaks the remaining capabilities into executable work packages. Both are design and sequencing only. This roadmap remains the source of truth for what is actually delivered.

## Milestones

The sequence is deliberate. Each milestone must be genuinely production grade, tested, and documented before the next begins. Feature count is not the goal. Quality is.

### Milestone 0: Foundation

Goal: a clean, correct base that every later capability depends on.

- Repository hygiene: remove duplicated modules, remove dev backdoors and fabricated responses, one Flyway owned schema, no Hibernate automatic schema generation.
- FHIR Data Platform stood up on the HAPI FHIR JPA server with the international core profile set.
- Identity and Access Management on Keycloak, with SMART on FHIR scopes and a consistent central authorization layer.
- Audit and Compliance redesigned to be concurrency safe and free of protected health information in logs.
- Continuous integration: build, lint, type check, unit and integration tests with Testcontainers, module boundary verification, and dependency scanning.
- Frontend shell and design system foundation: application shell, patient context, command palette, typed API client, strict TypeScript, accessible primitives, and theming.

Acceptance: the system builds and deploys with one command, continuous integration is green, module boundaries are enforced, and there are no fabricated clinical responses in the codebase.

Status: **complete**.

### Milestone 1: The narrow excellent core

Goal: a usable outpatient EHR built to a production grade standard.

Platform: FHIR Data Platform, IAM, Consent and Privacy, Audit, Terminology, Workflow, Administration.

Patient Administration: Patient and Master Patient Index with real merge and unmerge, Encounters, Scheduling.

Clinical Core: Problems, Allergies, Medications, Orders and Results, Clinical Documentation, Vitals, Clinical Decision Support.

Acceptance: a clinician can register a patient, schedule and hold an encounter, maintain problems and allergies, place and result orders, prescribe with interaction checking, document a note with sign off, and record vitals, all as FHIR resources, all audited, all covered by tests including end to end flows.

Status: **beta complete**. The interaction checker intentionally supplies a transparent, versioned safety floor based on 15 expert-consensus high-priority interaction classes. It is not a comprehensive commercial interaction database. Deployments must retain this limitation in user training and may add a separately licensed provider through the CDS boundary later.

### Milestone 2: Diagnostics and engagement

Goal: extend the core outward where it adds the most value.

Candidates: Laboratory, Radiology and imaging, the Patient Portal, Questionnaires and PROMs, and Coverage and Eligibility with the first regional adapter pack.

Immunizations, the coverage record, and care team and goals have landed ahead of that sequence, because none needed an external system and all sit directly on the existing FHIR gateway. Vaccination forecasting, payer eligibility checking, and CarePlan itself are not included. The [implementation plan](implementation-plan.md) lists which further capabilities need no external system and can be built the same way.

### Milestone 3 and beyond: breadth

Goal: grow into the wider module vision, one capability at a time, each to the same standard. Specialty clinical modules, revenue cycle, operations and supply chain, and data and intelligence follow as demand and contribution allow.

## Status table

This table is the single source of truth for status and is updated as work lands.

| Capability | Milestone | Status |
|---|---|---|
| Repository hygiene | 0 | Done |
| Continuous integration | 0 | Done |
| Platform security foundation (OAuth2 resource server) | 0 | Done |
| FHIR Data Platform (dedicated HAPI FHIR server) | 0 | Done |
| Identity and Access Management (roles, SMART scopes) | 0 | Done |
| Audit and Compliance (hash chained trail) | 0 | Done |
| Frontend shell and design system | 0 | Done |
| Terminology and Ontology, M1 SNOMED/RxNorm slice | 1 | Beta |
| Consent and Privacy, consent/emergency-review slice | 1 | Beta |
| Workflow and Rules Engine, Task worklist slice | 1 | Beta |
| Administration and Configuration, directory/feature-flag slice | 1 | Beta |
| Patient and Master Patient Index | 1 | Beta |
| Encounters and ADT, outpatient slice | 1 | Beta |
| Scheduling and Resource Management | 1 | Beta |
| Problems and Diagnoses | 1 | Beta |
| Allergies and Intolerances | 1 | Beta |
| Medications | 1 | Beta |
| Orders and Results | 1 | Beta |
| Clinical Documentation | 1 | Beta |
| Vitals and Flowsheets, outpatient vital-set slice | 1 | Beta |
| Clinical Decision Support, allergy/high-priority DDI slice | 1 | Beta |
| Immunizations, vaccination history slice | 2 | Beta |
| Coverage record, without eligibility checking | 2 | Beta |
| Care team and goals, without CarePlan | 2 | Beta |
| Treatment relationship enforcement | 1 | Beta, off by default |
| Everything else | 2+ | Planned |

A complete account of what still stands between this system and real clinical use, including the parts that are not software, is in [production readiness](production-readiness.md).

No M0/M1 capability is marked `Stable`. Reaching that status requires production deployment guidance, jurisdictional profiles and policy, performance and disaster-recovery evidence, accessibility assessment, security review, terminology edition governance, clinical safety governance, and certification where applicable.

## How status is kept honest

- A capability moves to `Beta` only when it is feature complete and under test.
- A capability moves to `Stable` only when it is production ready, covered by tests, and documented.
- Integrations that are not yet real are labeled as stubs in the code and are never presented as working.
