# Development Setup

This guide gets a contributor from a fresh clone to the Milestone 1 beta stack.

## Prerequisites

- Docker and Docker Compose
- Java 21 JDK (the compilation target and CI version)
- Node.js `^20.19.0` or `>=22.12.0` (the Vite 8 engine range; CI uses Node 20)
- Git

## Repository layout

```
Zantrix
  backend/      Spring Boot modular monolith, client of the HAPI FHIR server
  frontend/     React, TypeScript, Vite clinical workspace
  docs/         architecture, operation, and contribution documentation
  docker/       first-start database provisioning
  docker-compose.yml   complete local stack
  realm-export.json    Keycloak realm with synthetic test users
```

## 1. Start the complete stack

```bash
docker compose up --build -d
```

| Service | Local port | Purpose |
|---|---:|---|
| Frontend | 5173 | Clinical workspace |
| Backend | 8080 | Application API and secured FHIR facade |
| Keycloak | 8081 | Development OIDC realm |
| Snowstorm | 8082 | FHIR terminology API |
| HAPI FHIR | 8090 | Internal canonical FHIR store, exposed locally for diagnostics only |
| PostgreSQL | 5433 | Zantrix, Keycloak, and HAPI databases |

Elasticsearch is private to the Compose network. On first start, PostgreSQL creates separate `zantrix_db`, `keycloak`, and `hapi` databases. Readiness can be checked with `docker compose ps`, `http://localhost:8080/actuator/health`, and the Keycloak and HAPI metadata endpoints.

The credentials and test users in the repository are development-only values. They are not secrets and must never be used in a real deployment.

### Load licensed SNOMED CT content

Snowstorm deliberately starts with no SNOMED CT edition. Obtain an RF2 release through your organization or national release center, confirm that the deployment is covered by the applicable license, and import it using Snowstorm's RF2 import process. Never commit RF2 packages or extracted terminology data.

Until an edition is loaded, SNOMED search and validation fail closed and SNOMED-coded clinical entry is unavailable; the rest of the stack remains usable. The `zantrix_terminology_data` volume retains the imported edition across normal restarts. `docker compose down --volumes` deletes it and should only be used when all local data is disposable.

## 2. Run the backend outside Compose

Start the supporting services, then stop or omit the Compose backend and run:

```bash
cd backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

Important endpoint groups are:

- Public: `GET /actuator/health`, health subpaths, `GET /actuator/info`, and `GET /fhir/R4/metadata`.
- Application API: `/api/v1/**` for clinical, administration, terminology, privacy, and audit workflows.
- Secured FHIR R4 facade: `/fhir/R4`, with supported-resource CRUD, search, history, and transaction operations.

The raw HAPI endpoint on port 8090 is for local diagnostics only. It must be private in a deployed environment; external FHIR clients use the Zantrix facade.

## 3. Run the frontend outside Compose

```bash
cd frontend
npm ci
npm run dev
```

The Vite dev server normally uses `http://localhost:5173`. Defaults are in `frontend/.env.example`; copy that file to `.env.local` only for overrides. The workspace includes the M1 patient registry, chart, scheduling, tasks, administration, privacy, and audit paths.

## Test accounts

Keycloak imports `realm-export.json` on the first database start. The synthetic users `physician_test`, `nurse_test`, `admin_test`, and `privacy_test` all use the development-only password `test`. The realm also defines `PHARMACIST` and `PATIENT` roles without corresponding test users. Never reuse this realm or these accounts outside local development.

## Running tests

Backend:

```bash
cd backend
./mvnw clean verify
```

Docker must be running. Maven Failsafe and Testcontainers exercise PostgreSQL migrations and audit integrity, IAM authorization, a real HAPI FHIR server, and the complete M1 clinician flow. Test code replaces only out-of-scope network terminology validation so licensed terminology is never required by CI.

Frontend:

```bash
cd frontend
npm ci
npm run lint
npm test
npm run build
npm audit --omit=dev --audit-level=high
```

The browser flow needs the running Compose stack:

```bash
npx playwright install chromium
npm run test:e2e
```

It signs in through the real Keycloak realm, registers a patient through the real backend and HAPI store, and opens the chart. CI runs the same flow against a freshly built complete stack.

## Runtime configuration

Backend configuration is environment driven. The main overrides are `ZANTRIX_FHIR_BASE_URL`, `ZANTRIX_TERMINOLOGY_BASE_URL`, `ZANTRIX_RXNORM_BASE_URL`, `ZANTRIX_OIDC_ISSUER_URI`, `ZANTRIX_OIDC_JWK_SET_URI`, `ZANTRIX_CORS_ALLOWED_ORIGINS`, and `ZANTRIX_CONSENT_MODE`. The issuer must equal the `iss` claim seen by clients; the JWK URL may use a private network address.

## Coding standards

- All code, comments, and commit messages are in English. See [ADR 0005](architecture/decisions/0005-english-first-with-i18n.md).
- No em dashes anywhere in the repository.
- Backend modules do not import another module's `internal` package. Boundary tests enforce this. See [backend architecture](architecture/backend.md).
- Frontend code uses strict TypeScript, avoids explicit `any`, and routes user-facing copy through i18next. See [frontend architecture](architecture/frontend.md).
- Database schema changes use new Flyway migrations. Never modify a migration after it has been applied outside a disposable development environment.

See [CONTRIBUTING.md](../CONTRIBUTING.md) for the contribution workflow.

## Troubleshooting

- If a service cannot reach PostgreSQL, run `docker compose ps` and inspect its logs.
- If Keycloak login fails, confirm that the `zantrix` realm imported and that the token contains the requested `user/*.cruds` scope.
- If a disposable development database predates realm changes, `docker compose down --volumes` followed by a clean start reimports it. This deletes every local Zantrix and terminology record.
- If SNOMED entry reports terminology unavailable or invalid, check `http://localhost:8082/version`, confirm the licensed RF2 edition was imported, and inspect `docker compose logs terminology elasticsearch`.
- If integration tests cannot start containers, confirm that Docker is running and reachable.
