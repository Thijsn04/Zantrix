# Zantrix Documentation

Welcome to the Zantrix documentation. Zantrix is an open source, FHIR native Electronic Health Record. This is the entry point for understanding what the project is, how it is designed, and how to contribute.

Zantrix is in early development and is rebuilding its foundation. Architecture and module documents describe the target design, but each architecture page also states the relevant current implementation and remaining gaps. The [roadmap](roadmap.md) is the single source of truth for delivery status.

## Start here

- [Product vision](vision.md). What Zantrix is for and the principles behind it.
- [Architecture overview](architecture/overview.md). The shape of the system.
- [Module vision](modules/README.md). How Zantrix is divided into capabilities and how they fit together.
- [Roadmap](roadmap.md). What is being built now and in what order.

## Architecture

- [Overview](architecture/overview.md)
- [FHIR strategy](architecture/fhir-strategy.md)
- [Backend architecture](architecture/backend.md)
- [Frontend architecture](architecture/frontend.md)
- [Security and privacy](architecture/security-and-privacy.md)
- [Interoperability and localization](architecture/interoperability.md)
- [Architecture decision records](architecture/decisions/)

## Contributing

- [Development setup](development.md)
- [Backend commands](../backend/HELP.md)
- [Frontend commands](../frontend/README.md)
- [Contributing guide](../CONTRIBUTING.md)
- [Code of conduct](../CODE_OF_CONDUCT.md)
- [Security policy](../SECURITY.md)

## Documentation principles

1. **Honest status.** Documentation never claims a capability is further along than the code supports. Status lives in the roadmap.
2. **English first.** All documentation is in English. Other languages are provided as translations.
3. **Design in docs, status in the roadmap.** Architecture and module documents describe the intended design. They do not track progress.
4. **Decisions are recorded.** Significant choices are captured as architecture decision records and are immutable once accepted.
5. **No em dashes.** A house style rule for the whole repository.
