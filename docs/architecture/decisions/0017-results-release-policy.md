# ADR 0017: Clinician gated results release, configurable per deployment

## Status

Accepted. Not yet implemented. Required before the patient portal ships in phase F3 of the [frontend plan](../frontend-plan.md).

## Context

The patient portal shows a patient their own results. This is the most consequential screen in the product, because the reader may be alone, may have no clinical training, and may be looking at a result that changes their life.

Releasing everything the moment it is final treats a suspected malignancy on a Friday evening the same as a normal cholesterol value. Releasing nothing until someone remembers to act leaves patients waiting for information that is already about them and already theirs.

Jurisdictions differ and are actively changing. Some require prompt patient access by law, some leave timing to the provider, and some distinguish by result type. A single hardcoded rule would make Zantrix wrong in most places.

Whatever the timing rule, the software must never be the thing that decides a patient is ready to read something. That judgement belongs to a clinician and to the care provider's own policy.

## Decision

Results release is a backend owned state, configurable per deployment, defaulting to release after clinician review.

**Release state lives on the result, not in the interface.** Every result carries an explicit state: awaiting a release decision, released to the patient, or withheld with a documented reason. The portal renders that state. It never infers releasability from status, date, or result values, because an inference is a clinical decision made by accident.

**The default is gated.** Out of the box, a result is not visible to the patient until a clinician has reviewed and released it. A deployment that wants a different rule chooses it deliberately.

**Configurable policies** a deployment may select:

- Gated: released only by explicit clinician action. This is the default.
- Immediate: released when the result is final.
- Delayed: released automatically after a configured interval unless a clinician withholds it first, so a normal result is not held up by an absent clinician while an abnormal one can still be caught.

The policy may be set per result category, because imaging reports and routine chemistry do not carry the same weight.

**Withholding is documented and bounded.** A clinician who withholds a result records why, and the record shows that a decision was made rather than leaving the result in limbo indefinitely.

**Unreleased results are invisible, not merely hidden.** The portal must not disclose that an unreleased result exists: no counts, no badges, no gaps in a numbered list, no notification. A patient learning that something exists which they are not allowed to see is worse than not knowing yet, and it undermines the reason for gating at all.

**Released results carry context, not just values.** A released result is presented with its reference range, plain language explanation, what to do next, and how to reach the care team. A bare number with an out of range marker is not an acceptable patient facing presentation.

**The clinician always sees everything.** Release state governs patient visibility only. It never hides a result from clinical staff.

## Consequences

Positive:

- The clinical judgement stays with a clinician, and the software carries it out rather than substituting for it.
- Deployments in different jurisdictions can comply without forking or patching.
- A safe default means an operator who configures nothing does not accidentally publish sensitive results.
- Release, withholding and the reason are recorded, so the decision is auditable.
- Excluding any signal about unreleased results closes a leak that would otherwise defeat the whole mechanism.

Negative:

- Gating creates a queue, and a queue can be neglected. A result left unreviewed is invisible to the patient indefinitely, which is its own harm. The release queue therefore needs ageing and escalation, and that is more workflow than a simple rule.
- Per category policy is more configuration surface to explain, test and get wrong.
- The delayed policy is a compromise that can still release an abnormal result automatically when nobody acted in time. Operators choosing it must understand that.
- Plain language explanation of results is content work, not engineering, and it does not exist yet. Shipping the screen without it would be worse than not shipping it.
- The portal cannot show a complete count of a patient's own results while any are unreleased, which is a small honesty cost accepted deliberately.
