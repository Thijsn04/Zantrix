# Frontend Delivery Plan

This document describes the intended shape of the complete Zantrix frontend, from the current Milestone 1 workspace to the full [module vision](../modules/README.md), and how it stays aligned with the backend as that backend grows.

It is a plan. Nothing here should be read as delivered. The [roadmap](../roadmap.md) remains the single source of truth for status, and [frontend architecture](frontend.md) describes what exists today.

## Decisions this plan rests on

| Decision | Record |
|---|---|
| The API contract is generated, and the backend owns it | [ADR 0011](decisions/0011-generated-api-contract.md) |
| Two surfaces, one foundation: clinician workspace and patient portal | [ADR 0012](decisions/0012-two-frontend-surfaces.md) |
| The design system is hand built, with accessibility as a build gate | [ADR 0013](decisions/0013-hand-built-design-system.md) |
| Offline is read only, with writes blocked and staleness always visible | [ADR 0014](decisions/0014-read-only-offline.md) |
| A clinical view can be opened in a second window, with per window patient context | [ADR 0015](decisions/0015-multi-window-workspace.md) |
| Third party SMART on FHIR applications may be hosted, under strict isolation | [ADR 0016](decisions/0016-smart-application-hosting.md) |
| Results reach the patient after clinician review by default, configurable per deployment | [ADR 0017](decisions/0017-results-release-policy.md) |
| A portal user may act for another person under a provider established relationship | [ADR 0018](decisions/0018-proxy-access.md) |

The complete inventory of screens both surfaces will contain is in [frontend screens](frontend-screens.md). How those screens and the backend capability under them are actually broken into work is in the [implementation plan](../implementation-plan.md).

Earlier decisions that continue to bind this plan are [ADR 0004](decisions/0004-frontend-application-shell.md) on the application shell and [ADR 0005](decisions/0005-english-first-with-i18n.md) on English first internationalization.

## The organizing idea

The backend is a modular monolith where each capability owns a set of FHIR resources and publishes a narrow interface. The frontend mirrors that structure exactly: one feature module per backend capability, composed by a shell that owns only cross cutting concerns.

This is not symmetry for its own sake. It means a new backend capability has one obvious place to land in the frontend, a capability can be switched off per deployment without unpicking shared code, and the two sides can be reasoned about together.

Three rules follow from it, and they hold for every phase below.

1. **The frontend never becomes a second data model.** It composes application endpoints and FHIR resources. Where a screen needs an aggregate that does not exist, the fix is a thin backend endpoint, not client side assembly of clinical facts.
2. **The frontend is never the security boundary.** Role and capability checks in the interface exist so a user is not offered an action the server will refuse. The server remains the only thing that enforces.
3. **Nothing is presented as real that is not.** No placeholder metrics, no invented status, no screen that implies a capability the backend does not have.

## Target repository structure

Today the frontend is a single Vite application. Two surfaces sharing one foundation needs a workspace:

```
frontend/
  packages/
    design/         design system: tokens, primitives, accessibility tests
    contract/       generated API and FHIR types, the ApiClient, error mapping
    i18n/           locale resources and formatting helpers
    clinical/       shared clinical presentation: code display, units, safety wording
  apps/
    workspace/      clinician application
    portal/         patient application
```

`packages/clinical` deserves justification. Presenting a coded value, a dose, a reference range or a safety warning correctly is domain logic that both surfaces need and that must not be reimplemented differently in each. It contains presentation only. It holds no data access.

The migration from the current single application is mechanical and is scheduled in phase F0, before the tree grows further.

### Feature module shape

Inside an application, each capability is a folder owning its screens, hooks and data access:

```
features/<capability>/
  index.ts          the module's public surface: routes, navigation entries, capability id
  routes.tsx        lazily loaded route definitions
  api.ts            queries and mutations, built on the generated contract
  components/       screens and panels
```

