# ADR 0016: SMART on FHIR application hosting

## Status

Accepted in principle. Not yet implemented, and required before the diagnostics phase. Sequenced in phase F2 of the [frontend plan](../frontend-plan.md).

## Context

Substantial parts of the [module vision](../../modules/README.md) are commonly delivered by third parties rather than built: imaging viewers, specialty calculators, risk scores, and diagnostic vendor tools. The established integration route for these is SMART on FHIR, where an application is launched from the record with context and a scoped token, and renders inside the clinical workspace.

Rebuilding each of these is not realistic and, for something like a diagnostic image viewer, not desirable. Refusing to host them pushes clinicians into separate applications with separate logins, which is the fragmentation Zantrix exists to reduce.

The risk is that embedding third party code inside the clinical workspace puts foreign code next to patient data and next to the session that authorizes access to it. A hosted application that can read the host session, observe other patients, or outlive its launch context is a serious exposure.

## Decision

The workspace can host SMART on FHIR applications, under conditions that keep a hosted application strictly bounded.

**Isolation.** A hosted application runs in a sandboxed frame with its own origin. It has no access to the host session, host storage, or the host document. Communication with the host is limited to the SMART launch handshake and an explicit, minimal message contract.

**Its own token, never the host's.** A hosted application receives a token issued for it, scoped to the launch context and to the least privilege it declares. The clinician's own session token is never handed to embedded code.

**Context is given, not discovered.** The application receives the patient and encounter it was launched for. It cannot navigate to another patient, and it cannot enumerate the record. Changing patient in the host ends the launch rather than silently re-pointing the embedded application.

**Bounded lifetime.** A launch ends when its context ends: closing the activity, changing patient, session lock, or sign out. Tokens are short lived and are not renewable beyond the launch.

**Registered, not open.** Only applications an operator has explicitly registered and enabled may be launched. There is no open app store and no runtime registration. Registration is an administrative action and is audited.

**Audited like any other access.** A launch, and the access the hosted application makes, appear in the audit trail attributed to both the application and the launching user, because from the patient's perspective their record was accessed.

**Visibly foreign.** The interface makes clear that the content is a third party application and names it. A hosted application must not be able to present itself as part of Zantrix.

## Consequences

Positive:

- Diagnostic and specialty capability can be integrated without rebuilding it or sending clinicians to a separate system.
- The standard route means vendors that support SMART need no bespoke contract.
- Isolation, scoped tokens and bounded lifetime keep the exposure of embedded code far smaller than the host session.
- Operators decide what runs in their deployment.

Negative:

- Hosting third party code near patient data is a real increase in attack surface, mitigated but not eliminated.
- Correct SMART launch, token issuance and scope handling is significant work, and requires backend authorization support that does not exist yet.
- Embedded applications have their own accessibility, internationalization and visual behaviour, and the workspace cannot guarantee any of it. A poor application degrades the clinician's experience of Zantrix.
- Support becomes harder: a failure inside a hosted application looks to the user like a failure of the record.
- Registration and review of applications is an ongoing operator responsibility, not a one time setup step.
