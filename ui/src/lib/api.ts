import type {
  ApiResponse,
  AuthResponse,
  LoginPayload,
  User,
  UserCreatePayload,
  UserUpdatePayload
} from '@/types';

async function requestJson<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
  const response = await fetch(input, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {})
    },
    credentials: 'include'
  });

  const text = await response.text();
  const body = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(body?.message ?? 'Request failed');
  }

  return body as T;
}

export async function login(payload: LoginPayload) {
  return requestJson<ApiResponse<AuthResponse>>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function logout() {
  return requestJson<ApiResponse<{ success: boolean }>>('/api/auth/logout', {
    method: 'POST'
  });
}

export async function fetchSession() {
  return requestJson<{ authenticated: boolean }>('/api/session');
}

export async function fetchUsers() {
  const result = await requestJson<ApiResponse<User[]>>('/api/users');
  return result.data;
}

export async function createUser(payload: UserCreatePayload) {
  const result = await requestJson<ApiResponse<User>>('/api/users', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  return result.data;
}

export async function updateUser(id: number, payload: UserUpdatePayload) {
  const result = await requestJson<ApiResponse<User>>(`/api/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
  return result.data;
}

export async function removeUser(id: number) {
  return requestJson<ApiResponse<null>>(`/api/users/${id}`, {
    method: 'DELETE'
  });
}
