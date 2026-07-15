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

  async get<T>(path: string, signal?: AbortSignal): Promise<T> {
    return this.request<T>(path, { signal });
  }

  async post<T>(path: string, body?: unknown, signal?: AbortSignal): Promise<T> {
    return this.request<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body), signal });
  }

  async put<T>(path: string, body?: unknown, signal?: AbortSignal): Promise<T> {
    return this.request<T>(path, { method: 'PUT', body: body === undefined ? undefined : JSON.stringify(body), signal });
  }

  private async request<T>(path: string, init: RequestInit): Promise<T> {
    const response = await fetch(new URL(path, this.baseUrl), {
      ...init,
      headers: { Authorization: `Bearer ${this.accessToken}`, Accept: 'application/json',
        ...(init.body ? { 'Content-Type': 'application/json' } : {}) },
    });
    if (!response.ok) {
      const body = await response.json().catch((): ApiErrorBody => ({}));
      throw new ApiError(response.status, body.detail ?? body.message ?? body.title ?? response.statusText);
    }
    if (response.status === 204) return undefined as T;
    return response.json() as Promise<T>;
  }
}
