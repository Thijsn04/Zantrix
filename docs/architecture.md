# Architecture and technology

Reconstructed from the owner-supplied handover dated 15 October 2026. This document records design requirements and their stated limits; it does not establish implemented functionality.

[Documentation index](README.md)

## Chosen direction

The owner accepted the following technological direction:

- Java with Spring Boot for the backend.
- React with TypeScript for the frontend.
- PostgreSQL as the primary database.
- One repository for official project components.
- A modular monolith rather than separate services for every module.
- A uniform supported backend/frontend stack for external modules as well.

Exact versions and detailed libraries remain open.

The [module-platform direction](module-platform-options.md) is accepted: shared startup dependencies, an official SDK/BOM, declared-library staging checks, PropertiesLauncher with preflight/supervision and native browser import maps. The [Core/Bridge and data/transaction direction](core-bridge-data-design.md) is also accepted: role separation, owner schemas, no cross-owner SQL joins/foreign keys, explicit local atomic composition and durable outbox follow-up. Exact APIs/tooling, outbox layout and enforcement implementation remain open. The accepted identity, shared UI and emergency-read refinements follow below. The separate [visual foundation](visual-direction.md) is now accepted: clear/accessibility-led light surfaces with compact precision and low decoration; the approved reference selects Clinical Teal #0F766E, selection #ECF8F5, canvas #F4F6F8 and Inter 4.1 with cv05/cv08/zero/tabular figures (R155–R156). Its sole shell header and stable filled/empty grid are the ergonomic baseline; remaining brand assets/tokens and production implementation remain open.

The accepted [identity, workspace/session and authorization direction](identity-session-authorization-design.md) uses institution-controlled Keycloak brokerage, an OIDC/server-session browser boundary, authoritative Core workspace/access state and scoped asynchronous authority. Its refinements require per-principal transaction guards, verifiably fresh unlock, explicit patient context, no persistent clinical browser cache, fail-closed display and protected volatile draft recovery. Implementation, exact TTL and runtime proof remain pending. The [shared UI direction](shared-ui-design.md) is also accepted (R130–R134): specialized React components in shell slots, mandatory shell navigation/overlays, registered drafts, semantic tokens/accessibility and context-bound SDK transport. Exact UI APIs and enforcement proof remain open. The [independent emergency reader and production contract](emergency-read-design.md) are also accepted (R135–R142): module-free typed projections, mandatory classification/contract evidence, outbox triggers, atomic durable publication, verified coverage and local hash-chained audit reconciliation. Exact emergency dataset, offline security and operational targets remain open; product access policies remain in force.

## Why Java rather than switching to C#/.NET

The owner has more experience with Java and TypeScript but explicitly asked for a serious comparison rather than choosing familiarity alone.
The comparison concluded:

- Java/Spring and C#/ASP.NET Core are both credible platforms.
- Both support mature transactions, security facilities, Linux hosting and PostgreSQL.
- Both have FHIR tooling.
- Neither language intrinsically makes an EHR clinically safe.
- Java experience reduces the initial maintainer's learning and code-review burden.
- Java has relevant architecture-verification tooling and healthcare ecosystem examples.
- No decisive requirement was identified that justified switching to .NET.

This was not based on a representative performance benchmark.

## External projects examined

Public repositories and documentation were examined:

- OpenMRS: Java/Spring and an extension-module model.
- OpenMRS frontend: React/TypeScript ecosystem.
- OpenEMR: PHP-based EHR.
- EHRbase: Java/PostgreSQL openEHR data platform.
- HAPI FHIR: Java FHIR tooling.
- Firely SDK: .NET FHIR tooling.
- Spring Modulith and ArchUnit: module/architecture verification.
- .NET support policy.

References:

