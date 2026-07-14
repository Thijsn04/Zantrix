# Zantrix Backend

The backend is a Java 21, Spring Boot 3.3 modular monolith. It is an OAuth2 resource server and a guarded client of the dedicated HAPI FHIR R4 service.

## Run locally

Start PostgreSQL, Keycloak, and HAPI FHIR from the repository root first:

```bash
docker compose up -d
```

Then start the backend:

```bash
./mvnw spring-boot:run
```

On Windows use `.\mvnw.cmd spring-boot:run`. The API listens on port 8080.

## Verify

```bash
./mvnw clean verify
```

Docker must be running because integration tests use Testcontainers with PostgreSQL and the official HAPI FHIR image.

See [development setup](../docs/development.md) for endpoints, test accounts, configuration, and troubleshooting, and [backend architecture](../docs/architecture/backend.md) for module rules and current limitations.
