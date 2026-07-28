import { useEffect, useMemo, useState } from 'react';
import { QueryClient, QueryClientProvider, useQuery } from '@tanstack/react-query';
import { Command, LogOut, Moon, Search, Sun } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { ApiClient } from '../lib/api/client';
import { Button } from '../design/Button';
import { CommandPalette } from './CommandPalette';
import { landingPage, navigationFor, type WorkspacePage } from './navigation';
import { can, primaryRole } from '../lib/roles';
import type { CurrentUser, PatientSummary } from '../lib/api/types';
import { RoleDashboard } from '../features/home/RoleDashboard';
import { MAX_OPEN_PATIENTS, PatientWorkspace } from '../features/patients/PatientWorkspace';
import { Worklist } from '../features/worklist/Worklist';
import { Administration } from '../features/admin/Administration';
import { PrivacyOffice } from '../features/privacy/PrivacyOffice';
import { Notice } from '../design/Feedback';
import { Panel } from '../design/Panel';

const queryClient = new QueryClient({ defaultOptions: { queries: { retry: 1, staleTime: 30_000 } } });

export function AppWorkspace(props: { accessToken: string; onSignOut: () => void }) {
  return <QueryClientProvider client={queryClient}><Workspace {...props} /></QueryClientProvider>;
}

function Workspace({ accessToken, onSignOut }: { accessToken: string; onSignOut: () => void }) {
  const { t } = useTranslation();
  const [paletteOpen, setPaletteOpen] = useState(false);
  const [dark, setDark] = useState(() => localStorage.getItem('zantrix-theme') === 'dark');
  const [page, setPage] = useState<WorkspacePage>();
  const [openPatients, setOpenPatients] = useState<PatientSummary[]>([]);
  const [activePatientId, setActivePatientId] = useState<string>();
  const [limitReached, setLimitReached] = useState(false);
  const client = useMemo(() => new ApiClient(accessToken), [accessToken]);
  const session = useQuery({ queryKey: ['current-user'], queryFn: ({ signal }) => client.currentUser(signal) });
  const user = session.data;

  useEffect(() => {
    document.documentElement.classList.toggle('dark', dark);
    localStorage.setItem('zantrix-theme', dark ? 'dark' : 'light');
  }, [dark]);

  useEffect(() => {
    const listener = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault();
        setPaletteOpen(open => !open);
      }
    };
    window.addEventListener('keydown', listener);
    return () => window.removeEventListener('keydown', listener);
  }, []);

  const navigation = navigationFor(user);
  // Until the user explicitly navigates, the page is derived from their role
  // rather than stored, so no effect has to synchronize it after the session loads.
  const current = page ?? landingPage(user);

  /**
   * Opening a third chart is refused rather than silently closing one. Losing
   * track of which patient is in context is the classic wrong-patient error,
   * so the clinician has to decide what to close.
   */
  function openPatient(patient: PatientSummary) {
    setPage('patients');
    if (openPatients.some(candidate => candidate.id === patient.id)) {
      setActivePatientId(patient.id);
      setLimitReached(false);
      return;
    }
    if (openPatients.length >= MAX_OPEN_PATIENTS) {
      setActivePatientId(undefined);
      setLimitReached(true);
      return;
    }
    setOpenPatients([...openPatients, patient]);
    setActivePatientId(patient.id);
    setLimitReached(false);
  }

  function closePatient(id: string) {
    const remaining = openPatients.filter(candidate => candidate.id !== id);
    setOpenPatients(remaining);
    setLimitReached(false);
    if (activePatientId === id) setActivePatientId(remaining[0]?.id);
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label={t('app.home')}>Z</a>
        <button className="command-trigger" onClick={() => setPaletteOpen(true)}>
          <Search size={14} aria-hidden="true" /> {t('command.open')}
          <kbd><Command size={11} aria-hidden="true" />K</kbd>
        </button>
        <div className="topbar-actions">
          {user ? <span className="role-chip">{t(`dashboard.role.${primaryRole(user)}`, { defaultValue: primaryRole(user) })}</span> : null}
          <span className="session-name">{user?.displayName ?? user?.username ?? t('session.signedIn')}</span>
          <Button className="icon-button" aria-label={t('theme.toggle')} onClick={() => setDark(!dark)}>
            {dark ? <Sun size={15} aria-hidden="true" /> : <Moon size={15} aria-hidden="true" />}
          </Button>
          <Button onClick={onSignOut}><LogOut size={15} aria-hidden="true" /> {t('session.signOut')}</Button>
        </div>
      </header>

      <div className="workspace-layout">
        <nav className="sidebar" aria-label={t('navigation.label')}>
          <p className="eyebrow">{t('navigation.workspace')}</p>
          {navigation.map(item => (
            <button key={item.page} className={current === item.page ? 'active' : ''}
              aria-current={current === item.page ? 'page' : undefined}
              onClick={() => setPage(item.page)}>{t(item.labelKey)}</button>
          ))}
        </nav>

        <main id="workspace" className="workspace">
          {session.isLoading ? <Panel title={t('common.loading')} level={1}><span /></Panel> : null}
          {session.isError ? <Notice tone="danger">{t('session.profileUnavailable')}</Notice> : null}
          {user ? (
            <WorkspacePageView client={client} user={user} page={current}
              openPatients={openPatients} activePatientId={activePatientId} limitReached={limitReached}
              onNavigate={setPage} onOpenPatient={openPatient} onActivatePatient={setActivePatientId}
              onClosePatient={closePatient} onSearchPatients={() => { setActivePatientId(undefined); setLimitReached(false); }} />
          ) : null}
        </main>
      </div>

      {paletteOpen ? (
        <CommandPalette onClose={() => setPaletteOpen(false)} onSignOut={onSignOut}
          destinations={navigation} onNavigate={destination => setPage(destination)}
          openPatients={openPatients} activePatientId={activePatientId}
          onActivatePatient={id => { setPage('patients'); setActivePatientId(id); }}
          onClosePatient={closePatient} />
      ) : null}
    </div>
  );
}

