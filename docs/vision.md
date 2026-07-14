# Product Vision

## The problem

Electronic Health Records are among the most important software in healthcare, and among the most closed. They tend to be proprietary, expensive, locked to a vendor, and built on private data models that make it hard to move data or integrate new tools. Care providers lose control of their own information, and clinicians work in interfaces that add to their burden rather than reducing it.

## The vision

Zantrix is an open source EHR that a care provider actually controls. It is built on open standards from the ground up, it runs anywhere, and it is designed to be a genuinely good tool to work in. The same core scales from a single clinic to a large hospital, with capabilities turned on as they are needed.

Zantrix aims to be excellent, not merely present. That means a narrow set of capabilities built to a production grade standard first, and honest communication about what is and is not ready, rather than a broad surface of demos.

## Principles

1. **Open and transparent.** AGPLv3 licensed. Improvements return to the community. There are no closed data formats and no vendor lock in.

2. **Standards first.** HL7 FHIR R4 is the canonical data model, served by a conformant FHIR API, and extended with SNOMED CT, LOINC, ICD, RxNorm, and DICOM. Data is interoperable by default rather than as an afterthought.

3. **International first.** The core is region neutral. Country specific requirements live in optional adapter packs, so Zantrix is usable anywhere and can still go deep on a given country's needs.

4. **Modular and scalable.** A modular monolith with strict internal boundaries. Capabilities are enabled per deployment. The architecture can split into services later without a rewrite.

5. **An application, not a website.** The interface is a fast, dense, keyboard friendly clinical workspace with persistent patient context, built on a real design system. It is designed to reduce the registration burden on clinical staff.

6. **Secure and private by construction.** Access control, consent, break the glass, and a tamper evident audit trail apply to every capability. NEN 7510 and ISO 27001 are design goals.

7. **Honest engineering.** Documentation reflects reality. Status is tracked openly. Stubs are labeled as stubs. Quality comes before feature count.

## What success looks like

- A care provider can run Zantrix on their own infrastructure, own their data, and export it in a standard format at any time.
- A clinician finds the interface fast and unobtrusive.
- A developer can read the FHIR API and integrate a new tool without a bespoke contract.
- A new country can be supported by adding an adapter pack, not by forking the core.
- Every capability marked ready is genuinely tested, documented, and production grade.

## Non goals

- Zantrix is not trying to replicate every proprietary feature of an incumbent system on day one. It builds an excellent core first and grows deliberately.
- Zantrix does not add closed or country locked assumptions to the core to move faster. Regional behaviour stays in adapter packs.
