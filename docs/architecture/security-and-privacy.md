# Security and Privacy

Zantrix handles highly sensitive data. Security and privacy are platform concerns, not optional module features. Zantrix targets NEN 7510 and ISO 27001 as design goals; the project does not claim certification or production readiness.

## Current implementation

The Milestone 0 security baseline currently includes:

- Keycloak 24.0.4 in the local Compose environment with a development realm, synthetic users, and realm roles.
- A stateless Spring Security OAuth2 resource server that validates issuer-backed JWTs outside tests.
- Conversion of Keycloak realm roles to `ROLE_*` authorities and OAuth2/SMART scopes to `SCOPE_*` authorities.
- Authentication on every backend route except Actuator health and info.
- A central `FhirAccessGateway` that checks SMART resource scopes for read, create, update, and delete.
- Conservative patient-context handling: patient scopes can access only the token's own Patient resource. Other compartment resources are denied until compartment-aware policy exists.
- A Flyway-managed relational audit chain that records actor, action, resource reference, patient reference, outcome, source IP, and break-glass flag without resource bodies or clinical content.
- Verification that detects changed entries, broken links, and deletion from the end of the chain through the separately locked chain head.

Known gaps are consent enforcement, treatment-relationship and organization checks, sensitive-record policy, break the glass, multi-factor policy for production, audit search/review tooling, FHIR AuditEvent and Provenance export, transport/TLS deployment policy, rate limiting, security headers, encryption-at-rest guidance, and durable reconciliation when a remote FHIR mutation succeeds but its audit write fails.

## Target authentication

Authentication remains delegated to Keycloak through OAuth2 and OpenID Connect. Production deployments will define strong session, passwordless or multi-factor, and single sign-on policies in the identity provider. SMART on FHIR launch support for EHR-launched and standalone third-party apps is planned; the current code only recognizes SMART-style scopes in bearer tokens.

## Target authorization

The completed access model combines:

- **Roles** for coarse application actions.
- **SMART scopes** for resource-type and operation permissions.
- **Context and relationships** such as organization, location, practitioner-patient relationship, and patient context.
- **Consent and sensitivity** rules that can narrow otherwise valid access.

All FHIR access must pass through the gateway so these decisions and audit behavior remain centralized. The raw HAPI client is internal by architectural decision.

## Consent, privacy, and break the glass

Consent resources, sensitive-record flags, GDPR data-subject workflows, and emergency access are target capabilities. Break-glass access will require a justification, be time limited and visible, produce a high-priority audit record, and create a mandatory review task. None of these workflows is implemented yet.

Application logs must never contain resource bodies, names, clinical detail, tokens, or other unnecessary protected health information. Development data must be synthetic.

## Audit model

The current audit trail is append only and tamper evident, but it is not a FHIR AuditEvent store. Writes take a row lock on one chain-head record, append the event, and advance the head in one Zantrix database transaction. This serializes audit appends while leaving unrelated application work concurrent.

Guarded FHIR mutations cross two services: HAPI may commit before the Zantrix audit write completes. ADR 0007 therefore prevents clinical write capabilities from being called stable until a durable operation journal and reconciliation process closes that failure window.

FHIR AuditEvent export, Provenance for clinically significant changes, privacy-officer search, review queues, and integrity reports are planned on top of the relational source trail.

## Secrets, transport, and data protection

The checked-in Compose credentials and accounts are intentionally weak local-development values. They must never be reused in a real deployment. Production requirements include external secret management, TLS for all traffic, explicit CORS and security headers, rate limiting, protected backups, and appropriate encryption at rest. These controls are deployment requirements and are not delivered by the current local Compose file.

## Responsible disclosure

Follow [SECURITY.md](../../SECURITY.md) and do not open public issues for vulnerabilities.
