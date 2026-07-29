import { useEffect, useRef, useState } from 'react';
import { Lock } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '../design/Button';

/** How long a workstation may sit untouched before the record is covered. */
export const IDLE_TIMEOUT_MS = 10 * 60 * 1000;

const ACTIVITY = ['pointerdown', 'keydown', 'wheel', 'touchstart'] as const;

/**
 * Covers the record when a shared workstation is left unattended.
 *
 * This hides the interface without unmounting it, so nothing in progress is
 * lost and the clinician returns to exactly what they left. It is a privacy
 * measure, not authentication: the session is untouched, and signing out
 * remains the way to leave the workstation to someone else.
 */
export function IdleLock({ user, onSignOut }: { user: string; onSignOut: () => void }) {
  const { t } = useTranslation();
  const [locked, setLocked] = useState(false);
  const resumeRef = useRef<HTMLButtonElement>(null);
  const restoreFocusTo = useRef<Element | null>(null);

  useEffect(() => {
    if (locked) return undefined;
    let timer = window.setTimeout(() => {
      restoreFocusTo.current = document.activeElement;
      setLocked(true);
    }, IDLE_TIMEOUT_MS);

    const reset = () => {
      window.clearTimeout(timer);
      timer = window.setTimeout(() => {
        restoreFocusTo.current = document.activeElement;
        setLocked(true);
      }, IDLE_TIMEOUT_MS);
    };
    ACTIVITY.forEach(event => window.addEventListener(event, reset, { passive: true }));
    return () => {
      window.clearTimeout(timer);
      ACTIVITY.forEach(event => window.removeEventListener(event, reset));
    };
  }, [locked]);

  useEffect(() => {
    if (locked) resumeRef.current?.focus();
  }, [locked]);

  if (!locked) return null;

  function resume() {
    setLocked(false);
    // Return the clinician to whatever they were working in.
    if (restoreFocusTo.current instanceof HTMLElement) restoreFocusTo.current.focus();
  }

  return (
    <div className="idle-lock" role="dialog" aria-modal="true" aria-labelledby="idle-lock-title">
      <section>
        <Lock size={22} aria-hidden="true" />
        <h2 id="idle-lock-title">{t('lock.title')}</h2>
        <p>{t('lock.description')}</p>
        <p className="lock-user">{user}</p>
        <div className="form-actions">
          <Button ref={resumeRef} onClick={resume}>{t('lock.resume')}</Button>
          <Button className="quiet-button" onClick={onSignOut}>{t('session.signOut')}</Button>
        </div>
      </section>
    </div>
  );
}
