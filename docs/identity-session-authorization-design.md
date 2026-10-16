# Identity, workspace sessions and authorization design

Date: 15 October 2026. Status: Accepted architectural direction, refined by the owner's subsequent four technical mechanisms and explicit protected-buffer choice (R120–R129). Existing account/badge/session/access requirements retain their recorded status in the [identity requirements](identity-access.md), [authorization requirements](authorization.md) and [decision register](decisions.md). Exact implementation, buffer lifetime and proof remain pending. No product code, provider configuration, dependency installation or runtime proof is included. Keep this document local until explicitly requested to commit/push.

## Purpose and fixed requirements

Support Zantrix-managed staff accounts and institutional federation, managed and self-hosted operation, trusted on-site password/badge access, remote MFA and at most one active online staff workspace per person. Multiple patient workspaces can live within that staff workspace. Preserve roaming drafts, actual locking, scoped profiles/permissions, explicit restrictions, temporary access, consented support and audit.

Do not rediscover the accepted five-minute privacy screen, seven-minute actual lock, badge presentation to lock, daily/occasional badge verification or high-impact-administration reauthentication. Their detailed security bounds and hardware remain open. The read-only emergency reader has a separate offline boundary and cannot be assumed to obey instantaneous central revocation during a partition.

## Responsibility boundaries

| Layer | Accepted responsibility | What it does not establish |
| --- | --- | --- |
| Identity provider/broker | Account credentials, authentication, institutional federation and evidence of authentication assurance/freshness | Clinical permissions, an active Zantrix workspace or permission for a patient operation |
| Core principal/access services | Stable staff/service principal links, organization/location grants, temporary access, policy revision and authorized technical administration | Patient identity ownership or clinical validity of a requested mutation |
| Core workspace/session service | Authoritative online workspace ownership, lock/revocation state, server session and trusted execution context | A promise that an unreachable workstation has erased already displayed data |
| Bridge and data owner | Apply shared context/permission checks through published contracts, then enforce owner-specific state and clinical invariants | A sandbox against malicious modules in the same process |

The staff principal is distinct from a patient identity. The patient-registration module retains ownership of patient identity and merge history. Authentication of a staff member does not imply that they may inspect every patient.

## Identity-provider alternatives

| Option | Arrangement | Main trade-off |
| --- | --- | --- |
| A — Keycloak broker, accepted | Deploy an institution-controlled Keycloak service for local accounts and brokerage to institutional providers; Zantrix uses one supported OIDC integration to that broker | Concentrates authentication/federation integration, but adds a service to operate and makes broker health significant |
| B — direct providers plus a local-account provider | Zantrix integrates directly with configured institutional OIDC providers and separately supports an established provider for local accounts | Can avoid an extra brokering hop for a federated institution, but expands provider-specific linking, assurance, logout and lifecycle integration paths |

The owner accepted A as the initial supported setup, retaining an explicit OIDC adapter boundary. This selects a new architectural direction, not restoration of the old repository's Keycloak deployment, version or realm. Option B remains comparison history. The broker is installed within the institution's managed/self-hosted deployment; it is not a mandatory Zantrix-operated cloud service. It can be a supporting process without changing the accepted modular monolith for clinical modules.

