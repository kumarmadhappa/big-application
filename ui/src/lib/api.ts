import type {
  ApiResponse,
  AuthResponse,
  BankingAccount,
  BankingAccountCreatePayload,
  BankingAccountSearchParams,
  BankingAccountUpdatePayload,
  BankingTransactionPayload,
  BankingTransactionResponse,
  LoginPayload,
  SessionResponse,
  User,
  UserCreatePayload,
  UserUpdatePayload
} from '@/types';

async function requestJson<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
  const method = init?.method ?? 'GET';
  const target = typeof input === 'string' ? input : input instanceof URL ? input.toString() : String(input);

  console.info(`[api] ${method} ${target}`);

  const response = await fetch(input, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {})
    },
    credentials: 'include'
  });

  console.info(`[api] ${method} ${target} -> ${response.status}`);

  const text = await response.text();
  let body: any = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = null;
    }
  }
  if (!response.ok) {
    console.info(`[api] ${method} ${target} failed`);
    throw new Error(body?.message ?? text?.trim() ?? response.statusText ?? 'Request failed');
  }

  return body as T;
}

export async function login(payload: LoginPayload) {
  return requestJson<ApiResponse<AuthResponse>>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function bankingLogin(payload: LoginPayload) {
  return requestJson<ApiResponse<AuthResponse>>('/api/banking/auth/login', {
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
  return requestJson<SessionResponse>('/api/session');
}

export async function fetchUsers() {
  const result = await requestJson<ApiResponse<User[]>>('/api/users');
  return result.data;
}

export async function fetchBankingAccounts() {
  const result = await requestJson<ApiResponse<BankingAccount[]>>('/api/banking/accounts/mine');
  return result.data;
}

export async function fetchAdminBankingAccounts() {
  const result = await requestJson<ApiResponse<BankingAccount[]>>('/api/banking/admin/accounts');
  return result.data;
}

export async function searchAdminBankingAccounts(params: BankingAccountSearchParams) {
  const searchParams = new URLSearchParams();
  if (params.name?.trim()) {
    searchParams.set('name', params.name.trim());
  }
  if (params.accountNumber?.trim()) {
    searchParams.set('accountNumber', params.accountNumber.trim());
  }
  if (params.accountId?.trim()) {
    searchParams.set('accountId', params.accountId.trim());
  }
  const queryString = searchParams.toString();
  const result = await requestJson<ApiResponse<BankingAccount[]>>(
    `/api/banking/admin/accounts/search${queryString ? `?${queryString}` : ''}`
  );
  return result.data;
}

export async function createBankingAccount(payload: BankingAccountCreatePayload) {
  const result = await requestJson<ApiResponse<BankingAccount>>('/api/banking/admin/accounts', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  return result.data;
}

export async function updateBankingAccount(id: number, payload: BankingAccountUpdatePayload) {
  const result = await requestJson<ApiResponse<BankingAccount>>(`/api/banking/admin/accounts/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
  return result.data;
}

export async function deleteBankingAccount(id: number) {
  return requestJson<ApiResponse<null>>(`/api/banking/admin/accounts/${id}`, {
    method: 'DELETE'
  });
}

export async function adminDeposit(accountId: number, payload: BankingTransactionPayload) {
  const result = await requestJson<ApiResponse<BankingTransactionResponse>>(`/api/banking/admin/accounts/${accountId}/transactions/deposit`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  return result.data;
}

export async function adminWithdraw(accountId: number, payload: BankingTransactionPayload) {
  const result = await requestJson<ApiResponse<BankingTransactionResponse>>(`/api/banking/admin/accounts/${accountId}/transactions/withdraw`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  return result.data;
}

export async function depositBankingAccount(accountId: number, payload: BankingTransactionPayload) {
  const result = await requestJson<ApiResponse<BankingTransactionResponse>>(`/api/banking/accounts/${accountId}/transactions/deposit`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  return result.data;
}

export async function withdrawBankingAccount(accountId: number, payload: BankingTransactionPayload) {
  const result = await requestJson<ApiResponse<BankingTransactionResponse>>(`/api/banking/accounts/${accountId}/transactions/withdraw`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
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
