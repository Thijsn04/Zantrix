# Zantrix EPD - Coding Guidelines & Best Practices

Dit document beschrijft de standaarden en richtlijnen voor alle code die wordt geschreven voor het Zantrix EPD. Aangezien Zantrix een open-source medisch systeem is, is de hoogste mate van leesbaarheid, veiligheid en stabiliteit vereist.

## 1. Algemene Principes
- **Kwaliteit boven Snelheid:** Zantrix is een medisch systeem (MDR/NEN7510-context). We accepteren geen 'quick fixes' of tijdelijke hacks in de main codebase.
- **Leesbaarheid:** Code wordt vaker gelezen dan geschreven. Schrijf code alsof de volgende persoon die het leest een psychopaat is die weet waar je woont. Gebruik duidelijke, beschrijvende namen voor variabelen en methodes.
- **Taal:** De voertaal voor code, variabelen, comments, commits en Pull Requests is Engels. (Domeintermen die strikt Nederlands zijn vanwege wetgeving, zoals BSN, mogen behouden blijven met een Engelse omschrijving).
- **DRY & SOLID:** Hanteer *Don't Repeat Yourself* en de SOLID principes voor objectgeoriënteerd programmeren.

## 2. Backend Guidelines (Java & Spring Boot)
De backend is het hart van Zantrix en beheert alle medische logica, gebouwd in Java 21+ met Spring Boot.

- **Architectuur (Modulaire Monoliet):**
  - Maak gebruik van Spring Modulith. Domeinen (bijv. `patient`, `billing`, `scheduling`) moeten strikt gescheiden zijn in eigen packages.
  - Modules mogen niet direct in elkaars databases of interne services lezen. Communicatie tussen modules gebeurt via interne events of strikt gedefinieerde interfaces.
- **HAPI FHIR Integratie:**
  - Gebruik de HAPI FHIR library voor alle datamodellen die naar buiten gaan.
  - Interne domeinmodellen (Entities) en FHIR-resources moeten gescheiden blijven. Gebruik mappers (bijv. MapStruct) om interne Entities om te zetten naar FHIR Resources.
- **Foutafhandeling:**
  - Geef nooit ruwe stacktraces terug aan de API. Gebruik een globale `@ControllerAdvice` in Spring om exceptions op te vangen en gestandaardiseerde, veilige JSON-foutmeldingen terug te sturen.

## 3. Frontend Guidelines (React & TypeScript)
De frontend levert de taak-gedreven interface voor het zorgpersoneel, gebouwd in React met TypeScript via Vite.

- **Strict TypeScript:**
  - Het gebruik van het type `any` is STRIKT VERBODEN. Medische data vereist 100% type safety. Definieer interfaces voor alle API-responses.
- **Component Structuur:**
  - Gebruik uitsluitend Functional Components met React Hooks. Geen Class Components.
  - Houd componenten klein en gefocust op één taak (Single Responsibility).
- **Meertaligheid (i18n):**
  - Alle voor de gebruiker zichtbare teksten moeten via `react-i18next` vertaald worden. Hardcoded Nederlandse of Engelse zinnen in de DOM zijn niet toegestaan.
- **Styling (Vanilla CSS):**
  - We gebruiken pure, scoped Vanilla CSS of CSS Modules. Frameworks zoals Tailwind of Bootstrap zijn VERBODEN.
  - Gebruik centraal gedefinieerde CSS variabelen (`--primary-blue`, `--bg-color`) uit `index.css` om uniformiteit te waarborgen.

## 4. Database & API (PostgreSQL & REST)
- **Database Migraties:**
  - Geen handmatige SQL-scripts op de database. Alle database-wijzigingen moeten verlopen via Flyway of Liquibase. Migratiescripts moeten in Git staan ter versiebeheer.
- **JSONB in PostgreSQL:**
  - Gebruik relationele tabellen voor harde zoekcriteria (patiënt ID, geboortedatum) en `JSONB` kolommen voor diep-geneste, flexibele FHIR-documenten (zoals de ruwe observatie-data).
- **API Naming Conventions:**
  - REST API endpoints gebruiken kebab-case en zijn in meervoud.
  - ✅ `GET /api/v1/patients` (Goed)
  - ❌ `GET /api/v1/getPatientInfo` (Fout)

## 5. Security & Privacy (CRUCIAAL)
Omdat dit een EPD is, geldt 'Security by Design'.

- **Nooit PHI in logbestanden:** PHI (Protected Health Information) zoals namen, BSN-nummers of medische condities mogen NOOIT in de standaard applicatielogs (zoals de console of log-bestanden) worden geprint.
- **NEN7510 Audit Trail (Gerealiseerd):** Elke lezende of schrijvende actie naar de database wordt via Spring AOP (Aspect-Oriented Programming) cryptografisch vastgelegd, inclusief Hash-Chaining, IP adres, medewerker en de gerelateerde patiënt.
- **Geen Hardcoded Secrets:** Wachtwoorden, API keys en database-credentials mogen nooit in de code staan. Gebruik `.env` bestanden.

## 6. Git & Version Control
- **Branching Model:** We gebruiken een variant op GitHub Flow. `main` is altijd productie-klaar. Ontwikkeling gebeurt op `feature/naam-van-feature` of `bugfix/beschrijving`.
- **Commit Messages:** Gebruik Conventional Commits.
  - `feat(patient): add search by BSN`
  - `fix(auth): resolve token expiration bug`
  - `docs(readme): update setup instructions`
- **Pull Requests (PR's):** Een PR mag pas gemerged worden als de CI/CD pijplijn succesvol draait en de code is gevalideerd.
