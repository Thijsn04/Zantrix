# Tasks, worklists and administrative configuration

Reconstructed from the owner-supplied handover dated 15 October 2026. This document records design requirements and their stated limits; it does not establish implemented functionality.

[Documentation index](README.md)

## Worklist types

Support both:

- Personal worklists.
- Shared worklists.

Shared worklists can apply to:

- Teams.
- Locations.
- Other configured organizational scopes.

Responsibility and handover must be understandable.

## Assignment

Assignment is optional.
Staff may:

- Execute a task directly from a shared list.
- Assign it to themselves beforehand.
- Assign or transfer it according to permissions.

Do not require personal assignment before every action.

## Execution safety

Direct execution still needs protection against duplicate processing.
Distinguish:

- Personal assignment.
- Temporary execution ownership or locking.
- Completion.

Exact mechanisms remain open.
The owner did not authorize silently allowing concurrent execution of the same task.

## Urgency and timing

Support configurable:

- Urgency.
- Due time.
- Reminders.
- Escalation.

Not every worklist needs these features.
Configuration should be possible per task type or worklist.
Modules may supply sensible defaults and applicable safety constraints.

## Task creation

Support:

- Manual tasks.
- Automatically generated tasks.

Examples:

- A received result awaiting review.
- A document awaiting signature.

The exact event contracts and task categories are not designed.

## General configurability

The owner emphasized that configurability is a general requirement, not limited to worklists.
Administrators should be able to configure supported behavior, including:

- Creating worklists.
- Associating teams.
- Automatic-task rules.
- Workflow rules.
- Urgency.
- Deadlines.
- Reminders.
- Escalation.

Modules should expose capabilities and defaults.
Avoid ad hoc custom application forks per institution.

## Configuration safety

Configuration must respect:

- Permissions.
- Valid dependencies.
- Valid contracts.
- Data integrity.
- Defined clinical safety constraints.

Not every safety mechanism may be freely disabled.
Precise limits require further design.

## Separate test environment

The owner requested a separate test Zantrix environment:

- Used by the Zantrix team.
- Available to institutions for testing.
- Suitable for workflows and configuration.

The owner explicitly deferred the exact implementation.
Do not prematurely assume:

- A single shared public instance.
- A dedicated clone per institution.
- Copies of production patient data.
- A specific hosting topology.
- A particular synchronization mechanism.

Use synthetic data and protect against messages reaching real care endpoints.
Relate this environment to the synthetic-data module and developer conformance facility.

## Update trials

Institutions must be able to trial module updates before production installation. The test environment uses the intended module versions and relevant configuration, synthetic data and isolated external connections. Test requirements for particular changes will depend on risk; no universal mandatory test gate or environment topology has been selected.

## Configuration promotion

Controlled configuration transfer from test to production is now an agreed requirement, superseding the earlier decision to leave the existence of that capability open. Transfer uses an identifiable change set with an advance view of differences, dependencies and effects. Zantrix validates the set again against the current production environment before applying it.

Only configuration is promoted. Test patients, test accounts and test connections do not automatically accompany it. Exact formats, approval/application workflow, handling of environment-specific references and secrets, production drift/conflicts, and recovery remain to be designed. Do not treat this as automatic promotion, production-data copying or a choice of hosting topology.

See [module lifecycle](module-lifecycle.md) and [the remaining open questions](open-questions.md).
