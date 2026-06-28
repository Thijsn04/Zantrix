export interface VitalSignObservation {
  id?: string;
  patientId: string;
  recorderId: string;
  type: 'HEART_RATE' | 'BLOOD_PRESSURE_SYSTOLIC' | 'BLOOD_PRESSURE_DIASTOLIC' | 'TEMPERATURE' | 'RESPIRATORY_RATE' | 'O2_SATURATION' | 'WEIGHT' | 'HEIGHT';
  value: number;
  unit: string;
  measuredAt: string;
  createdAt?: string;
}

const API_BASE = '/api/v1/vitals';

export async function fetchVitals(patientId: string): Promise<VitalSignObservation[]> {
  const response = await fetch(`${API_BASE}/patient/${patientId}`);
  if (!response.ok) throw new Error('Failed to fetch vitals');
  return response.json();
}

export async function recordVitals(vitals: VitalSignObservation[]): Promise<VitalSignObservation[]> {
  const response = await fetch(`${API_BASE}/batch`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(vitals),
  });
  if (!response.ok) throw new Error('Failed to record vitals');
  return response.json();
}
