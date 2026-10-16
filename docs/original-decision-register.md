# Recovered original decision register

Recovered on 15 October 2026 from the original chat's complete recorded file diffs, applied chronologically with exact context checks. The original rows, identifiers and statuses below are preserved, including D36's supersession by D38. This is recovery of recorded text, not a live read of the failed cloud filesystem.

See [conversation reconciliation](conversation-reconciliation.md) for provenance and [the current register](decisions.md) for the R-series cross-reference and later requirements. Original D07 remains Proposed. Later support/worklist/configuration/test-environment answers are captured by the R-series; they were not assigned fabricated original D numbers.

Baseline date: 2026-10-15. Source: the project owner's rebuild discussion. Agreed means accepted direction, not deployed functionality. IDs are stable references for future designs and changes.

| ID | Status | Decision | Detail |
| --- | --- | --- | --- |
| D01 | Agreed | Rebuild the application; remove superseded working-tree implementation and docs while preserving recoverability. | [Foundation](superpowers/specs/2026-10-15-zantrix-foundation-design.md) |
| D02 | Agreed | Open-source modular EHR intended eventually for real clinical use across care settings. | [Product](product.md) |
| D03 | Agreed | First deployable target: Dutch general practice, fully replacing an existing HIS. | [Product](product.md) |
| D04 | Agreed | First engineering milestone: a complete GP consultation, not full HIS replacement. | [Roadmap](roadmap.md) |
| D05 | Agreed | International foundation; care-setting and country behavior live in modules. | [Architecture](architecture.md) |
| D06 | Agreed | Multiple locations and multiple care settings within one organization. | [Product](product.md) |
| D07 | Proposed | One independent organization per installation; separate organizations exchange data. Shared multi-organization hosting is not in the initial scope. | [Open questions](open-questions.md) |
| D08 | Agreed | Managed hosting and full self-hosting; no direct Zantrix support for self-hosters, but official-module bug reports remain welcome. | [Governance](governance.md) |
| D09 | Agreed | Official software open source; managed hosting may charge per module. External module licensing and marketplace commission remain open. | [Governance](governance.md) |
| D10 | Agreed | One monorepo; modular monolith; uniform backend and frontend stack for official and external modules. | [Architecture](architecture.md) |
| D11 | Agreed | Java/Spring Boot, React/TypeScript, PostgreSQL; exact versions and libraries remain open. | [Architecture](architecture.md) |
| D12 | Agreed | Core is technical; mandatory Bridge provides internal interaction; Connect handles external interoperability. | [Module contract](module-contract.md) |
| D13 | Agreed | Patient registration and clinical capabilities are modules, not Dutch-specific Core functionality. | [Architecture](architecture.md) |
| D14 | Agreed | Module-owned domain models, explicit shared semantics, typed/versioned commands, queries, events, manifests, and dependencies. | [Module contract](module-contract.md) |
| D15 | Agreed | No direct cross-module table writes or independently editable duplicate clinical truth. Necessary copies have explicit purpose and provenance. | [Module contract](module-contract.md) |
| D16 | Agreed | Technical administrators install/update modules; functional administrators configure allowed capabilities. Planned maintenance is acceptable. | [Governance](governance.md) |
| D17 | Agreed | External extension ecosystem with strict review; marketplace is a later possibility. Local unreviewed modules are possible with explicit status and responsibility. | [Governance](governance.md) |
| D18 | Agreed | One shared user experience and mandatory components, interaction rules, and extension points. | [Clinical safety](clinical-safety.md) |
| D19 | Agreed | Multiple open patient workspaces with a limit, explicit active identity, and isolated context. Exact limit remains open. | [Clinical safety](clinical-safety.md) |
| D20 | Agreed | Automatically save drafts; show actual save status; restore safely; do not finalize automatically. | [Clinical safety](clinical-safety.md) |
| D21 | Agreed | Concurrent viewing; concurrent editing only where proven safe; otherwise one editor for the relevant scope. | [Clinical safety](clinical-safety.md) |
| D22 | Agreed | Edit-lock takeover requires an extra permission, reason, audit, preserved draft, and rejection of stale writers. | [Clinical safety](clinical-safety.md) |
| D23 | Agreed | One organization-wide patient identity and coherent record across locations; controlled merge and correction. | [Clinical safety](clinical-safety.md) |
| D24 | Agreed | Preserve finalized clinical history; changes use attributable corrections/additions and domain-specific lifecycles. | [Clinical safety](clinical-safety.md) |
| D25 | Agreed | Contextual access and justified, reviewed break-glass access; restriction at patient level initially, not separately per note. | [Clinical safety](clinical-safety.md) |
| D26 | Agreed | External receipt and clinical adoption are distinct; preserve source and assess conflicts/corrections. | [Module contract](module-contract.md) |
| D27 | Agreed | Availability, privacy, and security are foundational; internet failure must not unnecessarily disable reachable local capabilities. | [Continuity](continuity.md) |
| D28 | Agreed | Local read-only emergency provision, including managed-hosting sites, aiming for a full record copy and five-minute lag under healthy conditions. | [Continuity](continuity.md) |
| D29 | Agreed | GP workflows combine contacts, episodes, and registrations; do not impose that structure universally. SOEP is supported outside Core. | [Clinical safety](clinical-safety.md) |
| D30 | Agreed | Own patient-portal module and external portal integration; portal is independent of the Netherlands pack and not required for the first milestone. | [Product](product.md) |
| D31 | Agreed | POH/chronic-care and POH-GGZ workflows are eventual goals; first deployable scope depends on the initial practice. | [Product](product.md) |
| D32 | Agreed | Internal synthetic-data module and a developer conformance facility; no real patient data or production messages. | [Governance](governance.md) |
| D33 | Agreed | English code and documentation; localization contributed through modules, including country modules. Language and jurisdiction are separate concerns. | [Documentation](documentation-standards.md) |
| D34 | Agreed | Public website for product information, release-aware docs, developer information, and possibly a later catalog. | [Product](product.md) |
| D35 | Agreed | One initial developer, potential future contributors, no fixed deadline; explicit docs, versioning, maintenance, and clinical review are required. | [Governance](governance.md) |
| D36 | Superseded by D38 for recording frequency | Continue requirements questions after the repository reset and preserve substantive answers. The original same-turn recording instruction is replaced by batched checkpoints. | [Documentation](documentation-standards.md) |
| D37 | Agreed | Support both Zantrix-managed staff accounts and federation with an institution's identity provider through standard protocols. Provider/products and exact protocols remain open; emergency-reader authentication requires separate design. | [Architecture](architecture.md) |
| D38 | Agreed | Batch documentation updates approximately every ten answered design questions instead of after every answer, with earlier checkpoints when requested or needed before handoff/implementation. Supersedes D36's recording frequency. | [Documentation](documentation-standards.md) |
| D39 | Agreed, requires security validation | Trusted on-site sign-in can use password or an appropriate badge without routine MFA; remote access requires MFA. Device/location trust criteria remain open. | [Identity](identity-access.md) |
| D40 | Agreed | Badge-based staff switching on shared workstations locks the previous session and preserves drafts without leaving its patient information visible. | [Identity](identity-access.md) |
| D41 | Agreed | Personal application workspaces roam between workstations, retaining open records, drafts, and clinical context. | [Identity](identity-access.md) |
| D42 | Agreed | At most one active staff workspace per user; takeover locks the old workstation and stale writers are rejected. Offline emergency partition behavior remains open. | [Identity](identity-access.md) |
| D43 | Agreed | Badge presentation explicitly locks; five minutes of inactivity hides patient information behind a two-minute countdown, followed by actual locking at seven minutes total. | [Identity](identity-access.md) |
| D44 | Agreed, requires security validation | A click dismisses the privacy screen during the countdown without authentication; after actual locking, authentication is required. The privacy screen is not an authenticated lock. | [Identity](identity-access.md) |
| D45 | Agreed, detailed bounds open | First badge sign-in of the day also requires a password; subsequent badge-only use is allowed with infrequent occasional password re-verification. | [Identity](identity-access.md) |
| D46 | Agreed | High-impact administration, such as permission grants and module installation, requires reauthentication; avoid adding these prompts to ordinary care work. | [Identity](identity-access.md) |
| P01 | Proposed | Spring Modulith/ArchUnit for architecture verification; exact adoption and package strategy require design. | [Architecture](architecture.md) |
| P02 | Proposed | Transactional outbox for durable asynchronous follow-up; transaction boundaries and delivery semantics require design. | [Module contract](module-contract.md) |
| P03 | Proposed | Load modules at controlled application startup, not through hot replacement of clinical code. | [Governance](governance.md) |
| P04 | Proposed | Evolve explicit semantic versions and a compatibility matrix; exact release/support policy remains open. | [Documentation](documentation-standards.md) |

The previous FHIR-only internal storage requirement is superseded. FHIR and openEHR remain relevant standards to assess, not an automatic storage mandate. The previous repository's legal disclaimer, maturity claims, and national workflow assumptions are not adopted as new design decisions; the existing license text remains in place.