- [github.com/openmrs/openmrs-core](https://github.com/openmrs/openmrs-core)
- [github.com/openmrs/openmrs-esm-core](https://github.com/openmrs/openmrs-esm-core)
- [github.com/openemr/openemr](https://github.com/openemr/openemr)
- [github.com/ehrbase/ehrbase](https://github.com/ehrbase/ehrbase)
- [github.com/hapifhir/hapi-fhir](https://github.com/hapifhir/hapi-fhir)
- [github.com/FirelyTeam/firely-net-sdk](https://github.com/FirelyTeam/firely-net-sdk)
- [github.com/spring-projects/spring-modulith](https://github.com/spring-projects/spring-modulith)
- [github.com/TNG/ArchUnit](https://github.com/TNG/ArchUnit)
- [github.com/dotnet/core/blob/main/release-policies.md](https://github.com/dotnet/core/blob/main/release-policies.md)

Some external websites were blocked by network policy.
The complete internal architectures of Epic and HiX were not verified. Do not invent claims about them.

## Core, Bridge and Connect

These names are explicitly important.

## Core

Core is the technical foundation.
Its proposed responsibilities include:

- Technical identity integration.
- Organizations and locations.
- Shared security facilities.
- Module lifecycle.
- Compatibility and configuration foundations.

Clinical capabilities should not automatically be put in Core.
Patient registration is a separate module.

## Bridge

Bridge is the mandatory internal interaction component.
It connects Core and the different modules through standardized contracts.
It is not the external integration component.
It must not become:

- A duplicate store of every clinical record.
- A container for all clinical business logic.
- An untyped universal action dispatcher.

## Connect

Connect handles external interoperability.
It provides or coordinates:

- Adapters.
- External communication.
- Standards mapping.
- Delivery state and failure handling.
- Country-specific connector implementations where applicable.

The initial terminology was different earlier in the discussion. The owner's correction supersedes that terminology:
Bridge = internal. Connect = external.

## Module categories

The conceptual categories are:

1. Technical platform components.
2. Shared clinical modules.
3. Care-setting modules.
4. Country modules.
5. Shared user-interface platform.
6. External interoperability adapters.
7. Internal testing and synthetic-data facilities.

The final package boundaries are not selected.

## One installation

Modules should initially run within one Zantrix installation.
The owner accepted one installation rather than designing every module as a separately deployed service.
This does not mean that:

- PostgreSQL must run in the same process.
- Every supporting service must be embedded.
- The emergency reader depends on the primary application.
- All interactions must become network calls.

## Monorepo

Core, Bridge, Connect and official modules should be developed in one repository.
Benefits include:

- Coordinated cross-module changes.
- Shared tests.
- Consistent documentation.
- Easier development for one initial maintainer.

Modules still need explicit versions, interfaces and dependencies.
External developers can maintain their modules in separate repositories.

## Versioning and installation

Separate module versioning is required.
However:

- Independently versioned modules are not necessarily independently hot-updatable.
- Supported combinations must be explicit.
- Incompatible combinations must be blocked.
- Required dependencies cannot be removed silently.
- Database migrations require controlled procedures.

Planned maintenance and a controlled restart are acceptable. Technical administrators must be able to install ready-made module packages without recompiling Zantrix. Exact backend/frontend packaging, loading and discovery remain open; no hot replacement of clinical code is required.

Both an official catalog and local package-file installation are required. Approval status remains specific to versions and supported combinations. See [module lifecycle](module-lifecycle.md) for the agreed installation, update, retirement and test-environment requirements.

## Data ownership

The accepted starting point is module-owned domain models.
For each authoritative datum:

- Assign one owner.
- Expose explicit contracts.
- Avoid direct writes to another module's tables.
- Avoid independently editable duplicate truth.

Copies may still be needed for:

- Backups.
- Emergency access.
- Derived views.
- Historical snapshots.
- External messages.

Such copies need explicit source, purpose, freshness and write-authority rules.

## Clinical standards

The previous requirement that all internal clinical data must be stored as FHIR is superseded.
Nevertheless, existing clinical standards must be examined before inventing equivalents.
Relevant areas include:

- FHIR.
- openEHR.
- Identifiers.
- Terminology.
- Units.
- Provenance.
- Clinical versioning and correction semantics.

Connect cannot reconstruct meaning that the internal model did not retain.

## Complexity limits

No current requirement mandates:

- Kubernetes.
- Microservices.
- Full event sourcing.
- A universal message broker.
- A custom network transport protocol.
- Dynamic hot replacement of clinical modules.

Introduce these only for a demonstrated need.

## Trust boundary

Modules in one process are not security-isolated from each other.
Module boundaries, review and Bridge checks do not provide a sandbox against hostile installed code.
This limitation must be reflected in module governance and deployment policy.
