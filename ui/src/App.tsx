import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { Layout } from '@/components/Layout';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { LoginPage } from '@/pages/LoginPage';
import { UsersPage } from '@/pages/UsersPage';
import type { ReactNode } from 'react';

function Protected({ children }: { children: ReactNode }) {
  const auth = useAuth();
  const location = useLocation();

  if (auth.loading) {
    return <p className="text-slate-400">Loading session...</p>;
  }

  if (!auth.authenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <>{children}</>;
}

export default function App() {
  return (
    <AuthProvider>
      <Layout>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/users"
            element={
              <Protected>
                <UsersPage />
              </Protected>
            }
          />
          <Route path="*" element={<Navigate to="/users" replace />} />
        </Routes>
      </Layout>
    </AuthProvider>
  );
}
