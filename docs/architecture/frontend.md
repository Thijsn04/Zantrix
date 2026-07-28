# Frontend Architecture

The Zantrix frontend is a clinical workspace. It should feel like a focused desktop grade application that clinicians work in all day, not like a website they visit. This document defines what that means and how the frontend is built.

## Current implementation

The frontend has React 19, TypeScript 6 strict mode, Vite 8, Tailwind CSS 4, i18next, PWA generation, environment-driven backend/OIDC configuration, and a top-level `react-oidc-context` provider. It presents explicit sign-in/session states, loads `/api/v1/iam/me` through one typed API client, and renders role-aware workspace navigation after authentication.

The workspace is role-shaped. `lib/roles.ts` holds a capability model that mirrors each backend `@PreAuthorize` rule, and navigation, landing page, chart tabs, and individual actions are derived from it. A physician lands on their own work queue, a privacy officer on outstanding emergency-access reviews, and a pharmacist on their task queue, because the pharmacist role cannot read the patient directory at all. When a backend rule changes, the capability model changes in the same commit.

Patient search is the entry point to clinical work, and everything about one patient is a section of their chart: appointments, encounters, problems, allergies, medications, vitals, orders, results, and notes. There is no separate scheduling destination, because an appointment only means something once a patient is in context.

Up to two charts can be open at once. Each stays mounted while hidden, so switching preserves query caches, the selected section, and anything already typed. Opening a third is refused with an explicit message rather than silently closing one, because losing track of which patient is in context is the classic wrong-patient error. Each tab repeats name, age, sex, and MRN so a mistaken switch is visible.

The M1 workspace includes patient search, two-stage registration with duplicate review, a persistent patient storyboard carrying identity plus active allergies and problems, a snapshot overview of the active record, the appointment arrival lifecycle and availability search, Task worklists scoped to assigned/unclaimed/all, administration directories and feature flags, and the privacy office with emergency-access review, a filtered audit trail, and hash-chain verification. Prescribing runs and displays the interaction assessment before the prescription is written and requires a documented override for a critical issue.

Light/dark tokens, responsive layouts, labelled forms, ARIA tab and table semantics, focus styles, explicit API errors, and a searchable Ctrl/Cmd+K command palette are implemented. React Query owns server state and invalidation; Lucide supplies icons.

## An application, not a website

The difference is not decoration. A clinical application earns the feel through real behaviour:

- **Persistent patient context.** Once a patient is in context, they stay in context across the chart, orders, notes, and results, until explicitly changed. Context is visible at all times.
- **Real workspace tabs.** A clinician can hold two patient charts open and switch between them without losing state, because each chart stays mounted. Tabs reflect real open work, not cosmetic decoration. The limit is deliberate: two is enough to compare or hand over, and few enough that the patient in context stays unambiguous.
- **Keyboard first.** Every frequent action has a keyboard path. A command palette provides fast, searchable access to navigation and actions. Power users should rarely need the mouse.
- **Dense and calm.** Clinical screens show a lot of information. The design uses clear hierarchy, restraint, and tabular alignment so density reads as calm rather than cluttered. No decorative chrome, no fake status readouts, no invented latency numbers.
- **Fast.** Navigation is instant because data is cached and prefetched. The interface never blocks on a spinner when cached data can be shown and revalidated.
- **Reliable.** Every screen has explicit loading, empty, and error states. Failures are surfaced clearly and recoverably.

## Design system first

There is a single design system. Screens are composed from it, not styled ad hoc.

- **Primitives.** Button, Input, Select, Combobox, Checkbox, Radio, Modal, Drawer, Tabs, Table, Card, Badge, Toast, Tooltip, and form controls, all accessible and themeable.
- **Tokens.** Color, spacing, typography, radius, and elevation are defined as tokens. Components consume tokens. Individual screens do not hardcode long utility class strings for one off styling.
- **Theming.** Light and dark themes are first class and are driven by tokens, so both are correct without per component effort.
- **Accessibility.** WCAG 2.2 AA is the baseline. Components ship with correct roles, labels, focus management, and keyboard support. Accessibility is a requirement, not a later pass.

The design system lives in its own layer with documented components, so contributors build new screens quickly and consistently.

## Structure

```
frontend/src
  app          application shell, navigation, providers
  design       design system: primitives, tokens, theming
  lib          api client, capability model, formatting, i18n, utilities
  features     one folder per capability (patients, chart, schedule, worklist, admin, privacy, terminology)
```

- **Feature folders** mirror the capabilities in the [module vision](../modules/README.md). A feature owns its components, hooks, and data access for one domain.
- **The shell** owns global concerns: the top bar, role-aware navigation, patient context, command palette, and session handling.
- **Route level pages** are not a separate layer yet. The shell selects the active workspace directly, because the workspace is a small fixed set of destinations rather than a deep URL hierarchy. A router becomes worthwhile when deep linking into a chart is added.

## Data layer

- **One typed API client.** All application network access uses one bearer-token client with configurable base URL, GET/POST/PUT helpers, abort support, no-content handling, and problem-detail error extraction.
- **Auth is centralized.** The top-level OIDC provider owns login, logout, token attachment, callback handling, renewal, and current-session loading. The development realm requests the `user/*.cruds` SMART scope.
- **FHIR aware boundary.** The workspace uses task-oriented typed application DTOs while the backend owns FHIR transaction composition. External clients use the separate `/fhir/R4` facade. Generated resource typings remain a future improvement for direct FHIR-based frontend features.
- **Server state via React Query.** Caching, background revalidation, and optimistic updates are handled by React Query. This is what makes navigation feel instant.
- **Strict typing.** TypeScript strict mode is on and ESLint rejects explicit `any`.

## Internationalization

The product is English first, with full internationalization through i18next. Every user facing string is a translation key. There are no hardcoded strings in components, and no mixing of languages within a screen. Additional languages, including Dutch, are translation resources rather than code changes. Locale also drives dates, numbers, and units.

## Progressive Web App

The build currently generates a PWA manifest and service worker. Installability and safe offline behavior still need product-level testing and policy. Offline behavior must never hide stale data or allow unsafe clinical actions against stale data.

## Quality

- **Component tests** with the Testing Library for behaviour, covering the capability model, patient registry and duplicate review, the appointment arrival lifecycle, and medication safety including the critical-issue override path.
- **End-to-end tests** with Playwright run against the real Compose stack and cover Keycloak login, patient registration, FHIR-backed selection, and chart opening.
- **Linting and type checking** run in continuous integration and block merges on failure.
- **Remaining hardening** includes broader component and browser coverage, formal WCAG 2.2 AA assessment, locale-aware date/unit presentation, richer non-blocking feedback, and a clinical offline-safety policy. The service worker does not authorize offline clinical mutation.
