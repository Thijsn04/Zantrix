# Shared UI shell and module interaction design

Date: 15 October 2026. Status: Accepted architectural direction (R130–R134), following the owner's explicit choice of controlled freedom and four UI-contract boundaries. Exact APIs, enforcement tooling and runtime proof remain open. This document specifies requirements; no product implementation, finished visual brand, SDK publication or clinical validation is included. The later [visual direction](visual-direction.md) selects the visual foundation while leaving exact brand/token values open. Keep documentation local until explicitly requested to commit/push.

## Controlled freedom

Modules may supply specialized React components inside approved shell extension points. They are not restricted to declarative forms or platform-generated screen templates. Growth curves, timelines, medication administration views, odontograms and scheduling boards illustrate why specialized interaction is needed; these examples are not commitments to implement those specialties in the initial GP milestone or approved clinical rules.

The shared shell and SDK impose navigation, patient-context, accessibility, draft and security contracts on those components. Official and external modules follow the same requirements. A specialized component does not acquire ownership of the entire page or permission to establish its own patient/session authority.

## Responsibility boundaries

| Component | Accepted responsibility |
| --- | --- |
| Shell | Staff/patient workspace lifecycle, authoritative patient banner, application routing/title, focus and overlay coordination, lock/priority presentation, technical draft registration/status and protected volatile recovery |
| UI-SDK | Shared components and semantic tokens; versioned slot, navigation/dialog, draft and context-bound client contracts; common validation/error, localization and accessibility conventions |
| Module UI | Domain content and specialized interaction within its declared slot; minimum serializable draft deltas, domain-specific presentation and accessible operation |
| Backend data owner | Authorized reads/writes, patient/entity association, clinical validation, edit/version/concurrency rules and authoritative finalization |
| Core and Bridge | Current workspace/access authority and mandatory technical contract/context enforcement, as defined in the accepted identity and transaction designs |

The frontend shell is a technical presentation boundary. A shell-owned patient banner obtains patient facts through the registration owner's contract; this does not move patient registration into Core or make the browser the source of truth.

## Shell-owned slots, navigation and overlays

An extension declares its module identity, supported contract version, approved slot, relevant scope and required capabilities. Exact manifest fields and discovery remain open within the [accepted module-platform direction](module-platform-options.md). The shell resolves the slot, binds context and controls mounting, navigation and cleanup. A task panel may have a non-patient scope or an explicitly bound patient; the module cannot infer one from the last foreground tab.

Modules must not:

- Render a competing patient header, patient selector or patient-search bar that independently changes the enclosing workspace. A domain workflow needing patient lookup requests a shell-controlled selection flow with explicit result/context validation; it cannot silently switch the current patient.
- Escape their allocated region through uncontrolled fixed/absolute positioning, global styles, portals to arbitrary document roots or global window modals. Positioning inside a declared local container for a specialized chart is permitted under the layout contract.
- Mutate `document.title`, `window.history` or the browser URL directly. Sub-navigation and deep links go through shell routing, preserving context, dirty-state guards and appropriate history behavior.
- Render independent document-level dialog/popover/fullscreen layers. Use the SDK's shell-owned overlay service; `usePatientDialog()` is an illustrative name, not a published hook.

The overlay service supplies the correct patient/context framing, focus lifecycle and priority handling. Actual locking interrupts and removes clinical dialogs and their clinical content; it does not merely cover them with a higher number. Define which urgent notifications can remain visible without patient disclosure while locked. The exact priority taxonomy is Open.

