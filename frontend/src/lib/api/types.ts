export interface CurrentUser {
  subject: string;
  username: string | null;
  displayName: string | null;
  roles: string[];
  scopes: string[];
}

export interface ApiErrorBody {
  message?: string;
  detail?: string;
  title?: string;
}

export interface PatientSummary { id: string; displayName: string; birthDate: string; administrativeGender: string; active: boolean; identifier?: string; duplicateScore: number }
export interface ClinicalSummary { id: string; status?: string; clinicalStatus?: string; display?: string; code?: string; title?: string; dosage?: string; serviceDisplay?: string; start?: string; end?: string; observedAt?: string; value?: number; unit?: string; description?: string; priority?: string; authoredAt?: string }
export interface TaskSummary extends ClinicalSummary { patientId: string; owner?: string; dueAt?: string }
export interface AppointmentSummary extends ClinicalSummary { patientId: string; practitionerId: string; locationId?: string }
export interface Page<T> { content: T[]; totalElements: number; totalPages: number; number: number }
