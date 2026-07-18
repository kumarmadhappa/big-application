import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import {
  adminDeposit,
  adminWithdraw,
  createBankingAccount,
  deleteBankingAccount,
  fetchAdminBankingAccounts,
  updateBankingAccount
} from '@/lib/api';
import type {
  BankingAccount,
  BankingAccountCreatePayload,
  BankingAccountUpdatePayload,
  BankingTransactionPayload
} from '@/types';

type CreateForm = BankingAccountCreatePayload;
type UpdateForm = BankingAccountUpdatePayload & { id: number | null };
type TransactionForm = BankingTransactionPayload & { accountNumber: string };

const emptyCreateForm = (): CreateForm => ({
  holderType: 'PERSON',
  displayName: '',
  username: '',
  email: '',
  password: '',
  accountSegment: 'CONSUMER',
  accountType: 'SAVINGS',
  initialBalance: 0,
  creditLimit: 0
});

const emptyUpdateForm = (): UpdateForm => ({
  id: null,
  displayName: '',
  accountSegment: 'CONSUMER',
  accountType: 'SAVINGS',
  creditLimit: 0
});

const emptyTransactionForm = (): TransactionForm => ({
  accountNumber: '',
  amount: 0
});

export function BankingAdminPage() {
  const auth = useAuth();
  const isAdmin = useMemo(() => auth.user?.roles.includes('ADMIN'), [auth.user]);
  const [accounts, setAccounts] = useState<BankingAccount[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [createForm, setCreateForm] = useState<CreateForm>(emptyCreateForm());
  const [updateForm, setUpdateForm] = useState<UpdateForm>(emptyUpdateForm());
  const [transactionForm, setTransactionForm] = useState<TransactionForm>(emptyTransactionForm());

  useEffect(() => {
    void loadAccounts();
  }, []);

  if (auth.loading) {
    return <p className="text-slate-400">Loading banking session...</p>;
  }

  if (!auth.authenticated) {
    return <Navigate to="/banking/login" replace />;
  }

  if (!isAdmin) {
    return <Navigate to="/banking/holder" replace />;
  }

  async function loadAccounts() {
    setLoading(true);
    setError('');
    try {
      setAccounts(await fetchAdminBankingAccounts());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load banking accounts');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createBankingAccount(createForm);
      setCreateForm(emptyCreateForm());
      await loadAccounts();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to create account');
    } finally {
      setSaving(false);
    }
  }

  async function handleUpdate(event: FormEvent) {
    event.preventDefault();
    if (updateForm.id == null) {
      return;
    }
    setSaving(true);
    setError('');
    try {
      await updateBankingAccount(updateForm.id, updateForm);
      setUpdateForm(emptyUpdateForm());
      await loadAccounts();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to update account');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(accountId: number) {
    if (!confirm(`Delete account ${accountId}?`)) {
      return;
    }
    setError('');
    try {
      await deleteBankingAccount(accountId);
      await loadAccounts();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to delete account');
    }
  }

  async function handleTransaction(action: 'DEPOSIT' | 'WITHDRAWAL') {
    const selectedAccount = accounts.find((account) => account.accountNumber === transactionForm.accountNumber);
    if (!selectedAccount) {
      setError('Select a valid account number');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const payload = { amount: Number(transactionForm.amount) };
      if (action === 'DEPOSIT') {
        await adminDeposit(selectedAccount.id, payload);
      } else {
        await adminWithdraw(selectedAccount.id, payload);
      }
      setTransactionForm(emptyTransactionForm());
      await loadAccounts();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to process transaction');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-8">
      <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-semibold">Banking Admin</h2>
            <p className="text-sm text-slate-400">Create, update, delete, and transact on any account.</p>
          </div>
          <button
            type="button"
            onClick={() => void loadAccounts()}
            className="rounded-lg border border-slate-700 px-3 py-2 text-sm text-slate-200 hover:bg-slate-800"
          >
            Refresh
          </button>
        </div>
        {error && <p className="mt-4 rounded-lg bg-red-950/70 px-3 py-2 text-sm text-red-200">{error}</p>}
        {loading ? (
          <p className="mt-4 text-slate-400">Loading accounts...</p>
        ) : (
          <div className="mt-4 overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="text-slate-400">
                <tr>
                  <th className="py-2">Account</th>
                  <th className="py-2">Type</th>
                  <th className="py-2">Balance</th>
                  <th className="py-2">Holder</th>
                  <th className="py-2"></th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((account) => (
                  <tr key={account.id} className="border-t border-slate-800">
                    <td className="py-3">{account.accountNumber}</td>
                    <td className="py-3">{account.accountSegment} / {account.accountType}</td>
                    <td className="py-3">
                      {account.balance.toFixed(2)}
                      {account.accountType === 'CREDIT' && account.creditLimit != null ? ` (limit ${account.creditLimit.toFixed(2)})` : ''}
                    </td>
                    <td className="py-3">{account.holderName} ({account.holderUsername})</td>
                    <td className="py-3">
                      <div className="flex flex-wrap gap-2">
                        <button
                          type="button"
                          className="text-cyan-400"
                          onClick={() => setUpdateForm({
                            id: account.id,
                            displayName: account.holderName,
                            accountSegment: account.accountSegment,
                            accountType: account.accountType,
                            creditLimit: account.creditLimit ?? 0
                          })}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className="text-red-400"
                          onClick={() => void handleDelete(account.id)}
                        >
                          Delete
                        </button>
                        <button
                          type="button"
                          className="text-emerald-400"
                          onClick={() => setTransactionForm({ accountNumber: account.accountNumber, amount: 0 })}
                        >
                          Deposit
                        </button>
                        <button
                          type="button"
                          className="text-amber-400"
                          onClick={() => setTransactionForm({ accountNumber: account.accountNumber, amount: 0 })}
                        >
                          Withdraw
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="grid gap-6 lg:grid-cols-3">
        <form onSubmit={handleCreate} className="rounded-2xl border border-slate-800 bg-slate-900 p-6 lg:col-span-2">
          <h3 className="text-lg font-semibold">Create account</h3>
          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            <Field label="Display name" value={createForm.displayName} onChange={(value) => setCreateForm({ ...createForm, displayName: value })} />
            <Field label="Username" value={createForm.username} onChange={(value) => setCreateForm({ ...createForm, username: value })} />
            <Field label="Email" value={createForm.email} onChange={(value) => setCreateForm({ ...createForm, email: value })} />
            <Field label="Password" value={createForm.password} type="password" onChange={(value) => setCreateForm({ ...createForm, password: value })} />
            <NumberField label="Initial balance" value={createForm.initialBalance} onChange={(value) => setCreateForm({ ...createForm, initialBalance: value })} />
            <NumberField label="Credit limit" value={createForm.creditLimit ?? 0} onChange={(value) => setCreateForm({ ...createForm, creditLimit: value })} />
            <SelectField label="Holder type" value={createForm.holderType} onChange={(value) => setCreateForm({ ...createForm, holderType: value as CreateForm['holderType'] })} options={['PERSON', 'COMPANY']} />
            <SelectField label="Account segment" value={createForm.accountSegment} onChange={(value) => setCreateForm({ ...createForm, accountSegment: value as CreateForm['accountSegment'] })} options={['CONSUMER', 'COMMERCIAL']} />
            <SelectField label="Account type" value={createForm.accountType} onChange={(value) => setCreateForm({ ...createForm, accountType: value as CreateForm['accountType'] })} options={['SAVINGS', 'CREDIT']} />
          </div>
          <div className="mt-4 flex gap-3">
            <button disabled={saving} className="rounded-lg bg-cyan-500 px-4 py-2 font-medium text-slate-950 hover:bg-cyan-400 disabled:opacity-60">
              {saving ? 'Saving...' : 'Create account'}
            </button>
            <button type="button" onClick={() => setCreateForm(emptyCreateForm())} className="rounded-lg border border-slate-700 px-4 py-2 text-slate-200 hover:bg-slate-800">
              Reset
            </button>
          </div>
        </form>

        <form onSubmit={handleUpdate} className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
          <h3 className="text-lg font-semibold">Update account</h3>
          <p className="mt-1 text-sm text-slate-400">Select Edit from a row to load values here.</p>
          <div className="mt-4 space-y-4">
            <Field label="Display name" value={updateForm.displayName} onChange={(value) => setUpdateForm({ ...updateForm, displayName: value })} />
            <NumberField label="Credit limit" value={updateForm.creditLimit ?? 0} onChange={(value) => setUpdateForm({ ...updateForm, creditLimit: value })} />
            <SelectField label="Account segment" value={updateForm.accountSegment} onChange={(value) => setUpdateForm({ ...updateForm, accountSegment: value as UpdateForm['accountSegment'] })} options={['CONSUMER', 'COMMERCIAL']} />
            <SelectField label="Account type" value={updateForm.accountType} onChange={(value) => setUpdateForm({ ...updateForm, accountType: value as UpdateForm['accountType'] })} options={['SAVINGS', 'CREDIT']} />
          </div>
          <div className="mt-4 flex gap-3">
            <button disabled={saving || updateForm.id == null} className="rounded-lg bg-cyan-500 px-4 py-2 font-medium text-slate-950 hover:bg-cyan-400 disabled:opacity-60">
              {saving ? 'Saving...' : 'Update account'}
            </button>
            <button type="button" onClick={() => setUpdateForm(emptyUpdateForm())} className="rounded-lg border border-slate-700 px-4 py-2 text-slate-200 hover:bg-slate-800">
              Clear
            </button>
          </div>
        </form>
      </section>

      <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <h3 className="text-lg font-semibold">Admin transaction</h3>
        <p className="mt-1 text-sm text-slate-400">Choose any account from the table to deposit or withdraw.</p>
        <div className="mt-4 flex flex-wrap items-end gap-4">
          <Field label="Account Number" value={transactionForm.accountNumber} onChange={(value) => setTransactionForm({ ...transactionForm, accountNumber: value })} />
          <NumberField label="Amount" value={transactionForm.amount} onChange={(value) => setTransactionForm({ ...transactionForm, amount: value })} />
          <button type="button" disabled={saving || !transactionForm.accountNumber} onClick={() => void handleTransaction('DEPOSIT')} className="rounded-lg bg-emerald-500 px-4 py-2 font-medium text-slate-950 hover:bg-emerald-400 disabled:opacity-60">Deposit</button>
          <button type="button" disabled={saving || !transactionForm.accountNumber} onClick={() => void handleTransaction('WITHDRAWAL')} className="rounded-lg bg-amber-500 px-4 py-2 font-medium text-slate-950 hover:bg-amber-400 disabled:opacity-60">Withdraw</button>
        </div>
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
      <input className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-cyan-400" value={value} type={type} onChange={(e) => onChange(e.target.value)} />
    </label>
  );
}

function NumberField({
  label,
  value,
  onChange
}: {
  label: string;
  value: number;
  onChange: (value: number) => void;
}) {
  return (
    <label className="block">
      <span className="text-sm text-slate-300">{label}</span>
      <input className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-cyan-400" type="number" step="0.01" value={value} onChange={(e) => onChange(Number(e.target.value))} />
    </label>
  );
}

function SelectField({
  label,
  value,
  onChange,
  options
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: string[];
}) {
  return (
    <label className="block">
      <span className="text-sm text-slate-300">{label}</span>
      <select className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-cyan-400" value={value} onChange={(e) => onChange(e.target.value)}>
        {options.map((option) => (
          <option key={option} value={option}>{option}</option>
        ))}
      </select>
    </label>
  );
}
