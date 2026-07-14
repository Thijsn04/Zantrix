# Roadmap

This is the honest status of Zantrix. The [module vision](modules/README.md) describes the full intended scope. This document describes what is actually being built, in what order, and against what acceptance criteria. If a capability is not listed as underway here, treat it as not started, regardless of how it is described elsewhere.

## Where the project is now

Zantrix is being rebuilt from the foundation up. An earlier prototype existed with partial modules, but it had significant issues: duplicated modules, a hybrid data model that was neither fully custom nor fully FHIR, schema managed by two tools at once, an audit mechanism that did not scale, fabricated integration responses, and a frontend with cosmetic chrome but no design system. The decision was made to rebuild the foundation properly rather than extend that base.

The current phase is **foundation and documentation**. This documentation set defines the target. Implementation of the new foundation follows.

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

### Milestone 1: The narrow excellent core

Goal: a usable outpatient EHR built to a production grade standard.

Platform: FHIR Data Platform, IAM, Consent and Privacy, Audit, Terminology, Workflow, Administration.

Patient Administration: Patient and Master Patient Index with real merge and unmerge, Encounters, Scheduling.

Clinical Core: Problems, Allergies, Medications, Orders and Results, Clinical Documentation, Vitals, Clinical Decision Support.

Acceptance: a clinician can register a patient, schedule and hold an encounter, maintain problems and allergies, place and result orders, prescribe with interaction checking, document a note with sign off, and record vitals, all as FHIR resources, all audited, all covered by tests including end to end flows.

### Milestone 2: Diagnostics and engagement

Goal: extend the core outward where it adds the most value.

Candidates: Laboratory, Radiology and imaging, the Patient Portal, Questionnaires and PROMs, and Coverage and Eligibility with the first regional adapter pack.

### Milestone 3 and beyond: breadth

Goal: grow into the wider module vision, one capability at a time, each to the same standard. Specialty clinical modules, revenue cycle, operations and supply chain, and data and intelligence follow as demand and contribution allow.

## Status table

This table is the single source of truth for status and is updated as work lands.

| Capability | Milestone | Status |
|---|---|---|
| Repository hygiene | 0 | Done |
| Continuous integration | 0 | Done |
| Platform security foundation (OAuth2 resource server) | 0 | Done |
| FHIR Data Platform (dedicated HAPI FHIR server) | 0 | In progress |
| Identity and Access Management (roles, SMART scopes) | 0 | In progress |
| Audit and Compliance (hash chained trail) | 0 | In progress |
| Frontend shell and design system | 0 | In progress |

The audit trail records to a tamper evident, privacy safe hash chain today. Rendering audit entries as FHIR AuditEvent resources for export is a near term follow up; the stored fields already carry the FHIR action and outcome codes.
| Terminology and Ontology | 1 | Planned |
| Consent and Privacy | 1 | Planned |
| Workflow and Rules Engine | 1 | Planned |
| Administration and Configuration | 1 | Planned |
| Patient and Master Patient Index | 1 | Planned |
| Encounters and ADT | 1 | Planned |
| Scheduling and Resource Management | 1 | Planned |
| Problems and Diagnoses | 1 | Planned |
| Allergies and Intolerances | 1 | Planned |
| Medications | 1 | Planned |
| Orders and Results | 1 | Planned |
| Clinical Documentation | 1 | Planned |
| Vitals and Flowsheets | 1 | Planned |
| Clinical Decision Support | 1 | Planned |
| Everything else | 2+ | Planned |

## How status is kept honest

- A capability moves to `Beta` only when it is feature complete and under test.
- A capability moves to `Stable` only when it is production ready, covered by tests, and documented.
- Integrations that are not yet real are labeled as stubs in the code and are never presented as working.
