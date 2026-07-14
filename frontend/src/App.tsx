import { useAuth } from 'react-oidc-context';
import { AppWorkspace } from './app/AppWorkspace';
import { Button } from './design/Button';
import { useTranslation } from 'react-i18next';

/** Application entry point with explicit authentication states. */
export default function App() {
  const { t } = useTranslation();
  const auth = useAuth();

  if (auth.isLoading) {
    return <main className="screen-state" aria-busy="true">{t('session.loading')}</main>;
  }

  if (auth.error) {
    return <main className="screen-state" role="alert">{t('session.unavailable')}</main>;
  }

  if (!auth.isAuthenticated) {
    return (
      <main className="welcome">
        <section className="welcome-card" aria-labelledby="welcome-title">
          <p className="eyebrow">{t('app.productType')}</p>
          <h1 id="welcome-title">{t('app.title')}</h1>
          <p>{t('app.welcome')}</p>
          <Button onClick={() => void auth.signinRedirect()}>{t('session.signIn')}</Button>
        </section>
      </main>
    );
  }

  return <AppWorkspace accessToken={auth.user?.access_token ?? ''} onSignOut={() => void auth.signoutRedirect()} />;
}
