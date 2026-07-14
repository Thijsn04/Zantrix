# ADR 0001: Modular monolith with Spring Modulith

## Status

Accepted.

## Context

Zantrix spans many domains, from patient administration to clinical documentation to revenue cycle. A microservice per domain would give strong isolation but at a high operational cost: many deployables, network boundaries, distributed transactions, and complex local development. At the project's current stage the team and deployments are small, and that cost is not justified. At the same time, an unstructured monolith would rot into tangled dependencies, which is already visible in the earlier codebase where modules reached directly into each other's data and duplicated one another.

## Decision

Build Zantrix as a modular monolith using Spring Modulith. One deployable application, divided into modules that map to the capabilities in the module vision. Boundaries are explicit: each module exposes a published interface and domain events, keeps its implementation in an internal package, and never imports another module's internals. Module boundaries are verified by tests that fail the build on a violation.

## Consequences

Positive:

- Strong separation without the operational overhead of many services.
- Simple local development and deployment.
- Clear extraction path. A module can become its own service later, because it already communicates through interfaces and events.
- Boundary violations are caught mechanically, not by review discipline alone.

Negative:

- All modules share one process and one deployment, so they scale together.
- Discipline is required to keep interactions on the front door. The verification tests exist precisely to enforce this.