A feature module imports from `packages/*` and from its own folder. It does not import from another feature module. Where two features genuinely need the same thing, it moves into a package. This is the frontend equivalent of the backend rule that a module never reaches into another module's internals, and it is enforced by a lint rule rather than convention.

## The contract layer

This is the mechanism that makes "the frontend matches the backend" checkable rather than aspirational. The reasoning is in [ADR 0011](decisions/0011-generated-api-contract.md).

**Pipeline.** The backend generates `openapi.json` from its controllers at build time and commits it. The frontend generates TypeScript types from that document into `packages/contract/generated`. FHIR resource types are generated from the R4 StructureDefinitions. Generated output is committed so that a contract change is visible in review.

**Drift gate.** Continuous integration regenerates both artifacts and fails if the result differs from what is committed. A backend change that alters the contract cannot merge without the frontend seeing it.

**What stays hand written.** The `ApiClient` remains hand written and small. It owns bearer token attachment, aborting, no content responses, RFC 9457 problem detail extraction, and the mapping from transport failure to a message a clinician can act on. Generated types describe payloads; they do not describe how this system behaves when something goes wrong, and that behaviour is worth reading directly.

**Runtime validation.** Responses are validated against the schema in development and in tests. Production traffic is not validated on every call.

**Error contract.** The backend uses RFC 9457 problem details for application endpoints and `OperationOutcome` for the FHIR facade. The contract layer normalizes both into one internal error type carrying status, a safe message, and a machine readable code where present, so features never parse error shapes themselves.

**Prerequisite.** None of this exists yet on the backend side. Publishing the OpenAPI document is a backend task and a blocking dependency for phase F0.

## Capability, role and deployment gating

Three independent things decide whether a user sees a screen, and conflating them is how interfaces drift out of step with servers.

1. **Deployment capability.** Whether the operator has enabled the capability at all, read from the administration feature flags. A disabled capability contributes no routes, no navigation and no bundle.
2. **Role and scope.** Whether this user may use it, mirroring the backend authorization rules. The existing capability model in `lib/roles.ts` grows into `packages/contract` and remains a deliberate mirror: when a backend rule changes, this changes in the same commit.
3. **Clinical context.** Whether it applies right now, for example that a chart section requires a patient in context.

A screen the user may not reach is not rendered and its code is not loaded. A screen they may reach but cannot act in shows the reason rather than a disabled control with no explanation.

## Navigation and workspace model

**Patient search is the entry point to clinical work, and everything about one patient is a section of that patient.** This already holds in the workspace and it holds for every capability added later. A new clinical capability becomes a chart section, not a new destination in the sidebar.

Top level destinations stay few and stable: the role dashboard, patient search, the worklist, and the administrative and privacy areas for the roles that own them.

**Routing.** The workspace currently selects screens from shell state. It moves to real URLs, because a clinical application needs deep links into a chart section, working browser history, and the ability to open a chart in a second window. The route shape follows the navigation model: `/patients/:patientId/:section`. Open charts are shell state rather than URL state, because a URL identifies one patient.

**Multi window.** A clinical view can be opened in a second browser window, per [ADR 0015](decisions/0015-multi-window-workspace.md). Patient context belongs to the window and is resolved from its URL, so nothing done in one window changes what another window is showing. Only session, lock and theme are shared across windows, so signing out or locking applies everywhere at once. Each window always shows the identity of the patient it is displaying.

**Open charts.** Two patient charts may be open at once, each staying mounted so switching preserves state. Opening a third is refused rather than silently closing one. The reasoning is in [frontend architecture](frontend.md) and it is a safety rule, not a resource limit: the classic wrong patient error is losing track of which record is in front of you.

## State and data

- **Server state is React Query.** Cache keys are namespaced by capability and patient. Invalidation is declared alongside the mutation that causes it.
- **Client state stays local.** The shell owns session, theme, open charts and command palette. Anything else lives in the feature that needs it. There is no global store of clinical data.
- **Optimistic updates are restricted.** They are acceptable for reversible, low risk interactions such as toggling a filter or claiming a task. They are never used for clinical writes. A clinician must not see a medication, order or note appear as saved before the server has accepted it.
- **Long lists are virtualized.** Flowsheets, audit trails, result histories and worklists are unbounded and are built for that from the start.

