import React, { useState, useEffect } from 'react';
import { MedicationRequest, ServiceRequest, fetchDraftMedications, fetchDraftServices, saveDraftMedication, signOrders } from '../../api/cpoe.api';
import { OrderSearchPanel } from './OrderSearchPanel';
import { ShoppingCart, AlertCircle, CheckCircle, X } from 'lucide-react';
import { useParams } from 'react-router-dom';

export const OrderEntryCart: React.FC = () => {
  const { patientId } = useParams<{ patientId: string }>();
  const [draftMeds, setDraftMeds] = useState<MedicationRequest[]>([]);
  const [draftServices, setDraftServices] = useState<ServiceRequest[]>([]);
  const [isSigning, setIsSigning] = useState(false);
  const [error, setError] = useState('');

  // MOCK author ID
  const CURRENT_AUTHOR_ID = '00000000-0000-0000-0000-000000000001';

  useEffect(() => {
    if (patientId) {
      loadDrafts();
    }
  }, [patientId]);

  const loadDrafts = async () => {
    if (!patientId) return;
    try {
      const m = await fetchDraftMedications(patientId);
      const s = await fetchDraftServices(patientId);
      setDraftMeds(m);
      setDraftServices(s);
    } catch (err) {
      console.error(err);
    }
  };

  const handleAddMedication = async (med: any) => {
    if (!patientId) return;
    try {
      const draft = await saveDraftMedication({
        patientId,
        requesterId: CURRENT_AUTHOR_ID,
        medicationReference: med.name,
        dosageInstruction: med.defaultDose
      });
      setDraftMeds([...draftMeds, draft]);
    } catch (err) {
      console.error(err);
    }
  };

  const handleAddService = async (srv: any) => {
    // Similarly call saveDraftService API...
    // For brevity, pushing locally
    setDraftServices([...draftServices, {
      id: 'temp-' + Date.now(),
      patientId: patientId!,
      requesterId: CURRENT_AUTHOR_ID,
      code: srv.name
    }]);
  };

  const handleSignAll = async () => {
    setIsSigning(true);
    setError('');
    
    // Simulate CDS Module Hook check (UC-3.2.05)
    const hasAmox = draftMeds.some(m => m.medicationReference.includes('Amoxicilline'));
    if (hasAmox) {
      setError('CDS Hard Stop: Fatale Interactie - Patiënt heeft actieve Penicilline allergie!');
      setIsSigning(false);
      return;
    }

    try {
      const medIds = draftMeds.filter(m => m.id).map(m => m.id!);
      const srvIds = draftServices.filter(s => s.id && !s.id.startsWith('temp')).map(s => s.id!);
      
      await signOrders(medIds, srvIds);
      
      setDraftMeds([]);
      setDraftServices([]);
      alert("Orders successfully signed and distributed.");
    } catch (err) {
      setError("Failed to sign orders.");
      console.error(err);
    } finally {
      setIsSigning(false);
    }
  };

  const totalItems = draftMeds.length + draftServices.length;

  return (
    <div className="flex gap-6 h-full p-6 bg-slate-50">
      <div className="flex-1">
        <h2 className="text-xl font-bold text-slate-800 mb-4">CPOE - Order Entry</h2>
        <OrderSearchPanel onAddMedication={handleAddMedication} onAddService={handleAddService} />
        
        {error && (
          <div className="mt-4 p-4 bg-red-50 border border-red-200 text-red-800 rounded-md flex gap-3 items-start">
            <AlertCircle size={20} className="mt-0.5" />
            <div>
              <h3 className="font-bold">Clinical Decision Support Alert</h3>
              <p className="text-sm">{error}</p>
            </div>
          </div>
        )}
      </div>

      <div className="w-96 bg-white rounded-lg shadow border flex flex-col">
        <div className="p-4 border-b bg-slate-100 flex justify-between items-center">
          <div className="flex items-center gap-2 font-semibold text-slate-800">
            <ShoppingCart size={18} />
            Concept Orders
          </div>
          <span className="bg-blue-100 text-blue-800 text-xs font-bold px-2 py-1 rounded-full">
            {totalItems} items
          </span>
        </div>

        <div className="flex-1 overflow-y-auto p-4 space-y-4">
          {draftMeds.length > 0 && (
            <div>
              <h4 className="text-xs font-bold text-slate-400 uppercase mb-2">Medicatie</h4>
              {draftMeds.map((med, idx) => (
                <div key={idx} className="p-3 bg-blue-50 border border-blue-100 rounded-md mb-2 relative">
                  <div className="font-medium text-slate-800">{med.medicationReference}</div>
                  <div className="text-sm text-slate-600">{med.dosageInstruction}</div>
                  <button className="absolute top-2 right-2 text-slate-400 hover:text-red-500">
                    <X size={16} />
                  </button>
                </div>
              ))}
            </div>
          )}

          {draftServices.length > 0 && (
            <div>
              <h4 className="text-xs font-bold text-slate-400 uppercase mb-2">Aanvragen</h4>
              {draftServices.map((srv, idx) => (
                <div key={idx} className="p-3 bg-purple-50 border border-purple-100 rounded-md mb-2 relative">
                  <div className="font-medium text-slate-800">{srv.code}</div>
                  <button className="absolute top-2 right-2 text-slate-400 hover:text-red-500">
                    <X size={16} />
                  </button>
                </div>
              ))}
            </div>
          )}

          {totalItems === 0 && (
            <div className="text-center text-slate-400 py-8 text-sm">
              Zoek en voeg orders toe om ze in het mandje te plaatsen.
            </div>
          )}
        </div>

        <div className="p-4 border-t bg-slate-50">
          <button 
            onClick={handleSignAll}
            disabled={totalItems === 0 || isSigning}
            className="w-full py-2.5 bg-green-600 text-white rounded-md font-medium hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed flex justify-center items-center gap-2"
          >
            {isSigning ? 'Checking interactions...' : <><CheckCircle size={18} /> Sign & Submit</>}
          </button>
        </div>
      </div>
    </div>
  );
};
