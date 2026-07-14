-- Runs only on the first initialization of the Postgres data volume.
-- Creates the separate databases used by the HAPI FHIR server and Keycloak,
-- keeping each concern in its own database rather than sharing one.
CREATE DATABASE hapi;
CREATE DATABASE keycloak;
