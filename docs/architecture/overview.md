# Architecture Overview

Zantrix is a FHIR-native, modular-monolith EHR. This document gives the high-level shape of the implemented Milestone 1 system.

## Current implementation

Milestone 0 is complete and the Milestone 1 outpatient core is beta complete. The system includes a clinical React workspace, a Java modular monolith, guarded application and FHIR APIs, HAPI FHIR as the canonical clinical store, Keycloak identity, a relational accountability store, Snowstorm terminology, and Elasticsearch terminology indexing.

## System context

```
                        +-----------------------------+
   Clinicians  ----->   |        Zantrix Frontend     |
                        | React clinical workspace    |
                        +--------------+--------------+
                                       | HTTPS, OIDC, SMART scopes
                                       v
                        +-----------------------------+
                        |       Zantrix Backend       |
                        | Spring Boot modular         |
                        | monolith and FHIR gateway   |
                        +----+---------+---------+----+
                             |         |         |
              +--------------+   +-----+----+   +----------+
              | HAPI FHIR    |   | Keycloak |   |PostgreSQL|
              | canonical R4 |   | OIDC/RBAC|   |audit,    |
              | resource API |   +----------+   |journal,  |
              +------+-------+                  |control   |
                     |                          +----------+
              +------+-------+
              | PostgreSQL   |       +---------------------+
              | FHIR schema  |       | Snowstorm FHIR     |
              +--------------+       | terminology API    |
                                     +----------+----------+
                                                |
                                     +----------+----------+
                                     | Elasticsearch      |
                                     | terminology index  |
                                     +---------------------+
```

External interoperability adapters, imaging, analytics, patient engagement, and regional packs remain later milestones.

## The three big ideas

1. **FHIR native.** FHIR R4 is the canonical clinical schema, not an export over private clinical tables. HAPI FHIR stores, versions, and searches resources. Application modules use the guarded gateway. See [FHIR strategy](fhir-strategy.md).

2. **Modular monolith.** One backend deployment has strictly separated modules verified by Spring Modulith. Modules publish narrow APIs and can be extracted later if independent scaling becomes necessary. See [backend architecture](backend.md).

3. **A clinical application, not a website.** The frontend maintains explicit patient context and task-focused navigation across the patient chart, scheduling, worklists, administration, and privacy workflows. See [frontend architecture](frontend.md).

## Layers

| Layer | Responsibility | Current boundary |
|---|---|---|
| Frontend | Clinical and administrative experience | M1 outpatient workspace |
| Application modules | Domain workflows over FHIR | M1 platform, patient administration, and clinical core |
| FHIR platform | Canonical resources, validation, search, history, transactions | HAPI R4 behind guarded Java and HTTP facades |
| Identity and privacy | Authentication, role/scope authorization, consent, emergency access | Keycloak plus central backend policy |
| Terminology | Coded entry and validation | Snowstorm for licensed SNOMED; NLM RxNorm validation |
| Accountability | Audit chain, operation journal, reviews, AuditEvent export | Zantrix PostgreSQL plus FHIR AuditEvent |
| Interoperability | External systems and regional adapters | FHIR facade only; other adapters planned |

## Cross-cutting concerns

- **Security and privacy.** Central authentication, role and SMART scope conversion, patient context, consent, justified emergency access, review tasks, security headers, and audit reconciliation are implemented. Production relationship policy and infrastructure controls remain deployment work.
- **Terminology.** Snowstorm search, expansion, and validation fail closed. Deployments supply their licensed SNOMED CT edition. RxNorm validation sends only system/code/display data and never patient context.
- **Observability.** Actuator health/info and application logs are present. Metrics export, distributed tracing, and production SLOs remain future hardening.
- **Configuration.** Service URLs, OIDC issuer/JWK endpoints, CORS, consent mode, intervals, and feature flags are configurable. Production secret management is external to the local stack.

## Deliberate boundary

The M1 beta is a narrow outpatient EHR, not the complete [module vision](../modules/README.md). Inpatient ADT, comprehensive medication knowledge, specialty care, diagnostics integrations, revenue cycle, analytics, patient engagement, regional certification, and operational production controls are later work. The [roadmap](../roadmap.md) is authoritative.

## Key decisions

The reasoning behind major choices is recorded in the [architecture decision records](decisions/).
