# Reconstruction verification — 15 October 2026

Historical report: references below to preserved Git history concern the previous repository. See the later [clean repository preparation](clean-repository-start.md) for current provenance and publication status.

This document records the initial handover-based checkpoint, published as `b333771c639ae9767efba4fa317fdd761b88ed60`. A later [review of the full original conversation](conversation-reconciliation.md) recovered the original decision register from recorded diffs. Statements below about unavailable original texts describe the initial inspection, before the owner supplied that chat link. They are not a claim that the original register remains unavailable.

## Source and observed state

The owner supplied a 15-document handover and explicitly authorized removal of the old application, reconstruction of documentation, commit and push. The [complete source text](reference/2026-10-15-owner-handover.txt) is preserved verbatim. This is a reconstruction from that source, not a claim of byte-for-byte recovery of the earlier cloud documents.

At inspection, the local checkout was on `master`, with remote `https://github.com/Thijsn04/Zantrix.git`. Fetch, remote HEAD inspection and GitHub repository/branch metadata independently showed default branch `master` at `1f09c73b1e781b522a45c3a99d79e172518a1462`. The branch was reported unprotected and the repository public. The tracked working tree was clean, with 365 tracked files. No untracked non-ignored work was found. The old application and its old documentation were still present; the earlier documentation-only rebuild was absent from this checkout and remote `master`.

No applicable AGENTS.md was found in the checkout or inspected ancestor directories. Ignored local content included `.vscode/settings.json`, backend build output and frontend dependencies/build output. These were included in the verified backup; the editor settings remain in place.

## Preservation

A local recovery directory outside the repository was created at:

`C:\Users\thijs\Documents\GitHub\.zantrix-recovery\2026-10-15-local-reconstruction`

It contains a complete checkout copy including `.git` and ignored files, a verified `history.bundle`, the owner handover source, `SHA256SUMS` and `reset-record.json`. All 22,251 non-Git working-tree files were compared with their backups using SHA-256 before removal. The bundle was verified with Git. The original LICENSE bytes are unchanged. The previous tracked state remains reachable through ordinary Git history; no history rewrite is part of this change. After tracked application files were removed, the remaining ignored backend/frontend output and dependencies were moved into `retained-generated-files` in that recovery directory, preserving the original local files as well as their verified copies.

This new local backup does not prove that the older cloud archive survived. It is not a published GitHub artifact. No services were started for this documentation task, and no external databases or Docker volumes were changed.

## Cloud recovery limit

The historical cloud environment ID and failures are preserved in [project history](project-history-and-recovery.md#cloud-failure). This session exposes no cloud-environment status, executor or snapshot-recovery capability. The earlier `/workspace/Zantrix` checkout and `/workspace/.zantrix-recovery/2026-10-15-before-rebuild` archive could not be inspected from this Windows checkout. Their survival or loss remains unknown. No cloud environment was reset, replaced or deleted.

If that environment becomes accessible, inspect it read-only first, preserve recovered content, then compare it against this baseline and the subsequently recovered conversation records. Do not replace either decision-identifier set or recovered user work blindly.

## Reconstruction scope

All 15 handover documents are represented by scoped Markdown documents or explicitly split into roadmap/open questions. The complete source is retained to prevent omission of nuance. The reconstruction includes the post-D46 authorization, worklist, configuration and separate test-environment requirements. Provisional break-glass review and proposed tenancy remain unconfirmed. Details not selected in the handover remain open.

The old tracked application, runtime configuration, automation workflows and obsolete documentation are removed. The license, history and existing code of conduct are preserved. New agent/contribution/security guidance describes the current design phase without restoring obsolete startup or clinical-readiness claims.

The original decision register and original foundation-design file were not recovered. Known historical IDs are reserved and documented; R-series IDs identify the reconstructed register. The restored foundation-design path contains a clearly labeled overview linked to the detailed documents.

## Validation and publication boundary

The reconstruction checks passed for 24 Markdown documents and 169 relative links, including heading-anchor checks. The complete source was compared byte-for-byte with the supplied handover. All 85 reconstructed register entries and all 44 original open questions are present. License, code of conduct and local editor settings match their backups. Legacy runtime directories/configuration are absent. Markdown heading/list formatting and `git diff --check` passed. The actual staged diff is reviewed before commit; publication verification follows afterward. The local verification receipt is stored alongside the backup. There is no replacement application test suite and no product code has been added.

This file is committed as part of the documentation baseline, before its own commit SHA exists. A successful local commit does not prove publication. After the ordinary non-force push, compare local HEAD with remote `refs/heads/master` and the GitHub commit/tree response. Store the resulting SHA and checks in the external recovery receipt and report them to the owner. The earlier no-push statement in the historical account applies to the failed cloud session, not the outcome of this new publication step.
