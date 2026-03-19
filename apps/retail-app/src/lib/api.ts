import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const AUDIENCE = 'retail';  // This app's audience claim

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token and audience to requests
api.interceptors.request.use((config) => {
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // Include audience in header for backend validation
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

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export const authApi = {
  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/api/auth/login', {
      ...request,
      audience: AUDIENCE,
    });
    return response.data;
  },

  register: async (request: RegisterRequest) => {
    const response = await api.post('/api/onboarding/register', request);
    return response.data;
  },

  getMe: async () => {
    const response = await api.get('/api/onboarding/me');
    return response.data;
  },
};

export const accountsApi = {
  getAll: async () => {
    const response = await api.get('/api/accounts');
    return response.data;
  },
};

export const transfersApi = {
  create: async (data: { fromId: string; toId: string; amount: number }) => {
    const response = await api.post('/api/transfers', data);
    return response.data;
  },
};

export default api;
