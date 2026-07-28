# Frontend Screen Inventory

This document lists every screen the two Zantrix surfaces are intended to contain, what each is for, and which backend capability has to exist first.

It is a plan. Screens marked as delivered exist today; everything else does not. The [roadmap](../roadmap.md) remains the source of truth for status, the [frontend plan](frontend-plan.md) explains the architecture and phasing, and the [module vision](../modules/README.md) defines the capabilities referenced in the dependency column.

## How to read this

**Phase** refers to the delivery phases in the [frontend plan](frontend-plan.md): F0 foundation, F1 outpatient core, F2 diagnostics, F3 patient portal, F4 inpatient and acute, F5 revenue and operations, F6 analytics, F7 specialty and regional. `Delivered` means it exists in the current workspace.

**Depends on** names the backend capability by its module vision identifier. Where a screen needs something the backend does not expose yet, that is stated rather than assumed.

**Every screen** carries designed loading, empty, error, permission denied and offline states. That is a standing requirement from the frontend plan and is not repeated per row.

**Gating** is threefold, as described in the plan: the deployment must have the capability enabled, the user's role must permit it, and the clinical context must apply. A screen a user cannot reach is not rendered and its code is not loaded.

---

## A. Shell and session

The frame every other screen sits in. Owned by the shell, not by a feature.

| Screen | Purpose | Route | Depends on | Phase |
|---|---|---|---|---|
| Sign in | Explicit unauthenticated state and OIDC entry | `/signin` | P2 | Delivered |
| Session loading | Honest state while the session resolves | overlay | P2 | Delivered |
| Session error | Identity provider unreachable, recoverable | overlay | P2 | Delivered |
| Idle lock | Covers the record on a shared workstation without losing work | overlay | P2 | Delivered |
| Role dashboard | Where each role starts their day | `/` | varies by role | Delivered |
| Command palette | Keyboard access to navigation, actions and open charts | overlay | none | Delivered |
| Notification centre | Cross capability alerts and messages | `/notifications` | P8 | F3 |
| Not found | Honest failure rather than a blank screen | `/*` | none | Delivered |
| Capability unavailable | Explains why an area is not permitted, rather than rendering nothing | inline | P9 | Delivered |

The role dashboard is one screen with role shaped content, not several. A physician sees their work queue, a privacy officer sees outstanding reviews, an administrator sees the control plane.

---

## B. Cross patient work areas

Screens whose subject is a queue, a day or a department rather than one patient. These are the top level destinations.

| Screen | Purpose | Route | Depends on | Phase |
|---|---|---|---|---|
| Patient search | The entry point to clinical work | `/patients` | A1 | Delivered |
| Patient registration | Two stage registration with duplicate review before creation | `/patients/new` | A1 | Delivered |
| Duplicate review queue | Work the possible duplicates the index has flagged, merge or dismiss | `/patients/duplicates` | A1 | F1 |
| Merge and unmerge | Perform and reverse a merge, with the manifest of what moved | in the identity section | A1 | Delivered |
| Worklist | Tasks assigned to me, unclaimed, and all | `/worklist` | P6 | Delivered |
| Results to review | Results awaiting clinician acknowledgement | `/worklist/results` | C4, C10 | F2 |
| Notes to co-sign | Documentation awaiting a second signature | `/worklist/cosign` | C5 | F2 |
| Results awaiting release | Results the patient cannot see yet, with ageing so none is forgotten | `/worklist/release` | C4, E1, [ADR 0017](decisions/0017-results-release-policy.md) | F3 |
| Staff messages | Secure messaging between staff | `/worklist/messages` | P8 | F3 |
| Prescription requests | Refill and renewal requests awaiting a decision | `/worklist/prescriptions` | C3, E1 | F3 |
| Department schedule | The front desk day view across all patients for a clinic, practitioner or location | `/schedule` | A3 | Delivered |
| Schedule management | Create practitioner schedules and slots | in the appointments section | A3 | Delivered |
| Check in desk | Arrival, waiting list and rooming status for today | `/schedule/checkin` | A3, A4 | F2 |
| Ward list | Patients on a ward, with status at a glance | `/wards/:wardId` | A2 inpatient, O1 | F4 |
| Bed board | Bed occupancy, cleaning and capacity | `/wards/beds` | O1 | F4 |
| Census | Admissions, discharges and transfers over a period | `/wards/census` | A2 inpatient | F4 |
| Emergency trackboard | Live view of the department, triage category and waiting time | `/ed` | S1 | F4 |
| Operating room board | Case scheduling and theatre status | `/or` | S2 | F7 |
| Pharmacy verification queue | Prescriptions awaiting pharmacist verification and dispensing | `/pharmacy` | C3, D6 | F2 |
| Laboratory worklist | Specimens, pending analyses and result entry | `/lab` | D1 | F2 |
| Microbiology worklist | Cultures and sensitivities, which follow a different lifecycle | `/lab/micro` | D2 | F7 |
| Radiology worklist | Imaging orders, protocolling and reporting | `/radiology` | D4 | F2 |
| Coding worklist | Encounters awaiting diagnosis and procedure coding | `/coding` | R1 | F5 |
| Charge capture | Turning recorded activity into billable items | `/charges` | R2 | F5 |
| Claims worklist | Claims, rejections and resubmission | `/claims` | R3 | F5 |
| Transport and services | Patient transport, cleaning and turnover tasks | `/operations` | O2, O3 | F5 |

