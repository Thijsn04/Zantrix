import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import App from './App';
import './i18n';

vi.mock('react-oidc-context', () => ({ useAuth: () => ({ isLoading: false, isAuthenticated: false, signinRedirect: vi.fn() }) }));

describe('App', () => {
  it('offers an accessible sign-in entry point when no session exists', () => {
    render(<App />);
    expect(screen.getByRole('heading', { name: 'Zantrix' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Sign in' })).toBeInTheDocument();
  });
});
