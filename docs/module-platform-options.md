# Module platform: accepted direction and compared options

Date: 15 October 2026. Status: option A is accepted as architectural direction, including the owner's SDK/BOM, declared-library, PropertiesLauncher/preflight and native import-map choices. Concrete versions, package layout, enforcement implementation and runtime proof remain incomplete. No product code, published BOM, dependency installation or runtime proof is included. Commit/push only on explicit owner request. See R105–R110 in the [decision register](decisions.md).

## Purpose and fixed constraints

Choose how separately built module packages become one coherent Zantrix application. The accepted basis is Java/Spring Boot, React/TypeScript, PostgreSQL and a modular monolith. Core is technical, Bridge is internal interaction and Connect is external interoperability. Modules have separate versions, explicit data ownership and typed contracts. All modules use the supported stack and shared experience.

Technical administrators can install packages from an official catalog or local files without recompiling Zantrix. Planned maintenance/restart is acceptable. Managed and self-hosted installations are supported; normal operation cannot require a live vendor catalog. Compatibility, clinical migration approval and usable retirement remain required. Exact framework versions, build tooling and archive layout remain open; the selected startup and frontend mechanisms below constrain that work.

## Accepted direction: controlled startup with a shared platform baseline

The owner approved option A for the initial platform. A module is built by its developer against published Zantrix APIs and platform dependencies. An administrator installs its already-built artifacts. The deployment process composes an approved artifact set and runtime configuration; it does not compile source or rebuild the host frontend. Acceptance of this direction does not establish that the package/frontend proof has succeeded.

The key trade-off is deliberate dependency discipline: modules cannot bring arbitrary conflicting versions of shared frameworks. A library conflict may require a new compatible module build or a coordinated platform upgrade. This is compatible with independent module versions, but not with a promise that any pair of independently released packages will work together.

## Backend alternatives

| Option | Mechanism | Benefit | Cost and limit |
| --- | --- | --- | --- |
| A — shared startup dependencies, selected | Stage prebuilt module JARs and approved dependencies; start one backend with a resolved shared classpath and explicit module registration | Fewer class-identity/container boundaries; one supported Spring baseline; fits controlled restarts and one initial maintainer | Shared library conflicts must be rejected or resolved before activation; package/module boundaries are not JVM isolation |
| B — per-module class loaders, not selected | Use an established plugin framework such as PF4J, with a defined shared SDK and Spring integration; still activate only during controlled restart | More separation of private module libraries and existing plugin lifecycle/discovery facilities | Additional class identity, dependency delegation, Spring registration, persistence/transaction integration and diagnostics to design/test; not a security sandbox or separate failure boundary |

These were two ways to implement the accepted modular monolith, not a proposal to return to one service per module. Under B, shared contract types would need consistent resolution across host/plugin boundaries. No PF4J/per-module-class-loader implementation is selected or tested.

Option A does not mean editing Core to add a known module list. A bootstrap registration contract must discover approved installed module descriptors, establish an acyclic dependency/contract graph and register module contributions explicitly. Backend classes share a process and trust boundary. Architecture verification and permissions reduce errors; they do not sandbox malicious installed code or prove table ownership enforcement.

## Verified building blocks and evidence limits

Official documentation was consulted on 15 October 2026. Its published versions are evidence about mechanisms, not selected Zantrix dependency versions.

