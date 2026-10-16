# Core–Bridge module contract

Reconstructed from the owner-supplied handover dated 15 October 2026. This document records design requirements and their stated limits; it does not establish implemented functionality.

[Documentation index](README.md)

## Status

The owner approved the contract outline below.
Exact classes, interfaces, schemas, package names and SDK signatures remain open.

The [module-platform direction](module-platform-options.md) selects the SDK/BOM, startup and frontend composition mechanisms. The separately accepted [Core/Bridge and data/transaction direction](core-bridge-data-design.md) specifies owner schemas, strict cross-owner SQL boundaries, REQUIRED-style atomic composition and same-transaction outbox obligations with idempotent consumers and immutable original context. Exact APIs/tooling and outbox layout remain open. The outline below remains the baseline, read together with those later refinements.

## Module manifest

Each module declares:

- Unique module identity.
- Module version.
- Supported platform-contract versions.
- Required dependencies.
- Optional dependencies.
- Supported dependency versions.
- Offered and consumed interfaces.
- Commands, queries and events.
- Required permissions.
- Configuration.
- Owned data.
- Data migrations.
- User-interface extension points.
- Patient/dossier data classification and registered Emergency Read Contract, or proven applicable exemption.
- Localization contributions.
- Supporting services where required.

Core checks compatibility before installation and during startup.
Administrators should understand the consequences before enabling, updating, disabling or removing a module.

The [module lifecycle requirements](module-lifecycle.md) also require an exit route for modules holding lasting clinical data and evidence of that route during official review. Exact manifest fields, package formats, migration/archive contracts and verification mechanisms remain open. A module's published lifecycle information must make supported installation, updates, dependencies and retirement consequences understandable.

## Emergency read contract

The accepted [emergency read design](emergency-read-design.md) requires a working versioned tested contract before production activation for every persistent patient/dossier-data owner, including local modules. Declare clinicalDataClassification; patient-bound tables require a contract by default, and a transient exemption needs evidence across owned storage. The contract exports typed, versioned, human-readable projections and documents, without reader-side module code. Covered mutations carry same-transaction outbox obligations; delivery exposes durable revisions, expected-owner coverage and explicit gaps. Exact schemas, consistency frontier and conformance tooling remain open (R135–R142).

## Queries

Queries request information without modifying clinical state.
Examples:

- Retrieve a patient's active medication list.
- Retrieve appointment information.

A query can still create an access-audit record.

## Commands

Commands request an explicit state change.
Examples:

- Finalize a consultation note.
- Create a task.
- Request a diagnostic procedure.

Commands need defined:

- Preconditions.
- Permissions.
- Clinical invariants.
- Transaction boundaries.
- Concurrency behavior.
- Error outcomes.
- Repeat-request behavior.

## Events

Events report facts that have already occurred.
Examples:

- A consultation note was finalized.
- A result was received.
- A task was completed.

An event is not an instruction to pretend an uncommitted change happened.

## Typed contracts

Use explicit, typed and versioned contracts.
Do not make the public module API one generic function accepting arbitrary JSON and an action string.
Modules must be understandable and testable without reading each other's internals.

## Synchronous and asynchronous work

Not every interaction should become asynchronous.
Use direct interactions when the caller needs an immediate authoritative result.
Use durable asynchronous processing when follow-up may occur later.
For asynchronous follow-up, a transactional outbox is now accepted under R116, superseding its earlier proposal status.
Detailed decisions remain open for:

- Delivery guarantees.
- Ordering.
- Retries.
- Deduplication.
- Dead-letter handling.
- Reconciliation.

Do not promise exactly-once delivery across independent external systems.

## Transactions

Shared PostgreSQL does not automatically make cross-module operations safe.
Each operation must define what needs to succeed together.
Cross-module transaction participation requires explicit contracts.
Do not bypass ownership merely to obtain a convenient database transaction.

## Request context

Interactions must carry and validate appropriate context, including:

- Authenticated human or service identity.
- Organization.
- Relevant patient.
- Relevant care context.
- Correlation and traceability.

Do not blindly trust actor names or patient IDs supplied by callers.

## Authorization

Core supplies shared security facilities.
Bridge enforces relevant technical contract rules.
The owning module must still enforce:

- Domain permissions.
- Valid state transitions.
- Patient/context restrictions.
- Clinical invariants.

Background jobs, imports and external messages must not bypass these rules.

## UI extension contract

Modules contribute screens, sections, tasks and actions through approved extension points.
They must use:

- Shared navigation.
- Shared patient workspace.
- Shared components.
- Shared validation and error conventions.
- Shared accessibility rules.
- Shared save-state behavior.

The frontend does not independently establish that an operation belongs to a particular patient. The server checks context.

The accepted [shared UI contract](shared-ui-design.md) permits specialized React interactions in declared slots while reserving patient headers/selection, routing, overlays/focus and locking for the shell. Every editable extension registers draft deltas with the shared draft/recovery service. Custom views use semantic tokens and meet accessibility/scoped-style rules. Backend calls use the context-bound SDK client; direct transport and arbitrary endpoints are prohibited. These are admission/conformance requirements for trusted same-page modules, not a hostile-code sandbox. Exact SDK signatures and proof remain open (R130–R134).

## Connect and incoming information

Separate:

1. Information received from another system.
2. Information assessed or adopted into the local clinical workflow.

Preserve source and meaning.
An incoming medication list must not silently overwrite the local active list.
Define handling for:

- Conflicts.
- Repeated delivery.
- Corrections.
- Retractions.
- Identity uncertainty.
- Operator review.

Standards may include FHIR, HL7 v2, DICOM and national profiles where concrete use cases require them. Supporting every standard is not an initial milestone commitment.
