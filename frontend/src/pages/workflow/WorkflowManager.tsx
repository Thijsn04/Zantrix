import React, { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { Network, CheckCircle, Clock } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function WorkflowManager() {
  const { t } = useTranslation();
  const auth = useAuth();
  const [tasks, setTasks] = useState<any[]>([]);

  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    const res = await fetch('http://localhost:8080/api/v1/workflow/tasks', {
      headers: { Authorization: `Bearer ${auth.user?.access_token}` }
    });
    if (res.ok) setTasks(await res.json());
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6 flex items-center gap-2">
        <Network className="text-zantrix-blue" />
        {t('workflow.manager_title', 'Workflow & Tasks Manager')}
      </h1>

      <div className="bg-white p-4 shadow rounded max-w-4xl mx-auto">
        <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
          <Clock /> Assigned Tasks
        </h2>
        {tasks.length === 0 ? <p className="text-gray-500">No active tasks found.</p> : (
          <ul className="space-y-3">
            {tasks.map(task => (
              <li key={task.id} className="p-4 border rounded hover:bg-slate-50 transition flex justify-between items-center">
                <div>
                  <h3 className="font-bold text-slate-800">{task.title}</h3>
                  <div className="text-sm text-slate-500">Status: {task.status} | Patient ID: {task.patientId}</div>
                </div>
                <button className="flex items-center gap-1 text-sm bg-blue-100 text-blue-700 px-3 py-1.5 rounded font-medium hover:bg-blue-200">
                  <CheckCircle size={16} /> Mark Complete
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
