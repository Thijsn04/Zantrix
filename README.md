<div align="center">

# Zantrix

**An open source, FHIR native Electronic Health Record**

Zantrix is a modern, transparent alternative to closed EHR platforms. It is built around international healthcare standards, an immutable audit trail, and a modular architecture, so that a small clinic and a large hospital can run the same core.

[![License: AGPL v3](https://img.shields.io/badge/License-AGPLv3-blue.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-6DB33F.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19-61DAFB.svg?logo=react&logoColor=black)](https://react.dev/)
[![HL7 FHIR](https://img.shields.io/badge/HL7-FHIR%20R4-e6007e.svg)](https://hl7.org/fhir/)
[![Status: M1 beta](https://img.shields.io/badge/status-M1%20beta-yellow.svg)](docs/roadmap.md)

</div>

---

## Status

Milestones 0 and 1 are feature complete at beta quality. The repository now provides a one-command local stack, a secured FHIR R4 facade, patient and outpatient encounter workflows, scheduling, problems, allergies, medications, orders and results, clinical notes, vitals, task worklists, consent, emergency access review, terminology integration, administration, and a usable clinical workspace. These paths are audited and covered by unit, architecture, real-infrastructure integration, and browser tests.

Zantrix is not yet production ready, medically certified, or certified against NEN 7510, ISO 27001, or a national EHR program. A production deployment still needs jurisdiction-specific profiles and policy, infrastructure hardening, operational validation, and licensed terminology content. See the [roadmap](docs/roadmap.md) for the exact boundary.

The [roadmap](docs/roadmap.md) is the source of truth for what is built and what comes next.

If you are looking for the design of the system, start with the [documentation](docs/README.md).

## Why Zantrix

Electronic Health Records are typically locked behind proprietary vendors, with closed data models and hard vendor lock in. Zantrix takes the opposite position:

- **Open source and transparent.** Control stays with the care provider, under the AGPLv3 license.
- **Standards first.** HL7 FHIR R4 is the canonical data model and the backend exposes a secured FHIR R4 facade. Snowstorm provides FHIR terminology operations over deployment-supplied SNOMED CT content. There are no private clinical storage formats.
- **International first.** The core is region neutral. Country specific concerns, such as the Dutch BSN, national exchange networks, and reimbursement rules, live in optional adapter packs that are disabled by default.
- **Modular.** Capabilities can be turned on or off, so the same platform fits an independent treatment centre or an academic hospital.
- **Task driven.** The interface is designed to reduce the registration burden on clinical staff and to run like a real clinical application, not a marketing website.

## Architecture

Zantrix is a **modular monolith**: one deployable application with strictly separated internal domains, ready to split into services later if a domain needs it.

| Layer | Technology |
|---|---|
| **FHIR platform** | HAPI FHIR R4 JPA server as the canonical resource store and REST API |
| **Backend** | Java 21, Spring Boot 3, Spring Modulith |
| **Frontend** | React 19, TypeScript 6, Vite 8, Tailwind CSS 4, i18next, PWA |
| **Database** | PostgreSQL 16 |
| **Identity** | Keycloak, OAuth2 and OpenID Connect, SMART scope enforcement in the backend |
| **Terminology** | Snowstorm 10.11 backed by Elasticsearch 8.11; licensed SNOMED CT RF2 is operator supplied |
| **Interoperability (planned)** | HL7 v2 and FHIR bridges via Apache Camel |

For the reasoning behind these choices, see the [architecture decision records](docs/architecture/decisions/).

### Security and privacy baseline

Zantrix targets NEN 7510 and ISO 27001 as design goals. These are goals, not certifications.

- **Implemented:** stateless JWT authentication, Keycloak realm-role mapping, SMART resource-scope checks, patient-context checks, FHIR Consent enforcement, justified emergency access with review tasks, tamper-evident audit search and integrity reporting, FHIR AuditEvent export, security headers, and a durable FHIR mutation journal with reconciliation.
- **Deployment work:** production MFA and session policy, TLS and secret management, encryption and backup policy, rate limiting, organization/relationship policy, jurisdiction-specific privacy rules, and independent security assessment.

See [security and privacy](docs/architecture/security-and-privacy.md) for the full model.

## Documentation

- [Documentation index](docs/README.md)
- [Product vision](docs/vision.md)
- [Architecture overview](docs/architecture/overview.md)
- [Module vision](docs/modules/README.md)
- [Roadmap](docs/roadmap.md)
- [Contributing](CONTRIBUTING.md)

## Getting started

**Requirements:** Docker and Docker Compose, a Java 21 JDK, and a Node.js version supported by Vite 8 (`^20.19.0` or `>=22.12.0`). CI uses Java 21 and Node 20.

```bash
# 1. Build and start the full local stack
docker compose up --build -d

# Or run the backend and frontend outside Compose while developing:
# backend: Spring Boot API on port 8080
cd backend
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run

# 3. Start the frontend (Vite dev server)
cd frontend
npm ci
npm run dev
```

Keycloak imports synthetic test users from `realm-export.json` on first start. Snowstorm starts without terminology content: import an edition your organization is licensed to use before entering SNOMED-coded clinical data. See [development](docs/development.md) for setup, terminology loading, test accounts, and verification commands.

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) first. Security issues should follow [SECURITY.md](SECURITY.md).

## License

Licensed under the **GNU Affero General Public License v3.0 (AGPLv3)**. Improvements to the codebase must be shared back with the community. See [LICENSE](LICENSE).

---

<div align="center">
<sub>Built by <a href="https://github.com/Thijsn04">Thijs Nannings</a>, Medical Informatics at UvA, <a href="https://lythos.nl">Lythos</a></sub>
</div>
