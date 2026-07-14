# ADR 0004: Frontend as an application shell, not a website

## Status

Accepted.

## Context

The earlier frontend imitated the surface of a desktop application with non functional decoration: fake File, Edit, and Help menus, a single cosmetic tab, and a status bar showing an invented connection latency. At the same time it lacked the substance of an application: no persistent patient context, no real multi item workspace, no command palette, no design system, hardcoded hosts, and untyped data. The result looked improvised rather than production ready.

Clinicians work in an EHR for entire shifts. The interface must be fast, dense, keyboard friendly, and consistent, and it must feel like a tool they operate, not a site they browse.

## Decision

Build the frontend as a genuine application shell. The feel comes from real behaviour, not decoration:

- Persistent, always visible patient context.
- Real workspace tabs that hold live state for multiple open patients or tasks.
- A command palette and comprehensive keyboard support.
- A single design system with accessible primitives and design tokens, meeting WCAG 2.2 AA.
- A single typed API client with configuration driven hosts, centralized authentication, and React Query for server state.
- Strict TypeScript with the `any` type disallowed.

Remove all cosmetic chrome that does not do anything, including the fake menus and the invented status readouts.

## Consequences

Positive:

- A professional, consistent, and fast clinical experience.
- Screens are composed from a shared system, so they are quick to build and uniform.
- Real accessibility and type safety, which matter especially for medical data.

Negative:

- More up front investment in the design system and shell before feature screens land.
- Contributors must learn and use the design system rather than styling screens ad hoc. This is enforced in review and by the component library.
