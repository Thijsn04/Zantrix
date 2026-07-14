<div align="center">

# Zantrix

**An open source, FHIR native Electronic Health Record**

Zantrix is a modern, transparent alternative to closed EHR platforms. It is built around international healthcare standards, an immutable audit trail, and a modular architecture, so that a small clinic and a large hospital can run the same core.

[![License: AGPL v3](https://img.shields.io/badge/License-AGPLv3-blue.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-6DB33F.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19-61DAFB.svg?logo=react&logoColor=black)](https://react.dev/)
[![HL7 FHIR](https://img.shields.io/badge/HL7-FHIR%20R4-e6007e.svg)](https://hl7.org/fhir/)
[![Status: early development](https://img.shields.io/badge/status-early%20development-yellow.svg)](docs/roadmap.md)

</div>

---

## Status

Zantrix is in early development. The project is undergoing a ground up rebuild of its foundation and documentation. Nothing here should be treated as production ready yet. The [roadmap](docs/roadmap.md) tracks honest, verifiable status per capability, and the documentation states clearly what is planned versus what is built.

If you are looking for the design of the system, start with the [documentation](docs/README.md).

## Why Zantrix

Electronic Health Records are typically locked behind proprietary vendors, with closed data models and hard vendor lock in. Zantrix takes the opposite position:

- **Open source and transparent.** Control stays with the care provider, under the AGPLv3 license.
- **Standards first.** HL7 FHIR R4 is the canonical data model, served by a conformant FHIR API. It is extended with SNOMED CT, LOINC, ICD, and DICOM. There are no closed, proprietary formats.
- **International first.** The core is region neutral. Country specific concerns, such as the Dutch BSN, national exchange networks, and reimbursement rules, live in optional adapter packs that are disabled by default.
- **Modular.** Capabilities can be turned on or off, so the same platform fits an independent treatment centre or an academic hospital.
- **Task driven.** The interface is designed to reduce the registration burden on clinical staff and to run like a real clinical application, not a marketing website.

## Architecture

Zantrix is a **modular monolith**: one deployable application with strictly separated internal domains, ready to split into services later if a domain needs it.

| Layer | Technology |
|---|---|
| **FHIR platform** | HAPI FHIR R4 JPA server as the canonical resource store and REST API |
| **Backend** | Java 21, Spring Boot 3, Spring Modulith |
| **Frontend** | React 19, TypeScript, Vite, Tailwind CSS, i18next, PWA |
| **Database** | PostgreSQL 16 |
| **Identity** | Keycloak, OAuth2 and OpenID Connect, SMART on FHIR |
| **Search** | Elasticsearch for terminology and resource indexing |
| **Interoperability** | HL7 v2 and FHIR bridges via Apache Camel |

For the reasoning behind these choices, see the [architecture decision records](docs/architecture/decisions/).

### Security by design

Zantrix targets NEN 7510 and ISO 27001 as design goals. These are goals, not certifications.

- **Access control.** Role based and attribute based access via Keycloak and SMART scopes.
- **Immutable audit trail.** Every access is recorded as a FHIR AuditEvent in a tamper evident, hash chained log.
- **Break the glass.** Emergency access escalation with mandatory justification and after the fact review.
- **Consent and privacy.** Consent aware access and GDPR data subject workflows.

See [security and privacy](docs/architecture/security-and-privacy.md) for the full model.

## Documentation

- [Documentation index](docs/README.md)
- [Product vision](docs/vision.md)
- [Architecture overview](docs/architecture/overview.md)
- [Module vision](docs/modules/README.md)
- [Roadmap](docs/roadmap.md)
- [Contributing](CONTRIBUTING.md)

## Getting started

**Requirements:** Docker and Docker Compose, Java 21 or newer, Node.js 20 or newer.

```bash
# 1. Start infrastructure (PostgreSQL, Keycloak, Elasticsearch)
docker compose up -d

# 2. Start the backend (Spring Boot API on port 8080)
cd backend
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run

# 3. Start the frontend (Vite dev server)
cd frontend
npm install
npm run dev
```

Keycloak imports test users from `realm-export.json` on first start. See [development](docs/development.md) for the full local setup, seed data, and test accounts.

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) first. Security issues should follow [SECURITY.md](SECURITY.md).

## License

Licensed under the **GNU Affero General Public License v3.0 (AGPLv3)**. Improvements to the codebase must be shared back with the community. See [LICENSE](LICENSE).

---

<div align="center">
<sub>Built by <a href="https://github.com/Thijsn04">Thijs Nannings</a>, Medical Informatics at UvA, <a href="https://lythos.nl">Lythos</a></sub>
</div>
