# Architecture Overview

Zantrix is a FHIR native, modular monolith EHR. This document gives the high level shape of the system. Deeper topics have their own documents, linked below.

## Current implementation

Milestone 0 is underway. The default branch currently provides the three local infrastructure services shown below, a Spring Boot backend with authenticated diagnostic endpoints and guarded FHIR CRUD access, a relational hash chained audit module, and a minimal React/OIDC application shell. No clinical capability module is implemented yet. The public FHIR facade, consent, break the glass, terminology, interoperability, analytics, feature flags, and the complete clinical workspace are target architecture.

## System context

```
                        +-----------------------------+
   Clinicians  ----->   |        Zantrix Frontend     |
   Patients    ----->   |  React SPA, application UI  |
                        +--------------+--------------+
                                       | HTTPS, OAuth2 / SMART on FHIR
                                       v
                        +-----------------------------+
                        |       Zantrix Backend       |
                        |   Spring Boot, modular      |
                        |   monolith (Spring Modulith)|
                        |   secured FHIR gateway      |
                        +----+---------+---------+----+
                             |         |         |
              +--------------+   +-----+----+   +----------+
              |  HAPI FHIR   |   | Keycloak |   |Postgres  |
              |  JPA server  |   |  (OIDC)  |   |(Zantrix) |
              | canonical    |   +----------+   +----------+
              | FHIR API     |
              +------+-------+
                     |
              +------+-------+   +-------------------------------+
              | Postgres     |   | External systems via          |
              | (FHIR store) |   | Interoperability (HL7 v2,     |
              +--------------+   | FHIR, regional adapter packs) |
                                 +-------------------------------+
```

## The three big ideas

1. **FHIR native.** FHIR R4 is not an export format layered on top of a private schema. It is the schema. The HAPI FHIR JPA server is the canonical store and REST API for clinical data. Application modules read and write FHIR resources. See [FHIR strategy](fhir-strategy.md).

2. **Modular monolith.** One deployable application, with strictly separated internal modules enforced by Spring Modulith. Modules communicate through published interfaces and domain events, never by reaching into each other's internals. A module can be extracted into its own service later if it needs independent scaling, without a rewrite. See [backend architecture](backend.md).

3. **An application, not a website.** The frontend will be a focused clinical workspace with persistent patient context, keyboard driven navigation, a command palette, real workspace tabs, and dense, calm information design. The current UI is a placeholder while that foundation is built. See [frontend architecture](frontend.md).

## Layers

| Layer | Responsibility | Document |
|---|---|---|
| Frontend | Clinical and administrative user experience | [frontend.md](frontend.md) |
| Application modules | Domain logic and workflows over FHIR | [backend.md](backend.md), [modules](../modules/README.md) |
| FHIR platform | Canonical resource store, validation, search, operations | [fhir-strategy.md](fhir-strategy.md) |
| Identity | Authentication, authorization, SMART scopes | [security-and-privacy.md](security-and-privacy.md) |
| Interoperability | External integration and regional adapters | [interoperability.md](interoperability.md) |
| Data platform | PostgreSQL today; Elasticsearch and analytics export planned | [fhir-strategy.md](fhir-strategy.md) |

## Cross cutting concerns

- **Security and privacy.** Authentication, role/scope conversion, guarded FHIR operations, and the audit chain are implemented foundations. Consent, break the glass, and broader contextual authorization are planned. See [security and privacy](security-and-privacy.md).
- **Terminology** is planned as a shared service through which coded data resolves.
- **Observability** currently consists of Spring Boot Actuator health and info endpoints plus standard application logging. Structured JSON logs, metrics export, tracing, and deployment-level readiness remain planned.
- **Configuration** currently covers service URLs, database credentials, CORS, and OIDC settings through environment variables. Capability-level feature flags are planned.

## What is deliberately not here yet

The [module vision](../modules/README.md) describes the full intended scope, and the [roadmap](../roadmap.md) states what is actually being built now. Unless a section explicitly says "current implementation," capability descriptions are target design.

## Key decisions

The reasoning behind the major choices is recorded as [architecture decision records](decisions/). Start there if you want to understand why the system is shaped this way.
