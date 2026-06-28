import React, { useState, useEffect } from 'react';
import { Condition, fetchConditions, recordCondition, resolveCondition, MOCK_DIAGNOSES } from '../../api/problems.api';
import { Search, Plus, Check, Stethoscope } from 'lucide-react';
import { useParams } from 'react-router-dom';

export const ProblemList: React.FC = () => {
  const { patientId } = useParams<{ patientId: string }>();
  const [conditions, setConditions] = useState<Condition[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [isAdding, setIsAdding] = useState(false);

  // MOCK author ID
  const CURRENT_RECORDER_ID = '00000000-0000-0000-0000-000000000001';

  useEffect(() => {
    if (patientId) {
      loadConditions();
    }
  }, [patientId]);

  const loadConditions = async () => {
    if (!patientId) return;
    try {
      const data = await fetchConditions(patientId);
      setConditions(data);
    } catch (err) {
      console.error(err);
    }
  };

  const filteredDiagnoses = MOCK_DIAGNOSES.filter(d => 
    d.display.toLowerCase().includes(searchQuery.toLowerCase()) || 
    d.code.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleAddCondition = async (diag: typeof MOCK_DIAGNOSES[0]) => {
    if (!patientId) return;
    try {
      await recordCondition({
        patientId,
        recorderId: CURRENT_RECORDER_ID,
        code: diag.code,
        codingSystem: diag.system,
        display: diag.display,
        onsetDate: new Date().toISOString().split('T')[0] // today
      });
      setIsAdding(false);
      setSearchQuery('');
      loadConditions();
    } catch (err) {
      console.error(err);
    }
  };

  const handleResolve = async (id: string) => {
    try {
      await resolveCondition(id);
      loadConditions();
    } catch (err) {
      console.error(err);
    }
  };

  const activeConditions = conditions.filter(c => c.clinicalStatus === 'ACTIVE' || c.clinicalStatus === 'RECURRENCE' || c.clinicalStatus === 'RELAPSE');
  const inactiveConditions = conditions.filter(c => c.clinicalStatus === 'RESOLVED' || c.clinicalStatus === 'INACTIVE' || c.clinicalStatus === 'REMISSION');

  return (
    <div className="flex gap-6 h-full p-6 bg-slate-50">
      <div className="w-2/3 flex flex-col gap-6">
        <div className="bg-white rounded-lg shadow border overflow-hidden flex flex-col">
          <div className="p-4 border-b bg-slate-100 flex justify-between items-center">
            <h2 className="font-bold text-slate-800 flex items-center gap-2">
              <Stethoscope size={18} /> Actieve Problemen
            </h2>
            <button 
              onClick={() => setIsAdding(!isAdding)}
              className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1.5 rounded-md text-sm hover:bg-blue-700"
            >
              {isAdding ? 'Annuleren' : <><Plus size={16} /> Toevoegen</>}
            </button>
          </div>

          {isAdding && (
            <div className="p-4 bg-blue-50 border-b">
              <div className="relative mb-2">
                <Search className="absolute left-3 top-2.5 text-slate-400" size={18} />
                <input 
                  type="text" 
                  placeholder="Zoek diagnose (SNOMED / ICD-10)..."
                  className="w-full pl-10 pr-4 py-2 border rounded-md focus:ring-2 focus:ring-blue-500 outline-none"
                  value={searchQuery}
                  onChange={e => setSearchQuery(e.target.value)}
                  autoFocus
                />
              </div>
              
              {searchQuery && (
                <div className="bg-white border rounded-md max-h-60 overflow-y-auto shadow-sm">
                  {filteredDiagnoses.map(diag => (
                    <div 
                      key={diag.code}
                      onClick={() => handleAddCondition(diag)}
                      className="p-3 border-b hover:bg-slate-50 cursor-pointer flex justify-between items-center"
                    >
                      <div>
                        <div className="font-medium text-slate-800">{diag.display}</div>
                        <div className="text-xs text-slate-500">{diag.system}: {diag.code}</div>
                      </div>
                      <Plus size={16} className="text-blue-600" />
                    </div>
                  ))}
                  {filteredDiagnoses.length === 0 && (
                    <div className="p-4 text-center text-sm text-slate-500">Geen resultaten gevonden.</div>
                  )}
                </div>
              )}
            </div>
          )}

          <div className="p-0 overflow-y-auto flex-1 min-h-[300px]">
            {activeConditions.length === 0 ? (
              <div className="p-8 text-center text-slate-400">Geen actieve problemen.</div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 text-slate-500 text-xs uppercase">
                    <th className="p-3 font-semibold border-b">Aandoening</th>
                    <th className="p-3 font-semibold border-b w-32">Systeem</th>
                    <th className="p-3 font-semibold border-b w-32">Sinds</th>
                    <th className="p-3 font-semibold border-b w-24 text-center">Acties</th>
                  </tr>
                </thead>
                <tbody>
                  {activeConditions.map(c => (
                    <tr key={c.id} className="border-b hover:bg-slate-50">
                      <td className="p-3">
                        <div className="font-medium text-slate-800">{c.display}</div>
                        <div className="text-xs text-slate-500">{c.code}</div>
                      </td>
                      <td className="p-3 text-sm text-slate-600">
                        <span className="bg-slate-100 px-2 py-1 rounded text-xs">{c.codingSystem}</span>
                      </td>
                      <td className="p-3 text-sm text-slate-600">{c.onsetDate || '-'}</td>
                      <td className="p-3 text-center">
                        <button 
                          onClick={() => handleResolve(c.id!)}
                          title="Markeer als opgelost"
                          className="text-slate-400 hover:text-green-600 p-1"
                        >
                          <Check size={18} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>

      <div className="w-1/3">
        <div className="bg-white rounded-lg shadow border overflow-hidden">
          <div className="p-4 border-b bg-slate-50">
            <h2 className="font-bold text-slate-800 text-sm">Historie (Opgelost / Inactief)</h2>
          </div>
          <div className="p-0 overflow-y-auto max-h-[600px]">
            {inactiveConditions.length === 0 ? (
              <div className="p-8 text-center text-slate-400 text-sm">Geen historie.</div>
            ) : (
              <ul className="divide-y">
                {inactiveConditions.map(c => (
                  <li key={c.id} className="p-4 hover:bg-slate-50">
                    <div className="font-medium text-slate-700 line-through decoration-slate-300">{c.display}</div>
                    <div className="text-xs text-slate-500 mt-1 flex justify-between">
                      <span>{c.code}</span>
                      <span>Opgelost: {c.abatementDate || '-'}</span>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
