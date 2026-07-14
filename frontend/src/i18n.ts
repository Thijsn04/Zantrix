import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

const resources = { en: { translation: {
  app: { title: 'Zantrix', productType: 'Clinical workspace', welcome: 'Sign in to open your secure clinical workspace.', home: 'Zantrix home' },
  session: { loading: 'Loading your secure session…', unavailable: 'Your session could not be initialized. Please try again.', signIn: 'Sign in', signOut: 'Sign out', signedIn: 'Signed in' },
  navigation: { label: 'Workspace navigation', workspace: 'Workspace', home: 'Home' },
  workspace: { title: 'Your workspace is ready', empty: 'Select a patient or open a task to begin.' },
  patientContext: { label: 'Patient context', noneTitle: 'No patient selected', noneDescription: 'Select a patient before opening clinical work.' },
  command: { open: 'Search commands', title: 'Command palette', description: 'Use this menu for fast workspace actions.', goHome: 'Go to workspace', close: 'Close' },
  theme: { toggle: 'Toggle color theme' },
} } } as const;

void i18n.use(initReactI18next).init({ resources, lng: 'en', fallbackLng: 'en', interpolation: { escapeValue: false } });
export default i18n;
