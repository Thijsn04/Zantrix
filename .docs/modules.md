# Zantrix EPD - Volledige Module Catalogus (Enterprise Editie)

Dit document bevat de uitputtende, fijnmazige lijst van alle meer dan 50+ modules voor het Zantrix EPD, ontworpen om te concurreren met systemen als EPIC en HiX. Dankzij de modulaire architectuur kunnen zorginstellingen exact hun eigen ecosysteem samenstellen.
De modules gemarkeerd met `[MVP]` vormen samen het absoluut noodzakelijke fundament voor versie 1.0.

## 1. Fundament & Systeem (Core Foundation)

- **1.1 Identity & Access Management (IAM) [MVP]**
  - **Status:** **✅ Gerealiseerd**
  - **Details:** Bevat RBAC integratie via OAuth2 (Keycloak/JWT). Bevat PWA Frontend Shell met routering. Inclusief `Break-the-Glass` noodtoegang via JWT-manipulatie en filter override. Volledige cryptografische audit logging (NEN7510) via Spring AOP (wie, wat, IP, patient ID, hash-chaining).
- **1.2 Patient Master Index (PMI) [MVP]**
  - **Status:** **✅ Gerealiseerd**
  - **Details:** Centrale database voor NAW, BSN, verzekeringsdata en deduplicatie. Inclusief zoekfunctionaliteit en merge/deduplicatie logica.
- **1.3 Audit & Compliance Engine [MVP]**
  - **Status:** **✅ Gerealiseerd (Onderdeel van 1.1)**
  - **Details:** NEN7510/AVG logging van elke lees- en schrijfactie. Gerealiseerd via `@AuditLoggable` aspect.
- **1.4 Terminology & Ontology Server [MVP]**
  - **Status:** **✅ Gerealiseerd**
  - **Details:** Volledige HAPI FHIR R4 Terminology Server integratie (PostgreSQL). Bevat automatische import-pipeline (via `OntologyImportService`) voor SNOMED CT (RF2), LOINC en FHIR CodeSystem JSONs (ICD-10/DHD). Inclusief een interne, afgeschermde test-browser in de frontend voor beheerders.
- **1.5 Interoperability Engine (Bridges)**
  - **Details:** HL7 v2 en FHIR vertaalmachine voor externe communicatie.
- **1.6 Workflow & Rules Engine**
  - **Details:** De achterliggende motor voor het automatiseren van protocollen en taken.
- **1.7 Data Warehouse (Caboodle-alternatief)**
  - **Details:** Gestructureerde data-opslag voor complexe ziekenhuisbrede query's.

## 2. Patiëntenstroom & Logistiek (Patient Access & Flow)
- **2.1 Ambulatory Scheduling (Polikliniek) [MVP]:** Complexe agenda's, afspraakbeheer en resource-planning.
- **2.2 Grand Central / ADT (Kliniek):** Admission, Discharge, Transfer. Het complete bedden- en capaciteitsbeheer.
- **2.3 Welcome & Kiosk:** Zelfservice aanmeldzuilen in de centrale hal, inclusief ID-scanners.
- **2.4 Referral Management:** Beheer van inkomende en uitgaande verwijzingen (Integratie met ZorgDomein).
- **2.5 Patient Transport & Logistics:** App voor logistiek medewerkers om patiënten in bedden of rolstoelen door het ziekenhuis te vervoeren.
- **2.6 Bed Management & Cleaning:** Status van bedden (vies/schoon) aangestuurd voor de schoonmaakploegen.

## 3. Klinische Basis (Clinical Core)
- **3.1 Clinical Notes & Charting [MVP]:** Vrije tekst, sjablonen en spraakherkenning (SOAP) voor verslaglegging.
- **3.2 CPOE (Order Entry) [MVP]:** Digitaal aanvragen van medicatie, lab, radiologie en consulten.
- **3.3 Problem & Diagnosis List:** Beheer van de actieve en inactieve episodelijst (gekoppeld aan SNOMED).
- **3.4 Allergies, Veto's & Alerts:** Registratie van allergieën, intoleranties, en reanimatie/wilsverklaringen.
- **3.5 Vital Signs & Flowsheets:** Registratie van bloeddruk, pols, vochtbalans, MEWS-scores.
- **3.6 Clinical Decision Support (CDS):** Pop-ups en waarschuwingen voor fatale medicatie-interacties en klinische protocollen.
- **3.7 Task Management (Handovers):** Systeem voor overdrachten (SBAR) tussen dag- en nachtdiensten.
- **3.8 Zantrix Mobile Care:** Een app voor smartphones/tablets zodat verpleegkundigen barcodes van medicatie kunnen scannen aan het bed.

