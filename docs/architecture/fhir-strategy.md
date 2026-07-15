# FHIR Strategy

Zantrix is FHIR native. This document explains what that means in practice and how the data platform is built.

## What FHIR native means here

Many systems treat FHIR as an integration format: they keep a private relational schema and generate FHIR at the edges. Zantrix does the opposite. FHIR R4 resources are the canonical representation of clinical data. They are what is stored, validated, searched, versioned, and served.

The benefits are direct:

- No lossy mapping between an internal model and FHIR. There is one model.
- Standard search, history, and operations come from the platform rather than being reimplemented per domain.
- Interoperability is the default state of the data, not a project.
- Contributors who know FHIR already understand the data model.

The cost is that the team must know FHIR well and must model carefully with profiles. That cost is accepted deliberately (see [ADR 0002](decisions/0002-fhir-native-hapi-jpa.md)).

## Current implementation

Docker Compose runs HAPI FHIR 8.10 in R4 mode against its own PostgreSQL database. Application code cannot inject its raw transport. `FhirAccessGateway` provides capabilities, CRUD, search with bounded pagination, history, and transaction bundles. It validates resources, checks SMART scopes and patient context, applies consent policy, records outcomes, and journals mutations for reconciliation.

The backend exposes the supported M0/M1 resource surface through `/fhir/R4`, including its own CapabilityStatement. The local raw HAPI port remains available for development diagnostics and tests only; it must be private in a real deployment.

## The FHIR platform

The core of the data layer is the **HAPI FHIR R4 JPA server**, run as a dedicated service using the official image (see [ADR 0006](decisions/0006-hapi-fhir-as-dedicated-service.md)). The Zantrix backend is a client of that server and acts as the secured gateway in front of it.

- **Storage.** Resources are persisted by HAPI in its own PostgreSQL database. Zantrix does not hand write clinical tables that duplicate FHIR resources.
- **API.** HAPI provides the internal R4 engine. Zantrix exposes its own guarded Java boundary and HTTP facade for supported resource types. Patch, bulk/batch processing, subscriptions, and custom clinical operations are outside M1.
- **Validation.** Every create, update, and transaction resource is validated against base R4. Profiles declared in `meta.profile` must resolve from the configured validation support and are enforced. Unsupported or invalid resources fail before reaching HAPI.
- **Search indexing.** HAPI relational search backs clinical resource search. Elasticsearch backs Snowstorm terminology indexing and is not an alternate clinical store.

## Profiles and regionalization

Milestone 1 uses the base international FHIR R4 definitions as its active core. No regional Zantrix profile pack is distributed yet.

Regional requirements are packaged as **profile packs** that a deployment can enable:

- A United States pack aligning to US Core.
- A Netherlands pack aligning to Nictiz zib and MedMij.

A deployment selects which packs are active. Validation, terminology bindings, and required extensions follow the selected packs. Country specific identifiers, such as the Dutch BSN, are modeled as standard FHIR identifiers within their pack, never as core fields.

## Application modules over FHIR

Application modules do not each open their own connection to the database. They use an internal FHIR access layer:

- Reads and writes go through a typed FHIR client facade against the FHIR server, so all access is validated, audited, and consistent.
- A module owns a set of resource types and profiles as its responsibility, and publishes a narrow interface plus domain events for other modules. For example, the Orders capability owns ServiceRequest and DiagnosticReport handling, and emits events when a result is finalized.
- Where a workflow needs state that FHIR does not model well, that state is kept as a supporting FHIR resource (such as Task) or, only when genuinely necessary, as a small module private table that references FHIR resources by id. Clinical facts always live in FHIR.

`FhirAccessGateway` keeps the raw HAPI client internal, enforces SMART resource scopes and patient context, applies consent, records success and failure, and supports search/history/transactions. A denied operation follows the failure-audit path. Mutations are first represented in a durable journal; if HAPI commits but the relational audit write fails, the client receives an explicit audit-pending error and a reconciler completes accountability without blindly replaying the clinical mutation. See [ADR 0007](decisions/0007-guarded-fhir-access.md) and [ADR 0010](decisions/0010-durable-fhir-mutation-accountability.md).

## Analytics without hurting the operational store

Serving analytics from the live FHIR store hurts clinical performance. Instead:

- The Analytics and Reporting Platform uses **FHIR Bulk Data export** to move data into an analytics friendly store on a schedule or by subscription.
- Dashboards, population health, and research query the analytics store, not the operational server.

## Versioning and history

HAPI resource versioning and `_history` provide per-resource change history through the gateway. The relational hash chain is the accountability source trail, and a scheduled exporter publishes corresponding FHIR AuditEvent resources. Provenance for selected clinically significant authorship remains future work. These records serve different purposes and neither replaces the other.

## Terminology

Snowstorm 10.11.2 provides the terminology FHIR service, backed by Elasticsearch 8.11.1. Zantrix uses value set `$expand` for bounded SNOMED searches and `$validate-code` for fail-closed clinical entry. Operators import an RF2 edition covered by their own SNOMED CT license; no licensed content is committed or redistributed. RxNorm ingredient identifiers are validated against the public NLM RxNorm API using code-only requests with a short cache. LOINC vitals use an explicit M1 allow-list and UCUM units. Broader LOINC/ICD content and translation workflows are later terminology work. See [ADR 0008](decisions/0008-licensed-snomed-with-snowstorm.md).
