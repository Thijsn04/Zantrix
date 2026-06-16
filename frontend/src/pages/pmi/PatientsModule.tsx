import { useState, useEffect } from 'react';
import { PatientSearch } from './PatientSearch';
import { PatientForm } from './PatientForm';
import { PatientDetail } from './PatientDetail';
import { X, Search } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useSearchParams } from 'react-router-dom';

/**
 * Represents a tab within the PatientsModule workspace.
 */
type Tab = {
  id: string;
  title: string;
  type: 'SEARCH' | 'DETAIL' | 'FORM';
  patientId?: string;
};

/**
 * PatientsModule Component
 * 
 * The main container for the Patient Master Index (PMI) functionality in the frontend.
 * It manages an internal tab system, allowing users to keep multiple patient records,
 * forms, and searches open simultaneously without losing context.
 * 
 * @returns {JSX.Element} The rendered module with tabbed navigation.
 */
export function PatientsModule() {
  const { t } = useTranslation();
  const [tabs, setTabs] = useState<Tab[]>([{ id: 'search', title: t('patient_search.title'), type: 'SEARCH' }]);
  const [activeTabId, setActiveTabId] = useState('search');
  const [searchParams, setSearchParams] = useSearchParams();

  const openPatient = (id: string, name: string) => {
    setTabs(prevTabs => {
      const existing = prevTabs.find(t => t.type === 'DETAIL' && t.patientId === id);
      if (existing) {
        setActiveTabId(existing.id);
        return prevTabs;
      } else {
        const newTab: Tab = { id: `patient-${id}`, title: name, type: 'DETAIL', patientId: id };
        setActiveTabId(newTab.id);
        return [...prevTabs, newTab];
      }
    });
  };

  useEffect(() => {
    const openId = searchParams.get('open');
    const openName = searchParams.get('name');
    
    if (openId && openName) {
      openPatient(openId, openName);
      searchParams.delete('open');
      searchParams.delete('name');
      setSearchParams(searchParams, { replace: true });
    }
  }, [searchParams, setSearchParams]);

  const openForm = () => {
    const newTab: Tab = { id: `form-${Date.now()}`, title: t('patient_search.new_patient'), type: 'FORM' };
    setTabs([...tabs, newTab]);
    setActiveTabId(newTab.id);
  };

  const closeTab = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    const newTabs = tabs.filter(t => t.id !== id);
    setTabs(newTabs);
    if (activeTabId === id) {
      setActiveTabId(newTabs[newTabs.length - 1]?.id || '');
    }
  };

  const activeTab = tabs.find(t => t.id === activeTabId);

  return (
    <div className="h-full flex flex-col bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded shadow-sm overflow-hidden">
      {/* Internal Module Tab Bar */}
      <div className="flex bg-slate-50 dark:bg-slate-800 border-b border-slate-300 dark:border-slate-700 shrink-0 overflow-x-auto items-end pt-1 px-1 gap-1">
        {tabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTabId(tab.id)}
            className={`flex items-center gap-2 px-3 py-1.5 text-xs font-medium rounded-t border-t border-l border-r transition whitespace-nowrap group ${
              activeTabId === tab.id 
                ? 'bg-white dark:bg-slate-900 border-slate-300 dark:border-slate-700 text-zantrix-blue shadow-[0_1px_0_#fff] dark:shadow-[0_1px_0_#0f172a]' // Overlap bottom border slightly
                : 'bg-transparent border-transparent text-slate-500 dark:text-slate-400 hover:bg-slate-200/50 dark:hover:bg-slate-700/50'
            }`}
            style={activeTabId === tab.id ? { marginBottom: '-1px' } : {}}
          >
            {tab.type === 'SEARCH' && <Search size={14} />}
            {tab.title}
            {tab.type !== 'SEARCH' && (
              <div 
                onClick={(e) => closeTab(tab.id, e)}
                className={`rounded p-0.5 transition ${activeTabId === tab.id ? 'hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400 dark:text-slate-500 hover:text-red-500 dark:hover:text-red-400' : 'text-slate-400 dark:text-slate-500 opacity-0 group-hover:opacity-100 hover:bg-slate-300 dark:hover:bg-slate-600'}`}
              >
                <X size={12} />
              </div>
            )}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="flex-1 min-h-0 p-4 bg-slate-50/30 dark:bg-slate-900/50">
        {activeTab?.type === 'SEARCH' && (
          <PatientSearch onOpenPatient={openPatient} onCreatePatient={openForm} />
        )}
        {activeTab?.type === 'FORM' && (
          <PatientForm 
            onCancel={() => {
              const newTabs = tabs.filter(t => t.id !== activeTab.id);
              setTabs(newTabs);
              setActiveTabId(newTabs[newTabs.length - 1]?.id || '');
            }} 
            onSaved={(id, name) => {
              const newTabs = tabs.filter(t => t.id !== activeTab.id);
              const newTab: Tab = { id: `patient-${id}`, title: name, type: 'DETAIL', patientId: id };
              setTabs([...newTabs, newTab]);
              setActiveTabId(newTab.id);
            }} 
          />
        )}
        {activeTab?.type === 'DETAIL' && activeTab.patientId && (
          <PatientDetail patientId={activeTab.patientId} />
        )}
      </div>
    </div>
  );
}
