import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { fetchSession, login as loginRequest, logout as logoutRequest } from '@/lib/api';
import type { AuthResponse, LoginPayload } from '@/types';
import type { ReactNode } from 'react';

type AuthContextValue = {
  authenticated: boolean;
  loading: boolean;
  user?: AuthResponse['user'];
  login: (payload: LoginPayload) => Promise<void>;
  logout: () => Promise<void>;
  refreshSession: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState<AuthResponse['user']>();

  const refreshSession = useCallback(async () => {
    setLoading(true);
    try {
      const session = await fetchSession();
      setAuthenticated(session.authenticated);
      if (!session.authenticated) {
        setUser(undefined);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refreshSession();
  }, []);

  const login = useCallback(async (payload: LoginPayload) => {
    const response = await loginRequest(payload);
    setAuthenticated(true);
    setUser(response.data.user);
  }, []);

  const logout = useCallback(async () => {
    await logoutRequest();
    setAuthenticated(false);
    setUser(undefined);
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    authenticated,
    loading,
    user,
    login,
    logout,
    refreshSession
  }), [authenticated, loading, user, login, logout, refreshSession]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
