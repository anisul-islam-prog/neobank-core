'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { accountsApi, transfersApi } from '@/lib/api';

interface Account {
  id: string;
  ownerName: string;
  balance: number;
  branchId?: string;
}

interface Transfer {
  id: string;
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  currency: string;
  status: string;
}

export default function DashboardPage() {
  const router = useRouter();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [transferForm, setTransferForm] = useState({
    toAccountId: '',
    amount: '',
  });
  const [transferMessage, setTransferMessage] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/');
      return;
    }
    loadAccounts();
  }, []);

  const loadAccounts = async () => {
    try {
      const data = await accountsApi.getAll();
      setAccounts(data);
    } catch (error) {
      console.error('Failed to load accounts:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleTransfer = async (e: React.FormEvent) => {
    e.preventDefault();
    setTransferMessage('');

    try {
      const fromAccount = accounts[0]; // Use first account as sender
      await transfersApi.create({
        fromId: fromAccount.id,
        toId: transferForm.toAccountId,
        amount: parseFloat(transferForm.amount),
      });
      setTransferMessage('✅ Transfer completed successfully!');
      loadAccounts(); // Refresh balances
      setTransferForm({ toAccountId: '', amount: '' });
    } catch (error: any) {
      setTransferMessage('❌ Transfer failed: ' + (error.response?.data?.message || 'Unknown error'));
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    router.push('/');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-blue-50">
        <div className="text-xl text-gray-600">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-blue-50">
      {/* Header */}
      <header className="bg-blue-600 text-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold">🏦 NeoBank Retail</h1>
          <button
            onClick={handleLogout}
            className="px-4 py-2 bg-blue-700 hover:bg-blue-800 rounded-md"
          >
            Logout
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* Account Balances */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Your Accounts</h2>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {accounts.map((account) => (
              <div key={account.id} className="bg-white rounded-lg shadow p-6">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-sm text-gray-500">Savings Account</span>
                  <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                    ACTIVE
                  </span>
                </div>
                <div className="text-3xl font-bold text-gray-900 mb-1">
                  ${account.balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </div>
                <div className="text-sm text-gray-600">{account.ownerName}</div>
                <div className="text-xs text-gray-400 mt-2">ID: {account.id}</div>
              </div>
            ))}
          </div>
        </section>

        {/* Transfer Form */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Make a Transfer</h2>
          <form onSubmit={handleTransfer} className="bg-white rounded-lg shadow p-6 max-w-md">
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Recipient Account ID
                </label>
                <input
                  type="text"
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter account UUID"
                  value={transferForm.toAccountId}
                  onChange={(e) => setTransferForm({ ...transferForm, toAccountId: e.target.value })}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Amount (USD)
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="0.00"
                  value={transferForm.amount}
                  onChange={(e) => setTransferForm({ ...transferForm, amount: e.target.value })}
                />
              </div>
              <button
                type="submit"
                className="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-md"
              >
                Transfer Funds
              </button>
              {transferMessage && (
                <div className={`text-sm ${transferMessage.startsWith('✅') ? 'text-green-600' : 'text-red-600'}`}>
                  {transferMessage}
                </div>
              )}
            </div>
          </form>
        </section>

        {/* Transaction History */}
        <section>
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Recent Transactions</h2>
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                <tr>
                  <td colSpan={4} className="px-6 py-4 text-center text-gray-500">
                    No recent transactions
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  );
}
