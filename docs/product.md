# Product vision, scope and boundaries

Reconstructed from the owner-supplied handover dated 15 October 2026. This document records design requirements and their stated limits; it does not establish implemented functionality.

[Documentation index](README.md)

## Vision

Zantrix is intended to become:

- A fully open-source modular electronic health record.
- Clinically usable after the necessary validation, operational work and applicable regulatory obligations.
- Suitable over time for different countries and care settings.
- A strong example of meaningful interoperability between medical systems.
- Maintainable by an open-source community rather than dependent on undocumented knowledge.

The owner does not want a superficial demonstration with many nominal features that do not form coherent clinical workflows.

## Initial care setting

The first care setting is general practice.
The first country is the Netherlands.
The first deployable Dutch GP version is intended to fully replace an existing huisartsinformatiesysteem, or HIS.
It is not intended merely as a supplementary tool beside an existing HIS.
However, the first engineering milestone is smaller than a full replacement release.

## First engineering milestone

Build one complete GP consultation workflow:

1. Find and select the patient.
2. Open the relevant appointment or care contact.
3. Review relevant existing information.
4. Record findings.
5. Determine and document the plan.
6. Complete or finalize the consultation appropriately.

This milestone must exercise the architecture, not just demonstrate isolated forms.
It should include the relevant foundations for:

- Correct patient context.
- Draft preservation.
- Finalization and corrections.
- Authorization.
- Safe editing.
- Shared UI.
- Synthetic scenarios.

A complete consultation milestone does not establish:

- Full HIS replacement.
- Production readiness.
- Clinical deployment approval.
- Completion of all required integrations.

## Full HIS replacement

The eventual deployable scope must account for the actual practice's complete work.
Potentially necessary areas include:

- Clinical record workflows.
- Medication workflows and prescribing.
- Diagnostics and results.
- Correspondence and referrals.
- Scheduling.
- Practice administration.
- Required national integrations.
- Patient access.
- Migration from the previous HIS.
- Operational continuity.
- Security, privacy and clinical assurance.

The exact inventory must be established with the first practice. These examples are not a completed functional specification.

## POH and POH-GGZ

Support for practice nurses, chronic-care workflows and POH-GGZ is an eventual goal.
Whether these are required in the first deployable release is unresolved.
The first practice's actual workflows determine whether the absence of those capabilities would prevent replacement.

## International foundation

Do not embed Dutch requirements in the universal Core.
The architecture must distinguish:

- Shared technical foundations.
- Shared clinical capabilities.
- Care-setting workflows.
- Country-specific requirements.
- Language and localization.

National identifiers, exchange profiles, terminology requirements, connectors and applicable rules belong in appropriate country modules or adapters.

## Care-setting modules

General practice must be a separate care-setting module.
Other future examples include:

- Hospital care.
- Mental-health care.
- Other specialist or institutional settings.

Care-setting modules compose shared capabilities into suitable workflows and workspaces.
They must not duplicate the underlying patient identity or create competing authoritative records merely because they provide another interface.

## Multiple locations and care settings

One organization must be able to have:

- Multiple locations.
- Staff working across locations.
- Multiple care settings in the same installation.

Location-specific permissions and workflows are required.
The accepted patient-record direction is:

- One organization-wide patient identity.
- One coherent patient record across locations.
- Access governed by role, organization and care context.

## Independent organizations and tenancy

A proposal was made to begin with one independent organization per installation.
The owner questioned the value of sharing an installation between independent organizations, since excellent interoperability should allow them to cooperate without sharing a system.
The benefits and costs of shared tenancy were explained.
However, the one-organization-per-installation proposal was not explicitly finalized as a complete tenancy design.
Treat this as an unresolved architectural confirmation, not permission to implement arbitrary multi-tenancy.

The [original conversation review](conversation-reconciliation.md#organization-and-installation-boundaries) confirms this was already discussed. Continue with one independent organization per installation as the recorded working proposal (D07); do not ask the same broad tenancy question again. Revisit a specific organizational boundary only when a concrete workflow, data model or deployment decision requires it. The original register's Proposed status is preserved.

## Installation and administrator console

The owner explicitly requested an extensive installation/setup process and an even more extensive administrator console. Provide guided institution setup for organization, locations, care settings, country support, modules/dependencies, identity and connections, with readiness checks.

Administration should expose configuration, permissions, module versions, required/optional dependencies, update consequences, migrations, connection status, synchronization and failures. Technical installation/update authority remains separate from functional configuration. Detailed screens, roles and configuration mechanisms are still to be designed; this requirement does not make safety constraints freely disableable.

See [administrative configuration](tasks-and-configuration.md) and [module governance](governance.md).

## Hosting models

Support both:

## Zantrix-managed hosting

Zantrix can provide hosting and technical management.
The owner expects the possibility of charging per enabled module for hosting/services.
Exact support commitments, service levels and operational responsibilities are not specified yet.

## Self-hosting

Institutions can host and manage Zantrix themselves or through their own provider.
There is no direct support entitlement from Zantrix for self-hosted installations.
Self-hosters may still report bugs in official Zantrix modules.
Installation, updating, backup and restoration must be documented sufficiently for independent operation.
A mandatory dependency on a Zantrix-operated cloud control service has not been agreed.

## Patient portal

An official patient portal should be developed as a separate module.
It is:

- Not inherently part of the Netherlands country module.
- Not required for the first engineering milestone.
- Not committed as an immediate first-version deliverable.

External patient portals must also be able to integrate through documented and authorized interfaces.
Portal features, patient identity, proxy access, guardianship and national sign-in mechanisms remain to be designed.

## Public website

A separate public Zantrix website is required for:

- Product information.
- Promotion and project information.
- Documentation.
- Developer information.
- Possibly a future module catalog or marketplace.

Documentation should be applicable to specific releases.
No website technology, domain, hosting provider, visual design or publishing process has been selected.

## Migration

Migration tools may be developed later, but the foundation must be ready for migration.
The design must support preservation of:

- Source-system identifiers.
- Provenance.
- Original authors and timestamps where available.
- Documents.
- Terminology.
- Clinical history and corrections.

Import validation, identity matching, duplicate assessment and reconciliation require explicit design.
A validated migration path is a prerequisite before an existing practice can actually replace its HIS.
