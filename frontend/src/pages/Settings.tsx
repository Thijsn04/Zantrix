import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from 'react-oidc-context';
import { Settings as SettingsIcon, Save } from 'lucide-react';
import i18nInstance from '../i18n';

export function Settings() {
  const { t } = useTranslation();
  const auth = useAuth();
  const [theme, setTheme] = useState('light');
  const [language, setLanguage] = useState('nl');
  const [status, setStatus] = useState<{type: 'success' | 'error', message: string} | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/v1/iam/me', {
          headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
        });
        if (response.ok) {
          const data = await response.json();
          if (data.theme) setTheme(data.theme);
          if (data.language) setLanguage(data.language);
        }
      } catch (e) {
        console.error(e);
      }
    };
    if (auth.isAuthenticated) {
      fetchProfile();
    }
  }, [auth.isAuthenticated, auth.user?.access_token]);

  const handleSave = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/iam/me', {
        method: 'PUT',
        headers: { 
          'Authorization': `Bearer ${auth.user?.access_token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ theme, language })
      });
      
      if (response.ok) {
        setStatus({ type: 'success', message: t('settings.success') });
        void i18nInstance.changeLanguage(language);
        // Dispatch an event so AppShell can update its footer if needed (or we can just reload)
        window.location.reload();
      } else {
        setStatus({ type: 'error', message: t('settings.error') });
      }
    } catch (e) {
      setStatus({ type: 'error', message: t('settings.error') });
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center text-white shadow-sm">
          <SettingsIcon size={20} />
        </div>
        <div>
          <h1 className="text-xl font-semibold text-slate-900 dark:text-white tracking-tight">{t('settings.title')}</h1>
        </div>
      </div>

      <div className="bg-white dark:bg-slate-800 rounded-lg shadow-sm border border-slate-200 dark:border-slate-700 p-6 space-y-6">
        {status && (
          <div className={`p-3 rounded text-sm ${status.type === 'success' ? 'bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400 border border-green-200 dark:border-green-800' : 'bg-red-50 dark:bg-red-900/30 text-red-700 dark:text-red-400 border border-red-200 dark:border-red-800'}`}>
            {status.message}
          </div>
        )}

        <div>
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">{t('settings.theme.label')}</label>
          <div className="flex gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input 
                type="radio" 
                name="theme" 
                value="light" 
                checked={theme === 'light'} 
                onChange={(e) => setTheme(e.target.value)}
                className="text-zantrix-blue focus:ring-zantrix-blue dark:bg-slate-700 dark:border-slate-600"
              />
              <span className="text-sm text-slate-700 dark:text-slate-300">{t('settings.theme.light')}</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input 
                type="radio" 
                name="theme" 
                value="dark" 
                checked={theme === 'dark'} 
                onChange={(e) => setTheme(e.target.value)}
                className="text-zantrix-blue focus:ring-zantrix-blue dark:bg-slate-700 dark:border-slate-600"
              />
              <span className="text-sm text-slate-700 dark:text-slate-300">{t('settings.theme.dark')}</span>
            </label>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">{t('settings.language.label')}</label>
          <div className="flex gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input 
                type="radio" 
                name="language" 
                value="nl" 
                checked={language === 'nl'} 
                onChange={(e) => setLanguage(e.target.value)}
                className="text-zantrix-blue focus:ring-zantrix-blue dark:bg-slate-700 dark:border-slate-600"
              />
              <span className="text-sm text-slate-700 dark:text-slate-300">{t('settings.language.nl')}</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input 
                type="radio" 
                name="language" 
                value="en" 
                checked={language === 'en'} 
                onChange={(e) => setLanguage(e.target.value)}
                className="text-zantrix-blue focus:ring-zantrix-blue dark:bg-slate-700 dark:border-slate-600"
              />
              <span className="text-sm text-slate-700 dark:text-slate-300">{t('settings.language.en')}</span>
            </label>
          </div>
        </div>

        <div className="pt-4 border-t border-slate-200 dark:border-slate-700 flex justify-end">
          <button 
            onClick={handleSave}
            className="flex items-center gap-2 bg-zantrix-blue text-white px-4 py-2 rounded text-sm font-medium hover:bg-blue-700 transition shadow-sm"
          >
            <Save size={16} />
            {t('settings.save')}
          </button>
        </div>
      </div>
    </div>
  );
}
