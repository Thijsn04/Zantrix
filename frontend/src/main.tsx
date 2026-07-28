import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { AuthProvider } from 'react-oidc-context';
import { BrowserRouter } from 'react-router-dom';
import './index.css';
import './i18n';
import App from './App';
import { config } from './config';

/**
 * OpenID Connect configuration for Keycloak, driven by runtime config so that
 * no host is hardcoded. See config.ts and .env.example.
 */
const oidcConfig = {
  authority: config.oidc.authority,
  client_id: config.oidc.clientId,
  redirect_uri: config.oidc.redirectUri,
  post_logout_redirect_uri: config.oidc.redirectUri,
  scope: 'openid profile email user/*.cruds',
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider {...oidcConfig}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </AuthProvider>
  </StrictMode>,
);
