# Zantrix Frontend

The Zantrix frontend is a React and TypeScript clinical application foundation. It is in Milestone 0 development. The current screen is a placeholder; runtime OIDC configuration and a top-level OIDC provider are wired, while login controls, protected routing, the API client, patient context, workspace shell, and design system still need to be built.

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
