import { useState, useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { User, Activity, FileText } from 'lucide-react';
import { PatientMergeModal } from './PatientMergeModal';
import { useTranslation } from 'react-i18next';

/**
 * Displays the detailed medical dossier for a single patient.
 * 
 * Fetches patient data from the backend by ID and presents demographics,
 * insurance details, notes, and vitals. Allows the user to trigger a patient merge workflow.
 *
 * @param {Object} props - The component properties.
 * @param {string} props.patientId - The UUID of the patient to display.
 */
export function PatientDetail({ patientId }: { patientId: string }) {
  const { t } = useTranslation();
  const auth = useAuth();
  const [patient, setPatient] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [showMergeModal, setShowMergeModal] = useState(false);

  useEffect(() => {
    fetch(`http://localhost:8080/api/v1/patients/${patientId}`, {
      headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
    })
    .then(res => res.json())
    .then(data => {
      setPatient(data);
      setLoading(false);
    })
    .catch(() => setLoading(false));
  }, [patientId, auth.user?.access_token]);

  if (loading) return <div className="p-4 text-slate-500 dark:text-slate-400 font-medium">{t('patient_detail.loading')}</div>;
  if (!patient) return <div className="p-4 text-red-500 dark:text-red-400 font-medium">{t('patient_detail.not_found')}</div>;

  return (
    <div className="h-full flex flex-col gap-4">
      <div className="bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded shadow-sm p-4 flex items-start justify-between shrink-0">
        <div className="flex gap-4 items-center">
          <div className="w-16 h-16 bg-slate-100 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded flex items-center justify-center text-slate-400 dark:text-slate-500">
            <User size={32} />
          </div>
          <div>
            <h1 className="text-2xl font-semibold text-slate-900 dark:text-white tracking-tight">
              {patient.isEmergency ? t('patient_detail.emergency_name') : `${patient.lastName}, ${patient.firstName}`}
            </h1>
            <div className="text-sm text-slate-600 dark:text-slate-300 flex gap-4 mt-1">
              <span><strong>{t('patient_detail.bsn')}</strong> {patient.bsn || '-'}</span>
              <span><strong>{t('patient_detail.born')}</strong> {patient.birthDate || '-'}</span>
              <span><strong>{t('patient_detail.gender')}</strong> {patient.gender}</span>
              {patient.status !== 'ACTIVE' && (
                <span className="text-orange-600 dark:text-orange-400 font-medium bg-orange-100 dark:bg-orange-900/30 px-2 py-0.5 rounded">{t('patient_detail.status')} {patient.status}</span>
              )}
            </div>
            <div className="text-sm text-slate-600 dark:text-slate-300 flex gap-4 mt-1">
              <span><strong>{t('patient_detail.insurance')}</strong> {patient.insuranceCompany || '-'}</span>
              <span><strong>{t('patient_detail.policy')}</strong> {patient.insuranceNumber || '-'}</span>
            </div>
          </div>
        </div>
        <div className="flex gap-2">
           <button className="px-3 py-1.5 border border-slate-300 dark:border-slate-600 rounded text-sm font-medium hover:bg-slate-50 dark:hover:bg-slate-700 transition shadow-sm dark:text-slate-200">{t('patient_detail.edit_dossier')}</button>
           {patient.status !== 'MERGED' && (
             <button onClick={() => setShowMergeModal(true)} className="px-3 py-1.5 bg-orange-100 dark:bg-orange-900/30 text-orange-800 dark:text-orange-400 border border-orange-200 dark:border-orange-800 rounded text-sm font-medium hover:bg-orange-200 dark:hover:bg-orange-900/50 transition shadow-sm">{t('patient_detail.merge_patient')}</button>
           )}
        </div>
      </div>

      <div className="flex-1 grid grid-cols-3 gap-4 min-h-0">
        <div className="col-span-2 bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded shadow-sm flex flex-col">
          <div className="bg-slate-100 dark:bg-slate-900/50 border-b border-slate-300 dark:border-slate-700 px-3 py-2 font-medium flex items-center gap-2 dark:text-slate-200">
            <FileText size={16} className="text-zantrix-blue" /> {t('patient_detail.notes_title')}
          </div>
          <div className="flex-1 p-4 overflow-auto text-slate-500 dark:text-slate-400 text-sm flex items-center justify-center italic">
            {t('patient_detail.no_notes')}
          </div>
        </div>
        
        <div className="bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded shadow-sm flex flex-col">
          <div className="bg-slate-100 dark:bg-slate-900/50 border-b border-slate-300 dark:border-slate-700 px-3 py-2 font-medium flex items-center gap-2 dark:text-slate-200">
            <Activity size={16} className="text-red-600 dark:text-red-400" /> {t('patient_detail.vitals_title')}
          </div>
          <div className="flex-1 p-4 overflow-auto text-slate-500 dark:text-slate-400 text-sm flex items-center justify-center italic">
            {t('patient_detail.no_vitals')}
          </div>
        </div>
      </div>

      {showMergeModal && (
        <PatientMergeModal
          sourcePatientId={patient.id}
          sourcePatientName={patient.isEmergency ? t('patient_detail.emergency_name') : `${patient.lastName}, ${patient.firstName}`}
          onClose={() => setShowMergeModal(false)}
          onMerged={() => {
            setShowMergeModal(false);
            setLoading(true); // reload patient to see updated status
            fetch(`http://localhost:8080/api/v1/patients/${patientId}`, {
              headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
            }).then(res => res.json()).then(data => { setPatient(data); setLoading(false); });
          }}
        />
      )}
    </div>
  );
}
