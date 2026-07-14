/**
 * Runtime configuration for the Zantrix frontend.
 *
 * All external hosts come from environment variables so that nothing is
 * hardcoded across the codebase. Sensible development defaults are used when
 * a variable is not set. See .env.example for the full list.
 */
export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  oidc: {
    authority: import.meta.env.VITE_OIDC_AUTHORITY ?? 'http://localhost:8081/realms/zantrix',
    clientId: import.meta.env.VITE_OIDC_CLIENT_ID ?? 'zantrix-frontend',
    redirectUri: import.meta.env.VITE_OIDC_REDIRECT_URI ?? window.location.origin,
  },
} as const;
