# Open questions

These detailed decisions remain open. Their presence is not authorization to select technologies or start product implementation. Questions 1–44 preserve the handover order, with later answers narrowing the remaining questions. New lifecycle questions continue the numbering.

1. Confirm the independent-organization tenancy model.
2. Exact supported platform versions.
3. JDK distribution.
4. Build tools and package managers.
5. Exact module registration/discovery, package layout and supervisor implementation, within the accepted PropertiesLauncher/preflight, release-train BOM and no-recompilation direction (R105–R108).
6. Core/Bridge concrete contracts.
7. Exact shared UI-SDK slot/context/disposal, routing/overlay, registered-draft, token/theme, accessibility and typed client/error contracts plus admission checks (R130–R134); ESM output/subpaths, browser/CSP policy and deployment transitions within native import-map composition (R109).
8. Compatibility algorithm.
9. Persistence libraries.
10. Exact manifest/schema ownership validation and migration/query enforcement, within the accepted no-cross-owner-JOIN/foreign-key rule; address dynamic SQL and shared-credential limits (R112/R118).
11. Concrete Bridge transaction declarations, Spring resource/context registration, lock ordering/timeouts, safe rollback/retry and bounded error-audit path for accepted joint transactions (R113–R115).
12. Outbox storage layout (central technical preference versus owner tables), implementation, dispatcher/claim/recovery and per-subscriber delivery records; the same-transaction outbox mechanism itself is accepted (R116/R119).
13. Ordering, retries and deduplication.
14. Clinical modeling standards.
15. Terminology and profiles.
16. Specify versions, assurance/claim profiles, operational ownership and failure handling for the accepted Keycloak broker/application OIDC arrangement (R120–R121); prove fresh unlock/step-up and timestamp handling (R125).
17. Account provisioning/linking and safe issuer/subject lifecycle, including reliable deprovisioning and reconciling institutional claims with Core grants.
18. Trusted-device/location classification.
19. Badge/reader security.
20. Daily and occasional reauthentication bounds.
21. Specify and prove the accepted per-principal guard integration: Core-before-owner lock ordering, isolation/retries, policy-wide changes, expiry and cache consistency; coordinate inactivity and transfer (R122/R124).
22. Offline enforcement of access revocation.
23. Lock granularity and expiry.
24. Patient-workspace numerical limit.
25. Server-side draft protocol and protected volatile-buffer implementation: exact hard TTL, minimal delta/base data, acknowledgement-loss reconciliation, user-switch destruction and authorized version/conflict recovery after roaming (R127–R129). The buffer choice is accepted.
26. Exact emergency dataset/site scope, format/versioned fallback schemas, expected-owner enrollment and multi-owner consistent cutoff/barrier protocol within the accepted mandatory Emergency Read Contract (R135–R141).
27. Independent emergency-reader identity/authorization, expiry/revocation bounds, keys, safe renderer/storage and hash-chain anchoring, audit-failure behavior and recovery-ingestion protocol (R135/R138/R142).
28. Recovery and backup targets.
29. First-practice complete workflow inventory.
30. Clinical validation participants and process.
31. Applicable regulatory responsibilities.
32. Import/export/migration.
33. Translation packaging.
34. External-module licensing.
35. Release/support lifecycle, urgent security-update policy and the precise managed-hosting maintenance mandate, within the agreed explicit update scheduling rule (R092).
36. Security reporting channel.
37. Portal features and representation.
38. Website stack and publishing.
39. Synthetic-data environment enforcement.
40. Configuration and worklist rule mechanisms.
41. Test-environment topology and management.
42. Exact change-set format, approval/application workflow, environment-specific reference/secret handling, drift/conflicts and recovery for agreed controlled configuration promotion from test to production (R094).
43. Break-glass review details.
44. Task execution guards and handover behavior.
45. Package integrity/authenticity, catalog connectivity, offline verification/review-status freshness and equivalent checks across catalog/local installation.
46. Disable/archive/removal contracts, migration and dependency resolution, unfinished task/message handling, archive readability/access, failure recovery and evidence for required exit routes.
47. Risk classification and mandatory test criteria for specific updates; representative configuration and isolated endpoint setup.
48. Specify per-operation service/workflow mandates, delegation/cancellation and current restriction checks within the accepted asynchronous-authority categories and immutable historical context (R123).

