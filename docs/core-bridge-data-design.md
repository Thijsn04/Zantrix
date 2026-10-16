# Core/Bridge interaction and data/transaction design

Date: 15 October 2026. Status: accepted architectural direction, with the owner's additional transaction, consumer/context and schema-boundary requirements. Exact signatures, persistence tooling and delivery implementation remain incomplete. Central versus owner-schema outbox storage is still a choice; the central layout is the current preference, not an implemented or finally selected schema. See R111–R119 in the [decision register](decisions.md). This is not an implementation plan and no product code or probe is included. Keep changes local until explicitly asked to commit/push.

## Intended outcome

Make typed module interaction reliable within the accepted shared-process platform, without turning Core or Bridge into the owner of all clinical data. A clinical command must report an accurate outcome, enforce context/permissions and avoid silent loss, duplicate execution or competing clinical truth. The first complete GP consultation should exercise these boundaries; the contract must also support other settings and countries.

## Responsibilities

| Component | Accepted responsibility | Boundary |
| --- | --- | --- |
| Core | Technical identity/session and organization/location facilities, platform lifecycle/configuration and shared security/audit infrastructure | Does not own patient registration, notes or all clinical workflow logic |
| Bridge | Registry of typed/versioned offered contracts; mediated invocation; common context, compatibility, correlation and execution conventions | Does not own clinical tables or act as an arbitrary action-name/JSON dispatcher |
| Owning clinical module | Domain state, permissions/invariants, repository access, migrations and domain lifecycle | No direct access to another module's repositories, entities or tables as an undocumented shortcut |
| Use-case/workflow owner | Explicit coordination of a multi-module operation through Bridge contracts | Lives in an appropriate capability/care-setting module; not a clinical orchestration bucket inside Bridge |
| Connect | External delivery/adapters, mappings with the owning modules and visible delivery/failure state | External delivery is not part of an assumed local database atomic commit |

The mandatory Bridge can use typed, registered interface proxies/facades for synchronous work in the same JVM. It does not require network calls or serializing every local request. Contract modules expose immutable request/result types and stable identifiers, not ORM entities, database connections or callers' transaction objects. SDK/module manifests declare offered/consumed versions and dependencies. Concrete signatures and registration implementation remain open.

## Invocation and trusted context

Contract direction, with exact signatures/error representations still to design:

1. The entry point authenticates the staff/service identity and obtains trusted organization/session context; user-supplied patient/contact IDs are still untrusted input.
2. Bridge resolves the declared typed contract, validates applicable execution/context constraints and delegates through the owning module's published boundary.
3. The owner checks operation permissions, patient/care context, current state, expected revision and applicable edit/session ownership before writing. Platform checks do not replace owner invariants.
4. The command persists its permitted state transition, outcome receipt and required successful-action audit/event obligations in the defined transaction.
5. Return success only for the committed operation. Validation, permission, stale-version, conflict, unavailable and indeterminate outcomes are distinguishable.

Exact session fencing, identity-provider integration and authorization policy are separate upcoming sections. A lost response can leave the caller unsure whether a commit succeeded; it must not automatically be reported as a confirmed failure and blindly repeated.

Queries go through owning-module query contracts and preserve access audit. Authorized workspaces can compose results through a care-setting/application query service. Cross-module views need defined freshness/version semantics where clinical consistency matters; this is not permission to join all owners' private tables directly or declare every screen a globally consistent snapshot.

## Accepted persistence ownership direction

Begin with one PostgreSQL database and a schema owned by each data-owning module, with module-specific migrations. Shared patient and organization references use the authoritative owner's stable public identity; modules do not create independently editable copies. Persisted IDs, provenance and version/correction semantics remain explicit and require the separate clinical-model design.

For the accepted atomic local composition path, participating repositories use one supported Spring transaction-manager/resource arrangement and the same physical JDBC transaction/connection. Separate connection pools or module-specific database users do not automatically participate in that transaction. This local single-resource design does not require XA/distributed two-phase commit. The exact access/ORM library and registration model remain open; mentioning Hibernate/JPA in conditional rules does not select them as the persistence stack. The platform BOM controls any shared runtime once selected.

