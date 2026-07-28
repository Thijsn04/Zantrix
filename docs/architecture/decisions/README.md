# Architecture Decision Records

An Architecture Decision Record (ADR) captures a significant architectural choice, the context around it, and its consequences. ADRs are immutable once accepted. If a decision changes, a new ADR supersedes the old one rather than editing history.

## Format

Each ADR has: a status, the context that forced a decision, the decision itself, and the consequences that follow, both positive and negative.

## Index

| ADR | Title | Status |
|---|---|---|
| [0001](0001-modular-monolith.md) | Modular monolith with Spring Modulith | Accepted |
| [0002](0002-fhir-native-hapi-jpa.md) | FHIR native persistence via HAPI FHIR JPA server | Accepted (topology superseded by 0006) |
| [0003](0003-international-first-with-regional-adapters.md) | International first core with regional adapter packs | Accepted |
| [0004](0004-frontend-application-shell.md) | Frontend as an application shell, not a website | Accepted |
| [0005](0005-english-first-with-i18n.md) | English first with internationalization | Accepted |
| [0006](0006-hapi-fhir-as-dedicated-service.md) | HAPI FHIR JPA server as a dedicated service | Accepted |
| [0007](0007-guarded-fhir-access.md) | Guarded FHIR access boundary | Accepted (accountability extended by 0010) |
| [0008](0008-licensed-snomed-with-snowstorm.md) | Licensed SNOMED CT through Snowstorm | Accepted |
| [0009](0009-transparent-medication-safety-floor.md) | Transparent medication safety floor | Accepted |
| [0010](0010-durable-fhir-mutation-accountability.md) | Durable FHIR mutation accountability | Accepted |
| [0011](0011-generated-api-contract.md) | Generated API contract between frontend and backend | Accepted, not yet implemented |
| [0012](0012-two-frontend-surfaces.md) | Two frontend surfaces sharing one foundation | Accepted, portal not yet started |
| [0013](0013-hand-built-design-system.md) | Hand built design system with an accessibility test gate | Accepted |
| [0014](0014-read-only-offline.md) | Read only offline with explicit staleness | Accepted, not yet implemented |