## Offline behaviour

Read only, per [ADR 0014](decisions/0014-read-only-offline.md). Only already viewed data is available, staleness is always visible and undismissable, every write is blocked with an explanation, and medication administration status and unacknowledged results are never served from cache. Off by default, and enabling it in a deployment requires a documented clinical safety assessment.

## Design system

Hand built on Tailwind, per [ADR 0013](decisions/0013-hand-built-design-system.md).

**Tokens** cover colour, spacing, typography, radius, elevation, motion and density. Light and dark are first class. Components consume tokens; screens do not hardcode values.

**Primitive inventory**, in the order the phases need them:

| Group | Primitives |
|---|---|
| Present | Button, IconButton, Panel, DataTable, Field, Tabs, Badge, Notice |
| Near term | Combobox, Dialog, Drawer, Menu, Select, Checkbox, Radio, Toast, Tooltip, Pagination |
| Later | DatePicker, TimePicker, TreeView, SplitPane, VirtualTable, Chart primitives, FileUpload, SignaturePad |

**Accessibility gate.** Every primitive ships with keyboard, focus and screen reader tests before a feature may use it. Automated assertions run in continuous integration on primitives and composed screens. Combobox, Dialog, Menu and DatePicker require explicit review against the relevant ARIA authoring practice. Automated checks are a floor; periodic manual assessment with a screen reader is required before any capability is called stable.

**Component workbench.** Primitives are developed and reviewed in isolation, with every state visible, so states such as error, empty, loading, dense and disabled are designed rather than discovered.

## Clinical safety in the interface

These requirements apply to every phase and are part of the definition of done, not a later hardening pass.

- **Patient identity is always visible** wherever clinical data is shown or entered, and it is repeated at the point of any write that could be misattributed.
- **Wrong patient prevention.** Switching charts is explicit and visible. No background action ever changes which patient is in context.
- **Alert fatigue is a design constraint.** Interruptive alerts are reserved for issues that genuinely require a decision, currently the critical medication safety issues. Everything else is presented in place. An alert that is dismissed reflexively is worse than no alert.
- **Overrides are documented, never silent.** Where the backend requires a reason to proceed, the interface captures it and shows what is being overridden.
- **Destructive and irreversible actions** state their consequence in the confirmation, and name the patient.
- **Units and coded values are never guessed.** Where the backend requires a coding system, the interface uses a validated concept picker and does not accept free text as a code.
- **Failure is explicit.** A failed clinical write is never silently retried, and never leaves the interface implying success.
- **Session and idle policy.** Clinical workstations are shared. The workspace locks on idle and clears the interface on sign out, including any offline cache.

## Internationalization

English first, all user facing text through i18next, per [ADR 0005](decisions/0005-english-first-with-i18n.md). Keys are namespaced per feature so a capability carries its own translations. Dates, numbers, units and names are formatted through locale aware helpers rather than string concatenation. Layouts are built so that a right to left locale is a configuration change rather than a rewrite, and the first such locale is validated before that is claimed.

Patient facing wording in the portal is held to a plain language standard and reviewed separately from clinical wording, because the same value needs different framing for the two audiences.

## Performance

Clinical work is interrupted constantly, so the cost of a slow interface is measured in attention rather than seconds.

- Per route bundle budgets, enforced in continuous integration. A route that exceeds its budget fails the build.
- Every capability is lazily loaded, so a deployment only pays for what it enables.
- Navigation between already visited screens is served from cache and revalidated in the background.
- Long lists are virtualized and paginated at the server.
- Budgets are defined for time to interactive on the chart and for interaction latency on data entry, measured on representative hardware rather than a developer laptop.

## Testing

