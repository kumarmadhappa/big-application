import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { Layout } from '@/components/Layout';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { BankingLoginPage } from '@/pages/BankingLoginPage';
import { BankingAdminPage } from '@/pages/BankingAdminPage';
import { BankingHolderPage } from '@/pages/BankingHolderPage';
import { BankingLandingPage } from '@/pages/BankingLandingPage';
import { HomePage } from '@/pages/HomePage';
import { LoginPage } from '@/pages/LoginPage';
import { UsersPage } from '@/pages/UsersPage';
import type { ReactNode } from 'react';

function Protected({ children, redirectTo }: { children: ReactNode; redirectTo: string }) {
  const auth = useAuth();
  const location = useLocation();

  if (auth.loading) {
    return <p className="text-slate-400">Loading session...</p>;
  }

  if (!auth.authenticated) {
    return <Navigate to={redirectTo} replace state={{ from: location }} />;
  }

  return <>{children}</>;
}

export default function App() {
  return (
    <AuthProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/banking/login" element={<BankingLoginPage />} />
          <Route
            path="/banking"
            element={
              <Protected redirectTo="/banking/login">
                <BankingLandingPage />
              </Protected>
            }
          />
          <Route
            path="/banking/admin"
            element={
              <Protected redirectTo="/banking/login">
                <BankingAdminPage />
              </Protected>
            }
          />
          <Route
            path="/banking/holder"
            element={
              <Protected redirectTo="/banking/login">
                <BankingHolderPage />
              </Protected>
            }
          />
          <Route
            path="/users"
            element={
              <Protected redirectTo="/login">
                <UsersPage />
              </Protected>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </AuthProvider>
  );
}
