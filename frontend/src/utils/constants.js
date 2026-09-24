// ============================================
// constants.js — Application constants
// ============================================

// API Base URL
export const API_BASE_URL = 'http://localhost:8081/api';

// User Roles
export const ROLES = {
  MAIN_ADMIN: 'MAIN_ADMIN',
  DEPT_ADMIN: 'DEPT_ADMIN',
};

// Route Paths
export const ROUTES = {
  LOGIN: '/login',
  MAIN_ADMIN_DASHBOARD: '/main-admin/dashboard',
  MAIN_ADMIN_DEPARTMENTS: '/main-admin/departments',
  MAIN_ADMIN_DEPT_ADMINS: '/main-admin/department-admins',
  MAIN_ADMIN_CATEGORIES: '/main-admin/categories',
  MAIN_ADMIN_TRANSACTIONS: '/main-admin/transactions',
  MAIN_ADMIN_ANALYTICS: '/main-admin/analytics',
  MAIN_ADMIN_DAILY_REPORT: '/main-admin/daily-report',
  MAIN_ADMIN_MONTHLY_REPORT: '/main-admin/monthly-report',
  DEPT_ADMIN_DASHBOARD: '/dept-admin/dashboard',
  DEPT_ADMIN_TRANSACTIONS: '/dept-admin/transactions',
  DEPT_ADMIN_ANALYTICS: '/dept-admin/analytics',
  DEPT_ADMIN_DAILY_REPORT: '/dept-admin/daily-report',
  DEPT_ADMIN_MONTHLY_REPORT: '/dept-admin/monthly-report',
};

// Transaction Types
export const TRANSACTION_TYPES = {
  IN: 'IN',
  OUT: 'OUT',
};

// Payment Methods
export const PAYMENT_METHODS = [
  { value: 'CASH', label: 'Cash' },
  { value: 'BANK_TRANSFER', label: 'Bank Transfer (NEFT/RTGS/IMPS)' },
  { value: 'UPI', label: 'UPI (GPay / PhonePe / Paytm)' },
  { value: 'CARD', label: 'Credit / Debit Card' },
  { value: 'CHEQUE', label: 'Cheque / Demand Draft' },
  { value: 'OTHER', label: 'Other / Direct Voucher' },
];

// Local Storage Keys
export const STORAGE_KEYS = {
  TOKEN: 'token',
  USER: 'user',
};
