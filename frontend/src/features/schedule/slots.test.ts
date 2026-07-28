import { describe, expect, it } from 'vitest';
import { buildSlots, dayWindow, MAX_GENERATED_SLOTS } from './slots';

describe('buildSlots', () => {
  it('divides a range into whole slots', () => {
    const slots = buildSlots('2026-08-03', '09:00', '10:00', 15);
    expect(slots).toHaveLength(4);
    expect(new Date(slots[0].start).getHours()).toBe(9);
    expect(slots[0].end).toBe(slots[1].start);
  });

  it('drops a trailing partial slot rather than shortening it', () => {
    // 50 minutes at 20 minutes each leaves 10 minutes, which is not bookable.
    const slots = buildSlots('2026-08-03', '09:00', '09:50', 20);
    expect(slots).toHaveLength(2);
    expect(new Date(slots[1].end).getMinutes()).toBe(40);
  });

  it('returns nothing when the range is empty or inverted', () => {
    expect(buildSlots('2026-08-03', '10:00', '10:00', 15)).toEqual([]);
    expect(buildSlots('2026-08-03', '11:00', '09:00', 15)).toEqual([]);
  });

  it('returns nothing when a field is missing or the interval is not positive', () => {
    expect(buildSlots('', '09:00', '10:00', 15)).toEqual([]);
    expect(buildSlots('2026-08-03', '09:00', '10:00', 0)).toEqual([]);
    expect(buildSlots('2026-08-03', '09:00', '10:00', Number.NaN)).toEqual([]);
  });

  it('bounds the number of slots so a mistyped range cannot flood the schedule', () => {
    const slots = buildSlots('2026-08-03', '00:00', '23:59', 5);
    expect(slots).toHaveLength(MAX_GENERATED_SLOTS);
  });
});

describe('dayWindow', () => {
  it('spans exactly one local day', () => {
    const window = dayWindow('2026-08-03');
    expect(window).not.toBeNull();
    const hours = (Date.parse(window!.to) - Date.parse(window!.from)) / 3_600_000;
    expect(hours).toBe(24);
  });

  it('returns nothing for an unparseable date', () => {
    expect(dayWindow('')).toBeNull();
    expect(dayWindow('not-a-date')).toBeNull();
  });
});
