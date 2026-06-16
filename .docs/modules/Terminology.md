# Terminology & Ontology Server Module

## Overview
The **Terminology & Ontology Server** module provides standardized medical coding and classification for the Zantrix EPD. It ensures that medical data is consistent, interoperable, and adheres to international standards.

## Key Features

### 1. HAPI FHIR R4 Terminology Server
- Fully integrated HAPI FHIR R4 terminology capabilities backed by PostgreSQL.
- Serves as the foundation for clinical decision support, order entry, and diagnostic charting across the EPD.

### 2. Automated Import Pipeline
- Features an `OntologyImportService` capable of parsing and importing massive external terminology files.
- Supports standardized code systems:
  - **SNOMED CT** (RF2 format)
  - **LOINC**
  - **FHIR CodeSystem JSONs** (ICD-10, DHD)

### 3. Management & Testing
- Provides an internal, restricted API (`AdminTerminologyController`) for system administrators to trigger and monitor import jobs.
- The frontend includes a test-browser allowing administrators to query and verify the imported terminologies before they are used in production by clinicians.

## Integration & Modularity
- The Terminology module provides reference data to all clinical modules (e.g., when a doctor selects a diagnosis from the Problem List, it pulls from this Terminology Server).
- Operates independently, ensuring that terminology data doesn't clutter patient or IAM databases.
- Protected by the IAM module to ensure only authorized administrators can trigger ontology imports.

## Code Standards
- Focuses on performance and scalability due to the large volume of terminology data.
- Relies on Spring Modulith principles to keep terminology boundaries strict.
