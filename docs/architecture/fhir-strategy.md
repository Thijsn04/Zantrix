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

Docker Compose runs the official HAPI FHIR 8.10 image in R4 mode against its own PostgreSQL database. The backend uses the HAPI R4 client through `FhirAccessGateway`; application code cannot inject the raw transport. The gateway currently supports server capabilities plus resource read, create, update, and delete. It checks SMART resource scopes, restricts patient-context scopes to direct self-Patient access, and records successful and failed operations in the Zantrix audit chain.

The local HAPI port is available directly for development and tests. It must remain internal in a real deployment. The Zantrix backend does not yet expose a public FHIR facade.

## The FHIR platform

The core of the data layer is the **HAPI FHIR R4 JPA server**, run as a dedicated service using the official image (see [ADR 0006](decisions/0006-hapi-fhir-as-dedicated-service.md)). The Zantrix backend is a client of that server and acts as the secured gateway in front of it.

- **Storage.** Resources are persisted by HAPI in its own PostgreSQL database. Zantrix does not hand write clinical tables that duplicate FHIR resources.
- **API.** HAPI provides the complete internal FHIR REST API. The Zantrix gateway currently exposes only its Java CRUD boundary to application modules and a connectivity-status endpoint over HTTP. Public FHIR/SMART access, search, history, patch, batch, transactions, and custom operations remain planned.
- **Validation.** HAPI performs its default resource handling today. Zantrix profile packs and explicit validation against active profiles have not been configured.
- **Search indexing.** HAPI's relational search is available on the internal service. Elasticsearch, terminology indexing, and gateway search are not present.

## Profiles and regionalization

Zantrix will ship an **international core profile set** based on the base FHIR R4 specification and widely used international profiles. No Zantrix profile pack is present in the repository yet.

Regional requirements are packaged as **profile packs** that a deployment can enable:

- A United States pack aligning to US Core.
- A Netherlands pack aligning to Nictiz zib and MedMij.

A deployment selects which packs are active. Validation, terminology bindings, and required extensions follow the selected packs. Country specific identifiers, such as the Dutch BSN, are modeled as standard FHIR identifiers within their pack, never as core fields.

## Application modules over FHIR

Application modules do not each open their own connection to the database. They use an internal FHIR access layer:

- Reads and writes go through a typed FHIR client facade against the FHIR server, so all access is validated, audited, and consistent.
- A module owns a set of resource types and profiles as its responsibility, and publishes a narrow interface plus domain events for other modules. For example, the Orders capability owns ServiceRequest and DiagnosticReport handling, and emits events when a result is finalized.
- Where a workflow needs state that FHIR does not model well, that state is kept as a supporting FHIR resource (such as Task) or, only when genuinely necessary, as a small module private table that references FHIR resources by id. Clinical facts always live in FHIR.

The first version of this facade is implemented as `FhirAccessGateway`. It keeps the raw HAPI client internal, enforces SMART resource scopes, verifies direct self-Patient access for patient-scoped tokens, and records successful and failed operations. A denied operation follows the same failure-audit path. Search, transactions, profile enforcement, consent evaluation, compartment-aware patient access, and durable reconciliation between FHIR mutations and audit writes remain Milestone 0 work. See [ADR 0007](decisions/0007-guarded-fhir-access.md).

## Analytics without hurting the operational store

Serving analytics from the live FHIR store hurts clinical performance. Instead:

- The Analytics and Reporting Platform uses **FHIR Bulk Data export** to move data into an analytics friendly store on a schedule or by subscription.
- Dashboards, population health, and research query the analytics store, not the operational server.

## Versioning and history

HAPI's FHIR resource versioning and `_history` provide per-resource change history on the internal service. The implemented Zantrix access log is relational and hash chained; FHIR AuditEvent and Provenance export are planned. These records serve different purposes and neither replaces the other.

## Terminology

The target terminology service uses standard FHIR terminology operations (`$lookup`, `$validate-code`, `$translate`, and value set `$expand`) backed by licensed or distributable terminology content such as SNOMED CT, LOINC, ICD, and RxNorm. Terminology loading and operations are not configured yet. See the Terminology capability in the [module vision](../modules/README.md).
