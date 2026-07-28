import type { CurrentUser } from '../lib/api/types';
import { can, type Capability } from '../lib/roles';

export type WorkspacePage = 'home' | 'patients' | 'schedule' | 'tasks' | 'admin' | 'privacy';

interface NavigationItem {
  page: WorkspacePage;
  labelKey: string;
  /** The capability a user needs for this destination, or undefined when always available. */
  capability?: Capability;
}

const ITEMS: NavigationItem[] = [
  { page: 'home', labelKey: 'navigation.home' },
  { page: 'patients', labelKey: 'navigation.patients', capability: 'patients' },
  { page: 'schedule', labelKey: 'navigation.schedule', capability: 'scheduling' },
  { page: 'tasks', labelKey: 'navigation.tasks', capability: 'tasks' },
  { page: 'admin', labelKey: 'navigation.admin', capability: 'administration' },
  { page: 'privacy', labelKey: 'navigation.privacy', capability: 'privacy' },
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
export function landingPage(user: CurrentUser | undefined): WorkspacePage {
  if (!user) return 'home';
  if (user.roles.includes('PRIVACY_OFFICER') && !user.roles.includes('PHYSICIAN')) return 'privacy';
  if (user.roles.includes('PHARMACIST') && !can(user, 'patients')) return 'tasks';
  return 'home';
}
