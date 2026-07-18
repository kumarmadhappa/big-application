import { Navigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

export function BankingLandingPage() {
  const auth = useAuth();

  if (auth.loading) {
    return <p className="text-slate-400">Loading banking session...</p>;
  }

  if (!auth.authenticated) {
    return <Navigate to="/banking/login" replace />;
  }

  const isAdmin = auth.user?.roles.includes('ADMIN');
  return <Navigate to={isAdmin ? '/banking/admin' : '/banking/holder'} replace />;
}
