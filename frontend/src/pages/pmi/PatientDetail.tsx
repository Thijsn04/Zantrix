import { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { User, Activity, FileText } from 'lucide-react';
import { PatientMergeModal } from './PatientMergeModal';

export function PatientDetail({ patientId }: { patientId: string }) {
  const auth = useAuth();
  const [patient, setPatient] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [showMergeModal, setShowMergeModal] = useState(false);

  useEffect(() => {
    fetch(`http://localhost:8080/api/v1/patients/${patientId}`, {
      headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
    })
    .then(res => res.json())
    .then(data => {
      setPatient(data);
      setLoading(false);
    })
    .catch(() => setLoading(false));
  }, [patientId, auth.user?.access_token]);

  if (loading) return <div className="p-4 text-slate-500 font-medium">Dossier laden...</div>;
  if (!patient) return <div className="p-4 text-red-500 font-medium">Patiënt niet gevonden of onvoldoende rechten.</div>;

  return (
    <div className="h-full flex flex-col gap-4">
      <div className="bg-white border border-slate-300 rounded shadow-sm p-4 flex items-start justify-between shrink-0">
        <div className="flex gap-4 items-center">
          <div className="w-16 h-16 bg-slate-100 border border-slate-300 rounded flex items-center justify-center text-slate-400">
            <User size={32} />
          </div>
          <div>
            <h1 className="text-2xl font-semibold text-slate-900 tracking-tight">
              {patient.isEmergency ? 'John Doe (Noodpatiënt)' : `${patient.lastName}, ${patient.firstName}`}
            </h1>
            <div className="text-sm text-slate-600 flex gap-4 mt-1">
              <span><strong>BSN:</strong> {patient.bsn || '-'}</span>
              <span><strong>Geboren:</strong> {patient.birthDate || '-'}</span>
              <span><strong>Geslacht:</strong> {patient.gender}</span>
              {patient.status !== 'ACTIVE' && (
                <span className="text-orange-600 font-medium bg-orange-100 px-2 py-0.5 rounded">Status: {patient.status}</span>
              )}
            </div>
            <div className="text-sm text-slate-600 flex gap-4 mt-1">
              <span><strong>Verzekeraar:</strong> {patient.insuranceCompany || '-'}</span>
              <span><strong>Polisnummer:</strong> {patient.insuranceNumber || '-'}</span>
            </div>
          </div>
        </div>
        <div className="flex gap-2">
           <button className="px-3 py-1.5 border border-slate-300 rounded text-sm font-medium hover:bg-slate-50 transition shadow-sm">Dossier Bewerken</button>
           {patient.status !== 'MERGED' && (
             <button onClick={() => setShowMergeModal(true)} className="px-3 py-1.5 bg-orange-100 text-orange-800 border border-orange-200 rounded text-sm font-medium hover:bg-orange-200 transition shadow-sm">Patiënt Mergen</button>
           )}
        </div>
      </div>

      <div className="flex-1 grid grid-cols-3 gap-4 min-h-0">
        <div className="col-span-2 bg-white border border-slate-300 rounded shadow-sm flex flex-col">
          <div className="bg-slate-100 border-b border-slate-300 px-3 py-2 font-medium flex items-center gap-2">
            <FileText size={16} className="text-zantrix-blue" /> Medische Notities
          </div>
          <div className="flex-1 p-4 overflow-auto text-slate-500 text-sm flex items-center justify-center italic">
            Geen notities beschikbaar. Module nog in ontwikkeling.
          </div>
        </div>
        
        <div className="bg-white border border-slate-300 rounded shadow-sm flex flex-col">
          <div className="bg-slate-100 border-b border-slate-300 px-3 py-2 font-medium flex items-center gap-2">
            <Activity size={16} className="text-red-600" /> Vitale Waarden
          </div>
          <div className="flex-1 p-4 overflow-auto text-slate-500 text-sm flex items-center justify-center italic">
            Geen metingen gevonden.
          </div>
        </div>
      </div>

      {showMergeModal && (
        <PatientMergeModal
          sourcePatientId={patient.id}
          sourcePatientName={patient.isEmergency ? 'John Doe (Noodpatiënt)' : `${patient.lastName}, ${patient.firstName}`}
          onClose={() => setShowMergeModal(false)}
          onMerged={() => {
            setShowMergeModal(false);
            setLoading(true); // reload patient to see updated status
            fetch(`http://localhost:8080/api/v1/patients/${patientId}`, {
              headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
            }).then(res => res.json()).then(data => { setPatient(data); setLoading(false); });
          }}
        />
      )}
    </div>
  );
}
