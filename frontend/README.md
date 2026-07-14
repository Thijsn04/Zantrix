# Zantrix Frontend

The Zantrix frontend is a React and TypeScript clinical application shell. It is in Milestone 0 foundation development; the current screen is a placeholder while authentication, the API client, patient context, and the design system are built.

## Commands

```bash
npm ci
npm run dev
npm run lint
npm test
npm run build
```

Configuration is read from Vite environment variables. Copy `.env.example` to `.env.local` to override the development defaults.

The target structure and interaction model are documented in [`docs/architecture/frontend.md`](../docs/architecture/frontend.md).
