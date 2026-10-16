# Repository working instructions

## Current phase

This repository is a documentation-only rebuild. Read [HANDOVER.md](HANDOVER.md), the [documentation index](docs/README.md), [decisions](docs/decisions.md), [open questions](docs/open-questions.md) and [current recovery verification](docs/recovery-verification.md) before substantive work.

- Continue as an architecture and product-design collaborator. Do not start product implementation merely because detailed requirements are documented.
- Use the existing checkout. Do not create a worktree unless explicitly requested by the applicable workflow.
- Inspect branch, remote, working-tree changes and repository instructions before mutations. Preserve unknown work and the existing license. Earlier history was archived externally for the owner-authorized 16 October clean start; preserve the new history thereafter. See `docs/clean-repository-start.md`. Never force-push.
- Keep design documentation up to date locally. Commit only when the owner explicitly requests a commit; push only when the owner explicitly requests a push. Permission to record decisions is not permission to commit or publish them. Earlier removal/reconstruction publication authorization applied to completed recovery checkpoints and is not standing authorization for future commits or pushes. This later owner instruction takes precedence over historical handover text and skill defaults.
- Treat the old application as historical evidence. Do not silently restore its dependencies, startup instructions, FHIR-only storage assumptions or maturity claims.

## Decisions and communication

- Communicate with the owner in Dutch. Write repository documentation, code, identifiers and comments in English. Product interfaces must support localization.
- Ask one substantive main question at a time, with concrete alternatives and a recommendation when useful. Do not ask settled questions again.
- Capture answers in batches of approximately ten, or earlier for an explicit request, a handoff or an implementation dependency.
- Distinguish Agreed, Provisional, Proposed, Open, Implemented and Superseded. Never promote a recommendation or preference into an accepted detailed design.
- Preserve stable identifiers. The original D01–D46 and P01–P04 register has been recovered from recorded chat diffs; see `docs/original-decision-register.md` and the cross-reference in `docs/decisions.md`. Retain the R-series reconstruction IDs as well. Never reassign either set.
- Read `docs/conversation-reconciliation.md` before resuming questions. Do not repeat broad questions already discussed in the original chat; ask only for a concrete remaining decision or contradiction.
- Preserve pending authorization, worklist, configuration and separate test-environment requirements. Their mechanisms remain open.

## Architectural boundaries

- Core is the technical foundation; patient registration is a separate module.
- Bridge is mandatory internal interaction. Connect is external interoperability.
- Preserve one authoritative owner per datum and explicit typed, versioned contracts.
- Preserve one coherent shared UI and safe patient/session context.
- Separate international foundations, clinical capabilities, care settings, country requirements and language.
- Modules in one process are not sandboxed against hostile code.

## Evidence and sensitive information

- Documentation is not implementation. Historical tests are not tests of the rebuild. Conformance is not clinical approval or certification.
- Do not claim security, clinical readiness, standards compliance, test success or publication without supporting evidence.
- Use synthetic data. Never commit credentials, patient information or confidential records, including in examples and logs.
- No confidential security reporting channel has been verified; do not invent an address or claim private reporting is enabled.
- For documentation changes check relative links, heading anchors, formatting, status consistency, the actual diff and `git diff --check`. Product tests are not available in this baseline.