| Layer | Scope | Gate |
|---|---|---|
| Unit | Formatting, capability model, clinical calculations | Every change |
| Component | Behaviour of primitives and screens, including error and empty states | Every change |
| Accessibility | Automated assertions on primitives and screens, keyboard paths | Every change |
| Contract | Generated types match the committed contract | Every change |
| Visual regression | Design system and key screens, light and dark | Every change |
| End to end | Real stack, per role journeys through the delivered capabilities | Every change |
| Manual accessibility | Screen reader and keyboard only assessment | Before stable |
| Clinical review | Wording, safety framing, alert behaviour | Before stable |

End to end coverage is organized by role journey rather than by screen, because the thing that must keep working is a clinician completing a task.

## Delivery phases

Each phase depends on backend capability that must exist first. Where it does not, that is stated. Phases are sequenced, not dated.

### F0. Foundation

Purpose: make the rest of the plan buildable, before the surface area grows.

- Split the repository into the package and application workspace described above.
- Stand up the contract pipeline and the drift gate. **Blocked on the backend publishing an OpenAPI document.**
- Introduce routing with deep links into chart sections.
- Move the capability model into the shared package and add the cross feature import lint rule.
- Establish the accessibility gate, the component workbench, bundle budgets and visual regression.

Acceptance: a contract change on the backend fails the frontend build; every existing primitive passes the accessibility gate; every route has a budget.

### F1. Consolidate the outpatient core

Purpose: bring the delivered Milestone 1 workspace to the standard the rest of the plan assumes.

- Complete the primitive inventory needed by existing screens, replacing ad hoc markup.
- Full keyboard paths for every frequent clinical action, and command palette coverage of actions rather than only navigation.
- Explicit loading, empty, error and permission states everywhere.
- Read only offline, off by default.
- Manual accessibility assessment and clinical wording review of the delivered flows.

Backend dependency: none beyond what exists.

Acceptance: the Milestone 1 journeys are usable end to end by keyboard alone, pass manual accessibility assessment, and every screen has designed states.

### F2. Diagnostics and results depth

Aligned with backend Milestone 2. Laboratory and radiology depth, structured result review with reference ranges and trends, specimen and imaging order flows, and an imaging viewer surface.

Backend dependency: Laboratory (D1), Radiology (D4), Imaging and PACS (D5).

New primitives: chart primitives for result trends, VirtualTable, image viewer integration.

### F3. Patient portal, first release

The second surface. Appointments, results release with clinician controlled timing, documents, secure messaging, and questionnaires.

Backend dependency: Patient Portal (E1), Questionnaires (E3), Notifications and Messaging (P8), and a patient facing authentication and consent model.

This phase carries the largest non technical requirement in the plan: results release policy, plain language presentation, and safety guidance for a patient reading an abnormal result without a clinician present. It is not shipped without clinical governance.

### F4. Inpatient and acute

Ward and bed views, admission, transfer and discharge, the emergency department trackboard, observation flowsheets and early warning scores.

Backend dependency: Encounters and ADT beyond the outpatient slice (A2), Bed Management (O1), Emergency Department (S1).

This is where density and real time behaviour are hardest. Trackboards and flowsheets need virtualization, live updates and a legible visual encoding of urgency that does not depend on colour alone.

### F5. Revenue cycle and operations

Coding, charge capture, claims and the operational capabilities in Layer 7.

Backend dependency: Layer 6 and Layer 7 capabilities.

These users are not clinicians. They work in long, repetitive, keyboard heavy sessions, and the interface is optimized for throughput rather than for the careful pace of clinical review.

### F6. Analytics and dashboards

Operational dashboards, population health cohorts and measure reporting, built on the analytics platform rather than the operational store.

Backend dependency: Analytics and Reporting Platform (P10).

Visualization work here follows an explicit charting standard so that every dashboard reads as one system and remains accessible without relying on colour.

### F7. Specialty packs and regional adapters

Specialty experiences from Layer 4 and the regional adapter packs. Each specialty is a feature module composing the clinical core plus its own content. Regional behaviour arrives as configuration and adapter provided fields, never as branching inside core screens.

