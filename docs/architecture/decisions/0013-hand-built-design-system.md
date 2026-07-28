# ADR 0013: Hand built design system with an accessibility test gate

## Status

Accepted.

## Context

The workspace is composed from a small design system built directly on Tailwind CSS: Button, Panel, DataTable, Field, Tabs, Badge and Feedback primitives over a token layer.

A complete EHR needs far more: combobox, dialog, drawer, menu, listbox, date and time pickers, tooltips, toasts, tree views, virtualized tables and split panes. Two approaches were available. Adopt headless primitives such as Radix, Ark or React Aria and keep only the styling, or continue building every primitive by hand.

The tradeoff is not visual. Headless libraries exist mainly because the interaction and accessibility behaviour of these widgets is genuinely hard: focus management, roving tabindex, typeahead, screen reader announcement, dismissal semantics and pointer versus keyboard differences. Rebuilding that correctly is a real and recurring cost.

Against that, an external dependency in the interaction layer of a clinical application is a long lived commitment. It shapes the component API, it must be kept current for security, and its release cadence becomes a constraint.

## Decision

Zantrix builds its design system by hand on Tailwind CSS, with no headless component dependency.

Because the accessibility work is therefore owned rather than inherited, it is made a gate rather than an intention:

- Every interactive primitive ships with tests for keyboard operation, focus management and screen reader semantics before it is used in a feature.
- Automated accessibility assertions run in continuous integration against every primitive and every composed screen, and a violation fails the build.
- Four primitives are treated as high risk and require an explicit review against the relevant ARIA authoring practice before merge: combobox, dialog, menu and date picker. These are the widgets where hand built implementations most often fail keyboard and screen reader users.
- A primitive is not considered available for use until it meets these gates. Screens do not work around a missing primitive with ad hoc markup.

WCAG 2.2 AA is the baseline for both surfaces. Automated checks are treated as a floor, not proof, and periodic manual assessment including screen reader testing remains required before any capability is called stable.

## Consequences

Positive:

- Full control over interaction, density and visual language, which matters for a dense clinical interface.
- No third party dependency in the interaction layer, and no upgrade treadmill for it.
- Accessibility behaviour is understood by the team that owns it rather than assumed from a library.
- The component surface stays small, because every primitive has to earn its existence.

Negative:

- Significantly more work per complex widget, and the work is the difficult kind.
- Real risk of subtly incorrect behaviour for keyboard and screen reader users. The test gate reduces this risk but does not remove it.
- Automated accessibility tooling catches a minority of real barriers, so the gate can create false confidence if manual assessment is skipped.
- If the gate proves too slow to sustain, the honest response is a new ADR adopting headless primitives, not quietly lowering the bar.
