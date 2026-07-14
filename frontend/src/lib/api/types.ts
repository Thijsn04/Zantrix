export interface CurrentUser {
  subject: string;
  username: string | null;
  displayName: string | null;
  roles: string[];
  scopes: string[];
}

export interface ApiErrorBody {
  message?: string;
}
