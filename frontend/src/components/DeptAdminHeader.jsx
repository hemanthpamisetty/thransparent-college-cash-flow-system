import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '../utils/constants';

export const DeptAdminHeader = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN, { replace: true });
  };

  const navItems = [
    { label: 'Dashboard', path: ROUTES.DEPT_ADMIN_DASHBOARD, icon: '🏠' },
    { label: 'Transactions', path: ROUTES.DEPT_ADMIN_TRANSACTIONS, icon: '💳' },
    { label: 'Financial Reports', path: ROUTES.DEPT_ADMIN_ANALYTICS, icon: '📊' },
  ];

  return (
    <>
      <header className="dashboard-header">
        <div className="header-left" onClick={() => navigate(ROUTES.DEPT_ADMIN_DASHBOARD)}>
          <span className="header-logo">💰</span>
          <h1>College Cashflow</h1>
        </div>
        <div className="header-right">
          <span className="user-info">
            Welcome, <strong>{user?.fullName || 'Admin'}</strong>
          </span>
          <span className="role-badge dept-admin">
            {user?.departmentName ? `${user.departmentName} ADMIN` : 'DEPT ADMIN'}
          </span>
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

export default DeptAdminHeader;
