import { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { UserPlus, AlertCircle, Search } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export function PatientSearch({ onOpenPatient, onCreatePatient }: { onOpenPatient: (id: string, name: string) => void, onCreatePatient: () => void }) {
  const { t } = useTranslation();
  const auth = useAuth();
  const [patients, setPatients] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  const fetchPatients = (q: string = '') => {
    setLoading(true);
    const url = q ? `http://localhost:8080/api/v1/patients/search?q=${encodeURIComponent(q)}` : 'http://localhost:8080/api/v1/patients';
    fetch(url, {
      headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
    })
    .then(res => res.json())
    .then(data => {
      if (Array.isArray(data)) {
        setPatients(data);
      } else {
        console.error("Unexpected API response:", data);
        setPatients([]);
      }
      setLoading(false);
    })
    .catch((err) => {
      console.error("API Error:", err);
      setPatients([]);
      setLoading(false);
    });
  };

  useEffect(() => {
    fetchPatients(searchQuery);
  }, [auth.user?.access_token]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchPatients(searchQuery);
  };

  return (
    <div className="h-full flex flex-col">
      <div className="flex justify-between items-center mb-4 shrink-0">
        <h1 className="text-xl font-semibold text-slate-800 dark:text-white">{t('patient_search.title')}</h1>
      </div>
      
      <div className="bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded shadow-sm flex-1 flex flex-col overflow-hidden">
        <div className="bg-slate-50 dark:bg-slate-900/50 border-b border-slate-300 dark:border-slate-700 p-3 flex gap-2">
          <form onSubmit={handleSearch} className="flex-1 flex gap-2 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 dark:text-slate-500" size={16} />
            <input 
              type="text" 
              placeholder={t('patient_search.search_placeholder')} 
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="flex-1 pl-10 pr-4 py-2 border border-slate-300 dark:border-slate-600 rounded text-sm focus:outline-none focus:border-zantrix-blue focus:ring-1 focus:ring-zantrix-blue dark:bg-slate-700 dark:text-white"
            />
            <button type="submit" className="bg-slate-200 dark:bg-slate-700 text-slate-800 dark:text-slate-200 px-4 py-1.5 rounded text-sm font-medium hover:bg-slate-300 dark:hover:bg-slate-600 transition">
              {t('patient_search.search_button')}
            </button>
          </form>
          <button onClick={onCreatePatient} className="bg-zantrix-blue text-white px-3 py-1.5 rounded text-sm font-medium flex items-center gap-2 hover:bg-blue-700 transition">
            <UserPlus size={16} /> <span className="hidden sm:inline">{t('patient_search.new_patient')}</span>
          </button>
        </div>
        
        <div className="overflow-auto flex-1">
          <table className="w-full text-left text-sm border-collapse">
            <thead className="bg-slate-100 dark:bg-slate-900/50 text-slate-600 dark:text-slate-400 sticky top-0 border-b border-slate-300 dark:border-slate-700 z-10 shadow-sm">
              <tr>
                <th className="p-2 font-medium border-r border-slate-200 dark:border-slate-700">{t('patient_search.columns.bsn')}</th>
                <th className="p-2 font-medium border-r border-slate-200 dark:border-slate-700">{t('patient_search.columns.name')}</th>
                <th className="p-2 font-medium border-r border-slate-200 dark:border-slate-700">{t('patient_search.columns.birthDate')}</th>
                <th className="p-2 font-medium border-r border-slate-200 dark:border-slate-700">{t('patient_search.columns.gender')}</th>
                <th className="p-2 font-medium">{t('patient_search.columns.status')}</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={5} className="p-4 text-center text-slate-500 dark:text-slate-400">{t('patient_search.loading')}</td></tr>
              ) : patients.length === 0 ? (
                <tr><td colSpan={5} className="p-4 text-center text-slate-500 dark:text-slate-400">{t('patient_search.no_results')}</td></tr>
              ) : patients.map(p => (
                <tr 
                  key={p.id} 
                  onClick={() => onOpenPatient(p.id, p.isEmergency ? 'John Doe (Nood)' : `${p.lastName}, ${p.firstName}`)}
                  className="border-b border-slate-200 dark:border-slate-700/50 hover:bg-slate-50 dark:hover:bg-slate-700/50 cursor-pointer transition"
                >
                  <td className="p-2 border-r border-slate-200 dark:border-slate-700/50 font-mono dark:text-slate-300">{p.bsn || '-'}</td>
                  <td className="p-2 border-r border-slate-200 dark:border-slate-700/50 dark:text-white">
                    {p.isEmergency ? (
                      <span className="text-red-600 dark:text-red-400 flex items-center gap-1 font-semibold"><AlertCircle size={14} /> John Doe (Nood)</span>
                    ) : (
                      `${p.lastName || '-'}, ${p.firstName || '-'}`
                    )}
                  </td>
                  <td className="p-2 border-r border-slate-200 dark:border-slate-700/50 tabular-nums dark:text-slate-300">{p.birthDate || '-'}</td>
                  <td className="p-2 border-r border-slate-200 dark:border-slate-700/50 dark:text-slate-300">{p.gender}</td>
                  <td className="p-2">
                    {p.status === 'MERGED' ? (
                      <span className="bg-orange-100 dark:bg-orange-900/30 text-orange-800 dark:text-orange-400 px-2 py-0.5 rounded text-xs">{t('patient_search.status.merged')}</span>
                    ) : p.status === 'DECEASED' ? (
                      <span className="bg-slate-200 dark:bg-slate-700 text-slate-800 dark:text-slate-300 px-2 py-0.5 rounded text-xs">{t('patient_search.status.deceased')}</span>
                    ) : (
                      <span className="bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400 px-2 py-0.5 rounded text-xs">{t('patient_search.status.active')}</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
