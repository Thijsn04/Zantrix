# Backend Architecture

The backend is a modular monolith built with Java 21 and Spring Boot 3, using Spring Modulith to enforce module boundaries. Its canonical data platform is a dedicated HAPI FHIR JPA server ([ADR 0006](decisions/0006-hapi-fhir-as-dedicated-service.md)) that the backend talks to as a client and secures as a gateway.

## Current implementation

The backend contains platform modules for FHIR, IAM/security, audit, terminology, consent/privacy, workflow, and administration; patient-administration modules for the MPI, encounters, and scheduling; and clinical modules for problems, allergies, medications/CDS, orders/results, documentation, vitals, and immunizations. Spring Modulith verification enforces their boundaries across 168 production classes.

Application endpoints live under `/api/v1`. A secured R4 facade under `/fhir/R4` publishes metadata plus supported-resource CRUD, search, history, and transaction behavior. An OpenAPI document is generated from the controllers, committed as `backend/openapi.json`, and served from `/v3/api-docs` to authenticated callers. A test regenerates it and fails on any difference, so the published contract cannot drift from the code. See [ADR 0011](decisions/0011-generated-api-contract.md).

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

The Zantrix gateway publishes its own CapabilityStatement at `/fhir/R4/metadata`; it does not expose the internal HAPI metadata as its contract. The application API is described by the generated OpenAPI document.

## Cross cutting infrastructure

- **Security.** Keycloak JWTs, realm roles, SMART scopes, patient context, Consent, and justified emergency access are evaluated centrally. Emergency use creates a mandatory privacy review Task. Treatment-relationship and sensitive-category policy remain future extensions.
- **Audit.** A concurrency-safe hash chain records successful, denied, and failed operations without resource bodies. Filtered search, integrity reports, emergency review, FHIR AuditEvent export, and a durable mutation journal/reconciler are implemented.
- **Error handling.** Application endpoints use RFC 9457-style problem details for domain and validation failures. The FHIR facade uses FHIR JSON and validation failures map to unprocessable-entity responses.
- **Observability.** Actuator health and info endpoints are implemented. Structured JSON logging, metrics export, distributed tracing, and deployment readiness integration are planned.
- **Configuration.** Database, FHIR, Snowstorm, RxNorm, OIDC issuer/JWK, CORS, consent mode, scheduler intervals, and capability feature flags are configurable. Production secret-manager integration remains deployment work.

## Testing strategy

- **Unit tests** for domain logic, mappers, and calculators.
- **Integration tests** with Testcontainers against PostgreSQL and HAPI FHIR. They verify application boot, audit persistence/integrity, IAM policy, raw FHIR transport, and the complete M1 clinician journey. There are no in-memory database substitutes for implemented persistence paths.
- **Modulith verification tests** that fail the build if module boundaries are violated.
- **FHIR validation tests** exercise base R4 validation and declared-profile rejection. Broader published conformance fixtures remain future work.
- **Browser tests** exercise the real Compose topology, Keycloak login, backend authorization, patient registration, FHIR persistence, and chart navigation.

See [development](../development.md) for how to run these locally.
