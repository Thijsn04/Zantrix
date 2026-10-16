# Clinical record, workspace and editing safety

Reconstructed from the owner-supplied handover dated 15 October 2026. This document records design requirements and their stated limits; it does not establish implemented functionality.

[Documentation index](README.md)

## Organization-wide record

A patient has one organizational identity and coherent record across locations.
Do not create separate competing records simply because the patient is seen at different sites or in different care settings.

## Identity management

Design for:

- Duplicate detection.
- Identity matching.
- External identifiers.
- Merge.
- Recovery from an incorrect merge.
- Source-system provenance.

A matching identifier or demographic field must not automatically establish that two records belong to the same person.

## Contacts, episodes and registrations

For GP care, combine:

- A timeline.
- Care contacts.
- Episodes or longitudinal problems.
- Individual clinical registrations.

One contact can address multiple problems.
An episode can span multiple contacts.
These are relationships and views over clinical facts, not instructions to duplicate the same note in multiple places.
Information must be able to await classification rather than requiring a fabricated diagnosis or episode.
Do not impose this exact GP workflow on every future institution.

## Starting, preparing and completing contacts

Agreed: an authorized employee can start a care contact without a planned appointment, for example a telephone contact, walk-in or home visit. Retain contact type, involved staff, time and correct patient association. An appointment can supply context but is not a prerequisite.

Provisional direction: an assistant may prepare the contact and hand it over to a GP within the same coherent contact, retaining authorship per registration and a clear transfer of responsibility. The owner's answer was tentative; exact workflow and acceptance mechanics need later confirmation.

Agreed: the consultation can be completed and its report finalized while follow-up remains open. Follow-up actions are tracked independently in worklists with visible responsibility and handling status. Finishing the consultation must not imply that requested investigations or other follow-up are complete. Domain-specific completion and signing rules remain to be designed.

These requirements are captured to preserve answers already given. Further consultation-detail discovery is deferred while the technical foundation is reviewed.

## SOEP

Support SOEP for appropriate GP documentation.
Do not put SOEP in universal Core.
Other countries and care settings must be able to use different documentation structures.
SOEP is not exclusively a Dutch concept; distinguish actual national requirements from broader clinical patterns.

## One coherent user experience

The owner considers this extremely important.
Every module must feel native to Zantrix.
A shared visual style alone is insufficient.
Standardize:

- Navigation.
- Workspaces.
- Patient context.
- Forms.
- Validation.
- Messages.
- Errors.
- Accessibility.
- Terminology.
- Save behavior.
- Interaction patterns.

External modules must follow the same rules.
Epic was mentioned as a reference for multiple patient workspaces, not as a specification to copy wholesale.

The accepted [shared UI design](shared-ui-design.md) allows specialized React interactions inside approved shell extension points. The shell owns the patient banner/selection, navigation, overlays and lock priority. Modules must register editable draft state, use the shared context-bound backend client and satisfy semantic-token, scoped-style and accessibility contracts. Clinical validation and finalization remain with the backend data owner; an earlier validation response or enabled button is insufficient authority to finalize a different/currently unauthorized version.

## Multiple patient workspaces

Support multiple simultaneously open patient records.
There must be a limit.
The exact number is not decided.
Requirements:

- Active patient clearly visible.
- Patient identity clear in dialogs.
- Workspace-specific care context.
- Isolated drafts.
- Pending requests bound to their original patient.
- No silent loss when closing a workspace.
- No unintentional context transfer when switching patients.

The accepted [explicit patient-context contract](identity-session-authorization-design.md#explicit-patient-context-for-every-clinical-operation) requires patient-workspace and expected-patient identifiers on each patient-bound request, with server-side membership, access and owner-entity validation. A session-global current patient must never determine autosave destinations or late-response routing.

## Drafts

Automatically save input as drafts.
Display truthful states such as:

- Saving.
- Saved.
- Save failed.

Autosave does not finalize or sign.
Restore drafts safely after refresh, interruption or session expiration.
Preserve:

- Patient.
- Author.
- Care context.
- Version.

Require appropriate authentication before accessing restored drafts.
Do not clear input merely because a request was started.

The regular online shell and modules must not persist patient data or drafts in localStorage, sessionStorage, IndexedDB or browser caches. The server acknowledgement establishes durable saving. On detected loss of authority, lock and unmount clinical views, clearing display/module state. Retain only the unacknowledged delta in a shell-private volatile recovery buffer (R127–R129).

Recovery requires the same principal, fresh current authorization, correct patient context and reconciliation against the latest server draft/version; roaming never permits silent overwrite. Destroy the buffer before a different user's workspace loads and when its hard TTL expires. Exact TTL remains open. Check expiry on resume/access as well as through timers; browser suspension can delay cleanup. Tab/process loss can lose unacknowledged input, so do not promise lossless recovery. See the [full browser/recovery contract](identity-session-authorization-design.md#browser-storage-and-fail-closed-presentation).

## Finalization and corrections

Drafts may be editable.
Finalized clinical content must retain historical versions.
Use identifiable corrections or additions with:

- Author.
- Time.
- Reason.
- Relation to the previous content.

Other domains need their own lifecycle, such as stopping or replacing medication.

## Concurrent work

Multiple users may view the same record.
Concurrent editing is allowed only where it is demonstrably safe.
The owner explicitly rejected uncontrolled editing of the same component.
Use:

- One editor per protected registration or workflow scope.
- A broader protected scope when independence cannot be established.
- Whole-record editing exclusion if necessary.

Example: medication-list changes may require broader protection than independent notes.

## Lock takeover

Taking over editing requires an additional permission.
It is not a standard right for every clinician.
Requirements:

- Explicit reason.
- Audit.
- Preserve the existing draft.
- Reject stale writes from the original session.
- Provide safe handling of unsynchronized input.

Exact lease, heartbeat and expiry behavior remain open.

## Duplicate prevention

Editing locks alone do not prevent duplicate data.
Also address:

- Double clicks.
- Retried requests.
- Repeated external messages.
- Sequential duplicate entry.
- Similar but clinically distinct facts.

Do not automatically merge superficially similar records.

## Patient-level confidentiality

The owner selected initial restriction at patient level.
Separate confidentiality boundaries for individual notes or dossier components are not an initial requirement.
Roles still govern permitted actions.
Validate whether this model is sufficient for the intended first deployment and applicable obligations.

## Clinical assurance

The owner has hospital domain knowledge.
GP-specific workflows still need review with actual GP users.
Keep assumptions separate from validated requirements.
Clinical deployment needs evidence concerning:

- Hazards.
- Mitigations.
- Usability.
- Data integrity.
- Security.
- Privacy.
- Operations.
- Applicable regulatory responsibilities.
