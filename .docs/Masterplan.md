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
- **Frontend:** React met TypeScript (Vite). Strikte foutcontrole, 100% Vanilla CSS (geen zware frameworks), PWA-compatibel, Meertalig via `react-i18next`.
- **API-First:** De frontend en backend communiceren uitsluitend via REST/GraphQL API's. Hierdoor is het systeem direct klaar voor externe koppelingen.
- **Hosting & Distributie:** Docker Containers.

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
Op dit moment zijn **Module 1.1 (IAM & Security)**, **Module 1.2 (Patient Master Index - PMI)** en **Module 1.3 (Audit & Compliance)** succesvol ontworpen en gebouwd. De technische basis (het fundament) staat robuust overeind:

- **Frontend (PWA Shell):** Een React/TypeScript applicatie met Vite, volledig meertalig (`react-i18next`), ingericht als Native App-ervaring. Gerealiseerd met uitsluitend Vanilla CSS voor superieure performance en branding.
- **Backend:** Spring Boot 3 met PostgreSQL database. Voorzien van strakke REST API's.
- **Security:** Keycloak integratie (OAuth2 Resource Server) met Rollen/Groepen (RBAC).
- **NEN7510 Audit Logging:** Cryptografische vastlegging van elke lezende/schrijvende actie via Spring AOP. Bevat Hash-Chaining en context mapping (`@AuditLoggable`).
- **Break The Glass:** Een noodprocedure voor verhoogde toegang, veilig ingebed in de applicatie.
- **PMI (Patiëntenregister):** Het centrale patiëntenregister (Module 1.2) is actief met zoekfunctionaliteit, opslag van verzekeringsdata, en een veilige deduplicatie/merge workflow.

De volgende logische module om te ontwikkelen (gezien de MVP-scope) is **Module 2.1: Ambulatory Scheduling (Polikliniek)** of **Module 3.1: Clinical Notes & Charting**.
