export interface ClinicalTask {
  id?: string;
  patientId: string;
  assigneeId?: string;
  creatorId: string;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  title: string;
  description?: string;
  dueDate?: string;
  createdAt?: string;
  completedAt?: string;
}

const API_BASE = '/api/v1/tasks';

export async function fetchPatientTasks(patientId: string): Promise<ClinicalTask[]> {
  const response = await fetch(`${API_BASE}/patient/${patientId}`);
  if (!response.ok) throw new Error('Failed to fetch patient tasks');
  return response.json();
}

export async function fetchUserTasks(userId: string): Promise<ClinicalTask[]> {
  const response = await fetch(`${API_BASE}/user/${userId}`);
  if (!response.ok) throw new Error('Failed to fetch user tasks');
  return response.json();
}

export async function createTask(task: ClinicalTask): Promise<ClinicalTask> {
  const response = await fetch(`${API_BASE}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(task),
  });
  if (!response.ok) throw new Error('Failed to create task');
  return response.json();
}

export async function completeTask(id: string): Promise<ClinicalTask> {
  const response = await fetch(`${API_BASE}/${id}/complete`, {
    method: 'PUT',
  });
  if (!response.ok) throw new Error('Failed to complete task');
  return response.json();
}
