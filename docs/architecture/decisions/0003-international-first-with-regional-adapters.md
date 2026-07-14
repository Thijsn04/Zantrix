# ADR 0003: International first core with regional adapter packs

## Status

Accepted.

## Context

The earlier codebase wove Netherlands specific concerns directly into core modules: the BSN as a first class patient field, fixed national insurer data, and national coding assumptions. Some of these were fabricated responses rather than real integrations. This limits the audience to one country and couples the clinical core to one jurisdiction's rules.

An open source EHR aiming for broad adoption needs a core that runs anywhere, while still supporting the deep regional requirements that make an EHR usable in a given country.

## Decision

Keep the core region neutral, based on international FHIR R4 and widely used international profiles. Package all country specific behaviour, meaning profiles, terminology bindings, national identifiers, connectors, and reimbursement rules, as regional adapter packs that a deployment enables. Adapter packs are disabled by default, and no core module depends on any adapter pack.

The Netherlands pack is the first adapter pack and serves as the reference implementation for the pattern. National identifiers such as the BSN are modeled as standard FHIR identifiers within their pack.

## Consequences

Positive:

- The core is usable in any country out of the box.
- Regional complexity is isolated, explicit, and honest about what is real versus stubbed.
- New countries are added as packs without touching the core.

Negative:

- An extra layer of indirection for regional behaviour.
- Regional features require a clear extension surface in the core, which must be designed and maintained.
