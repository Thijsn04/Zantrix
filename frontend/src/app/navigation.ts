import type { CurrentUser } from '../lib/api/types';
import { can, type Capability } from '../lib/roles';

export type WorkspacePage = 'home' | 'patients' | 'schedule' | 'tasks' | 'admin' | 'privacy';

export interface NavigationItem {
  page: WorkspacePage;
  path: string;
  labelKey: string;
  /** The capability a user needs for this destination, or undefined when always available. */
  capability?: Capability;
}

/**
 * Top level destinations.
 *
 * A patient's own appointments, notes and results are sections of their chart
 * rather than destinations, because they only mean something once a patient is
 * in context. The department schedule is here instead: a front desk works a day
 * across every patient and cannot pick one first.
 */
const ITEMS: NavigationItem[] = [
  { page: 'home', path: '/home', labelKey: 'navigation.home' },
  { page: 'patients', path: '/patients', labelKey: 'navigation.patients', capability: 'patients' },
  { page: 'schedule', path: '/schedule', labelKey: 'navigation.schedule', capability: 'scheduling' },
  { page: 'tasks', path: '/worklist', labelKey: 'navigation.tasks', capability: 'tasks' },
  { page: 'admin', path: '/admin', labelKey: 'navigation.admin', capability: 'administration' },
  { page: 'privacy', path: '/privacy', labelKey: 'navigation.privacy', capability: 'privacy' },
];

export function navigationFor(user: CurrentUser | undefined): NavigationItem[] {
  return ITEMS.filter(item => !item.capability || can(user, item.capability));
}

/**
 * Where a role starts its day.
 *
 * A privacy officer has no clinical duties, so they land on review work. A
 * pharmacist cannot read the patient directory, so they land on their queue
 * rather than a search box that would only return an authorization error.
 */
export function landingPath(user: CurrentUser | undefined): string {
  if (!user) return '/home';
  if (user.roles.includes('PRIVACY_OFFICER') && !user.roles.includes('PHYSICIAN')) return '/privacy';
  if (user.roles.includes('PHARMACIST') && !can(user, 'patients')) return '/worklist';
  return '/home';
}

/** Which destination a path belongs to, so navigation can mark the current one. */
export function activePage(pathname: string): WorkspacePage | undefined {
  return ITEMS.find(item => pathname === item.path || pathname.startsWith(`${item.path}/`))?.page;
}
