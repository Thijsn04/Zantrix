# Authorization, support and audit

Reconstructed from the owner-supplied handover dated 15 October 2026. This document records design requirements and their stated limits; it does not establish implemented functionality.

[Documentation index](README.md)

See the accepted [identity/session/authorization design](identity-session-authorization-design.md) for current online authority, transaction fencing, fresh authentication, patient-context validation and scoped asynchronous execution. These mechanisms preserve the requirements below; detailed implementation and proof remain pending.

## Basic model

Use function profiles as the baseline.
Allow:

- Standard Zantrix profiles.
- Institution-defined profiles.
- Multiple profiles per person.
- Location-scoped assignments.
- Individual additional permissions.

Examples such as GP, assistant and practice administrator are illustrative, not a finalized role catalog.

## Individual additions

Individual extra rights are:

- Temporary by default.
- Automatically expired.
- Audited.

Permanent additions are possible with:

- An explicit choice.
- A recorded reason.

Effective permissions must be understandable to administrators:

- What is granted.
- Where it came from.
- Where it applies.
- When it expires.
- Who granted it.

## Custom profiles

Institutions must be able to create profiles reflecting their own organization.
Modules expose supported permissions.
Custom profiles cannot disable fundamental platform protections.

## Multiple profiles and location scope

One person can have multiple profiles.
Different assignments can apply at different locations.
An extra profile does not automatically remove a patient-specific restriction.

## Explicit restrictions

An explicit restriction takes precedence over an allowance.
Only specifically designated restrictions can be overridden through the authorized emergency procedure.
The exact policy evaluation model remains to be designed.

## Four-eyes principle

Dual approval for sensitive changes is:

- Optional.
- Configurable.
- Disabled by default.

When it is disabled, the normal requirements still apply:

- Appropriate permissions.
- Reauthentication where required.
- Audit.

## Temporary staff

Temporary staff and locums have:

- Access start date.
- Access end date.

After expiry:

- Access ends automatically.
- Active access/sessions must be terminated appropriately.
- Historical clinical authorship remains.

Authorized staff can explicitly extend the period.

## Revocation

Support immediate online revocation of:

- Access.
- Active sessions.

Distinguish:

- Blocking a badge.
- Disabling the complete account.
- Removing particular permissions.

Immediate revocation across an unreachable emergency reader cannot be guaranteed without a dedicated design.

## Least privilege

Access is based on actual work need.
Technical administration does not automatically authorize patient-record access.
This also applies to Zantrix hosting and support personnel.
Do not grant access merely because someone manages servers or modules.

## Support access

The institution may grant Zantrix support temporary patient-record access.
Requirements:

- Explicit institutional consent.
- Named support person.
- Defined purpose.
- Limited scope.
- End time.
- Revocation.
- Audit.

No consent means no application-level dossier access.
Infrastructure access to databases and backups requires separate safeguards.

## Audit oversight

Provide an audit overview for specifically authorized personnel.
Include relevant information about:

- Actor.
- Time.
- Patient record.
- Action.
- Available care context.
- Explicit reason where required.
- Support access.
- Break-glass use.

Ordinary administrators do not automatically get this overview.
Audit-overview access is itself logged.

## Break-glass

Allow justified emergency access within a defined scope.
Do not make every legitimate operational exception an emergency.
Regular access must accommodate:

- Triage.
- Assistants.
- Cover.
- Consultation.
- Other genuine care involvement not established by a scheduled appointment.

Provisional preference: break-glass use produces a notification and retrospective review task without delaying necessary access.
The owner's answer to automated review was tentative.
Still open:

- Review owner.
- Deadline.
- Alerts.
- Allowed scope.
- Restrictions that cannot be overridden.
- Escalation.
