# ADR 0011: Generated API contract between frontend and backend

## Status

Accepted. Not yet implemented. Delivery is sequenced in the [frontend plan](../frontend-plan.md) and status is tracked in the [roadmap](../../roadmap.md).

## Context

The frontend consumes two different contracts. The task oriented application API under `/api/v1` is the one the workspace uses for almost everything. The FHIR R4 facade under `/fhir/R4` is used by external clients today and will be used by patient facing and SMART surfaces later.

Neither contract is currently machine readable for the frontend. Application DTO types are transcribed into TypeScript by hand, and the backend does not publish an OpenAPI document.

Hand transcription fails silently. During the workspace rebuild, three DTO types were transcribed incorrectly: `AuditVerificationResult`, `EmergencyAccessReview` and `FeatureFlagView`. TypeScript compilation succeeded in every case, because a wrong field name is not a type error on the consuming side. It produces `undefined` at runtime instead.

For an audit report that is an annoyance. For a dose, a route, an allergy list or a result value it is a patient safety defect that no compiler and no reviewer is reliably going to catch. A system intended for production clinical use cannot depend on careful transcription as its only control.

## Decision

The contract is generated, and the backend owns it.

**Application API.** The backend generates an OpenAPI document from its actual controllers using springdoc-openapi, rather than maintaining a specification by hand. Because the document is derived from the code, it cannot describe something the implementation does not do. The generated document is committed to the repository so that a contract change appears as a reviewable diff in the pull request that causes it.

**Frontend types.** The frontend generates TypeScript types from that document. It generates types only, not a client. The existing `ApiClient` is retained by hand because it owns bearer token attachment, abort handling, no content responses and RFC 9457 problem detail extraction, and those behaviours are worth reading and testing directly.

**FHIR types.** Types for FHIR resources are generated from the R4 StructureDefinitions for the surfaces that speak FHIR directly. FHIR is already a formal versioned contract, so this requires no new agreement between the two sides.

**Drift detection.** Continuous integration regenerates both the OpenAPI document and the frontend types and fails the build if the result differs from what is committed. A contract change is therefore always deliberate and always reviewed.

**Runtime validation.** Generated types are erased at runtime. Responses are validated against the schema in development and in tests, where a mismatch should fail loudly. Production responses are not validated on every call, because the cost is not justified once the contract is enforced at build time.

**Versioning.** Within `/api/v1` changes are additive. A field is never repurposed or removed without a new major path, because a deployed frontend and a deployed backend are not upgraded in the same instant.

## Consequences

Positive:

- A renamed or removed field breaks the build instead of silently becoming `undefined` in a clinical field.
- The contract cannot drift from the implementation, because it is derived from the implementation.
- Contract changes are visible in review as an explicit diff rather than discovered later.
- FHIR surfaces get accurate typings without a bespoke agreement.
- The network client stays small, readable and directly testable.

Negative:

- The backend must add and maintain springdoc-openapi, and annotate controllers well enough that the generated document is accurate. An inaccurate generated document is worse than none, because it is trusted.
- Code generation is added to the build on both sides, which makes the pipeline slower and adds a failure mode when generation itself breaks.
- Generated types describe shape, not meaning. They do not prevent a correctly typed value being used in the wrong clinical place, so review and tests remain necessary.
- Until the backend publishes the document, the frontend keeps hand written types and carries the silent failure risk described above.
