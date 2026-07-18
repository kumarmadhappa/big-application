import { useState, type FormEvent } from 'react';
import type { LoginPayload } from '@/types';

type Props = {
  title: string;
  description: string;
  submitLabel: string;
  onSubmit: (payload: LoginPayload) => Promise<void>;
};

export function LoginForm({ title, description, submitLabel, onSubmit }: Props) {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await onSubmit({ login, password });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-md rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl">
      <h1 className="text-2xl font-semibold">{title}</h1>
      <p className="mt-2 text-sm text-slate-400">{description}</p>

      <form className="mt-6 space-y-4" onSubmit={submit}>
        <label className="block">
          <span className="text-sm text-slate-300">Username or email</span>
          <input
            className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-cyan-400"
            value={login}
            onChange={(e) => setLogin(e.target.value)}
          />
        </label>
        <label className="block">
          <span className="text-sm text-slate-300">Password</span>
          <input
            type="password"
            className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-cyan-400"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
        {error && <p className="rounded-lg bg-red-950/70 px-3 py-2 text-sm text-red-200">{error}</p>}
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-cyan-500 px-4 py-2 font-medium text-slate-950 hover:bg-cyan-400 disabled:opacity-60"
        >
          {submitting ? 'Signing in...' : submitLabel}
        </button>
      </form>
    </div>
  );
}
