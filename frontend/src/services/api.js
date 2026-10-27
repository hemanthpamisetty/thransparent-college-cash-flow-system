// ============================================
// api.js — Axios instance and API configuration
// ============================================

import axios from 'axios';
import { getToken, clearAuthData } from '../utils/storage';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api';

// Create Axios instance with base URL
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor — attach JWT token to every request
api.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor — handle 401 (unauthorized token expiry) globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only auto-redirect on 401 for non-auth endpoints (e.g. token expired/invalid on protected APIs)
    // Never reload/redirect on /auth/ login failure so login errors remain visible to users
    const isAuthEndpoint = error.config?.url?.includes('/auth/');
    if (error.response?.status === 401 && !isAuthEndpoint) {
      clearAuthData();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
