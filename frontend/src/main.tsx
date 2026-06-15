import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import './i18n'
import App from './App.tsx'
import { AuthProvider } from 'react-oidc-context'

const oidcConfig = {
  authority: "http://localhost:8081/realms/zantrix",
  client_id: "zantrix-frontend",
  redirect_uri: "http://localhost:5173",
  post_logout_redirect_uri: "http://localhost:5173",
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  }
};

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider {...oidcConfig}>
      <App />
    </AuthProvider>
  </StrictMode>,
)
