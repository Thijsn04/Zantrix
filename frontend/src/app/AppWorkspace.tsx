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
import { PatientRegistry } from '../features/patients/PatientRegistry';
import { PatientChart } from '../features/chart/PatientChart';
import { AppointmentDesk } from '../features/schedule/AppointmentDesk';
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
  const [patient, setPatient] = useState<PatientSummary>();
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
            <WorkspacePageView client={client} user={user} page={current} patient={patient}
              onNavigate={setPage} onSelectPatient={setPatient} />
          ) : null}
        </main>
      </div>

      {paletteOpen ? (
        <CommandPalette onClose={() => setPaletteOpen(false)} onSignOut={onSignOut}
          destinations={navigation} onNavigate={destination => setPage(destination)}
          patient={patient} onClearPatient={() => setPatient(undefined)} />
      ) : null}
    </div>
  );
}

function WorkspacePageView({ client, user, page, patient, onNavigate, onSelectPatient }: {
  client: ApiClient;
  user: CurrentUser;
  page: WorkspacePage;
  patient?: PatientSummary;
  onNavigate: (page: WorkspacePage) => void;
  onSelectPatient: (patient?: PatientSummary) => void;
}) {
  const { t } = useTranslation();

  if (page === 'patients') {
    if (!can(user, 'patients')) return <Notice tone="warning">{t('common.notPermitted')}</Notice>;
    return patient
      ? <PatientChart client={client} patient={patient} user={user} onClear={() => onSelectPatient(undefined)} />
      : <PatientRegistry client={client} onSelect={onSelectPatient} />;
  }
  if (page === 'schedule') {
    if (!can(user, 'scheduling')) return <Notice tone="warning">{t('common.notPermitted')}</Notice>;
    return <AppointmentDesk client={client} patient={patient} canManageSchedules={can(user, 'manageSchedules')} />;
  }
  if (page === 'tasks') {
    if (!can(user, 'tasks')) return <Notice tone="warning">{t('common.notPermitted')}</Notice>;
    return <Worklist client={client} user={user} patient={patient} />;
  }
  if (page === 'admin') {
    if (!can(user, 'administration')) return <Notice tone="warning">{t('common.notPermitted')}</Notice>;
    return <Administration client={client} />;
  }
  if (page === 'privacy') {
    if (!can(user, 'privacy')) return <Notice tone="warning">{t('common.notPermitted')}</Notice>;
    return <PrivacyOffice client={client} />;
  }
  return <RoleDashboard client={client} user={user} patient={patient} onNavigate={onNavigate} />;
}
