# Zantrix EPD - UI & Branding Guidelines

Dit document definieert de visuele identiteit, de ontwerpprincipes en de UI-standaarden (User Interface) voor het Zantrix EPD. Ons doel is het bouwen van een medisch systeem dat rust uitstraalt, de cognitieve belasting van zorgverleners verlaagt en medische fouten door onduidelijk design voorkomt.

## 1. Merkidentiteit (Branding)
Zantrix is ontworpen als het moderne, open en transparante alternatief in de zorg-IT.
- **Kernwaarden:** Betrouwbaar, Taakgericht, Rustgevend, Toegankelijk.
- **De Naam:** 'Zantrix' wordt altijd met een hoofdletter Z geschreven.
- **Tone of Voice:** De tekst in de applicatie (knoppen, foutmeldingen, labels) is professioneel, beknopt en objectief. We gebruiken actieve taal (bijv. "Patiënt opslaan" in plaats van "De gegevens van de patiënt kunnen nu worden opgeslagen").

## 2. Ontwerpprincipes (UX Filosofie)
Om de "klikmoeheid" (bekend bij systemen als EPIC en HiX) te bestrijden, hanteren we de volgende vuistregels:
- **Taak-gedreven Design:** Toon alleen de informatie en knoppen die nodig zijn voor de huidige taak. Verberg secundaire acties in menu's of tabbladen. (Progressive Disclosure).
- **Witruimte (White Space) is Functioneel:** Prop schermen niet vol. Gebruik witruimte om secties (zoals anamnese, medicatie, vitale waarden) visueel van elkaar te scheiden zonder harde lijnen.
- **Consistente Navigatie:** De zoekbalk voor patiënten en de noodknoppen (Break-the-Glass) bevinden zich altijd op exact dezelfde plek op het scherm, ongeacht de geopende module.
- **Zichtbare Systeemstatus:** De applicatie moet altijd duidelijk maken wat er gebeurt (bijv. "Data wordt opgeslagen...", "Connectie met lab verloren").

## 3. Typografie
Leesbaarheid is cruciaal, vooral bij getallen (denk aan doseringen of lab-waardes).
- **Primaire Font:** Inter (of een vergelijkbaar schreefloos/sans-serif systeemlettertype zoals Roboto of San Francisco).
- **Getallen (Tabular Nums):** Bij tabellen met vitale waarden of financiële data moet monospaced (tabular) spatiëring gebruikt worden, zodat getallen mooi onder elkaar uitlijnen.
- **Hiërarchie:**
  - `H1` (Dossiertitel): 24px, Semi-Bold
  - `H2` (Sectietitel): 18px, Medium
  - `Body` (Standaard tekst): 14px, Regular
  - `Small` (Metadata, tijdstempels): 12px, Text-Gray

## 4. Kleurenpalet
We gebruiken een kleurenpalet dat rustgevend is voor de ogen, ook tijdens lange nachtdiensten. We vermijden felle, schreeuwerige kleuren, behalve bij medische waarschuwingen. We gebruiken uitsluitend Vanilla CSS (geen CSS frameworks) om onze variabelen exact af te stemmen op het medische domein.

### Primaire Kleuren (Branding & Acties):
- **Zantrix Blue (`var(--primary-blue)` / `#0369a1`):** Gebruikt voor primaire knoppen, actieve tabbladen en links. Het straalt klinische betrouwbaarheid uit.

### Achtergronden & Vlakken (Neutrals):
- **Background (`var(--bg-light)` / `#f8fafc`):** Een hele zachte off-white/grijze tint om oogvermoeidheid tegen te gaan. (Puur wit `#ffffff` wordt alleen gebruikt voor invoervelden en actieve kaarten).
- **Text Core (`var(--text-main)` / `#0f172a`):** Bijna zwart, voor maximaal contrast en leesbaarheid.

### Semantische Kleuren (Medische Status):
- 🔴 **Critical / Error (`var(--color-critical)` / `#dc2626`):** Alleen voor allergieën, fatale fouten, en abnormale lab-uitslagen.
- 🟡 **Warning (`var(--color-warning)` / `#d97706`):** Voor waarschuwingen (bijv. "Patiënt is nuchter" of "Dossier incompleet").
- 🟢 **Success / Safe (`var(--color-safe)` / `#16a34a`):** Voor succesvolle acties (bijv. "Medicatie voorgeschreven") of normale lab-waarden.

## 5. UI Componenten
We maken gebruik van herbruikbare, speciaal ontworpen Vanilla CSS componenten (geen Tailwind, geen zware libraries).

- **Knoppen (Buttons):**
  - *Primary:* Blauwe achtergrond, witte tekst. (Alleen voor de belangrijkste actie op het scherm, bijv. "Consult Afronden").
  - *Secondary:* Witte achtergrond, grijze rand (Outline). Voor annuleer-knoppen of secundaire acties.
  - *Destructive:* Rode achtergrond. Voor het verwijderen van data of ongedaan maken van een medische order. Vereist altijd een bevestigings-dialoog!
- **Foutmeldingen & Alerts (Toasters):**
  - Gebruik subtiele 'toast' notificaties rechtsonder in beeld voor bevestigingen ("Opgeslagen").
  - Gebruik een onontkoombare, schermvullende modale dialoog (Modal/Dialog) voor kritieke waarschuwingen (bijv. "Waarschuwing: Ernstige interactie tussen medicatie A en B").
- **Formulieren & Invoervelden:**
  - Labels staan altijd boven het invoerveld (niet ernaast), zodat de interface makkelijk schaalt naar tablets en mobiele telefoons.
  - Verplichte velden worden duidelijk gemarkeerd met een asterisk (*).

## 6. Toegankelijkheid (Accessibility / A11y) & Dark Mode
- **WCAG 2.1 AA Standaard:** Alle schermen moeten voldoen aan minimale contrast-ratio's. Zorgpersoneel met lichte visuele beperkingen (zoals kleurenblindheid) moet het systeem foutloos kunnen gebruiken.
- **Niet alleen kleur:** Breng medische waarschuwingen nooit alléén met kleur over (bijv. een rood bolletje). Gebruik altijd kleur én een icoon of tekst (bijv. 🔴 Allergie).
- **Toetsenbord Navigatie:** Alle formulieren (vooral in de CPOE/Medicatie module) moeten volledig via het toetsenbord (`Tab`, `Enter`, pijltjestoetsen) te bedienen zijn. Artsen typen vaak blind en willen niet constant naar de muis grijpen.
- **Dark Mode:** Omdat Zantrix ook in de nachtzorg en op radiologie-afdelingen (waar het donker is) wordt gebruikt, is een naadloze Dark Mode een harde eis. De interface ondersteunt een donker palet met gedimde contrasten, gestuurd door CSS custom properties.
