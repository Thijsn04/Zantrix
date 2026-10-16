# Zantrix

Zantrix is being redesigned as an open-source, modular electronic health record. The first care setting is general practice in the Netherlands. The eventual Dutch deployment goal is full replacement of a practice's existing HIS; the first engineering milestone is one complete GP consultation.

**Current state: documentation-only design baseline.** The previous application has been removed under the owner's authorization, with the old Git history archived externally and the existing license preserved. No replacement application, running service, completed module SDK or clinical deployment readiness is claimed. Historical application tests do not validate this rebuild.

The agreed direction is a modular monolith using Java/Spring Boot, React/TypeScript and PostgreSQL. Exact versions and many detailed design choices remain open. Core provides technical foundations, Bridge handles internal module interaction, and Connect handles external interoperability.

- [Clean repository preparation and provenance](docs/clean-repository-start.md)
- [Documentation index](docs/README.md)
- [Decision register and status vocabulary](docs/decisions.md)
- [Open design questions](docs/open-questions.md)
- [Roadmap](docs/roadmap.md)
- [Reconstruction verification and recovery limits](docs/recovery-verification.md)
- [Handover](HANDOVER.md)
- [Contributing](CONTRIBUTING.md)
- [Security policy](SECURITY.md)
- [Code of conduct](CODE_OF_CONDUCT.md)

Continue requirements discovery and detailed architecture review before preparing scoped implementation plans. Product implementation has not been authorized by this documentation checkpoint.

## License

The existing [GNU AGPL v3 license](LICENSE) is unchanged. External-module distribution and licensing policy remain open.
