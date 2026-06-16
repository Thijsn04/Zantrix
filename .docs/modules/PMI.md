# Patient Master Index (PMI) Module

## Overview
The **Patient Master Index (PMI)** is the central repository for patient demographic data within the Zantrix EPD. It serves as the single source of truth for patient identities across all other modules.

## Key Features

### 1. Patient Demographics
- Stores core patient information including:
  - BSN (Burger Service Nummer)
  - Names, Date of Birth, Gender
  - Insurance details
  - Emergency contact indicators
- Tracks active/inactive status and creation/update timestamps.

### 2. FHIR Integration
- Fully supports **HAPI FHIR R4**.
- Patient entities store hard criteria relationally (for quick search) and deep-nested FHIR data as `JSONB` in the PostgreSQL database.
- Uses `FhirPatientConverter` to serialize/deserialize between internal models and FHIR standard representations.

### 3. Search & Deduplication
- Provides robust search capabilities for locating patients.
- Includes deduplication and merge logic: allowing duplicate patient records to be merged into a single primary record, tracking the merge history (`mergedIntoId`).

## Integration & Modularity
- The PMI module interacts with the Frontend through standard REST APIs (`PatientController`), following kebab-case naming conventions.
- Protected by the **IAM module**: all endpoints require explicit RBAC permissions (e.g., `hasAnyRole('DOCTOR', 'NURSE')`).
- Automatically logs all interactions via the IAM's `@AuditLoggable` annotation to ensure NEN7510 compliance.

## Code Standards
- Patient domains, DTOs, Controllers, and Services are strictly separated.
- No direct database access from other modules; all patient data access must route through the PMI APIs or internal event interfaces.
