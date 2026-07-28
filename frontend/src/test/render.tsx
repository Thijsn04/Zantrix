import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, type RenderResult } from '@testing-library/react';
import type { ReactElement } from 'react';
import { vi } from 'vitest';
import { ApiClient } from '../lib/api/client';
import '../i18n';

/**
 * A test double for the sole HTTP boundary. The workspace only ever calls
 * get/post/put, so a fake with those three methods stands in for the real
 * client without touching the network, and keeps tests type-safe against the
 * real ApiClient contract.
 */
export interface FakeApiClient {
  get: ReturnType<typeof vi.fn>;
  post: ReturnType<typeof vi.fn>;
  put: ReturnType<typeof vi.fn>;
}

export function fakeClient(overrides: Partial<FakeApiClient> = {}): { client: ApiClient; fake: FakeApiClient } {
  const fake: FakeApiClient = {
    get: vi.fn().mockResolvedValue([]),
    post: vi.fn().mockResolvedValue({}),
    put: vi.fn().mockResolvedValue({}),
    ...overrides,
  };
  return { client: fake as unknown as ApiClient, fake };
}

/** Renders a component inside a fresh React Query provider that never retries. */
export function renderWithQuery(ui: ReactElement): RenderResult {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>);
}
