import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { act } from 'react';
import { IDLE_TIMEOUT_MS, IdleLock } from './IdleLock';
import '../i18n';

describe('IdleLock', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  const idle = (by = IDLE_TIMEOUT_MS) => act(() => { vi.advanceTimersByTime(by); });

  it('stays out of the way while the workstation is in use', () => {
    render(<IdleLock user="Dr. Test" onSignOut={vi.fn()} />);
    idle(IDLE_TIMEOUT_MS - 1000);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('covers the record once the workstation is left unattended', () => {
    render(<IdleLock user="Dr. Test" onSignOut={vi.fn()} />);
    idle();
    expect(screen.getByRole('dialog', { name: 'Workspace locked' })).toBeInTheDocument();
    // Naming the signed-in user makes it obvious whose session is still open.
    expect(screen.getByText('Dr. Test')).toBeInTheDocument();
  });

  it('restarts the countdown on activity', () => {
    render(<IdleLock user="Dr. Test" onSignOut={vi.fn()} />);
    idle(IDLE_TIMEOUT_MS - 1000);
    act(() => { window.dispatchEvent(new Event('keydown')); });
    idle(IDLE_TIMEOUT_MS - 1000);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('uncovers the record without touching the session', () => {
    const onSignOut = vi.fn();
    render(<IdleLock user="Dr. Test" onSignOut={onSignOut} />);
    idle();

    // fireEvent is synchronous, so it does not depend on the fake clock.
    fireEvent.click(screen.getByRole('button', { name: 'Resume' }));
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    // Resuming is a privacy measure, not a sign out.
    expect(onSignOut).not.toHaveBeenCalled();
  });

  it('offers signing out as the way to hand the workstation over', () => {
    const onSignOut = vi.fn();
    render(<IdleLock user="Dr. Test" onSignOut={onSignOut} />);
    idle();

    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));
    expect(onSignOut).toHaveBeenCalled();
  });
});
