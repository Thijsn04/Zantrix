# ADR 0010: Durable FHIR mutation accountability

## Status

Accepted.

## Context

ADR 0007 requires all FHIR access to be audited, but HAPI FHIR and the Zantrix accountability database are separate transactional systems. HAPI can commit a mutation immediately before the audit database becomes unavailable. Retrying the clinical request blindly can duplicate or conflict with an already committed fact, while reporting ordinary failure hides a real change.

## Decision

Represent every guarded FHIR mutation in a Flyway-managed operation journal before sending it to HAPI. The journal records operation identity, resource reference, actor and patient context, request fingerprint, remote state, and audit state without storing a clinical resource body.

After HAPI succeeds, persist remote success before completing the relational audit append. If the audit append fails, return a specific audit-pending exception rather than a generic retryable clinical failure. A scheduled reconciler converts remotely successful pending journal entries into the tamper-evident audit chain and marks them complete. It does not replay the clinical mutation.

The relational chain remains the source accountability trail. A separate scheduled exporter publishes completed entries as FHIR AuditEvent resources and records export progress durably.

## Consequences

Positive:

- A committed clinical mutation cannot disappear from accountability because of a short relational outage.
- Clients receive an honest outcome and are not encouraged to duplicate a potentially committed request.
- Reconciliation and AuditEvent export are observable, restart safe, and independently retryable.

Negative:

- The design guarantees eventual accountability, not atomic commit across both services.
- Journal monitoring and reconciliation lag become production operational requirements.
- Request fingerprints and operation identifiers must remain stable and privacy preserving.