- [Spring Boot PropertiesLauncher](https://docs.spring.io/spring-boot/specification/executable-jar/property-launcher.html) supports additional JAR/classpath locations through `loader.path`. It is now selected as a bootstrap building block for A; it does not supply Zantrix dependency solving, review, installation or lifecycle contracts.
- [Spring Boot executable-JAR restrictions](https://docs.spring.io/spring-boot/specification/executable-jar/restrictions.html) describe class-loader constraints that a proof must account for. No launch configuration has been validated for Zantrix.
- [PF4J class loading](https://pf4j.org/doc/class-loading.html) documents separate plugin class loaders by default and configurable delegation. These support B's library-separation mechanism, not a claim of hostile-code isolation or complete Spring integration.
- [Spring Modulith verification](https://docs.spring.io/spring-modulith/reference/verification.html) checks module graph cycles and permitted API/dependency access. It remains a candidate architecture-verification tool rather than the runtime package installer. Applicability to the selected external-module arrangement still needs proof.

The assessment behind A was an architectural inference from Zantrix's constraints and the additional integration obligations of B. The owner subsequently approved A. Neither the assessment nor approval is a benchmark or demonstrated reliability result.

## SDK, BOM and release-train dependency policy

Publish an official SDK and `zantrix-platform-bom` tied to a specific platform release train. The owner's `2026.2` is illustrative, not an existing or selected release. A Maven BOM can also be consumed by Gradle; selecting the repository build tool and concrete dependency versions remains separate work.

Shared frameworks and APIs, including Spring Boot, Jackson, SLF4J and any selected shared persistence runtime, come from the platform. Modules declare them as host-provided compile-time dependencies and must not bundle another copy. Maven uses `provided`; Gradle normally uses `compileOnly`, or `compileOnlyApi` where a published API exposes those types. Test execution needs a matching platform test fixture/runtime; compile-only dependencies do not supply that runtime themselves.

These scopes and a BOM are not runtime enforcement. Build/conformance controls must inspect the resolved dependency graph and actual packaged contents. Staging must reject conflicting, undeclared or bundled protected libraries, including nested/shaded copies where detectable. A trusted platform artifact inventory binds supported versions to expected artifact hashes; the raw BOM alone is not a signed runtime file inventory or proof of behavioral compatibility.

An additional calculation/library dependency must be declared in the module manifest, including transitive contents and artifact identity needed for validation. Staging checks it against the whole deployment set for duplicate/conflicting classes, coordinates and relevant provider/resources. In the selected shared classpath, a so-called private library is not actually isolated from other modules. Conflict treatment and controlled relocation exceptions still need policy; do not silently accept first-JAR-wins behavior.

References: [Maven dependency management and scopes](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html), [Gradle library configurations](https://docs.gradle.org/current/userguide/java_library_plugin.html) and [Gradle platform consumption](https://docs.gradle.org/current/userguide/platforms.html). These mechanisms support the policy; no SDK, BOM or enforcement plugin has yet been built or published.

## Selected backend bootstrap

Use Spring Boot `PropertiesLauncher` with `loader.path`, preceded by a strict preflight CLI/supervisor. It checks manifests, artifact hashes, an acyclic resolved module-dependency graph (including activated optional dependency edges) and exact platform-provided artifact agreement before starting the clinical application JVM or executing installed module code. The validator itself may have a separate runtime; the requirement does not ban a JVM-based CLI.

The supervisor launches only the validated, fixed deployment set. Loader paths, environment settings and filesystem writes must not permit substitution or extra unvalidated JARs between preflight and launch. Artifact hashes need a trusted expected inventory; checking a checksum supplied alongside an untrusted replacement is not authenticity verification. Trust/signing/key distribution and the concrete supervisor implementation remain open. Clinical/schema readiness is still checked after application initialization; preflight does not prove it.

## Frontend requirement shared by both options

A separately installed backend JAR does not automatically add React screens. Each applicable module must provide prebuilt UI contributions against a supported frontend contract. The shared shell owns navigation, patient workspaces, context, routing coordination, design components, accessibility and save/error conventions. Contributions register at explicit extension points; they do not create independent application shells.

Serve installed, versioned UI assets from the institution's deployment. Backend code, UI assets, contract versions and active configuration belong to one identified deployment set. The browser loads contributions through a controlled registry of that set, with shared React/renderer and SDK/component instances where required. It must not silently mix an old host with newly activated module assets. Exact cache/session transition and unsaved-draft handling need design.

Native browser ES modules and import maps are selected. Webpack Module Federation was compared but is not the chosen composition mechanism. Developers can choose compatible build tooling, provided their output follows the supported ESM/SDK contract and externalizes platform-provided dependencies. [React's duplicate-runtime guidance](https://react.dev/warnings/invalid-hook-call-warning) explains why module and renderer imports must resolve consistently.

The backend generates a deployment-specific JSON map that the host places in an import-map script before application imports. It binds React, React DOM, their required subpath imports and `@zantrix/ui-sdk` to canonical versioned local ESM URLs. Within a browser document, compatible imports resolving to the same URL/type share the evaluated module; this is not a cross-tab process singleton or merely an HTTP-cache guarantee. See the [HTML import-map standard](https://html.spec.whatwg.org/multipage/webappapis.html#import-maps).

Import maps do not prevent a package from embedding React or using alternate URLs/scoped overrides. SDK/build validation must reject such bypasses for protected dependencies, and module code remains trusted code. Exact browser baseline, CSP, asset integrity and subpath inventory remain to be specified. Existing browser documents keep their deployment mapping; do not attempt to swap a resolved module underneath an active patient workspace.

Both backend options still need this frontend contract. Ordinary bundling that requires recompiling the host whenever a module is added does not meet the accepted installation requirement. Runtime-loaded UI code in the same page is also trusted installed code; a shared shell is not a browser security sandbox.

## Proposed package and activation boundary

A conceptual package supplies a manifest, prebuilt backend/UI artifacts where applicable, declared dependencies/contracts, permissions, translations, migrations and lifecycle documentation. Exact archive layout, signatures and schema are still open. Data-changing migrations retain the separately agreed trial/review/approval requirement.

Stage a candidate combination without activating it. Validate artifact identity/integrity and supported contracts, module and shared-library dependencies, expected frontend contributions and applicable schema/migration requirements. Technical loading success alone does not authorize clinical use or prove every combination works. Dependency ranges need tested compatibility evidence.

Activate one explicit deployment set during authorized maintenance. The administration path needs an update/restart mechanism that remains operable while the primary application is stopped; its concrete supervision/deployment mechanism is an open design question. Do not assume the clinical application's own running process can replace/restart itself safely.

After controlled data changes, initialize the selected modules and release clinical access only after the required checks. A failed module must not silently create a partially operational combination missing a required capability. An administrative recovery route must remain possible; a process failure does not automatically authorize restoring an old database or discarding recorded clinical work. Exact failure states and degraded-operation policy are still open.

## Proof obligations before implementation commitment

After the relevant design and probe are approved, verify the chosen mechanism with a small synthetic vertical slice:

- Install a separately built backend/UI module into an unchanged host, without source compilation on the institution's system.
- Reject incompatible SDK/framework or required-module combinations with an understandable error.
- Use the shared shell, renderer and patient context through documented extension points.
- Preserve immutable/versioned asset references across restart; identify stale browser sessions without silently losing drafts.
- Exercise a failed activation and distinguish application rollback from database recovery.
- Demonstrate declared registration boundaries and architecture checks for external as well as official modules.

No probe has been run. The startup mechanism and native frontend composition direction are now selected; exact registration/transaction/persistence integration, authenticity verification, browser policy and supervisor implementation still need review and proof.

## Review outcome and next section

The owner approved A, the SDK/BOM and manifest policy, PropertiesLauncher/preflight and native import maps. The subsequent [interaction and data section](core-bridge-data-design.md) was then separately accepted and refined in R111–R118; its exact implementation and outbox storage layout still need design. Do not ask for either accepted direction again. Continue to identity/session/context authorization.

Visual presentation and branding follow in a separate design discussion. This proposal only defines where shared interface responsibilities live; it selects no visual identity or product layout.
