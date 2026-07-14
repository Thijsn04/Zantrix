# Security and Privacy

Zantrix handles some of the most sensitive data there is. Security and privacy are part of the platform, applied to every capability, not features that individual modules opt into. Zantrix targets NEN 7510 and ISO 27001 as design goals. These are goals the architecture is built to support, not certifications the project holds.

## Authentication

- Authentication is delegated to **Keycloak** using OAuth2 and OpenID Connect.
- The backend is an OAuth2 resource server. It validates access tokens and never handles passwords itself.
- **SMART on FHIR** launch is supported for both EHR launched and standalone applications, so third party clinical apps integrate through a standard, scoped flow.
- Multi factor authentication and single sign on are configured in Keycloak.

## Authorization

Access control combines several layers, evaluated on every request:

- **Roles.** Coarse grained roles such as clinician, nurse, administrator, and privacy officer. Roles are defined in the identity provider and carried in the token.
- **SMART scopes.** Fine grained scopes describe what an application or session may do with which resource types.
- **Attribute and relationship checks.** Whether a user has a treatment relationship with a patient, whether the patient record is flagged sensitive, and the user's organization and location context.
- **Consent.** The patient's recorded consent can narrow what is visible. See below.

Authorization decisions are made centrally so that every path, both the FHIR API and application endpoints, is protected consistently. A capability cannot accidentally expose data by forgetting to add a check.

## Consent and privacy

- Patient **Consent** resources can restrict access to a record or parts of it. The access layer is consent aware and filters accordingly.
- Records can carry **sensitive flags**, for example for VIP patients or behavioral health, which raise the bar for access and route it through break the glass.
- **GDPR data subject workflows** are supported: a privacy officer can produce a complete export of a patient's processed data, including access logs, and can process erasure requests within legal constraints.
- **No protected health information in application logs.** Identifiers, names, and clinical detail are never written to standard application logs. Access to clinical data is recorded in the audit trail, not in console logs.

## Break the glass

Emergency access allows a clinician to reach a record they would normally not have access to, for example in the emergency department.

- It requires an explicit, recorded justification before access is granted.
- It is time limited and clearly indicated in the interface while active.
- It generates a high visibility audit entry and a review task for a privacy officer.
- Accountability is after the fact and mandatory. Break the glass widens access, it does not hide it.

## Audit trail

Every access to and change of patient data is recorded as a FHIR **AuditEvent**.

- The audit log is **tamper evident** through a hash chain, so that removal or alteration of an entry is detectable.
- The mechanism is designed to be **concurrency safe**. It does not serialize all writes through the tail of a single table, which was a scalability and correctness weakness in the earlier design.
- Audit records reference the actor, the patient, the action, the source, and whether break the glass was in effect. They do not embed protected health information beyond the necessary references.
- **Provenance** resources record who made clinically significant changes and why, complementing FHIR resource history.

Privacy officers have tooling to search, review, and verify the integrity of the audit trail.

## Secrets and transport

- No secrets in source control. Credentials and keys come from the environment or a secret manager. The development Docker setup uses clearly non production values, documented as such.
- All traffic is over TLS in any real deployment.
- Cross origin access, security headers, and rate limiting are configured explicitly rather than left to defaults.

## Data protection

- Sensitive identifiers, such as national numbers, are protected at rest.
- Backups and the analytics store inherit the same access and audit expectations as the operational store.

## Responsible disclosure

Security issues are handled under [SECURITY.md](../../SECURITY.md). Please do not open public issues for vulnerabilities.
