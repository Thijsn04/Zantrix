# Visual identity and clinical information design direction

Created: 15 October 2026. Owner approval: 16 October 2026. Status: Approved visual and ergonomic reference (R155–R156), incorporating R143–R154. The approved baseline is Clinical Teal #0F766E, selection #ECF8F5, canvas #F4F6F8 and Inter 4.1 with the verified disambiguation/numeric features. This is design approval, not product implementation, a complete brand kit or clinical/accessibility certification. Remaining token roles, logo, low-light palette and production asset delivery remain Open. Keep documentation local until explicitly requested to commit/push.

## Approved reference baseline

The owner explicitly approved the final reference on 16 October 2026. Preserve the [approved browser reference snapshot](reference/visual/2026-10-16-approved-teal-reference.html) and its [integrity/approval receipt](reference/visual/2026-10-16-approval.json). [Snapshot scope and use](reference/visual/README.md) distinguish this synthetic design artifact from application code. Earlier comparisons and intermediate review artifacts remain historical evidence, not alternative current standards.

| Role | Approved choice |
| --- | --- |
| Brand and primary action accent | Clinical Teal `#0F766E` |
| Soft selection surface | `#ECF8F5` |
| Main canvas | `#F4F6F8` |
| Typeface | Inter 4.1, full verified InterVariable font file |
| Disambiguation features | `cv05` 1, `cv08` 1, `zero` 1 |
| Aligned numeric presentation | `tnum` 1 / tabular digits |
| Numerical locale in the Dutch reference | Decimal commas, including `1,05 mg / 10 ml` |

The sole shell patient header, grouped demographic metadata, upper-right draft status, adjacent alert badges, birth-date/close tabs, persistent background dirty markers, stable desktop two-column layout and explicit in-card empty states are the approved ergonomic reference. Keep notes bounded in width; apply responsive stacking consistently across patients. Do not reintroduce identity bars in module cards or turn an empty dataset into a confirmed negative clinical finding.

Retain the table's right-aligned numeric values and matching column heading, with left-aligned adjacent units and restrained horizontal spacing. Do not introduce a spacing adjustment solely for the final non-blocking optical observation. Equal-width digits and right alignment do not by themselves establish decimal-point alignment across mixed formats or precision.

The snapshot's default 13 px study setting and 12–14 px controls preserve the approved specimen for comparison, not a universal mandate for every UI element/device. Additional components, alert semantics, supported hardware, accessibility target, exact remaining tokens and full responsive behavior still require specification/validation. The module SDK is not implemented or published by this approval. Later intentional visual changes require an identifiable revision and review; preserve this dated baseline.

## Accepted character

Use a calm, clear, professional visual language with compact clinical information presentation. Favor light neutral surfaces, highly readable sans-serif typography, restrained structure and a subtle recognizable shell accent. Tables and forms should expose useful information without unnecessary scrolling, oversized decoration or excessive nested cards.

Retain enough hierarchy, grouping and space to distinguish patient identity, content, actions and clinical status. Compactness is not permission to shrink essential text, truncate critical meaning or crowd controls. The owner's cognitive/ergonomic rationale informs design intent; it is not comparative usability evidence that one palette is universally safest or that rounded/warm alternatives inherently cause clinical errors.

## Surface and color roles

Use the approved #F4F6F8 cool-neutral canvas and #ECF8F5 selection surface with Clinical Teal #0F766E, keeping the reference's white working panels and clear shell/content boundary. Exact remaining token values and full contrast/state coverage remain Open. Avoid relying on subtle background differences alone to communicate grouping, selection or state. Define glare/contrast behavior through representative screen and lighting evaluation rather than claiming the palette solves hardware variation.

Separate brand accent, interactive selection/focus and semantic clinical status tokens. The owner selected Clinical Teal #0F766E as the brand accent after reviewing the comparison. Indigo remains a historical alternative; the earlier carousel selection of Deep Indigo was not a final brand decision and is superseded by the explicit written choice (R149). Red/amber warning roles and blue/teal selection illustrate the intended distinction, not a finalized severity taxonomy. A recorded allergy is not automatically the highest-severity active alert. Clinical severity, urgency and acknowledgment rules need their own reviewed meaning, independent of brand preference.

