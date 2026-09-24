// ============================================
// AuthContext.jsx — Authentication state management
// ============================================

import { createContext, useContext, useState, useEffect } from 'react';
import { getToken, getUser, saveToken, saveUser, clearAuthData } from '../utils/storage';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  // On mount, load saved auth data from localStorage
  useEffect(() => {
    const savedToken = getToken();
    const savedUser = getUser();
    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(savedUser);
    }
    setLoading(false);
  }, []);

  /**
   * Save login response to state and localStorage.
   * Called after a successful login API call.
   */
  const login = (loginResponse) => {
    const { token: jwt, ...userData } = loginResponse;
    saveToken(jwt);
    saveUser(userData);
    setToken(jwt);
    setUser(userData);
  };

  /**
   * Clear auth data from state and localStorage.
   */
  const logout = () => {
    clearAuthData();
    setToken(null);
    setUser(null);
  };

  const isAuthenticated = !!token && !!user;

  return (
    <AuthContext.Provider value={{ user, token, loading, isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * Custom hook to access auth context.
 * Usage: const { user, login, logout, isAuthenticated } = useAuth();
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
