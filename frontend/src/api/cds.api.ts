export interface CdsAlert {
  id?: string;
  patientId: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  title: string;
  message: string;
  dismissed?: boolean;
  createdAt?: string;
}

const API_BASE = '/api/v1/cds';

export async function fetchActiveAlerts(patientId: string): Promise<CdsAlert[]> {
  const response = await fetch(`${API_BASE}/patient/${patientId}`);
  if (!response.ok) throw new Error('Failed to fetch CDS alerts');
  return response.json();
}

export async function dismissAlert(alertId: string, dismissedBy: string): Promise<void> {
  const response = await fetch(`${API_BASE}/alerts/${alertId}/dismiss?dismissedBy=${dismissedBy}`, {
    method: 'PUT'
  });
  if (!response.ok) throw new Error('Failed to dismiss alert');
}
