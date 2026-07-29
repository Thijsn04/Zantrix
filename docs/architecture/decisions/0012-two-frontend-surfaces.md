# ADR 0012: Two frontend surfaces sharing one foundation

## Status

Accepted. Only the clinician workspace exists today. Delivery is sequenced in the [frontend plan](../frontend-plan.md).

## Context

The [module vision](../../modules/README.md) includes capabilities aimed at patients rather than staff: the Patient Portal (E1), Telehealth (E2), Questionnaires and PROMs (E3), and self service check in (A4). These serve a fundamentally different user in a different setting.

The clinician workspace is dense, desktop first, keyboard first, and used continuously by a trained user inside a trusted network. A patient surface is the opposite: used occasionally, on a phone, by an untrained user, over the public internet, with a far smaller and more carefully bounded view of the record.

Serving both from one interface would force a compromise that is wrong for both. Building them as two unrelated applications would duplicate the design system, the contract layer, authentication and internationalization, and would let the two drift apart visually and behaviourally.

## Decision

Zantrix has two frontend surfaces, developed in one repository, sharing one foundation.

**The clinician workspace** keeps its current character: dense, desktop first, keyboard first, with persistent patient context and role shaped navigation.

**The patient portal** is a separate application shell: mobile first, with a deliberately narrow view of the record, larger targets, plain language, and no clinical jargon presented without explanation.

They share the design system, the generated contract layer, the internationalization setup and the authentication primitives. They do not share navigation, information density, session policy or offline policy, because those differ by user and setting.

The portal is not merely a filtered workspace. A patient reading their own result needs different framing, different wording and different safety guidance than a clinician reviewing the same value, and the portal is designed for that reading rather than inheriting the clinical one.

Both surfaces are gated per deployment. A clinic that does not offer a portal does not deploy one.

## Consequences

Positive:

- Each surface can be right for its user rather than a compromise between two.
- The design system, contract layer and authentication are built once.
- The security boundary is explicit: a publicly reachable surface is a separate build with its own session and exposure policy, instead of a code path inside the clinical application.
- A deployment can run the workspace alone.

Negative:

- The repository becomes a workspace of multiple packages, which is more build configuration than a single application.
- Shared code needs deliberate ownership, or one surface will make changes that quietly degrade the other.
- Two shells means two navigation models, two sets of end to end tests and two release surfaces to keep accessible.
- Patient facing wording carries clinical communication risk and needs review by someone other than the engineer who wrote it.