function WorkspacePageView({
  client, user, page, openPatients, activePatientId, limitReached,
  onNavigate, onOpenPatient, onActivatePatient, onClosePatient, onSearchPatients,
}: {
  client: ApiClient;
  user: CurrentUser;
  page: WorkspacePage;
  openPatients: PatientSummary[];
  activePatientId?: string;
  limitReached: boolean;
  onNavigate: (page: WorkspacePage) => void;
  onOpenPatient: (patient: PatientSummary) => void;
  onActivatePatient: (id: string) => void;
  onClosePatient: (id: string) => void;
  onSearchPatients: () => void;
}) {
  const { t } = useTranslation();
  const activePatient = openPatients.find(candidate => candidate.id === activePatientId);

  if (page === 'patients') {
    if (!can(user, 'patients')) return <Notice tone="warning">{t('common.notPermitted')}</Notice>;
    return (
      <PatientWorkspace client={client} user={user} openPatients={openPatients} activeId={activePatientId}
        limitReached={limitReached} onOpen={onOpenPatient} onActivate={onActivatePatient}
        onClose={onClosePatient} onSearch={onSearchPatients} />
    );
  }
  if (page === 'tasks') {
    if (!can(user, 'tasks')) return <Notice tone="warning">{t('common.notPermitted')}</Notice>;
    return <Worklist client={client} user={user} patient={activePatient} />;
  }
  if (page === 'admin') {
    if (!can(user, 'administration')) return <Notice tone="warning">{t('common.notPermitted')}</Notice>;
    return <Administration client={client} />;
  }
  if (page === 'privacy') {
    if (!can(user, 'privacy')) return <Notice tone="warning">{t('common.notPermitted')}</Notice>;
    return <PrivacyOffice client={client} />;
  }
  return <RoleDashboard client={client} user={user} patient={activePatient} onNavigate={onNavigate} />;
}
