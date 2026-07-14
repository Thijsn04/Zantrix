# Development Setup

This guide gets a contributor from a fresh clone to a running local Zantrix. The project is in early development, so expect the setup to evolve alongside the foundation work described in the [roadmap](roadmap.md).

## Prerequisites

- Docker and Docker Compose
- Java 21 JDK (the compilation target and CI version)
- Node.js `^20.19.0` or `>=22.12.0` (the Vite 8 engine range; CI uses Node 20)
- Git

## Repository layout

```
Zantrix
  backend/      Spring Boot modular monolith, client of the HAPI FHIR server
  frontend/     React, TypeScript, Vite application
  docs/         documentation (you are here)
  docker-compose.yml   local infrastructure
  realm-export.json    Keycloak realm with test users
```

## 1. Start infrastructure

The Docker Compose file starts the supporting services currently implemented: PostgreSQL, Keycloak, and HAPI FHIR. Elasticsearch is planned for terminology and search work and is not part of the current Compose file.

```bash
docker compose up --build -d
```

This builds and starts PostgreSQL (port 5433), Keycloak (port 8081), the dedicated HAPI FHIR JPA server (port 8090), the backend (port 8080), and the frontend (port 5173). On first start, PostgreSQL creates separate databases for the application, the FHIR server, and Keycloak. The FHIR server is reachable at `http://localhost:8090/fhir`.

The credentials in the compose file are development only values. They are not secrets and must never be used in a real deployment.

## 2. Run the backend

```bash
cd backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

The backend serves the application API on port 8080. It is a client of the FHIR server and is the intended secured gateway in front of it. The currently exposed endpoints are:

- Public: `GET /actuator/health`, health subpaths, and `GET /actuator/info`.
- Authenticated: `GET /api/v1/system/info`, `GET /api/v1/iam/me`, and `GET /api/v1/fhir/status`.

The raw HAPI endpoint on port 8090 is exposed only for local development. There is no public Zantrix FHIR REST proxy yet.

## 3. Run the frontend

```bash
cd frontend
npm ci
npm run dev
```

The Vite dev server prints the local URL, normally `http://localhost:5173`. The frontend defaults to the local backend and Keycloak values shown in `frontend/.env.example`; copy that file to `.env.local` only when overrides are needed. It offers OIDC sign-in and calls `/api/v1/iam/me` only after authentication. The workspace does not yet include clinical flows or patient selection.

## Test accounts

Keycloak imports a development realm from `realm-export.json` on first start. It contains test users for local development only. Their credentials are intentionally trivial and exist only in this development realm. Never reuse this realm or these accounts outside local development.

The imported users are `physician_test`, `nurse_test`, `admin_test`, and `privacy_test`, all with the development-only password `test`. The realm also defines `PHARMACIST` and `PATIENT` roles, but does not currently include users for them. See `realm-export.json` for the authoritative configuration.

## Running tests

Backend:

```bash
cd backend
./mvnw clean verify
```

Backend integration tests use Maven Failsafe and Testcontainers, which requires a running Docker daemon. Zantrix-owned persistence is tested against PostgreSQL, and FHIR connectivity is tested against the official HAPI FHIR image. The Compose topology that connects HAPI to PostgreSQL still needs its own smoke test. JWT decoding is mocked in IAM integration tests; a real Keycloak integration suite has not landed yet.

Frontend:

```bash
cd frontend
npm run lint
npm test
npm run build
npm audit --omit=dev --audit-level=high
```

Run `npm ci` first when validating a fresh checkout. Component tests cover the unauthenticated sign-in entry point. Playwright and end-to-end clinical flows have not landed because no clinical flow exists yet.

## Coding standards

- All code, comments, and commit messages are in English. See [ADR 0005](architecture/decisions/0005-english-first-with-i18n.md).
- No em dashes anywhere in the repository.
- Backend: keep module boundaries clean. Do not import another module's internal package. Boundary tests will fail the build otherwise. See [backend architecture](architecture/backend.md).
- Frontend: strict TypeScript, no `any`, all user facing strings through i18next, screens composed from the design system. See [frontend architecture](architecture/frontend.md).
- Database schema changes go through Flyway migrations only.

See [CONTRIBUTING.md](../CONTRIBUTING.md) for the full contribution workflow.

## Troubleshooting

- If the backend cannot reach the database, confirm the infrastructure containers are healthy with `docker compose ps`.
- If Keycloak login fails, confirm the realm imported by checking the Keycloak admin console on its mapped port.
- If integration tests fail to start containers, confirm the Docker daemon is running and reachable.
