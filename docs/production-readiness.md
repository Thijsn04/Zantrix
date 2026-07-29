# Production Readiness

What stands between Zantrix as it is today and Zantrix being used to deliver real care.

This is the companion to the [roadmap](roadmap.md). The roadmap says what is built; this says what is missing, including the parts that are not software. It exists because "the outpatient workflow is implemented and tested" is true and is nowhere near the same statement as "a clinic can use this".

Nothing here is a reason to stop building. It is the list a deploying organization would have to work through, and most of it belongs to them rather than to this repository.

## How to read this

**Blocking** means a clinic genuinely cannot go live without it. **Serious** means they could technically start but would be taking on risk or manual work that most organizations would not accept. **Deliberate** means the limitation is a decision already recorded, not an oversight.

Ownership matters as much as the item. Some of this is engineering here; a large part is the deploying organization's, and some cannot be done by a software project at all.

---

## 1. Blocking: legal and regulatory

None of this is engineering work in this repository, and none of it can be done on a deployment's behalf.

| Missing | Owner |
|---|---|
| Regulatory classification of the deployed system in its jurisdiction, and whatever conformity assessment follows | Deploying organization |
| Clinical risk management file and a clinical safety case, with a named clinical safety officer | Deploying organization |
| Usability engineering evidence, where the jurisdiction requires it | Deploying organization |
| Data protection impact assessment, lawful basis, and processing agreements | Deploying organization |
| Retention and disposal policy meeting the local medical record retention period | Deploying organization |
| Information security certification where required, for example NEN 7510 or ISO 27001 | Deploying organization |

The project's position is stated in the [README](../README.md): Zantrix is delivered as source code, is not a certified medical device, and the maintainers obtain no certification.

## 2. Blocking: access control that a real organization needs

The authorization model is genuinely enforced, and it is genuinely too coarse for a real deployment. This is the most serious engineering gap in the system.

| Missing | Consequence |
|---|---|
| ~~Treatment relationship check~~ | **Implemented.** `zantrix.access.relationship-mode` requires the clinician to work for the patient's managing organization, and optionally to have care evidence. It ships `off` for upgrade safety and warns at startup, so a deployment must still turn it on |
| Location and unit scoping | Organization scoping now exists. Finer scoping to a ward or clinic within an organization does not |
| Multi-tenancy | Several organizations cannot share an installation with separated data |
| Purpose of use | Access for treatment, billing, research and quality improvement are indistinguishable in policy and in the audit trail |
| Sensitive category policy | No separate handling for psychiatric, sexual health, genetic or VIP records. Break the glass exists; category based restriction does not |
| Production identity policy | No MFA, no session or password policy, no federation. The realm in the repository is development only |

Relationship enforcement is now built, and turning it on is a deployment decision that also requires the practitioner and patient directory data to be populated. Multi-tenancy, purpose of use and sensitive categories remain absent. [Security and privacy](architecture/security-and-privacy.md) describes the model.

## 3. Blocking: the record cannot leave the system

A clinic is not an island. Today Zantrix cannot exchange anything with anyone.

| Missing | Consequence |
|---|---|
| Outbound prescribing | A prescription cannot reach a pharmacy. It exists only inside Zantrix |
| Laboratory and radiology interfaces | An order cannot reach a laboratory and a result cannot come back automatically. Results are typed in by hand |
| HL7 v2 and messaging interfaces | No integration with any existing hospital system. Apache Camel is planned, not present |
| Referral and correspondence out | No letters, no discharge summaries, no referral messages |
| Printing | There is no print output anywhere in the interface. Consent forms, prescriptions, letters and summaries cannot be put on paper |
| Document upload and scanning | Inbound paper and external PDFs cannot be attached to a record. DocumentReference is not owned by any module |
| Data migration in | No import path from an existing record system. A clinic would start empty |
| Bulk export out | No FHIR Bulk Data export, so no portability, no analytics feed, and no clean exit |

Printing looks small next to the rest and is not. In most jurisdictions some artefacts still have to be producible on paper.

## 4. Blocking: operations

| Missing | Consequence |
|---|---|
| Secret management | Credentials in the repository are development values. There is no secret backend |
| TLS everywhere and network isolation | Compose exposes HAPI, Snowstorm and Elasticsearch on host ports. In a deployment these must be private |
| Encryption at rest and key management | Not configured |
| Backup and a tested restore | Nothing exists. A backup that has never been restored is not a backup |
| High availability and disaster recovery | Single instance, no failover, no recovery objectives |
| Monitoring, metrics and alerting | Actuator health and info only. No metrics export, no tracing, no alerts, no service level objectives |
| Log shipping, retention and review | Logs stay in containers |
| Rate limiting and edge protection | None |
| Vulnerability management and penetration testing | Dependency scanning runs in continuous integration. Nothing else |
| Upgrade and schema migration procedure | Flyway migrations exist; there is no documented upgrade or rollback runbook |
| Performance and capacity evidence | The system has never been tested at realistic data volume or concurrency. Its behaviour under load is unknown |

## 5. Blocking: clinical content and terminology

The software is content free. A clinic cannot use an empty one.

