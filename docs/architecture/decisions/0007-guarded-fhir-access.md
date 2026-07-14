# ADR 0007: Guarded FHIR access boundary

## Status

Accepted.

## Context

The dedicated HAPI FHIR server is the canonical clinical store. Exposing its raw client to every application module would let a module bypass authorization, consent, and audit by mistake. A network call to HAPI and an audit write to the Zantrix database also cannot share a database transaction, so that consistency boundary must be explicit.

## Decision

All application modules access clinical FHIR resources through the `FhirAccessGateway`. The raw HAPI client is internal to the platform module.

The gateway applies these controls:

- SMART resource scopes are checked before the request reaches HAPI.
- Patient-context scopes currently permit only direct access to the token's own Patient resource. Other patient-compartment resources remain denied until the gateway supports compartment-aware queries.
- Successful operations, denied attempts, and failed operations are recorded through the audit module.
- Capability metadata is available for authenticated platform diagnostics without a clinical resource scope.

Role checks remain available for coarse application actions. Resource access still requires a suitable SMART scope.

Audit recording is synchronous. A failed audit write is surfaced to the caller. Because HAPI and the audit store are separate services, a remote mutation may already be committed if the subsequent audit write fails. Clinical write capabilities must not be marked stable until a durable operation journal and reconciliation mechanism closes this failure window.

## Consequences

Positive:

- One enforceable path for clinical data access.
- Modules cannot inject the raw FHIR client.
- Scope and audit behavior can be tested independently from HAPI transport behavior.
- Denied access attempts are visible in the audit trail.

Negative:

- The gateway adds a small amount of indirection to every FHIR operation.
- Cross-service mutation and audit atomicity remains an explicit Milestone 0 risk.
- Search, batch, transaction, and custom operation support must be added to the gateway deliberately rather than using HAPI directly.
