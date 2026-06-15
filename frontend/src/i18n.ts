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
      },
      "app": {
        "loading": "Authenticatie laden...",
        "auth_error": "Authenticatie fout",
        "title": "Zantrix EPD",
        "subtitle": "Klinisch Informatiesysteem",
        "login": "Aanmelden",
        "btg_success": "Noodtoegang geactiveerd. Alle acties worden gelogd.",
        "btg_error_rights": "Fout bij escaleren rechten.",
        "btg_error_system": "Systeemfout: backend onbereikbaar."
      },
      "shell": {
        "menu": {
          "file": "Bestand",
          "edit": "Bewerken",
          "help": "Help"
        },
        "search_placeholder": "Patiënt zoeken (F3)...",
        "search_loading": "Zoeken...",
        "search_no_results": "Geen patiënten gevonden.",
        "search_error": "Fout bij zoeken.",
        "btg_tooltip": "Noodtoegang (Break The Glass)",
        "role_user": "GEBRUIKER",
        "logout_tooltip": "Uitloggen",
        "nav": {
          "home": "Startscherm",
          "patient_dossier": "Patiëntendossier",
          "audit_logs": "Audit Logs",
          "terminology": "Terminologie Browser",
          "unknown": "Onbekend"
        },
        "settings": "Instellingen",
        "footer": {
          "connected": "Verbonden",
          "session": "Sessie",
          "version": "Versie 1.0.1 (Productie)"
        },
        "btg_modal": {
          "title": "Break The Glass Activeren",
          "warning": "Je staat op het punt om noodrechten (Break The Glass) te activeren. Dit geeft je toegang tot afgeschermde dossiers.",
          "alert_note": " Deze actie triggert een alert en vereist achteraf verantwoording.",
          "reason_label": "Verplichte Reden:",
          "reason_placeholder": "Bijv: Spoedopname SEH patiënt X, buiten reguliere diensttijd.",
          "cancel": "Annuleren",
          "activate": "Activeren"
        }
      },
      "settings": {
        "title": "Instellingen",
        "theme": {
          "label": "Thema",
          "light": "Licht",
          "dark": "Donker"
        },
        "language": {
          "label": "Taal",
          "nl": "Nederlands",
          "en": "Engels"
        },
        "save": "Opslaan",
        "success": "Instellingen succesvol opgeslagen.",
        "error": "Fout bij opslaan van instellingen."
      },
      "privacy": {
        "title": "Systeem Audit Logs",
        "subtitle": "System Audit Logs",
        "search_placeholder": "Zoek in logs...",
        "loading": "Audit trail laden...",
        "columns": {
          "timestamp": "Tijdstip",
          "user": "Gebruiker",
          "action": "Actie",
          "resource": "Resource",
          "context": "Context"
        }
      },
      "patient_search": {
        "title": "Patiënten zoeken (PMI)",
        "new_patient": "Nieuwe Patiënt",
        "search_placeholder": "Zoek op BSN of Achternaam...",
        "search_button": "Zoeken",
        "loading": "Laden...",
        "no_results": "Geen patiënten gevonden.",
        "columns": {
          "bsn": "BSN",
          "name": "Naam",
          "birthDate": "Geboortedatum",
          "gender": "Geslacht",
          "status": "Status"
        },
        "status": {
          "merged": "Gemerged",
          "deceased": "Overleden",
          "active": "Actief"
        }
      },
      "patient_detail": {
        "loading": "Dossier laden...",
        "not_found": "Patiënt niet gevonden of onvoldoende rechten.",
        "emergency_name": "John Doe (Noodpatiënt)",
        "bsn": "BSN:",
        "born": "Geboren:",
        "gender": "Geslacht:",
        "status": "Status:",
        "insurance": "Verzekeraar:",
        "policy": "Polisnummer:",
        "edit_dossier": "Dossier Bewerken",
        "merge_patient": "Patiënt Mergen",
        "notes_title": "Medische Notities",
        "no_notes": "Geen notities beschikbaar. Module nog in ontwikkeling.",
        "vitals_title": "Vitale Waarden",
        "no_vitals": "Geen metingen gevonden."
      },
      "terminology": {
        "title": "Terminologie Browser",
        "search_placeholder": "Zoek concept (bijv. 'Astma')...",
        "search_button": "Zoeken",
        "loading": "Zoeken...",
        "no_results": "Geen concepten gevonden.",
        "import_tab": "Beheer & Import",
        "import_title": "Terminologie Import (Beheerder)",
        "import_snomed_btn": "Importeer SNOMED CT",
        "import_loinc_btn": "Importeer LOINC",
        "import_success": "Import proces is op de achtergrond gestart."
      }
    }
  },
  en: {
    translation: {
      "dashboard": {
        "no_patient": "No active patient",
        "no_patient_desc": "Use the search bar (F3) or choose a patient from the list to open a file.",
        "privacy_title": "Privacy Dashboard",
        "privacy_desc": "Choose \"Audit Logs\" in the left menu to monitor access logs."
      },
      "app": {
        "loading": "Loading authentication...",
        "auth_error": "Authentication error",
        "title": "Zantrix EMR",
        "subtitle": "Clinical Information System",
        "login": "Sign In",
        "btg_success": "Emergency access activated. All actions will be logged.",
        "btg_error_rights": "Error escalating rights.",
        "btg_error_system": "System error: backend unreachable."
      },
      "shell": {
        "menu": {
          "file": "File",
          "edit": "Edit",
          "help": "Help"
        },
        "search_placeholder": "Search patient (F3)...",
        "search_loading": "Searching...",
        "search_no_results": "No patients found.",
        "search_error": "Error searching.",
        "btg_tooltip": "Emergency Access (Break The Glass)",
        "role_user": "USER",
        "logout_tooltip": "Sign Out",
        "nav": {
          "home": "Home",
          "patient_dossier": "Patient File",
          "audit_logs": "Audit Logs",
          "terminology": "Terminology Browser",
          "unknown": "Unknown"
        },
        "settings": "Settings",
        "footer": {
          "connected": "Connected",
          "session": "Session",
          "version": "Version 1.0.1 (Production)"
        },
        "btg_modal": {
          "title": "Activate Break The Glass",
          "warning": "You are about to activate emergency rights (Break The Glass). This grants you access to protected files.",
          "alert_note": " This action triggers an alert and requires justification afterwards.",
          "reason_label": "Mandatory Reason:",
          "reason_placeholder": "E.g.: ER admission of patient X outside regular hours.",
          "cancel": "Cancel",
          "activate": "Activate"
        }
      },
      "settings": {
        "title": "Settings",
        "theme": {
          "label": "Theme",
          "light": "Light",
          "dark": "Dark"
        },
        "language": {
          "label": "Language",
          "nl": "Dutch",
          "en": "English"
        },
        "save": "Save",
        "success": "Settings successfully saved.",
        "error": "Error saving settings."
      },
      "privacy": {
        "title": "System Audit Logs",
        "subtitle": "System Audit Logs",
        "search_placeholder": "Search logs...",
        "loading": "Loading audit trail...",
        "columns": {
          "timestamp": "Timestamp",
          "user": "User",
          "action": "Action",
          "resource": "Resource",
          "context": "Context"
        }
      },
      "patient_search": {
        "title": "Patient Search (PMI)",
        "new_patient": "New Patient",
        "search_placeholder": "Search by SSN or Last Name...",
        "search_button": "Search",
        "loading": "Loading...",
        "no_results": "No patients found.",
        "columns": {
          "bsn": "SSN",
          "name": "Name",
          "birthDate": "Birth Date",
          "gender": "Gender",
          "status": "Status"
        },
        "status": {
          "merged": "Merged",
          "deceased": "Deceased",
          "active": "Active"
        }
      },
      "patient_detail": {
        "loading": "Loading dossier...",
        "not_found": "Patient not found or insufficient permissions.",
        "emergency_name": "John Doe (Emergency Patient)",
        "bsn": "SSN:",
        "born": "Born:",
        "gender": "Gender:",
        "status": "Status:",
        "insurance": "Insurance:",
        "policy": "Policy Number:",
        "edit_dossier": "Edit Dossier",
        "merge_patient": "Merge Patient",
        "notes_title": "Medical Notes",
        "no_notes": "No notes available. Module still in development.",
        "vitals_title": "Vitals",
        "no_vitals": "No measurements found."
      },
      "terminology": {
        "title": "Terminology Browser",
        "search_placeholder": "Search concept (e.g. 'Asthma')...",
        "search_button": "Search",
        "loading": "Searching...",
        "no_results": "No concepts found.",
        "import_tab": "Manage & Import",
        "import_title": "Terminology Import (Admin)",
        "import_snomed_btn": "Import SNOMED CT",
        "import_loinc_btn": "Import LOINC",
        "import_success": "Import process started in the background."
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
