import { config } from '../../config';
import type { ApiErrorBody, CurrentUser } from './types';

export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/** The sole HTTP boundary for frontend features. */
export class ApiClient {
  private readonly accessToken: string;
  private readonly baseUrl: string;

  constructor(accessToken: string, baseUrl = config.apiBaseUrl) {
    this.accessToken = accessToken;
    this.baseUrl = baseUrl;
  }

  async currentUser(signal?: AbortSignal): Promise<CurrentUser> {
    return this.get<CurrentUser>('/api/v1/iam/me', signal);
  }

  private async get<T>(path: string, signal?: AbortSignal): Promise<T> {
    const response = await fetch(new URL(path, this.baseUrl), {
      headers: { Authorization: `Bearer ${this.accessToken}`, Accept: 'application/json' },
      signal,
    });
    if (!response.ok) {
      const body = await response.json().catch((): ApiErrorBody => ({}));
      throw new ApiError(response.status, body.message ?? response.statusText);
    }
    return response.json() as Promise<T>;
  }
}
