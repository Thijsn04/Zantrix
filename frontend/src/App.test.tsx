import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import App from './App';
import './i18n';

describe('App', () => {
  it('renders the localized application identity', () => {
    render(<App />);

    expect(screen.getByRole('heading', { name: 'Zantrix' })).toBeInTheDocument();
    expect(screen.getByText(/clinical application is being rebuilt/i)).toBeInTheDocument();
  });
});
