'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Cookies from 'js-cookie';

interface Account {
  id: string;
  ownerName: string;
  balance: number;
}

interface Card {
  id: string;
  accountId: string;
  cardType: 'VIRTUAL' | 'PHYSICAL';
  status: 'ACTIVE' | 'FROZEN' | 'BLOCKED' | 'REPORTED_STOLEN' | 'EXPIRED' | 'REPLACED';
  spendingLimit: number | null;
  cardholderName: string;
  cardNumberMasked: string;
  expiryDate: string;
}

export default function DashboardPage() {
  const router = useRouter();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [cards, setCards] = useState<Card[]>([]);
  const [loading, setLoading] = useState(true);
  const [revealedCards, setRevealedCards] = useState<Record<string, { cardNumber: string; cvv: string }>>({});
  const [revealLoading, setRevealLoading] = useState<Record<string, boolean>>({});

  useEffect(() => {
    const token = Cookies.get('token');
    if (!token) {
      router.push('/');
      return;
    }

    fetchData();
  }, [router]);

  const fetchData = async () => {
    try {
      const [accountsRes, cardsRes] = await Promise.all([
        fetch('/api/accounts', {
          headers: { 'Authorization': `Bearer ${Cookies.get('token')}` },
        }),
        fetch('/api/cards', {
          headers: { 'Authorization': `Bearer ${Cookies.get('token')}` },
        }),
      ]);

      if (accountsRes.ok) {
        const accountsData = await accountsRes.json();
        setAccounts(Array.isArray(accountsData) ? accountsData : []);
      }

      if (cardsRes.ok) {
        const cardsData = await cardsRes.json();
        setCards(Array.isArray(cardsData) ? cardsData : []);
      }
    } catch (error) {
      console.error('Failed to fetch data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRevealCard = async (cardId: string) => {
    if (revealedCards[cardId]) {
      // Hide card
      setRevealedCards({ ...revealedCards, [cardId]: undefined as any });
      return;
    }

    setRevealLoading({ ...revealLoading, [cardId]: true });

    try {
      const response = await fetch(`/api/cards/${cardId}/reveal`, {
        headers: { 'Authorization': `Bearer ${Cookies.get('token')}` },
      });

      if (response.ok) {
        const data = await response.json();
        setRevealedCards({ ...revealedCards, [cardId]: { cardNumber: data.cardNumber, cvv: data.cvv } });
      }
    } catch (error) {
      console.error('Failed to reveal card:', error);
    } finally {
      setRevealLoading({ ...revealLoading, [cardId]: false });
    }
  };

  const handleLogout = () => {
    Cookies.remove('token');
    router.push('/');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">NeoBank Dashboard</h1>
          <button
            onClick={handleLogout}
            className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
          >
            Logout
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Accounts Section */}
        <section className="mb-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Your Accounts</h2>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {accounts.length === 0 ? (
              <div className="col-span-full bg-white rounded-xl shadow p-6 text-center text-gray-500">
                No accounts found. Create an account to get started.
              </div>
            ) : (
              accounts.map((account) => (
                <div
                  key={account.id}
                  className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl shadow-lg p-6 text-white"
                >
                  <p className="text-blue-100 text-sm">Account Balance</p>
                  <p className="text-3xl font-bold mt-2">
                    ${Number(account.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </p>
                  <p className="text-blue-100 text-sm mt-4">{account.ownerName}</p>
                  <p className="text-blue-200 text-xs mt-1">ID: {account.id.slice(0, 8)}...</p>
                </div>
              ))
            )}
          </div>
        </section>

        {/* Cards Section */}
        <section>
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Your Cards</h2>
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {cards.length === 0 ? (
              <div className="col-span-full bg-white rounded-xl shadow p-6 text-center text-gray-500">
                No cards found. Request a card to get started.
              </div>
            ) : (
              cards.map((card) => {
                const isRevealed = revealedCards[card.id];
                return (
                  <div
                    key={card.id}
                    className={`rounded-xl shadow-lg p-6 ${
                      card.status === 'ACTIVE' 
                        ? 'bg-gradient-to-br from-gray-800 to-gray-900 text-white' 
                        : 'bg-gray-200 text-gray-500'
                    }`}
                  >
                    <div className="flex justify-between items-start mb-4">
                      <span className="text-sm font-medium">
                        {card.cardType === 'VIRTUAL' ? 'Virtual Card' : 'Physical Card'}
                      </span>
                      <span className={`text-xs px-2 py-1 rounded ${
                        card.status === 'ACTIVE' ? 'bg-green-500' : 'bg-red-500'
                      }`}>
                        {card.status}
                      </span>
                    </div>

                    <div className="mb-4">
                      <p className="text-xs text-gray-400 mb-1">Card Number</p>
                      {isRevealed ? (
                        <p className="text-lg font-mono tracking-wider">{isRevealed.cardNumber}</p>
                      ) : (
                        <p className="text-lg font-mono tracking-wider">{card.cardNumberMasked}</p>
                      )}
                    </div>

                    <div className="flex justify-between items-end">
                      <div>
                        <p className="text-xs text-gray-400">Cardholder</p>
                        <p className="text-sm font-medium">{card.cardholderName}</p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-400">Expires</p>
                        <p className="text-sm font-medium">{card.expiryDate}</p>
                      </div>
                      {isRevealed && (
                        <div>
                          <p className="text-xs text-gray-400">CVV</p>
                          <p className="text-sm font-medium">{isRevealed.cvv}</p>
                        </div>
                      )}
                    </div>

                    <button
                      onClick={() => handleRevealCard(card.id)}
                      disabled={revealLoading[card.id] || card.status !== 'ACTIVE'}
                      className="mt-4 w-full bg-white/20 hover:bg-white/30 disabled:opacity-50 text-white py-2 px-4 rounded-lg transition-colors text-sm"
                    >
                      {revealLoading[card.id] 
                        ? 'Loading...' 
                        : isRevealed 
                          ? 'Hide Details' 
                          : 'Reveal Details'}
                    </button>
                  </div>
                );
              })
            )}
          </div>
        </section>
      </main>
    </div>
  );
}
