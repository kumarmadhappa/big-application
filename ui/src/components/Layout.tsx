import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import type { ReactNode } from 'react';

export function Layout({ children }: { children: ReactNode }) {
  const auth = useAuth();
  const userLabel = auth.user?.username ? `Signed in as ${auth.user.username}` : 'Logged in';

  return (
    <div className="min-h-full">
      <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <Link to="/users" className="text-lg font-semibold text-white">
            Big Application UI
          </Link>
          <nav className="flex items-center gap-4 text-sm">
            {auth.authenticated && (
              <>
                <NavLink to="/users" className={({ isActive }) => isActive ? 'text-cyan-400' : 'text-slate-300'}>
                  Users
                </NavLink>
                <span className="max-w-[220px] truncate rounded-full border border-slate-700 px-3 py-1.5 text-slate-200">
                  {userLabel}
                </span>
                <button
                  type="button"
                  onClick={() => void auth.logout()}
                  className="rounded-md border border-slate-700 px-3 py-1.5 text-slate-200 hover:bg-slate-800"
                >
                  Logout
                </button>
              </>
            )}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-6 py-8">{children}</main>
    </div>
  );
}
