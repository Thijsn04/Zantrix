import React, { useState } from 'react';
import { X, Calendar as CalendarIcon, User, Clock } from 'lucide-react';
import { Appointment } from '../../hooks/useAppointments';

interface Props {
  onClose: () => void;
  selectedSlot: { start: Date, end: Date } | null;
  practitionerId: string;
  onSave: (appt: Appointment) => Promise<void>;
}

export function AppointmentModal({ onClose, selectedSlot, practitionerId, onSave }: Props) {
  const [patientId, setPatientId] = useState(''); // Normally a search dropdown
  const [startTime, setStartTime] = useState(selectedSlot ? selectedSlot.start.toISOString().slice(0, 16) : '');
  const [endTime, setEndTime] = useState(selectedSlot ? selectedSlot.end.toISOString().slice(0, 16) : '');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!patientId || !startTime || !endTime) return;
    setIsSubmitting(true);
    
    // Convert local datetime-local string to UTC OffsetDateTime format required by backend
    const startIso = new Date(startTime).toISOString();
    const endIso = new Date(endTime).toISOString();

    await onSave({
      patientId,
      practitionerId,
      startTime: startIso,
      endTime: endIso,
      status: 'BOOKED'
    });
    setIsSubmitting(false);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
      <div className="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-md overflow-hidden border border-slate-200 dark:border-slate-700 animate-in fade-in zoom-in-95 duration-200">
        <div className="flex justify-between items-center p-4 border-b border-slate-200 dark:border-slate-700">
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white flex items-center gap-2">
            <CalendarIcon size={18} className="text-zantrix-blue" />
            Book Appointment
          </h2>
          <button onClick={onClose} className="text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200">
            <X size={20} />
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="p-4 space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 flex items-center gap-1">
              <User size={14} /> Patient ID
            </label>
            <input 
              type="text" 
              required
              value={patientId}
              onChange={(e) => setPatientId(e.target.value)}
              className="w-full bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-600 rounded-md px-3 py-2 text-sm text-slate-900 dark:text-white focus:ring-2 focus:ring-zantrix-blue outline-none"
              placeholder="Enter Patient UUID"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 flex items-center gap-1">
                <Clock size={14} /> Start Time
              </label>
              <input 
                type="datetime-local" 
                required
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-600 rounded-md px-3 py-2 text-sm text-slate-900 dark:text-white focus:ring-2 focus:ring-zantrix-blue outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1 flex items-center gap-1">
                <Clock size={14} /> End Time
              </label>
              <input 
                type="datetime-local" 
                required
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-600 rounded-md px-3 py-2 text-sm text-slate-900 dark:text-white focus:ring-2 focus:ring-zantrix-blue outline-none"
              />
            </div>
          </div>

          <div className="pt-4 flex justify-end gap-2">
            <button 
              type="button" 
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 bg-slate-100 dark:bg-slate-700 rounded hover:bg-slate-200 dark:hover:bg-slate-600"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              disabled={isSubmitting}
              className="px-4 py-2 text-sm font-medium text-white bg-zantrix-blue rounded hover:bg-blue-700 disabled:opacity-50"
            >
              {isSubmitting ? 'Booking...' : 'Book Appointment'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
