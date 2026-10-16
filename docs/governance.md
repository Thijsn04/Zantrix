# Module ecosystem, testing and open-source governance

Reconstructed from the owner-supplied handover dated 15 October 2026. This document records design requirements and their stated limits; it does not establish implemented functionality.

[Documentation index](README.md)

## Development capacity

The owner is currently the only developer.
More contributors may join later.
The project has no fixed delivery deadline and will be worked on when time and motivation permit.
The project should nevertheless be exemplary in:

- Structure.
- Documentation.
- Versioning.
- Contribution guidance.
- Maintainability.

Security maintenance for deployed systems cannot be treated as optional hobby-time work.

## External modules

External developers should be able to build modules without modifying Core.
A marketplace may be added later.

## Official review

Externally developed modules offered through an official reviewed channel require strict review.
Review should cover:

- Security.
- Privacy.
- Clinical risk.
- Permissions.
- Dependency integrity.
- Compatibility.
- Migrations.
- Reliability.
- Failure behavior.
- UI consistency.
- Accessibility.
- Tests.
- Documentation.
- Maintenance ownership.

Official review also covers installation, updates, supported data transfer, archiving and removal for identified versions/combinations. Modules holding lasting clinical data require a documented exit route. The [module lifecycle requirements](module-lifecycle.md) describe the agreed scope; detailed review evidence and archive/migration mechanisms remain open.

Production activation also requires a validated versioned [Emergency Read Contract](emergency-read-design.md#mandatory-classification-and-production-gate) for every persistent patient/dossier-data owner, or a proven applicable transient exemption. Classification, typed fallback rendering, failure/coverage behavior and compatible reader evidence are required for the exact version/combination. This gate applies to local/unreviewed modules as well as official catalog modules.

Specific standards, review bodies and evidence requirements remain open.
Approval must be version-specific and combination-specific.
Do not claim that review guarantees absolute safety.

## Local extensions

Institutions may install local modules outside Zantrix review.
The interface must make the unreviewed state explicit.
Zantrix cannot claim those combinations are part of its reviewed configuration.
Warnings do not replace technical safeguards or determine all legal responsibility.

## Installation authority

Technical administrators install and update modules.
Functional administrators configure capabilities within the allowed configuration.
Dependencies and compatibility must be visible before changes.

The requested [guided installation and extensive administrator console](product.md#installation-and-administrator-console) cover initial institution setup and ongoing configuration/operational visibility. Exact interfaces and administrator roles remain open.

Technical administrators can install ready-made packages without recompiling Zantrix, through an official catalog or a local package file. Both routes retain compatibility, permission, dependency and migration controls and visible source/review status. A local installation does not imply mandatory contact with a Zantrix service. See [installation and retirement](module-lifecycle.md).

## Synthetic-data module

Build a separate module intended for internal development/testing that generates fictitious patients and coherent clinical data.
It should support reproducible scenarios rather than random unrelated fields.
Possible scenarios include:

- Consultations.
- Medication.
- Allergies.
- Results.
- Duplicate identities.
- Conflicting external information.

Use synthetic data only.
Block production use and prevent test messages from reaching real care systems.
Exact enforcement remains open.

## Developer conformance facility

Provide external module developers with:

- A development kit.
- Examples.
- Synthetic scenarios.
- Automated conformance checks.

Possible checks include:

- Manifest correctness.
- Missing dependencies.
- Incompatible versions.
- Contract adherence.
- Permission/context behavior.
- Migrations.
- Failures and retries.
- Shared UI integration.

Passing tests is not final approval or clinical certification.

## Licensing

The owner's intended baseline is:

- Official Zantrix software is open source.
- Managed hosting may charge per enabled module.

External closed-source modules and marketplace commissions remain unresolved.
The owner explicitly deferred that decision.
The old repository's existing AGPL v3 license was preserved unchanged during cleanup.
No alternative license was selected.
Before publishing module SDK/distribution terms, assess compatibility between the existing license and external-module policy.

## Updates and release policy

Requirements:

- Explicit module versions.
- Supported combinations.
- Release notes.
- Migration instructions.
- Deprecation policy.
- Security maintenance.
- Version-aware documentation.

Semantic versioning was proposed, but its exact use and release-support policy remain open.

Updates changing behavior or data require explicit maintenance scheduling by an authorized administrator, or by Zantrix within an institution's prior mandate for managed hosting. Checking/downloading can be automatic; installation follows the authorized maintenance process. Institutions must be able to trial updates in their test environment before production installation. Risk-based mandatory testing, urgent security-update policy and support commitments remain to be designed. See [update authorization and testing](module-lifecycle.md#update-authorization-and-maintenance).

## Shared interface review

A module must feel native to Zantrix.
Review must cover more than appearance:

- Context.
- Navigation.
- Actions.
- Errors.
- Draft behavior.
- Clinical consistency.
- Accessibility.
