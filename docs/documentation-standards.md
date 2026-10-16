# Documentation standards and repository guidance

Reconstructed from the owner-supplied handover dated 15 October 2026. This document records design requirements and their stated limits; it does not establish implemented functionality.

[Documentation index](README.md)

## Documentation standards

Use English for:

- Source.
- Identifiers.
- Comments.
- Inline API documentation.
- Technical documents.
- Developer guidance.

Localize product interfaces.
Do not assume language and country are the same thing.
Country modules should add relevant translations, but external modules also need a mechanism to supply their own translations.

## Documentation structure

Maintain:

- A concise and truthful root README.
- A documentation index.
- Stable decision identifiers.
- Scoped design documents.
- Open questions.
- Implementation plans after relevant design review.
- Operator instructions once executable software exists.

Describe:

- Why.
- Responsibilities.
- Contracts.
- Permissions.
- Data ownership.
- Dependencies.
- Migration.
- Failure behavior.
- Verification.
- Limits.

## Decision capture

Batch documentation updates after approximately ten answered questions.
Persist earlier when:

- The owner explicitly asks.
- A handoff is needed.
- Implementation depends on the decision.

Do not treat unresolved preferences as final decisions.
Record superseded decisions explicitly.

## Local recording, commits and pushes

The owner explicitly requested that decisions continue to be recorded locally, while committing and pushing happen only when explicitly requested. A request to update documentation is not authorization to commit it. A request to commit does not automatically authorize a push. Earlier recovery/publication authorization applied to those completed checkpoints; do not reuse it for later changes.

Report local verification separately from a commit or verified remote publication. Keep this rule in repository instructions so a future session does not infer standing publication permission from the historical handover.

## Clinical and security claims

Never claim:

- Certification.
- Clinical readiness.
- Standards compliance.
- Security assurance.
- Test success.

unless actual evidence supports that claim.
Regulatory obligations depend on intended use and jurisdiction.
Do not repeat the old documentation's assumption that all legal or clinical responsibility necessarily belongs to the deploying institution.

## Sensitive data

Do not place patient data or credentials in:

- Issues.
- Documentation.
- Examples.
- Test fixtures.
- Screenshots.
- Logs.

Use synthetic data.
Separate public bug reporting from confidential security reporting.

## Security reporting

No confidential security contact was established in the discussion.
Do not invent an address or claim private reporting is enabled.
Establish a real confidential reporting channel before security-sensitive deployment or marketplace operation.

## Repository state warning

The rebuild was documentation-only.
Do not use old Git history to silently restore:

- Old architecture.
- Old dependencies.
- Old startup commands.
- Old maturity claims.
- Old country assumptions.

## Agent guidance

Use the existing checkout.
The environment is already isolated.
Do not create worktrees unless explicitly requested in the applicable workflow.
Preserve:

- Existing changes.
- License.
- History.
- Untracked user work.

Separate:

- User decisions.
- Assistant recommendations.
- Implemented functionality.

Do not introduce product code merely to make the repository appear complete.
