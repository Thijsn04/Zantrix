import type { KeyboardEvent, ReactNode } from 'react';

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
  function onKeyDown(event: KeyboardEvent<HTMLButtonElement>) {
    const index = tabs.findIndex(tab => tab.id === active);
    const next = event.key === 'ArrowRight' ? (index + 1) % tabs.length
      : event.key === 'ArrowLeft' ? (index - 1 + tabs.length) % tabs.length
      : event.key === 'Home' ? 0 : event.key === 'End' ? tabs.length - 1 : null;
    if (next === null) return;
    event.preventDefault();
    onChange(tabs[next].id);
    document.getElementById(`tab-${tabs[next].id}`)?.focus();
  }

  return (
    <>
      <div className="tabs" role="tablist" aria-label={label}>
        {tabs.map(tab =>
          <button key={tab.id} id={`tab-${tab.id}`} role="tab" type="button"
            aria-controls={`panel-${tab.id}`} aria-selected={tab.id === active}
            tabIndex={tab.id === active ? 0 : -1}
            onKeyDown={onKeyDown} onClick={() => onChange(tab.id)}>
            {tab.label}{typeof tab.count === 'number' ? <span className="tab-count">{tab.count}</span> : null}
          </button>)}
      </div>
      <div role="tabpanel" id={`panel-${active}`} aria-labelledby={`tab-${active}`} tabIndex={0} className="tab-panel">
        {children}
      </div>
    </>
  );
}
