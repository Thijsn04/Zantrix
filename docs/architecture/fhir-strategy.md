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

## The FHIR platform

The core of the data layer is the **HAPI FHIR R4 JPA server**, run as a dedicated service using the official image (see [ADR 0006](decisions/0006-hapi-fhir-as-dedicated-service.md)). The Zantrix backend is a client of that server and acts as the secured gateway in front of it.

- **Storage.** Resources are persisted by HAPI in its own PostgreSQL database. Zantrix does not hand write clinical tables that duplicate FHIR resources.
- **API.** The FHIR REST API (read, create, update, patch, delete, history, search, transaction, and operations) is served by HAPI. The server is internal, and FHIR and SMART on FHIR access is provided through the Zantrix gateway, which applies authentication, authorization, consent, and audit.
- **Validation.** Resources are validated against the active profiles on write. Invalid resources are rejected with an OperationOutcome.
- **Search indexing.** HAPI search parameters back standard FHIR search. Elasticsearch is used for terminology and for large scale or full text search where the relational indexes are not enough.

## Profiles and regionalization

Zantrix ships an **international core profile set** based on the base FHIR R4 specification and widely used international profiles. This keeps the core region neutral.

Regional requirements are packaged as **profile packs** that a deployment can enable:

- A United States pack aligning to US Core.
- A Netherlands pack aligning to Nictiz zib and MedMij.

A deployment selects which packs are active. Validation, terminology bindings, and required extensions follow the selected packs. Country specific identifiers, such as the Dutch BSN, are modeled as standard FHIR identifiers within their pack, never as core fields.

## Application modules over FHIR

Application modules do not each open their own connection to the database. They use an internal FHIR access layer:

- Reads and writes go through a typed FHIR client facade against the FHIR server, so all access is validated, audited, and consistent.
- A module owns a set of resource types and profiles as its responsibility, and publishes a narrow interface plus domain events for other modules. For example, the Orders capability owns ServiceRequest and DiagnosticReport handling, and emits events when a result is finalized.
- Where a workflow needs state that FHIR does not model well, that state is kept as a supporting FHIR resource (such as Task) or, only when genuinely necessary, as a small module private table that references FHIR resources by id. Clinical facts always live in FHIR.

## Analytics without hurting the operational store

Serving analytics from the live FHIR store hurts clinical performance. Instead:

- The Analytics and Reporting Platform uses **FHIR Bulk Data export** to move data into an analytics friendly store on a schedule or by subscription.
- Dashboards, population health, and research query the analytics store, not the operational server.

## Versioning and history

FHIR resource versioning and `_history` provide a per resource audit of change. This complements, and does not replace, the AuditEvent based access log described in [security and privacy](security-and-privacy.md). Provenance resources capture who did what and why for clinically significant changes.

## Terminology

Terminology is served through the standard FHIR terminology operations (`$lookup`, `$validate-code`, `$translate`, and value set `$expand`) backed by SNOMED CT, LOINC, ICD, and RxNorm. See the Terminology capability in the [module vision](../modules/README.md).
