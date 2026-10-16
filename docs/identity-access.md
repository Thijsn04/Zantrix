# Authentication, badges and roaming sessions

Reconstructed from the owner-supplied handover dated 15 October 2026. This document records design requirements and their stated limits; it does not establish implemented functionality.

[Documentation index](README.md)

## Account models

Support:

- Zantrix-managed staff accounts.
- Federation with an institution's identity provider through standard protocols.

Microsoft Entra ID was an example, not a mandatory selected product.
Do not interpret Zantrix-managed accounts as a decision to invent a custom password-security implementation.
The supported direction is an institution-controlled Keycloak broker with an application OIDC boundary (R120); exact versions, assurance and federation profiles remain open.

The accepted [identity/session/authorization design](identity-session-authorization-design.md) specifies Keycloak brokerage, OIDC/server sessions, Core-owned workspace authority and current scoped worker authority (R120–R123). R124–R129 refine transaction fencing, fresh unlock, explicit patient context, browser storage, fail-closed presentation and protected volatile draft recovery. Exact configuration and runtime proof remain pending; read this requirements document together with that design.

Supporting institutional federation does not automatically grant a federated user a local-password fallback. Account linking, provisioning/deprovisioning and recovery require explicit design. This qualification was present in the original recorded architecture and is preserved by the [conversation review](conversation-reconciliation.md).

## Local and remote access

The owner's preference is:

## Trusted on-site workstations

Allow:

- Username and password.
- A suitable employee badge.

Do not require MFA at every ordinary on-site sign-in.

## Remote access

Require MFA, including working from home.

## Trust classification

Still to design:

- What constitutes a trusted workstation.
- What constitutes on-site access.
- How VPN access is classified.
- Device trust.
- Network trust.
- Institutional identity assurance.

An IP address or VPN connection alone must not silently establish physical presence.

## Badge technology

No badge type or reader has been selected.
A publicly readable badge number is not sufficient authentication assurance.
Assess:

- Verification mechanism.
- Copy resistance.
- Reader security.
- Lost-badge revocation.
- Recovery.
- Audit.

## Daily and occasional verification

Accepted preference:

- First badge sign-in of the day also requires the user's password.
- Later badge sign-ins and unlocks may use the badge alone.
- Occasionally ask for the password again during the day.
- Do not make such checks too frequent.

Open details:

- Meaning of a day.
- Night shifts.
- Maximum time since verification.
- Frequency and bounds of occasional checks.
- Identity-provider compatibility.
- Recovery when a password is unavailable.

Random checks are not a substitute for a properly bounded authentication policy.

## Switching users

On shared workstations:

- A staff member can switch to their own session using a badge.
- The previous user's session locks.
- Previous drafts remain associated with that user.
- Their patient information must not remain visible.

## Roaming workspace

The owner wants it to feel as though the employee takes their computer with them.
Interpret this as a roaming Zantrix application workspace containing:

- Open patient records.
- Drafts.
- Clinical context.
- Relevant application state.

This is not a decision to implement a full remote desktop.

## One active workspace

The owner explicitly requires:
A person must never be active in two places simultaneously.

For the primary application:

- One active staff workspace per user.
- Moving to another workstation locks the old one.
- Multiple patient workspaces inside that active session are allowed.
- The server rejects stale-session operations.
- Reconnection must not allow old writes to overwrite newer work.

The interaction with disconnected emergency readers remains open. A global guarantee cannot simply be assumed during network partitions.

## Deliberate locking

Presenting the badge can immediately lock the workstation.
Do not depend on detecting that a contactless badge has left the area.

## Inactivity behavior

Accepted timings:

| Inactivity | Behavior |
| --- | --- |
| Before five minutes | Normal session unless otherwise locked |
| At five minutes | Hide all patient information behind a screen with a two-minute countdown |
| During countdown | A click returns to the session without authentication |
| At seven minutes total | Actually lock the session |

After actual locking, authentication is required under the applicable sign-in policy.
The first screen is a privacy screen, not an authenticated lock.
Its protection is limited because another person could dismiss it.
Review the policy against the threat model and applicable deployment requirements before implementation.

## Sensitive administration

Require reauthentication for high-impact administrative operations, including:

- Granting permissions.
- Installing modules.

Do not add these prompts indiscriminately to normal clinical work.
Open details include:

- Which actions are protected.
- Authentication strength.
- Verification freshness.
- MFA requirements for particular operations.
- Audit.

The owner's preference does not waive legally or clinically required checks.
