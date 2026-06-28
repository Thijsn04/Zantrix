import React, { useState, useEffect } from 'react';
import { ClinicalTask, fetchPatientTasks, createTask, completeTask } from '../../api/tasks.api';
import { CheckSquare, Square, Clock, AlertTriangle, Plus } from 'lucide-react';
import { useParams } from 'react-router-dom';
import { useAuth } from 'react-oidc-context';

export const TaskManager: React.FC = () => {
  const { patientId } = useParams<{ patientId: string }>();
  const [tasks, setTasks] = useState<ClinicalTask[]>([]);
  const [isAdding, setIsAdding] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<'LOW'|'NORMAL'|'HIGH'|'URGENT'>('NORMAL');
  const auth = useAuth();

  const CURRENT_USER_ID = auth.user?.profile?.sub || '00000000-0000-0000-0000-000000000001';

  useEffect(() => {
    if (patientId) {
      loadTasks();
    }
  }, [patientId]);

  const loadTasks = async () => {
    if (!patientId) return;
    try {
      const data = await fetchPatientTasks(patientId);
      setTasks(data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleAdd = async () => {
    if (!patientId || !title.trim()) return;
    try {
      await createTask({
        patientId,
        creatorId: CURRENT_USER_ID,
        assigneeId: CURRENT_USER_ID,
        status: 'TODO',
        priority,
        title,
        description
      });
      setIsAdding(false);
      setTitle('');
      setDescription('');
      setPriority('NORMAL');
      loadTasks();
    } catch (err) {
      console.error(err);
    }
  };

  const handleComplete = async (taskId: string) => {
    try {
      await completeTask(taskId);
      loadTasks();
    } catch (err) {
      console.error(err);
    }
  };

  const priorityColors = {
    LOW: 'bg-slate-100 text-slate-800',
    NORMAL: 'bg-blue-100 text-blue-800',
    HIGH: 'bg-orange-100 text-orange-800',
    URGENT: 'bg-red-100 text-red-800 font-bold',
  };

  return (
    <div className="flex flex-col h-full bg-slate-50 p-6">
      <div className="bg-white rounded-lg shadow border overflow-hidden flex flex-col flex-1">
        <div className="p-4 border-b bg-cyan-50 border-cyan-100 flex justify-between items-center">
          <h2 className="font-bold text-cyan-800 flex items-center gap-2">
            <CheckSquare size={20} /> Taken & Handovers
          </h2>
          <button 
            onClick={() => setIsAdding(!isAdding)}
            className="flex items-center gap-1 bg-cyan-600 text-white px-3 py-1.5 rounded-md text-sm hover:bg-cyan-700"
          >
            {isAdding ? 'Annuleren' : <><Plus size={16} /> Nieuwe Taak</>}
          </button>
        </div>

        {isAdding && (
          <div className="p-4 bg-white border-b shadow-inner flex flex-col gap-3">
            <input 
              type="text" 
              placeholder="Taak titel..."
              className="w-full border rounded p-2 text-sm focus:ring-2 focus:ring-cyan-500 outline-none"
              value={title}
              onChange={e => setTitle(e.target.value)}
            />
            <textarea 
              placeholder="Optionele beschrijving of instructies..."
              className="w-full border rounded p-2 text-sm focus:ring-2 focus:ring-cyan-500 outline-none h-20"
              value={description}
              onChange={e => setDescription(e.target.value)}
            />
            <div className="flex justify-between items-center">
              <select 
                value={priority}
                onChange={e => setPriority(e.target.value as any)}
                className="border rounded p-2 text-sm outline-none"
              >
                <option value="LOW">Laag</option>
                <option value="NORMAL">Normaal</option>
                <option value="HIGH">Hoog</option>
                <option value="URGENT">Spoed (Urgent)</option>
              </select>
              
              <button 
                onClick={handleAdd}
                disabled={!title.trim()}
                className="bg-cyan-600 text-white px-4 py-2 rounded-md hover:bg-cyan-700 font-medium disabled:opacity-50"
              >
                Toevoegen
              </button>
            </div>
          </div>
        )}

        <div className="p-4 flex-1 overflow-y-auto">
          {tasks.length === 0 ? (
            <div className="text-center text-slate-400 py-8">Geen taken voor deze patiënt.</div>
          ) : (
            <div className="space-y-3">
              {tasks.map(task => (
                <div key={task.id} className={`p-4 border rounded-lg flex gap-4 ${task.status === 'DONE' ? 'bg-slate-50 opacity-60' : 'bg-white shadow-sm'}`}>
                  <button 
                    onClick={() => task.status !== 'DONE' && handleComplete(task.id!)}
                    className="mt-0.5 text-cyan-600 hover:text-cyan-800"
                    disabled={task.status === 'DONE'}
                  >
                    {task.status === 'DONE' ? <CheckSquare size={24} /> : <Square size={24} />}
                  </button>
                  <div className="flex-1">
                    <div className="flex justify-between items-start">
                      <h4 className={`font-semibold ${task.status === 'DONE' ? 'line-through text-slate-500' : 'text-slate-800'}`}>
                        {task.title}
                      </h4>
                      <span className={`text-xs px-2 py-0.5 rounded-full ${priorityColors[task.priority]}`}>
                        {task.priority}
                      </span>
                    </div>
                    {task.description && (
                      <p className="text-sm text-slate-600 mt-1">{task.description}</p>
                    )}
                    <div className="flex items-center gap-4 mt-3 text-xs text-slate-400">
                      <span className="flex items-center gap-1">
                        <Clock size={14} /> Gemaakt: {new Date(task.createdAt!).toLocaleString()}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
