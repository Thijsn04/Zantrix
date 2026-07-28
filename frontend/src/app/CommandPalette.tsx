import { useEffect, useId, useMemo, useRef, useState, type KeyboardEvent } from 'react';
import { useTranslation } from 'react-i18next';
import type { PatientSummary } from '../lib/api/types';
import type { WorkspacePage } from './navigation';

interface Destination { page: WorkspacePage; labelKey: string }

interface Props {
  onClose: () => void;
  onSignOut: () => void;
  destinations: Destination[];
  onNavigate: (page: WorkspacePage) => void;
  openPatients: PatientSummary[];
  activePatientId?: string;
  onActivatePatient: (id: string) => void;
  onClosePatient: (id: string) => void;
}

/**
 * Keyboard-first command palette.
 *
 * It lists the destinations the signed-in user is actually allowed to reach,
 * plus the session actions. Arrow keys move, Enter runs, Escape closes.
 *
 * The component is mounted only while the palette is open, so its filter and
 * selection start fresh every time without an effect resetting them.
 */
export function CommandPalette({
  onClose, onSignOut, destinations, onNavigate,
  openPatients, activePatientId, onActivatePatient, onClosePatient,
}: Props) {
  const { t } = useTranslation();
  const headingId = useId();
  const inputRef = useRef<HTMLInputElement>(null);
  const [filter, setFilter] = useState('');
  const [highlighted, setHighlighted] = useState(0);

  const commands = useMemo(() => {
    const entries = destinations.map(destination => ({
      id: `go-${destination.page}`,
      label: t('command.goTo', { target: t(destination.labelKey) }),
      run: () => onNavigate(destination.page),
    }));
    // Switching between the open charts is the most frequent action once more
    // than one patient is open, so it belongs in the palette.
    for (const patient of openPatients) {
      if (patient.id !== activePatientId) {
        entries.push({
          id: `switch-${patient.id}`,
          label: t('command.switchPatient', { name: patient.displayName }),
          run: () => onActivatePatient(patient.id),
        });
      }
      entries.push({
        id: `close-${patient.id}`,
        label: t('command.clearPatient', { name: patient.displayName }),
        run: () => onClosePatient(patient.id),
      });
    }
    entries.push({ id: 'sign-out', label: t('session.signOut'), run: onSignOut });
    const needle = filter.trim().toLowerCase();
    return needle ? entries.filter(entry => entry.label.toLowerCase().includes(needle)) : entries;
  }, [activePatientId, destinations, filter, onActivatePatient, onClosePatient, onNavigate, onSignOut, openPatients, t]);

  // Focusing the input is a DOM side effect, not state, so it belongs in an effect.
  useEffect(() => { inputRef.current?.focus(); }, []);

  // Clamping during render keeps the selection valid as the filter narrows the list.
  const active = commands.length === 0 ? 0 : Math.min(highlighted, commands.length - 1);

  function onKeyDown(event: KeyboardEvent<HTMLDivElement>) {
    if (event.key === 'Escape') { onClose(); return; }
    if (commands.length === 0) return;
    if (event.key === 'ArrowDown') { event.preventDefault(); setHighlighted((active + 1) % commands.length); }
    if (event.key === 'ArrowUp') { event.preventDefault(); setHighlighted((active - 1 + commands.length) % commands.length); }
    if (event.key === 'Enter') { event.preventDefault(); commands[active].run(); onClose(); }
  }

  return (
    <div className="dialog-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="command-palette" role="dialog" aria-modal="true" aria-labelledby={headingId}
        onMouseDown={event => event.stopPropagation()} onKeyDown={onKeyDown}>
        <h2 id={headingId}>{t('command.title')}</h2>
        <input ref={inputRef} type="text" value={filter} aria-label={t('command.filter')}
          placeholder={t('command.filter')} onChange={event => setFilter(event.target.value)} />
        <div className="command-list" role="listbox" aria-label={t('command.title')}>
          {commands.length === 0 ? <p className="field-hint">{t('command.noMatches')}</p> : commands.map((command, position) => (
            <button key={command.id} role="option" aria-selected={position === active}
              onMouseEnter={() => setHighlighted(position)}
              onClick={() => { command.run(); onClose(); }}>
              {command.label}
              {position === active ? <span className="command-hint">{t('command.enter')}</span> : null}
            </button>
          ))}
        </div>
      </section>
    </div>
  );
}
