# ADR 0006: HAPI FHIR JPA server as a dedicated service

## Status

Accepted. Supersedes the deployment topology of [ADR 0002](0002-fhir-native-hapi-jpa.md).

## Context

ADR 0002 established FHIR R4 on the HAPI FHIR JPA server as the canonical model,
store, and API, and proposed embedding that server inside the Zantrix Spring
Boot process. Building the platform revealed problems with embedding:

- The HAPI JPA server is a large, version sensitive assembly. The official
  distribution is a full application, not a small library to import. Wiring it
  by hand into another Spring Boot application is fragile and tightly couples
  Zantrix to internal HAPI configuration classes that change between versions.
- The official HAPI server is already a well built, battle tested, maintained
  artifact. Re-implementing its bootstrapping adds risk for no benefit.
- It is far easier to verify a dedicated service. The official image can be run
  directly in local development and in integration tests.

Zantrix already runs infrastructure as separate services (PostgreSQL, Keycloak).
The FHIR server fits the same pattern.

## Decision

Run the HAPI FHIR JPA server as a dedicated service, using the official image,
backed by its own PostgreSQL database. The Zantrix backend is a client of that
server. It holds a shared R4 FhirContext and a FHIR client pointed at the
server, and all capabilities read and write FHIR resources through it.

The HAPI server is internal. It is not exposed to the public internet. The
Zantrix backend is the secured gateway in front of it, applying authentication,
authorization, consent, and audit. External FHIR and SMART on FHIR access is
provided through the Zantrix gateway, not by exposing HAPI directly.

Schema ownership is clarified: HAPI owns and migrates the FHIR resource schema in
its own database. Flyway owns the Zantrix application schema in the Zantrix
database. Neither tool manages the other's tables.

## Consequences

Positive:

- Uses the maintained, battle tested official server instead of fragile custom
  wiring.
- Clean separation. The Zantrix deployable stays a focused application, and the
  FHIR store can scale independently.
- Verifiable. Local development and integration tests run the real server from
  the official image.
- The gateway position gives one clear place to enforce security, consent, and
  audit over all FHIR access.

Negative:

- Two processes instead of one, and a network hop from the backend to the FHIR
  server. Both are local and consistent with how the other infrastructure runs.
- The backend depends on the FHIR server being reachable. This is expected for a
  data platform dependency and is handled with health checks and clear errors.

## Notes

This does not change the FHIR native principle. FHIR resources remain the
canonical representation of clinical data. Only the way the FHIR server is run
has changed.
