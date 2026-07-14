# Architecture Overview

Zantrix is a FHIR native, modular monolith EHR. This document gives the high level shape of the system. Deeper topics have their own documents, linked below.

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
                        |                             |
                        |  +-----------------------+  |
                        |  |  HAPI FHIR JPA server |  |
                        |  |  canonical FHIR API   |  |
                        |  +-----------------------+  |
                        +----+---------+---------+----+
                             |         |         |
                   +---------+   +-----+----+   +----------+
                   |PostgreSQL|  |Elastic-  |  | Keycloak |
                   |  16      |  |search    |  | (OIDC)   |
                   +----------+  +----------+  +----------+
                             |
                   +---------+---------------------+
                   | External systems via          |
                   | Interoperability (HL7 v2,     |
                   | FHIR, regional adapter packs) |
                   +-------------------------------+
```

## The three big ideas

1. **FHIR native.** FHIR R4 is not an export format layered on top of a private schema. It is the schema. The HAPI FHIR JPA server is the canonical store and REST API for clinical data. Application modules read and write FHIR resources. See [FHIR strategy](fhir-strategy.md).

2. **Modular monolith.** One deployable application, with strictly separated internal modules enforced by Spring Modulith. Modules communicate through published interfaces and domain events, never by reaching into each other's internals. A module can be extracted into its own service later if it needs independent scaling, without a rewrite. See [backend architecture](backend.md).

3. **An application, not a website.** The frontend is a focused clinical workspace: persistent patient context, keyboard driven navigation, a command palette, real workspace tabs, and dense, calm information design. It is built on a proper design system, not ad hoc styling. See [frontend architecture](frontend.md).

## Layers

| Layer | Responsibility | Document |
|---|---|---|
| Frontend | Clinical and administrative user experience | [frontend.md](frontend.md) |
| Application modules | Domain logic and workflows over FHIR | [backend.md](backend.md), [modules](../modules/README.md) |
| FHIR platform | Canonical resource store, validation, search, operations | [fhir-strategy.md](fhir-strategy.md) |
| Identity | Authentication, authorization, SMART scopes | [security-and-privacy.md](security-and-privacy.md) |
| Interoperability | External integration and regional adapters | [interoperability.md](interoperability.md) |
| Data platform | PostgreSQL, Elasticsearch, analytics export | [fhir-strategy.md](fhir-strategy.md) |

## Cross cutting concerns

- **Security and privacy** are built into the platform, not bolted on. Access control, consent, break the glass, and a tamper evident audit trail apply to every capability. See [security and privacy](security-and-privacy.md).
- **Terminology** is a shared service. Coded data everywhere resolves through it.
- **Observability** is a first class requirement. Structured logs, metrics, tracing, and health probes ship with the platform.
- **Configuration** decides which capabilities are enabled for a given deployment. The same build runs a small clinic or a hospital.

## What is deliberately not here yet

Zantrix is in an early, honest state. The [module vision](../modules/README.md) describes the full intended scope, and the [roadmap](../roadmap.md) states what is actually being built now. When this overview describes a capability, it describes the target design. Status always lives in the roadmap, never inflated in prose.

## Key decisions

The reasoning behind the major choices is recorded as [architecture decision records](decisions/). Start there if you want to understand why the system is shaped this way.
