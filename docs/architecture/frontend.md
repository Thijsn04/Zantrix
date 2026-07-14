# Frontend Architecture

The Zantrix frontend is a clinical workspace. It should feel like a focused desktop grade application that clinicians work in all day, not like a website they visit. This document defines what that means and how the frontend is built.

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

- **One typed API client.** All network access goes through a single client. There is no hardcoded host scattered across the codebase. The base URL comes from configuration. The previous code hardcoded a development host in dozens of places, and some calls sent no authentication token. Both are corrected here.
- **Auth is centralized.** Tokens, refresh, and the current session are handled in one place and exposed through context. The frontend does not decode tokens by hand in individual components.
- **FHIR aware.** A FHIR client wraps the platform's FHIR API with types generated from the resource definitions, so features work with typed resources rather than untyped JSON.
- **Server state via React Query.** Caching, background revalidation, and optimistic updates are handled by React Query. This is what makes navigation feel instant.
- **Strict typing.** TypeScript strict mode is on and the `any` type is not allowed. Medical data requires full type safety. The previous code used `any` in many places despite its own guideline forbidding it.

## Internationalization

The product is English first, with full internationalization through i18next. Every user facing string is a translation key. There are no hardcoded strings in components, and no mixing of languages within a screen. Additional languages, including Dutch, are translation resources rather than code changes. Locale also drives dates, numbers, and units.

## Progressive Web App

The frontend is a PWA so it can be installed and can degrade gracefully on unreliable networks. Offline behaviour is scoped carefully: it never hides that data may be stale, and it never allows unsafe clinical actions against stale data.

## Quality

- **Component tests** with the Testing Library for behaviour.
- **End to end tests** with Playwright for critical clinical flows, such as registering a patient, placing an order, and signing a note.
- **Linting and type checking** run in continuous integration and block merges on failure.
