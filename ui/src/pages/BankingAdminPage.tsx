import { useMemo, useState, type FormEvent } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import {
  adminDeposit,
  adminWithdraw,
  createBankingAccount,
  deleteBankingAccount,
  searchAdminBankingAccounts,
  updateBankingAccount
} from '@/lib/api';
import type {
  BankingAccount,
  BankingAccountCreatePayload,
  BankingAccountSearchParams,
  BankingAccountUpdatePayload,
  BankingTransactionPayload
} from '@/types';

type CreateForm = BankingAccountCreatePayload;
type SearchForm = Required<BankingAccountSearchParams>;
type UpdateForm = BankingAccountUpdatePayload & { id: number | null };
type TransactionForm = BankingTransactionPayload & { accountNumber: string };
type AdminModule = 'search' | 'create' | 'update' | 'transactions';
type UiMessage = { type: 'error' | 'success'; text: string } | null;

const adminModules: Array<{ id: AdminModule; label: string; description: string }> = [
  { id: 'search', label: 'Search accounts', description: 'Find and manage existing accounts' },
  { id: 'create', label: 'Create account', description: 'Open a new banking account' },
  { id: 'update', label: 'Update account', description: 'Edit the selected account' },
  { id: 'transactions', label: 'Transactions', description: 'Deposit or withdraw funds' }
];

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