Browser [stacking contexts and top-layer elements](https://developer.mozilla.org/en-US/docs/Web/CSS/Guides/Positioned_layout/Stacking_context) make a large `z-index` alone insufficient. Validate document-level overlays, keyboard focus, screen-reader exposure and background interaction under locking. An obsolete dialog result must not execute an action after its patient/workspace/version has changed. The shell owns dismissal and cleanup of overlays when a slot unmounts or loses authority.

## Mandatory draft registration and owner finalization

Every extension accepting editable clinical input registers that input with the shell's draft service before it can become dirty. `useClinicalDraft(slotId, schemaVersion)` is an illustrative SDK shape; identity also includes the module/draft key, principal, staff/patient workspace, patient/care context, originating generation, base draft version and serialization version. A slot name alone cannot uniquely identify every draft or confer authority.

Pass the minimum serializable state delta to the common service as input changes, including specialized interactions. Do not wait until a component unmounts or a long debounce expires to tell the shell what needs protection. The capture protocol must address pending IME/composition edits and in-progress gestures without fabricating a completed clinical action. Modules must not serialize functions, DOM objects or unnecessary patient-record snapshots into the recovery buffer.

The shell coordinates dirty state, save attempts, truthful acknowledgements, close/navigation warnings and [R127–R129 recovery](identity-session-authorization-design.md#browser-storage-and-fail-closed-presentation). The module's backend owner defines the draft schema, validation, versions and domain lifecycle. A shared draft service does not make Core the owner of all clinical content or select a universal clinical draft table. Pending responses retain their original context and version; an older acknowledgement cannot mark newer input Saved.

Patient workspace/tab closing must distinguish confirmed server drafts from unacknowledged input. Recovery follows same-principal fresh authorization, patient validation, version/conflict reconciliation, user-switch destruction and hard TTL. The regular online shell locks on detected authority loss; the buffer is not permission to continue editing offline. No module may add localStorage, sessionStorage, IndexedDB or another persistent clinical cache as a fallback.

A module may show a Finalize/Sign action using shared controls. Client-side validation guides the user, but only the backend owner can accept finalization. Finalization must target an explicit draft/content version and atomically recheck current authority, clinical invariants, concurrency and applicable validations in the business transaction. A prior validation response cannot authorize changed content or survive a later revocation without rechecking. Expose success only after authoritative acceptance; autosave or storing text is never clinical finalization. Domain-specific signature/attestation requirements remain Open.

## Semantic tokens, scoped styling and accessibility

Modules must use the UI-SDK's semantic design tokens for colors, typography, spacing, shadows and other shared visual roles, including specialized SVG/canvas rendering. `--zantrix-color-severity-critical` and `--zantrix-spacing-md` illustrate names, not finalized token inventory or brand values. Do not hardcode substitute palettes, fonts or shadows. Numerical chart coordinates and clinical measurements are domain geometry, not arbitrary design-token replacements.

CSS can consume custom properties directly. Canvas rendering and non-CSS rendering paths obtain resolved token values through the shared theme contract and repaint on relevant theme/contrast changes. Token use alone does not make a custom control accessible or ensure adequate contrast. Provide keyboard operation, meaningful names/structure, focus behavior and a usable accessible presentation of the same clinically relevant information/actions. Avoid color-only meaning; a single image label is insufficient for a complex interactive chart. Exact accessibility target and clinical visual semantics require review and testing.

Tokens do not scope selectors or prevent style leakage. Require scoped module styles, no unauthorized global resets/selectors and a controlled shell stylesheet/token surface. Exact CSS tooling, containment and any Shadow DOM choice remain Open; none is assumed. The style/extension contract must be checked across concurrently mounted modules. The later [visual brief](visual-direction.md) accepts a clear light-neutral foundation with compact density and low decoration. The final [approved reference](visual-direction.md#approved-reference-baseline) selects Teal #0F766E, selection #ECF8F5, canvas #F4F6F8 and Inter 4.1 with the tested features (R155–R156). Other theme/token values, alert-severity mappings and logo remain open; design approval does not select clinical alert meaning.

## Context-bound backend client

All module backend communication uses the shell-supplied UI-SDK client. Raw `fetch`, axios or other direct transport bypasses from module code are prohibited. Modules declare supported endpoint/contract capabilities; the client does not accept arbitrary external URLs or arbitrary privileged headers. External clinical integration runs through authorized backend/Connect contracts, not direct module-browser connections.

Bind each client operation to the slot's explicit immutable patient/workspace context, applicable version and operation contract. Inject the configured patient-workspace/expected-patient fields, CSRF protections and necessary request metadata centrally. Header names remain subject to the accepted request-contract design. Modules cannot replace principal identity, generations or context headers to impersonate another workspace. The server still independently validates all assertions, permission and owner entities; hidden buttons or injected headers do not establish authority.

Classify authentication failures and typed stale-workspace outcomes centrally, triggering the accepted lock/cleanup/recovery lifecycle. A generic HTTP 409 can be a normal clinical edit conflict and must not automatically trigger session revocation. Domain validation, authorization denial, version conflict, temporary failure and uncertain completion need distinct outcomes. Do not blindly retry a mutation after transport loss; use declared idempotency/request identity and server reconciliation. Cancellation and navigation alone do not prove a server operation did not commit.

Guard late responses and asynchronous callbacks against stale context/generation and unmounted slots. Coordinate query-cache identities and cleanup across patient, principal, workspace and policy changes. The client may carry typed requests to published contracts, but must not become an untyped arbitrary-action dispatch API.

These are enforceable module admission/conformance requirements, not a sandbox against hostile JavaScript in the same page. Dependency/source checks, linting, review and browser integration proofs must reject transport, DOM, style and storage bypasses. [CSP connection restrictions](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/connect-src) can limit destinations, but cannot distinguish an SDK request from a module's direct request to the same permitted origin. Backend authorization remains mandatory, including for admitted modules. The precise enforcement pipeline is Open; it has not been built or proven.

## Proof obligations and remaining decisions

Before implementation, specify versioned slot/context/disposal, router/overlay, draft serialization, theme and typed transport/error contracts. A later approved synthetic UI proof must exercise specialized interaction alongside a second patient tab, pending autosave, context switching, delayed/reordered responses, edit conflicts, authority loss, recovery and user switching. Include dirty-state capture, server-acknowledged saving and finalization of the exact validated content version.

Demonstrate that modules cannot accidentally cover the lock screen, retain interactive clinical overlays, leak CSS into siblings or expose an earlier patient's state after remount. Verify keyboard/screen-reader and supported theme/contrast behavior of custom visualizations, and inspect module/network/cache behavior for direct-transport or storage bypasses. Define visible module-load/render-failure handling and draft recovery; exact error-boundary behavior and whether a failed mandatory capability blocks its workflow remain Open. Do not promise isolation from a module that blocks the shared JavaScript thread.

No product/SDK runtime proof has run. R130–R134 accept the direction and mandatory contracts, not exact SDK signatures or demonstrated safety. The [primary/emergency operational boundary and emergency contract](emergency-read-design.md) are now accepted in R135–R142; exact dataset, security and operational mechanisms remain open. The [visual foundation](visual-direction.md) is now accepted in R143–R146; the final dated core-color/Inter 4.1 and ergonomic reference is approved in R155–R156, while remaining assets/tokens and production SDK proof remain pending. Product implementation remains unauthorized.
