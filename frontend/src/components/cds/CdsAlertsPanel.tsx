import React, { useState, useEffect } from 'react';
import { CdsAlert, fetchActiveAlerts, dismissAlert } from '../../api/cds.api';
import { AlertCircle, AlertTriangle, Info, X } from 'lucide-react';

interface CdsAlertsPanelProps {
  patientId: string;
  userId: string;
}

export const CdsAlertsPanel: React.FC<CdsAlertsPanelProps> = ({ patientId, userId }) => {
  const [alerts, setAlerts] = useState<CdsAlert[]>([]);

  useEffect(() => {
    if (patientId) {
      loadAlerts();
    }
    // Optional: poll every 30s
    const interval = setInterval(loadAlerts, 30000);
    return () => clearInterval(interval);
  }, [patientId]);

  const loadAlerts = async () => {
    try {
      const data = await fetchActiveAlerts(patientId);
      setAlerts(data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleDismiss = async (alertId: string) => {
    try {
      await dismissAlert(alertId, userId);
      loadAlerts();
    } catch (err) {
      console.error(err);
    }
  };

  if (alerts.length === 0) return null;

  return (
    <div className="space-y-3">
      <h3 className="font-bold text-slate-800 text-sm mb-2 uppercase tracking-wide">Actieve Waarschuwingen</h3>
      {alerts.map(alert => {
        const isCritical = alert.severity === 'CRITICAL';
        const isWarning = alert.severity === 'WARNING';
        
        return (
          <div 
            key={alert.id} 
            className={`p-3 rounded-md border flex items-start gap-3 relative ${
              isCritical ? 'bg-red-50 border-red-200 text-red-900' :
              isWarning ? 'bg-yellow-50 border-yellow-200 text-yellow-900' :
              'bg-blue-50 border-blue-200 text-blue-900'
            }`}
          >
            {isCritical ? <AlertCircle size={20} className="mt-0.5 text-red-600" /> :
             isWarning ? <AlertTriangle size={20} className="mt-0.5 text-yellow-600" /> :
             <Info size={20} className="mt-0.5 text-blue-600" />}
             
            <div className="flex-1 pr-6">
              <strong className="block text-sm">{alert.title}</strong>
              <span className="text-xs">{alert.message}</span>
            </div>
            
            <button 
              onClick={() => handleDismiss(alert.id!)}
              className="absolute top-2 right-2 p-1 hover:bg-black/5 rounded-md"
              title="Waarschuwing negeren"
            >
              <X size={16} className="opacity-60" />
            </button>
          </div>
        );
      })}
    </div>
  );
};
