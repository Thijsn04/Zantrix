import { useState, useEffect, useCallback } from 'react';
import { useAuth } from 'react-oidc-context';

/**
 * Represents a medical appointment in the frontend application.
 */
export interface Appointment {
  id?: string;
  patientId: string;
  practitionerId: string;
  locationId?: string;
  startTime: string;
  endTime: string;
  status: string;
  fhirData?: any;
}

/**
 * Custom hook for fetching and managing appointments.
 * 
 * Automatically fetches appointments for a specific practitioner within a given time range.
 * Requires the user to be authenticated.
 *
 * @param {string} [practitionerId] - The UUID of the practitioner.
 * @param {string} [start] - The ISO string representing the start of the query window.
 * @param {string} [end] - The ISO string representing the end of the query window.
 * @returns An object containing the appointments, loading state, error state, and mutator methods.
 */
export function useAppointments(practitionerId?: string, start?: string, end?: string) {
  const auth = useAuth();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const fetchAppointments = useCallback(async () => {
    if (!auth.isAuthenticated || !practitionerId || !start || !end) return;
    
    setIsLoading(true);
    setError(null);
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/appointments?practitionerId=${practitionerId}&start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`,
        {
          headers: { 'Authorization': `Bearer ${auth.user?.access_token}` }
        }
      );
      if (!response.ok) throw new Error('Failed to fetch appointments');
      const data = await response.json();
      setAppointments(data);
    } catch (err: any) {
      setError(err);
    } finally {
      setIsLoading(false);
    }
  }, [auth.isAuthenticated, auth.user?.access_token, practitionerId, start, end]);

  useEffect(() => {
    fetchAppointments();
  }, [fetchAppointments]);

  const createAppointment = async (appointment: Appointment) => {
    const response = await fetch('http://localhost:8080/api/v1/appointments', {
      method: 'POST',
      headers: { 
        'Authorization': `Bearer ${auth.user?.access_token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(appointment)
    });
    if (!response.ok) throw new Error('Failed to create appointment');
    const newAppt = await response.json();
    setAppointments(prev => [...prev, newAppt]);
    return newAppt;
  };

  return { appointments, isLoading, error, createAppointment, refresh: fetchAppointments };
}
