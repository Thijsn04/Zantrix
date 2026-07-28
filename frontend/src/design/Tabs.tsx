import { useId, type KeyboardEvent, type ReactNode } from 'react';

export interface TabDefinition<T extends string> {
  id: T;
  label: string;
  /** Optional count shown alongside the label, for worklist style tabs. */
  count?: number;
}

/**
 * Tabs following the ARIA authoring practice: a single tab stop, arrow-key
 * navigation between tabs, and a labelled panel bound to the selected tab.
 */
export function Tabs<T extends string>({ label, tabs, active, onChange, children }: {
  label: string;
  tabs: TabDefinition<T>[];
  active: T;
  onChange: (tab: T) => void;
  children: ReactNode;
}) {
  // Several tab groups can be mounted at once, for example two open patient
  // charts, so the ids must be unique per instance rather than per tab name.
  const scope = useId();
  const tabId = (id: T) => `${scope}-tab-${id}`;
  const panelId = (id: T) => `${scope}-panel-${id}`;

  function onKeyDown(event: KeyboardEvent<HTMLButtonElement>) {
    const index = tabs.findIndex(tab => tab.id === active);
    const next = event.key === 'ArrowRight' ? (index + 1) % tabs.length
      : event.key === 'ArrowLeft' ? (index - 1 + tabs.length) % tabs.length
      : event.key === 'Home' ? 0 : event.key === 'End' ? tabs.length - 1 : null;
    if (next === null) return;
    event.preventDefault();
    onChange(tabs[next].id);
    document.getElementById(tabId(tabs[next].id))?.focus();
  }

  return (
    <>
      <div className="tabs" role="tablist" aria-label={label}>
        {tabs.map(tab =>
          <button key={tab.id} id={tabId(tab.id)} role="tab" type="button"
            aria-controls={panelId(tab.id)} aria-selected={tab.id === active}
            tabIndex={tab.id === active ? 0 : -1}
            onKeyDown={onKeyDown} onClick={() => onChange(tab.id)}>
            {tab.label}{typeof tab.count === 'number' ? <span className="tab-count">{tab.count}</span> : null}
          </button>)}
      </div>
      <div role="tabpanel" id={panelId(active)} aria-labelledby={tabId(active)} tabIndex={0} className="tab-panel">
        {children}
      </div>
    </>
  );
}
