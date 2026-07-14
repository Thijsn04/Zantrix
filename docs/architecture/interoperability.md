# Interoperability and Localization

Interoperability is a first class capability, and it is also where all country specific behaviour lives. Keeping regional concerns in adapter packs is what lets the Zantrix core stay international first.

## Principles

1. **FHIR is the internal lingua franca.** Everything that enters or leaves Zantrix is translated to or from FHIR at the boundary. Internally there is one representation.
2. **The core is region neutral.** No national identifier, exchange network, or reimbursement rule is hardcoded into a core module.
3. **Regions are packs.** Country specific integration, profiles, terminology bindings, and rules are packaged as adapter packs that a deployment enables. They are off by default.

## Inbound and outbound integration

- **HL7 v2.** Classic hospital messaging, such as ADT, orders, and results, is handled through Apache Camel routes that map v2 messages to and from FHIR resources. This lets Zantrix participate in existing hospital integration engines.
- **FHIR APIs.** Zantrix both exposes a FHIR API and can act as a FHIR client to external servers.
- **FHIR Subscriptions.** Internal and external subscribers can be notified of changes, which drives event based integration without polling.
- **Documents.** C-CDA and other document formats are imported and exported where needed, stored as FHIR DocumentReference and Composition.
- **Imaging.** DICOM and DICOMweb connect imaging systems, with ImagingStudy resources referencing the images.

## Regional adapter packs

An adapter pack bundles everything specific to a country or network:

- Profiles and terminology bindings, for example US Core, or Nictiz zib for the Netherlands.
- Identifier systems, for example the Dutch BSN as a standard FHIR identifier.
- Connectors to national systems.
- Region specific validation and workflow rules.

### The Netherlands pack

The first adapter pack targets the Netherlands and includes:

- BSN handling and verification against the national person registry service.
- National exchange connectivity.
- Insurance eligibility checks against the national payer clearinghouse.
- Reimbursement coding for the Dutch system.

This pack is a reference for how future regional packs are structured. It is disabled by default, and the core does not depend on it.

## Why this matters

The earlier codebase mixed Dutch specific logic, such as fixed insurer names and national identifiers, directly into core modules, and returned fabricated responses in place of real integrations. Moving these concerns into explicit, honest adapters does two things. It keeps the core usable anywhere, and it makes clear where an integration is real versus where it is a stub awaiting a real connection.
