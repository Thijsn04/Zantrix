import { createContext, useContext } from 'react';

/**
 * Encounter and author context for clinical entry.
 *
 * Almost every clinical write needs the encounter it belongs to and the
 * practitioner responsible. Asking for both on every single form is what made
 * the earlier workspace feel like a set of API forms, so they are captured once
 * per chart session and reused, while staying visible and changeable.
 */
export interface ClinicalContextValue {
  encounterId: string;
  practitionerId: string;
  setEncounterId: (value: string) => void;
  setPractitionerId: (value: string) => void;
  ready: boolean;
}

export const ClinicalContext = createContext<ClinicalContextValue | undefined>(undefined);

export function useClinicalContext(): ClinicalContextValue {
  const value = useContext(ClinicalContext);
  if (!value) throw new Error('useClinicalContext must be used inside ClinicalContextProvider');
  return value;
}
