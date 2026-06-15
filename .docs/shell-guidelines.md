# Zantrix EPD - Application Shell & Behavior Guidelines

Dit document beschrijft de strikte technische en visuele regels om ervoor te zorgen dat de Zantrix React-frontend niet aanvoelt als een standaard website, maar exact de gebruikerservaring (UX) en robuustheid levert van een native enterprise desktop applicatie (zoals EPIC of HiX).

Deze regels gelden voor de 'schil' van de applicatie (de layout waarin alle medische modules draaien).

## 1. De "No-Scroll" Hoofdregel (100vh)
Zantrix is een cockpit, geen landingspagina. De interface als geheel mag nooit scrollbaar zijn.

- **Implementatie:** De hoofdcontainer (`<div id="root">` of de main `<App>` component) moet altijd exact de breedte en hoogte van de viewport innemen.
- **CSS Regels:** Gebruik altijd `height: 100vh; width: 100vw; overflow: hidden; display: flex; flex-direction: column;` op het hoofdelement.
- **Wanneer wél scrollen?** Alleen binnen specifieke panelen of tabbladen (bijv. een lange lijst met patiënten of de inhoud van een specifiek medisch document) mag scrollen worden toegestaan via `overflow: auto;`. De navigatie, statusbalken en tab-headers blijven altijd stilstaan op het scherm.

## 2. Interne Tabblad Navigatie (In-App Tabs)
Om te voorkomen dat artsen verdwalen of "terug" moeten klikken (browser-gedrag), gebruiken we in-app tabbladen.

- **Gedrag:** Wanneer een gebruiker vanuit een lijst op een patiënt klikt, opent er een nieuw tabblad binnen de Zantrix UI, direct naast de eventuele andere geopende patiënten of modules.
- **State Management:** De geopende tabbladen worden opgeslagen in de lokale (React) state. Zelfs als er van module wordt gewisseld (bijv. van 'Mijn Patiënten' naar 'Agenda'), blijven de patiënt-tabbladen openstaan tenzij ze actief worden gesloten.
- **Sneltoetsen:** Ondersteun keyboard shortcuts (bijv. `Ctrl+Tab`) om razendsnel tussen deze interne tabbladen te wisselen.

## 3. Verbod op Tekst Selecteren (User-Select None)
In een applicatie-omgeving is het per ongeluk blauw markeren van tekst, iconen of headers bij snel klikken extreem hinderlijk en het breekt de illusie van een native app.

- **Implementatie:** Zet `user-select: none;` op het allerhoogste niveau van de DOM (bijv. de `<body>` of de `<App>` container).
- **Uitzonderingen:** Sta tekstselectie alleen toe (`user-select: text;`) in specifieke containers waar het kopiëren van data verwacht wordt (zoals een medische notitie, een lab-uitslag of een invoerveld).

## 4. Hoge Datadichtheid (Dense UI)
Gezondheidszorg vereist het in één oogopslag overzien van grote hoeveelheden complexe data (zoals vitale waarden of medicatielijsten). 'Luchtige' webdesigns werken hier niet.

- **Spatiëring:** Gebruik minimale padding en margins. Componenten mogen compact tegen elkaar aan staan, gescheiden door subtiele borders of achtergrondkleuren (zie de UI-guidelines voor het palet).
- **Lettergrootte:** Het basislettertype voor data is kleiner dan bij typische websites (bijv. `12px` of `13px`) om zoveel mogelijk informatie op één scherm (boven de fold) te krijgen, zonder in te boeten op leesbaarheid.
- **Grid Layouts:** Gebruik robuuste CSS Grid- of Flexbox-layouts die de beschikbare schermruimte in paneeltjes verdelen, waarbij elk paneel zijn eigen header en eigen scroll-context heeft (Dashboard-stijl).

## 5. De Enterprise Window Elementen
Om de structuur van een desktopapplicatie na te bootsen, bevat de layout vaste, niet-scrollende elementen:

- **Window / Menu Bar (Top):** Bevat het logo, de ingelogde gebruiker, globaal zoeken, notificaties en klassieke acties.
- **Tab Bar (Sub-Top):** Bevat de in-app tabbladen van geopende dossiers of modules.
- **Werkveld (Midden):** De grote container waar de actieve module in draait (overflow gereguleerd).
- **Status Bar (Bottom):** Een permanente smalle balk onderaan die vitale systeem-informatie toont (bijv. "Server Verbonden (12ms)", "Gebruiker: Arts", "Omgeving: Productie").

## 6. Progressive Web App (PWA) Installatie
Om het web-gevoel volledig te elimineren, wordt de applicatie geconfigureerd als een PWA (Progressive Web App).
Dit stelt de IT-afdeling in staat om Zantrix te "installeren" op de Windows-werkplekken. Hierdoor draait de applicatie in een eigen venster, zonder de adresbalk van de browser, de "terug/vooruit"-knoppen, of bladwijzers. De interface is hierdoor 100% overgeleverd aan de interne navigatie van Zantrix.
