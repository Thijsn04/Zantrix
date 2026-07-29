# Implementation Plan

How Zantrix gets from the current Milestone 1 beta to the complete [screen inventory](architecture/frontend-screens.md).

This is a plan for executing work, not a status document. The [roadmap](roadmap.md) remains the single source of truth for what is delivered. The [frontend delivery plan](architecture/frontend-plan.md) describes the architecture and the phase sequence; this document describes how each phase is actually broken into work that can be started, reviewed and finished.

## The honest size of this

The inventory contains roughly sixty screens that do not exist. They are not sixty frontend tasks. Almost all of them sit on backend capability that has not been built: inpatient admission and transfer, a laboratory system, radiology and imaging, pharmacy dispensing, claims, analytics, patient authentication, results release, and proxy relationships.

Counting only what is genuinely absent, the remaining work is on the order of twenty five backend capabilities and two frontend surfaces. That is a multi year programme at a small team's pace, not a backlog to burn down in a quarter. Sequencing it well matters more than starting it quickly, which is why this document exists before any of it begins.

No dates appear here. Phases and packages are ordered by dependency and value; how fast they move depends on capacity this document cannot know.

## The method: vertical slices

**Every increment delivers a backend capability, its contract, its screens and its tests together, in one reviewable change.**

This is the central rule, and it exists because the alternative already failed once. The previous prototype built interfaces ahead of their servers and filled the gap with fabricated responses, which is why this codebase was restarted. The [frontend delivery plan](architecture/frontend-plan.md) turns that history into a standing rule: nothing is presented as real that is not.

Two consequences follow, and both are deliberate constraints on how work is split:

- **No screen merges without a working server behind it.** Not a stub, not a fixture, not a feature flag hiding an empty panel. If the endpoint is not ready, the screen is not ready.
- **No endpoint merges without a consumer.** An API built ahead of its use gets the shape wrong, and nobody finds out until much later.

A slice is therefore vertical: one capability, end to end, narrow enough to finish. "Laboratory ordering" is a slice. "The laboratory module" is not.

### What a slice contains

A slice is ready to start when the FHIR resources it owns are decided, the endpoints are specified, and the screens are identified in the inventory.

A slice is finished when all of the following hold. This is the same bar the [frontend delivery plan](architecture/frontend-plan.md) sets for calling a capability stable, applied per slice so it is never a later cleanup pass:

- Backend module with enforced boundaries, its resources owned by exactly one module.
- Authorization mirrored into the frontend capability model in the same commit.
- Contract regenerated, drift gate green.
- Screens with designed loading, empty, error, permission denied and offline states.
- Keyboard operable, passing the accessibility gate ([ADR 0013](architecture/decisions/0013-hand-built-design-system.md)).
- Unit, component and integration tests, plus an end to end journey for the role it serves.
- Audit and consent applied through the existing gateway, not around it.
- Documentation updated, including what the slice deliberately does not do.
- Roadmap and screen inventory updated.

## Package 0: Contract and foundations

**This blocks everything and should be finished before any capability package starts.**

Every later package is cheaper with this in place and more expensive without it. Deferring it means every one of the remaining sixty screens is built against hand transcribed types, and [ADR 0011](architecture/decisions/0011-generated-api-contract.md) records what that costs: three DTOs were transcribed wrongly during the workspace rebuild and TypeScript accepted all three.

| Work | Side |
|---|---|
| springdoc-openapi generating the specification from the controllers, committed | Backend, **done** |
| Type generation from that specification | Frontend, **done** |
| Drift gate: continuous integration regenerates and fails on any difference | Both, **done** |
| Nullability annotations on response records, so the generated schema stops reporting every field as optional | Backend |
| Replace the hand written transport types with the generated ones, once the schema is accurate enough to carry them | Frontend |
| FHIR R4 types from StructureDefinitions | Frontend |
| Repository split into `packages/` and `apps/` as the plan describes | Frontend |
| Cross feature import lint rule | Frontend |
| Accessibility gate, component workbench, bundle budgets, visual regression baselines | Frontend |
| Runtime response validation in development and tests | Frontend |

Acceptance: a backend contract change fails the frontend build; every existing primitive passes the accessibility gate; every route has a budget.

The contract half of this is in place. A renamed or removed field now fails the frontend build, which was verified by reintroducing one of the three transcription errors that motivated [ADR 0011](architecture/decisions/0011-generated-api-contract.md). The remaining work is nullability on the backend records, without which the generated types cannot replace the hand written ones outright.

## Capabilities that need no external system

Most of the inventory waits on a subsystem that does not exist: an analyzer, a PACS, a payer, a video provider, a message broker. A meaningful part does not. These sit directly on the FHIR gateway that is already running, so they can be built now, in the vertical slice form above, without waiting for anything.

This is the working queue. It is ordered by clinical value per unit of work.

