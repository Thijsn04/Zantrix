/**
 * Presentation helpers for clinical data.
 *
 * Dates and numbers are rendered through the active locale so the workspace
 * follows the user's regional conventions rather than a hardcoded format.
 */

export function formatDate(value?: string | null, locale = 'en'): string {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString(locale, { year: 'numeric', month: 'short', day: '2-digit' });
}

export function formatDateTime(value?: string | null, locale = 'en'): string {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value
    : date.toLocaleString(locale, { year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

export function formatTime(value?: string | null, locale = 'en'): string {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' });
}

/** Whole years between a birth date and today, the form clinicians expect. */
export function age(birthDate?: string | null): number | null {
  if (!birthDate) return null;
  const born = new Date(birthDate);
  if (Number.isNaN(born.getTime())) return null;
  const today = new Date();
  let years = today.getFullYear() - born.getFullYear();
  const monthDelta = today.getMonth() - born.getMonth();
  if (monthDelta < 0 || (monthDelta === 0 && today.getDate() < born.getDate())) years -= 1;
  return years < 0 ? null : years;
}

/** Short sex marker used in patient headers, for example "F". */
export function sexMarker(administrativeGender?: string | null): string {
  if (!administrativeGender) return '?';
  const first = administrativeGender.trim().charAt(0).toUpperCase();
  return first === 'U' || first === '' ? '?' : first;
}

export function isToday(value?: string | null): boolean {
  if (!value) return false;
  const date = new Date(value);
  const now = new Date();
  return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth() && date.getDate() === now.getDate();
}
