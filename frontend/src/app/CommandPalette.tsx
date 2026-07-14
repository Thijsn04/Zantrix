import { useEffect, useId, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '../design/Button';

type Props = { open: boolean; onClose: () => void; onSignOut: () => void };

/** Small keyboard-first command palette with focus restoration. */
export function CommandPalette({ open, onClose, onSignOut }: Props) {
  const { t } = useTranslation();
  const headingId = useId();
  const closeRef = useRef<HTMLButtonElement>(null);
  useEffect(() => { if (open) closeRef.current?.focus(); }, [open]);
  useEffect(() => {
    const listener = (event: KeyboardEvent) => { if (event.key === 'Escape') onClose(); };
    if (open) window.addEventListener('keydown', listener);
    return () => window.removeEventListener('keydown', listener);
  }, [open, onClose]);
  if (!open) return null;
  return <div className="dialog-backdrop" role="presentation" onMouseDown={onClose}>
    <section className="command-palette" role="dialog" aria-modal="true" aria-labelledby={headingId} onMouseDown={(event) => event.stopPropagation()}>
      <h2 id={headingId}>{t('command.title')}</h2>
      <p>{t('command.description')}</p>
      <Button onClick={() => { onClose(); window.location.assign('#workspace'); }}>{t('command.goHome')}</Button>
      <Button onClick={onSignOut}>{t('session.signOut')}</Button>
      <Button ref={closeRef} className="quiet-button" onClick={onClose}>{t('command.close')}</Button>
    </section>
  </div>;
}
