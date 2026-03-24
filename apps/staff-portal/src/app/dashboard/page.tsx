'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { loansApi, onboardingApi } from '@/lib/api';

interface Loan {
  id: string;
  accountId: string;
  principal: number;
  termMonths: number;
  interestRate: number;
  monthlyPayment: number;
  status: string;
  appliedAt: string;
}

interface User {
  id: string;
  username: string;
  email: string;
  status: string;
  roles: string[];
}

export default function StaffDashboardPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<'loans' | 'kyc' | 'approvals'>('loans');
  const [loans, setLoans] = useState<Loan[]>([]);
  const [pendingUsers, setPendingUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/');
      return;
    }
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      if (activeTab === 'loans') {
        const pendingLoans = await loansApi.getPending();
        setLoans(pendingLoans);
      } else {
        const users = await onboardingApi.searchUsers('');
        const pending = users.filter((u: User) => u.status === 'PENDING');
        setPendingUsers(pending);
      }
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleApproveLoan = async (loanId: string) => {
    try {
      await loansApi.approve(loanId);
      setMessage('✅ Loan approved successfully!');
      loadData();
    } catch (error: any) {
      setMessage('❌ Loan approval failed: ' + (error.response?.data?.message || 'Unknown error'));
    }
  };

  const handleApproveUser = async (userId: string) => {
    try {
      await onboardingApi.approveUser(userId);
      setMessage('✅ User approved successfully!');
      loadData();
    } catch (error: any) {
      setMessage('❌ User approval failed: ' + (error.response?.data?.message || 'Unknown error'));
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    router.push('/');
  };

  return (
    <div className="min-h-screen bg-purple-50">
      {/* Header */}
      <header className="bg-purple-600 text-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold">👨‍💼 NeoBank Staff Portal</h1>
          <button
            onClick={handleLogout}
            className="px-4 py-2 bg-purple-700 hover:bg-purple-800 rounded-md"
          >
            Logout
          </button>
        </div>
      </header>

      {/* Tabs */}
      <div className="max-w-7xl mx-auto px-4 py-4">
        <div className="flex space-x-4 border-b border-gray-200">
          <button
            onClick={() => setActiveTab('loans')}
            className={`px-4 py-2 font-medium ${
              activeTab === 'loans'
                ? 'border-b-2 border-purple-600 text-purple-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            Loan Approvals
          </button>
          <button
            onClick={() => setActiveTab('kyc')}
            className={`px-4 py-2 font-medium ${
              activeTab === 'kyc'
                ? 'border-b-2 border-purple-600 text-purple-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            KYC Approvals
          </button>
          <button
            onClick={() => router.push('/dashboard/approvals')}
            className={`px-4 py-2 font-medium ${
              activeTab === 'approvals'
                ? 'border-b-2 border-purple-600 text-purple-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            🔒 Maker-Checker Queue
          </button>
        </div>
      </div>

      <main className="max-w-7xl mx-auto px-4 py-8">
        {message && (
          <div className={`mb-4 p-4 rounded-md ${message.startsWith('✅') ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {message}
          </div>
        )}

        {loading ? (
          <div className="text-center text-gray-600">Loading...</div>
        ) : activeTab === 'loans' ? (
          /* Loan Approvals Tab */
          <section>
            <h2 className="text-2xl font-bold text-gray-800 mb-4">Pending Loan Applications</h2>
            <div className="bg-white rounded-lg shadow overflow-hidden">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Account ID</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Principal</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Term</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Interest</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Monthly Payment</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Applied</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {loans.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="px-6 py-4 text-center text-gray-500">
                        No pending loan applications
                      </td>
                    </tr>
                  ) : (
                    loans.map((loan) => (
                      <tr key={loan.id}>
                        <td className="px-6 py-4 text-sm text-gray-900">{loan.accountId}</td>
                        <td className="px-6 py-4 text-sm text-gray-900">
                          ${loan.principal.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-900">{loan.termMonths} months</td>
                        <td className="px-6 py-4 text-sm text-gray-900">{(loan.interestRate * 100).toFixed(2)}%</td>
                        <td className="px-6 py-4 text-sm text-gray-900">
                          ${loan.monthlyPayment.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-500">
                          {new Date(loan.appliedAt).toLocaleDateString()}
                        </td>
                        <td className="px-6 py-4 text-sm">
                          <button
                            onClick={() => handleApproveLoan(loan.id)}
                            className="px-3 py-1 bg-purple-600 hover:bg-purple-700 text-white rounded-md text-xs"
                          >
                            Approve
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>
        ) : (
          /* KYC Approvals Tab */
          <section>
            <h2 className="text-2xl font-bold text-gray-800 mb-4">Pending User Approvals (KYC)</h2>
            <div className="bg-white rounded-lg shadow overflow-hidden">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Username</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Roles</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {pendingUsers.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-4 text-center text-gray-500">
                        No pending user approvals
                      </td>
                    </tr>
                  ) : (
                    pendingUsers.map((user) => (
                      <tr key={user.id}>
                        <td className="px-6 py-4 text-sm text-gray-900">{user.username}</td>
                        <td className="px-6 py-4 text-sm text-gray-900">{user.email}</td>
                        <td className="px-6 py-4 text-sm text-gray-500">
                          {user.roles.map((r) => r.replace('ROLE_', '')).join(', ')}
                        </td>
                        <td className="px-6 py-4 text-sm">
                          <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded text-xs">
                            {user.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-sm">
                          <button
                            onClick={() => handleApproveUser(user.id)}
                            className="px-3 py-1 bg-purple-600 hover:bg-purple-700 text-white rounded-md text-xs"
                          >
                            Approve
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>
        )}
      </main>
    </div>
  );
}
