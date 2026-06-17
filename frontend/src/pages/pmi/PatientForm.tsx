import { useState } from 'react';
import { useAuth } from 'react-oidc-context';
import { AlertCircle, Save, X } from 'lucide-react';

/**
 * Form component for registering a new patient.
 * 
 * Collects demographics and insurance information. Supports a quick toggle for 
 * emergency patient registration ("John Doe" scenario), which disables standard 
 * required fields. Submits the new patient to the backend FHIR-compliant endpoint.
 *
 * @param {Object} props - The component properties.
 * @param {Function} props.onSaved - Callback invoked when the patient is successfully saved.
 * @param {Function} props.onCancel - Callback invoked when the form is cancelled.
 */
export function PatientForm({ onSaved, onCancel }: { onSaved: (id: string, name: string) => void, onCancel: () => void }) {
  const auth = useAuth();
  const [isEmergency, setIsEmergency] = useState(false);
  const [formData, setFormData] = useState({
    bsn: '',
    firstName: '',
    lastName: '',
    birthDate: '',
    gender: 'UNKNOWN',
    insuranceCompany: '',
    insuranceNumber: ''
  });
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSaving(true);

    try {
      const payload = {
        ...formData,
        birthDate: formData.birthDate || null,
        isEmergency,
        fhirDataJson: null
      };

      const res = await fetch('http://localhost:8080/api/v1/patients', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${auth.user?.access_token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Fout bij opslaan patiënt');
      }

      const savedPatient = await res.json();
      const displayName = savedPatient.isEmergency ? 'John Doe (Nood)' : `${savedPatient.lastName}, ${savedPatient.firstName}`;
      onSaved(savedPatient.id, displayName);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto mt-4 bg-white border border-slate-300 rounded shadow-sm">
      <div className="bg-slate-100 border-b border-slate-300 px-4 py-3 flex justify-between items-center">
        <h2 className="text-lg font-semibold text-slate-800">Nieuwe Patiënt Registreren</h2>
        <button onClick={onCancel} className="text-slate-500 hover:text-slate-700">
          <X size={20} />
        </button>
      </div>
      
      <form onSubmit={handleSubmit} className="p-4 flex flex-col gap-4">
        {error && (
          <div className="bg-red-50 text-red-700 p-3 rounded border border-red-200 text-sm flex items-start gap-2">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <div className="flex items-center gap-2 bg-orange-50 p-3 border border-orange-200 rounded">
          <input 
            type="checkbox" 
            id="isEmergency" 
            checked={isEmergency}
            onChange={(e) => setIsEmergency(e.target.checked)}
            className="w-4 h-4 text-zantrix-blue rounded border-slate-300 focus:ring-zantrix-blue"
          />
          <label htmlFor="isEmergency" className="text-sm font-medium text-orange-900 cursor-pointer flex-1">
            Noodpatiënt (John Doe) registreren
          </label>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="flex flex-col gap-1">
            <label className="text-xs font-semibold text-slate-700">BSN {!isEmergency && '*'}</label>
            <input 
              type="text" 
              value={formData.bsn}
              onChange={e => setFormData({...formData, bsn: e.target.value})}
              required={!isEmergency}
              disabled={isEmergency}
              className="border border-slate-300 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-zantrix-blue focus:ring-1 focus:ring-zantrix-blue disabled:bg-slate-100 disabled:text-slate-500 font-mono"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs font-semibold text-slate-700">Geboortedatum</label>
            <input 
              type="date" 
              value={formData.birthDate}
              onChange={e => setFormData({...formData, birthDate: e.target.value})}
              className="border border-slate-300 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-zantrix-blue focus:ring-1 focus:ring-zantrix-blue disabled:bg-slate-100"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs font-semibold text-slate-700">Voornaam</label>
            <input 
              type="text" 
              value={formData.firstName}
              onChange={e => setFormData({...formData, firstName: e.target.value})}
              disabled={isEmergency}
              className="border border-slate-300 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-zantrix-blue focus:ring-1 focus:ring-zantrix-blue disabled:bg-slate-100 disabled:text-slate-500"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs font-semibold text-slate-700">Achternaam</label>
            <input 
              type="text" 
              value={formData.lastName}
              onChange={e => setFormData({...formData, lastName: e.target.value})}
              disabled={isEmergency}
              className="border border-slate-300 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-zantrix-blue focus:ring-1 focus:ring-zantrix-blue disabled:bg-slate-100 disabled:text-slate-500"
            />
          </div>
          <div className="flex flex-col gap-1 col-span-2">
            <label className="text-xs font-semibold text-slate-700">Geslacht</label>
            <select 
              value={formData.gender}
              onChange={e => setFormData({...formData, gender: e.target.value})}
              className="border border-slate-300 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-zantrix-blue focus:ring-1 focus:ring-zantrix-blue bg-white"
            >
              <option value="UNKNOWN">Onbekend</option>
              <option value="MALE">Man</option>
              <option value="FEMALE">Vrouw</option>
              <option value="OTHER">Anders</option>
            </select>
          </div>
          
          <div className="flex flex-col gap-1">
            <label className="text-xs font-semibold text-slate-700">Verzekeraar</label>
            <input 
              type="text" 
              value={formData.insuranceCompany}
              onChange={e => setFormData({...formData, insuranceCompany: e.target.value})}
              disabled={isEmergency}
              className="border border-slate-300 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-zantrix-blue focus:ring-1 focus:ring-zantrix-blue disabled:bg-slate-100 disabled:text-slate-500"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs font-semibold text-slate-700">Polisnummer</label>
            <input 
              type="text" 
              value={formData.insuranceNumber}
              onChange={e => setFormData({...formData, insuranceNumber: e.target.value})}
              disabled={isEmergency}
              className="border border-slate-300 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-zantrix-blue focus:ring-1 focus:ring-zantrix-blue disabled:bg-slate-100 disabled:text-slate-500"
            />
          </div>
        </div>

        <div className="flex justify-end gap-2 mt-4 pt-4 border-t border-slate-200">
          <button type="button" onClick={onCancel} className="px-4 py-2 border border-slate-300 rounded text-sm font-medium text-slate-700 hover:bg-slate-50 transition">
            Annuleren
          </button>
          <button type="submit" disabled={saving} className="px-4 py-2 bg-zantrix-blue text-white rounded text-sm font-medium hover:bg-blue-700 transition flex items-center gap-2 disabled:opacity-50">
            <Save size={16} /> {saving ? 'Opslaan...' : 'Patiënt Opslaan'}
          </button>
        </div>
      </form>
    </div>
  );
}