[Keycloak's administration guide](https://www.keycloak.org/docs/latest/server_admin/index.html#_identity_broker) documents brokerage to external OIDC/SAML providers. This supports the accepted mechanism, not a claim that all institutional MFA, hardware badges or provisioning work without integration. OIDC is the accepted application-to-broker protocol; SAML is only a possible upstream compatibility route where actually needed, not an initial promise to implement every federation option.

The recommendation adds operational work: service/database ownership, upgrades, backup, keys, trusted hostname/proxy configuration and availability. Those require operating evidence before deployment. An upstream institutional outage can prevent new federated authentication even if the local broker is running. Do not silently create a local-password fallback or reuse credentials collected by the application.

## Stable identity linking and provisioning

Create a stable internal staff/service principal and explicitly link validated issuer/subject identities. Do not merge staff accounts or grant clinical access based solely on matching names or email addresses. Broker/imported identities and issuer changes need controlled linking and migration. [OIDC Core](https://openid.net/specs/openid-connect-core-1_0.html#IDToken) supplies issuer/subject and authentication-context concepts; the internal principal and grant lifecycle remain Zantrix responsibilities.

Successful authentication or automatic provider registration is not automatic admission to the clinical organization. Access must pass the configured onboarding/assignment process. Institutional groups can be mapped only through configured authorized rules; a provider role is not unrestricted authority over Zantrix. Deprovisioning, temporary-staff expiry and provider/account disable events must reconcile with Core grants and sessions. Exact provisioning API, event/notification reliability and identity-link recovery remain open; SCIM or any particular directory integration is not selected here.

Direct Core revocation can reject subsequent online operations at its ordered boundary. A change made only in an upstream directory is not known instantly without a reliable integration; notification/polling bounds and failure handling must be explicit before claiming end-to-end immediate deprovisioning.

Technical provider/server administration does not grant application dossier permissions. Infrastructure impersonation, credential reset and database access still need separate operational safeguards; application role checks do not eliminate that privileged infrastructure risk.

## Browser authentication/session boundary

Use the Spring backend as a confidential OIDC client and browser-facing backend-for-frontend. The same-origin React application receives a server-session cookie, not provider access/refresh tokens stored in browser local/session storage. Use authorization-code authentication with PKCE and validate issuer, audience, signatures, redirect binding, state/nonce and applicable assurance. [Spring Security OAuth2 Login](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html) supplies relevant code-flow integration; [OAuth security best practice](https://www.rfc-editor.org/rfc/rfc9700.html) informs the protocol profile. No default configuration is claimed to implement every requirement.

Provider tokens and session secrets remain server-side, with controlled storage, expiry and rotation. Cookie flags, fixation protection, CSRF defenses and logout behavior need an explicit tested profile. [Spring's CSRF guidance](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html) remains relevant for cookie-authenticated mutations. Same-origin routing or HttpOnly cookies alone do not solve CSRF or malicious JavaScript. Runtime-loaded modules are trusted UI code and must follow the common client/session contract.

API/service integrations use their own narrowly scoped identities, not a simulated human browser session or a staff refresh token. The staff one-active-workspace rule does not mean background services must impersonate or occupy that human workspace. External patient portals have a separate future identity/representation design.

## Authoritative workspace and revocation model

Maintain an authoritative Core record for each staff principal's active online workspace and monotonically changing session/authorization generations. Bind requests to the authenticated server session, workspace and relevant patient/contact context; caller-supplied IDs still require validation. Multiple patient tabs retain separate patient-workspace identities within one staff workspace.

On authenticated transfer to another workstation, atomically advance workspace ownership/generation, invalidate the old workspace's authority and restore confirmed server-side workspace/draft state. Send the former workstation a lock signal for prompt visual response, but server access rejection does not depend on successful signal delivery. A provider SSO session or unexpired JWT alone must not authorize a stale workspace.

For mutations, serialize the relevant access/workspace guard with the business transaction through the Core contract. A validated generation must remain valid for the defined commit boundary: an operation cannot check once, race a completed revocation, and then commit unchecked. A short operation that wins the ordering may commit before revocation completes; after revocation completes, stale operations must not newly commit. The accepted per-principal guard below makes this ordering concrete; lock ordering and integration still need proof.

Read/access checks consult current authoritative state. A cache or missed invalidation cannot silently override immediate online revocation; policy/session caches need a consistent freshness protocol, or cannot be authoritative. If required online authority cannot be established, reject protected online operations rather than accepting stale privilege. Denial does not authorize discarding drafts or clinical obligations.

Time-based expiry of temporary staff, support consent and individual additions must be checked against trusted server time at the defined operation/commit boundary. A policy generation that has not changed yet is not permission to continue beyond an end time. Exact transaction time bounds and expiry/commit protocol require proof.

This enforces future server operations, not retraction of data already delivered. A disconnected workstation cannot be guaranteed to hide its screen immediately by a remote signal. Its local privacy/lock behavior, persistent storage, device security and bounded recovery need their own tests. Emergency-reader partition behavior remains a separate design and must not be mislabeled as a global single-active guarantee.

## Per-principal transaction guard

R124 accepts a Core-owned PostgreSQL guard record per principal with `workspace_generation` and `policy_generation` as monotonic BIGINT values, plus active workspace, status and applicable expiry. `core_session.active_workspaces` is an illustrative qualified table name, not a separate schema per employee. Core owns its access API; modules must not query this table directly.

The mutation protocol is:

1. Begin the actual business transaction on the shared JDBC resource. Before owner mutations, owner locks or ORM operations that could auto-flush, call the Core guard through Bridge.
2. Core locks the principal's guard row with `SELECT ... FOR SHARE` in that same transaction. Validate the authenticated principal, staff workspace, both expected generations, active status and operation-specific current authority. Missing records or mismatches deny the operation and roll back; do not recreate authority from a cookie or caller assertion.
3. Keep the guard lock through the outer transaction's completion. Domain writes and required outbox writes participate in that transaction. A savepoint rollback must not release the guard and then permit an unguarded commit; reacquire/revalidate or abort the outer operation.
4. Transfer/revocation updates the same row, advancing the appropriate generation and changing ownership/status or policy atomically. Report completion only after that transaction commits. Grant changes covered by a principal's policy generation must participate in this ordering; unrelated asynchronous cache invalidation is insufficient.

The [PostgreSQL row-lock matrix](https://www.postgresql.org/docs/current/explicit-locking.html#LOCKING-ROWS) establishes the relevant conflict. An ordinary non-key `UPDATE` can acquire `FOR NO KEY UPDATE`, rather than `FOR UPDATE`; both conflict with `FOR SHARE`. `FOR KEY SHARE` would be too weak. This avoids a single global workspace lock, but shared clinical/domain resources, multi-principal operations and database capacity can still cause contention. Do not claim other clinicians can never block.

Require a deterministic Core-before-owner lock order, bounded transactions, lock-wait limits and safe whole-operation handling of deadlocks/serialization failures. Multi-principal guard acquisition needs a consistent principal order. The isolation/retry profile remains to be selected and tested; an old transaction snapshot is not evidence of current authority. Organization-wide policy updates need an equally ordered revision/guard mechanism or complete affected-principal fencing before reporting revocation complete; the single row alone does not solve those updates.

A [Spring transaction synchronization](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/support/TransactionSynchronization.html#beforeCommit(boolean)) can provide a final validation/fail-closed backstop. It is not the selected substitute for the early guard: `beforeCommit` does not establish that no query or ORM flush occurred earlier, and taking Core locks after domain locks can invert lock order. Time-based expiry still needs an explicit final time check and defined authorization boundary; row locks do not stop the clock. No protocol claims to make an arbitrary wall-clock deadline identical to the physical commit instant.

## Explicit patient context for every clinical operation

R126 prohibits a global mutable server-session `current_patient_id`. The session identifies the staff workspace; Core registers distinct patient-workspace identifiers within it. Every patient-bound API call, including autosave, polling, delayed callbacks and module actions, explicitly supplies the patient-workspace ID and expected patient ID through a supported request contract. `X-Zantrix-Patient-Workspace-Id` and `X-Zantrix-Expected-Patient-Id` are candidate header names; equivalent path parameters are allowed. Platform operations without a patient use their own explicit scope.

Bridge validates that the patient workspace belongs to the current authorized staff workspace and is bound to exactly that patient. Core validates current authority; the data owner additionally validates that referenced drafts, records, contacts and target entities belong to that patient and care context. IDs and headers are assertions to validate, not credentials or proof of access. Patient identifiers must not leak through routine request logging.

Changing the foreground tab never retargets an already-created request. A late response must carry its original context/version and be ignored or routed safely if that workspace has closed or changed. Recovery cannot silently rebind an old draft to a newly selected patient. Detailed envelope/error contracts remain open for the shared UI design.

## Roaming, locks and authentication freshness

The five-minute privacy screen remains dismissible during its two-minute countdown as agreed. At actual locking, unlock requires the applicable authentication policy; an existing provider SSO cookie must not automatically bypass the required unlock/step-up. Background polling or server keepalives must not be treated as meaningful staff activity. Exact inactivity signals and server/client timer coordination remain open.

Restore only confirmed stored draft versions on a new workstation. Unsynchronized input on the old workstation needs an explicit authorized recovery path; stale-session writes cannot overwrite newer work. A transfer must state what was restored and what was not confirmed saved. No full remote desktop or lossless recovery from an unavailable device is assumed.

High-impact administration uses operation-bound reauthentication freshness, with the action, scope and outcome audited. The exact strength/window must follow the selected provider and institutional policy. Ordinary clinical work does not inherit indiscriminate administration prompts.

R125 requires a fresh, one-use server-side challenge for actual unlock and high-impact reauthentication, bound to the intended principal, current workspace generation, lock event or privileged action, expiry, state and cryptographic nonce. A user switch is a separate explicit flow. The OIDC request requires active authentication (`prompt=login` / `max_age=0`) and the applicable assurance profile. Requesting `acr_values` alone does not establish freshness or prove the returned assurance met policy.

Validate the signed ID token and normal code-flow bindings, matching nonce, subject, current challenge/workspace and required returned assurance. Require and check `auth_time`; `iat` or a newly issued token is not proof of new authentication. [OIDC Core](https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest) requires `auth_time` for a `max_age` request. The profile must prove authentication after the applicable lock/challenge boundary, with explicit clock and timestamp-resolution handling. Blindly comparing a millisecond `locked_at` to a seconds-resolution provider time can reject a valid same-second login. Do not use broad clock-skew tolerance to accept demonstrably pre-lock authentication; ambiguous evidence must trigger another verifiable challenge or deny unlock. Verify the chosen broker/upstream and badge flow actually performs the required fresh authentication; an existing SSO cookie must not bypass it.

## Browser storage and fail-closed presentation

R127 prohibits patient information and drafts in persistent browser storage for the regular online workstation, including localStorage and IndexedDB. Apply the same rule to module SDKs, client persistence plugins, browser HTTP caches and service-worker/Cache Storage paths; use an explicit no-store response/cache policy and verify it. Provider/session secrets remain server-side as already required. Controlled exports/downloads, endpoint-managed files and the separate emergency reader need their own explicit policy; this is not permission to turn the online shell into an offline record cache.

Draft input lives in the shell's volatile state and is saved to the server-side draft store with patient context, expected version and workspace generation. Saving is a request/acknowledgement protocol, whether triggered promptly or periodically; starting a request is not confirmation of durability. Only a successful server acknowledgement establishes the displayed Saved state. Exact autosave cadence and revision/conflict handling remain open.

R128 requires the shell to lock on detected loss of the BFF authority channel, an authentication failure or an explicitly typed stale-workspace response. A generic HTTP 409 can also be a clinical version conflict and must not automatically mean session revocation. Stop clinical interaction and further protected requests, unmount clinical views, clear patient display/query/module caches and show the lock screen. Discard or safely context-check late responses. Apply both server push and a client heartbeat/liveness watchdog; modules cannot opt out.

Detection is bounded by the selected heartbeat/watchdog policy, not instantaneous knowledge of a broken network. Suspended tabs and a frozen browser may not execute timers, so device/workstation locking remains necessary. Unmounting a component alone does not clear shared stores, and JavaScript cannot promise forensic zeroization of all prior memory, browser history or already viewed pixels. The proof must inspect the full shell/module/cache lifecycle.

R129 accepts a protected volatile recovery buffer instead of discarding unacknowledged input at every transient interruption. Keep only the unsaved delta in a shell-private closure/service outside display/module stores, bound to `principal_id`, staff/patient workspace, patient and care context, originating workspace generation, `base_draft_version` and expiry. Store nothing in localStorage, sessionStorage or IndexedDB. Retain the minimum base/context needed for safe reconciliation; never infer a different patient from the current tab.

Recovery requires fresh authentication of the same principal on the same workstation with the original tab/process still alive, plus current patient/operation authorization. Re-establish the single active workspace under the normal Core transfer protocol; an old generation is not revived by recovering a buffer. Compare the latest server draft/version with the buffer base. An unacknowledged request may already have committed, so reconcile request identity/version before replaying it. When another workstation has advanced the draft or ownership, offer an explicit authorized merge/copy into a reviewed draft or discard choice. Never automatically overwrite the server, reinstate old authority or treat recovered text as finalized clinical content. Editing locks and conditional version checks still apply to the resulting save.

Before a different principal's workspace loads, invalidate the previous principal's buffer and remove its references and recoverable copies from shell/module stores. An ordinary lock retains the isolated delta for its owner; an explicit user switch destroys it. A plain JavaScript closure is a privacy/control boundary within trusted application code, not a sandbox from hostile same-origin code or a promise of forensic memory zeroization. Dereferencing immutable strings cannot guarantee immediate physical RAM erasure.

Apply a hard maximum buffer lifetime, anchored at interruption without renewal by background heartbeats. Its exact duration is Open; a shift/session maximum or fixed post-lock duration were examples, not selected values. A timer performs cleanup while the shell runs; independently check the deadline before any access, on resume/reconnect and before unlock/recovery. Expired content is never offered for recovery even if a suspended tab delayed timer execution. Do not claim JavaScript can erase RAM at an exact wall-clock instant while suspended. Report unsaved-state expiry/loss truthfully without retaining the discarded clinical text.

This is best-effort recovery from transient disruption. Closing/reloading the tab, process/device failure, a different user's login or TTL expiry can lose the unsaved delta; it is not a durability guarantee. Confirmed server drafts remain recoverable under current authority. The owner's privacy/clinical-safety rationale records the design intent, not established NEN 7510 compliance or clinical validation.

## On-site, remote and badge assurance

Classify trusted on-site access using institution-controlled device and access-path evidence, not a client-supplied flag, IP address or VPN membership alone. A managed device credential and trusted gateway are candidate inputs; no hardware/trust technology is selected. Trust changes during an existing session must trigger appropriate re-evaluation; a session obtained on-site is not an unrestricted remote-MFA exemption.

Remote routes require verified MFA assurance. Provider evidence must be validated through the configured assurance policy rather than trusting an arbitrary MFA boolean. Authentication time/assurance has to survive federation/brokering in a testable form. If the upstream provider cannot meet the required assurance, that route is unsupported until resolved rather than silently downgraded.

Badge authentication requires a verifiable credential/reader flow linked to the correct principal. A keyboard-emulated/plain readable badge number is not sufficient proof. Device/reader registration, cryptographic challenge or equivalent assurance, replay resistance and loss/revocation remain an independent integration design. No Keycloak feature is claimed to supply the finished badge solution.

Preserve first-daily password verification and occasional bounded rechecks as the accepted preference. Federated passwordless or institutional authentication may not expose a password prompt; Zantrix must not collect an institutional password to manufacture one. Provider-supported fresh authentication and any equivalent policy require explicit validation/owner review. Do not silently change the accepted requirement or provision a local fallback. Meaning of day/night shift, maximum verification age and frequency remain open.

## Authorization and owner enforcement

Core evaluates applicable function profiles, individual additions/expiry, organization/location scope, explicit restrictions and any permitted emergency exception. Return an operation-specific authorization decision/context tied to current policy state; do not hand out an unconditional patient-access flag. The owning module still checks patient/care relationships, state transitions, edit ownership and clinical invariants through appropriate contracts.

Keep technical administration, audit oversight, dossier access and support consent as distinct capabilities. Explicit denies override ordinary grants; break-glass applies only to designated overridable restrictions with reason, scope and audit. Whether a separate policy engine is useful, concrete evaluation types, precedence implementation and failure policy still need design; adding one is not selected here. No cross-owner SQL join is introduced to compute effective access.

## Authority for asynchronous work

The immutable outbox context accepted in R117 records the original actor/purpose/authorization basis. At execution, record the actual service identity, current applicable policy and outcome separately. No actor string, historical role list, browser session or stored staff token grants execution authority.

The accepted execution policy distinguishes:

- **Continuation of a committed obligation:** deterministic indexing, durable task materialization or another explicitly authorized workflow step may execute under a scoped service/workflow mandate after the original staff member logs out. Requiring their browser session to survive would lose legitimate work.
- **A new discretionary clinical/privileged action:** needs current applicable authority or explicit bounded delegation. A historic snapshot cannot revive revoked rights, extend support consent or justify a different patient operation. Hold and route for review/reassignment when authority is absent; do not silently discard the obligation or impersonate a successor.
- **External dispatch:** validate the committed message/workflow state, current service/recipient policy and any cancellation/retraction before sending. This is not identical to either an automatic permission replay or requiring a live doctor session for every retry.

Classify each durable command/event contract and declare its permitted consumer/service capabilities, purpose, scope, expiry/delegation and cancellation behavior. This policy must be reviewed per operation. Staff revocation does not universally cancel every committed obligation, and the existence of an obligation does not bypass current restrictions. Disabled/uninstalled consumers and refused authority need visible recovery paths.

## Provider failures and the emergency reader

Separate broker outage, upstream federation outage, Core/session-store outage, network isolation and actual endpoint compromise. Existing primary sessions may continue only while the required server authority and bounded authentication freshness can be established; exact grace/failure rules are unresolved. Do not promise that federation works offline or allow default password bypass during outages.

The emergency reader authenticates/authorizes independently under the agreed read-only continuity model. It needs its own protected credentials/keys, granted dataset/scope, access expiry/freshness, local audit and recovery process. It cannot depend on live primary Core/Bridge, broker or unavailable upstream IdP. Those provisions and offline revocation bounds remain the later continuity section; this proposal does not choose their mechanism.

## Review and proof obligations

The owner accepted R120–R123, supplied the transaction-guard, fresh-authentication, patient-context and browser-storage/lock refinements in R124–R128, and explicitly selected the protected volatile recovery buffer in R129. Its exact TTL and integration contracts remain open. Do not reopen the buffer choice or settled badge timings and role requirements.

Before implementation, review the actual provider/assurance profile, identity linking/provisioning and revocation integration, workspace/commit fencing protocol, cookie/CSRF controls and asynchronous service/delegation contracts. A later approved synthetic proof must cover federation linking without email-based takeover, remote assurance downgrade rejection, stale-tab/session races, both orderings of workstation transfer/revocation versus mutation commit, rollback/savepoint/auto-flush behavior, policy changes and expiry, locked-session SSO bypass (including same-second timestamps), support expiry and service execution after human logout/revocation. Also prove patient A/B isolation under concurrent autosave and reordered responses, no persistent clinical browser cache, typed stale-session errors, lost push/network/heartbeat and suspended-tab behavior, same-principal authorized recovery, different-principal destruction, acknowledgement-loss deduplication, roaming conflict handling and TTL checks after suspension. Verify that module stores and late responses cannot resurrect discarded data.

Exact versions, broker topology, badge hardware, policy-engine adoption, grace/expiry numbers and emergency authentication remain open. No proof has been run. Accepted architecture and protocol requirements are not implemented functionality or proven clinical/security guarantees. The [shared UI shell/platform/module direction](shared-ui-design.md) is now accepted in R130–R134 and applies these authority/recovery constraints to all extensions. Primary/emergency operational boundaries follow; visual presentation and branding remain a separate later section.