Backend dependency: Interoperability and Localization (P7) and the relevant specialty capabilities.

## What must be true before any frontend capability is called stable

Mirroring the honesty rules in the [roadmap](../roadmap.md), a capability moves to stable only when all of the following hold:

- Contract types are generated, not transcribed.
- Every state is designed: loading, empty, error, permission denied, offline.
- Keyboard operable end to end, and manually assessed with a screen reader.
- Component and end to end tests cover the role journeys it serves.
- Within its bundle budget and interaction latency budget.
- User facing wording reviewed, and clinically reviewed where it presents clinical information.
- Documented, including what it deliberately does not do.

## Hosting third party applications

The workspace can embed registered SMART on FHIR applications, per [ADR 0016](decisions/0016-smart-application-hosting.md). A hosted application runs sandboxed on its own origin, receives its own scoped short lived token rather than the clinician's, is given its patient context and cannot navigate away from it, ends when that context ends, and is visibly identified as third party. Only applications an operator has registered may launch, and launches are audited.

This matters most for diagnostics, where imaging viewers and vendor tools are commonly delivered this way, which is why it is required before phase F2 rather than left until the specialty phases.

## Resolved questions

Decisions that were open when this plan was first written, and how they were settled.

| Question | Resolution |
|---|---|
| Multi window support | Yes. Per window patient context, shared session and lock. [ADR 0015](decisions/0015-multi-window-workspace.md) |
| SMART application hosting | Yes, for registered applications under strict isolation. [ADR 0016](decisions/0016-smart-application-hosting.md) |
| Native mobile applications | No. Patient facing mobile use is served by the portal as a mobile first web application. No second technology stack. |
| Regulatory posture | Zantrix supplies software and does not obtain certification. Regulatory classification, conformity assessment, clinical risk management and validation are the responsibility of the deploying organization. Stated in the [README](../../README.md). |
| Visual regression tooling | Playwright's own screenshot comparison, since Playwright is already used for browser testing. Baselines are committed and generated in the continuous integration container so they are deterministic across contributor machines. |
| Cross patient department schedule | Yes. A front desk day view across every patient, separate from the chart. It needs a backend endpoint listing appointments by date, practitioner or location, which does not exist yet. |
| Results release to the portal | Configurable per deployment, defaulting to release after clinician review. [ADR 0017](decisions/0017-results-release-policy.md) |
| Proxy and caregiver access | Yes, under a provider established relationship with explicit scope, expiry and immediate revocation. [ADR 0018](decisions/0018-proxy-access.md) |

Because the project does not carry a conformity assessment, the interface must not imply one. No screen states or suggests regulatory approval, certification, or fitness for a regulated purpose.

## Backend capability this plan depends on

The plan assumes backend work that does not exist yet. Listing it here keeps the dependency visible rather than discovered mid phase.

| Needed for | Backend capability |
|---|---|
| F0 contract pipeline | A published OpenAPI document generated from the controllers |
| F1 department schedule | Listing appointments by date, practitioner or location rather than only by patient |
| F2 SMART hosting | Launch context, application registration, and tokens scoped to a launch |
| F3 results release | A release state on results, the configured policy, and withholding with a reason |
| F3 proxy access | RelatedPerson and Consent modelling, tokens carrying a subject distinct from the authenticated person, and dual attribution in audit |
| F3 portal identity | Patient authentication separate from staff identity, and enrolment identity verification |

## Remaining open questions

None outstanding. New questions are recorded here as they arise rather than settled silently.

Two items are decided in principle but need content and policy work before the screens they belong to can ship, and neither is engineering work:

1. **Plain language result content.** [ADR 0017](decisions/0017-results-release-policy.md) requires released results to carry explanation and guidance rather than a bare value. That content does not exist and must be written and clinically reviewed.
2. **Age based narrowing of proxy access.** [ADR 0018](decisions/0018-proxy-access.md) makes this deployment configuration because the correct answer is jurisdictional. The shipped default must be chosen conservatively and documented.