These are open decisions, not missing implementation details to invent silently.

## Resumption point

The latest accepted architecture choices include R111–R118 in the [decision register](decisions.md): technical Core/Bridge versus workflow/data ownership, owner schemas and strict SQL boundaries, hybrid transactions, rollback/I/O discipline, same-transaction outbox and idempotent consumers, immutable original context and validation. R119 central outbox storage remains Proposed; R100 fresh migration checks and R102 assistant handover remain provisional. Continue with one substantive question at a time and record approximately every ten answers, or earlier when a design depends on them. Commit/push only on explicit request for those actions (R095).

The [identity, workspace sessions and authorization direction](identity-session-authorization-design.md) is accepted in R120–R129, including the owner's protected volatile-buffer choice. Exact profiles, buffer lifetime and runtime proof remain open. The [shared UI shell/module boundaries](shared-ui-design.md) are also accepted in R130–R134. The [independent emergency reader and mandatory production contract](emergency-read-design.md) are accepted in R135–R142. Exact dataset/site scope, offline authority, consistency protocol and operational targets remain open. Do not repeat settled reader independence, production-contract requirement, UI freedom, provider, buffer, badge-timing, MFA-location or function-profile choices.

The [full original conversation review](conversation-reconciliation.md) supersedes the earlier suggestion to repeat the broad independent-organization tenancy question. That topic was already discussed: one independent organization per installation is the recorded working proposal, with separate organizations exchanging data. Question 1 remains a specific confirmation gate before fixing organizational schemas, not an instruction to repeat the earlier discovery discussion. Ask only if a concrete boundary or contradiction needs resolution.

Installation/setup and administrator-console design are also required, with detailed screens, roles and configuration mechanisms still open. POH/POH-GGZ scope, first-practice requirements, operational commitments and detailed security policies remain unresolved in their [scoped documents](README.md). Resume with a concrete unanswered design question after reviewing the recovered decisions.

## Current design focus

The owner asked to stop detailed migration/consultation discovery and first work through the technical foundation. Review these areas in order, preserving existing accepted product requirements:

1. Module packages/runtime, compatibility and shared frontend extension loading.
2. Module-owned persistence, cross-module operations, transaction boundaries and durable follow-up.
3. Identity, authorization/context enforcement and the single active workspace.
4. Shared UI shell, patient workspace and platform/module responsibilities.
5. Primary/emergency operational boundaries and continuity assumptions.

The [module-platform direction](module-platform-options.md) and [Core/Bridge and data/transaction direction](core-bridge-data-design.md) have both been accepted at architectural level; exact implementation/proof and outbox table layout remain pending. The [identity/session/context section](identity-session-authorization-design.md) is also accepted, including authority for asynchronous work, patient isolation and protected volatile recovery. The [shared UI shell/platform/module direction](shared-ui-design.md) is accepted, with specialized React components in shell slots and mandatory presentation, draft, token/accessibility and transport contracts. The [independent emergency reader direction](emergency-read-design.md) is accepted, including mandatory production contracts, verified coverage and independent audit reconciliation. The five foundation areas now have accepted architectural directions; detailed emergency/UI/transaction/authentication protocols, operational targets and buffer TTL still require design/proof rather than repeated high-level approval. The separate [visual direction](visual-direction.md) is now accepted (R143–R146): Clear and Accessible with compact precision and low decoration. The final visual/ergonomic reference is approved in R155–R156: Clinical Teal #0F766E, selection #ECF8F5, canvas #F4F6F8 and Inter 4.1 with the tested glyph/numeric features. Logo, dimensions beyond the reference, remaining token values, production font delivery/fallback and later low-light palette remain open. The tab/close treatment is accepted in R151, with a sole compact shell header, stable empty-state layout and Dutch numeric presentation refined by R152–R154. Verify exact responsive/interaction contracts and any host-specific rendering/input issues against actual evidence. Website/portal styling has not been selected. Use the approved dated reference for later component/interaction specifications; do not repeat the settled core palette, typography or layout choice.

Migration implementation details, numeric history visibility thresholds and exact assistant/contact handover mechanics are intentionally deferred. Known requirements remain preserved rather than being discarded. No product implementation, dependency installation, commit or push is authorized by this design discussion.
