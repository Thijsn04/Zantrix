# Frontend Architecture

The Zantrix frontend is a clinical workspace. It should feel like a focused desktop grade application that clinicians work in all day, not like a website they visit. This document defines what that means and how the frontend is built.

## Current implementation

The frontend currently renders a localized placeholder screen. It has React 19, TypeScript 6 strict mode, Vite 8, Tailwind CSS 4, i18next, PWA generation, environment-driven backend/OIDC configuration, and a top-level `react-oidc-context` provider. One Testing Library smoke test covers the placeholder.

React Query, React Router, Lucide, and the OIDC libraries are installed as foundation dependencies. The application does not yet have routes, backend calls, login controls, patient context, workspace tabs, a command palette, a typed FHIR/API client, a design system, or Playwright tests. The remainder of this document is the target frontend architecture.

## An application, not a website

The difference is not decoration. A clinical application earns the feel through real behaviour:

- **Persistent patient context.** Once a patient is in context, they stay in context across the chart, orders, notes, and results, until explicitly changed. Context is visible at all times.
- **Real workspace tabs.** A clinician can have several patients or several tasks open and switch between them without losing state. Tabs reflect real open work, not cosmetic decoration. The previous UI had a single fake tab and non functional File and Edit menus. Those are gone.
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
  app          application shell, routing, providers, context
  design       design system: primitives, tokens, theming
  lib          api client, auth, fhir client, i18n, utilities
  features     one folder per capability (patients, scheduling, orders, ...)
  pages        route level compositions of features
```

- **Feature folders** mirror the capabilities in the [module vision](../modules/README.md). A feature owns its components, hooks, and data access for one domain.
- **The shell** owns global concerns: the top bar, patient context, workspace tabs, command palette, navigation, notifications, and session handling.

## Data layer

- **One typed API client.** All network access will go through a single client. The base URL already comes from configuration, but the client itself has not been built. The previous code hardcoded a development host in many places and sometimes omitted authentication; the rebuild will not repeat that pattern.
- **Auth is centralized.** A top-level OIDC provider is wired today. Login/logout controls, protected routing, token attachment, refresh behavior, and the current-session API integration remain to be built.
- **FHIR aware.** A FHIR client wraps the platform's FHIR API with types generated from the resource definitions, so features work with typed resources rather than untyped JSON.
- **Server state via React Query.** Caching, background revalidation, and optimistic updates are handled by React Query. This is what makes navigation feel instant.
- **Strict typing.** TypeScript strict mode is on. ESLint currently applies the recommended TypeScript rules; an explicit repository-wide `any` prohibition still needs to be added.

## Internationalization

The product is English first, with full internationalization through i18next. Every user facing string is a translation key. There are no hardcoded strings in components, and no mixing of languages within a screen. Additional languages, including Dutch, are translation resources rather than code changes. Locale also drives dates, numbers, and units.

## Progressive Web App

The build currently generates a PWA manifest and service worker. Installability and safe offline behavior still need product-level testing and policy. Offline behavior must never hide stale data or allow unsafe clinical actions against stale data.

## Quality

- **Component tests** with the Testing Library for behaviour.
- **End to end tests** with Playwright are planned for critical clinical flows once those flows exist.
- **Linting and type checking** run in continuous integration and block merges on failure.
