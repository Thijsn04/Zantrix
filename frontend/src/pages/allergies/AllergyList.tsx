import React, { useState, useEffect } from 'react';
import { AllergyIntolerance, fetchAllergies, recordAllergy, searchAllergens } from '../../api/allergies.api';
import { Search, Plus, AlertTriangle, ShieldAlert } from 'lucide-react';
import { useParams } from 'react-router-dom';
import { useAuth } from 'react-oidc-context';

export const AllergyList: React.FC = () => {
  const { patientId } = useParams<{ patientId: string }>();
  const [allergies, setAllergies] = useState<AllergyIntolerance[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<{code: string, display: string}[]>([]);
  const [isAdding, setIsAdding] = useState(false);
  const auth = useAuth();
  
  const CURRENT_RECORDER_ID = auth.user?.profile?.sub || '00000000-0000-0000-0000-000000000001';

  useEffect(() => {
    if (patientId) {
      loadAllergies();
    }
  }, [patientId]);

  const loadAllergies = async () => {
    if (!patientId) return;
    try {
      const data = await fetchAllergies(patientId);
      setAllergies(data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    if (searchQuery.length >= 2) {
      searchAllergens(searchQuery).then(setSearchResults);
    } else {
      setSearchResults([]);
    }
  }, [searchQuery]);

  const handleAddAllergy = async (allergen: {code: string, display: string}, type: 'ALLERGY'|'INTOLERANCE', criticality: 'LOW'|'HIGH'|'UNABLE_TO_ASSESS') => {
    if (!patientId) return;
    try {
      await recordAllergy({
        patientId,
        recorderId: CURRENT_RECORDER_ID,
        type,
        criticality,
        code: allergen.code,
        display: allergen.display,
        reactions: JSON.stringify([{ manifestation: 'Onbekend' }])
      });
      setIsAdding(false);
      setSearchQuery('');
      loadAllergies();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="flex flex-col h-full bg-slate-50 p-6">
      <div className="bg-white rounded-lg shadow border overflow-hidden flex flex-col flex-1">
        <div className="p-4 border-b bg-red-50 border-red-100 flex justify-between items-center">
          <h2 className="font-bold text-red-800 flex items-center gap-2">
            <ShieldAlert size={20} /> Allergieën & Intoleranties
          </h2>
          <button 
            onClick={() => setIsAdding(!isAdding)}
            className="flex items-center gap-1 bg-red-600 text-white px-3 py-1.5 rounded-md text-sm hover:bg-red-700"
          >
            {isAdding ? 'Annuleren' : <><Plus size={16} /> Toevoegen</>}
          </button>
        </div>

        {isAdding && (
          <div className="p-4 bg-white border-b">
            <div className="relative mb-2">
              <Search className="absolute left-3 top-2.5 text-slate-400" size={18} />
              <input 
                type="text" 
                placeholder="Zoek stof, medicatie of allergeen..."
                className="w-full pl-10 pr-4 py-2 border rounded-md focus:ring-2 focus:ring-red-500 outline-none"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                autoFocus
              />
            </div>
            
            {searchQuery && (
              <div className="border rounded-md max-h-60 overflow-y-auto shadow-sm">
                {searchResults.map(allergen => (
                  <div key={allergen.code} className="p-3 border-b hover:bg-slate-50 flex justify-between items-center">
                    <div>
                      <div className="font-medium text-slate-800">{allergen.display}</div>
                    </div>
                    <div className="flex gap-2">
                      <button 
                        onClick={() => handleAddAllergy(allergen, 'INTOLERANCE', 'LOW')}
                        className="text-xs bg-slate-100 px-2 py-1 rounded hover:bg-slate-200"
                      >
                        + Intolerantie
                      </button>
                      <button 
                        onClick={() => handleAddAllergy(allergen, 'ALLERGY', 'HIGH')}
                        className="text-xs bg-red-100 text-red-800 px-2 py-1 rounded hover:bg-red-200 font-bold"
                      >
                        + Allergie (High Risk)
                      </button>
                    </div>
                  </div>
                ))}
                {searchResults.length === 0 && (
                  <div className="p-4 text-center text-sm text-slate-500">Geen allergenen gevonden.</div>
                )}
              </div>
            )}
          </div>
        )}

        <div className="p-0 overflow-y-auto flex-1">
          {allergies.length === 0 ? (
            <div className="p-8 text-center text-slate-400">Patiënt heeft geen geregistreerde allergieën.</div>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 text-slate-500 text-xs uppercase">
                  <th className="p-4 font-semibold border-b">Stof / Allergeen</th>
                  <th className="p-4 font-semibold border-b w-32">Type</th>
                  <th className="p-4 font-semibold border-b w-40">Ernst (Criticality)</th>
                  <th className="p-4 font-semibold border-b w-48">Geregistreerd Op</th>
                </tr>
              </thead>
              <tbody>
                {allergies.map(a => (
                  <tr key={a.id} className="border-b hover:bg-slate-50">
                    <td className="p-4">
                      <div className="font-bold text-slate-800 flex items-center gap-2">
                        {a.criticality === 'HIGH' && <AlertTriangle size={16} className="text-red-500" />}
                        {a.display}
                      </div>
                    </td>
                    <td className="p-4 text-sm text-slate-600">
                      {a.type}
                    </td>
                    <td className="p-4 text-sm">
                      <span className={`px-2 py-1 rounded-full text-xs font-bold ${
                        a.criticality === 'HIGH' ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'
                      }`}>
                        {a.criticality}
                      </span>
                    </td>
                    <td className="p-4 text-sm text-slate-500">
                      {a.createdAt ? new Date(a.createdAt).toLocaleDateString() : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};
