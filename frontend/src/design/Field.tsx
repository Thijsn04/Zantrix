import { useId, type PropsWithChildren, type ReactNode } from 'react';

/**
 * Labelled form control. The label is always associated with the control by id,
 * so every input in the workspace has an accessible name.
 */
export function Field({ label, hint, required, children }:
{ label: string; hint?: string; required?: boolean; children: (id: string, describedBy?: string) => ReactNode }) {
  const id = useId();
  const hintId = hint ? `${id}-hint` : undefined;
  return (
    <div className="field">
      <label htmlFor={id}>{label}{required ? <span aria-hidden="true"> *</span> : null}</label>
      {children(id, hintId)}
      {hint ? <p className="field-hint" id={hintId}>{hint}</p> : null}
    </div>
  );
}

/** Groups related fields under a legend, so long clinical forms stay readable. */
export function FieldSet({ legend, children }: PropsWithChildren<{ legend: string }>) {
  return <fieldset className="field-set"><legend>{legend}</legend><div className="field-grid">{children}</div></fieldset>;
}
