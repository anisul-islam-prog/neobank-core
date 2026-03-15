'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Cookies from 'js-cookie';

interface DocToken {
  tokenId: string;
  tokenPreview: string;
  description: string;
  createdByUsername: string;
  createdAt: string;
  expiresAt: string;
  useCount: number;
  lastUsedAt: string | null;
}

interface TokenGenerationResponse {
  tokenId: string;
  token: string;
  description: string;
  createdAt: string;
  expiresAt: string;
  accessUrl: string;
}

export default function AdminDocsPage() {
  const router = useRouter();
  const [tokens, setTokens] = useState<DocToken[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [generatedToken, setGeneratedToken] = useState<TokenGenerationResponse | null>(null);
  const [copySuccess, setCopySuccess] = useState<string | null>(null);
  const [description, setDescription] = useState('API Documentation Access');
  const [durationHours, setDurationHours] = useState(24);

  useEffect(() => {
    const token = Cookies.get('token');
    if (!token) {
      router.push('/');
      return;
    }

    // Check if user has SYSTEM_ADMIN role (simplified check)
    // In production, decode JWT and check claims
    fetchTokens();
  }, [router]);

  const fetchTokens = async () => {
    try {
      const response = await fetch('/api/auth/admin/docs/tokens', {
        headers: {
          'Authorization': `Bearer ${Cookies.get('token')}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setTokens(Array.isArray(data) ? data : []);
      } else if (response.status === 403) {
        alert('Access denied. SYSTEM_ADMIN role required.');
        router.push('/dashboard');
      }
    } catch (error) {
      console.error('Failed to fetch tokens:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateToken = async () => {
    setGenerating(true);
    setGeneratedToken(null);

    try {
      const response = await fetch('/api/auth/admin/docs/tokens', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${Cookies.get('token')}`,
        },
        body: JSON.stringify({
          description,
          durationHours,
        }),
      });

      if (response.ok) {
        const data = await response.json();
        setGeneratedToken(data);
        fetchTokens(); // Refresh token list
      } else {
        const error = await response.json();
        alert(`Failed to generate token: ${error.message || 'Unknown error'}`);
      }
    } catch (error) {
      console.error('Failed to generate token:', error);
      alert('Failed to generate token');
    } finally {
      setGenerating(false);
    }
  };

  const handleRevokeToken = async (tokenId: string) => {
    if (!confirm('Are you sure you want to revoke this token? Active Swagger sessions will be terminated.')) {
      return;
    }

    try {
      const response = await fetch(`/api/auth/admin/docs/tokens/${tokenId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${Cookies.get('token')}`,
        },
      });

      if (response.ok) {
        fetchTokens();
        if (generatedToken && generatedToken.tokenId === tokenId) {
          setGeneratedToken(null);
        }
      } else {
        alert('Failed to revoke token');
      }
    } catch (error) {
      console.error('Failed to revoke token:', error);
      alert('Failed to revoke token');
    }
  };

  const handleCopyUrl = async (url: string, tokenId: string) => {
    try {
      await navigator.clipboard.writeText(url);
      setCopySuccess(tokenId);
      setTimeout(() => setCopySuccess(null), 2000);
    } catch (error) {
      console.error('Failed to copy URL:', error);
      // Fallback for older browsers
      const textArea = document.createElement('textarea');
      textArea.value = url;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      setCopySuccess(tokenId);
      setTimeout(() => setCopySuccess(null), 2000);
    }
  };

  const formatDateTime = (isoString: string) => {
    return new Date(isoString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const isExpired = (expiresAt: string) => {
    return new Date(expiresAt) < new Date();
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-6 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between">
            <h1 className="text-3xl font-bold text-gray-900">
              🔒 Documentation Access Control
            </h1>
            <button
              onClick={() => router.push('/dashboard')}
              className="text-gray-600 hover:text-gray-900"
            >
              ← Back to Dashboard
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        {/* Security Notice */}
        <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-8">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-yellow-700">
                <strong>Security Notice:</strong> API documentation is now restricted. Generate access tokens to share Swagger UI links. 
                Tokens expire after the specified duration and can be revoked at any time.
              </p>
            </div>
          </div>
        </div>

        {/* Generate Token Section */}
        <div className="bg-white shadow rounded-lg p-6 mb-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Generate New Documentation Key</h2>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Description
              </label>
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                placeholder="API Documentation Access"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Duration (hours)
              </label>
              <input
                type="number"
                value={durationHours}
                onChange={(e) => setDurationHours(parseInt(e.target.value) || 24)}
                min="1"
                max="720"
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div className="flex items-end">
              <button
                onClick={handleGenerateToken}
                disabled={generating}
                className="w-full bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                {generating ? 'Generating...' : '🔑 Generate Key'}
              </button>
            </div>
          </div>

          {/* Generated Token Display */}
          {generatedToken && (
            <div className="mt-4 p-4 bg-green-50 border border-green-200 rounded-md">
              <h3 className="text-lg font-medium text-green-800 mb-2">
                ✅ Token Generated Successfully
              </h3>
              <p className="text-sm text-green-700 mb-3">
                <strong>Important:</strong> This is the only time the full token will be displayed. 
                Copy the URL below and store it securely.
              </p>
              
              <div className="space-y-2">
                <div>
                  <label className="block text-xs font-medium text-green-700 mb-1">
                    Token Preview
                  </label>
                  <code className="block bg-white px-3 py-2 rounded text-sm font-mono text-gray-800">
                    {generatedToken.token}
                  </code>
                </div>
                
                <div>
                  <label className="block text-xs font-medium text-green-700 mb-1">
                    Swagger UI Access URL
                  </label>
                  <div className="flex gap-2">
                    <code className="flex-1 bg-white px-3 py-2 rounded text-sm font-mono text-gray-800 truncate">
                      {generatedToken.accessUrl}
                    </code>
                    <button
                      onClick={() => handleCopyUrl(generatedToken.accessUrl, generatedToken.tokenId)}
                      className="px-3 py-2 bg-green-600 text-white rounded hover:bg-green-700 text-sm"
                    >
                      {copySuccess === generatedToken.tokenId ? '✓ Copied!' : '📋 Copy'}
                    </button>
                  </div>
                </div>

                <div className="text-xs text-green-600">
                  Expires: {formatDateTime(generatedToken.expiresAt)}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Active Tokens List */}
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Active Documentation Keys</h2>
          
          {loading ? (
            <div className="text-center py-8 text-gray-500">Loading...</div>
          ) : tokens.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              No active documentation keys. Generate one above.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Token</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Description</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Created By</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Created</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Expires</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Uses</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {tokens.map((token) => (
                    <tr key={token.tokenId} className={isExpired(token.expiresAt) ? 'bg-gray-100' : ''}>
                      <td className="px-4 py-3 text-sm font-mono text-gray-900">
                        {token.tokenPreview}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-900">{token.description}</td>
                      <td className="px-4 py-3 text-sm text-gray-900">{token.createdByUsername}</td>
                      <td className="px-4 py-3 text-sm text-gray-500">
                        {formatDateTime(token.createdAt)}
                      </td>
                      <td className="px-4 py-3 text-sm">
                        <span className={isExpired(token.expiresAt) ? 'text-red-600' : 'text-gray-500'}>
                          {formatDateTime(token.expiresAt)}
                          {isExpired(token.expiresAt) && ' (Expired)'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-500">{token.useCount}</td>
                      <td className="px-4 py-3 text-sm">
                        <button
                          onClick={() => handleRevokeToken(token.tokenId)}
                          className="text-red-600 hover:text-red-900"
                        >
                          Revoke
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
