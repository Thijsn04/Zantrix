# Security and Privacy

Zantrix handles highly sensitive data. Security and privacy are platform concerns, not optional module features. NEN 7510 and ISO 27001 are design goals; Zantrix does not claim certification or production readiness.

## Implemented M1 baseline

- Keycloak 24.0.4 supplies OIDC authentication, synthetic development users, realm roles, and a `user/*.cruds` SMART scope. Browser-visible issuer validation is separated from private JWK retrieval so token issuer matching remains exact in containers.
- Spring Security is stateless. Realm roles become `ROLE_*` authorities and OAuth2/SMART scopes become `SCOPE_*` authorities. Only health/info probes and FHIR capability metadata are public.
- Every clinical FHIR operation goes through `FhirAccessGateway`. It checks operation-level SMART scopes, requires or derives patient context, and prevents a patient scope from crossing into another patient's record.
- Active FHIR Consent resources are evaluated before patient-resource access. The default `deny-on-explicit` mode allows access unless an effective deny provision matches. `require-active` makes an effective permit mandatory.
- Physicians and nurses can invoke emergency access only with an approved reason code (`immediate-threat`, `unavailable-provider`, or `disaster`). The event is prominently marked in audit, creates a mandatory privacy-review Task, and does not bypass authentication or scope requirements.
- Security response headers include a deny-by-default content security policy for the API, no-referrer policy, frame protection, and disabled camera, microphone, and geolocation permissions.
- Resource bodies, clinical narratives, names, and tokens are excluded from the relational access log. Development data must remain synthetic.

## Authorization model

Authorization combines:

1. **Roles** for coarse application actions and work areas.
2. **SMART scopes** for FHIR resource types and operations.
3. **Patient context** for patient-compartment isolation.
4. **Consent** for patient-defined narrowing of otherwise valid access.
5. **Emergency context** for a narrow, justified, reviewable consent override.

6. **Treatment relationship** for whether this clinician has any business in this patient's record.

Purpose-of-use and sensitive-category policy remain the next contextual layers. They are not implied by the current role model and must be added before broader production use.

## Treatment relationship

A role check answers what kind of thing a user may do. It does not answer whether they have anything to do with a given patient, so on its own it grants every clinician access to the whole population. `TreatmentRelationshipGuard` closes that at the same boundary as consent, which means no application module can go around it.

`zantrix.access.relationship-mode` selects the rule:

- `organization`: the patient must be managed by an organization the clinician works for, resolved from active PractitionerRole records. Suitable for a single clinic.
- `care-relationship`: additionally accepts evidence of care, membership of the patient's care team or an encounter with them.
- `off`: no requirement. This is the historical behaviour, it is **not acceptable for real clinical use**, and the application logs a warning at startup when it is left there.

Patient, Practitioner, PractitionerRole, Organization, Location, Schedule, Slot and Consent are exempt, because they are how a clinician finds the record they then need a relationship for. Everything patient-identifiable is checked.

The check fails closed: a patient with no managing organization is unreachable in `organization` mode. Registration records that organization for exactly this reason.

Emergency access bypasses the relationship check exactly as it bypasses consent. It is already justified, prominently audited, and creates a mandatory review.

## Consent and emergency access

Consent is stored as FHIR Consent. The privacy API creates, lists, and revokes these resources; enforcement lives at the FHIR boundary so application modules cannot bypass it. Emergency access bypasses only the consent decision for the current request. It remains time-bounded to that request, requires a clinician role and reason code, produces a high-priority audit record, and creates an independent review record and Task.

GDPR export/erasure request workflows, category-based sensitive-record flags, and jurisdiction-specific legal-basis policy are not part of M1.

## Audit and cross-service accountability

The relational source trail is append-only and tamper evident. Appends lock one chain-head record, link each entry to the prior hash, and advance the head in the same PostgreSQL transaction. Integrity verification detects changed entries, broken links, and tail deletion. Privacy officers can filter events and inspect emergency-review records.

FHIR mutation and relational audit commits cannot share one transaction. Before a mutation, Zantrix stores a durable operation-journal entry. After HAPI succeeds, it records remote success and completes the audit. If the audit write fails, the caller receives an explicit audit-pending outcome and the scheduled reconciler completes accountability without replaying a potentially committed clinical mutation. A scheduled exporter also publishes relational events as FHIR AuditEvent resources. See [ADR 0010](decisions/0010-durable-fhir-mutation-accountability.md).

FHIR resource history, the relational access trail, AuditEvent, and clinical Provenance answer different questions. Provenance for selected clinically significant authorship is not yet implemented.

## Production controls outside the local stack

The checked-in credentials are intentionally weak local-development values. Production requires at least strong MFA/session policy, managed secrets, TLS on every hop, restricted raw HAPI/Snowstorm/Elasticsearch access, encryption and key management, protected backups with restore tests, rate limiting and edge controls, log export and retention policy, vulnerability management, high availability, disaster recovery, and independent security/privacy assessment.

## Responsible disclosure

Follow [SECURITY.md](../../SECURITY.md) and never open a public issue for a suspected vulnerability.