Use text, symbols and structure alongside color. A brand accent must not make ordinary selection look like a warning or conceal keyboard focus. Carry shared token roles consistently into custom SVG/canvas components under the [accepted UI contract](shared-ui-design.md#semantic-tokens-scoped-styling-and-accessibility).

## Typography and numeric clarity

Inter 4.1 is the approved sans-serif UI family (R156). The selected full InterVariable font file has verified cv05/cv08/zero/tnum support; Plus Jakarta Sans remains comparison history. Preserve the actual file/feature identity when packaging. Language coverage, font delivery/licensing, fallback behavior and additional supported weights still need production assessment; approval is not a claim that every substitute subset or platform rendering is equivalent.

Require stable tabular numeric presentation for comparable values, dosages, times and identifiers where applicable. [CSS numeric variants](https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/font-variant-numeric) distinguish equal-width digits (`tabular-nums`) from optional forms such as `slashed-zero`. Equal-width digits are not a requirement that every character be monospace, and do not guarantee distinction between `1`, `I`, `l`, `0` and `O`. Glyph discrimination is a separate selection/test criterion; a suitable identifier/monospace style may be used where needed.

Keep units, signs, decimals, dates, time zones and clinically relevant precision explicit and consistently aligned. Numerical presentation follows appropriate localization and owner-defined meaning without changing stored identifiers or introducing ambiguous automatic formatting. Test actual dose/value strings and patient identifiers, not only a font specimen. No font makes misreading impossible.

## Density and shape

Use compact tables/forms, restrained separators and modest corner rounding. The owner's 4–6 px radius range is a starting design reference, not a fixed value for every element. Exact line heights, padding, row heights, type sizes, breakpoints and numeric tokens require visual/interaction review.

Preserve meaningful grouping, readable validation, keyboard focus and usable pointer/touch targets on supported workstations and COW/laptop devices. Visual row density and the effective control hit area can differ. [W3C target-size guidance](https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum.html) makes size/spacing a separate accessibility concern; exact supported accessibility target remains Open and is not established by this brief. Any density preferences must preserve the same safety/context information rather than hide it to fit more rows.

## Token and theme implications

The shared SDK defines semantic surface, text, border, action, selection/focus, status, spacing, typography, radius and numeric-presentation roles. Modules consume those roles under R133 rather than introducing independent palettes or hardcoded replacements. This brief does not publish an exact token inventory or CSS implementation.

The light palette is the current foundation. A low-light/dark palette is a later design requirement through the same token roles, not an implemented feature or a simple color inversion. Review clinical status distinctions, contrast, charts, imagery, focus and theme transitions separately for that palette. No rollout date, automatic shift switching or user/device preference mechanism is selected.

## Remaining review and scope

Use the approved reference for subsequent component/interaction studies rather than reopening the settled accent/type/surface choice without a concrete reason. Assess patient identity, dense numeric tables, action hierarchy, warnings, draft state, dialogs and a specialized chart at realistic display sizes and zoom. Include keyboard, contrast and relevant low-quality/touch displays before claiming usability or safety. Such visual studies are design artifacts, not authorization to build the product.

Brand accent, selection surface, canvas and Inter 4.1 typography are now selected under R155–R156. Logo/wordmark, remaining exact tokens and low-light palette remain Open. Patient-portal/public-website styling and identity may share the brand but have not been specified by the clinical-workplace choice. R143–R146 began as direction and constraints; R155–R156 approve the dated reference after the documented prototype iterations. Prototype checks exist, while no product implementation or clinical validation is established.

## First comparative visual study

The owner requested identical compact patient-workspace layouts for Clinical Teal and Deep Indigo (R147–R148). A local conversation-only interactive study now exists outside the repository. It is a synthetic design artifact, not a product application or authorization to implement one. It compares palette while holding layout, font, example content and interactions constant. At the earlier foundation-brief checkpoint no prototype existed; this later study does not establish clinical usability or production behavior.

| Candidate role | Clinical Teal | Deep Indigo |
| --- | --- | --- |
| Brand/action accent | `#0F766E` | `#4F46E5` |
| Soft selection surface | `#ECF8F5` | `#F0F1FF` |
| Main canvas | `#F4F6F8` | `#F4F6F8` |
| Working surface | `#FFFFFF` | `#FFFFFF` |
| Primary text | `#172B3A` | `#172B3A` |
| Secondary text | `#526273` | `#526273` |

Shared sample status roles are red `#B42318` on `#FEF3F2`, amber `#92400E` on `#FFFBEB`, green `#166534` on `#F0FDF4` and informational blue `#1D4ED8` on `#EFF6FF`. These are study values; Teal #0F766E is subsequently accepted as the brand accent in R149. Selection #ECF8F5 and canvas #F4F6F8 are now approved in R155; the remaining study values are not a complete finalized token palette or proof that every UI contrast/state passes accessibility review.

Use Inter 4.1 as the common study font, from the full version-pinned `InterVariable.woff2` file in the official Inter repository. Inspection of that actual file confirms GSUB features `tnum`, `zero`, `cv05`, `cv08` and `ss02`; the tested compact Fontsource Latin subset did not include the requested disambiguation features. Do not infer feature availability from the family name. [Inter's feature documentation](https://rsms.me/inter/#features) describes the available alternatives. The study activates targeted `cv05` (lowercase l), `cv08` (uppercase I) and `zero` (slashed zero), with `tnum`; broader `ss02` is available but not silently substituted for the targeted set.

R147 requires checking the shipped font file/subset, supported combinations and actual rendered results, not simply setting arbitrary feature tags. Feature identifiers are font/version-specific; no impossible-to-confuse guarantee follows. Include `10I1lO0-8B` and `1.05 mg / 10 ml` in patient identity and medication typography tests at 12, 13 and 14 px, with feature on/off comparison. The mixed-character string is a synthetic patient/batch identifier, not a valid numeric BSN. The dose string is typographic test content with a literal decimal point, not a prescription or finalized localization convention.

The study includes multiple patient tabs, a persistent patient header, explicit allergy/isolation indicators, numeric lab/vital and fictitious medication tables, concept dirty/saved states, note input and distinct warning/blocking-error examples. Saving is explicitly demo-only and has no backend/patient connection. Finalization remains disabled. Browser interaction checks assess only this study; they are not verification of any Zantrix clinical, authorization, draft-durability or session implementation. The owner subsequently chose the Teal accent (R149); the subsequent final approval in R156 also selects Inter 4.1 with the tested features.

## Owner review and revised Teal study

The owner explicitly selected Clinical Teal #0F766E and requested corrections to draft feedback, lab rendering, patient-scoped warnings, neutral absence/status presentation and shell text selection. The pasted assessment refers to six screenshots, but only its text was attached. Do not claim direct inspection of those six images.

The original local preview did respond to real keyboard input on Sam's draft and contained all six lab names/values when checked in the browser. The exact cause of the owner's reported stale-state/blank-cell behavior has not been reproduced or established. Other issues were directly present: static medication/finalization notices on every patient, a blue isolation badge, missing close controls and a large empty left column.

A separate revised conversation-only Teal study preserves the first comparison and now uses an explicit measurement dataset/row renderer, delegated draft events and capture before navigation/close/settings updates. Dirty state compares current text against the last demo-saved text and updates the header, tab marker, badge and Save control together. These changes and checks improve the study; they do not prove the original host-specific cause has been fixed or establish real server-side persistence.

Patient warnings now depend on that patient's example data. No record is distinguished from an assessed negative finding; unknown isolation status is labeled not assessed. Non-alert isolation status uses a neutral treatment. Empty notes have neutral guidance instead of a preemptive large red finalization block; genuine warning/error semantics remain independently defined. Empty patient examples expand the note panel rather than reserve an unused overview column. Shell controls/chrome suppress accidental text selection while identifiers, clinical values and note text remain selectable.

At this intermediate checkpoint the revised study proposed full birth dates in tabs and separate close buttons. The owner subsequently accepted them in R151 and approved the final reference in R155. Birth date is an additional cue, not guaranteed unique identity. Closing a dirty tab requires an explicit save-and-close, discard-and-close or continue choice; the named patient remains clear. Actual server durability, authority, shutdown/recovery and lost connection handling are not implemented by this study. All demo state remains volatile.

Local browser verification covered typing on an initially empty note, all four dirty indicators, redirty after saving, patient isolation, tab-switch preservation, explicit close cancel/save/discard, reopening saved demo state, and capturing a changed field before switching even if no input event was delivered. Complete lab names/values and filled/empty layouts were inspected. The six original reported screenshots and the live inline host were not independently verified, so keep that evidence boundary visible.

## Independent review and follow-up corrections

An independent reviewer confirmed two P2 faults in the revised study: patient identity scrolled out of view while editing at 390 × 844, and closing/canceling left keyboard focus on hidden action buttons. The owner requested corrections and a new independent reviewer. Preserve the original study and review evidence outside the repository.

The corrected study keeps only the compact name/birth-date/patient-ID banner sticky, with demographics, save state and alerts outside that sticky region. The same shell-bound identity is shown adjacent to the note and its save action. Focus is explicitly restored after cancel/Escape, ordinary Save, clean/save/discard close and reopen, including closing a background patient's tab. Cancel restores the current patient's editor and caret; closing restores a visible remaining workspace or reopen control.

The new reviewer also found an oversized sticky banner at 200% CSS zoom and missing context during parent scrolling of a tall iframe. The compact banner removes the tested overlap. An IntersectionObserver-based presentation guard checks the visible identity zones, including iframe clipping: if none is sufficiently visible, note entry becomes read-only and Save is disabled without clearing its text; visible identity restores operation. The guard notice retains layout space to avoid moving observed zones when it appears/disappears. This is a Proposed study mechanism, not a chosen production security or authorization boundary; observer delivery is asynchronous.

The new reviewer independently retested the final correction in standalone Chromium and a local iframe wrapper. Both original faults and the additional zoom/parent-scroll reproductions were resolved within those stabilized test states, with no new blocking finding. Checks included actual continuation typing after cancel without refocusing, all close outcomes, saved/unsaved patient isolation, long/resized notes, settings changes and preservation/recovery of text while context is outside view. CSS zoom is a stress test, not proof of formal browser zoom/accessibility conformance. The actual inline host/Tweak integration remains unverified.

The external `independent-review-round2-final.md` and separately named `round2-final-*` artifacts contain final evidence. The reviewer reported reusing some intermediate `round2-independent-*` outputs; the first round-2 report retains its initial measurements, but those intermediate images/results must not be presented as unchanged initial-failure evidence. Round-1 evidence remains preserved. No application functionality, clinical readiness or runtime security proof is established by these mockup corrections.

## Single header and stable workspace refinement

Owner review recorded 16 October 2026 (R151–R154). The owner accepts the Teal direction, neutral status treatment and patient tabs with birth dates/close controls, and rejects duplicate patient-identity bars inside the note card and above its buttons. The later direction supersedes the earlier context-duplication workaround and expansion of empty notes across the page; earlier review artifacts remain historical evidence.

The new conversation-only reference study uses one shell-owned identity component. Name, birth date/age/sex and patient ID are grouped, save state is at the upper right, and alert/status badges follow directly without an internal divider. Only the identity core sticks while scrolling; alerts follow in the same borderless visual grouping. There are no module-owned identity bars. Birth dates are additional cues and do not guarantee unique identification or eliminate wrong-patient risk.

At desktop width, every patient retains the same two-column grid: lab and medication cards on the left, bounded note editor on the right. Patients without supplied demo content have explicit compact empty states inside those same cards. Their note card does not jump sideways or stretch across the workspace. Narrow layouts consistently stack the same components. The empty-state text says no data is included in this demo; it must not assert confirmed absence of medication or normal results from missing data.

Dutch study values and both glyph-test lines now use `1,05 mg / 10 ml`, matching the other comma-formatted values. The earlier dot-form test string is retained only in historical comparison artifacts/text. Unflagged result cells are visually blank, with an accessible no-signal label rather than an assertion of clinical normality. No reference ranges or clinical validity are established by the synthetic values; Testpreparaat Alfa/Beta are fictitious, not real approved products.

The context observer now watches only the sole shell identity core. In a tall embedded frame, if that identity is outside the visible area, editing and Save pause while text is preserved; returning the shell identity restores operation. Removing duplicate labels therefore does not silently permit edits with no visible patient identity. The guard message is shown only when needed; its geometry is below the sole observed header. This remains an asynchronous presentation mechanism and not server authorization or proof of real inline-host behavior.

Local Chromium verification confirmed equal note-card x/y/width across Mara, Sam and Noor at the desktop test size, all four dirty indicators after actual typing in initially empty Sam, redirty after Save, continued typing after close cancellation, patient isolation, complete named lab rows, empty signal cells and comma-formatted dose specimens. At 390 × 844 with 14 px text and CSS zoom 200%, the sticky core did not cover the editor. A sandboxed local iframe test confirmed that losing the sole identity pauses input/Save without changing the text, and restoring visible identity resumes it. This is a new reference study, not the exact artifact reviewed in the prior independent round; no renewed independent-review or actual-host pass is claimed for it.
