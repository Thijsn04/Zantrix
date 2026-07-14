# ADR 0002: FHIR native persistence via HAPI FHIR JPA server

## Status

Accepted.

## Context

The earlier codebase kept custom JPA entities for clinical data and mapped them to FHIR at the edges. This produced a hybrid where some data was custom and some was FHIR, with duplicated resource types across modules, lossy mapping, and reimplemented search. It also ran Hibernate automatic schema generation alongside Flyway and the HAPI schema, risking schema drift.

We need one clear model for clinical data that is standards based, interoperable by default, and does not require every module to reinvent storage and search.

## Decision

Adopt FHIR R4 as the canonical model and use the HAPI FHIR JPA server, embedded in the Spring Boot application, as the canonical clinical data store and REST API. Application modules read and write FHIR resources through an internal FHIR access layer rather than through private clinical tables. Flyway is the sole owner of database schema, and Hibernate automatic schema generation is disabled.

Supporting, non clinical workflow state may use small module private tables that reference FHIR resources by id, only when FHIR does not model that state well. Clinical facts always live in FHIR.

## Consequences

Positive:

- One model for clinical data. No lossy internal to FHIR mapping.
- Standard search, history, validation, and operations come from the platform.
- Data is interoperable by default. External FHIR clients and SMART apps work against the same API.
- A single, reviewed schema history through Flyway. No runtime schema drift.

Negative:

- The team must know FHIR well and must model carefully with profiles.
- Some access patterns are less convenient than a purpose built relational schema, and are addressed with thin application endpoints that compose FHIR operations.
- Tying to HAPI is a significant dependency. It is a mature, widely used open source implementation, which makes this an acceptable risk.

## Alternatives considered

- **Custom domain model with a FHIR facade.** More control over query shapes, but the team owns FHIR conformance entirely, and it perpetuates the two model problem. Rejected in favor of a single canonical model.
