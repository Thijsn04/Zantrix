export interface AllergyIntolerance {
  id?: string;
  patientId: string;
  recorderId: string;
  type: 'ALLERGY' | 'INTOLERANCE';
  criticality: 'LOW' | 'HIGH' | 'UNABLE_TO_ASSESS';
  code: string;
  display: string;
  reactions?: string; // JSON
  note?: string;
  createdAt?: string;
}

const API_BASE = '/api/v1/allergies';

export async function fetchAllergies(patientId: string): Promise<AllergyIntolerance[]> {
  const response = await fetch(`${API_BASE}/patient/${patientId}`);
  if (!response.ok) throw new Error('Failed to fetch allergies');
  return response.json();
}

export async function recordAllergy(allergy: AllergyIntolerance): Promise<AllergyIntolerance> {
  const response = await fetch(`${API_BASE}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(allergy),
  });
  if (!response.ok) throw new Error('Failed to record allergy');
  return response.json();
}

export async function searchAllergens(query: string): Promise<{code: string, display: string}[]> {
  const response = await fetch(`/api/terminology/search?query=${encodeURIComponent(query)}`);
  if (!response.ok) return [];
  const concepts = await response.json();
  return concepts.map((c: any) => ({ code: c.code, display: c.preferredTerm }));
}