| Capability | What it needs | Status |
|---|---|---|
| Immunizations (C9) | FHIR Immunization | **Delivered** |
| Coverage record (A5, without eligibility) | FHIR Coverage | **Delivered** |
| Care team and goals (C8) | FHIR CareTeam, Goal | **Delivered** |
| Care plans (C8) | FHIR CarePlan over the above | Next |
| Record history | The gateway already exposes `_history` | Queued |
| Clinical documentation depth (C5) | Templates, order sets, co-signing, all relational | Queued |
| In application messaging (P8, without email or push) | FHIR Communication | Queued |
| Questionnaires and responses (E3, staff facing) | FHIR Questionnaire, QuestionnaireResponse | Queued |
| Inpatient admission, transfer and discharge (A2) | FHIR Encounter and Location. No external system, only workflow | Queued |
| Bed and ward state (O1) | FHIR Location plus Encounter | Queued |
| Flowsheets and early warning scores (C6) | FHIR Observation plus calculation | Queued, see caution |
| Provenance for clinical authorship (P4) | FHIR Provenance | Queued |
| Specimen and result workflow (D1, without an analyzer) | FHIR Specimen, ServiceRequest, Observation | Queued |
| Radiology ordering and reporting (D4, without PACS) | FHIR ServiceRequest, DiagnosticReport | Queued |
| Patient access log | The audit chain already records this | Blocked on a policy decision |

Three entries carry a caveat that is not about effort:

- **Early warning scores** are a published clinical algorithm. Implementing one means the software computes a number a clinician may act on, so it needs the score named, versioned and attributed in the interface, and it needs clinical sign off before it is switched on. It is engineering plus governance, like the medication safety floor in [ADR 0009](architecture/decisions/0009-transparent-medication-safety-floor.md).
- **Laboratory and radiology without their subsystem** deliver the ordering and reporting workflow, not the department. That boundary has to be stated wherever it appears, or a deployment will assume an integration exists.
- **The patient access log** needs no new capability, only a decision about which roles may see who accessed a record. That is a privacy policy question, not an engineering one, and it is the reason this entry is blocked rather than queued.

Everything else in the inventory waits on a subsystem, an identity model, or licensed content, and is covered by the packages below.

## Capability packages

Packages are grouped by the phase they belong to in the [frontend delivery plan](architecture/frontend-plan.md). Each names the backend capability it must build, because that is the part that does not exist.

### Phase F1: complete the outpatient core

| Package | Backend work | Screens |
|---|---|---|
| Chart completion | None. The endpoints exist. | Remaining primitives, full keyboard paths, command palette actions, manual accessibility assessment |
| Read only offline | None | Service worker policy, staleness indicator, write blocking ([ADR 0014](architecture/decisions/0014-read-only-offline.md)) |

This phase is small because the backend is already there. It is the last point at which the delivered core can be brought to standard cheaply, before the surface area triples.

### Phase F2: diagnostics

| Package | Backend work | Screens |
|---|---|---|
| Results review depth | Reference ranges, interpretation, acknowledgement state, trend queries | Result detail, trends, results to review, notes to co-sign |
| Laboratory | D1: specimen lifecycle, analyzer ingestion boundary, result entry | Laboratory worklist, specimen tracking |
| Radiology | D4: imaging orders, protocolling, report authoring | Radiology worklist, protocolling, reporting |
| Imaging viewing | D5: DICOMweb boundary, ImagingStudy | Chart imaging section, study list, viewer |
| SMART hosting | Launch context, application registration, launch scoped tokens ([ADR 0016](architecture/decisions/0016-smart-application-hosting.md)) | Application registry, embedded launch surface |
| Pharmacy queue | D6: verification and dispensing workflow | Pharmacy verification queue |
| Access transparency | None. The audit chain exists. | Patient access log section, record history section |

SMART hosting is placed here rather than later because diagnostic vendors commonly ship this way, and building each viewer instead is not realistic.

### Phase F3: the patient portal

The largest package in the programme, and the one with the longest non engineering lead time.

| Package | Backend work | Screens |
|---|---|---|
| Portal identity | Patient authentication separate from staff, enrolment identity verification | Portal sign in, enrolment |
| Proxy access | RelatedPerson and Consent modelling, tokens carrying a subject distinct from the authenticated person, dual attribution in audit, age based narrowing policy ([ADR 0018](architecture/decisions/0018-proxy-access.md)) | Acting for someone else, my representatives, request access, staff managed authorized representatives |
| Results release | Release state on results, configured policy per category, withholding with a reason ([ADR 0017](architecture/decisions/0017-results-release-policy.md)) | Release worklist with ageing, release and withhold actions, portal results |
| Portal clinical content | Read models scoped to what a patient may see | Home, appointments, medications, health summary, documents |
| Messaging and questionnaires | P8 Notifications, E3 Questionnaires | Portal messages, staff messages, questionnaires, notification centre |

