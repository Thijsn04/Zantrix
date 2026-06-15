import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

const resources = {
  nl: {
    translation: {
      "dashboard": {
        "no_patient": "Geen actieve patiënt",
        "no_patient_desc": "Gebruik de zoekbalk (F3) of kies een patiënt uit de lijst om een dossier te openen.",
        "privacy_title": "Privacy Dashboard",
        "privacy_desc": "Kies \"Audit Logs\" in het linkermenu om de toegangslogs te monitoren."
      }
    }
  }
};

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: 'nl',
    fallbackLng: 'nl',
    interpolation: {
      escapeValue: false
    }
  });

export default i18n;
