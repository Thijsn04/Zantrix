import React, { useState, useEffect } from 'react';
import { VitalSignObservation, fetchVitals, recordVitals } from '../../api/vitals.api';
import { Activity, Plus, Save } from 'lucide-react';
import { useParams } from 'react-router-dom';
import { useAuth } from 'react-oidc-context';

export const VitalsDashboard: React.FC = () => {
  const { patientId } = useParams<{ patientId: string }>();
  const [vitals, setVitals] = useState<VitalSignObservation[]>([]);
  const [isAdding, setIsAdding] = useState(false);

  // Form state
  const [hr, setHr] = useState('');
  const [bpSys, setBpSys] = useState('');
  const [bpDia, setBpDia] = useState('');
  const [temp, setTemp] = useState('');
  const [spo2, setSpo2] = useState('');
  const auth = useAuth();

  const CURRENT_RECORDER_ID = auth.user?.profile?.sub || '00000000-0000-0000-0000-000000000001';

  useEffect(() => {
    if (patientId) {
      loadVitals();
    }
  }, [patientId]);

  const loadVitals = async () => {
    if (!patientId) return;
    try {
      const data = await fetchVitals(patientId);
      setVitals(data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSave = async () => {
    if (!patientId) return;

    const newVitals: VitalSignObservation[] = [];
    const timestamp = new Date().toISOString();

    if (hr) newVitals.push({ patientId, recorderId: CURRENT_RECORDER_ID, type: 'HEART_RATE', value: Number(hr), unit: 'bpm', measuredAt: timestamp });
    if (bpSys) newVitals.push({ patientId, recorderId: CURRENT_RECORDER_ID, type: 'BLOOD_PRESSURE_SYSTOLIC', value: Number(bpSys), unit: 'mmHg', measuredAt: timestamp });
    if (bpDia) newVitals.push({ patientId, recorderId: CURRENT_RECORDER_ID, type: 'BLOOD_PRESSURE_DIASTOLIC', value: Number(bpDia), unit: 'mmHg', measuredAt: timestamp });
    if (temp) newVitals.push({ patientId, recorderId: CURRENT_RECORDER_ID, type: 'TEMPERATURE', value: Number(temp), unit: '°C', measuredAt: timestamp });
    if (spo2) newVitals.push({ patientId, recorderId: CURRENT_RECORDER_ID, type: 'O2_SATURATION', value: Number(spo2), unit: '%', measuredAt: timestamp });

    if (newVitals.length === 0) return;

    try {
      await recordVitals(newVitals);
      setIsAdding(false);
      setHr(''); setBpSys(''); setBpDia(''); setTemp(''); setSpo2('');
      loadVitals();
    } catch (err) {
      console.error(err);
    }
  };

  // Group by timestamp for the flowsheet view
  const groupedVitals = vitals.reduce((acc, v) => {
    const time = new Date(v.measuredAt).toLocaleString();
    if (!acc[time]) acc[time] = {};
    acc[time][v.type] = v;
    return acc;
  }, {} as Record<string, Record<string, VitalSignObservation>>);

  const times = Object.keys(groupedVitals).sort((a, b) => new Date(b).getTime() - new Date(a).getTime());

  return (
    <div className="flex flex-col h-full bg-slate-50 p-6">
      <div className="bg-white rounded-lg shadow border overflow-hidden flex flex-col flex-1">
        <div className="p-4 border-b bg-indigo-50 border-indigo-100 flex justify-between items-center">
          <h2 className="font-bold text-indigo-800 flex items-center gap-2">
            <Activity size={20} /> Flowsheet Vitaalwaarden
          </h2>
          <button 
            onClick={() => setIsAdding(!isAdding)}
            className="flex items-center gap-1 bg-indigo-600 text-white px-3 py-1.5 rounded-md text-sm hover:bg-indigo-700"
          >
            {isAdding ? 'Annuleren' : <><Plus size={16} /> Meting Invoeren</>}
          </button>
        </div>

        {isAdding && (
          <div className="p-4 bg-white border-b shadow-inner">
            <div className="grid grid-cols-5 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-500 mb-1">Hartslag (bpm)</label>
                <input type="number" value={hr} onChange={e => setHr(e.target.value)} className="w-full border rounded p-2 text-sm" placeholder="bijv. 80" />
              </div>
              <div className="col-span-2 flex gap-2">
                <div className="flex-1">
                  <label className="block text-xs font-bold text-slate-500 mb-1">Bovendruk (mmHg)</label>
                  <input type="number" value={bpSys} onChange={e => setBpSys(e.target.value)} className="w-full border rounded p-2 text-sm" placeholder="120" />
                </div>
                <div className="flex-1">
                  <label className="block text-xs font-bold text-slate-500 mb-1">Onderdruk (mmHg)</label>
                  <input type="number" value={bpDia} onChange={e => setBpDia(e.target.value)} className="w-full border rounded p-2 text-sm" placeholder="80" />
                </div>
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-500 mb-1">Temp (°C)</label>
                <input type="number" step="0.1" value={temp} onChange={e => setTemp(e.target.value)} className="w-full border rounded p-2 text-sm" placeholder="37.2" />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-500 mb-1">SpO2 (%)</label>
                <input type="number" value={spo2} onChange={e => setSpo2(e.target.value)} className="w-full border rounded p-2 text-sm" placeholder="98" />
              </div>
            </div>
            <div className="mt-4 flex justify-end">
              <button 
                onClick={handleSave}
                className="flex items-center gap-2 bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 font-medium"
              >
                <Save size={18} /> Opslaan
              </button>
            </div>
          </div>
        )}

        <div className="overflow-x-auto flex-1">
          {times.length === 0 ? (
             <div className="p-8 text-center text-slate-400">Geen vitaalwaarden geregistreerd.</div>
          ) : (
            <table className="min-w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-100 text-slate-600 text-xs uppercase tracking-wider">
                  <th className="p-3 font-bold border-b border-r sticky left-0 bg-slate-100 w-40 z-10">Tijdstip</th>
                  <th className="p-3 font-bold border-b border-r text-center">Pols (bpm)</th>
                  <th className="p-3 font-bold border-b border-r text-center">RR Sys (mmHg)</th>
                  <th className="p-3 font-bold border-b border-r text-center">RR Dia (mmHg)</th>
                  <th className="p-3 font-bold border-b border-r text-center">Temp (°C)</th>
                  <th className="p-3 font-bold border-b text-center">SpO2 (%)</th>
                </tr>
              </thead>
              <tbody>
                {times.map(time => {
                  const data = groupedVitals[time];
                  return (
                    <tr key={time} className="border-b hover:bg-slate-50">
                      <td className="p-3 border-r font-medium text-slate-800 text-sm sticky left-0 bg-white shadow-[2px_0_5px_-2px_rgba(0,0,0,0.1)]">{time}</td>
                      <td className="p-3 border-r text-center font-mono">{data.HEART_RATE?.value || '-'}</td>
                      <td className="p-3 border-r text-center font-mono">{data.BLOOD_PRESSURE_SYSTOLIC?.value || '-'}</td>
                      <td className="p-3 border-r text-center font-mono">{data.BLOOD_PRESSURE_DIASTOLIC?.value || '-'}</td>
                      <td className="p-3 border-r text-center font-mono">{data.TEMPERATURE?.value || '-'}</td>
                      <td className="p-3 text-center font-mono">{data.O2_SATURATION?.value || '-'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};
