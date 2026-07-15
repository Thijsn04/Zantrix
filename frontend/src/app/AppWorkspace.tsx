import { useEffect, useMemo, useState } from 'react';
import { QueryClient, QueryClientProvider, useQuery } from '@tanstack/react-query';
import { Command, LogOut, Moon, Search, Sun } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { ApiClient } from '../lib/api/client';
import { Button } from '../design/Button';
import { CommandPalette } from './CommandPalette';
import { PatientContext } from './PatientContext';
import { ClinicalWorkspace, type WorkspacePage } from './ClinicalWorkspace';
import type { PatientSummary } from '../lib/api/types';

const queryClient = new QueryClient({ defaultOptions: { queries: { retry: 1, staleTime: 60_000 } } });

export function AppWorkspace(props: { accessToken: string; onSignOut: () => void }) {
  return <QueryClientProvider client={queryClient}><Workspace {...props} /></QueryClientProvider>;
}

function Workspace({ accessToken, onSignOut }: { accessToken: string; onSignOut: () => void }) {
  const { t } = useTranslation();
  const [paletteOpen, setPaletteOpen] = useState(false);
  const [dark, setDark] = useState(() => localStorage.getItem('zantrix-theme') === 'dark');
  const [page, setPage] = useState<WorkspacePage>('home');
  const [patient, setPatient] = useState<PatientSummary>();
  const client = useMemo(() => new ApiClient(accessToken), [accessToken]);
  const session = useQuery({ queryKey: ['current-user'], queryFn: ({ signal }) => client.currentUser(signal) });

  useEffect(() => {
    document.documentElement.classList.toggle('dark', dark);
    localStorage.setItem('zantrix-theme', dark ? 'dark' : 'light');
  }, [dark]);
  useEffect(() => {
    const listener = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault();
        setPaletteOpen(true);
      }
    };
    window.addEventListener('keydown', listener);
    return () => window.removeEventListener('keydown', listener);
  }, []);

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label={t('app.home')}>Z</a>
        <Button className="command-trigger" onClick={() => setPaletteOpen(true)}>
          <Search size={16} aria-hidden="true" /> {t('command.open')} <kbd><Command size={12} aria-hidden="true" />K</kbd>
        </Button>
        <div className="topbar-actions">
          <span className="session-name">{session.data?.displayName ?? session.data?.username ?? t('session.signedIn')}</span>
          <Button className="quiet-button" aria-label={t('theme.toggle')} onClick={() => setDark(!dark)}>
            {dark ? <Sun size={16} aria-hidden="true" /> : <Moon size={16} aria-hidden="true" />}
          </Button>
          <Button className="quiet-button" onClick={onSignOut}><LogOut size={16} aria-hidden="true" /> {t('session.signOut')}</Button>
        </div>
      </header>
      <div className="workspace-layout">
        <nav className="sidebar" aria-label={t('navigation.label')}>
          <p className="eyebrow">{t('navigation.workspace')}</p>
          {(['home', 'patients', 'schedule', 'tasks'] as WorkspacePage[]).map(item =>
            <button key={item} className={page === item ? 'active' : ''} onClick={() => setPage(item)}>{t(`navigation.${item}`)}</button>)}
          {session.data?.roles.includes('ADMIN') && <button className={page === 'admin' ? 'active' : ''} onClick={() => setPage('admin')}>{t('navigation.admin')}</button>}
          {session.data?.roles.includes('PRIVACY_OFFICER') && <button className={page === 'privacy' ? 'active' : ''} onClick={() => setPage('privacy')}>{t('navigation.privacy')}</button>}
        </nav>
        <main id="workspace" className="workspace">
          <PatientContext patient={patient} />
          <ClinicalWorkspace client={client} user={session.data} page={page} selected={patient}
            onSelect={value => { setPatient(value); if (value) setPage('patients'); }} />
        </main>
      </div>
      <CommandPalette open={paletteOpen} onClose={() => setPaletteOpen(false)} onSignOut={onSignOut} />
    </div>
  );
}
