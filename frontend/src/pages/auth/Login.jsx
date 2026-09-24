// ============================================
// Login.jsx — Login page with role switching
// ============================================

import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { mainAdminLogin, deptAdminLogin } from '../../services/authService';
import { ROUTES, ROLES } from '../../utils/constants';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const { login, isAuthenticated, user } = useAuth();

  // Tab state: 'MAIN_ADMIN' or 'DEPT_ADMIN'
  const [activeTab, setActiveTab] = useState('MAIN_ADMIN');

  // Form fields
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [adminUsername, setAdminUsername] = useState('');
  const [deptAdminUsername, setDeptAdminUsername] = useState('');
  const [deptPassword, setDeptPassword] = useState('');

  // UI state
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // If already authenticated, redirect to appropriate dashboard
  if (isAuthenticated && user) {
    if (user.role === ROLES.MAIN_ADMIN) {
      return <Navigate to={ROUTES.MAIN_ADMIN_DASHBOARD} replace />;
    }
    if (user.role === ROLES.DEPT_ADMIN) {
      return <Navigate to={ROUTES.DEPT_ADMIN_DASHBOARD} replace />;
    }
  }

  const handleTabSwitch = (tab) => {
    setActiveTab(tab);
    setError('');
  };

  // Main Admin login handler
  const handleMainAdminLogin = async (e) => {
    e.preventDefault();
    setError('');

    if (!username.trim() || !password.trim()) {
      setError('Please fill in all fields');
      return;
    }

    setLoading(true);
    try {
      const response = await mainAdminLogin(username, password);
      login(response);
      navigate(ROUTES.MAIN_ADMIN_DASHBOARD, { replace: true });
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed. Please try again.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  // Department Admin login handler
  const handleDeptAdminLogin = async (e) => {
    e.preventDefault();
    setError('');

    if (!adminUsername.trim() || !deptAdminUsername.trim() || !deptPassword.trim()) {
      setError('Please fill in all fields');
      return;
    }

    setLoading(true);
    try {
      const response = await deptAdminLogin(adminUsername, deptAdminUsername, deptPassword);
      login(response);
      navigate(ROUTES.DEPT_ADMIN_DASHBOARD, { replace: true });
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed. Please try again.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-container">
        {/* Header */}
        <div className="login-header">
          <div className="login-logo">💰</div>
          <h1>College Cashflow</h1>
          <p>Monitoring System</p>
        </div>

        {/* Tab Switcher */}
        <div className="login-tabs">
          <button
            className={`tab-btn ${activeTab === 'MAIN_ADMIN' ? 'active' : ''}`}
            onClick={() => handleTabSwitch('MAIN_ADMIN')}
          >
            Main Admin
          </button>
          <button
            className={`tab-btn ${activeTab === 'DEPT_ADMIN' ? 'active' : ''}`}
            onClick={() => handleTabSwitch('DEPT_ADMIN')}
          >
            Department Admin
          </button>
        </div>

        {/* Error Message */}
        {error && <div className="login-error">{error}</div>}

        {/* Main Admin Login Form */}
        {activeTab === 'MAIN_ADMIN' && (
          <form className="login-form" onSubmit={handleMainAdminLogin}>
            <div className="form-group">
              <label htmlFor="main-username">Username</label>
              <input
                id="main-username"
                type="text"
                placeholder="Enter admin username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
              />
            </div>
            <div className="form-group">
              <label htmlFor="main-password">Password</label>
              <input
                id="main-password"
                type="password"
                placeholder="Enter password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </div>
            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>
        )}

        {/* Department Admin Login Form */}
        {activeTab === 'DEPT_ADMIN' && (
          <form className="login-form" onSubmit={handleDeptAdminLogin}>
            <div className="form-group">
              <label htmlFor="admin-username">Admin Username</label>
              <input
                id="admin-username"
                type="text"
                placeholder="Enter main admin username"
                value={adminUsername}
                onChange={(e) => setAdminUsername(e.target.value)}
                autoComplete="username"
              />
            </div>
            <div className="form-group">
              <label htmlFor="dept-username">Department Admin Username</label>
              <input
                id="dept-username"
                type="text"
                placeholder="Enter department admin username"
                value={deptAdminUsername}
                onChange={(e) => setDeptAdminUsername(e.target.value)}
                autoComplete="username"
              />
            </div>
            <div className="form-group">
              <label htmlFor="dept-password">Password</label>
              <input
                id="dept-password"
                type="password"
                placeholder="Enter password"
                value={deptPassword}
                onChange={(e) => setDeptPassword(e.target.value)}
                autoComplete="current-password"
              />
            </div>
            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>
        )}

        {/* Footer */}
        <div className="login-footer">
          <p>College Cashflow Monitoring System &copy; 2026</p>
        </div>
      </div>
    </div>
  );
};

export default Login;
