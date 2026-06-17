import { useState } from 'react';
import { useAuth } from 'react-oidc-context';
import { AlertCircle, Search, X } from 'lucide-react';

/**
 * Modal component to facilitate merging duplicate patient records.
 * 
 * Allows users to search for a target patient and merge the current (source) 
 * patient into it. This action updates the source patient's status to 'MERGED'
 * and permanently links the records.
 *
 * @param {Object} props - The component properties.
 * @param {string} props.sourcePatientId - The UUID of the patient being merged (made inactive).
 * @param {string} props.sourcePatientName - The display name of the source patient for context.
 * @param {Function} props.onClose - Callback to close the modal without merging.
 * @param {Function} props.onMerged - Callback invoked after a successful merge operation.
 */
export function PatientMergeModal({ 
  sourcePatientId, 
  sourcePatientName, 
  onClose, 
  onMerged 
}: { 
  sourcePatientId: string; 
  sourcePatientName: string; 
  onClose: () => void; 
  onMerged: () => void; 
}) {
  const auth = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [selectedTargetId, setSelectedTargetId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [merging, setMerging] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    
    try {
      const res = await fetch(`http://localhost:8080/api/v1/patients/search?q=${encodeURIComponent(searchQuery)}`, {
        headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
      });
      const data = await res.json();
      setSearchResults(data.filter((p: any) => p.id !== sourcePatientId && p.status !== 'MERGED'));
    } catch (err) {
      console.error(err);
    }
  };

  const handleMerge = async () => {
    if (!selectedTargetId) return;
    
    setError(null);
    setMerging(true);
    
    try {
      const res = await fetch(`http://localhost:8080/api/v1/patients/${sourcePatientId}/merge?targetId=${selectedTargetId}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
      });
      
      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Fout bij het mergen van patiënten');
      }
      
      onMerged();
    } catch (err: any) {
      setError(err.message);
      setMerging(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg overflow-hidden flex flex-col">
        <div className="bg-orange-600 px-4 py-3 flex items-center justify-between text-white">
          <div className="flex items-center gap-2 font-semibold">
            <AlertCircle size={16} /> Dossiers Samenvoegen (Deduplicatie)
          </div>
          <button onClick={onClose} className="hover:bg-orange-700 p-1 rounded">
            <X size={16} />
          </button>
        </div>
        
        <div className="p-4 flex flex-col gap-4">
          <div className="text-sm text-slate-700">
            Je staat op het punt om het huidige dossier <strong>({sourcePatientName})</strong> te mergen in een ander dossier. 
            Dit maakt het huidige dossier inactief. Dit kan niet ongedaan worden gemaakt.
          </div>
          
          {error && (
            <div className="bg-red-50 text-red-700 p-3 rounded border border-red-200 text-sm">
              {error}
            </div>
          )}
          
          <form onSubmit={handleSearch} className="flex gap-2">
            <input 
              type="text" 
              placeholder="Zoek doel-patiënt op BSN of naam..." 
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="flex-1 border border-slate-300 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-zantrix-blue focus:ring-1 focus:ring-zantrix-blue"
            />
            <button type="submit" className="bg-slate-200 text-slate-800 px-3 py-1.5 rounded text-sm font-medium hover:bg-slate-300 transition flex items-center gap-2">
              <Search size={14} /> Zoeken
            </button>
          </form>
          
          {searchResults.length > 0 && (
            <div className="border border-slate-200 rounded max-h-48 overflow-auto">
              {searchResults.map(p => (
                <div 
                  key={p.id}
                  onClick={() => setSelectedTargetId(p.id)}
                  className={`p-2 border-b border-slate-200 last:border-b-0 cursor-pointer text-sm flex justify-between items-center ${selectedTargetId === p.id ? 'bg-orange-50 border-orange-200' : 'hover:bg-slate-50'}`}
                >
                  <div>
                    <div className="font-medium">{p.isEmergency ? 'John Doe (Nood)' : `${p.lastName}, ${p.firstName}`}</div>
                    <div className="text-xs text-slate-500">BSN: {p.bsn || '-'} | Geboren: {p.birthDate || '-'}</div>
                  </div>
                  {selectedTargetId === p.id && (
                    <span className="text-orange-600 font-semibold text-xs">Doel</span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
        
        <div className="px-5 py-3 bg-slate-50 border-t border-slate-200 flex justify-end gap-2">
          <button onClick={onClose} className="px-3 py-1.5 rounded border border-slate-300 text-sm font-medium hover:bg-slate-100 transition">Annuleren</button>
          <button 
            onClick={handleMerge} 
            disabled={!selectedTargetId || merging} 
            className="px-3 py-1.5 rounded bg-orange-600 text-white text-sm font-medium hover:bg-orange-700 transition disabled:opacity-50"
          >
            {merging ? 'Bezig...' : 'Bevestig Merge'}
          </button>
        </div>
      </div>
    </div>
  );
}
