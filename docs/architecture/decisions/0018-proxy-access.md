# ADR 0018: Proxy access in the patient portal

## Status

Accepted. Not yet implemented. Required before the patient portal ships in phase F3 of the [frontend plan](../frontend-plan.md).

## Context

Many patients do not manage their own record. Parents act for young children, family members act for relatives who cannot, and formally appointed representatives act under a legal mandate. A portal that only ever serves the patient themselves is unusable for a large share of real care.

Proxy access is also the portal's largest privacy risk. It is the one feature whose entire purpose is to let one person read another person's medical record, so getting the boundary wrong exposes exactly the data the rest of the system protects.

Two aspects make it harder than an ordinary permission.

Relationships change. A mandate is revoked, a guardianship ends, a family relationship breaks down, and the moment it ends the access must end with it.

Age changes the answer. In many jurisdictions a young person acquires confidentiality rights over parts of their record before they reach adulthood, and parental access narrows or ends at an age that differs by country and sometimes by information type. A system that grants a parent full access until a hardcoded eighteenth birthday will breach adolescent confidentiality in a large number of jurisdictions.

## Decision

The portal supports acting for another person, under a relationship the care provider establishes.

**Relationships are provider established, never self asserted.** A person cannot claim proxy access from the portal. The relationship is created by staff after identity and entitlement are verified, and is recorded as a FHIR RelatedPerson with a Consent that carries its scope and duration. Self service requests may be submitted, but they are a request for staff action, not a grant.

**Scope is explicit and can be partial.** A proxy is granted a stated scope: which record, which parts, and until when. Full access is a choice, not the default shape of the feature.

**Revocation is immediate.** Ending a relationship takes effect on the next request, not at the next session or token refresh. Any cached content for that subject is cleared from the proxy's device.

**Age based narrowing is configurable policy, not a constant.** The age at which proxy access narrows or ends, and what it narrows to, is deployment configuration, because the correct answer is jurisdictional. Zantrix ships a conservative default and requires an operator to choose deliberately. This is stated plainly rather than assumed, because an incorrect default here breaches a young person's confidentiality silently.

**Whose record you are in is unmistakable.** When acting for someone else, the interface carries a persistent, prominent indication naming the subject. It is not a subtle label. Switching subject is an explicit action and never happens as a side effect of navigation.

**Audit records both people.** Every access made under a proxy relationship is attributed to the person who actually acted and to the subject whose record was read. A record showing only the subject would make the audit trail actively misleading, which is worse than an incomplete one.

**The patient can see their own proxies.** A competent patient can see who holds access to their record and when it was granted, through the portal's access log and privacy screens.

**Writing is narrower than reading.** A proxy may act administratively, for example booking an appointment or completing a questionnaire, but clinically significant actions are not delegated by default.

## Consequences

Positive:

- The portal works for the patients who most need someone to help them use it.
- Provider established relationships keep identity verification where the verification capability actually is.
- Explicit scope and expiry mean access does not quietly outlive its reason.
- Dual attribution keeps the audit trail truthful, which matters most in precisely the cases where it is scrutinized.
- Making the acting subject unmistakable addresses the wrong record hazard, which is as real in the portal as in the workspace.

Negative:

- This is a significant amount of backend capability that does not exist: relationship modelling, scoped tokens carrying a subject distinct from the authenticated person, and consent enforcement across both.
- Age based narrowing is genuinely hard. It requires per jurisdiction policy, it changes what an existing relationship may see as the subject ages, and getting it wrong has legal consequences for the deploying organization.
- Partial scope multiplies the states every portal screen must handle, since any section may be unavailable for this subject.
- Staff mediated establishment adds front desk workload, and pressure to make it self service will recur. That pressure should be resisted, because verification is the entire safeguard.
- Family circumstances can be contested or unsafe. Software cannot resolve that, and deployments need a human process for disputed or harmful access.
