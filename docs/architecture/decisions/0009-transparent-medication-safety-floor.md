# ADR 0009: Transparent medication safety floor

## Status

Accepted.

## Context

Medication interaction databases are commonly proprietary and require a commercial license. NLM discontinued the RxNav drug-drug interaction API, so RxNorm remains suitable for medication identity but is not an interaction knowledge provider. Milestone 1 still needs deterministic safety checking that an open-source deployment can inspect, test, and ship without pretending to be comprehensive.

## Decision

Ship a versioned, code-reviewed safety floor based on the 15 high-priority interaction classes from the expert-consensus study by Phansalkar et al. (DOI `10.1136/amiajnl-2011-000612`). Normalize the participating ingredients to current RxNorm identifiers and test every rule in both medication order directions.

At prescribe time, Zantrix checks active MedicationRequest resources and AllergyIntolerance records. Findings are written as DetectedIssue resources in the same FHIR transaction as the MedicationRequest. A clinician may override a warning only through an explicit mitigation reason that is stored with the issue.

The UI and documentation must label this pack as high priority and non-comprehensive. It must never be presented as equivalent to a licensed commercial interaction, contraindication, dose, pregnancy, renal, or duplicate-therapy database. The CDS boundary may later compose an optional separately licensed provider without changing medication persistence.

## Consequences

Positive:

- Every open-source deployment gets a transparent, deterministic, tested safety minimum.
- Medication identity is based on independently validated RxNorm ingredients.
- Safety findings and overrides are preserved as interoperable FHIR resources.
- Knowledge changes are visible in code review and can be regression tested.

Negative:

- The pack cannot detect interactions outside the published high-priority classes.
- Ingredient normalization and clinical review require explicit maintenance as knowledge changes.
- Production deployments remain responsible for deciding whether their setting requires a broader licensed knowledge source.
