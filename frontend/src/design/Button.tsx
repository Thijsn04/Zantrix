import { forwardRef, type ButtonHTMLAttributes, type PropsWithChildren } from 'react';

type ButtonProps = PropsWithChildren<ButtonHTMLAttributes<HTMLButtonElement>>;

/** Consistent, accessible primary action primitive. */
export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { children, className = '', type = 'button', ...props }, ref,
) {
  return <button {...props} ref={ref} type={type} className={`button ${className}`.trim()}>{children}</button>;
});