**Two prerequisites here are not engineering work and will not be finished by engineers.** Plain language result content has to be written and clinically reviewed, and the shipped default age for proxy narrowing has to be chosen and documented. Both are recorded as outstanding in the [frontend delivery plan](architecture/frontend-plan.md). Starting the release and proxy packages before they are underway will strand finished code waiting on content.

### Phase F4: inpatient and acute

| Package | Backend work | Screens |
|---|---|---|
| Inpatient ADT | A2 beyond outpatient: admission, transfer, discharge, EpisodeOfCare | Ward list, census, episodes section |
| Bed management | O1: bed state, capacity, cleaning | Bed board |
| Flowsheets | C6 beyond the M1 vital set: general flowsheets, early warning scores | Flowsheet grid, score display |
| Emergency department | S1: triage, trackboard state | Trackboard |

This phase is where density and live updating are hardest. A trackboard needs virtualization, frequent revalidation, and urgency encoded in more than colour.

### Phase F5: revenue cycle and operations

| Package | Backend work | Screens |
|---|---|---|
| Coding and charge capture | R1, R2 | Coding worklist, charge capture |
| Claims | R3 | Claims worklist |
| Coverage | A5 | Coverage chart section |
| Care continuity | C8 care plans and goals, C9 immunizations | Care plan, care team, immunizations sections |
| Content authoring | Order set and note template storage | Order set editor, template editor |
| Operations | O2, O3 | Transport and services |
| Data subject requests | P3 GDPR workflows | Data subject requests, portal export |

### Phase F6: analytics

| Package | Backend work | Screens |
|---|---|---|
| Analytics platform | P10: bulk export, analytics store, query layer | Operational dashboards, cohorts, measure reports |

Dashboards follow an explicit charting standard so every one reads as the same system and stays legible without relying on colour.

### Phase F7: specialty and regional

| Package | Backend work | Screens |
|---|---|---|
| Interoperability | P7: HL7 v2 bridges, subscriptions, adapter pack plug points | Integration monitor |
| Specialty content | S-layer capabilities, one at a time | Growth charts, genetics, wound care, theatre board, microbiology |
| Telehealth | E2 | Portal telehealth |

Each specialty composes the clinical core plus its own content. Regional behaviour arrives as adapter provided configuration, never as branching inside core screens.

## Sequencing and parallel work

Package 0 is serial and blocks everything. After it, three tracks can run in parallel because they touch different backend modules:

- **Platform track:** contract maintenance, SMART hosting, analytics, interoperability.
- **Clinical track:** diagnostics, inpatient, specialty content.
- **Portal track:** identity, proxy, release, patient content.

The portal track should start its governance and content work early even if its engineering starts late, because that work has the longest lead time and cannot be compressed at the end.

Within a track, packages are serial. Across tracks they are independent, except that everything waits on Package 0 and the portal's results release package depends on the diagnostics results work having settled result state first.

## The order recommended here

1. Package 0, alone.
2. F1 chart completion, because it is small and the delivered core should be correct before it is built on.
3. F2 diagnostics, because it is the largest source of clinical value per unit of work and it unblocks the result state the portal needs.
4. F3 portal, with its content and governance work started during F2.
5. F4 onward, by whatever the deployment context actually demands.

The one judgement worth revisiting is whether the portal should come before diagnostics. It is the higher visible value and has the longest non engineering lead time, but it depends on result state that F2 settles, and shipping a patient facing results screen on top of an immature result model is the wrong risk to take.

## Risks that can stop a package

| Risk | Which package | Response |
|---|---|---|
| Plain language result content not written | F3 release | Start content and clinical review during F2, not after |
| Proxy age policy undecided | F3 proxy | Decide and document the shipped default before the package starts |
| Terminology licensing per deployment | Any coded entry | Already fails closed; operators supply their own edition |
| Accessibility gate too slow to sustain | Every frontend package | Per [ADR 0013](architecture/decisions/0013-hand-built-design-system.md), the honest response is a new ADR adopting headless primitives, not a quieter bar |
| Alert fatigue as decision support grows | F2, F4 | Interruptive alerts stay reserved for issues requiring a decision; everything else renders in place |
| Regulatory obligations at the deployment | All | The project supplies software and no conformity assessment. Deploying organizations carry classification, risk management and validation. See the [README](../README.md) |
| Contract drift if Package 0 slips | All | Do not start capability packages before the drift gate is green |

## Keeping this honest

- A screen reaching `main` without a working server behind it is a defect, regardless of how complete it looks.
- The [roadmap](roadmap.md) status table is updated when a package lands, not when it starts.
- The [screen inventory](architecture/frontend-screens.md) phase column moves to `Delivered` only for screens that are actually reachable and tested.
- A capability that turns out larger than its package is split and the plan is corrected, rather than the definition of done being relaxed to fit.
