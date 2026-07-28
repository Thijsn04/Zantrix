import { useEffect, useMemo, useState } from 'react';
import { QueryClient, QueryClientProvider, useQuery } from '@tanstack/react-query';
import { Command, LogOut, Moon, Search, Sun } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { ApiClient } from '../lib/api/client';
import { Button } from '../design/Button';
import { CommandPalette } from './CommandPalette';
import { IdleLock } from './IdleLock';
import { activePage, landingPath, navigationFor } from './navigation';
import { can, primaryRole } from '../lib/roles';
import type { CurrentUser } from '../lib/api/types';
import { RoleDashboard } from '../features/home/RoleDashboard';
import { PatientWorkspace } from '../features/patients/PatientWorkspace';
import { DepartmentSchedule } from '../features/schedule/DepartmentSchedule';
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
  const location = useLocation();
  const navigate = useNavigate();
  const [paletteOpen, setPaletteOpen] = useState(false);
  const [dark, setDark] = useState(() => localStorage.getItem('zantrix-theme') === 'dark');
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
  const current = activePage(location.pathname);

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
              onClick={() => navigate(item.path)}>{t(item.labelKey)}</button>
          ))}
        </nav>

        <main id="workspace" className="workspace">
          {session.isLoading ? <Panel title={t('common.loading')} level={1}><span /></Panel> : null}
          {session.isError ? <Notice tone="danger">{t('session.profileUnavailable')}</Notice> : null}
          {user ? <WorkspaceRoutes client={client} user={user} /> : null}
        </main>
      </div>

      {paletteOpen ? (
        <CommandPalette onClose={() => setPaletteOpen(false)} onSignOut={onSignOut}
          destinations={navigation} onNavigate={path => navigate(path)} />
      ) : null}

      {user ? <IdleLock user={user.displayName ?? user.username ?? ''} onSignOut={onSignOut} /> : null}
    </div>
  );
}

/** Every clinical view is addressable, so a second window can be opened at one. */
function WorkspaceRoutes({ client, user }: { client: ApiClient; user: CurrentUser }) {
  return (
    <Routes>
      <Route path="/" element={<Navigate to={landingPath(user)} replace />} />
      <Route path="/home" element={<RoleDashboard client={client} user={user} />} />
      <Route path="/patients/:patientId/:section?" element={<Guarded user={user} capability="patients">
        <PatientWorkspace client={client} user={user} />
      </Guarded>} />
      <Route path="/patients" element={<Guarded user={user} capability="patients">
        <PatientWorkspace client={client} user={user} />
      </Guarded>} />
      <Route path="/schedule" element={<Guarded user={user} capability="scheduling">
        <DepartmentSchedule client={client} canAdmit={can(user, 'scheduling')} />
      </Guarded>} />
      <Route path="/worklist" element={<Guarded user={user} capability="tasks">
        <Worklist client={client} user={user} />
      </Guarded>} />
      <Route path="/admin" element={<Guarded user={user} capability="administration">
        <Administration client={client} canVerifyAudit={can(user, 'privacy')} />
      </Guarded>} />
      <Route path="/privacy" element={<Guarded user={user} capability="privacy">
        <PrivacyOffice client={client} />
      </Guarded>} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

/**
 * A destination the user's role does not cover explains itself rather than
 * rendering a blank area. The server remains the thing that enforces.
 */
function Guarded({ user, capability, children }: {
  user: CurrentUser; capability: Parameters<typeof can>[1]; children: React.ReactNode;
}) {
  const { t } = useTranslation();
  if (!can(user, capability)) return <Notice tone="warning">{t('common.notPermitted')}</Notice>;
  return <>{children}</>;
}

function NotFound() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  return (
    <Panel title={t('notFound.title')} level={1} subtitle={t('notFound.description')}>
      <div className="form-actions">
        <Button onClick={() => navigate('/')}>{t('notFound.back')}</Button>
      </div>
    </Panel>
  );
}
