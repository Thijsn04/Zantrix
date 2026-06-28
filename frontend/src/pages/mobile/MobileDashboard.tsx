import React, { useState, useEffect } from 'react';
import { MobileDashboardData, fetchMobileDashboard } from '../../api/mobile.api';
import { Smartphone, CheckSquare, Bell, User } from 'lucide-react';
import { useAuth } from 'react-oidc-context';

export const MobileDashboard: React.FC = () => {
  const [data, setData] = useState<MobileDashboardData | null>(null);
  const auth = useAuth();

  const CURRENT_USER_ID = auth.user?.profile?.sub || '00000000-0000-0000-0000-000000000001';

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const d = await fetchMobileDashboard(CURRENT_USER_ID);
      setData(d);
    } catch (err) {
      console.error(err);
    }
  };

  if (!data) return <div className="p-4 text-center">Laden...</div>;

  return (
    <div className="flex flex-col h-[800px] w-[400px] bg-slate-100 mx-auto border-4 border-slate-800 rounded-[2rem] overflow-hidden shadow-2xl relative">
      {/* "Notch" */}
      <div className="absolute top-0 inset-x-0 h-6 bg-slate-800 rounded-b-3xl w-1/2 mx-auto z-10"></div>
      
      {/* Header */}
      <div className="bg-teal-600 text-white pt-10 pb-4 px-6 rounded-b-2xl shadow-md">
        <div className="flex justify-between items-center mb-4">
          <div className="font-bold text-lg flex items-center gap-2">
            <Smartphone size={20} /> Zantrix Mobile
          </div>
          <button className="relative">
            <Bell size={20} />
            <span className="absolute -top-1 -right-1 bg-red-500 w-3 h-3 rounded-full border-2 border-teal-600"></span>
          </button>
        </div>
        <div>
          <p className="text-teal-100 text-sm">Welkom terug,</p>
          <h2 className="text-xl font-bold">Verpleegkundige A.</h2>
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        <div className="bg-white rounded-xl shadow-sm p-4 flex items-center justify-between border border-slate-100">
          <div className="flex items-center gap-3">
            <div className="bg-orange-100 text-orange-600 p-2 rounded-lg">
              <CheckSquare size={24} />
            </div>
            <div>
              <h3 className="font-bold text-slate-800">Mijn Taken</h3>
              <p className="text-xs text-slate-500">Voor vandaag</p>
            </div>
          </div>
          <div className="text-2xl font-black text-slate-800">
            {data.activeTasksCount}
          </div>
        </div>

        <h3 className="font-bold text-slate-800 text-sm mt-6 mb-2 px-1">Aankomende Taken</h3>
        <div className="space-y-3">
          {data.tasks.filter(t => t.status !== 'DONE').slice(0, 5).map(task => (
            <div key={task.id} className="bg-white p-4 rounded-xl shadow-sm border border-slate-100 flex gap-3">
              <div className="mt-0.5">
                <div className={`w-3 h-3 rounded-full ${
                  task.priority === 'URGENT' ? 'bg-red-500' : 
                  task.priority === 'HIGH' ? 'bg-orange-500' : 'bg-blue-400'
                }`}></div>
              </div>
              <div>
                <h4 className="font-bold text-slate-800 text-sm">{task.title}</h4>
                <p className="text-xs text-slate-500 mt-1 line-clamp-2">{task.description}</p>
                <div className="mt-2 text-xs font-medium text-teal-600">Bekijk patiënt</div>
              </div>
            </div>
          ))}
          {data.activeTasksCount === 0 && (
            <div className="text-center p-6 text-slate-400 text-sm">U heeft geen openstaande taken!</div>
          )}
        </div>
      </div>

      {/* Bottom Tab Bar */}
      <div className="bg-white border-t p-3 flex justify-around items-center">
        <button className="flex flex-col items-center text-teal-600">
          <CheckSquare size={24} />
          <span className="text-[10px] mt-1 font-bold">Taken</span>
        </button>
        <button className="flex flex-col items-center text-slate-400 hover:text-slate-600">
          <User size={24} />
          <span className="text-[10px] mt-1 font-medium">Patiënten</span>
        </button>
      </div>
    </div>
  );
};
