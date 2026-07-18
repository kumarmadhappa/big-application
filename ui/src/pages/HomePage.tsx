import { Link } from 'react-router-dom';

export function HomePage() {
  return (
    <div className="mx-auto grid max-w-5xl gap-6 md:grid-cols-2">
      <Link
        to="/users"
        className="rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-lg transition hover:border-cyan-400 hover:bg-slate-800"
      >
        <h1 className="text-2xl font-semibold text-white">User Service</h1>
        <p className="mt-3 text-sm leading-6 text-slate-400">
          Manage users with registration, login, and CRUD operations.
        </p>
      </Link>

      <Link
        to="/banking"
        className="rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-lg transition hover:border-cyan-400 hover:bg-slate-800"
      >
        <h1 className="text-2xl font-semibold text-white">Banking System</h1>
        <p className="mt-3 text-sm leading-6 text-slate-400">
          Access banking login and account overview for the banking service.
        </p>
      </Link>
    </div>
  );
}
