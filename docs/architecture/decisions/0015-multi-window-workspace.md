# ADR 0015: Multi window clinical workspace

## Status

Accepted. Not yet implemented. Sequenced in phase F0 of the [frontend plan](../frontend-plan.md).

## Context

Clinicians routinely work across more than one screen. Reading a result on one monitor while writing the note on another, or keeping a trackboard visible while working a chart, is normal practice in established systems, and an application that cannot do it forces constant switching in exactly the situations where attention is scarcest.

The workspace currently holds two patient charts in the state of a single browser tab. Nothing survives being opened in a second window, because there is no routing and no shared state between windows.

Two windows showing clinical data introduce a hazard that a single window does not have. If patient context is shared globally, changing the patient in one window silently changes what the other window is showing, and a clinician can write to the wrong record while looking at a header that has moved on. If context is entirely independent, the two windows can drift apart in ways that are also confusing.

## Decision

The workspace supports opening a clinical view in a separate browser window.

**Patient context is owned by the window, not by the session.** Each window resolves the patient from its own URL and holds it for as long as that window lives. No action in one window ever changes which patient another window is displaying. This is the safety rule that makes multi window acceptable at all.

**Routing carries the context.** Every clinical view is addressable, so a window is opened by URL rather than by copying state. This is why routing is a prerequisite in the same phase.

**Only session and preference state is shared** between windows, using a broadcast channel: sign out, session expiry, idle lock and theme. Signing out or locking in one window applies to all of them immediately, because a shared workstation must not be left with an unlocked window behind a locked one.

**Server state is not shared across windows.** Each window keeps its own query cache and revalidates independently. Attempting to synchronize caches between windows adds a large amount of machinery to save a small amount of network traffic, and it reintroduces the coupling this decision is trying to avoid.

**Every window is identified.** A window showing a patient always displays that patient's identity banner, so a clinician glancing between monitors can tell the records apart without reading the content.

## Consequences

Positive:

- Matches how clinicians actually work across multiple monitors.
- Patient context per window removes the wrong record hazard that shared context would create.
- Deep links become useful outside the application, for example from a worklist notification.
- Session and lock behaviour stays coherent on shared workstations.

Negative:

- Independent caches mean the same data may be fetched by several windows, and two windows can briefly show different versions of the same record until each revalidates.
- Sign out, expiry and lock now need a cross window mechanism, which is another failure mode to test.
- The two open chart limit applies per window, so the total number of charts a user can have open is no longer bounded by one number. The safety argument still holds because each window names its own patient, but the limit is weaker than it looks.
- Browser window management is outside the application's control, and popup blocking will need explicit handling.
