export interface SlotInterval {
  start: string;
  end: string;
}

/** The largest number of slots one schedule submission may create. */
export const MAX_GENERATED_SLOTS = 96;

/**
 * Divides a clinic day into the individual intervals patients can be booked into.
 *
 * A partial interval at the end is dropped rather than shortened, because a
 * five minute remainder is not a bookable appointment. The count is bounded so
 * a mistyped range cannot generate an unusable schedule.
 */
export function buildSlots(date: string, from: string, to: string, minutes: number): SlotInterval[] {
  if (!date || !from || !to || !Number.isFinite(minutes) || minutes <= 0) return [];
  const start = new Date(`${date}T${from}`);
  const end = new Date(`${date}T${to}`);
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end <= start) return [];

  const slots: SlotInterval[] = [];
  let cursor = start;
  while (cursor < end && slots.length < MAX_GENERATED_SLOTS) {
    const next = new Date(cursor.getTime() + minutes * 60_000);
    if (next > end) break;
    slots.push({ start: cursor.toISOString(), end: next.toISOString() });
    cursor = next;
  }
  return slots;
}

/**
 * The local day, expressed as the instants a server should search between.
 *
 * The window is resolved here rather than server side, because only the client
 * knows which timezone the user means by "today".
 */
export function dayWindow(date: string): { from: string; to: string } | null {
  const start = new Date(`${date}T00:00`);
  if (Number.isNaN(start.getTime())) return null;
  const end = new Date(start);
  end.setDate(end.getDate() + 1);
  return { from: start.toISOString(), to: end.toISOString() };
}
