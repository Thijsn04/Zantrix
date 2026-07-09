<div align="center">

# 🏥 Zantrix

**An open-source, FHIR-native Electronic Health Record (EHR/EPD)**

Zantrix is a modern, transparent alternative to closed EHR platforms — built around international healthcare standards, an immutable audit trail, and modular scale, so a small clinic and an academic hospital can run the same core.

[![License: AGPL v3](https://img.shields.io/badge/License-AGPLv3-blue.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-6DB33F.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19-61DAFB.svg?logo=react&logoColor=black)](https://react.dev/)
[![HL7 FHIR](https://img.shields.io/badge/HL7-FHIR%20native-e6007e.svg)](https://hl7.org/fhir/)
![Status](https://img.shields.io/badge/status-MVP%20in%20progress-yellow.svg)

</div>

---

## Why Zantrix

Electronic Health Records are typically locked behind proprietary vendors, with closed data models and hard vendor lock-in. Zantrix takes the opposite stance:

- **Open-source & transparent** — control stays with the care provider (AGPLv3).
- **Standards-first** — HL7 FHIR as the core data model, extended with SNOMED CT, ICD-10 and DICOM. No closed, proprietary formats.
- **Modular** — turn modules on or off, from an independent treatment centre (ZBC) to a large hospital.
- **Task-driven UI** — designed to reduce the registration burden on clinical staff.

> Zantrix applies medical-informatics domain knowledge to an inspectable EHR — informed by first-hand experience with hospital information systems.

## Architecture

A **modular monolith**: one application with strictly separated internal domains, ready to split into services later if needed.

| Layer | Technology |
|---|---|
| **Backend** | Java 21 · Spring Boot 3 (HAPI FHIR-ready) |
| **Frontend** | React 19 · TypeScript · Vite · Tailwind CSS · i18next (light/dark, PWA) |
| **Database** | PostgreSQL 16 (`JSONB` for FHIR resources + ACID guarantees) |
| **Identity** | Keycloak 24 (OAuth2 / OpenID Connect, 2FA, SSO) |
| **Search** | Elasticsearch 8 |
| **API** | REST-first — ready for external integrations from day one |

### Security by design (targeting NEN7510 / ISO 27001)

- **RBAC** — fine-grained role-based access control via Keycloak.
- **Immutable audit trail** — every action is hash-chained (previous-hash, current-hash, IP, patient ID) via Spring AOP.
- **Break-the-glass** — emergency access escalation on the ER, with mandatory after-the-fact accountability.
- **2FA & SSO** as standard.

## Getting started

**Requirements:** Docker & Docker Compose · Java 21+ · Node.js 20+

```bash
# 1. Start infrastructure (PostgreSQL :5433, Keycloak :8081, Elasticsearch)
docker-compose up -d

# 2. Start the backend (Spring Boot API on :8080)
cd backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run

# 3. Start the frontend (Vite dev server)
cd frontend
npm install
npm run dev
```

Keycloak auto-imports test users from `realm-export.json`. See [`.docs/`](.docs/) for the full masterplan, architecture and module specifications.

## Roadmap (MVP)

- [x] **Core Identity & Security** — login, RBAC, immutable audit logging, 2FA
- [ ] **Patient Master Index (PMI)** — central patient register (demographics, BSN) *(in progress)*
- [ ] **Scheduling & Resource Planning** — calendars for practitioners, rooms and appointments
- [ ] Clinical documentation, ordering & results modules

## License

Licensed under the **GNU Affero General Public License v3.0 (AGPLv3)** — improvements to the codebase must be shared back with the community. See [LICENSE](LICENSE).

---

<div align="center">
<sub>Built by <a href="https://github.com/Thijsn04">Thijs Nannings</a> · Medical Informatics @ UvA · <a href="https://lythos.nl">Lythos</a></sub>
</div>