const emptySearchForm = (): SearchForm => ({
  name: '',
  accountNumber: '',
  accountId: ''
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

function formatAccountDetails(account: BankingAccount) {
  return `${account.accountNumber} (ID ${account.id}, holder ${account.holderName}, ${account.accountSegment} ${account.accountType}, balance ${account.balance.toFixed(2)})`;
}

export function BankingAdminPage() {
  const auth = useAuth();
  const isAdmin = useMemo(() => auth.user?.roles.includes('ADMIN'), [auth.user]);
  const [accounts, setAccounts] = useState<BankingAccount[]>([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<UiMessage>(null);
  const [saving, setSaving] = useState(false);
  const [searched, setSearched] = useState(false);
  const [activeModule, setActiveModule] = useState<AdminModule>('search');
  const [searchForm, setSearchForm] = useState<SearchForm>(emptySearchForm());
  const [createForm, setCreateForm] = useState<CreateForm>(emptyCreateForm());
  const [updateForm, setUpdateForm] = useState<UpdateForm>(emptyUpdateForm());
  const [transactionForm, setTransactionForm] = useState<TransactionForm>(emptyTransactionForm());

  if (auth.loading) {
    return <p className="text-slate-400">Loading banking session...</p>;
  }

  if (!auth.authenticated) {
    return <Navigate to="/banking/login" replace />;
  }

  if (!isAdmin) {
    return <Navigate to="/banking/holder" replace />;
  }

  function hasSearchCriteria(form: SearchForm) {
    return Boolean(form.name.trim() || form.accountNumber.trim() || form.accountId.trim());
  }

  function showMessage(type: 'error' | 'success', text: string) {
    setMessage({ type, text });
  }

  function clearMessage() {
    setMessage(null);
  }

  async function searchAccounts(form: SearchForm = searchForm, requireCriteria = true, showSearchSuccess = true) {
    if (requireCriteria && !hasSearchCriteria(form)) {
      showMessage('error', 'Enter a name, account number, or account ID to search');
      return false;
    }
    setLoading(true);
    clearMessage();
    setAccounts([]);
    setSearched(true);
    try {
      const results = await searchAdminBankingAccounts(form);
      setAccounts(results);
      if (showSearchSuccess) {
        showMessage('success', `Found ${results.length} account${results.length === 1 ? '' : 's'} matching your search.`);
      }
      return true;
    } catch (err) {
      showMessage('error', err instanceof Error ? err.message : 'Unable to search banking accounts');
      return false;
    } finally {
      setLoading(false);
    }
  }

  async function refreshSearchResults() {
    if (searched && hasSearchCriteria(searchForm)) {
      return searchAccounts(searchForm, false, false);
    }
    return true;
  }

  function clearSearch() {
    setSearchForm(emptySearchForm());
    setAccounts([]);
    setSearched(false);
    clearMessage();
  }

  async function handleSearch(event: FormEvent) {
    event.preventDefault();
    await searchAccounts();
  }

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    clearMessage();
    try {
      const account = await createBankingAccount(createForm);
      setCreateForm(emptyCreateForm());
      const refreshed = await refreshSearchResults();
      if (refreshed) {
        showMessage('success', `The account is created: ${formatAccountDetails(account)}.`);
      }
    } catch (err) {
      showMessage('error', err instanceof Error ? err.message : 'Unable to create account');
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
    clearMessage();
    try {
      const account = await updateBankingAccount(updateForm.id, updateForm);
      setUpdateForm(emptyUpdateForm());
      const refreshed = await refreshSearchResults();
      if (refreshed) {
        showMessage('success', `The account is updated: ${formatAccountDetails(account)}.`);
      }
    } catch (err) {
      showMessage('error', err instanceof Error ? err.message : 'Unable to update account');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(accountId: number) {
    if (!confirm(`Delete account ${accountId}?`)) {
      return;
    }
    clearMessage();
    try {
      await deleteBankingAccount(accountId);
      const refreshed = await refreshSearchResults();
      if (refreshed) {
        showMessage('success', `The account ID ${accountId} is deleted.`);
      }
    } catch (err) {
      showMessage('error', err instanceof Error ? err.message : 'Unable to delete account');
    }
  }

  function selectAccountForUpdate(account: BankingAccount) {
    setUpdateForm({
      id: account.id,
      displayName: account.holderName,
      accountSegment: account.accountSegment,
      accountType: account.accountType,
      creditLimit: account.creditLimit ?? 0
    });
    setActiveModule('update');
  }

  function selectAccountForTransaction(account: BankingAccount) {
    setTransactionForm({ accountNumber: account.accountNumber, amount: 0 });
    setActiveModule('transactions');
  }

  async function handleTransaction(action: 'DEPOSIT' | 'WITHDRAWAL') {
    const selectedAccount = accounts.find((account) => account.accountNumber === transactionForm.accountNumber);
    if (!selectedAccount) {
      showMessage('error', 'Select an account from the search results before transacting');
      return;
    }
    setSaving(true);
    clearMessage();
    try {
      const payload = { amount: Number(transactionForm.amount) };
      const transaction = action === 'DEPOSIT'
        ? await adminDeposit(selectedAccount.id, payload)
        : await adminWithdraw(selectedAccount.id, payload);
      setTransactionForm(emptyTransactionForm());
      const refreshed = await refreshSearchResults();
      if (refreshed) {
        if (action === 'DEPOSIT') {
          showMessage('success', `Deposit completed for ${transaction.accountNumber}: amount ${transaction.amount.toFixed(2)}, new balance ${transaction.balanceAfter.toFixed(2)}.`);
        } else {
          showMessage('success', `Withdrawal completed for ${transaction.accountNumber}: amount ${transaction.amount.toFixed(2)}, new balance ${transaction.balanceAfter.toFixed(2)}.`);
        }
      }
    } catch (err) {
      showMessage('error', err instanceof Error ? err.message : 'Unable to process transaction');
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
            <p className="text-sm text-slate-400">Choose an admin module to search, create, update, or transact on accounts.</p>
          </div>
        </div>
        <nav className="mt-5 grid gap-3 md:grid-cols-2 xl:grid-cols-4" aria-label="Banking admin modules">
          {adminModules.map((module) => {
            const active = activeModule === module.id;
            return (
              <button
                key={module.id}
                type="button"
                onClick={() => {
                  setActiveModule(module.id);
                  clearMessage();
                }}
                className={`rounded-xl border p-4 text-left transition ${
                  active
                    ? 'border-cyan-400 bg-cyan-950/40 text-cyan-100'
                    : 'border-slate-800 bg-slate-950 text-slate-200 hover:border-slate-600 hover:bg-slate-800'
                }`}
              >
                <span className="block font-medium">{module.label}</span>
                <span className="mt-1 block text-sm text-slate-400">{module.description}</span>
              </button>
            );
          })}
        </nav>
        {message && (
          <p
            className={`mt-4 rounded-lg px-3 py-2 text-sm ${
              message.type === 'error'
                ? 'bg-red-950/70 text-red-200'
                : 'bg-emerald-950/70 text-emerald-200'
            }`}
          >
            {message.text}
          </p>
        )}
      </section>

      {activeModule === 'search' && (
        <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
          <div>
            <h3 className="text-lg font-semibold">Search accounts</h3>
            <p className="mt-1 text-sm text-slate-400">Search by name, account number, or account ID before managing accounts.</p>
          </div>
        <form onSubmit={handleSearch} className="mt-4 grid gap-4 lg:grid-cols-[1fr_1fr_1fr_auto] lg:items-end">
          <Field label="Name" value={searchForm.name} onChange={(value) => setSearchForm({ ...searchForm, name: value })} />
          <Field label="Account Number" value={searchForm.accountNumber} onChange={(value) => setSearchForm({ ...searchForm, accountNumber: value })} />
          <Field label="Account ID" value={searchForm.accountId} type="number" onChange={(value) => setSearchForm({ ...searchForm, accountId: value })} />
          <div className="flex gap-3">
            <button disabled={loading} className="rounded-lg bg-cyan-500 px-4 py-2 font-medium text-slate-950 hover:bg-cyan-400 disabled:opacity-60">
              {loading ? 'Searching...' : 'Search'}
            </button>
            <button type="button" onClick={clearSearch} className="rounded-lg border border-slate-700 px-4 py-2 text-slate-200 hover:bg-slate-800">
              Clear
            </button>
          </div>
        </form>
        {loading ? (
          <p className="mt-4 text-slate-400">Searching accounts...</p>
        ) : !searched ? (
          <p className="mt-4 text-slate-400">Enter search criteria to find accounts.</p>
        ) : accounts.length === 0 ? (
          <p className="mt-4 text-slate-400">No accounts matched your search.</p>
        ) : (
          <div className="mt-4 overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="text-slate-400">
                <tr>
                  <th className="py-2">ID</th>
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
                    <td className="py-3">{account.id}</td>
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
                          onClick={() => selectAccountForUpdate(account)}
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
                          onClick={() => selectAccountForTransaction(account)}
                        >
                          Deposit
                        </button>
                        <button
                          type="button"
                          className="text-amber-400"
                          onClick={() => selectAccountForTransaction(account)}
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
      )}

      {activeModule === 'create' && (
        <form onSubmit={handleCreate} className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
          <h3 className="text-lg font-semibold">Create account</h3>
          <p className="mt-1 text-sm text-slate-400">Create a person or company holder and open a savings or credit account.</p>
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
      )}

      {activeModule === 'update' && (
        <form onSubmit={handleUpdate} className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
          <h3 className="text-lg font-semibold">Update account</h3>
          <p className="mt-1 text-sm text-slate-400">Select Edit from the search module to load account values here.</p>
          {updateForm.id == null && (
            <div className="mt-4 rounded-lg border border-slate-800 bg-slate-950 p-4 text-sm text-slate-400">
              No account selected. Search for an account, then choose Edit from the results.
              <button type="button" onClick={() => setActiveModule('search')} className="ml-3 text-cyan-400 hover:text-cyan-300">
                Go to search
              </button>
            </div>
          )}
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
      )}

      {activeModule === 'transactions' && (
      <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <h3 className="text-lg font-semibold">Admin transaction</h3>
        <p className="mt-1 text-sm text-slate-400">Choose an account from search results, then deposit or withdraw.</p>
        {!transactionForm.accountNumber && (
          <div className="mt-4 rounded-lg border border-slate-800 bg-slate-950 p-4 text-sm text-slate-400">
            No account selected. Search for an account, then choose Deposit or Withdraw from the results.
            <button type="button" onClick={() => setActiveModule('search')} className="ml-3 text-cyan-400 hover:text-cyan-300">
              Go to search
            </button>
          </div>
        )}
        <div className="mt-4 flex flex-wrap items-end gap-4">
          <Field label="Account Number" value={transactionForm.accountNumber} onChange={(value) => setTransactionForm({ ...transactionForm, accountNumber: value })} />
          <NumberField label="Amount" value={transactionForm.amount} onChange={(value) => setTransactionForm({ ...transactionForm, amount: value })} />
          <button type="button" disabled={saving || !transactionForm.accountNumber} onClick={() => void handleTransaction('DEPOSIT')} className="rounded-lg bg-emerald-500 px-4 py-2 font-medium text-slate-950 hover:bg-emerald-400 disabled:opacity-60">Deposit</button>
          <button type="button" disabled={saving || !transactionForm.accountNumber} onClick={() => void handleTransaction('WITHDRAWAL')} className="rounded-lg bg-amber-500 px-4 py-2 font-medium text-slate-950 hover:bg-amber-400 disabled:opacity-60">Withdraw</button>
        </div>
      </section>
      )}
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