## 4. Medische Specialismen (Departmental & Specialties)
- **4.1 Emergency Department (SEH):** Triage-systemen (MTS), real-time trackboard en snelle order-invoer.
- **4.2 OpTime (Operatiekamer):** OK-planning, instrumenten-telling, pre- en post-operatieve workflows.
- **4.3 Anesthesia:** Real-time registratie van vitale waarden en narcosemiddelen per minuut tijdens de operatie.
- **4.4 Maternity & Obstetrics (Verloskunde):** Zwangerschapsdossier, partogram (tijdlijn bevalling) en CTG-monitoring.
- **4.5 Neonatology:** Speciale protocollen, groeicurves en voeding-schema's voor (premature) pasgeborenen.
- **4.6 Intensive Care Unit (ICU):** Connectie met beademingsapparatuur, complexe infuuspompen en extreme data-dichtheid.
- **4.7 Oncology & Chemotherapy:** Strikte, meerdaagse chemotherapie-protocollen met toxische doseringsberekeningen (BSA).
- **4.8 Cardiology:** Integratie met ECG's, pacemakers en echo-cardiogrammen.
- **4.9 Ophthalmology (Oogheelkunde):** Grafische interfaces voor refractie, visus en tekenen op netvlies-modellen.
- **4.10 Dental & Maxillofacial:** 3D-odontogram (gebit-kaart) voor tandartsen en kaakchirurgen.
- **4.11 Orthopedics & Rehab:** Gewrichtsscores, revalidatiedoelen en prothese-registratie.
- **4.12 Endoscopy:** Workflow voor maag-darmonderzoeken inclusief automatische rapportage en beeld-capture.
- **4.13 Psychiatry & Behavioral Health (GGZ):** Separeer-registratie, suïcide-risicotaxaties en strenge privacy-afscherming.
- **4.14 Infection Control (Infectiepreventie):** Automatische detectie van BRMO/MRSA en contactonderzoek-workflows.
- **4.15 Transplant Management:** Matching en registratie van donoren, ontvangers en afstotingsverschijnselen.
- **4.16 Dialysis:** Protocollen voor hemodialyse en registratie van nierfunctievervangende therapie.
- **4.17 Wound Care:** Tijdlijn met foto-uploads en metingen van decubitus en brandwonden.
- **4.18 Pediatrics (Kindergeneeskunde):** Leeftijdsafhankelijke referentiewaarden, WHO-groeicurves en vaccinatieschema's.
- **4.19 Clinical Genetics:** Stamboom-tekenprogramma en registratie van DNA-sequencing resultaten.

## 5. Ondersteunende Diensten (Ancillary Services)
- **5.1 Clinical Laboratory (LIS):** Chemisch en hematologisch bloedonderzoek met automatische analyzer-koppelingen.
- **5.2 Microbiology Lab:** Registratie van bacteriekweken, schimmels en antibiotica-resistentiebepalingen.
- **5.3 Pathology Lab:** Workflow voor weefselonderzoek, biopten en macroscopie.
- **5.4 Radiology (RIS):** Planning en protocollering van MRI, CT, Röntgen en PET-scans.
- **5.5 PACS / DICOM Viewer:** Geïntegreerde diagnostische viewer voor 3D beeldmateriaal binnen het dossier.
- **5.6 Inpatient Pharmacy (Klinische Apotheek):** Voorraadbeheer ziekenhuis, VTGM (Voor Toediening Gereed Maken) en spuitpomp-bereidingen.
- **5.7 Outpatient Pharmacy (Poli-apotheek):** Receptverwerking, voorraad en uitgifte aan patiënten die naar huis gaan.
- **5.8 Blood Bank:** Bloedgroep-typering, uitgifte van erytrocyten-concentraten en transfusiereactie-registratie.
- **5.9 Dietetics & Food Services:** Dieetvoorschriften (bijv. zoutloos), voedingssondes en integratie met de ziekenhuiskeuken.

## 6. Financiën & Declaraties (Revenue Cycle)
- **6.1 Medical Coding (DBC/DOT):** Automatische afleiding van zorgactiviteiten naar declarabele producten.
- **6.2 Billing & Invoicing:** Genereren van (pro-forma) facturen en patiënt-eigen bijdrages.
- **6.3 Claims & Payer Management:** Elektronische declaraties naar zorgverzekeraars via clearinghouses (VECOZO).
- **6.4 Contract Management:** Beheer van prijsafspraken en omzetplafonds met verschillende verzekeraars.
- **6.5 Cost Accounting:** Inzicht in de werkelijke kosten per behandeling ten opzichte van de opbrengsten.

## 7. Patiënt & Externe Zorg (Patient Engagement)
- **7.1 Patient Portal (MyZantrix):** Patiënten kunnen afspraken maken, uitslagen zien en berichten sturen naar de arts.
- **7.2 Telemedicine & Virtual Care:** Videoconsulten naadloos geïntegreerd naast het dossier.
- **7.3 PROMs & PREMs:** Automatisch versturen van klinische en ervarings-vragenlijsten via e-mail of app.
- **7.4 Remote Patient Monitoring (RPM):** Thuismeten via wearables (smartwatches, bloeddrukmeters thuis).
- **7.5 CRM & Patient Outreach:** Campagnes voor preventieve zorg (bijv. oproepen voor griepprik).

## 8. Data, Analytics & AI
- **8.1 Business Intelligence Dashboards:** Real-time KPI's voor het management (bedbezetting, wachttijden).
- **8.2 Clinical Trials & Research:** Afschermen en anonimiseren van cohort-data voor wetenschappelijk onderzoek.
- **8.3 Population Health Management:** Identificeren van risicogroepen (bijv. alle diabetespatiënten met een stijgend HbA1c).
- **8.4 AI & Predictive Analytics:** Machine learning modellen voor bijv. het voorspellen van no-shows of het risico op sepsis.
