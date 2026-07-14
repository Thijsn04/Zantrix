# Development Setup

This guide gets a contributor from a fresh clone to a running local Zantrix. The project is in early development, so expect the setup to evolve alongside the foundation work described in the [roadmap](roadmap.md).

## Prerequisites

- Docker and Docker Compose
- Java 21 or newer (a JDK, not just a JRE)
- Node.js 20 or newer
- Git

## Repository layout

```
Zantrix
  backend/      Spring Boot modular monolith, embeds the HAPI FHIR JPA server
  frontend/     React, TypeScript, Vite application
  docs/         documentation (you are here)
  docker-compose.yml   local infrastructure
  realm-export.json    Keycloak realm with test users
```

## 1. Start infrastructure

The Docker Compose file starts the supporting services: PostgreSQL, Keycloak, and Elasticsearch.

```bash
docker compose up -d
```

The credentials in the compose file are development only values. They are not secrets and must never be used in a real deployment.

## 2. Run the backend

```bash
cd backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

The backend serves the application API and the FHIR API on port 8080.

## 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server prints the local URL. The frontend reads its API base URL and identity settings from configuration, so it points at the local backend and Keycloak by default.

## Test accounts

Keycloak imports a development realm from `realm-export.json` on first start. It contains test users for local development only. Their credentials are intentionally trivial and exist only in this development realm. Never reuse this realm or these accounts outside local development.

The realm defines roles for clinical, nursing, administrative, and privacy officer users. See `realm-export.json` for the current set. As the identity model is rebuilt in Milestone 0, this realm and its roles will be aligned to the roles the application actually checks.

## Running tests

Backend:

```bash
cd backend
./mvnw test
```

Backend integration tests use Testcontainers, which requires a running Docker daemon. They start real PostgreSQL and other dependencies in containers, so no in memory database is used.

Frontend:

```bash
cd frontend
npm run lint
npm test          # once the test setup lands
```

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
