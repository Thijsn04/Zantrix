# Zantrix EPD

Zantrix is een modern, open-source Elektronisch Patiëntendossier (EPD), ontworpen met de filosofie van maximale standaardisatie, veiligheid en modulaire schaalbaarheid.

De applicatie is opgesplitst in een React/TypeScript PWA-frontend (met Vite) en een robuuste Java Spring Boot 3 backend, ondersteund door een PostgreSQL database en beveiligd met Keycloak (OAuth2 / OpenID Connect).

## Systeemvereisten

Zorg dat de volgende software is geïnstalleerd:
- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- [Java 21](https://adoptium.net/) (of hoger)
- [Node.js](https://nodejs.org/) (minimaal v20 aanbevolen)
- Optioneel: Maven (er wordt een Maven Wrapper `mvnw` meegeleverd)

---

## 🚀 Snelstartgids (Lokale Ontwikkeling)

### 1. Start de infrastructuur (Database & Identity Provider)
Zantrix maakt gebruik van PostgreSQL voor dataopslag en Keycloak voor Identity & Access Management (IAM). Start deze services via Docker:

```bash
docker-compose up -d
```
*Dit start de PostgreSQL database (port 5433) en Keycloak (port 8081). De Keycloak server importeert automatisch de testgebruikers via `realm-export.json`.*

### 2. Start de Backend (Spring Boot)
De backend zorgt voor de bedrijfslogica en is toegankelijk via poort 8080. 
Open een nieuwe terminal in de map `backend`:

```bash
cd backend
./mvnw spring-boot:run
```
*(Op Windows gebruik je `.\mvnw.cmd spring-boot:run`)*

De Spring Boot API zal starten. Inclusief automatische database-migraties via Flyway (indien geconfigureerd) of Hibernate DDL.

### 3. Start de Frontend (React / Vite)
De frontend is een React applicatie met een 'native desktop' uitstraling, geschreven in TypeScript en gebouwd met Vite. 
Open een nieuwe terminal in de map `frontend`:

```bash
cd frontend
npm install
npm run dev
```
De web-applicatie zal lokaal bereikbaar zijn op `http://localhost:5173/`.

---

## 👤 Test Accounts (Inloggen)

Keycloak laadt automatisch de `zantrix` realm in met de volgende ingebouwde test-accounts. Het wachtwoord voor al deze accounts is **`test`**.

| Gebruikersnaam  | Rol | Omschrijving |
| :--- | :--- | :--- |
| `doctor_test` | DOCTOR | Standaard medische toegang. Kan *Break-the-Glass* (noodtoegang) triggeren. |
| `nurse_test` | NURSE | Toegang voor verpleegkundigen. Kan *Break-the-Glass* triggeren. |
| `privacy_test` | PRIVACY_OFFICER | Administratieve privacy toegang (voor Audit Logs etc.). |

*Beheerder van Keycloak zelf? Ga naar `http://localhost:8081/` en log in met `admin` / `admin`.*

---

## 🛠 Project Structuur

- **`/.docs`**: Uitgebreide documentatie over architectuur, masterplan, UI/UX (shell) guidelines, codeer-regels en de roadmap.
- **`/backend`**: Spring Boot 3 + Java applicatie met de interne modules (IAM, PMI, etc.).
- **`/frontend`**: React + TypeScript client applicatie, PWA ready, zonder overbodige framework ballast (uitsluitend Vanilla CSS voor maximale performance).
- **`docker-compose.yml`**: Voorziening voor de onderliggende resources (Database, Keycloak).
- **`realm-export.json`**: Pre-geconfigureerde Keycloak omgeving met rollen en test accounts.

## 📖 Documentatie
Voor verdere technische richtlijnen, gelieve de bestanden in de `/.docs` map in te zien. Ze bevatten onder meer:
- `Masterplan.md`: Het grote geheel en de doelen.
- `modules.md`: Overzicht van alle modules en hun status.
- `modules/IAM.md`: Documentatie IAM module.
- `modules/PMI.md`: Documentatie PMI module.
- `modules/Terminology.md`: Documentatie Terminology module.
- `coding-guidelines.md` & `shell-guidelines.md`: Code- en UX-standaarden.

---
*Gelicenseerd onder AGPLv3 (GNU Affero General Public License v3.0).*
