import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const AUDIENCE = 'staff';  // This app's audience claim

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

export const onboardingApi = {
  approveUser: async (userId: string) => {
    const response = await api.put(`/api/onboarding/users/${userId}/approve`);
    return response.data;
  },

  updateUserStatus: async (userId: string, status: string) => {
    const response = await api.patch(`/api/onboarding/users/${userId}/status?status=${status}`);
    return response.data;
  },

  searchUsers: async (query: string) => {
    const response = await api.get(`/api/staff/users/search?q=${query}`);
    return response.data;
  },
};

export const loansApi = {
  getPending: async () => {
    const response = await api.get('/api/loans/pending');
    return response.data;
  },

  approve: async (loanId: string) => {
    const response = await api.post(`/api/loans/${loanId}/approve`);
    return response.data;
  },
};

export default api;
