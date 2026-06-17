import React, { useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { useTranslation } from 'react-i18next';
import { useAppointments } from '../../hooks/useAppointments';
import { Calendar, Users } from 'lucide-react';
import { AppointmentModal } from '../../components/scheduling/AppointmentModal';

export function ScheduleView() {
  const { t } = useTranslation();
  const [currentDate, setCurrentDate] = useState(new Date());
  
  // Hardcoded practitioner for demo purposes. In reality, we'd fetch the logged in user or selected doctors.
  const practitionerId = "00000000-0000-0000-0000-000000000000"; 
  
  const start = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1).toISOString();
  const end = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).toISOString();

  const { appointments, isLoading, createAppointment, refresh } = useAppointments(practitionerId, start, end);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState<{ start: Date, end: Date } | null>(null);

  const handleDateSelect = (selectInfo: any) => {
    setSelectedSlot({ start: selectInfo.start, end: selectInfo.end });
    setIsModalOpen(true);
  };

  const handleEventClick = (clickInfo: any) => {
    // Edit functionality could be added here
    alert(`Appointment: ${clickInfo.event.title}`);
  };

  const calendarEvents = appointments.map(appt => ({
    id: appt.id,
    title: appt.status === 'BOOKED' ? 'Booked Appointment' : appt.status,
    start: appt.startTime,
    end: appt.endTime,
    backgroundColor: appt.status === 'BOOKED' ? '#3b82f6' : '#9ca3af',
  }));

  return (
    <div className="h-full flex flex-col p-6 bg-slate-50 dark:bg-slate-900">
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900 dark:text-white flex items-center gap-2">
            <Calendar className="text-zantrix-blue" size={24} />
            {t('scheduling.title', 'Scheduling')}
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            {t('scheduling.subtitle', 'Manage appointments and clinical encounters')}
          </p>
        </div>
        <div className="flex gap-2">
           <button 
            onClick={() => setIsModalOpen(true)}
            className="bg-zantrix-blue text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700 transition"
          >
            {t('scheduling.new_appointment', 'New Appointment')}
          </button>
        </div>
      </div>

      <div className="flex-1 bg-white dark:bg-slate-800 rounded-lg shadow-sm border border-slate-200 dark:border-slate-700 p-4 overflow-auto relative">
        {isLoading && (
          <div className="absolute inset-0 z-10 bg-white/50 dark:bg-slate-800/50 flex items-center justify-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-zantrix-blue"></div>
          </div>
        )}
        <FullCalendar
          plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
          headerToolbar={{
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
          }}
          initialView="timeGridWeek"
          editable={true}
          selectable={true}
          selectMirror={true}
          dayMaxEvents={true}
          weekends={true}
          events={calendarEvents}
          select={handleDateSelect}
          eventClick={handleEventClick}
          height="100%"
          slotMinTime="07:00:00"
          slotMaxTime="20:00:00"
        />
      </div>

      {isModalOpen && (
        <AppointmentModal 
          onClose={() => setIsModalOpen(false)}
          selectedSlot={selectedSlot}
          practitionerId={practitionerId}
          onSave={async (appt) => {
            await createAppointment(appt);
            setIsModalOpen(false);
          }}
        />
      )}
    </div>
  );
}
