# Suggested reconstruction checklist

Reusable reconstruction checklist preserved from the handover. Unchecked boxes below are template items, not the execution status of this reconstruction. See [current verification](recovery-verification.md) for the completed checks and limits.

[Documentation index](README.md)

## Before changing anything

- [ ] Confirm repository identity.
- [ ] Inspect current branch and remote HEAD.
- [ ] Inspect working-tree changes.
- [ ] Read current repository instructions.
- [ ] Check whether previous documentation exists.
- [ ] Check whether the recovery archive exists.
- [ ] Preserve any recovered local work.
- [ ] Do not assume the snapshot succeeded.

## Reconstruct the baseline

- [ ] Restore or recreate the documentation index.
- [ ] Restore the project vision and scope.
- [ ] Restore architecture and module contracts.
- [ ] Restore clinical safety requirements.
- [ ] Restore authentication and roaming requirements.
- [ ] Add all pending authorization decisions.
- [ ] Add all task/worklist/configuration decisions.
- [ ] Add the separate test-environment requirement.
- [ ] Preserve provisional/open status.
- [ ] Preserve English documentation.
- [ ] Preserve the existing license.
- [ ] Remove obsolete application claims if still present.

## Reconcile documentation

- [ ] Keep Core/Bridge/Connect terminology consistent.
- [ ] Keep full HIS replacement separate from the consultation milestone.
- [ ] Keep international requirements separate from Dutch GP behavior.
- [ ] Keep technical administration separate from clinical access.
- [ ] Keep task assignment separate from execution protection.
- [ ] Keep privacy screen separate from authenticated locking.
- [ ] Keep offline limitations explicit.
- [ ] Keep module conformance separate from clinical approval.
- [ ] Keep local writes separate from remote persistence.

## Verify and publish

- [ ] Check relative links.
- [ ] Check Markdown formatting.
- [ ] Run git diff --check when shell access is available.
- [ ] Review the actual diff.
- [ ] Commit the reconstructed documentation.
- [ ] Push without force.
- [ ] Verify the remote commit.
- [ ] Report branch, commit SHA and remaining gaps.

## Continue the discussion

Resume detailed design questions.
Do not treat this handover as:

- A finalized clinical specification.
- Approval for every implementation detail.
- A finished module SDK.
- An approved regulatory strategy.
- Authorization to silently choose all open items.

The latest accepted direction is a separate Zantrix test environment for both the project team and institutions, with implementation still to be determined.
