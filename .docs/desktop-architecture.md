# Zantrix Desktop Deployment Architecture

Dit document beschrijft de toekomstige deployment-strategie voor het Zantrix EPD in een productie/ziekenhuisomgeving.

## 1. Het Probleem met Standaard Web Browsers
Hoewel Zantrix intern wordt gebouwd als een moderne React Single Page Application (SPA), is implementatie in een standaard browser (Chrome, Edge, Safari) in een klinische setting ongewenst:
- **Sneltoets-Conflicten:** Standaard browser sneltoetsen (F3, Ctrl+P, F5, Ctrl+W) kunnen niet betrouwbaar worden overschreven. In de medische wereld is keyboard-only navigatie essentieel voor snelheid.
- **Beveiligingsrisico's:** Gebruikers hebben toegang tot Developer Tools (Ctrl+Shift+I), URL-balk manipulatie en browser-extensies die medische data kunnen afluisteren.
- **Ontbrekende Hardware-Integratie:** Een standaard browser kan niet direct communiceren met lokale hardware zoals smartcard-lezers (UZI-pas, eID), labelprinters of seriële poorten van medische apparatuur.
- **UI/UX Vervuiling:** Browser tabbladen, adresbalken en favorietenbalken nemen waardevolle schermruimte in en breken de "Enterprise App" illusie.

## 2. De Oplossing: De Desktop Wrapper
Om deze beperkingen op te heffen, wordt Zantrix gedistribueerd als een Desktop Applicatie via een wrapper-technologie.
- **Frontend Codebase:** Blijft 100% React + TypeScript + Vite.
- **Deployment:** De React code wordt verpakt in een Chromium-engine via **Electron**.
- **Resultaat:** Een installeerbare `.exe` (Windows) of `.app` (macOS) die voelt en functioneert als een native applicatie.

## 3. Strategische Keuze: Electron vs. Tauri
Voor Zantrix is gekozen voor **Electron**, ondanks de zwaardere footprint vergeleken met Tauri.
- **Reden:** De enterprise software wereld en zorg-IT (zoals ziekenhuis IT-beheer en Citrix-omgevingen) zijn zeer bekend met de werking en uitrol van Electron apps (Microsoft Teams en Slack draaien hier ook op). 
- Bovendien is het Node.js ecosysteem voor hardware-integratie (PC/SC voor UZI-passen) momenteel robuuster dan de Rust-tegenhanger.

## 4. Architectonische Gevolgen & Richtlijnen

### 4.1. Keyboard Shortcuts (IPC Routing)
Alle globale sneltoetsen moeten worden afgevangen op het Main Process (Electron backend) niveau, en niet in de React code (`window.addEventListener`).
- *Flow:* Gebruiker drukt F3 -> Electron pakt dit op, overschrijft het OS-gedrag -> Electron stuurt een IPC (Inter-Process Communication) signaal naar React: `action: globalSearch` -> React toont de patiëntenzoeker.

### 4.2. Beveiliging (Production Build)
In de productie-build worden de volgende zaken strikt afgedwongen:
- `webPreferences.devTools = false;` (Developer tools geblokkeerd).
- Rechtermuisklik (Context menu) wordt volledig uitgeschakeld of vervangen door een custom Zantrix medisch contextmenu.
- Geen toegang tot bestandsbeheer tenzij expliciet via een Zantrix-gereguleerd dialoogvenster.

### 4.3. Hardware API's (Lokale Connecties)
De Electron 'Main Process' laag fungeert als een bridge voor:
- **Identity:** UZI-pas / Smartcard lezers via PC/SC standaarden.
- **Printing:** Directe aansturing van Zebra labelprinters (voor polsbandjes en bloedbuisjes) zonder tussenkomst van de browser print-dialoog.
- **Apparatuur:** COM-poort / Seriële communicatie voor medische apparatuur op de intensive care.

---

## Implementatie Roadmap
**Huidige Status:** Uitgesteld (Later).
*Besluit:* Tijdens de huidige ontwikkelfase bouwen we eerst de klinische kern (waaronder Module 3.1) in de browser om snelle iteratie te behouden. We houden bij het bouwen van de frontend wel al rekening met een abstractielaag voor globale sneltoetsen, zodat de overstap naar Electron IPC later naadloos verloopt. De daadwerkelijke migratie naar Electron vindt pas plaats zodra we hardware-integraties (UZI-pas, Printers) moeten implementeren.
