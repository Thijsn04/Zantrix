# Backend Architecture

The backend is a modular monolith built with Java 21 and Spring Boot 3, using Spring Modulith to enforce module boundaries. Its canonical data platform is a dedicated HAPI FHIR JPA server ([ADR 0006](decisions/0006-hapi-fhir-as-dedicated-service.md)) that the backend talks to as a client and secures as a gateway.

## Current implementation

The backend currently contains the `audit` module and a `platform` module with FHIR, IAM, security, and system-web packages. Spring Modulith verification enforces the present boundaries. There are no patient, encounter, scheduling, or clinical modules yet.

Implemented HTTP endpoints are limited to public Actuator health/info, authenticated system and current-user information, and authenticated FHIR connectivity status. The backend does not yet expose a public FHIR REST proxy or OpenAPI description.

## Why a modular monolith

A large EHR spans many domains, but at this stage the team and the deployments are small. A modular monolith gives the separation of a service architecture without the operational cost of running many services. Boundaries are enforced in the codebase and verified in tests, so the option to extract a module into its own service later stays open. See [ADR 0001](decisions/0001-modular-monolith.md).

## Module structure

Each capability from the [module vision](../modules/README.md) is intended to map to a Spring Modulith module, one top level package under `com.zantrix`.

A module is organized by responsibility, for example:

```
com.zantrix.orders
  api            published interface and events other modules may use
  internal       implementation, not visible to other modules
  web            REST controllers for the frontend
```

Rules:

1. **No cross module internals.** A module never imports another module's `internal` package. This is enforced by Spring Modulith verification tests, which fail the build on a violation.
2. **Talk through the front door.** Modules interact through a published `api` interface or by consuming domain events. There is no shared reaching into another module's data.
3. **Events for decoupling.** State changes that other modules care about are published as application events, for example an event when an order result is finalized. Consumers subscribe. This keeps modules loosely coupled and mirrors how they would communicate if later split into services.
4. **One owner per resource area.** Each FHIR resource area has a single owning module, which prevents the duplication that the previous codebase suffered from.

## Data access

Clinical state is FHIR. Modules access it through the internal FHIR access layer described in [FHIR strategy](fhir-strategy.md), not through direct JPA entities that shadow FHIR resources.

Schema ownership is split cleanly by database. **Flyway** owns the Zantrix application schema in the Zantrix database, with Hibernate automatic schema generation disabled and every change a reviewed migration in version control. The **HAPI FHIR server** owns and migrates the FHIR resource schema in its own separate database. Neither tool manages the other's tables, so there is no schema drift.

## API surface

The target backend exposes two kinds of HTTP API:

- The **FHIR REST API**, for standards based access and for external clients and SMART apps.
- A small set of **application endpoints** for the frontend, where a task oriented, aggregated call is clearer than a series of raw FHIR calls. These endpoints are thin. They compose FHIR operations and module interfaces, and they never become a parallel data model.

The internal HAPI service already publishes its own CapabilityStatement. Publishing the Zantrix gateway CapabilityStatement and application OpenAPI description remains planned.

## Cross cutting infrastructure

- **Security.** The implemented layer validates Keycloak-issued JWTs, maps realm roles and OAuth2/SMART scopes, and requires authentication except for health/info probes. The FHIR gateway applies resource-scope and limited patient-self checks. Consent, break the glass, relationship checks, and stronger policy decisions are planned.
- **Audit.** The implemented audit module writes append-only relational entries linked by a concurrency-safe, verifiable hash chain. It records successful and failed guarded FHIR operations. FHIR AuditEvent export and privacy-officer tooling are planned.
- **Error handling.** Spring Security and framework defaults are currently used. A consistent domain-error and FHIR OperationOutcome mapping layer is planned.
- **Observability.** Actuator health and info endpoints are implemented. Structured JSON logging, metrics export, distributed tracing, and deployment readiness integration are planned.
- **Configuration.** Database, FHIR, OIDC, and CORS values are environment configurable. Capability feature flags and production secret-manager integration are planned.

## Testing strategy

- **Unit tests** for domain logic, mappers, and calculators.
- **Integration tests** with Testcontainers against real infrastructure. PostgreSQL and HAPI FHIR are covered today. Keycloak and Elasticsearch suites are added with the capabilities that require them. No in memory database substitutes are used for implemented persistence paths, to avoid dialect false positives.
- **Modulith verification tests** that fail the build if module boundaries are violated.
- **Contract and conformance tests** against the FHIR CapabilityStatement and active profiles are planned with profile enforcement.

See [development](../development.md) for how to run these locally.
