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

export interface BankingAccount {
  id: number;
  accountNumber: string;
  accountSegment: 'CONSUMER' | 'COMMERCIAL';
  accountType: 'SAVINGS' | 'CREDIT';
  balance: number;
  creditLimit: number | null;
  holderType: 'PERSON' | 'COMPANY';
  holderName: string;
  holderUsername: string;
}

export interface BankingAccountCreatePayload {
  holderType: 'PERSON' | 'COMPANY';
  displayName: string;
  username: string;
  email: string;
  password: string;
  accountSegment: 'CONSUMER' | 'COMMERCIAL';
  accountType: 'SAVINGS' | 'CREDIT';
  initialBalance: number;
  creditLimit?: number | null;
}

export interface BankingAccountSearchParams {
  name?: string;
  accountNumber?: string;
  accountId?: string;
}

export interface BankingAccountUpdatePayload {
  displayName: string;
  accountSegment: 'CONSUMER' | 'COMMERCIAL';
  accountType: 'SAVINGS' | 'CREDIT';
  creditLimit?: number | null;
}

export interface BankingTransactionPayload {
  amount: number;
}

export interface BankingTransactionResponse {
  transactionId: number;
  accountId: number;
  accountNumber: string;
  transactionType: 'DEPOSIT' | 'WITHDRAWAL';
  amount: number;
  balanceBefore: number;
  balanceAfter: number;
  createdAt: string;
}

export interface SessionResponse {
  authenticated: boolean;
  user?: AuthUser;
}
