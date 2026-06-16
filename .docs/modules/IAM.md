# Identity & Access Management (IAM) Module

## Overview
The **Identity & Access Management (IAM)** module is the core security and authentication system of the Zantrix EPD. Designed to meet strict MDR and NEN7510 compliance requirements, it ensures secure access, strict authorization, and complete traceability of all user actions.

## Key Features

### 1. Authentication & Authorization (RBAC)
- Integrates with **OAuth2** (Keycloak) using **JWT** tokens for secure stateless authentication.
- Enforces **Role-Based Access Control (RBAC)** restricting access to APIs and frontend components based on roles (e.g., `DOCTOR`, `NURSE`, `ADMIN`).

### 2. Audit Logging (NEN7510)
- Implements strict cryptographic audit logging for every read and write action.
- Logs include hash-chaining to ensure immutability, capturing:
  - User details
  - IP Address
  - Action performed
  - Target Patient ID
- Implemented globally via **Spring AOP** (`@AuditLoggable`), ensuring developers don't have to manually write log statements.

### 3. Break-the-Glass (Emergency Access)
- Provides a controlled mechanism for clinicians to gain emergency access to patient records when standard authorization fails.
- Requires explicit justification and logs the event with the highest priority for post-event review by security officers.

## Integration & Modularity
- The IAM module works independently but integrates with the frontend shell (routing and session handling).
- The `AuditAspect` and `@AuditLoggable` annotation are provided to other modules (like PMI and Terminology) so they can automatically inherit NEN7510 compliance without direct coupling.

## Code Standards
- Adheres to the Zantrix coding guidelines: **no PHI in application logs**, strict separation of domain models, and fully documented Java code with clear, descriptive names.
