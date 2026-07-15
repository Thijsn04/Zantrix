# Interoperability and Localization

Interoperability and country-specific behavior belong at explicit boundaries so the Zantrix core remains region neutral.

## Current implementation

The backend exposes a secured FHIR R4 facade for the M0/M1 resource set with CapabilityStatement, CRUD, search, history, and transaction behavior. This is the first real external interoperability surface. No regional pack, Apache Camel route, HL7 v2 interface, FHIR Subscription integration, document exchange, DICOM integration, or national-system connector is implemented.

The repository contains no Netherlands pack and performs no BSN verification, national exchange, insurance eligibility, or Dutch reimbursement operation. Zantrix never returns fabricated responses for those services.

## Target principles

1. **FHIR is the internal lingua franca.** Data entering or leaving Zantrix is translated to or from FHIR at the boundary.
2. **The core is region neutral.** National identifiers, exchange networks, and reimbursement rules are not hardcoded into core modules.
3. **Regions are packs.** Country-specific profiles, terminology bindings, rules, and connectors are optional adapter packs and are disabled by default.
4. **Integration status is explicit.** A connector is never presented as functional until it communicates with and is verified against the real external service.

## Target integration surfaces

- **HL7 v2:** Apache Camel routes for messages such as ADT, orders, and results.
- **FHIR APIs:** secured inbound FHIR/SMART resource access through the Zantrix gateway is implemented for M1; broader resource coverage, SMART launch context, bulk data, and outbound connectors remain planned.
- **FHIR Subscriptions:** event-driven internal and external notifications.
- **Documents:** import and export of formats such as C-CDA, represented with DocumentReference and Composition.
- **Imaging:** DICOM and DICOMweb integration with ImagingStudy references.

Except for the stated M1 FHIR facade, these are planned surfaces, not current features.

## Regional adapter packs

An adapter pack will bundle region-specific profiles, terminology bindings, identifier systems, connector implementations, and workflow rules. The planned Netherlands pack is the first reference implementation and is expected to cover standard FHIR representation of the BSN, authorized person-registry verification, national exchange connectivity, payer eligibility, and Dutch reimbursement coding.

Exact services, legal bases, certification requirements, and terminology licenses must be confirmed during pack design. The core must not depend on the pack.

## Why this boundary matters

The earlier codebase mixed Dutch-specific assumptions into core modules and returned fabricated integration responses. Explicit adapter packs keep the core portable and make the difference between a real connector, a development stub, and a future design unambiguous.