**The department schedule is deliberately separate from the chart.** A patient's own appointments belong in their chart, which is where they are. A front desk works a day across every patient, which is a different question asked by a different user, and folding it into the chart would force them to pick a patient before they know who is coming. It is served by `GET /api/v1/appointments/day`, which takes the day as explicit instants so no clinic timezone is assumed on the server.

---

## C. The patient chart

Everything about one patient is a section of that patient. A new clinical capability becomes a section here, not a new top level destination.

Route shape is `/patients/:patientId/:section`. The storyboard, carrying identity, active allergies, active problems and the next appointment, is present beside every section.

### C1. Delivered sections

| Section | Contains | Depends on |
|---|---|---|
| Snapshot | The active record on one page: problems, allergies, medications, recent vitals, open orders, recent results, encounter in progress | composite |
| Appointments | This patient's appointments, arrival lifecycle, availability search and booking | A3 |
| Encounters | Encounter list and lifecycle, and selecting the encounter clinical entry belongs to | A2 |
| Problems | Active and resolved problem list, SNOMED coded entry, resolve | C1 |
| Allergies | Allergy and intolerance list, coded entry, inactivate and entered in error | C2 |
| Medications | Active list, prescribing with a safety check, stop, administration, dispensing, reported medication reconciliation | C3, C7 |
| Vitals | The Milestone 1 vital set as a rooming form, with calculated body mass index | C6 |
| Orders | Order entry and the order list | C4 |
| Results | Results, and filing a result against the order that requested it | C4 |
| Notes | Draft, sign and addendum, with version history | C5 |
| Consent | Recording, listing and revoking this patient's consents | P3 |
| Identity | Merge history, merging a duplicate, and reversing a merge | A1 |

### C2. Planned sections

| Section | Purpose | Depends on | Phase |
|---|---|---|---|
| Documents | Scanned and received documents, external correspondence | C5, P7 | F2 |
| Imaging | Studies for this patient and the diagnostic viewer | D5 | F2 |
| Flowsheets | Full flowsheet grid, intake and output, early warning scores | C6 | F4 |
| Care plan and goals | Longitudinal plan, goals and progress | C8 | F5 |
| Care team | Who is involved in this patient's care, and in what role | C8, P2 | F5 |
| Immunizations | Vaccination history and forecast | C9 | F5 |
| Coverage | Insurance, eligibility and financial context | A5 | F5 |
| Consent and privacy | This patient's consents, restrictions and sensitivity flags | P3 | Delivered |
| Authorized representatives | Granting, scoping, expiring and revoking proxy access after staff verify entitlement | P3, E1, [ADR 0018](decisions/0018-proxy-access.md) | F3 |
| Access log | Who accessed this record and why, including emergency access | P4 | F2 |
| Record history | Version history for a resource, from FHIR history | P1 | F2 |
| Questionnaires | Assigned questionnaires and completed responses | E3 | F3 |
| Referrals | Outgoing and incoming referrals and their status | C4, P7 | F5 |
| Episodes of care | Longitudinal grouping across encounters | A2 | F4 |
| Growth charts | Age based percentiles and growth curves | S18 | F7 |
| Genetics | Pedigree and sequencing results | S14 | F7 |
| Wound care | Timeline with measurements and images | S15 | F7 |

