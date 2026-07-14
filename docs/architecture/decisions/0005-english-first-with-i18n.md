# ADR 0005: English first with internationalization

## Status

Accepted.

## Context

The earlier project mixed languages. Code and guidelines were nominally English, but the documentation was entirely Dutch, and the interface mixed Dutch and English within the same screens, sometimes with hardcoded strings that bypassed the translation system. This is confusing for an international open source audience and inconsistent with the project's own stated conventions.

## Decision

English is the primary language for all code, comments, commit messages, documentation, and the default user interface. The application is fully internationalized through i18next on the frontend, so every user facing string is a translation key and no strings are hardcoded in components. Additional languages, including Dutch, are provided as translation resources rather than code changes.

Domain terms that are strictly tied to a jurisdiction and have no English equivalent, such as the Dutch BSN, are kept in their original form with an English explanation, and live in the relevant regional adapter pack.

## Consequences

Positive:

- One consistent working language lowers the barrier for international contributors.
- Full internationalization means new languages do not require code changes.
- The interface is coherent, with no language mixing within a screen.

Negative:

- Existing Dutch material must be translated or removed. The earlier documentation was removed as part of the rebuild.
- Contributors must add translation keys rather than literal strings, which is enforced in review and by linting.
