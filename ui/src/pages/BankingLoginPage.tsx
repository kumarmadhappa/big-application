import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { LoginForm } from '@/components/LoginForm';

export function BankingLoginPage() {
  const auth = useAuth();
  const navigate = useNavigate();

  return (
    <LoginForm
      title="Sign in to Banking System"
      description="Enter your banking credentials to open the banking application."
      submitLabel="Sign in"
      onSubmit={async (payload) => {
        await auth.bankingLogin(payload);
        navigate('/banking');
      }}
    />
  );
}
