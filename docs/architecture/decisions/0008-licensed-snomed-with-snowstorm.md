# ADR 0008: Licensed SNOMED CT through Snowstorm

## Status

Accepted.

## Context

Milestone 1 requires real terminology search and validation, but SNOMED CT content cannot be redistributed indiscriminately. A deployment may hold an affiliate or national license while the open-source repository itself must remain usable and legally distributable without embedding RF2 content. Clinical entry must also fail safely when terminology is missing or unreachable.

## Decision

Run Snowstorm 10.11.2 as the FHIR R4 terminology service, backed by Elasticsearch 8.11.1. Each operator imports the SNOMED CT RF2 edition it is licensed to use into a persistent deployment volume. Zantrix never commits, packages, downloads on a user's behalf, or redistributes licensed content.

Clinical finding, substance, and procedure searches use bounded implicit SNOMED ECL value sets through FHIR `$expand`. Coded clinical writes use `$validate-code` and fail closed when the service is unavailable or rejects a code. The active edition and language configuration are deployment concerns.

RxNorm is not loaded into Snowstorm in M1. Medication ingredient codes are validated through the public NLM RxNorm properties API using code-only requests and a short cache. No patient identifier or clinical context is sent.

## Consequences

Positive:

- Zantrix uses a maintained open-source SNOMED server and standard FHIR terminology operations.
- Licensed content stays under the operator's control and outside source control and container images.
- Missing terminology cannot silently produce uncoded or unvalidated clinical facts.
- The boundary can later host regional editions, extensions, and language reference sets.

Negative:

- A fresh stack cannot enter SNOMED-coded clinical facts until the operator imports an edition.
- Snowstorm and Elasticsearch add memory, storage, upgrade, backup, and edition-governance responsibilities.
- M1 does not yet provide managed ICD, broad LOINC, ConceptMap translation, or terminology release promotion workflows.
