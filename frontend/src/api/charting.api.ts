export interface ClinicalNote {
  id?: string;
  patientId: string;
  encounterId?: string;
  authorId: string;
  supervisorId?: string;
  status?: 'IN_PROGRESS' | 'PRELIMINARY' | 'FINAL' | 'AMENDED' | 'ENTERED_IN_ERROR';
  noteType: string;
  content: string; // JSON string
  fhirData?: string;
  createdAt?: string;
  signedAt?: string;
}

export interface SmartTemplate {
  id?: string;
  shortcut: string;
  title: string;
  contentTemplate: string;
  authorId?: string;
}

const API_BASE = '/api/v1/charting';

export async function fetchNotes(patientId: string): Promise<ClinicalNote[]> {
  const response = await fetch(`${API_BASE}/notes/patient/${patientId}`);
  if (!response.ok) throw new Error('Failed to fetch notes');
  return response.json();
}

export async function saveDraft(note: ClinicalNote): Promise<ClinicalNote> {
  const response = await fetch(`${API_BASE}/notes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(note),
  });
  if (!response.ok) throw new Error('Failed to save draft');
  return response.json();
}

export async function signNote(noteId: string, authorId: string): Promise<ClinicalNote> {
  const response = await fetch(`${API_BASE}/notes/${noteId}/sign?authorId=${authorId}`, {
    method: 'POST',
  });
  if (!response.ok) throw new Error('Failed to sign note');
  return response.json();
}

export async function fetchTemplates(authorId: string): Promise<SmartTemplate[]> {
  const response = await fetch(`${API_BASE}/templates?authorId=${authorId}`);
  if (!response.ok) throw new Error('Failed to fetch templates');
  return response.json();
}
