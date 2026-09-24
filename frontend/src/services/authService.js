// ============================================
// authService.js — Authentication API calls
// ============================================

import api from './api';

/**
 * Main Admin Login
 * POST /api/auth/main-admin/login
 */
export const mainAdminLogin = async (username, password) => {
  const response = await api.post('/auth/main-admin/login', {
    username,
    password,
  });
  return response.data;
};

/**
 * Department Admin Login
 * POST /api/auth/dept-admin/login
 */
export const deptAdminLogin = async (adminUsername, departmentAdminUsername, password) => {
  const response = await api.post('/auth/dept-admin/login', {
    adminUsername,
    departmentAdminUsername,
    password,
  });
  return response.data;
};
