import { useEffect, useMemo, useState } from 'react';
import { createUser, fetchUsers, removeUser, updateUser } from '@/lib/api';
import type { Role, User } from '@/types';

type FormState = {
  id?: number;
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: Role[];
};

const emptyForm = (): FormState => ({
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  enabled: true,
  roles: ['USER']
});

export function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState<FormState>(emptyForm());
  const [saving, setSaving] = useState(false);

  const editing = useMemo(() => Boolean(form.id), [form.id]);

  const loadUsers = async () => {
    setLoading(true);
    setError('');
    try {
      setUsers(await fetchUsers());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadUsers();
  }, []);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        username: form.username,
        email: form.email,
        firstName: form.firstName,
        lastName: form.lastName,
        roles: form.roles,
        ...(form.password ? { password: form.password } : {})
      };

      if (editing && form.id) {
        await updateUser(form.id, { ...payload, enabled: form.enabled });
      } else {
        await createUser({ ...payload, password: form.password || 'StrongPass123!' });
      }
      setForm(emptyForm());
      await loadUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to save user');
    } finally {
      setSaving(false);
    }
  };

  const edit = (user: User) => {
    setForm({
      id: user.id,
      username: user.username,
      email: user.email,
      password: '',
      firstName: user.firstName,
      lastName: user.lastName,
      enabled: user.enabled,
      roles: user.roles
    });
  };

  const del = async (user: User) => {
    if (!confirm(`Delete ${user.username}?`)) {
      return;
    }
    setError('');
    try {
      await removeUser(user.id);
      await loadUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to delete user');
    }
  };

  const toggleRole = (role: Role) => {
    setForm((current) => {
      const exists = current.roles.includes(role);
      return {
        ...current,
        roles: exists ? current.roles.filter((item) => item !== role) : [...current.roles, role]
      };
    });
  };

  return (
    <div className="grid gap-8 lg:grid-cols-[1.3fr_1fr]">
      <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold">User management</h2>
            <p className="text-sm text-slate-400">Create, edit, and delete users through the user-service backend.</p>
          </div>
          <button
            type="button"
            onClick={() => void loadUsers()}
            className="rounded-lg border border-slate-700 px-3 py-2 text-sm text-slate-200 hover:bg-slate-800"
          >
            Refresh
          </button>
        </div>

        {loading ? (
          <p className="text-slate-400">Loading users...</p>
        ) : error ? (
          <p className="rounded-lg bg-red-950/70 px-3 py-2 text-sm text-red-200">{error}</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="text-slate-400">
                <tr>
                  <th className="py-2">Username</th>
                  <th className="py-2">Name</th>
                  <th className="py-2">Email</th>
                  <th className="py-2">Roles</th>
                  <th className="py-2">Status</th>
                  <th className="py-2"></th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id} className="border-t border-slate-800">
                    <td className="py-3">{user.username}</td>
                    <td className="py-3">{user.firstName} {user.lastName}</td>
                    <td className="py-3">{user.email}</td>
                    <td className="py-3">{user.roles.join(', ')}</td>
                    <td className="py-3">{user.enabled ? 'Enabled' : 'Disabled'}</td>
                    <td className="py-3">
                      <div className="flex gap-2">
                        <button className="text-cyan-400" onClick={() => edit(user)}>Edit</button>
                        <button className="text-red-400" onClick={() => void del(user)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <h2 className="text-xl font-semibold">{editing ? 'Edit user' : 'Create user'}</h2>
        <form className="mt-4 space-y-4" onSubmit={submit}>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Username" value={form.username} onChange={(value) => setForm({ ...form, username: value })} />
            <Field label="Email" value={form.email} onChange={(value) => setForm({ ...form, email: value })} />
            <Field label="First name" value={form.firstName} onChange={(value) => setForm({ ...form, firstName: value })} />
            <Field label="Last name" value={form.lastName} onChange={(value) => setForm({ ...form, lastName: value })} />
            <Field
              label={editing ? 'Password (optional)' : 'Password'}
              type="password"
              value={form.password}
              onChange={(value) => setForm({ ...form, password: value })}
            />
          </div>

          <label className="flex items-center gap-2 text-sm text-slate-300">
            <input
              type="checkbox"
              checked={form.enabled}
              onChange={(e) => setForm({ ...form, enabled: e.target.checked })}
            />
            Enabled
          </label>

          <div>
            <p className="text-sm text-slate-300">Roles</p>
            <div className="mt-2 flex gap-4">
              {(['USER', 'ADMIN'] as Role[]).map((role) => (
                <label key={role} className="flex items-center gap-2 text-sm text-slate-300">
                  <input
                    type="checkbox"
                    checked={form.roles.includes(role)}
                    onChange={() => toggleRole(role)}
                  />
                  {role}
                </label>
              ))}
            </div>
          </div>

          {error && <p className="rounded-lg bg-red-950/70 px-3 py-2 text-sm text-red-200">{error}</p>}

          <div className="flex gap-3">
            <button
              type="submit"
              disabled={saving}
              className="rounded-lg bg-cyan-500 px-4 py-2 font-medium text-slate-950 hover:bg-cyan-400 disabled:opacity-60"
            >
              {saving ? 'Saving...' : editing ? 'Update user' : 'Create user'}
            </button>
            <button
              type="button"
              onClick={() => setForm(emptyForm())}
              className="rounded-lg border border-slate-700 px-4 py-2 text-slate-200 hover:bg-slate-800"
            >
              Reset
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

function Field({
  label,
  value,
  onChange,
  type = 'text'
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  type?: string;
}) {
  return (
    <label className="block">
      <span className="text-sm text-slate-300">{label}</span>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-cyan-400"
      />
    </label>
  );
}
