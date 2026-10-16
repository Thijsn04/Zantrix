# Work actually performed and recovery status

Historical account supplied by the owner, reconstructed on 15 October 2026. Earlier cloud actions and test results below are reported history, not independently recovered evidence. The [current reconstruction verification](recovery-verification.md) records what was actually checked in this session.

[Documentation index](README.md)

## Original repository inspection

The original application used:

- Java 21.
- Spring Boot.
- React/TypeScript.
- Vite.
- PostgreSQL.
- Keycloak.
- HAPI FHIR.
- Snowstorm.
- Elasticsearch.
- Docker Compose.

The old code and documentation described an M1 outpatient system, but the owner considered it confusing, incomplete and poorly functioning.

## Onboarding work performed

The assistant:

- Inspected repository instructions, manifests and CI.
- Installed frontend dependencies using the lockfile.
- Configured writable Maven caches.
- Configured Maven proxy access.
- Installed a JDK 21 after discovering the available Java installation did not contain the expected compiler.
- Verified the downloaded JDK against its official SHA-256 checksum.
- Used the system Java truststore for the environment's supported TLS trust.
- Corrected local file permissions needed by database/realm bind mounts.
- Started PostgreSQL, Keycloak and HAPI FHIR.
- Started the native backend and Vite frontend.

## Old-application verification results

At that time:

- Frontend lint passed.
- Frontend tests: 70 passed.
- Frontend build passed.
- Generated frontend API types matched the backend contract.
- Backend unit tests: 50 passed.
- Backend integration tests: 7 passed.
- Backend health reported UP.
- FHIR metadata returned an R4 CapabilityStatement.
- Synthetic physician authentication through Keycloak worked.
- Authenticated patient-list access returned HTTP 200.
- Unauthenticated patient-list access returned HTTP 401.
- The reusable installation script was exercised again successfully.

These results apply only to the removed old application.
They do not validate the rebuild.

## Old-application limitations observed

- Full terminology stack startup was blocked by access to docker.elastic.co.
- Playwright Chromium download was blocked by access to cdn.playwright.dev.
- Browser E2E was not validated.
- Production dependency audit reported React Router findings: one high and one moderate vulnerability.
- The old dependency declarations and lockfile were not modified during onboarding.

## Initial technical assessment

A sample review identified:

- Existing infrastructure choices were not inherently unsuitable.
- Tests did not prove clinical usability.
- The old consultation-note UI cleared its form when starting the save request, before confirming success.
- FHIR-only internal storage deserved reconsideration.
- Separate HAPI and application audit stores introduced consistency/failure-boundary concerns.
- Several real-world clinical and external workflows were missing.

The owner subsequently selected a rebuild.

## Last observed Git identity

Before cleanup, the local branch was reported as work.
The observed HEAD and remote master reference were:
1f09c73b1e781b522a45c3a99d79e172518a1462
Do not assume this is still the current remote state.

## Cleanup authorization

The owner explicitly allowed removal of the existing application and documentation, stating that the old documents were no longer authoritative.
The owner later explicitly requested:

- Empty/clean the repository.
- Document all decisions.
- Continue asking questions afterward.
- Eventually commit and push everything.

## Preservation before cleanup

Before removing content:

- The checkout was inspected and reported clean.
- No additional project AGENTS instructions were found in the inspected paths.
- Legacy services started during onboarding were stopped.
- Docker data volumes were retained.
- A complete checkout archive was created outside the repository.
- An independent Git history bundle was created.
- The Git bundle was verified.
- The archive was compared with the checkout.
- SHA-256 checks were recorded and verified.

Recovery directory:
/workspace/.zantrix-recovery/2026-10-15-before-rebuild
Recorded contents included:

- checkout.tar
- history.bundle
- SHA256SUMS
- reset-record.json
- Copies of old local onboarding helper scripts.

The backup was local, not a published remote artifact.

## Cleanup executed

The assistant removed the old working-tree content while preserving:

- .git
- Existing LICENSE, unchanged.

This removed:

- Old backend.
- Old frontend.
- Old Docker setup.
- Old workflows.
- Old documentation.
- Generated runtime/build content inside the checkout.

Git history was not rewritten.

## New files written

The reconstructed documentation baseline included:

- README.md
- .gitignore
- AGENTS.md
- CONTRIBUTING.md
- SECURITY.md
- docs/README.md
- docs/decisions.md
- docs/product.md
- docs/architecture.md
- docs/module-contract.md
- docs/clinical-safety.md
- docs/continuity.md
- docs/governance.md
- docs/documentation-standards.md
- docs/roadmap.md
- docs/open-questions.md
- docs/identity-access.md
- docs/superpowers/specs/2026-10-15-zantrix-foundation-design.md

LICENSE was preserved rather than rewritten.

## Validation of new documents

The assistant checked:

- Relative Markdown links.
- Code-fence balance.
- Basic list formatting during the initial baseline.
- License unchanged.
- Git metadata retained.
- Old runtime directories removed.
- git diff --check.

After the authentication batch, 95 relative documentation links were reported valid.
No new application test suite existed because the repository was documentation-only.

## Decision IDs last verified in files

The last confirmed local decision register reached D46.
Important later entries included:

- D37: local and federated staff identity.
- D38: documentation updates in batches of approximately ten answers.
- D39–D46: local/remote authentication, badge switching, roaming workspace,

  single active session, inactivity screen, badge verification and sensitive
  administrative reauthentication.
The authorization/worklist/test-environment decisions after D46 were not confirmed written to files.
Inspect current files before allocating additional IDs.

## Commit and push status

No commit or push was performed before the environment failed.
The owner subsequently explicitly authorized commit and push.
A GitHub plugin mention did not expose callable GitHub tools in that session.
Do not infer remote persistence from the user's plugin mention.

## Cloud failure

Environment ID:
ccarenv_b64_Y2NhcmVudl9mNTczNTVmNGI5MDQ4MTkxYTI4OTQwMzU5MDZiNzczZg
Reported failures included:

- environment_startup_failed
- connectivity: offline
- observed_phase: failed
- exec-server connection attempt failed
- The snapshot outcome is unknown and the executor may already be consumed

The failed snapshot status means:

- The local documentation may or may not have survived.
- The local backup may or may not have survived.
- Their loss is not established.
- Their persistence is not established.

## Environment configuration

The old installation script was cleared in the editable environment draft.
Documentation-only startup instructions were successfully saved before the later failure.
Those instructions said not to restore/start the removed application.
Earlier saved network additions included:

- docker.elastic.co
- cdn.playwright.dev
- storage.googleapis.com
- playwright.download.prss.microsoft.com
- rxnav.nlm.nih.gov

Saving draft configuration did not establish publication or runtime application.
A later attempt to append pending decisions to the startup instructions failed with:
draft_not_editable
The tool indicated publication might have occurred or might still be in progress.
No successful save of that handoff was confirmed.

## Recovery priority

1. Inspect the existing environment/snapshot if possible.
2. Preserve any recovered working tree and backup.
3. Inspect current GitHub contents.
4. Reconstruct missing documentation from this handover and the conversation.
5. Do not claim exact recovery without verification.
6. Commit and push the completed documentation under the user's authorization.
7. Verify remote branch/commit and report them.

Do not blindly delete or replace the old environment before recovery is assessed.
