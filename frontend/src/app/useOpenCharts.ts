import { useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQueries, useQueryClient } from '@tanstack/react-query';
import type { ApiClient } from '../lib/api/client';
import type { PatientSummary } from '../lib/api/types';

/** Two charts is the working limit: enough to compare or hand over, few enough to stay unambiguous. */
export const MAX_OPEN_PATIENTS = 2;

export interface OpenCharts {
  open: PatientSummary[];
  activeId?: string;
  limitReached: boolean;
  loading: boolean;
  openPatient: (patient: PatientSummary) => void;
  activate: (id: string) => void;
  close: (id: string) => void;
  search: () => void;
}

/**
 * The charts this window has open.
 *
 * Patient context belongs to the window and is resolved from its URL, so
 * nothing done in one window changes what another window is showing. That is
 * what makes a second window safe to open at all.
 *
 * The open set is derived from the ids this window has opened plus whatever the
 * URL currently names, so a deep link, a reload, or a back navigation to a
 * closed chart all reopen it rather than landing on an empty area. Summaries
 * come from the query cache, so no state has to be synchronized to the route.
 */
export function useOpenCharts(client: ApiClient): OpenCharts {
  const { patientId } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [opened, setOpened] = useState<string[]>(() => patientId ? [patientId] : []);

  const openIds = useMemo(() => {
    if (!patientId || opened.includes(patientId)) return opened;
    return opened.length >= MAX_OPEN_PATIENTS ? opened : [...opened, patientId];
  }, [opened, patientId]);

  const results = useQueries({
    queries: openIds.map(id => ({
      queryKey: ['patient', id],
      queryFn: ({ signal }: { signal: AbortSignal }) => client.get<PatientSummary>(
        `/api/v1/patients/${encodeURIComponent(id)}`, signal),
    })),
  });

  const open = results.flatMap(result => result.data ? [result.data] : []);
  const activeId = openIds.includes(patientId ?? '') ? patientId : undefined;

  function openPatient(patient: PatientSummary) {
    if (openIds.includes(patient.id)) {
      navigate(`/patients/${encodeURIComponent(patient.id)}`);
      return;
    }
    if (openIds.length >= MAX_OPEN_PATIENTS) {
      // Refused rather than silently closing one: losing track of which patient
      // is in context is the classic wrong-patient error.
      navigate('/patients');
      return;
    }
    // The summary is already in hand, so seed the cache instead of refetching.
    queryClient.setQueryData(['patient', patient.id], patient);
    setOpened([...openIds, patient.id]);
    navigate(`/patients/${encodeURIComponent(patient.id)}`);
  }

  function close(id: string) {
    const remaining = openIds.filter(candidate => candidate !== id);
    setOpened(remaining);
    if (patientId === id) {
      navigate(remaining[0] ? `/patients/${encodeURIComponent(remaining[0])}` : '/patients');
    }
  }

  return {
    open,
    activeId,
    // A routed patient the open set could not accept is one the limit refused.
    limitReached: Boolean(patientId) && !activeId,
    loading: results.some(result => result.isLoading),
    openPatient,
    activate: id => navigate(`/patients/${encodeURIComponent(id)}`),
    close,
    search: () => navigate('/patients'),
  };
}
