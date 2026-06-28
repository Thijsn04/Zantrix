import { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { Bed, UserPlus, LogOut, ArrowRightLeft } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function GrandCentralDashboard() {
  const { t } = useTranslation();
  const auth = useAuth();
  const [beds, setBeds] = useState<any[]>([]);
  const [wards, setWards] = useState<any[]>([]);
  const [selectedWardId, setSelectedWardId] = useState<string>('');

  useEffect(() => {
    fetchWards();
  }, []);

  useEffect(() => {
    if (selectedWardId) {
      fetchBeds(selectedWardId);
    } else {
      setBeds([]);
    }
  }, [selectedWardId]);

  const fetchWards = async () => {
    const res = await fetch(`http://localhost:8080/api/v1/adt/wards`, {
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) {
      const data = await res.json();
      setWards(data);
      if (data.length > 0) {
        setSelectedWardId(data[0].id);
      }
    }
  };

  const fetchBeds = async (wardId: string) => {
    const res = await fetch(`http://localhost:8080/api/v1/adt/ward/${wardId}/census`, {
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) setBeds(await res.json());
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6 flex items-center gap-2">
        <Bed className="text-zantrix-blue" />
        {t('adt.grand_central_title', 'Grand Central (ADT)')}
      </h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-white p-4 shadow rounded">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold">Ward Census</h2>
            {wards.length > 0 && (
              <select 
                value={selectedWardId} 
                onChange={(e) => setSelectedWardId(e.target.value)}
                className="border border-slate-300 rounded p-1"
              >
                {wards.map(ward => (
                  <option key={ward.id} value={ward.id}>{ward.name}</option>
                ))}
              </select>
            )}
          </div>
          {beds.length === 0 ? <p className="text-gray-500">No beds configured or no data.</p> : (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {beds.map(bed => (
                <div key={bed.id} className={`p-4 border rounded shadow-sm flex flex-col items-center justify-center gap-2 ${
                  bed.status === 'AVAILABLE' ? 'bg-green-50 border-green-200' :
                  bed.status === 'OCCUPIED' ? 'bg-blue-50 border-blue-200' :
                  bed.status === 'CLEANING_NEEDED' ? 'bg-yellow-50 border-yellow-200' :
                  'bg-red-50 border-red-200'
                }`}>
                  <Bed size={24} className={
                    bed.status === 'AVAILABLE' ? 'text-green-600' :
                    bed.status === 'OCCUPIED' ? 'text-blue-600' :
                    bed.status === 'CLEANING_NEEDED' ? 'text-yellow-600' :
                    'text-red-600'
                  } />
                  <span className="font-bold">{bed.name}</span>
                  <span className="text-xs uppercase font-medium">{bed.status.replace('_', ' ')}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="bg-white p-4 shadow rounded flex flex-col gap-4">
          <h2 className="text-xl font-semibold mb-2">Actions</h2>
          <button className="w-full flex items-center justify-center gap-2 bg-blue-100 text-zantrix-blue px-4 py-2 rounded hover:bg-blue-200 font-medium">
            <UserPlus size={18} /> Admit Patient
          </button>
          <button className="w-full flex items-center justify-center gap-2 bg-purple-100 text-purple-700 px-4 py-2 rounded hover:bg-purple-200 font-medium">
            <ArrowRightLeft size={18} /> Transfer Patient
          </button>
          <button className="w-full flex items-center justify-center gap-2 bg-orange-100 text-orange-700 px-4 py-2 rounded hover:bg-orange-200 font-medium">
            <LogOut size={18} /> Discharge Patient
          </button>
        </div>
      </div>
    </div>
  );
}
