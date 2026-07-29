import { useId, type PropsWithChildren, type ReactNode } from 'react';

/**
 * A titled content region.
 *
 * The heading level is explicit so pages keep a correct outline, and the
 * section is named by that heading so it is exposed as a landmark a screen
 * reader user can jump between. A dense clinical screen is several panels, and
 * without names they are all just "section".
 */
export function Panel({ title, actions, subtitle, level = 2, children }:
PropsWithChildren<{ title: string; actions?: ReactNode; subtitle?: ReactNode; level?: 1 | 2 | 3 }>) {
  const Heading = `h${level}` as 'h1' | 'h2' | 'h3';
  const headingId = useId();
  return (
    <section className="panel" aria-labelledby={headingId}>
      <div className="panel-head">
        <div>
          <Heading className="panel-title" id={headingId}>{title}</Heading>
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
