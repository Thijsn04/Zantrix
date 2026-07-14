# Zantrix Frontend

The Zantrix frontend is a React and TypeScript clinical application foundation. It provides an OIDC sign-in boundary, authenticated session lookup, a typed API client, a responsive workspace shell, theme tokens, accessible button primitives, persistent patient-context location, and a keyboard command palette. It deliberately does not display fabricated patient or clinical data.

## Commands

```bash
npm ci
npm run dev
npm run lint
npm test
npm run build
npm audit --omit=dev --audit-level=high
```

Configuration is read from Vite environment variables. Development defaults point to the local backend and Keycloak. Copy `.env.example` to `.env.local` only to override them.

The target structure and interaction model are documented in [`docs/architecture/frontend.md`](../docs/architecture/frontend.md).
