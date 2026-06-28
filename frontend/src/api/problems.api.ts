export interface Condition {
  id?: string;
  patientId: string;
  recorderId: string;
  clinicalStatus?: 'ACTIVE' | 'RECURRENCE' | 'RELAPSE' | 'INACTIVE' | 'REMISSION' | 'RESOLVED';
  code: string;
  codingSystem: string;
  display: string;
  onsetDate?: string;
  abatementDate?: string;
  note?: string;
  createdAt?: string;
}

const API_BASE = '/api/v1/problems';

export async function fetchConditions(patientId: string): Promise<Condition[]> {
  const response = await fetch(`${API_BASE}/patient/${patientId}`);
  if (!response.ok) throw new Error('Failed to fetch conditions');
  return response.json();
}

export async function recordCondition(condition: Condition): Promise<Condition> {
  const response = await fetch(`${API_BASE}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(condition),
  });
  if (!response.ok) throw new Error('Failed to record condition');
  return response.json();
}

export async function resolveCondition(id: string, abatementDate?: string): Promise<Condition> {
  let url = `${API_BASE}/${id}/resolve`;
  if (abatementDate) url += `?abatementDate=${abatementDate}`;
  
  const response = await fetch(url, { method: 'PUT' });
  if (!response.ok) throw new Error('Failed to resolve condition');
  return response.json();
}

// Temporary Mock for Terminology Server (Elasticsearch representation)
export const MOCK_DIAGNOSES = [
  { code: 'E11.9', system: 'ICD-10', display: 'Type 2 diabetes mellitus without complications' },
  { code: 'I10', system: 'ICD-10', display: 'Essential (primary) hypertension' },
  { code: 'J45.909', system: 'ICD-10', display: 'Unspecified asthma, uncomplicated' },
  { code: '38341003', system: 'SNOMED-CT', display: 'Hypertensive disorder, systemic arterial' },
  { code: '44054006', system: 'SNOMED-CT', display: 'Diabetes mellitus type 2' },
];
