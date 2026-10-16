# Decision register

Reconstructed baseline dated 15 October 2026. Status applies to the requirement or direction, not implemented software. Details, qualifications and open mechanisms in the linked documents remain authoritative.

## Historical identifiers and reconstruction identifiers

The original D01–D46 and P01–P04 register has now been recovered from untruncated file diffs in the original chat. See the [original register](original-decision-register.md) and [recovery provenance](conversation-reconciliation.md). D36's original same-turn recording rule is explicitly superseded by D38's approximately ten-answer cadence. D07 remains Proposed.

R001 onward are stable reconstruction identifiers introduced before the original register became accessible. Retain both identifier sets. The cross-reference below identifies related current entries; scoped documents preserve details where an original decision spans more than one subject. It does not replace the original wording or status.

Authorization, worklists and the separate test-environment requirement remain included under R-series IDs even though their earlier filesystem persistence was unconfirmed. R086 restores the explicit installation/setup and administrator-console requirement from the original conversation and governance text.

## Reconstructed register

| ID | Area | Status | Direction | Details |
| --- | --- | --- | --- | --- |
| R001 | Product | Agreed | Rebuild as a mature open-source modular EHR | [Details](product.md) |
| R002 | First setting | Agreed | General practice | [Details](product.md) |
| R003 | First country | Agreed | Netherlands | [Details](product.md) |
| R004 | Deployment goal | Agreed | Fully replace an existing HIS | [Details](product.md) |
| R005 | First engineering milestone | Agreed | One complete GP consultation | [Details](product.md) |
| R006 | International design | Agreed | No Dutch workflows hardcoded in Core | [Details](product.md) |
| R007 | Care settings | Agreed | Separate care-setting modules | [Details](product.md) |
| R008 | Locations | Agreed | Multiple locations from the foundation | [Details](product.md) |
| R009 | Combined care settings | Agreed | Multiple settings within one organization | [Details](product.md) |
| R010 | Patient identity | Agreed | One coherent organizational identity/record | [Details](product.md) |
| R011 | Tenancy | Proposed | One independent organization per installation remains a proposal | [Details](product.md) |
| R012 | Hosting | Agreed | Managed and self-hosted | [Details](product.md) |
| R013 | Self-host support | Agreed | No direct support; official bug reports allowed | [Details](product.md) |
| R014 | Official software | Agreed | Open source | [Details](governance.md) |
| R015 | Revenue | Agreed | Managed services may charge per module | [Details](product.md) |
| R016 | External licensing | Open | Deferred | [Details](governance.md) |
| R017 | Repository | Agreed | One monorepo | [Details](architecture.md) |
| R018 | Backend | Agreed | Java/Spring Boot | [Details](architecture.md) |
| R019 | Frontend | Agreed | React/TypeScript | [Details](architecture.md) |
| R020 | Database | Agreed | PostgreSQL | [Details](architecture.md) |
| R021 | Module stack | Agreed | Same supported stack for all modules | [Details](architecture.md) |
| R022 | Deployment architecture | Agreed | Modular monolith | [Details](architecture.md) |
| R023 | Core | Agreed | Technical foundation | [Details](architecture.md) |
| R024 | Bridge | Agreed | Mandatory internal interaction | [Details](architecture.md) |
| R025 | Connect | Agreed | External interoperability | [Details](architecture.md) |
| R026 | Patient registry | Agreed | Separate clinical module | [Details](architecture.md) |
| R027 | Contracts | Agreed | Typed/versioned commands, queries and events | [Details](module-contract.md) |
| R028 | Dependencies | Agreed | Explicit required/optional dependencies and compatibility | [Details](architecture.md) |
| R029 | Ownership | Agreed | One authoritative owner per datum | [Details](architecture.md) |
| R030 | Duplicates | Agreed | No independently editable duplicate clinical truth | [Details](architecture.md) |
| R031 | Standards | Agreed | Reuse suitable existing semantics | [Details](architecture.md) |
| R032 | UI | Agreed | One mandatory shared experience | [Details](clinical-safety.md) |
| R033 | Module administration | Agreed | Technical installation, functional configuration | [Details](governance.md) |
| R034 | Updates | Agreed | Planned maintenance acceptable | [Details](architecture.md) |
| R035 | Module ecosystem | Agreed | External extensions with strict official review | [Details](governance.md) |
| R036 | Local extensions | Agreed | Possible but explicitly outside reviewed combinations | [Details](governance.md) |
| R037 | Patient workspaces | Agreed | Multiple with a limit | [Details](clinical-safety.md) |
| R038 | Drafts | Agreed | Automatic preservation with truthful status | [Details](clinical-safety.md) |
| R039 | Finalized records | Agreed | Historical correction/addition | [Details](clinical-safety.md) |
| R040 | Concurrent editing | Agreed | Only across demonstrably safe scopes | [Details](clinical-safety.md) |
| R041 | Takeover | Agreed | Extra permission, reason and audit | [Details](clinical-safety.md) |
| R042 | Restriction scope | Agreed | Patient-level initially | [Details](clinical-safety.md) |
| R043 | Break-glass | Agreed | Justified, scoped emergency exception | [Details](authorization.md) |
| R044 | Incoming information | Agreed | Receipt distinct from clinical adoption | [Details](module-contract.md) |
| R045 | Emergency provision | Agreed | Local, read-only | [Details](continuity.md) |
| R046 | Emergency dataset | Agreed | Aim for full record | [Details](continuity.md) |
| R047 | Emergency freshness | Agreed | Five-minute target under healthy connectivity | [Details](continuity.md) |
| R048 | SOEP | Agreed | Supported outside Core | [Details](clinical-safety.md) |
| R049 | Portal | Agreed | Separate, later module plus external integration | [Details](product.md) |
| R050 | POH/POH-GGZ | Agreed | Eventual goal; first deployment scope unresolved | [Details](product.md) |
| R051 | Synthetic data | Agreed | Dedicated internal module | [Details](governance.md) |
| R052 | Developer testing | Agreed | Conformance facility | [Details](governance.md) |
| R053 | Project language | Agreed | English | [Details](documentation-standards.md) |
| R054 | Localization | Agreed | Extensible; language separate from country | [Details](documentation-standards.md) |
| R055 | Website | Agreed | Required, implementation open | [Details](product.md) |
| R056 | Development capacity | Agreed | One initial developer, no fixed deadline | [Details](governance.md) |
| R057 | Documentation cadence | Agreed | Approximately every ten answered questions | [Details](documentation-standards.md) |
| R058 | Identity | Agreed | Own accounts and institutional federation | [Details](identity-access.md) |
| R059 | On-site sign-in | Agreed | Password or suitable badge | [Details](identity-access.md) |
| R060 | Remote sign-in | Agreed | MFA | [Details](identity-access.md) |
| R061 | Badge policy | Agreed | First daily password plus occasional recheck | [Details](identity-access.md) |
| R062 | Roaming | Agreed | Personal work context follows staff | [Details](identity-access.md) |
| R063 | Active session | Agreed | At most one active workspace per user | [Details](identity-access.md) |
| R064 | Inactivity | Agreed | Privacy screen at five minutes, lock two minutes later | [Details](identity-access.md) |
| R065 | Privacy-screen dismissal | Agreed | Click allowed before actual lock | [Details](identity-access.md) |
| R066 | Sensitive administration | Agreed | Reauthentication | [Details](identity-access.md) |
| R067 | Profiles | Agreed | Standard and custom institution profiles | [Details](authorization.md) |
| R068 | Individual permissions | Agreed | Temporary by default; permanent with reason | [Details](authorization.md) |
| R069 | Multiple profiles | Agreed | Supported with scope | [Details](authorization.md) |
| R070 | Explicit deny | Agreed | Overrides allow | [Details](authorization.md) |
| R071 | Dual approval | Agreed | Optional, off by default | [Details](authorization.md) |
| R072 | Temporary staff | Agreed | Start/end dates and automatic expiry | [Details](authorization.md) |
| R073 | Revocation | Agreed | Online immediate; badge/account distinct | [Details](authorization.md) |
| R074 | Technical administration | Agreed | No automatic dossier access | [Details](authorization.md) |
| R075 | Support access | Agreed | Explicit consent, temporary, named, scoped, audited | [Details](authorization.md) |
| R076 | Audit | Agreed | Specifically authorized oversight | [Details](authorization.md) |
| R077 | Break-glass review | Provisional | Provisional automatic notification/review task | [Details](authorization.md) |
| R078 | Worklists | Agreed | Personal and shared | [Details](tasks-and-configuration.md) |
| R079 | Assignment | Agreed | Optional | [Details](tasks-and-configuration.md) |
| R080 | Task safety | Agreed | Prevent duplicate execution | [Details](tasks-and-configuration.md) |
| R081 | Escalations | Agreed | Configurable per workflow/list | [Details](tasks-and-configuration.md) |
| R082 | Automatic tasks | Agreed | Supported alongside manual tasks | [Details](tasks-and-configuration.md) |
| R083 | Configuration | Agreed | Broad administrator-configurable supported behavior | [Details](tasks-and-configuration.md) |
| R084 | Test environment | Agreed | Separate environment for Zantrix and institutions | [Details](tasks-and-configuration.md) |
| R085 | Test implementation | Open | Test topology and concrete mechanisms remain open; update trials and controlled configuration promotion are now agreed in R093–R094 | [Details](tasks-and-configuration.md) |
| R086 | Installation and administration | Agreed | Guided institution setup and an extensive administrator console; detailed design remains open | [Details](product.md#installation-and-administrator-console) |
| R087 | Ready-made module installation | Agreed | Technical administrators install module packages without recompiling Zantrix, with compatibility/dependency/migration checks and planned maintenance/restart where needed | [Details](module-lifecycle.md#ready-made-installation-packages) |
| R088 | Installation sources | Agreed | Official catalog and local package-file installation; visible source, version and review status; both retain applicable technical checks | [Details](module-lifecycle.md#ready-made-installation-packages) |
| R089 | Assisted module removal | Agreed | Preserve usable history when disabling/removing; explain and help resolve dependencies, pending work and data through supported migration/archive routes | [Details](module-lifecycle.md#disabling-and-removal) |
| R090 | Required exit route | Agreed | Every module holding lasting clinical data documents a usable archive or supported transfer route, checks and recovery | [Details](module-lifecycle.md#required-exit-route-and-official-review) |
| R091 | Lifecycle review | Agreed | Official catalog review includes installation, updates, transfer, archiving and removal for supported versions/combinations | [Details](module-lifecycle.md#required-exit-route-and-official-review) |
| R092 | Update authorization | Agreed | Authorized administrators schedule behavior/data-changing updates; managed hosting may act under prior institutional mandate; checking/downloading can be automatic | [Details](module-lifecycle.md#update-authorization-and-maintenance) |
| R093 | Update trials | Agreed | Institutions can test updates before production using intended module versions, relevant configuration, synthetic data and isolated endpoints; mandatory testing thresholds remain open | [Details](tasks-and-configuration.md#update-trials) |
| R094 | Configuration promotion | Agreed | Controlled, identifiable configuration change sets from test to production, with difference/dependency/effect preview and fresh production validation; no automatic transfer of test patients/accounts/connections | [Details](tasks-and-configuration.md#configuration-promotion) |
| R095 | Local documentation and publication | Agreed | Continue local documentation checkpoints; commit only on explicit request to commit and push only on explicit request to push; supersedes reuse of earlier publication authorization | [Details](documentation-standards.md#local-recording-commits-and-pushes) |
| R096 | Historical visibility | Agreed | Retained history remains available through the ordinary record; default visibility uses data-type-specific status/relevance/age rules, configurable within safety constraints; no universal age cutoff | [Details](module-lifecycle.md#historical-visibility-and-migration-approval) |
| R097 | Trial and production migration approval | Agreed | Review trial migration results and obtain explicit authorized administrator approval before production migration; surface uncertain transformations | [Details](module-lifecycle.md#historical-visibility-and-migration-approval) |
| R098 | Mixed migration and archive | Agreed | Allow mixed destinations with complete accounting and working references; unresolved meaning/completeness/accessibility failures block execution | [Details](module-lifecycle.md#historical-visibility-and-migration-approval) |
| R099 | Migration of configuration and unfinished work | Agreed | Reports include configuration, tasks, drafts and outgoing messages with explicit transfer, completion or further-action outcomes | [Details](module-lifecycle.md#historical-visibility-and-migration-approval) |
| R100 | Trial-to-production revalidation | Provisional | Prefer a fresh change check before production migration and renewed approval for material differences; detailed migration discussion is deferred | [Details](module-lifecycle.md#historical-visibility-and-migration-approval) |
| R101 | Unscheduled contacts | Agreed | Authorized staff can start a patient-linked contact without an appointment, retaining type, staff and time | [Details](clinical-safety.md#starting-preparing-and-completing-contacts) |
| R102 | Assistant preparation and handover | Provisional | Prepare and transfer within one coherent contact, retaining per-registration authorship and clear responsibility; detailed workflow is not finalized | [Details](clinical-safety.md#starting-preparing-and-completing-contacts) |
| R103 | Contact completion and follow-up | Agreed | A consultation/report can be completed while separately tracked follow-up remains open with visible responsibility and status | [Details](clinical-safety.md#starting-preparing-and-completing-contacts) |
| R104 | Current design sequence | Agreed | Prioritize the technical foundation and concrete module-platform options; defer migration/consultation detail and then address visual presentation/branding as a separate design topic | [Details](open-questions.md#current-design-focus) |
| R105 | Module runtime direction | Agreed | Select option A: separately installed prebuilt modules with a shared supported platform dependency baseline and controlled startup | [Details](module-platform-options.md#accepted-direction-controlled-startup-with-a-shared-platform-baseline) |
| R106 | SDK, BOM and release trains | Agreed | Publish an official SDK and zantrix-platform-bom per release train; host-provided shared frameworks are compile-time dependencies, excluded from module packages and validated against the deployment inventory | [Details](module-platform-options.md#sdk-bom-and-release-train-dependency-policy) |
| R107 | Additional module libraries | Agreed | Declare additional/internal library contents in the manifest and validate conflicts across the staged deployment set; shared-classpath libraries are not privately isolated | [Details](module-platform-options.md#sdk-bom-and-release-train-dependency-policy) |
| R108 | Backend bootstrap | Agreed | PropertiesLauncher/loader.path with strict preflight CLI/supervisor checking the resolved module graph and expected artifact versions/hashes before starting the application JVM | [Details](module-platform-options.md#selected-backend-bootstrap) |
| R109 | Frontend composition | Agreed | Native browser ES modules/import maps, controlled by the deployment, with canonical shared React/React DOM/UI SDK imports; no Webpack Module Federation | [Details](module-platform-options.md#frontend-requirement-shared-by-both-options) |
| R110 | Next architecture section | Agreed | Continue to Core/Bridge and data/transactions; this authorizes design review, not implementation or acceptance of newly proposed transaction mechanisms | [Details](core-bridge-data-design.md) |
| R111 | Core/Bridge and workflow ownership | Agreed | Keep Core technical and Bridge facilitating typed interaction; separate workflow owners coordinate, while data owners retain clinical invariants and authorization | [Details](core-bridge-data-design.md#responsibilities) |
| R112 | Data ownership and SQL boundaries | Agreed | One PostgreSQL database with owner schemas; no cross-owner JOINs or foreign keys; use stable public IDs and Bridge contracts with explicit reference lifecycles | [Details](core-bridge-data-design.md#accepted-persistence-ownership-direction) |
| R113 | Hybrid transactions | Agreed | Owner-local by default, explicitly contracted joint operations on one physical JDBC transaction/resource arrangement, and durable follow-up; no XA requirement for this local boundary | [Details](core-bridge-data-design.md#transaction-alternatives) |
| R114 | Transaction and persistence-context discipline | Agreed | Open joint operations at the outer boundary with REQUIRED participation; no independent clinical subtransactions or swallowing transaction-aborting failures; safe logging/context handling needs explicit design | [Details](core-bridge-data-design.md#rollback-and-persistence-context-discipline) |
| R115 | External I/O boundary | Agreed | No synchronous external service I/O inside joint database transactions; external communication goes through durable obligations and Connect after commit | [Details](core-bridge-data-design.md#transaction-alternatives) |
| R116 | Durable events and idempotent consumers | Agreed | Insert outbox obligations in the source transaction; implement retry/recovery and atomic consumer deduplication or a proven idempotent equivalent, scoped per consumer | [Details](core-bridge-data-design.md#accepted-durable-follow-up-direction) |
| R117 | Immutable execution/audit snapshot | Agreed | Durable envelopes preserve original actor/organization/patient/time/authorization provenance; workers identify actual execution and enforce applicable authority separately | [Details](core-bridge-data-design.md#immutable-executionaudit-context) |
| R118 | Schema declaration and validation | Agreed | Manifest declares schema namespace resolved to exact owned schemas; build/staging validate mappings/migrations/queries against ownership, with explicit static-analysis and in-process trust limits | [Details](core-bridge-data-design.md#accepted-persistence-ownership-direction) |
| R119 | Outbox storage layout | Proposed | Prefer a central technical outbox via a shared transaction-aware API; owner-schema tables remain an alternative, and one dispatcher does not require a central table | [Details](core-bridge-data-design.md#storage-alternatives-and-preferred-layout) |
| R120 | Identity-provider arrangement | Agreed | Institution-controlled Keycloak broker for local/federated staff identity, with a supported application OIDC boundary; exact versions and assurance integration remain open | [Details](identity-session-authorization-design.md#identity-provider-alternatives) |
| R121 | Browser authentication/session boundary | Agreed | Spring server-side OIDC code-flow/PKCE client and same-origin server-session cookie; keep provider tokens off browser storage and define cookie/CSRF/freshness controls | [Details](identity-session-authorization-design.md#browser-authenticationsession-boundary) |
| R122 | Online workspace and revocation authority | Agreed | Core-authoritative workspace/policy generations with a commit-ordering guard, stale-operation rejection and explicit roaming/recovery semantics | [Details](identity-session-authorization-design.md#authoritative-workspace-and-revocation-model) |
| R123 | Asynchronous execution authority | Agreed | Separate historical audit context from current scoped service/workflow authority; distinguish committed continuation, new discretionary action and external dispatch | [Details](identity-session-authorization-design.md#authority-for-asynchronous-work) |
| R124 | Per-principal transaction fencing | Agreed | Core-owned workspace/policy generation row, guarded FOR SHARE in the business transaction before owner writes; transfer/revocation updates serialize with it; ordering/expiry/retry integration still needs proof | [Details](identity-session-authorization-design.md#per-principal-transaction-guard) |
| R125 | Fresh unlock and step-up evidence | Agreed | One-use bound OIDC challenge, active authentication, required auth_time/nonce/assurance checks and explicit timestamp handling; an old SSO session or token issue time is insufficient | [Details](identity-session-authorization-design.md#roaming-locks-and-authentication-freshness) |
| R126 | Explicit patient-workspace context | Agreed | Every patient-bound call carries patient workspace and expected patient; validate staff-workspace membership, current access and owner entity context; no session-global current patient | [Details](identity-session-authorization-design.md#explicit-patient-context-for-every-clinical-operation) |
| R127 | No persistent clinical browser storage | Agreed | Regular online shell/modules keep clinical data/drafts out of persistent browser storage/caches; server acknowledgement defines durable draft saving | [Details](identity-session-authorization-design.md#browser-storage-and-fail-closed-presentation) |
| R128 | Fail-closed clinical presentation | Agreed | Lock/unmount clinical views and clear display/module caches on detected BFF authority loss or typed stale-session failure; combine push and client liveness checks with explicit detection limits | [Details](identity-session-authorization-design.md#browser-storage-and-fail-closed-presentation) |
| R129 | Protected volatile draft recovery | Agreed | Retain only unsaved delta in shell-private memory; same-principal fresh authorization and version reconciliation, destruction before another user's workspace, hard TTL with resume checks; exact duration open | [Details](identity-session-authorization-design.md#browser-storage-and-fail-closed-presentation) |
| R130 | Controlled module UI freedom | Agreed | Specialized React components within approved shell extension points, using mandatory shared SDK/building blocks and safety contracts; not restricted to generated/declarative forms | [Details](shared-ui-design.md#controlled-freedom) |
| R131 | Shell-owned workspace and presentation | Agreed | Shell owns patient header/selection, routing/title, overlays/focus and lock priority; modules stay within approved containers and use shell services | [Details](shared-ui-design.md#shell-owned-slots-navigation-and-overlays) |
| R132 | Registered clinical drafts and owner finalization | Agreed | Every editable module registers versioned context-bound draft deltas with shell; shell manages technical state/recovery, backend owner validates and finalizes exact content under current authority | [Details](shared-ui-design.md#mandatory-draft-registration-and-owner-finalization) |
| R133 | Semantic UI tokens and accessible custom views | Agreed | Shared semantic tokens also govern SVG/canvas; scoped styling and accessible interaction are mandatory separate obligations; brand values and exact tooling remain open | [Details](shared-ui-design.md#semantic-tokens-scoped-styling-and-accessibility) |
| R134 | Mandatory context-bound UI transport | Agreed | Module backend calls use shell SDK client with centrally bound context/CSRF and typed failures; no direct transport or arbitrary endpoints; server independently enforces authority | [Details](shared-ui-design.md#context-bound-backend-client) |
| R135 | Independent emergency read runtime | Agreed | Standalone local protected reader/storage with no clinical module JARs or React plugins and no live primary identity/data dependency; no clinical writes | [Details](emergency-read-design.md#fixed-boundary-and-independence) |
| R136 | Emergency contract production gate | Agreed | Every persistent patient/dossier-data owner needs a working versioned tested Emergency Read Contract before production activation, including local modules and relevant updates | [Details](emergency-read-design.md#mandatory-classification-and-production-gate) |
| R137 | Declared data classification and exemption evidence | Agreed | Mandatory clinicalDataClassification; patient-bound tables require a contract by default; NON_DOSSIER_TRANSIENT exemption requires evidence and cannot bypass other durable storage | [Details](emergency-read-design.md#mandatory-classification-and-production-gate) |
| R138 | Standardized emergency baseline rendering | Agreed | Typed versioned projections with human-readable baseline and explicit coverage/references/documents; reader executes no module-specific rendering code | [Details](emergency-read-design.md#versioned-projection-and-human-readable-baseline) |
| R139 | Outbox-triggered emergency projections | Agreed | Covered changes commit projection obligations in the same transaction; async scoped workers generate/retry/reconcile standalone revisioned projections; baseline and load proof required | [Details](emergency-read-design.md#durable-change-trigger-and-projection-generation) |
| R140 | Atomic durable packages and truthful consistency | Agreed | Validate/stage durable complete packages before atomic publication; reject regression/replay and expose cross-owner gaps; component atomicity alone does not establish a coherent dossier | [Details](emergency-read-design.md#atomic-installation-and-consistency-boundaries) |
| R141 | Verified system and patient-owner coverage | Agreed | Expected-owner registry plus verified global and per-patient/domain frontiers; distinguish empty from unknown/missing/stale and separate completeness, consistency and freshness | [Details](emergency-read-design.md#per-patient-coverage-and-freshness-watermarks) |
| R142 | Independent emergency audit reconciliation | Agreed | Local append-only hash-chained access audit, protected identity/storage and explicit integrity limits; authenticated idempotent recovery ingestion preserves source evidence; no automatic compliance claim | [Details](emergency-read-design.md#independent-audit-and-recovery-reconciliation) |
| R143 | Visual identity direction | Agreed | Clear and Accessible foundation with compact precision and low decoration; light neutral surfaces, readable typography and restrained professional accent | [Details](visual-direction.md#accepted-character) |
| R144 | Numeric typography and glyph clarity | Agreed | Tabular numerical presentation plus separately verified glyph discrimination for values/doses/times/identifiers; typeface subsequently selected in R156; no impossible-error guarantee | [Details](visual-direction.md#typography-and-numeric-clarity) |
| R145 | Compact readable interaction | Agreed | Dense tables/forms with modest rounding and preserved grouping/readability/focus/usable targets; exact sizes and density mechanisms remain open | [Details](visual-direction.md#density-and-shape) |
| R146 | Shared visual roles and later low-light palette | Agreed | Separate brand/selection/status roles through shared tokens, light foundation and later reviewed low-light palette; core colors/font subsequently approved in R155–R156; remaining logo/token values stay open | [Details](visual-direction.md#token-and-theme-implications) |
| R147 | Font-file feature and glyph proof | Agreed | Inspect actual font/subset features and test identifiers/dose specimens at 12–14 px with glyph alternatives; feature tags are font-specific and no error-free reading is claimed | [Details](visual-direction.md#first-comparative-visual-study) |
| R148 | Controlled palette comparison | Agreed | Compare Clinical Teal and Deep Indigo on identical compact synthetic patient-workspace layouts with header/tabs, numeric tables, draft state and warning/error/input examples; subsequent final color/typeface approval is recorded in R155–R156 | [Details](visual-direction.md#first-comparative-visual-study) |
| R149 | Brand accent selection | Agreed | Clinical Teal #0F766E is the selected brand accent, superseding the earlier Indigo carousel preview; selection/canvas and final font subsequently approved in R155–R156; other tokens/logo remain open | [Details](visual-direction.md#owner-review-and-revised-teal-study) |
| R150 | Patient-scoped visual feedback | Agreed | Coherent dirty indicators, explicit complete measurement rows, contextual warnings, neutral non-alert status/chrome and selective text selection; no static alert blocks on empty notes | [Details](visual-direction.md#owner-review-and-revised-teal-study) |
| R151 | Patient-tab presentation | Agreed | Full birth-date cues and separate close controls with explicit dirty-close choices; the owner accepted the tab treatment, while full-width empty-note layout is superseded by R153 | [Details](visual-direction.md#single-header-and-stable-workspace-refinement) |
| R152 | Single compact shell identity | Agreed | Remove repeated identity bars from module cards/actions; keep compact name/metadata/save status and adjacent alert badges in the sole shell header, retaining context visibility controls | [Details](visual-direction.md#single-header-and-stable-workspace-refinement) |
| R153 | Stable filled and empty workspace layout | Agreed | Keep note location/width stable across patients on desktop, with compact explicit empty states in the existing left lab/medication cards; responsive stacking applies consistently | [Details](visual-direction.md#single-header-and-stable-workspace-refinement) |
| R154 | Consistent Dutch numeric presentation | Agreed | Use decimal commas throughout Dutch study values and dose specimens, keep unflagged signal cells visually empty, and test dirty behavior on initially empty patients | [Details](visual-direction.md#single-header-and-stable-workspace-refinement) |
| R155 | Approved visual and ergonomic reference | Agreed | Final owner-approved Teal #0F766E, selection #ECF8F5 and canvas #F4F6F8 with the single-header/stable-grid reference, dated snapshot and preserved numeric/unit alignment | [Details](visual-direction.md#approved-reference-baseline) |
| R156 | Approved UI typeface and features | Agreed | Inter 4.1 full verified font file with cv05/cv08/zero and tabular figures; selected typography supersedes earlier open/candidate status, while production packaging and broader validation remain open | [Details](visual-direction.md#approved-reference-baseline) |

## Original-to-reconstructed cross-reference

Related entries retain their current qualifications. For example, D40 badge switching is detailed in the identity document linked by the broader R062 roaming entry. D27 availability/privacy/security is a foundation spanning multiple scoped documents.

| Original ID | Related current entries |
| --- | --- |
| D01 | R001 |
| D02 | R001, R014 |
| D03 | R002, R003, R004 |
| D04 | R005 |
| D05 | R006, R007, R054 |
| D06 | R008, R009 |
| D07 | R011 |
| D08 | R012, R013 |
| D09 | R014, R015, R016 |
| D10 | R017, R021, R022 |
| D11 | R018, R019, R020 |
| D12 | R023, R024, R025 |
| D13 | R026 |
| D14 | R027, R028, R029, R031 |
| D15 | R029, R030 |
| D16 | R033, R034 |
| D17 | R035, R036 |
| D18 | R032 |
| D19 | R037 |
| D20 | R038 |
| D21 | R040 |
| D22 | R041 |
| D23 | R010 |
| D24 | R039 |
| D25 | R042, R043 |
| D26 | R044 |
| D27 | [Continuity foundations](continuity.md) |
| D28 | R045, R046, R047 |
| D29 | R048 |
| D30 | R049 |
| D31 | R050 |
| D32 | R051, R052 |
| D33 | R053, R054 |
| D34 | R055 |
| D35 | R056 |
| D36 | R057 |
| D37 | R058 |
| D38 | R057 |
| D39 | R059, R060 |
| D40 | R062 |
| D41 | R062 |
| D42 | R063 |
| D43 | R064 |
| D44 | R065 |
| D45 | R061 |
| D46 | R066 |

P01–P04 remain proposals in the [original register](original-decision-register.md). Later R067–R085 cover the post-D46 answers; R086 restores setup/administration detail. No original D-number equivalence is claimed for those entries.

R087–R094 capture the subsequent module-lifecycle, update and configuration round. R095 captures the owner's later instruction to record locally and commit/push only on explicit request. The original register remains historical evidence; later accepted requirements govern current work.

R096–R104 capture the subsequent historical-visibility, migration and contact answers and the owner's return to foundational architecture. R100 and R102 remain provisional. R105–R110 capture the owner's acceptance and concrete refinement of the module-platform direction. R111–R118 record acceptance/refinement of Core/Bridge and data/transacting boundaries. R119 preserves the central-outbox storage preference as Proposed because alternatives were supplied without a final table-layout choice. No implementation is claimed.

R120–R123 are accepted architectural direction following explicit owner approval. R124–R128 capture the four supplied technical refinements with documented protocol qualifications. R129 records the subsequent explicit choice of a protected volatile recovery buffer, including same-principal recovery, user-switch destruction, version/conflict checks and a hard TTL. Exact buffer lifetime, deployment/protocol details and runtime evidence remain open. Existing badge timings and emergency/offline limits are unchanged; none of these requirements establish implementation or compliance.

R130–R134 capture the owner's explicit acceptance of controlled UI freedom and four mandatory boundaries: shell-owned presentation/navigation, registered drafts with owner finalization, semantic tokens/accessibility/scoped styles and context-bound backend transport. Exact hooks, slot schema, admission checks and browser proof remain open; branding has not been chosen.

R135–R142 record the owner's acceptance of the independent emergency reader and mandatory validated production contract, including classification/exemptions, standardized projections, outbox-triggered delivery, atomic publication, verified watermarks and hash-chained audit reconciliation. Per-component atomicity is not whole-dossier consistency; hash chaining is not proof of absolute audit completeness. Dataset/site scope, offline security, consistency/barrier protocol and operational targets remain open. No runtime or compliance proof has been performed.

R143–R146 record the owner's subsequent visual choice: Clear and Accessible with compact clinical information density and low decoration. At that direction-only checkpoint, typefaces and accent examples were unselected; subsequent selections are recorded in R149 and R155–R156; tabular digit width is distinct from glyph discrimination. The light foundation and later low-light palette follow shared semantic roles. This is a brief, not implemented UI or clinical-usability evidence.

R149–R150 record the owner's explicit Teal choice and first review requirements. The later owner review accepts the birth-date/close-tab treatment in R151; R152–R154 refine the sole shell header, stable empty-state layout and consistent Dutch numeric presentation. Earlier duplicated identity bars and full-width empty notes are superseded. Prototype fixes/tests are evidence about a synthetic design study only; the reported original host-specific stale-state/blank-cell cause is not established.

R155–R156 record the owner's final approval of the visual/ergonomic reference and Inter 4.1 feature profile on 16 October 2026. The three approved core colors, single-header/stable-grid layout and dated design snapshot are now the baseline. This closes the font/core-palette decision; logo, remaining tokens, low-light palette and production delivery/validation remain open. No product implementation, publication or compliance claim follows.

## Status vocabulary

- **Agreed:** explicitly accepted requirement or direction; details and validation may remain open.
- **Provisional:** expressed preference still requiring confirmation or validation.
- **Proposed:** recommendation not fully accepted.
- **Open:** a decision is required.
- **Implemented:** supported by current code and verification. No product functionality has this status in this baseline.
- **Superseded:** replaced by a later decision.

## Explicitly superseded assumptions

- Mandatory FHIR storage of all internal clinical data is superseded by module-owned domain models with standards-informed semantics.
- Any earlier reversed Bridge/Connect terminology is superseded: Bridge is internal; Connect is external.
- Old outpatient M1 functionality, setup instructions and maturity claims describe historical code, not the rebuild.
- Earlier broad publication authorization is superseded for future work by R095: documentation updates stay local until the owner explicitly requests a commit and/or push.
- Leaving the existence of configuration promotion undecided is superseded by R094. Its detailed transfer mechanism and test-environment topology remain open.
- The transactional outbox proposed in historical P02 is now an accepted mechanism under R116. Keep P02 unchanged as source history; exact storage layout and delivery implementation remain open.

## Open mechanisms accompanying agreed goals

Agreement on a goal does not select its mechanism. In particular, the full emergency dataset, identity assurance, badge policy bounds, offline session/revocation behavior, configuration safeguards, migration, release policy and test-environment topology need further design. See [all open questions](open-questions.md).
