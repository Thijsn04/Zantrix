# ADR 0014: Read only offline with explicit staleness

## Status

Accepted. Not yet implemented. The current service worker caches application assets only.

## Context

Clinical networks are not reliable everywhere. Wireless coverage fails in basements, lifts, older wings and during incidents, and it fails at exactly the moments when the record is most needed.

The build already produces a progressive web app manifest and a service worker, but no policy governs what may be cached or what may be done offline. [Frontend architecture](../frontend.md) states the requirement that offline behaviour must never hide stale data or allow unsafe clinical action against stale data, without saying how.

Three options existed. Cache nothing clinical, so the application is unusable without a connection. Cache for reading only. Or allow offline work with writes queued for later synchronization.

Queued offline writes are the dangerous one. Two clinicians prescribing against different stale views of the same medication list, or a result acknowledged offline that was superseded before it synchronized, produce conflicts that cannot be resolved safely by software after the fact. Ordering, medication and result acknowledgement are precisely where a merge heuristic can cause harm.

## Decision

Zantrix supports read only offline access, and blocks all clinical writes when the record cannot be confirmed current.

- Only data the user has already viewed in the current session is available offline. Nothing is prefetched speculatively, so the offline cache never widens what a user can see.
- Any view served from cache while offline carries a persistent, prominent staleness indicator naming the time the data was retrieved. It is not a transient toast and it cannot be dismissed.
- Every action that writes to the record is disabled while offline, with an explanation rather than a silent failure. There is no queue and no deferred submission.
- Data classified as safety critical and time sensitive, specifically medication administration due status and unacknowledged results, is not served from cache at all. An empty state that says the information cannot be confirmed is safer than a value that may have changed.
- The offline cache is cleared on sign out and on session expiry, and is never written to unencrypted persistent storage on shared devices.

Enabling offline reading for a deployment requires a documented clinical safety assessment. It is off by default.

## Consequences

Positive:

- The record stays readable during a network failure, which is the common and useful case.
- The class of harm caused by conflicting offline writes is excluded by construction rather than mitigated.
- Staleness is always visible, so a clinician can tell whether they are looking at confirmed current data.
- Cached content is bounded by what the user already legitimately accessed.

Negative:

- Offline work is limited to reading. Documentation and ordering stop when the network stops.
- A persistent staleness banner adds visual weight and can encourage banner blindness if it appears too often.
- Caching any patient data on a device increases exposure and imposes device management and encryption obligations on the deployment.
- Deciding what counts as safety critical and therefore never cached is a clinical judgement that must be revisited as capabilities grow, not a fixed engineering list.
