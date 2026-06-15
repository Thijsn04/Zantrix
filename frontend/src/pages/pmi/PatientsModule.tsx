import { useState } from 'react';
import { PatientSearch } from './PatientSearch';
import { PatientForm } from './PatientForm';
import { PatientDetail } from './PatientDetail';
import { X, Search } from 'lucide-react';

type Tab = {
  id: string;
  title: string;
  type: 'SEARCH' | 'DETAIL' | 'FORM';
  patientId?: string;
};

export function PatientsModule() {
  const [tabs, setTabs] = useState<Tab[]>([{ id: 'search', title: 'Patiënten Zoeken', type: 'SEARCH' }]);
  const [activeTabId, setActiveTabId] = useState('search');

  const openPatient = (id: string, name: string) => {
    const existing = tabs.find(t => t.type === 'DETAIL' && t.patientId === id);
    if (existing) {
      setActiveTabId(existing.id);
    } else {
      const newTab: Tab = { id: `patient-${id}`, title: name, type: 'DETAIL', patientId: id };
      setTabs([...tabs, newTab]);
      setActiveTabId(newTab.id);
    }
  };

  const openForm = () => {
    const newTab: Tab = { id: `form-${Date.now()}`, title: 'Nieuwe Patiënt', type: 'FORM' };
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
    <div className="h-full flex flex-col bg-white border border-slate-300 rounded shadow-sm overflow-hidden">
      {/* Internal Module Tab Bar */}
      <div className="flex bg-slate-50 border-b border-slate-300 shrink-0 overflow-x-auto items-end pt-1 px-1 gap-1">
        {tabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTabId(tab.id)}
            className={`flex items-center gap-2 px-3 py-1.5 text-xs font-medium rounded-t border-t border-l border-r transition whitespace-nowrap group ${
              activeTabId === tab.id 
                ? 'bg-white border-slate-300 text-zantrix-blue shadow-[0_1px_0_#fff]' // Overlap bottom border slightly
                : 'bg-transparent border-transparent text-slate-500 hover:bg-slate-200/50'
            }`}
            style={activeTabId === tab.id ? { marginBottom: '-1px' } : {}}
          >
            {tab.type === 'SEARCH' && <Search size={14} />}
            {tab.title}
            {tab.type !== 'SEARCH' && (
              <div 
                onClick={(e) => closeTab(tab.id, e)}
                className={`rounded p-0.5 transition ${activeTabId === tab.id ? 'hover:bg-slate-100 text-slate-400 hover:text-red-500' : 'text-slate-400 opacity-0 group-hover:opacity-100 hover:bg-slate-300'}`}
              >
                <X size={12} />
              </div>
            )}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="flex-1 min-h-0 p-4 bg-slate-50/30">
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
