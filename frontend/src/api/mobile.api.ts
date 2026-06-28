import { ClinicalTask } from './tasks.api';

export interface MobileDashboardData {
  activeTasksCount: number;
  tasks: ClinicalTask[];
}

const API_BASE = '/api/v1/mobile-bff';

export async function fetchMobileDashboard(userId: string): Promise<MobileDashboardData> {
  const response = await fetch(`${API_BASE}/dashboard/${userId}`);
  if (!response.ok) throw new Error('Failed to fetch mobile dashboard data');
  return response.json();
}
