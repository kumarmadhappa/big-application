import { useEffect, useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { depositBankingAccount, fetchBankingAccounts, withdrawBankingAccount } from '@/lib/api';
import type { BankingAccount } from '@/types';

type TransactionForm = {
  accountNumber: string;
  amount: number;
};

const emptyTransactionForm = (): TransactionForm => ({
  accountNumber: '',
  amount: 0
});

export function BankingHolderPage() {
  const auth = useAuth();
  const isAdmin = useMemo(() => auth.user?.roles.includes('ADMIN'), [auth.user]);
  const [accounts, setAccounts] = useState<BankingAccount[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
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

  if (isAdmin) {
    return <Navigate to="/banking/admin" replace />;
  }

  async function loadAccounts() {
    setLoading(true);
    setError('');
    try {
      setAccounts(await fetchBankingAccounts());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load banking accounts');
    } finally {
      setLoading(false);
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
        await depositBankingAccount(selectedAccount.id, payload);
      } else {
        await withdrawBankingAccount(selectedAccount.id, payload);
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
    <div className="space-y-6">
      <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-semibold">Banking Holder</h2>
            <p className="text-sm text-slate-400">Manage your own accounts and run deposits or withdrawals.</p>
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
                    <td className="py-3">
                      <div className="flex flex-wrap gap-2">
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

      <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <h3 className="text-lg font-semibold">Self-service transaction</h3>
        <p className="mt-1 text-sm text-slate-400">Only your own accounts are listed here.</p>
        <div className="mt-4 flex flex-wrap items-end gap-4">
          <Field label="Account Number" value={transactionForm.accountNumber} onChange={(value) => setTransactionForm({ ...transactionForm, accountNumber: value })} />
          <NumberField label="Amount" value={transactionForm.amount} onChange={(value) => setTransactionForm({ ...transactionForm, amount: value })} />
          <button type="button" disabled={saving || !transactionForm.accountNumber} onClick={() => void handleTransaction('DEPOSIT')} className="rounded-lg bg-emerald-500 px-4 py-2 font-medium text-slate-950 hover:bg-emerald-400 disabled:opacity-60">
            {saving ? 'Processing...' : 'Deposit'}
          </button>
          <button type="button" disabled={saving || !transactionForm.accountNumber} onClick={() => void handleTransaction('WITHDRAWAL')} className="rounded-lg bg-amber-500 px-4 py-2 font-medium text-slate-950 hover:bg-amber-400 disabled:opacity-60">
            {saving ? 'Processing...' : 'Withdraw'}
          </button>
        </div>
      </section>
    </div>
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

function Field({
  label,
  value,
  onChange
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <label className="block">
      <span className="text-sm text-slate-300">{label}</span>
      <input
        className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-cyan-400"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </label>
  );
}
