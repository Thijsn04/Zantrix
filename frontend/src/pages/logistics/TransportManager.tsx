import React, { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { Truck, CheckCircle, Navigation } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function TransportManager() {
  const { t } = useTranslation();
  const auth = useAuth();
  const [jobs, setJobs] = useState<any[]>([]);

  useEffect(() => {
    fetchJobs();
  }, []);

  const fetchJobs = async () => {
    const res = await fetch('http://localhost:8080/api/v1/logistics/transport/jobs', {
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) setJobs(await res.json());
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6 flex items-center gap-2">
        <Truck className="text-zantrix-blue" />
        {t('logistics.transport_title', 'Patient Transport & Logistics')}
      </h1>

      <div className="bg-white p-4 shadow rounded max-w-4xl mx-auto">
        <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
          <Navigation /> Pending Transport Requests
        </h2>
        
        {jobs.length === 0 ? <p className="text-gray-500">No active transport requests.</p> : (
          <ul className="space-y-3">
            {jobs.map(job => (
              <li key={job.id} className="p-4 border rounded hover:bg-slate-50 transition flex justify-between items-center">
                <div>
                  <h3 className="font-bold text-slate-800">Patient: {job.patientId}</h3>
                  <div className="text-sm text-slate-500">
                    From: {job.fromLocationId} <br/> 
                    To: {job.toLocationId}
                  </div>
                  <div className="text-xs text-gray-400 mt-1">Requested: {new Date(job.requestedTime).toLocaleString()}</div>
                </div>
                <div className="flex gap-2">
                  <button className="flex items-center gap-1 text-sm bg-purple-100 text-purple-700 px-3 py-1.5 rounded font-medium hover:bg-purple-200">
                    Accept Job
                  </button>
                  <button className="flex items-center gap-1 text-sm bg-green-100 text-green-700 px-3 py-1.5 rounded font-medium hover:bg-green-200">
                    <CheckCircle size={16} /> Complete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
