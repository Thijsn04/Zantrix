# Zantrix foundation design baseline

Reconstructed on 15 October 2026 from the owner-supplied handover. This summary restores the previously named design-document location; its original cloud contents were not recovered. It is not a finalized clinical specification, approved SDK or implementation plan.

## Scope and accepted direction

Build toward a mature open-source modular EHR, beginning with Dutch general practice. Full HIS replacement is the eventual deployment goal. The first engineering milestone is a complete consultation with correct patient context, drafts, finalization/correction, authorization, safe editing, a coherent UI and synthetic scenarios.

Use Java/Spring Boot, React/TypeScript and PostgreSQL in a monorepo and modular monolith. Exact versions remain open. Core supplies technical foundations, Bridge supplies internal contracts, Connect supplies external interoperability. Patient registration is a separate module. One organization may span locations and care settings while retaining coherent patient identity and records. Independent-organization tenancy still needs confirmation.

The owner approved the dated [visual/ergonomic reference](../../visual-direction.md#approved-reference-baseline) on 16 October 2026: Teal #0F766E, selection #ECF8F5, canvas #F4F6F8 and Inter 4.1 with verified glyph/numeric features. The single shell header and stable filled/empty workspace layout are the baseline. This is not product implementation or an approved complete SDK.

## Detailed source documents

- [Product scope](../../product.md)
- [Architecture](../../architecture.md)
- [Approved module-contract outline](../../module-contract.md)
- [Accepted module-platform direction](../../module-platform-options.md)
- [Accepted Core/Bridge and data/transaction direction](../../core-bridge-data-design.md)
- [Accepted identity/session/authorization direction](../../identity-session-authorization-design.md)
- [Accepted shared UI shell/module direction](../../shared-ui-design.md)
- [Accepted emergency read contract/reader direction](../../emergency-read-design.md)
- [Accepted visual direction](../../visual-direction.md)
- [Clinical record and editing safety](../../clinical-safety.md)
- [Identity, badges and roaming](../../identity-access.md)
- [Authorization, support and audit](../../authorization.md)
- [Tasks, configuration and test environment](../../tasks-and-configuration.md)
- [Continuity and emergency provision](../../continuity.md)
- [Module governance and testing](../../governance.md)
- [Module installation, updates and retirement](../../module-lifecycle.md)
- [Decision register](../../decisions.md)
- [Open questions](../../open-questions.md)

## Review gate

Continue detailed questions and review the written design before preparing scoped implementation plans. No product implementation is part of the recovery checkpoint. Separate test use, update trials and controlled configuration promotion are agreed. Topology, detailed management and transfer mechanisms remain open. Ready-made module packages, catalog/local-file installation, assisted retirement and explicitly scheduled updates are also agreed requirements; their concrete contracts remain to be designed.
