import type { PropsWithChildren, ReactNode } from 'react';

/** A titled content region. The heading level is explicit so pages keep a correct outline. */
export function Panel({ title, actions, subtitle, level = 2, children }:
PropsWithChildren<{ title: string; actions?: ReactNode; subtitle?: ReactNode; level?: 1 | 2 | 3 }>) {
  const Heading = `h${level}` as 'h1' | 'h2' | 'h3';
  return (
    <section className="panel">
      <div className="panel-head">
        <div>
          <Heading className="panel-title">{title}</Heading>
          {subtitle ? <p className="panel-subtitle">{subtitle}</p> : null}
        </div>
        {actions ? <div className="panel-actions">{actions}</div> : null}
      </div>
      {children}
    </section>
  );
}

/** Consistent empty state so a screen never renders as a blank region. */
export function EmptyState({ children }: PropsWithChildren) {
  return <p className="empty-state">{children}</p>;
}
