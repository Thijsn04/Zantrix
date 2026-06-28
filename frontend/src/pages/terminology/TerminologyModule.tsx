import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from 'react-oidc-context';
import { Database, Search, UploadCloud, CheckCircle, AlertCircle, ShieldAlert } from 'lucide-react';

/**
 * Administrative module for managing medical terminologies and ontologies.
 * 
 * Provides an interface to query the FHIR ValueSet $expand endpoint for 
 * standard codes (SNOMED, LOINC). Also exposes an import interface to trigger
 * asynchronous backend terminology loading jobs (e.g., importing RF2 files).
 * 
 * @param {Object} props - The component properties.
 * @param {string[]} props.roles - The user's active roles (used to restrict access to SYSTEM_ADMIN).
 * @returns {JSX.Element} The terminology administration UI.
 */
export function TerminologyModule({ roles }: { roles: string[] }) {
  const { t } = useTranslation();
  const auth = useAuth();
  const [activeTab, setActiveTab] = useState<'search' | 'import'>('search');
  
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [importStatus, setImportStatus] = useState<{type: 'success' | 'error', msg: string} | null>(null);

  const isSystemAdmin = roles.includes('SYSTEM_ADMIN');

  useEffect(() => {
    if (!isSystemAdmin) return;
    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }
    const delayDebounceFn = setTimeout(() => {
      setIsSearching(true);
      // Using genuine HAPI FHIR endpoint for ValueSet expansion
      fetch(`http://localhost:8080/fhir/ValueSet/$expand?filter=${encodeURIComponent(searchQuery)}`, {
        headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
      })
      .then(res => res.json())
      .then(data => {
        if (data.expansion && data.expansion.contains) {
          setSearchResults(data.expansion.contains);
        } else {
          setSearchResults([]);
        }
        setIsSearching(false);
      })
      .catch(() => {
        setSearchResults([]);
        setIsSearching(false);
      });
    }, 500);

    return () => clearTimeout(delayDebounceFn);
  }, [searchQuery, auth.user?.access_token, isSystemAdmin]);

  const handleImport = async (type: string, file: File) => {
    try {
      setImportStatus(null);
      const formData = new FormData();
      formData.append('file', file);
      
      const res = await fetch(`http://localhost:8080/api/v1/terminology/admin/${type}`, {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${auth.user?.access_token}`
        },
        body: formData
      });
      if (res.ok) {
        setImportStatus({ type: 'success', msg: t('terminology.import_success') });
      } else {
        setImportStatus({ type: 'error', msg: 'Import failed' });
      }
    } catch(e) {
      setImportStatus({ type: 'error', msg: 'System Error' });
    }
  };

  const triggerFileInput = (type: string) => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = type === 'snomed' || type === 'loinc' ? '.zip,.csv' : '.json';
    input.onchange = (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (file) {
        handleImport(type, file);
      }
    };
    input.click();
  };

  if (!isSystemAdmin) {
    return (
      <div className="h-full flex flex-col items-center justify-center bg-slate-50 dark:bg-slate-900 text-slate-500 dark:text-slate-400">
        <ShieldAlert size={48} className="mb-4 text-red-500" />
        <h2 className="text-xl font-semibold mb-2">Toegang Geweigerd</h2>
        <p className="text-sm">Je hebt geen rechten om de Terminologie Browser te bekijken. (SYSTEM_ADMIN vereist)</p>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded shadow-sm">
      <div className="flex border-b border-slate-200 dark:border-slate-800">
        <button
          onClick={() => setActiveTab('search')}
          className={`px-4 py-3 text-sm font-medium transition-colors ${
            activeTab === 'search' 
              ? 'border-b-2 border-zantrix-blue text-zantrix-blue dark:text-blue-400' 
              : 'text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200'
          }`}
        >
          {t('terminology.title')}
        </button>
        <button
          onClick={() => setActiveTab('import')}
          className={`px-4 py-3 text-sm font-medium transition-colors ${
            activeTab === 'import' 
              ? 'border-b-2 border-zantrix-blue text-zantrix-blue dark:text-blue-400' 
              : 'text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200'
          }`}
        >
          {t('terminology.import_tab')}
        </button>
      </div>

      <div className="flex-1 overflow-auto p-6">
        {activeTab === 'search' ? (
          <div className="max-w-4xl mx-auto">
            <div className="relative mb-6">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
              <input 
                type="text" 
                placeholder={t('terminology.search_placeholder')}
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 rounded focus:outline-none focus:border-zantrix-blue dark:text-white"
              />
            </div>
            
            {isSearching ? (
              <div className="text-center text-slate-500 py-8">{t('terminology.loading')}</div>
            ) : searchResults.length === 0 && searchQuery.trim() ? (
              <div className="text-center text-slate-500 py-8">{t('terminology.no_results')}</div>
            ) : (
              <div className="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded overflow-hidden">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 dark:bg-slate-900 border-b border-slate-200 dark:border-slate-700">
                    <tr>
                      <th className="px-4 py-3 font-semibold text-slate-700 dark:text-slate-300">Code</th>
                      <th className="px-4 py-3 font-semibold text-slate-700 dark:text-slate-300">Display</th>
                      <th className="px-4 py-3 font-semibold text-slate-700 dark:text-slate-300">System</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                    {searchResults.map((concept, idx) => (
                      <tr key={idx} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                        <td className="px-4 py-3 font-mono text-xs dark:text-slate-300">{concept.code}</td>
                        <td className="px-4 py-3 font-medium text-slate-900 dark:text-white">{concept.display}</td>
                        <td className="px-4 py-3 text-slate-500 dark:text-slate-400 text-[10px] break-all">{concept.system || '-'}</td>
                      </tr>
                    ))}
                    {searchResults.length === 0 && !searchQuery.trim() && (
                      <tr>
                        <td colSpan={3} className="px-4 py-8 text-center text-slate-500">
                          <Database size={32} className="mx-auto mb-2 opacity-50" />
                          Test the FHIR ValueSet $expand connection
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        ) : (
          <div className="max-w-2xl mx-auto">
            <h2 className="text-xl font-semibold mb-6 dark:text-white flex items-center gap-2">
              <Database className="text-zantrix-blue" /> {t('terminology.import_title')}
            </h2>
            
            {importStatus && (
              <div className={`mb-6 p-4 rounded flex items-start gap-3 ${importStatus.type === 'success' ? 'bg-green-50 text-green-800 border border-green-200 dark:bg-green-900/30 dark:border-green-800 dark:text-green-400' : 'bg-red-50 text-red-800 border border-red-200 dark:bg-red-900/30 dark:border-red-800 dark:text-red-400'}`}>
                {importStatus.type === 'success' ? <CheckCircle size={20} /> : <AlertCircle size={20} />}
                <p className="text-sm font-medium">{importStatus.msg}</p>
              </div>
            )}

            <div className="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded p-6">
              <p className="text-sm text-slate-600 dark:text-slate-400 mb-6">
                Activeer asynchrone import-pipelines om grote ontologieën via HAPI FHIR ITermLoaderSvc direct in te laden.
              </p>
              
              <div className="space-y-4">
                <button 
                  onClick={() => triggerFileInput('snomed')}
                  className="w-full flex items-center justify-between p-4 border border-slate-200 dark:border-slate-700 rounded hover:bg-slate-50 dark:hover:bg-slate-700/50 transition group"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900/50 text-zantrix-blue dark:text-blue-400 rounded flex items-center justify-center">
                      <UploadCloud size={20} />
                    </div>
                    <div className="text-left">
                      <div className="font-semibold text-slate-900 dark:text-white">SNOMED CT</div>
                      <div className="text-xs text-slate-500">Upload RF2 Release</div>
                    </div>
                  </div>
                  <span className="text-sm font-medium text-zantrix-blue dark:text-blue-400 opacity-0 group-hover:opacity-100 transition-opacity">
                    Importeer &rarr;
                  </span>
                </button>
                
                <button 
                  onClick={() => triggerFileInput('loinc')}
                  className="w-full flex items-center justify-between p-4 border border-slate-200 dark:border-slate-700 rounded hover:bg-slate-50 dark:hover:bg-slate-700/50 transition group"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900/50 text-zantrix-blue dark:text-blue-400 rounded flex items-center justify-center">
                      <UploadCloud size={20} />
                    </div>
                    <div className="text-left">
                      <div className="font-semibold text-slate-900 dark:text-white">LOINC</div>
                      <div className="text-xs text-slate-500">Upload LOINC ZIP</div>
                    </div>
                  </div>
                  <span className="text-sm font-medium text-zantrix-blue dark:text-blue-400 opacity-0 group-hover:opacity-100 transition-opacity">
                    Importeer &rarr;
                  </span>
                </button>

                <button 
                  onClick={() => triggerFileInput('custom-codesystem')}
                  className="w-full flex items-center justify-between p-4 border border-slate-200 dark:border-slate-700 rounded hover:bg-slate-50 dark:hover:bg-slate-700/50 transition group"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900/50 text-zantrix-blue dark:text-blue-400 rounded flex items-center justify-center">
                      <UploadCloud size={20} />
                    </div>
                    <div className="text-left">
                      <div className="font-semibold text-slate-900 dark:text-white">ICD-10 / DHD</div>
                      <div className="text-xs text-slate-500">Upload Custom CodeSystem JSON</div>
                    </div>
                  </div>
                  <span className="text-sm font-medium text-zantrix-blue dark:text-blue-400 opacity-0 group-hover:opacity-100 transition-opacity">
                    Importeer &rarr;
                  </span>
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
