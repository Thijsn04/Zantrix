import { useState } from 'react';
import type { ReactNode } from 'react';
import { useAuth } from 'react-oidc-context';
import { Stethoscope, LogOut, ShieldCheck, Search, Settings, Home, FileText, Server, LockOpen, X } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';

interface AppShellProps {
  children: ReactNode;
  userProfile?: any;
  roles: string[];
  onBreakGlass: (reason: string) => void;
  breakGlassStatus: string | null;
}

export function AppShell({ children, userProfile, roles, onBreakGlass, breakGlassStatus }: AppShellProps) {
  const auth = useAuth();
  const location = useLocation();
  const [showBTGModal, setShowBTGModal] = useState(false);
  const [btgReason, setBtgReason] = useState("");
  const isPrivacyOfficer = roles.includes('PRIVACY_OFFICER');

  const navItems = [
    { name: 'Startscherm', path: '/', icon: Home },
    { name: 'Patiëntendossier', path: '/patients', icon: FileText },
  ];

  if (isPrivacyOfficer) {
    navItems.push({ name: 'Audit Logs', path: '/audit-logs', icon: ShieldCheck });
  }

  const activeNavItem = navItems.find(item => item.path === location.pathname) || { name: 'Onbekend' };

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
    <div className="h-screen w-screen flex flex-col bg-slate-50 text-xs md:text-sm font-sans overflow-hidden">
      
      {/* 1. Menu Bar (Top) */}
      <header className="h-12 bg-zantrix-blue text-white flex items-center justify-between px-3 shrink-0">
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2">
            <Stethoscope size={18} />
            <span className="font-semibold tracking-wide text-sm">ZANTRIX EPD</span>
          </div>
          <div className="hidden md:flex gap-4 text-xs font-medium text-blue-100">
            <span className="hover:text-white cursor-pointer transition">Bestand</span>
            <span className="hover:text-white cursor-pointer transition">Bewerken</span>
            <span className="hover:text-white cursor-pointer transition">Help</span>
          </div>
        </div>

        {/* Global Search */}
        <div className="flex-1 max-w-sm mx-4 relative hidden sm:block">
          <Search size={14} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-white/50" />
          <input 
            type="text" 
            placeholder="Patiënt zoeken (F3)..." 
            className="w-full bg-white/10 border border-white/20 text-white placeholder-white/50 text-xs rounded pl-8 pr-3 py-1 focus:outline-none focus:bg-white/20 transition"
          />
        </div>

        <div className="flex items-center gap-3">
          <button 
            onClick={() => setShowBTGModal(true)}
            title="Noodtoegang (Break The Glass)"
            className="flex items-center justify-center w-8 h-8 rounded bg-red-600 hover:bg-red-700 transition shadow-sm border border-red-800"
          >
            <LockOpen size={14} className="text-white" />
          </button>
          
          <div className="h-5 w-px bg-white/20 mx-1"></div>

          <div className="flex items-center gap-2">
            <div className="text-right hidden md:block">
              <div className="text-xs font-medium">{auth.user?.profile.given_name} {auth.user?.profile.family_name}</div>
              <div className="text-[10px] text-white/70 uppercase tracking-wider">{roles[0] || 'GEBRUIKER'}</div>
            </div>
          </div>
          
          <button onClick={handleLogout} className="p-1.5 hover:bg-white/10 rounded transition" title="Uitloggen">
            <LogOut size={16} />
          </button>
        </div>
      </header>

      {/* 2. Tab Bar (Sub-Top) */}
      <div className="h-8 bg-slate-200 border-b border-slate-300 flex items-end px-2 gap-1 shrink-0">
        <div className="bg-white px-4 py-1.5 rounded-t-md border-t border-l border-r border-slate-300 text-xs font-medium text-zantrix-blue shadow-sm flex items-center gap-2">
          {activeNavItem.name}
        </div>
      </div>

      {/* 3. Main Content Area */}
      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar Navigation */}
        <aside className="w-48 bg-slate-100 border-r border-slate-300 flex flex-col shrink-0">
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
                      : 'text-slate-700 hover:bg-slate-200'
                  }`}
                >
                  <item.icon size={14} className={isActive ? 'text-white' : 'text-slate-500'} />
                  {item.name}
                </Link>
              );
            })}
          </nav>

          <div className="p-2 border-t border-slate-300">
            <button className="flex items-center gap-2 text-xs text-slate-700 w-full px-2 py-1.5 rounded hover:bg-slate-200 transition">
              <Settings size={14} className="text-slate-500" />
              Instellingen
            </button>
          </div>
        </aside>

        {/* Content Pane */}
        <main className="flex-1 bg-white overflow-auto relative">
          {breakGlassStatus && (
            <div className="bg-red-50 border-b border-red-200 p-2 text-red-700 text-xs font-medium text-center flex items-center justify-center gap-2">
              <LockOpen size={14} /> {breakGlassStatus}
            </div>
          )}
          <div className="p-4 h-full">
            {children}
          </div>
        </main>
      </div>

      {/* 4. Status Bar (Bottom) */}
      <footer className="h-6 bg-slate-200 border-t border-slate-300 flex items-center justify-between px-3 text-[10px] text-slate-600 shrink-0 select-none">
        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1"><Server size={10} /> Verbonden (12ms)</span>
          <span>Sessie: {auth.user?.profile.preferred_username}</span>
        </div>
        <div className="flex items-center gap-4">
          <span>{userProfile?.language?.toUpperCase() || 'NL'} | {userProfile?.theme || 'Licht'}</span>
          <span>Versie 1.0.1 (Productie)</span>
        </div>
      </footer>

      {/* BTG Modal */}
      {showBTGModal && (
        <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md overflow-hidden flex flex-col">
            <div className="bg-red-600 px-4 py-3 flex items-center justify-between text-white">
              <div className="flex items-center gap-2 font-semibold">
                <LockOpen size={16} /> Break The Glass Activeren
              </div>
              <button onClick={() => setShowBTGModal(false)} className="hover:bg-red-700 p-1 rounded">
                <X size={16} />
              </button>
            </div>
            <div className="p-5">
              <p className="text-sm text-slate-700 mb-4">
                Je staat op het punt om noodrechten (Break The Glass) te activeren. Dit geeft je toegang tot afgeschermde dossiers. 
                <strong> Deze actie triggert een alert en vereist achteraf verantwoording.</strong>
              </p>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Verplichte Reden:</label>
              <textarea 
                value={btgReason}
                onChange={e => setBtgReason(e.target.value)}
                className="w-full border border-slate-300 rounded p-2 text-sm focus:outline-none focus:ring-1 focus:ring-red-600"
                rows={3}
                placeholder="Bijv: Spoedopname SEH patiënt X, buiten reguliere diensttijd."
              />
            </div>
            <div className="px-5 py-3 bg-slate-50 border-t border-slate-200 flex justify-end gap-2">
              <button onClick={() => setShowBTGModal(false)} className="px-3 py-1.5 rounded border border-slate-300 text-sm font-medium hover:bg-slate-100 transition">Annuleren</button>
              <button onClick={submitBreakGlass} disabled={!btgReason.trim()} className="px-3 py-1.5 rounded bg-red-600 text-white text-sm font-medium hover:bg-red-700 transition disabled:opacity-50">Activeren</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