The access log is worth calling out. The backend already keeps a tamper evident audit chain, so showing a patient's own access history to an authorized clinician, and later to the patient in the portal, is largely a presentation problem rather than new infrastructure.

### C3. Sub screens inside sections

Sections are not single views. The significant sub screens:

| Section | Sub screens |
|---|---|
| Medications | Active list, prescribe with safety review, override with documented reason, stop with reason, administration record, dispense, reconciliation on admission and discharge |
| Orders | Order list, single order entry, order sets, order details with status history |
| Results | Result list, result detail with reference ranges and interpretation, trend over time, acknowledgement, release to the patient or withhold with a documented reason |
| Notes | Note list, editor with templates and smart phrases, sign, addendum, co-sign request, version comparison |
| Vitals | Rooming form, flowsheet grid, single measurement detail, trend |
| Imaging | Study list, viewer, report, comparison with prior |
| Appointments | Day list, booking, availability search, arrival, completion, cancellation with reason |

---

## D. Administration and governance

For operators, administrators and privacy officers rather than clinicians.

| Screen | Purpose | Route | Depends on | Phase |
|---|---|---|---|---|
| Organizations | The organization directory | `/admin/organizations` | P9 | Delivered |
| Locations | Locations within an organization | `/admin/locations` | P9 | Delivered |
| Practitioners | Staff directory and the link to their login identity | `/admin/practitioners` | P9, P2 | Delivered |
| Practitioner roles | Which role a person holds, where, and for how long | `/admin/roles` | P2 | F1 |
| Feature flags | Enabling capabilities per deployment | `/admin/features` | P9 | Delivered |
| Platform status | Whether the application, FHIR server, terminology service and audit chain are reachable | in administration | P5, P1, P4 | Delivered |
| Value set browser | Inspect and search the value sets clinical entry binds to | `/admin/terminology/valuesets` | P5 | F2 |
| Portal release policy | Choose gated, immediate or delayed release, per result category | `/admin/portal` | E1, [ADR 0017](decisions/0017-results-release-policy.md) | F3 |
| Proxy access policy | The age at which proxy access narrows or ends, which is jurisdictional | `/admin/portal/proxy` | E1, [ADR 0018](decisions/0018-proxy-access.md) | F3 |
| Order set editor | Maintain reusable order sets | `/admin/ordersets` | C4, P6 | F5 |
| Note template editor | Maintain documentation templates and smart phrases | `/admin/templates` | C5 | F5 |
| SMART application registry | Register, enable and review third party applications | `/admin/apps` | P2, [ADR 0016](decisions/0016-smart-application-hosting.md) | F2 |
| Integration monitor | Inbound and outbound message flow, failures and retries | `/admin/integration` | P7 | F7 |
| Emergency access review | Work the mandatory review each emergency access creates | `/privacy/reviews` | P3, P4 | Delivered |
| Audit trail | Filtered access history for a privacy officer | `/privacy/audit` | P4 | Delivered |
| Chain integrity | Verify the audit hash chain and report the result | `/privacy/integrity` | P4 | Delivered |
| Consent registry | Consents across patients, and policy configuration | `/privacy/consents` | P3 | F2 |
| Data subject requests | Access, export and erasure requests under data protection law | `/privacy/requests` | P3 | F5 |
| Operational dashboards | Occupancy, waiting times and throughput | `/analytics` | P10, I1 | F6 |
| Population health | Cohort identification and risk stratification | `/analytics/cohorts` | I2 | F6 |
| Measure reports | Quality measures and reporting | `/analytics/measures` | P10 | F6 |

