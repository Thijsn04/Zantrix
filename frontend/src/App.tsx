import { useTranslation } from 'react-i18next';
import { useAuth } from 'react-oidc-context';
import { LogIn, Stethoscope, SearchX, ShieldCheck } from 'lucide-react';
import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { InactivityProvider } from './components/InactivityProvider';
import { PrivacyDashboard } from './pages/PrivacyDashboard';
import { AppShell } from './components/AppShell';
import { PatientsModule } from './pages/pmi/PatientsModule';

function Dashboard({ roles }: { roles: string[] }) {
  const { t } = useTranslation();
  
  if (roles.includes('PRIVACY_OFFICER')) {
    return (
      <div className="h-full flex flex-col items-center justify-center text-slate-400">
        <ShieldCheck size={48} className="mb-4 text-slate-300" />
        <h2 className="text-lg font-medium text-slate-600 mb-1">{t('dashboard.privacy_title')}</h2>
        <p className="text-sm">{t('dashboard.privacy_desc')}</p>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col items-center justify-center text-slate-400">
      <SearchX size={48} className="mb-4 text-slate-300" />
      <h2 className="text-lg font-medium text-slate-600 mb-1">{t('dashboard.no_patient')}</h2>
      <p className="text-sm">{t('dashboard.no_patient_desc')}</p>
    </div>
  );
}

function MainApp() {
  const auth = useAuth();
  const [breakGlassStatus, setBreakGlassStatus] = useState<string | null>(null);
  const [userProfile, setUserProfile] = useState<any>(null);

  let roles: string[] = [];
  if (auth.user?.access_token) {
    try {
      const payload = JSON.parse(atob(auth.user.access_token.split('.')[1]));
      roles = payload.realm_access?.roles || [];
      roles = roles.filter((r: string) => !['offline_access', 'uma_authorization', 'default-roles-zantrix'].includes(r));
    } catch (e) {
      console.error('Failed to parse access token', e);
    }
  }

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        await fetch('http://localhost:8080/api/v1/iam/auth/login', {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
        }).catch(() => {});

        const response = await fetch('http://localhost:8080/api/v1/iam/me', {
          headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
        });
        if (response.ok) {
          const data = await response.json();
          setUserProfile(data);
        }
      } catch (e) {}
    };
    if (auth.isAuthenticated) fetchProfile();
  }, [auth.isAuthenticated, auth.user?.access_token]);

  const handleBreakTheGlass = async (reason: string) => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/iam/break-the-glass', {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${auth.user?.access_token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ reason })
      });
      if (response.ok) {
        setBreakGlassStatus("Noodtoegang geactiveerd. Alle acties worden gelogd.");
      } else {
        setBreakGlassStatus("Fout bij escaleren rechten.");
      }
    } catch (e) {
      setBreakGlassStatus("Systeemfout: backend onbereikbaar.");
    }
  };

  if (auth.isLoading) return <div className="h-screen w-screen flex items-center justify-center text-slate-500 font-medium bg-slate-50">Authenticatie laden...</div>;
  if (auth.error) return <div className="h-screen w-screen flex items-center justify-center text-red-600 bg-slate-50">Authenticatie fout: {auth.error.message}</div>;

  if (!auth.isAuthenticated) {
    return (
      <div className="h-screen w-screen flex items-center justify-center bg-slate-200">
        <div className="bg-white p-8 rounded shadow-md border border-slate-300 text-center max-w-sm w-full">
          <div className="flex justify-center mb-6">
            <div className="w-14 h-14 bg-zantrix-blue rounded flex items-center justify-center text-white shadow-sm">
              <Stethoscope size={28} />
            </div>
          </div>
          <h1 className="text-xl font-semibold mb-1 tracking-tight text-slate-900">Zantrix EPD</h1>
          <p className="text-xs text-slate-500 mb-8">Klinisch Informatiesysteem</p>
          <button 
            onClick={() => void auth.signinRedirect()}
            className="w-full bg-zantrix-blue text-white py-2 px-4 rounded text-sm font-medium flex items-center justify-center gap-2 hover:bg-blue-700 transition shadow-sm"
          >
            <LogIn size={16} /> Aanmelden
          </button>
        </div>
      </div>
    );
  }

  return (
    <InactivityProvider>
      <AppShell 
        userProfile={userProfile} 
        roles={roles} 
        onBreakGlass={handleBreakTheGlass}
        breakGlassStatus={breakGlassStatus}
      >
        <Routes>
          <Route path="/" element={<Dashboard roles={roles} />} />
          <Route path="/patients" element={<PatientsModule />} />
          <Route path="/audit-logs" element={<PrivacyDashboard />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AppShell>
    </InactivityProvider>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <MainApp />
    </BrowserRouter>
  );
}
