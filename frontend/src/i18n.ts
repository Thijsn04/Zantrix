import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

/**
 * Internationalization for the Zantrix frontend.
 *
 * English is the default language. Every user facing string is a translation
 * key. Additional languages are added as resource bundles, not code changes.
 */
const resources = {
  en: {
    translation: {
      app: {
        title: 'Zantrix',
        rebuilding: 'The Zantrix clinical application is being rebuilt from the ground up.',
      },
    },
  },
} as const;

void i18n.use(initReactI18next).init({
  resources,
  lng: 'en',
  fallbackLng: 'en',
  interpolation: { escapeValue: false },
});

export default i18n;