---

## E. The patient portal

The second surface. Mobile first, public facing, deliberately narrow. It is not a filtered workspace: the same value needs different framing for a patient reading it alone.

| Screen | Purpose | Route | Depends on | Phase |
|---|---|---|---|---|
| Sign in | Patient authentication, separate from staff identity | `/signin` | P2, E1 | F3 |
| Identity verification | Establishing who the account belongs to at enrolment | `/enrol` | E1, A1 | F3 |
| Home | What needs attention: appointments, new results, messages, tasks | `/` | E1 | F3 |
| Appointments | Upcoming and past appointments | `/appointments` | A3, E1 | F3 |
| Book an appointment | Self service booking within what the clinic allows | `/appointments/book` | A3, A4 | F3 |
| Check in | Arriving for an appointment, and completing what is needed first | `/appointments/:id/checkin` | A4 | F3 |
| Results | Results released to the patient, in plain language with guidance | `/results` | C4, E1 | F3 |
| Medications | Current medication and requesting a refill | `/medications` | C3, E1 | F3 |
| Health summary | Problems, allergies and immunizations, explained | `/summary` | C1, C2, C9 | F3 |
| Documents | Letters, discharge summaries and reports | `/documents` | C5 | F3 |
| Messages | Secure messaging with the care team | `/messages` | P8 | F3 |
| Questionnaires | Assigned questionnaires, PROMs and PREMs | `/questionnaires` | E3 | F3 |
| Telehealth | Joining a video consultation | `/appointments/:id/join` | E2 | F7 |
| Profile | Contact details and communication preferences | `/profile` | A1 | F3 |
| Consent and sharing | What the patient has consented to, and withdrawing it | `/privacy` | P3 | F3 |
| Access log | Who has accessed this patient's record | `/privacy/access` | P4 | F3 |
| Acting for someone else | Choosing whose record to view, with the subject named at all times | `/proxy` | E1, A1, [ADR 0018](decisions/0018-proxy-access.md) | F3 |
| My representatives | Who holds access to this patient's own record, and requesting revocation | `/privacy/representatives` | P3, E1 | F3 |
| Request proxy access | Submitting a request for staff to verify, never a self granted permission | `/proxy/request` | E1 | F3 |
| Export my data | Downloading the record in a standard format | `/export` | P1, P10 | F5 |

The patient facing access log is a direct benefit of the tamper evident audit chain, and it is the kind of transparency the [product vision](../vision.md) argues for: the patient can see who looked at their record.

**The results screen is the highest risk screen in the entire product.** A patient reading an abnormal result without a clinician present needs framing, context and a route to help. It does not ship without a decided release policy and clinical governance, which is recorded as an open question in the [frontend plan](frontend-plan.md).

---

## Screens by phase

| Phase | Focus | Approximate new screens |
|---|---|---|
| F0 | Foundation: routing, contract, error boundary, capability states | 3 |
| F1 | Outpatient core completion, duplicate and merge review, consent, schedule management, idle lock | 10 |
| F2 | Diagnostics, results review, imaging, pharmacy and laboratory queues, access log, SMART registry | 16 |
| F3 | The patient portal, proxy access, results release, staff messaging, questionnaires | 26 |
| F4 | Inpatient and acute: wards, beds, census, trackboard, flowsheets, episodes | 8 |
| F5 | Revenue cycle, operations, care plans, immunizations, coverage, templates and order sets | 16 |
| F6 | Analytics and dashboards | 3 |
| F7 | Specialty sections, theatre, microbiology, telehealth, integration monitoring | 9 |

Counts are indicative, not a commitment. They exist to show where the weight of the work sits, which is diagnostics and the portal rather than the specialty phases.

## What this inventory deliberately excludes

- **Screens for capabilities with no backend.** Nothing here exists to demonstrate a capability the server does not have. Where a screen needs an endpoint that is not yet available, the dependency column says so.
- **Settings screens per feature.** Configuration belongs in the administration area, not scattered through the clinical interface.
- **Any screen implying regulatory approval.** Zantrix carries no conformity assessment, and no screen states or suggests one.
