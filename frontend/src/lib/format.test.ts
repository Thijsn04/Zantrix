import { describe, expect, it } from 'vitest';
import { age, isToday, sexMarker } from './format';

describe('age', () => {
  it('counts whole years and excludes a birthday that has not happened yet', () => {
    const today = new Date();
    const beforeBirthday = new Date(today.getFullYear() - 30, today.getMonth(), today.getDate() + 1);
    const afterBirthday = new Date(today.getFullYear() - 30, today.getMonth(), today.getDate() - 1);
    expect(age(beforeBirthday.toISOString().slice(0, 10))).toBe(29);
    expect(age(afterBirthday.toISOString().slice(0, 10))).toBe(30);
  });

  it('returns null for missing or unparseable dates', () => {
    expect(age(null)).toBeNull();
    expect(age('not-a-date')).toBeNull();
  });
});

describe('sexMarker', () => {
  it('reduces the administrative gender to a single marker', () => {
    expect(sexMarker('female')).toBe('F');
    expect(sexMarker('male')).toBe('M');
  });

  it('does not claim a marker it does not have', () => {
    expect(sexMarker('unknown')).toBe('?');
    expect(sexMarker(null)).toBe('?');
  });
});

describe('isToday', () => {
  it('recognises the current day and rejects other days', () => {
    expect(isToday(new Date().toISOString())).toBe(true);
    expect(isToday('2001-01-01T10:00:00Z')).toBe(false);
    expect(isToday(null)).toBe(false);
  });
});
