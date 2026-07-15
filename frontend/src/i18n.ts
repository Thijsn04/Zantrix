import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

const resources = { en: { translation: {
  app: { title: 'Zantrix', productType: 'Clinical workspace', welcome: 'Sign in to open your secure clinical workspace.', home: 'Zantrix home' },
  session: { loading: 'Loading your secure session…', unavailable: 'Your session could not be initialized. Please try again.', signIn: 'Sign in', signOut: 'Sign out', signedIn: 'Signed in' },
  navigation: { label: 'Workspace navigation', workspace: 'Workspace', home: 'Home', patients: 'Patients', schedule: 'Schedule', tasks: 'Tasks', admin: 'Administration', privacy: 'Privacy and audit' },
  workspace: { title: 'Your workspace is ready', empty: 'Select a patient or open a task to begin.' },
  patientContext: { label: 'Patient context', noneTitle: 'No patient selected', noneDescription: 'Select a patient before opening clinical work.' },
  command: { open: 'Search commands', title: 'Command palette', description: 'Use this menu for fast workspace actions.', goHome: 'Go to workspace', close: 'Close' },
  theme: { toggle: 'Toggle color theme' },
  common: { failed: 'The operation could not be completed.', add: 'Add entry', save: 'Save', cancel: 'Cancel', enabled: 'Enabled', disabled: 'Disabled', toggle: 'Toggle' },
  dashboard: { title: 'Clinical overview', welcome: 'Welcome, {{name}}.', openTasks: 'Open tasks', patientContext: 'Selected patients' },
  patients: { searchTitle: 'Patient registry', searchLabel: 'Search by name, birth date, or identifier', registerTitle: 'Register patient', given: 'Given name', family: 'Family name', birthDate: 'Birth date', gender: 'Administrative gender', unknown: 'Unknown', female: 'Female', male: 'Male', other: 'Other', email: 'Email', phone: 'Phone', identifier: 'Local medical record number', register: 'Register and review duplicates', change: 'Change patient' },
  chart: { encounters: 'Encounters', problems: 'Problems', allergies: 'Allergies', medications: 'Medications', vitals: 'Vitals', orders: 'Orders', results: 'Results', notes: 'Notes', empty: 'No records found.' },
  clinical: { encounterId: 'Encounter ID', orderId: 'Order ID', practitionerId: 'Practitioner ID', signerId: 'Signer practitioner ID', code: 'Clinical code', display: 'Display', value: 'Value or dose', unit: 'UCUM unit', frequency: 'Times per day', duration: 'Duration in days', quantity: 'Dispense quantity', note: 'Clinical note or dosage instructions' },
  actions: { start: 'Start encounter', finish: 'Finish encounter', resolve: 'Resolve', sign: 'Sign note' },
  notes: { assessment: 'Assessment and plan' },
  schedule: { title: 'Appointments', selectPatient: 'Select a patient before booking an appointment.', location: 'Location ID', start: 'Start', end: 'End', service: 'Service', book: 'Book appointment' },
  tasks: { title: 'Clinical worklist', start: 'Start', complete: 'Complete' },
  admin: { title: 'Administration and feature controls' },
  privacy: { title: 'Privacy operations', emergency: 'Emergency access reviews', audit: 'Audit trail' },
} } } as const;

void i18n.use(initReactI18next).init({ resources, lng: 'en', fallbackLng: 'en', interpolation: { escapeValue: false } });
export default i18n;
