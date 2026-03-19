import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const AUDIENCE = 'admin';  // This app's audience claim

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  config.headers['X-Audience'] = AUDIENCE;
  return config;
});

export interface LoginRequest {
  username: string;
  password: string;
  audience?: string;
}

export interface LoginResponse {
  success: boolean;
  userId: string;
  token: string;
  expiresIn: number;
  mustChangePassword: boolean;
  status: string;
}

export const authApi = {
  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/api/auth/login', {
      ...request,
      audience: AUDIENCE,
    });
    return response.data;
  },

  getMe: async () => {
    const response = await api.get('/api/onboarding/me');
    return response.data;
  },
};

export const adminApi = {
  // Documentation token management
  generateDocToken: async (data: { description: string; durationHours: number }) => {
    const response = await api.post('/api/auth/admin/docs/tokens', data);
    return response.data;
  },

  listDocTokens: async () => {
    const response = await api.get('/api/auth/admin/docs/tokens');
    return response.data;
  },

  revokeDocToken: async (tokenId: string) => {
    const response = await api.delete(`/api/auth/admin/docs/tokens/${tokenId}`);
    return response.data;
  },

  // User management
  getAllUsers: async () => {
    const response = await api.get('/api/admin/users');
    return response.data;
  },

  suspendUser: async (userId: string) => {
    const response = await api.patch(`/api/onboarding/users/${userId}/status?status=SUSPENDED`);
    return response.data;
  },

  // Audit logs
  getAuditLogs: async () => {
    const response = await api.get('/api/audit/logs');
    return response.data;
  },

  // Branch management
  getBranches: async () => {
    const response = await api.get('/api/admin/branches');
    return response.data;
  },

  createBranch: async (data: { code: string; name: string; city: string }) => {
    const response = await api.post('/api/admin/branches', data);
    return response.data;
  },
};

export default api;
