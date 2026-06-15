import { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { UserPlus, AlertCircle } from 'lucide-react';


export function PatientSearch({ onOpenPatient, onCreatePatient }: { onOpenPatient: (id: string, name: string) => void, onCreatePatient: () => void }) {
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
      setPatients(data);
      setLoading(false);
    })
    .catch(() => setLoading(false));
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
        <h1 className="text-xl font-semibold text-slate-800">Patiënten zoeken (PMI)</h1>
        <button onClick={onCreatePatient} className="bg-zantrix-blue text-white px-3 py-1.5 rounded text-sm font-medium flex items-center gap-2 hover:bg-blue-700 transition">
          <UserPlus size={16} /> Nieuwe Patiënt
        </button>
      </div>
      
      <div className="bg-white border border-slate-300 rounded shadow-sm flex-1 flex flex-col overflow-hidden">
        <div className="bg-slate-50 border-b border-slate-300 p-3 flex gap-2">
          <form onSubmit={handleSearch} className="flex-1 flex gap-2">
            <input 
              type="text" 
              placeholder="Zoek op BSN of Achternaam..." 
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="flex-1 border border-slate-300 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-zantrix-blue focus:ring-1 focus:ring-zantrix-blue"
            />
            <button type="submit" className="bg-slate-200 text-slate-800 px-4 py-1.5 rounded text-sm font-medium hover:bg-slate-300 transition">
              Zoeken
            </button>
          </form>
        </div>
        <div className="overflow-auto flex-1">
          <table className="w-full text-left text-sm border-collapse">
            <thead className="bg-slate-100 text-slate-600 sticky top-0 border-b border-slate-300 z-10 shadow-sm">
              <tr>
                <th className="p-2 font-medium border-r border-slate-200">BSN</th>
                <th className="p-2 font-medium border-r border-slate-200">Naam</th>
                <th className="p-2 font-medium border-r border-slate-200">Geboortedatum</th>
                <th className="p-2 font-medium border-r border-slate-200">Geslacht</th>
                <th className="p-2 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={5} className="p-4 text-center text-slate-500">Laden...</td></tr>
              ) : patients.length === 0 ? (
                <tr><td colSpan={5} className="p-4 text-center text-slate-500">Geen patiënten gevonden.</td></tr>
              ) : patients.map(p => (
                <tr 
                  key={p.id} 
                  onClick={() => onOpenPatient(p.id, p.isEmergency ? 'John Doe (Nood)' : `${p.lastName}, ${p.firstName}`)}
                  className="border-b border-slate-200 hover:bg-slate-50 cursor-pointer transition"
                >
                  <td className="p-2 border-r border-slate-200 font-mono">{p.bsn || '-'}</td>
                  <td className="p-2 border-r border-slate-200">
                    {p.isEmergency ? (
                      <span className="text-red-600 flex items-center gap-1 font-semibold"><AlertCircle size={14} /> John Doe (Nood)</span>
                    ) : (
                      `${p.lastName || '-'}, ${p.firstName || '-'}`
                    )}
                  </td>
                  <td className="p-2 border-r border-slate-200 tabular-nums">{p.birthDate || '-'}</td>
                  <td className="p-2 border-r border-slate-200">{p.gender}</td>
                  <td className="p-2">
                    {p.status === 'MERGED' ? (
                      <span className="bg-orange-100 text-orange-800 px-2 py-0.5 rounded text-xs">Gemerged</span>
                    ) : p.status === 'DECEASED' ? (
                      <span className="bg-slate-200 text-slate-800 px-2 py-0.5 rounded text-xs">Overleden</span>
                    ) : (
                      <span className="bg-green-100 text-green-800 px-2 py-0.5 rounded text-xs">Actief</span>
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