| Missing | Consequence |
|---|---|
| A licensed SNOMED CT edition | Coded clinical entry fails closed until an operator imports one. This is deliberate and licensed, but it is still a precondition |
| ICD-10 or ICD-11 | No diagnosis coding for reporting, reimbursement or statistics |
| A full LOINC distribution | Only a small allow list for the M1 vital set exists |
| A drug knowledge base | Prescribing has a transparent 15 class safety floor, not a commercial interaction database. See [ADR 0009](architecture/decisions/0009-transparent-medication-safety-floor.md) |
| Dose range, renal, hepatic, paediatric and pregnancy checking | None. A dose that is ten times too high raises nothing |
| Duplicate therapy and cross sensitivity checking | None. An allergy matches only on the exact coded ingredient |
| Order sets, note templates and care protocols | None. Every note and order is composed from scratch |
| Local configuration | Organizations, locations, practitioners and feature flags all have to be set up before first use |

The medication safety points are the ones to read twice. The floor that exists is honest about being a floor, and a real deployment prescribing without a licensed knowledge base is accepting a category of risk that most organizations will not accept.

## 6. Blocking: the practice cannot run its business

| Missing | Consequence |
|---|---|
| Coverage eligibility checking | Coverage is recorded; no payer is contacted and nothing is verified |
| Coding, charge capture, claims and invoicing | The practice cannot bill, so it cannot be paid |
| Mandatory reporting and quality registries | No submission to any national or regional registry |
| Appointment reminders | No notifications of any kind, so no reminders and no no-show reduction |
| Patient facing anything | No portal, no self service, no results release, no messaging |

## 7. Serious: clinical workflows that are absent

Not fatal on day one for a small outpatient clinic, and unacceptable for most other settings.

- Inpatient admission, transfer and discharge; wards, beds and census.
- Emergency department triage and trackboard.
- Theatre and perioperative workflow.
- Flowsheets beyond the fixed vital set, and early warning scores.
- Specimen handling, microbiology, pathology.
- Imaging viewing of any kind.
- Care plans as such. Goals and the care team exist; the plan that groups them does not.
- Clinician inbox unifying results, messages and items needing sign off.
- Growth charts, paediatric dosing and age based reference ranges.
- Free text search across notes; there is no way to find a note by its content.
- A patient timeline or any cross encounter longitudinal view.

## 8. Serious: quality evidence this repository does not yet have

- **Integration and browser tests have not been run in the current working session**, because the environment has no Docker. Unit tests, module boundaries, the contract gate, lint and build are green. The Testcontainers and Compose layers are verified only by continuous integration, and that must be confirmed green before anyone relies on this branch.
- No accessibility gate yet, and no formal WCAG 2.2 AA assessment. [ADR 0013](architecture/decisions/0013-hand-built-design-system.md) makes the gate a requirement; it is not built.
- No visual regression baselines.
- No FHIR conformance testing against a published suite.
- No load, soak or failure injection testing.
- Component test coverage is meaningful but far from complete.
- No clinical review of any wording, alert or workflow by a clinician.

## 9. Serious: internationalization

The interface exists in English only. There is no Dutch translation, and dates, numbers and units are not yet locale aware. For a Dutch clinic this alone is blocking, and it is a content and review task rather than an engineering one.

## 10. Deliberate limitations, already decided

These are recorded decisions, not omissions. They still constrain use.

- Medication interaction checking is a transparent floor and says so ([ADR 0009](architecture/decisions/0009-transparent-medication-safety-floor.md)).
- Offline is read only when it is implemented, and writes are blocked ([ADR 0014](architecture/decisions/0014-read-only-offline.md)).
- Two patient charts open at once, and a third is refused.
- Results reach patients after clinician review by default, once the portal exists ([ADR 0017](architecture/decisions/0017-results-release-policy.md)).
- Coverage is a record, not an eligibility check.
- Immunizations have no forecasting.
- The core carries no country specific behaviour; that lives in adapter packs that do not exist yet ([ADR 0003](architecture/decisions/0003-international-first-with-regional-adapters.md)).

## What is actually ready

So the list above is read in proportion: the outpatient clinical core is real. Patient registration with duplicate detection, merge and unmerge, encounters, scheduling, problems, allergies, medications with a safety check and documented overrides, orders and results, signed notes with addenda, vitals with calculated body mass index, immunizations, coverage, goals and care team, task worklists, consent, emergency access with mandatory review, and a tamper evident audit chain all work, are audited, and are covered by tests. Every clinical fact is a FHIR resource in a real FHIR server, reachable through one guarded gateway. The API contract is generated from the code and drift fails the build.

That is a genuine foundation. It is not a product a clinic can switch to.

## The shortest honest path to a first real deployment

If the goal is one small, low risk outpatient clinic rather than a hospital, the minimum set is roughly:

1. Treatment relationship and organization scoping in the authorization model.
2. Production security: secrets, TLS, network isolation, MFA, backup with a tested restore, monitoring.
3. A licensed SNOMED edition, ICD for coding, and a decision about prescribing without a drug knowledge base, taken by a clinician and written down.
4. Printing, and document upload for inbound paper.
5. Dutch translation and locale aware formatting, for a Dutch deployment.
6. The regulatory and clinical safety work in section 1, which runs in parallel and takes longest.
7. Data migration from whatever the clinic uses today, and a downtime procedure for when Zantrix is unavailable.

Billing, laboratory and pharmacy interfaces, and the patient portal would still be missing. Whether a clinic can work that way for a period is a question for that clinic, not for this document.
