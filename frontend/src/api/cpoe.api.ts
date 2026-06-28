export interface MedicationRequest {
  id?: string;
  patientId: string;
  requesterId: string;
  status?: 'DRAFT' | 'ACTIVE' | 'ON_HOLD' | 'CANCELLED' | 'COMPLETED';
  intent?: string;
  medicationReference: string;
  dosageInstruction?: string;
  dispenseRequest?: string;
}

export interface ServiceRequest {
  id?: string;
  patientId: string;
  requesterId: string;
  status?: 'DRAFT' | 'ACTIVE' | 'ON_HOLD' | 'CANCELLED' | 'COMPLETED';
  code: string;
  reasonCode?: string;
}

const API_BASE = '/api/v1/cpoe';

export async function fetchDraftMedications(patientId: string): Promise<MedicationRequest[]> {
  const response = await fetch(`${API_BASE}/drafts/medication/${patientId}`);
  if (!response.ok) throw new Error('Failed to fetch draft medications');
  return response.json();
}

export async function fetchDraftServices(patientId: string): Promise<ServiceRequest[]> {
  const response = await fetch(`${API_BASE}/drafts/service/${patientId}`);
  if (!response.ok) throw new Error('Failed to fetch draft services');
  return response.json();
}

export async function saveDraftMedication(order: MedicationRequest): Promise<MedicationRequest> {
  const response = await fetch(`${API_BASE}/orders/medication/draft`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(order),
  });
  if (!response.ok) throw new Error('Failed to save draft medication');
  return response.json();
}

export async function signOrders(medicationIds: string[], serviceIds: string[]): Promise<void> {
  const response = await fetch(`${API_BASE}/orders/sign`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ medicationIds, serviceIds }),
  });
  if (!response.ok) throw new Error('Failed to sign orders');
}

// Temporary Mock for Terminology Server (Elasticsearch representation)
export const MOCK_MEDICATIONS = [
  { id: '1', name: 'Amoxicilline 500mg capsule', defaultDose: '3x daags 1 capsule' },
  { id: '2', name: 'Ibuprofen 400mg tablet', defaultDose: 'Zo nodig 3x daags 1 tablet' },
  { id: '3', name: 'Paracetamol 1000mg tablet', defaultDose: '4x daags 1 tablet' },
  { id: '4', name: 'Metformine 500mg tablet', defaultDose: '2x daags 1 tablet' },
];

export const MOCK_SERVICES = [
  { id: 'L1', name: 'HbA1c (Lab)' },
  { id: 'L2', name: 'CRP (Lab)' },
  { id: 'L3', name: 'Hemoglobine (Lab)' },
  { id: 'R1', name: 'X-Thorax (Radiologie)' },
];
