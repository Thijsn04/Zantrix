import { useState, useEffect, useRef } from 'react';
import type { ReactNode } from 'react';
import { useAuth } from 'react-oidc-context';
import { Stethoscope, LogOut, ShieldCheck, Search, Settings, Home, FileText, Server, LockOpen, X, User, Database, Calendar } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

/**
 * Properties for the AppShell component.
 */
interface AppShellProps {
  children: ReactNode;
  userProfile?: any;
  roles: string[];
  onBreakGlass: (reason: string) => void;
  breakGlassStatus: string | null;
}

/**
 * The main application shell layout component.
 * 
 * Provides the global navigation, search functionality, theming context, 
 * and persistent status bar across all authenticated routes. Handles the 
 * "Break the Glass" (BTG) emergency access workflow UI.
 *
 * @param {AppShellProps} props - The properties for configuring the AppShell.
 */
export function AppShell({ children, userProfile, roles, onBreakGlass, breakGlassStatus }: AppShellProps) {
  const { t } = useTranslation();
  const auth = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [showBTGModal, setShowBTGModal] = useState(false);
  const [btgReason, setBtgReason] = useState("");
  const isPrivacyOfficer = roles.includes('PRIVACY_OFFICER');

  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [showSearchDropdown, setShowSearchDropdown] = useState(false);
  const searchRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setShowSearchDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }
    const delayDebounceFn = setTimeout(() => {
      setIsSearching(true);
      fetch(`http://localhost:8080/api/v1/patients/search?q=${encodeURIComponent(searchQuery)}`, {
        headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
      })
      .then(res => res.json())
      .then(data => {
        setSearchResults(Array.isArray(data) ? data.slice(0, 5) : []);
        setIsSearching(false);
      })
      .catch(() => {
        setSearchResults([]);
        setIsSearching(false);
      });
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [searchQuery, auth.user?.access_token]);

  const handlePatientSelect = (patient: any) => {
    setShowSearchDropdown(false);
    setSearchQuery('');
    const patientName = patient.isEmergency ? 'John Doe (Nood)' : `${patient.lastName}, ${patient.firstName}`;
    navigate(`/patients?open=${patient.id}&name=${encodeURIComponent(patientName)}`);
  };

  const navItems = [
    { name: t('shell.nav.home'), path: '/', icon: Home },
    { name: t('shell.nav.patient_dossier'), path: '/patients', icon: FileText },
    { name: 'Scheduling', path: '/schedule', icon: Calendar },
  ];

  if (roles.includes('SYSTEM_ADMIN')) {
    navItems.push({ name: t('shell.nav.terminology'), path: '/terminology', icon: Database });
  }

  if (isPrivacyOfficer) {
    navItems.push({ name: t('shell.nav.audit_logs'), path: '/audit-logs', icon: ShieldCheck });
  }

  const activeNavItem = navItems.find(item => item.path === location.pathname) || { name: t('shell.nav.unknown') };

  const submitBreakGlass = () => {
    if (btgReason.trim()) {
      onBreakGlass(btgReason);
      setShowBTGModal(false);
      setBtgReason("");
    }
  };

  const handleLogout = async () => {
    try {
      await fetch('http://localhost:8080/api/v1/iam/auth/logout', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
      });
    } catch(e) {}
    void auth.signoutRedirect();
  };

  return (
    <div className="h-screen w-screen flex flex-col bg-slate-50 dark:bg-slate-900 text-xs md:text-sm font-sans overflow-hidden">
      
      {/* 1. Menu Bar (Top) */}
      <header className="h-12 bg-zantrix-blue text-white flex items-center justify-between px-3 shrink-0">
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2">
            <Stethoscope size={18} />
            <span className="font-semibold tracking-wide text-sm">ZANTRIX EPD</span>
          </div>
          <div className="hidden md:flex gap-4 text-xs font-medium text-blue-100">
            <span className="hover:text-white cursor-pointer transition">{t('shell.menu.file')}</span>
            <span className="hover:text-white cursor-pointer transition">{t('shell.menu.edit')}</span>
            <span className="hover:text-white cursor-pointer transition">{t('shell.menu.help')}</span>
          </div>
        </div>

        {/* Global Search */}
        <div ref={searchRef} className="flex-1 max-w-sm mx-4 relative hidden sm:block">
          <Search size={14} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-white/50" />
          <input 
            type="text" 
            placeholder={t('shell.search_placeholder')} 
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setShowSearchDropdown(true);
            }}
            onFocus={() => {
              if (searchQuery.trim()) setShowSearchDropdown(true);
            }}
            className="w-full bg-white/10 border border-white/20 text-white placeholder-white/50 text-xs rounded pl-8 pr-3 py-1.5 focus:outline-none focus:bg-white/20 transition"
          />
          
          {showSearchDropdown && searchQuery.trim() && (
            <div className="absolute top-full mt-1 w-full bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded-md shadow-lg z-50 overflow-hidden text-slate-800 dark:text-slate-200">
              {isSearching ? (
                <div className="p-3 text-xs text-slate-500 dark:text-slate-400 text-center">{t('shell.search_loading')}</div>
              ) : searchResults.length === 0 ? (
                <div className="p-3 text-xs text-slate-500 dark:text-slate-400 text-center">{t('shell.search_no_results')}</div>
              ) : (
                <div className="flex flex-col">
                  {searchResults.map(p => (
                    <button
                      key={p.id}
                      onClick={() => handlePatientSelect(p)}
                      className="text-left px-3 py-2 text-xs border-b border-slate-100 dark:border-slate-700/50 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition flex items-center gap-2 last:border-0"
                    >
                      <div className="w-6 h-6 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center shrink-0">
                        <User size={12} className="text-slate-500 dark:text-slate-400" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="font-medium truncate">
                          {p.isEmergency ? <span className="text-red-500">John Doe (Nood)</span> : `${p.lastName}, ${p.firstName}`}
                        </div>
                        <div className="text-[10px] text-slate-500 dark:text-slate-400 flex justify-between">
                          <span>{p.bsn || '-'}</span>
                          <span>{p.birthDate || '-'}</span>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        <div className="flex items-center gap-3">
          <button 
            onClick={() => setShowBTGModal(true)}
            title={t('shell.btg_tooltip')}
            className="flex items-center justify-center w-8 h-8 rounded bg-red-600 hover:bg-red-700 transition shadow-sm border border-red-800"
          >
            <LockOpen size={14} className="text-white" />
          </button>
          
          <div className="h-5 w-px bg-white/20 mx-1"></div>

          <div className="flex items-center gap-2">
            <div className="text-right hidden md:block">
              <div className="text-xs font-medium">{auth.user?.profile.given_name} {auth.user?.profile.family_name}</div>
              <div className="text-[10px] text-white/70 uppercase tracking-wider">{roles[0] || t('shell.role_user')}</div>
            </div>
          </div>
          
          <button onClick={handleLogout} className="p-1.5 hover:bg-white/10 rounded transition" title={t('shell.logout_tooltip')}>
            <LogOut size={16} />
          </button>
        </div>
      </header>

      {/* 2. Tab Bar (Sub-Top) */}
      <div className="h-8 bg-slate-200 dark:bg-slate-800 border-b border-slate-300 dark:border-slate-700 flex items-end px-2 gap-1 shrink-0">
        <div className="bg-white dark:bg-slate-900 px-4 py-1.5 rounded-t-md border-t border-l border-r border-slate-300 dark:border-slate-700 text-xs font-medium text-zantrix-blue shadow-sm flex items-center gap-2">
          {activeNavItem.name}
        </div>
      </div>

      {/* 3. Main Content Area */}
      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar Navigation */}
        <aside className="w-48 bg-slate-100 dark:bg-slate-800/50 border-r border-slate-300 dark:border-slate-700 flex flex-col shrink-0">
          <nav className="flex-1 py-2 flex flex-col gap-0.5 px-1">
            {navItems.map(item => {
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center gap-2 px-2 py-1.5 text-xs font-medium rounded transition ${
                    isActive 
                      ? 'bg-zantrix-blue text-white shadow-sm' 
                      : 'text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700'
                  }`}
                >
                  <item.icon size={14} className={isActive ? 'text-white' : 'text-slate-500 dark:text-slate-400'} />
                  {item.name}
                </Link>
              );
            })}
          </nav>

          <div className="p-2 border-t border-slate-300 dark:border-slate-700">
            <Link to="/settings" className="flex items-center gap-2 text-xs text-slate-700 dark:text-slate-300 w-full px-2 py-1.5 rounded hover:bg-slate-200 dark:hover:bg-slate-700 transition">
              <Settings size={14} className="text-slate-500 dark:text-slate-400" />
              {t('shell.settings')}
            </Link>
          </div>
        </aside>

        {/* Content Pane */}
        <main className="flex-1 bg-white dark:bg-slate-900 overflow-auto relative">
          {breakGlassStatus && (
            <div className="bg-red-50 dark:bg-red-900/30 border-b border-red-200 dark:border-red-800 p-2 text-red-700 dark:text-red-400 text-xs font-medium text-center flex items-center justify-center gap-2">
              <LockOpen size={14} /> {breakGlassStatus}
            </div>
          )}
          <div className="p-4 h-full">
            {children}
          </div>
        </main>
      </div>

      {/* 4. Status Bar (Bottom) */}
      <footer className="h-6 bg-slate-200 dark:bg-slate-800 border-t border-slate-300 dark:border-slate-700 flex items-center justify-between px-3 text-[10px] text-slate-600 dark:text-slate-400 shrink-0 select-none">
        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1"><Server size={10} /> {t('shell.footer.connected')} (12ms)</span>
          <span>{t('shell.footer.session')}: {auth.user?.profile.preferred_username}</span>
        </div>
        <div className="flex items-center gap-4">
          <span>{userProfile?.language?.toUpperCase() || 'NL'} | {userProfile?.theme === 'dark' ? t('settings.theme.dark') : t('settings.theme.light')}</span>
          <span>{t('shell.footer.version')}</span>
        </div>
      </footer>

      {/* BTG Modal */}
      {showBTGModal && (
        <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-md overflow-hidden flex flex-col">
            <div className="bg-red-600 px-4 py-3 flex items-center justify-between text-white">
              <div className="flex items-center gap-2 font-semibold">
                <LockOpen size={16} /> {t('shell.btg_modal.title')}
              </div>
              <button onClick={() => setShowBTGModal(false)} className="hover:bg-red-700 p-1 rounded">
                <X size={16} />
              </button>
            </div>
            <div className="p-5">
              <p className="text-sm text-slate-700 dark:text-slate-300 mb-4">
                {t('shell.btg_modal.warning')} 
                <strong className="dark:text-white">{t('shell.btg_modal.alert_note')}</strong>
              </p>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">{t('shell.btg_modal.reason_label')}</label>
              <textarea 
                value={btgReason}
                onChange={e => setBtgReason(e.target.value)}
                className="w-full border border-slate-300 dark:border-slate-600 rounded p-2 text-sm focus:outline-none focus:ring-1 focus:ring-red-600 dark:bg-slate-700 dark:text-white"
                rows={3}
                placeholder={t('shell.btg_modal.reason_placeholder')}
              />
            </div>
            <div className="px-5 py-3 bg-slate-50 dark:bg-slate-900 border-t border-slate-200 dark:border-slate-700 flex justify-end gap-2">
              <button onClick={() => setShowBTGModal(false)} className="px-3 py-1.5 rounded border border-slate-300 dark:border-slate-600 text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800 transition dark:text-slate-300">{t('shell.btg_modal.cancel')}</button>
              <button onClick={submitBreakGlass} disabled={!btgReason.trim()} className="px-3 py-1.5 rounded bg-red-600 text-white text-sm font-medium hover:bg-red-700 transition disabled:opacity-50">{t('shell.btg_modal.activate')}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
