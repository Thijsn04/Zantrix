# Zantrix EPD - Masterplan & Fundament

Dit document beschrijft de visie, architectuur en module-opbouw van het open-source Zantrix Elektronisch Patiëntendossier (EPD). Zantrix is ontworpen als een schaalbaar, veilig en modern alternatief voor systemen zoals EPIC en HiX.

## 1. Visie & Doelstellingen
- **Open-Source & Transparant:** Zantrix brengt controle terug naar de zorginstelling en doorbreekt vendor lock-in.
- **100% Standaardisatie:** Volledige adoptie van internationale zorgstandaarden (HL7 FHIR, SNOMED, DICOM). Geen gesloten, eigen datamodellen.
- **Modulair & Schaalbaar:** Te gebruiken door zowel kleine zelfstandige behandelcentra (ZBC's) als grote academische ziekenhuizen door het aan- en uitzetten van specifieke modules.
- **Gebruiksvriendelijk:** Focust op 'taak-gedreven' interfaces om de registratielast voor zorgpersoneel drastisch te verminderen.

## 2. Architectuur & Technologie
Het systeem is ontworpen om uiterst stabiel en veilig te zijn, met bewezen enterprise-technologieën.

- **Architectuurstijl:** Modulaire Monoliet. Eén applicatie met strikt gescheiden interne domeinen/modules. Dit garandeert snelheid in de beginfase, met de mogelijkheid om later op te splitsen in microservices indien nodig.
- **Backend:** Java met Spring Boot 3. Bewezen, veilig en robuust. Dit maakt ook integratie met de HAPI FHIR library mogelijk.
- **Frontend:** React met TypeScript (Vite). Strikte foutcontrole, Tailwind CSS, Volledige Light/Dark Mode integratie, PWA-compatibel, Meertalig via `react-i18next`.
- **API-First:** De frontend en backend communiceren uitsluitend via REST/GraphQL API's. Hierdoor is het systeem direct klaar voor externe koppelingen.
- **Hosting & Distributie (Backend):** Docker Containers.
- **Client Distributie (Toekomst):** Native desktop applicatie via Electron voor geavanceerde hardware-integratie (UZI-pas) en strikte sneltoets-controle. Zie [desktop-architecture.md](desktop-architecture.md) voor de volledige uitrolstrategie.

## 3. Data & Opslag
Medische data is complex en sterk genest. Zantrix is "FHIR-native", wat betekent dat de interne structuur de HL7 FHIR datamodellen volgt.

- **Primaire Database:** PostgreSQL.
- **Waarom:** Het is de meest stabiele en geavanceerde open-source relationele database ter wereld. Dankzij de `JSONB`-ondersteuning in PostgreSQL kunnen we de flexibiliteit van medische documenten (zoals FHIR-resources) combineren met de harde garanties (ACID) van relationele databases voor financiële data en logboekregistraties.

## 4. Zorgstandaarden & Interoperabiliteit
Zantrix praat standaard de taal van de medische wereld.
- **Data-uitwisseling:** HL7 FHIR (als kern), aangevuld met traditionele HL7v2.
- **Terminologie:** Native ondersteuning voor SNOMED CT, ICD-10, en de Nederlandse Diagnosethesaurus.
- **Beeldvorming:** DICOM-standaard voor directe integratie met PACS-systemen.
- **Nederlandse Lokalisatie:** Zantrix ondersteunt "Adapters" voor specifieke nationale systemen zoals het LSP (Landelijk Schakelpunt) en ZorgDomein.

## 5. Security & Compliance
Beveiliging is geen add-on, maar is ingebouwd in het fundament van het systeem, met het oog op NEN7510 / ISO27001 certificering.

- **RBAC (Role-Based Access Control):** Een uiterst fijnmazig rechtensysteem via OAuth2 (Keycloak). Toegang wordt bepaald op basis van de rol van de gebruiker.
- **Break-the-Glass:** Een noodprocedure (via JWT-manipulatie) op de Spoedeisende Hulp (SEH). Artsen kunnen tijdelijk hun rechten verhogen, wat onmiddellijk een escalatie en achteraf verantwoording vereist.
- **Onveranderlijke Audit Trail:** Elke actie wordt cryptografisch vastgelegd via Spring AOP in een auditlog (inclusief previous-hash, current-hash, IP-adres en patiënt-ID).
- **Authenticatie:** Standaard 2FA (Two-Factor Authentication) en Single Sign-On (SSO).

## 6. Open-Source Licentie & Model
- **Licentie:** AGPLv3 (GNU Affero General Public License v3.0). Dit beschermt het project. Commerciële IT-bedrijven zijn verplicht verbeteringen terug te geven aan de community.
- **Verdienmodel:** Inkomsten via betaalde supportcontracten, managed cloud-hosting, of implementatietrajecten.

## 7. Zantrix Module Overzicht (MVP Fundament)
Zantrix wordt in fases gebouwd. De modules aangeduid met `[MVP]` behoren tot de absolute kern.
1. **[MVP] Core Identity & Security:** Inloggen, rechtenbeheer (RBAC), onveranderlijke audit logging en 2FA. *(Voltooid: Module 1.1 & 1.3)*
2. **[MVP] PMI (Patient Master Index):** Het centrale patiëntenregister. Demografische gegevens (NAW, BSN). *(Nu in ontwikkeling: Module 1.2)*
3. **[MVP] Scheduling & Resource Planning:** Agenda's voor artsen, ruimtes en afspraken.
4. **[MVP] Clinical EHR (Het Dossier):** Vastleggen van het medisch consult (SOAP).
5. **[MVP] CPOE (Computerized Physician Order Entry):** Elektronisch voorschrijven van medicatie.

## 8. Huidige Implementatie Status (Gerealiseerd)
Op dit moment zijn **Module 1.1 (IAM & Security)**, **Module 1.2 (Patient Master Index - PMI)**, **Module 1.3 (Audit & Compliance)**, **Module 1.4 (Terminology Server)** en **Module 2.1 (Ambulatory Scheduling)** succesvol ontworpen en gebouwd. De technische basis (het fundament) staat robuust overeind:

- **Frontend (PWA Shell):** Een React/TypeScript applicatie met Vite, ingericht als Native App-ervaring. Gerealiseerd met Tailwind CSS voor dynamische Dark Mode en superieure performance.
- **Globale Zoekbalk & Navigatie:** Een app-brede, debounced live-search dropdown is actief in de menubalk, waarmee gebruikers naadloos en direct patiëntendossiers (als nieuwe in-app tabbladen) in de hoofdmodule kunnen openen via slimme deep-linking URL-parameters.
- **Lokalisatie & Instellingen:** Volledig meertalig (NL/EN via `react-i18next`). De app bevat een Instellingenpagina waarbij het thema (Licht/Donker) en de taal direct gekoppeld zijn aan de sessie/database.
- **Backend:** Spring Boot 3 met PostgreSQL database. Voorzien van strakke REST API's en veilige globale foutafhandeling (`GlobalExceptionHandler`).
- **Security:** Keycloak integratie (OAuth2 Resource Server) met Rollen/Groepen (RBAC).
- **NEN7510 Audit Logging:** Cryptografische vastlegging van elke lezende/schrijvende actie via Spring AOP. Bevat Hash-Chaining en context mapping (`@AuditLoggable`).
- **Break The Glass:** Een noodprocedure voor verhoogde toegang, veilig ingebed in de applicatie.
- **PMI (Patiëntenregister):** Het centrale patiëntenregister (Module 1.2) is actief met zoekfunctionaliteit, opslag van verzekeringsdata, en een veilige deduplicatie/merge workflow.
- **Terminology & Ontology Server:** Een ingebouwde HAPI FHIR R4 server (Module 1.4) draait binnen de backend en faciliteert de razendsnelle opslag en ontsluiting van miljoenen medische concepten via **Elasticsearch** integratie (`ConceptRepository`, `TerminologySearchController`). Het bevat een automatische import-pipeline (`OntologyImportService`) voor SNOMED CT, LOINC en DHD (ICD-10) bestanden, direct vanaf de lokale disk, zónder externe dependencies.
- **Ambulatory Scheduling (Module 2.1):** Een hybride JSONB/PostgreSQL afspraken-engine met strikte datavalidatie, overlap-detectie en preventie van dubbele boekingen, gekoppeld aan artsen en patiënten. Dit vormt de basis voor FHIR 'Encounters'. De React frontend bevat een dynamische (Dark Mode compatible) kalender, gebouwd op `@fullcalendar/react`.
- **Automated Testing:** Robuuste testinfrastructuur gebaseerd op **Testcontainers** voor hermetische integratietests (`IntegrationTestBase`) met geïsoleerde PostgreSQL instanties.

De volgende logische module om te ontwikkelen (gezien de MVP-scope) is **Module 3.1: Clinical Notes & Charting**.
