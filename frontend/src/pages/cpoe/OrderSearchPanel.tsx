import React, { useState } from 'react';
import { MOCK_MEDICATIONS, MOCK_SERVICES } from '../../api/cpoe.api';
import { Search } from 'lucide-react';

interface OrderSearchPanelProps {
  onAddMedication: (medication: any) => void;
  onAddService: (service: any) => void;
}

export const OrderSearchPanel: React.FC<OrderSearchPanelProps> = ({ onAddMedication, onAddService }) => {
  const [query, setQuery] = useState('');

  const filteredMeds = MOCK_MEDICATIONS.filter(m => m.name.toLowerCase().includes(query.toLowerCase()));
  const filteredServices = MOCK_SERVICES.filter(s => s.name.toLowerCase().includes(query.toLowerCase()));

  return (
    <div className="bg-white rounded-lg shadow border p-4">
      <div className="relative mb-4">
        <Search className="absolute left-3 top-2.5 text-slate-400" size={18} />
        <input 
          type="text" 
          placeholder="Zoek medicatie, labs of procedures..."
          className="w-full pl-10 pr-4 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 outline-none"
          value={query}
          onChange={e => setQuery(e.target.value)}
        />
      </div>

      {query && (
        <div className="space-y-4 max-h-96 overflow-y-auto">
          {filteredMeds.length > 0 && (
            <div>
              <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Medicatie (EVS)</h4>
              <ul className="space-y-1">
                {filteredMeds.map(med => (
                  <li 
                    key={med.id}
                    onClick={() => { onAddMedication(med); setQuery(''); }}
                    className="p-2 hover:bg-blue-50 rounded cursor-pointer text-sm"
                  >
                    <div className="font-medium text-slate-800">{med.name}</div>
                    <div className="text-xs text-slate-500">{med.defaultDose}</div>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {filteredServices.length > 0 && (
            <div>
              <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Orders & Labs</h4>
              <ul className="space-y-1">
                {filteredServices.map(srv => (
                  <li 
                    key={srv.id}
                    onClick={() => { onAddService(srv); setQuery(''); }}
                    className="p-2 hover:bg-purple-50 rounded cursor-pointer text-sm"
                  >
                    <div className="font-medium text-slate-800">{srv.name}</div>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {filteredMeds.length === 0 && filteredServices.length === 0 && (
            <div className="text-sm text-slate-500 text-center py-4">
              Geen resultaten gevonden in de Terminology Server.
            </div>
          )}
        </div>
      )}
    </div>
  );
};