Schemas organize ownership but are not an isolation guarantee. [PostgreSQL's schema documentation](https://www.postgresql.org/docs/current/ddl-schemas.html) makes access dependent on privileges. A broad application credential can access multiple schemas; trusted in-process code may still bypass application conventions. Architecture/conformance checks, qualified repository access, reviewed migrations and deployment privileges must make those limits explicit. Separate module credentials would need a different transaction design; no hostile-module sandbox is claimed.

Cross-owner database JOINs and foreign keys are prohibited. References use stable public IDs and Bridge contracts, not cross-owner ORM relationships or repository access. Each module's manifest declares its schema namespace using `schema_prefix`; the installer resolves this to an exact allowed schema set. Prefix text must not become permission for arbitrary similarly named schemas. Storage-free modules must declare that they own no persisted schema.

Build/package verification and migration validation must reject detectable access outside the owner's declared schemas, including mismatched JPA mappings, joins/foreign keys and migration targets. Flyway/Liquibase are examples of tooling requiring these checks, not selected tools. Inspect native queries, views, routines, triggers and migration callbacks as applicable; an annotation check alone is insufficient. Dynamic SQL and trusted in-process code limit what static inspection can prove. Unverifiable constructs need controlled review/restriction and integration evidence, not a claim that scanning is a security sandbox.

Shared technical storage, such as a possible central outbox, has its own technical owner and migration set. Clinical modules call its published API in their existing transaction; they do not gain permission to alter the platform schema directly. A platform outbox is not an exception allowing arbitrary cross-owner clinical SQL.

Without cross-owner foreign keys, reference integrity is an application/lifecycle obligation. Matching, merges, correction/retirement and owner deletion rules must preserve or resolve durable references through explicit contracts. Checking existence once is not a universal protection against later removal or concurrent change. Exact reference-lifetime rules and identity formats remain to be designed.

## Transaction alternatives

| Approach | Rule | Main trade-off |
| --- | --- | --- |
| A — hybrid, accepted | Owner-local transactions by default; explicitly contracted synchronous multi-owner operations can share one local transaction; later work uses durable events | Preserves useful local atomicity while requiring disciplined boundaries and a single participating resource arrangement |
| B — strict independent transactions, not selected | Every module commits independently; multi-module workflows coordinate through durable steps/compensations | Stronger transaction independence, but extra intermediate states, recovery and compensation even for operations that could be atomic in this monolith |

The owner accepted A. Every command declares whether it starts an owner-local transaction, can participate in a supported atomic composition, or represents an independently progressing workflow. Only explicitly designed compatible operations may join a cross-module transaction. A workflow owner coordinates through public contracts; it cannot write the other owner's tables. A forbidden/mismatched participation request fails clearly instead of silently committing an independent part.

[Spring transaction propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html) supports participating versus independent transaction scopes. Open a joint operation at its outer workflow/initiating-owner boundary using REQUIRED semantics. Atomic participants join that physical transaction; they must not start independent clinical transactions with REQUIRES_NEW or use savepoints to silently weaken the agreed all-or-nothing operation. Exact contract declarations, annotation placement and transaction-manager configuration remain to be specified and tested.

REQUIRED is not deadlock prevention by itself. Define consistent lock acquisition, bounded timeouts and safe whole-operation retry where appropriate. Pool/resource behavior and rollback-only propagation need proof. Independent error logging, if necessary, is a narrowly controlled technical concern, not a general REQUIRES_NEW exception: prefer a separate post-rollback path and ensure it cannot create pool/self-lock dependencies or change clinical outcome. Its implementation remains open.

No synchronous external service/network I/O, apart from the participating database I/O itself, is allowed inside the joint transaction. In particular, Connect cannot deliver externally while locks are held; record the delivery obligation in the outbox instead. No long-running review or user interaction is held inside a database transaction. If work cannot be completed in one short local transaction, persist its own progress and use a documented recovery/continuation policy. Rolling back PostgreSQL cannot retract a message already delivered to another system.

## Rollback and persistence-context discipline

An atomic participant's transactional failure must propagate as failure of the complete operation. A caller must not swallow a failed Bridge mutation, clear a rollback marker or commit the remaining changes while reporting overall success. Expected non-mutating validation outcomes need explicit contracts; they are not permission to continue after a failed mutation.

If JPA/Hibernate is used, flushing can fail before final commit and a transaction-scoped persistence context is shared in ways that affect all participants. Do not permit arbitrary module-level clear/flush operations to discard another participant's state. After a transaction-aborting persistence failure, abort the operation and discard/recreate the affected transaction-scoped context; clearing it does not repair a failed transaction. Re-read state for any authorized retry rather than reusing potentially inconsistent entities. Exact persistence-unit/context layout remains open. [Jakarta Persistence transaction-rollback rules](https://jakarta.ee/specifications/persistence/3.2/jakarta-persistence-spec-3.2.html#_transaction_rollback) explain the lifecycle hazards; the rule is conditional and does not select an ORM.

## Accepted durable follow-up direction

Adopt a transactional outbox: persist the event/delivery obligation in exactly the same physical local database transaction as the owner's committed change. Failure to persist a required obligation fails that operation; an after-commit insert in another transaction is not equivalent. Publish/process it afterward. Shared technical infrastructure does not become the owner of clinical event meaning. This is not full event sourcing or a mandate for a separate broker.

Process-local after-commit callbacks alone do not establish crash-safe delivery. [Spring transaction-bound event listeners](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html) define transaction phases; a durable publication obligation still needs persisted state and recovery. The [Spring Modulith event publication registry](https://docs.spring.io/spring-modulith/reference/events.html#the-event-publication-registry) is an existing candidate to assess before building an equivalent; it is not selected and must fit Zantrix's contracts and context policy.

### Storage alternatives and preferred layout

The owner supplied two acceptable alternatives for review: owner-schema outbox tables managed through shared infrastructure, or a central technical outbox written through that infrastructure within the caller's transaction. Prefer the latter as the current proposal: one technical component owns a central schema/table (the owner's `core_outbox.events` is an illustrative name), with source module, event and correlation identifiers. It owns transport state only; the clinical owner owns semantics.

A central layout can simplify migrations, queue management and operational visibility, but introduces shared contention, retention and access concerns. Its rows still require per-consumer delivery state, not a single global delivered flag. Owner-schema tables do not inherently require separate pollers: one dispatcher can service several tables. Thus poller count alone does not finalize storage layout. Exact table/schema naming and central versus owner storage remain open, with the central layout recorded as Proposed in R119.

Repeated delivery must be safe. Consumers atomically claim/record a stable event ID scoped to the consuming handler and commit local effects with that inbox/processing record in the same consumer transaction. A separate check-then-insert is not enough under competing workers; uniqueness/concurrency must be enforced. A naturally idempotent handler can use another documented equivalent, with proof. One consumer's completion must not suppress other intended consumers. Retention must cover supported replay; schema upgrades must not silently reset processing history.

At-least-once delivery is the intended operational contract under durable storage, retries and recovery, not a guarantee supplied by an outbox table alone. Permanent failure or unavailable consumers remain visible for repair; external side effects still require their own idempotency/reconciliation. Decoupling follow-up avoids synchronous dependence on downstream services, but outbox persistence, storage failures, contention and backlog still affect the source system. No percentage of actions or latency guarantee has been measured.

Track pending, completed, retryable and attention-required outcomes accurately. Ordering is defined per operation/stream where required, not an assumed total order over every event. Retry/retention/backoff/dead-letter policies and event schemas remain open.

Connect persists outbound obligations and distinguishes queued, attempted, delivered, failed and unknown outcomes according to the external protocol. Timeouts after a remote side effect may be ambiguous; use external idempotency/reconciliation where available. Do not promise exactly-once delivery across systems.

## Repeat requests, conflicts and audit

Mutating command contracts define an idempotency policy. A repeated request with the same scoped key and same intent must return the recorded result rather than re-executing the clinical operation; reuse for a different payload is rejected. Bind the receipt to its authenticated scope and commit it with effects where applicable. Different new keys can still represent clinical duplicates, so idempotency is not domain duplicate detection. Key scope, retention and recovery lookup APIs need review.

Expected revisions and edit/session ownership guard concurrent writes independently of idempotency. A user's old tab cannot regain write authority just because its retry key is new. Lock granularity and lease mechanisms remain open in the existing safety requirements.

Successful mutation audit/provenance obligations must not disappear between a business commit and an audit enqueue. Propose storing the required local audit record or durable publication obligation in the same transaction. Denials, reads and rolled-back attempts require their own audited path rather than relying on a record that rolls back with the failed mutation. Audit schema, integrity, access, retention and handling of audit-storage failure are unresolved; do not claim a complete audit design here.

## Immutable execution/audit context

Every durable event envelope carries an immutable, provenance-bound execution/audit snapshot: originating staff/service actor, organization, applicable patient/care context, occurrence time, source module, correlation/causation and the authorization basis/policy reference under which the source action occurred. Exact field schema, minimization, integrity and retention remain to be designed. Do not embed reusable passwords, access tokens or an unnecessary full clinical record in the context.

Asynchronous execution records both the originating context and the actual executing service identity/time. A snapshot is historical evidence of the original decision, not a reusable bearer credential or unconditional authorization to perform later work. Current service capability, organizational restrictions and any required delegated authority must still be checked according to an explicit execution policy. Expired/revoked staff access does not imply blindly deleting legitimate committed obligations or blindly continuing every action. That policy is the next identity/authorization design section.

## Synthetic consultation illustration

If a use case requires note finalization and contact completion to succeed together, its workflow owner invokes both owners' explicitly atomic-capable contracts through Bridge in one supported local transaction. Both changes, outcome/audit obligations and required event obligations commit together or roll back. This illustrates transaction boundaries; it does not finalize contact/signing domain rules.

Follow-up can remain open after contact completion, as already accepted. A later task obligation can be durable without claiming that the task has already been created. If task creation must be immediate for a particular invariant, it belongs in that explicitly defined atomic operation instead. The UI must distinguish committed, pending and failed follow-up.

## Proof obligations and next decisions

Before an implementation plan, review actual persistence/transaction resources, contract signatures, command outcomes/idempotency and minimal audit/outbox boundaries. A later approved synthetic proof must verify:

- Module integration through Bridge without direct cross-owner repository/entity access.
- Complete rollback of an explicitly atomic two-owner operation when the second participant fails.
- Recovery when a successful commit's response is lost, and safe repeated requests.
- Durable follow-up after a crash between commit and publication, including repeated consumer delivery.
- Rejection of stale patient/session/edit context and unauthorized service execution.
- Truthful pending/failed delivery state and a recovery route.
- Rejection of cross-owner joins/foreign keys/migration targets and mappings outside the exact declared schema set, including hostile or dynamically assembled SQL limits.
- Complete rollback when an outbox insert fails, a participant incorrectly requests an independent transaction, or a persistence flush aborts the operation.
- Concurrent duplicate event processing, multiple independent subscribers and crash recovery after consumer commit but before dispatcher acknowledgment.
- Preservation of immutable original context while identifying the actual worker and rejecting unauthorized later execution.

No runtime tests have been run. The owner accepted the role separation, hybrid model, owner schemas, strict cross-owner SQL boundary, transaction discipline, same-transaction outbox, idempotent consumers and immutable original context. Exact APIs/tooling and the central versus owner-table outbox layout still need design/proof. Clinical examples in the review are illustrations, not approved clinical rules or claims of legal atomicity. The [identity, session/context and asynchronous authorization direction](identity-session-authorization-design.md) is now accepted under R120–R129, including concrete per-principal transaction guards and protected volatile recovery; implementation/proof remain pending.
