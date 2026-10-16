# Module installation, updates and retirement

Status: agreed requirements captured from the owner's follow-up answers on 15 October 2026. These are design requirements, not implemented capabilities or a finalized package/SDK specification. See R087–R094 in the [decision register](decisions.md).

## Ready-made installation packages

Technical administrators must be able to add a ready-made module package without modifying source or recompiling Zantrix. Before installation, check permissions, compatibility, required dependencies and data migrations and explain consequences. Installation can require a planned maintenance window and controlled restart. Exact package contents, backend/frontend loading, discovery, authenticity/integrity verification and compatibility algorithms remain open.

Support both an official catalog and installation from a local package file. Display source, module version and the version/combination-specific review status. The same applicable technical checks govern both routes. Local installation supports institutional extensions and self-hosting without a mandatory live connection to Zantrix; it does not turn an unreviewed combination into an approved one. Catalog connectivity, package-verification mechanisms and offline review-status freshness still need design.

## Emergency-read production condition

Every module declares its patient/dossier classification and supplies a working versioned tested [Emergency Read Contract](emergency-read-design.md) or a proven applicable exemption before production activation. Both catalog and local packages, and updates changing coverage/projection versions, pass the same gate. Stage compatible reader rendering and expected-owner enrollment/initial coverage; the exact activation handshake remains open. A later synchronization failure is a visible operational fault, not authorization for automatic clinical-module shutdown. Removal/updates preserve retained emergency readability and coverage declarations; this does not replace the ordinary archive/transfer exit route.

## Disabling and removal

Disabling a module stops its new actions and background processing while preserving existing data. Lasting clinical information must remain usable and readable. If dependencies or necessary record access cannot continue safely, block that particular operation and explain what must be resolved.

Actual module removal must remain possible. Zantrix must help administrators resolve blockers rather than only displaying an error. Before removal, show affected data, dependent modules, unfinished tasks and connections. Provide supported routes such as:

- Replacement: transfer data and responsibilities to another compatible module through a supported migration.
- Archiving: keep clinical history usable and readable without the original module's code.
- Dependency resolution: reconfigure capabilities or retire dependent modules in a controlled way.

A file export alone is insufficient if clinical history becomes unusable. Validate the selected route and recovery readiness before removing the module, then verify completeness, meaning, provenance and references. Detailed treatment of pending tasks, background messages, dependent workflows and migration/archiving failures remains open.

## Historical visibility and migration approval

Agreed: information retained after module retirement remains available through the ordinary patient record, with visibility rules per data type based on status, relevance and age. Administrators can configure supported visibility settings within defined safety constraints. Old or completed items need not remain in the default clinical overview; history must remain findable. No universal twenty-year visibility or deletion cutoff was selected. Hiding an item, transferring it and destroying it are different operations.

Production data migration requires a reviewed trial migration and explicit authorization from an appropriate administrator. The report shows what transfers, changes, fails or requires review because meaning is uncertain. A scheduled update alone does not authorize its data migration. A supported automated transformation is not permission for an unreviewed change of clinical meaning.

Mixed transfer and archiving is accepted: each datum must have a demonstrable destination, references must remain usable and no information may silently disappear. Unresolved failures threatening meaning, completeness or accessibility block execution.

The trial/report must also address configuration and unfinished work, including tasks, drafts and outgoing messages. State what transfers, what must first be completed and what requires additional action. Archiving historical clinical content is not an automatic resolution for unfinished work.

Provisional preference: recheck changes since the trial immediately before production execution; material changes require an updated report and renewed authorization. The owner expressed this tentatively and asked to defer further migration detail. The exact drift check and approval mechanism are not selected. Deeper migration design is postponed while the technical foundation is reviewed.

Removing executable module code does not authorize destruction of clinical history. No blanket data-deletion or retention policy has been selected. The archive capability and its access/authorization responsibilities still need design; do not assume that another module can interpret arbitrary stored data automatically.

## Required exit route and official review

Every module holding lasting clinical data must document a usable archive or supported transfer route, associated checks and recovery procedure. This prevents an institution from being trapped by an installed module. It does not require a migration from every module to every possible replacement.

Official catalog review must assess installation, ordinary operation, updates, supported transfers, archiving and removal for supported versions/combinations. Exit behavior needs evidence before admission, rather than being discovered when an institution tries to leave. Exact review standards, evidence formats and approval/withdrawal processes remain open. Conformance or review does not establish clinical certification.

## Update authorization and maintenance

Updates changing behavior or data require explicit scheduling by an authorized administrator. For managed hosting, an institution can grant Zantrix prior mandate to schedule/install within the agreed scope. Zantrix can automatically check for updates and optionally download them; installation follows the approved maintenance process. A downloaded package is not an installed update.

Security updates need visible urgency and follow-up. No emergency override, mandatory deadline or automatic forced-install policy has been agreed; those responsibilities and the precise mandate remain open. Existing [maintenance requirements](continuity.md#planned-maintenance) still apply, including recovery readiness, emergency access and post-migration checks. A previous binary is not automatically a safe database rollback.

## Test environments and configuration changes

Institutions must be able to trial module updates using the intended module versions and relevant configuration before production installation. Use synthetic data and isolate real care endpoints. Whether particular changes require testing is risk-based and still to be specified. This requirement does not select a shared test service, per-institution clone or production-data copying.

Support [controlled configuration promotion](tasks-and-configuration.md#configuration-promotion) as an identifiable change set with a preview of differences, dependencies and effects. Validate it against the current production environment again. Promote configuration only; test patients, accounts and connections do not automatically transfer. Exact formats, approvals, environment-specific reference/secret handling, conflict detection and recovery mechanisms remain open.

## Scope and next design work

The agreed requirements constrain the future Core/Bridge, module and shared-interface contracts. The subsequent [module-platform review](module-platform-options.md) selected PropertiesLauncher/preflight, a release-train SDK/BOM and native browser import maps. Exact build tooling, archive engine, signature/trust scheme and deployment topology remain open. Resolve concrete package/lifecycle contracts and failure behavior before implementation. Keep status and supported combinations visible to administrators throughout the lifecycle.
