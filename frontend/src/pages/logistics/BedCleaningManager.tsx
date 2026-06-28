import React, { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { Sparkles, CheckCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function BedCleaningManager() {
  const { t } = useTranslation();
  const auth = useAuth();
  const [beds, setBeds] = useState<any[]>([]);

  useEffect(() => {
    fetchBeds();
  }, []);

  const fetchBeds = async () => {
    const res = await fetch('http://localhost:8080/api/v1/logistics/cleaning/beds', {
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) setBeds(await res.json());
  };

  const handleClean = async (bedId: string) => {
    const res = await fetch(`http://localhost:8080/api/v1/logistics/cleaning/beds/${bedId}/clean`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) {
      fetchBeds();
    }
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6 flex items-center gap-2">
        <Sparkles className="text-zantrix-blue" />
        {t('logistics.cleaning_title', 'Bed Cleaning & Turnaround')}
      </h1>

      <div className="bg-white p-4 shadow rounded max-w-4xl mx-auto">
        <h2 className="text-xl font-semibold mb-4 text-slate-800">Beds Requiring Cleaning</h2>
        
        {beds.length === 0 ? <p className="text-gray-500">No beds currently need cleaning.</p> : (
          <ul className="space-y-3">
            {beds.map(bed => (
              <li key={bed.id} className="p-4 border border-yellow-200 bg-yellow-50 rounded hover:bg-yellow-100 transition flex justify-between items-center">
                <div>
                  <h3 className="font-bold text-yellow-800">{bed.name}</h3>
                  <div className="text-sm text-yellow-700">Location ID: {bed.id}</div>
                </div>
                <button 
                  onClick={() => handleClean(bed.id)}
                  className="flex items-center gap-1 text-sm bg-green-600 text-white px-4 py-2 rounded font-medium hover:bg-green-700 shadow-sm"
                >
                  <CheckCircle size={16} /> Mark as Clean & Available
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
