import type { ButtonHTMLAttributes } from 'react';

type IconButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & { label: string };

/** Icon-only button that always requires an accessible name. */
export function IconButton({ label, className = '', type = 'button', ...props }: IconButtonProps) {
  return <button {...props} type={type} aria-label={label} className={`icon-button ${className}`.trim()} />;
}
