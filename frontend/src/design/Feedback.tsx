import { useTranslation } from 'react-i18next';
import { ApiError } from '../lib/api/client';
import type { PropsWithChildren } from 'react';

/**
 * Renders a failed request as an explicit, readable message. Backend problem
 * details are shown verbatim; anything else falls back to a generic message
 * rather than leaking an internal error string.
 */
export function ErrorNotice({ error }: { error: unknown }) {
  const { t } = useTranslation();
  if (!error) return null;
  return <p className="notice notice-danger" role="alert">{error instanceof ApiError ? error.message : t('common.failed')}</p>;
}

export function Notice({ tone = 'info', children }: PropsWithChildren<{ tone?: 'info' | 'warning' | 'danger' }>) {
  return <p className={`notice notice-${tone}`}>{children}</p>;
}

/** Inline busy state for a region that is loading for the first time. */
export function Loading() {
  const { t } = useTranslation();
  return <p className="empty-state" aria-busy="true">{t('common.loading')}</p>;
}
