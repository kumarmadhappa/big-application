export type Role = 'USER' | 'ADMIN';

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export interface AuthUser {
  id: number;
  username: string;
  email: string;
  roles: Role[];
}

export interface AuthResponse {
  tokenType: string;
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  user: AuthUser;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface LoginPayload {
  login: string;
  password: string;
}

export interface UserCreatePayload {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  roles: Role[];
}

export interface UserUpdatePayload {
  username?: string;
  email?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  enabled?: boolean;
  roles?: Role[];
}
