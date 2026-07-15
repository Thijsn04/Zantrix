# Zantrix Frontend

The Zantrix frontend is a React and TypeScript outpatient clinical workspace. It provides OIDC sign-in, authenticated session lookup, a typed API client, role-aware navigation, patient registration and selection, chart tabs, clinical entry and lifecycle actions, scheduling, tasks, administration, privacy and audit views, responsive theming, internationalized copy, a PWA build, and a keyboard command palette. It reads only real API data and never displays fabricated clinical responses.

## Commands

```bash
npm ci
npm run dev
npm run lint
npm test
npm run build
npm run test:e2e
npm audit --omit=dev --audit-level=high
```

Configuration is read from Vite environment variables. Development defaults point to the local backend and Keycloak. Copy `.env.example` to `.env.local` only to override them.

The Playwright flow requires the complete Compose stack and a Chromium installation (`npx playwright install chromium`). The structure, implemented interaction model, and remaining accessibility and offline-hardening work are documented in [`docs/architecture/frontend.md`](../docs/architecture/frontend.md).
