# Contributing to Zantrix

Thank you for your interest in contributing. Zantrix is an open source, FHIR native Electronic Health Record, and it aims for a high standard of quality because it handles sensitive clinical data. This guide explains how to contribute effectively.

Please also read the [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you agree to uphold it.

## Before you start

- Read the [documentation](docs/README.md), especially the [architecture overview](docs/architecture/overview.md) and the [module vision](docs/modules/README.md).
- Check the [roadmap](docs/roadmap.md) to understand what is being built now. Contributions that fit the current milestone are the easiest to land.
- For anything non trivial, open an issue to discuss the approach first. This avoids wasted effort and keeps the design coherent.

## Development setup

See [docs/development.md](docs/development.md) for how to run Zantrix locally and how to run the tests.

## How we work

Zantrix is a modular monolith with strict internal boundaries and a FHIR native data model. Contributions must respect these principles:

- **FHIR native.** Clinical data is stored and served as FHIR resources. Do not add private clinical tables that shadow FHIR resources. See [FHIR strategy](docs/architecture/fhir-strategy.md).
- **Module boundaries.** Do not import another module's internal package. Communicate through published interfaces and domain events. Boundary verification tests must pass. See [backend architecture](docs/architecture/backend.md).
- **International first.** Do not add country specific behaviour to the core. It belongs in a regional adapter pack. See [ADR 0003](docs/architecture/decisions/0003-international-first-with-regional-adapters.md).
- **Application quality frontend.** Build screens from the design system, keep strict TypeScript with no `any`, and route all user facing text through i18next. See [frontend architecture](docs/architecture/frontend.md).

## Coding standards

- English for all code, comments, commit messages, and documentation.
- No em dashes anywhere in the repository.
- Backend: Java 21, Spring Boot, clean module boundaries, typed domain errors, no protected health information in logs.
- Frontend: strict TypeScript, no `any`, design system components, full internationalization.
- Database: schema changes only through Flyway migrations. No Hibernate automatic schema generation.
- Security: no secrets in source. No fabricated responses standing in for real integrations. Label stubs clearly as stubs.

## Tests are required

- Add unit tests for logic, mappers, and calculators.
- Add integration tests with Testcontainers for anything touching persistence or the API. Do not use in memory databases.
- Add or update Playwright tests for critical cross-service clinical flows and Testing Library coverage for focused frontend behavior.
- Continuous integration must be green before a pull request can merge. This includes build, lint, type check, tests, and module boundary verification.

## Commit messages

Use Conventional Commits, for example:

- `feat(orders): add specimen collection to lab orders`
- `fix(auth): correct scope resolution for nurse role`
- `docs(architecture): clarify audit hash chain`

## Pull requests

1. Fork the repository and create a branch from the default branch.
2. Make your change with tests and documentation.
3. Ensure the full test suite and linters pass locally.
4. Open a pull request that describes what changed and why, and links any related issue.
5. Respond to review. A pull request merges when continuous integration is green and a maintainer approves.

## Reporting security issues

Do not open a public issue for a security vulnerability. Follow the process in [SECURITY.md](SECURITY.md).

## License

By contributing, you agree that your contributions are licensed under the AGPLv3, the same license as the project. See [LICENSE](LICENSE).
