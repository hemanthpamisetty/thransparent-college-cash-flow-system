import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '../utils/constants';

export const MainAdminHeader = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN, { replace: true });
  };

  const navItems = [
    { label: 'Dashboard', path: ROUTES.MAIN_ADMIN_DASHBOARD, icon: '🏠' },
    { label: 'Transactions', path: ROUTES.MAIN_ADMIN_TRANSACTIONS, icon: '💳' },
    { label: 'Financial Reports', path: ROUTES.MAIN_ADMIN_ANALYTICS, icon: '📊' },
    { label: 'Departments', path: ROUTES.MAIN_ADMIN_DEPARTMENTS, icon: '🏛️' },
    { label: 'Department Admins', path: ROUTES.MAIN_ADMIN_DEPT_ADMINS, icon: '👥' },
    { label: 'Categories', path: ROUTES.MAIN_ADMIN_CATEGORIES, icon: '🏷️' },
  ];

  return (
    <>
      <header className="dashboard-header">
        <div className="header-left" onClick={() => navigate(ROUTES.MAIN_ADMIN_DASHBOARD)}>
          <span className="header-logo">💰</span>
          <h1>College Cashflow</h1>
        </div>
        <div className="header-right">
          <span className="user-info">
            Welcome, <strong>{user?.fullName || 'Admin'}</strong>
          </span>
          <span className="role-badge main-admin">MAIN ADMIN</span>
          <button className="logout-btn" onClick={handleLogout}>Logout</button>
        </div>
      </header>

      <nav className="admin-nav-bar">
        {navItems.map((item) => (
          <button
            key={item.path}
            className={`nav-item-btn ${location.pathname === item.path ? 'active' : ''}`}
            onClick={() => navigate(item.path)}
          >
            <span>{item.icon}</span>
            <span>{item.label}</span>
          </button>
        ))}
      </nav>
    </>
  );
};

export default MainAdminHeader;
