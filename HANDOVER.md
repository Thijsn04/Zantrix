# New-chat handover and working instructions

Historical account supplied by the owner, reconstructed on 15 October 2026. Earlier cloud actions and test results below are reported history, not independently recovered evidence. The [current reconstruction verification](docs/recovery-verification.md) records what was actually checked in this session.

[Documentation index](docs/README.md)

Current provenance: the [16 October clean repository start](docs/clean-repository-start.md) archives previous Git history externally. Historical statements below about preserved ordinary Git history refer to that archived repository.

Current working-rule override: the owner subsequently requested local documentation updates, with commits and pushes only on explicit request for those actions. Follow [AGENTS.md](AGENTS.md) and the [current documentation rules](docs/documentation-standards.md#local-recording-commits-and-pushes). Historical publication authorization below does not authorize new commits or pushes.

## Purpose

This handover reconstructs the Zantrix design discussion and the work performed through 15 October 2026.
It contains:

- The project owner's objectives and accepted requirements.
- Architectural choices and the distinction between agreed and unresolved matters.
- Clinical workflow, identity, authorization, interoperability and continuity requirements.
- Repository changes previously performed.
- The cloud-environment failure and the status of uncommitted work.
- Instructions for continuing the project without losing decisions or falsely claiming persistence.

This handover is not proof that the current GitHub repository contains these documents.

## Project identity

- Project: Zantrix.
- Repository: Thijsn04/Zantrix.
- Originally selected branch: master.
- Previous local checkout: /workspace/Zantrix.
- Project owner communicates in Dutch.
- All repository documentation, code, identifiers, code comments and inline documentation must be in English.
- Product interfaces must support localization.

## Immediate instruction for a new assistant

Continue as an architecture and product-design collaborator.
The owner wants to rebuild Zantrix as a mature, open-source, modular EHR that can eventually be used in real clinical care.
Do not start implementing the application merely because this handover contains extensive requirements. Requirements discovery and detailed architecture design are still ongoing.
The owner has explicitly requested:

1. A full rebuild rather than continuing to patch the old application.
2. Preservation of all design decisions.
3. Continued questions to thoroughly understand the product.
4. Documentation updates in batches of approximately ten answered questions, rather than filesystem work after every answer.
5. All accumulated work to be written to the repository.
6. Commit and push of that work once repository access is available.

The permission to commit and push is already present. Do not request that same permission again merely because the session changed.
Before writing or pushing:

- Inspect the actual repository, branch, remote state and local changes.
- Preserve recovered work.
- Read any current AGENTS.md.
- Determine whether the previous documentation rebuild survived.
- Merge recovered files and this handover carefully.
- Do not reset, overwrite or delete unknown local changes.
- Do not assume master still points to the previously observed commit.
- Do not force-push.
- Respect any branch protections.
- Verify the actual pushed commit and report its SHA and branch.

## Status vocabulary

Use these distinctions consistently:

- Agreed: an explicitly accepted requirement or architectural direction.
- Provisional: an expressed preference that still needs confirmation or validation.
- Proposed: an assistant recommendation that has not been fully accepted.
- Open: a decision still required.
- Implemented: supported by current code and verification.
- Superseded: replaced by a later decision.

Most material in this handover is design, not implemented functionality.
Do not equate:

- Documentation with implementation.
- A passing build with a clinically usable product.
- Automated tests with certification.
- Local file writes with a GitHub push.
- A saved environment draft with publication or filesystem recovery.

## Interaction style

The owner wants a collaborative discussion with many useful questions over time.
Prefer:

- One substantive main question at a time.
- Concrete alternatives and a recommendation.
- Explanations when the owner asks why a decision matters.
- Explicit separation of technical mechanisms from product requirements.
- Periodic summaries and documentation checkpoints.

Avoid:

- Repeatedly asking already answered questions.
- Treating every assistant proposal as accepted.
- Prematurely selecting technologies or implementation details that remain open.
- Excessive tool work after each short answer.
- Building a generic framework before proving representative clinical workflows.

## Existing design process

The discussion used an architectural brainstorming approach:

1. Understand the intended clinical product.
2. Establish scope and constraints.
3. Compare approaches.
4. Review architectural sections.
5. Consolidate a written design.
6. Review that written design.
7. Produce scoped implementation plans.
8. Implement and verify incrementally.

The owner accepted a first Core–Bridge module-contract outline. Detailed API design, implementation plans and product implementation have not been completed.
The documentation-only repository reset was explicitly authorized.
