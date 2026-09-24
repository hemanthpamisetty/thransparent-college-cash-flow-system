// ============================================
// storage.js — Local storage utility functions
// ============================================

import { STORAGE_KEYS } from './constants';

// Save token to local storage
export const saveToken = (token) => {
  if (token) {
    localStorage.setItem(STORAGE_KEYS.TOKEN, token);
  }
};

// Get token from local storage
export const getToken = () => {
  try {
    return localStorage.getItem(STORAGE_KEYS.TOKEN);
  } catch {
    return null;
  }
};

// Remove token from local storage
export const removeToken = () => {
  try {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
  } catch {
    // Ignore error
  }
};

// Save user data to local storage
export const saveUser = (user) => {
  if (user) {
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
  }
};

// Get user data from local storage (safe with error fallback)
export const getUser = () => {
  try {
    const user = localStorage.getItem(STORAGE_KEYS.USER);
    if (!user || user === 'undefined' || user === 'null') return null;
    return JSON.parse(user);
  } catch {
    try {
      localStorage.removeItem(STORAGE_KEYS.USER);
    } catch {
      // Ignore
    }
    return null;
  }
};

// Remove user data from local storage
export const removeUser = () => {
  try {
    localStorage.removeItem(STORAGE_KEYS.USER);
  } catch {
    // Ignore error
  }
};

// Clear all authentication data
export const clearAuthData = () => {
  removeToken();
  removeUser();
};
