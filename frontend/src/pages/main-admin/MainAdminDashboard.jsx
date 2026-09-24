import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';
import { ROUTES } from '../../utils/constants';
import MainAdminHeader from '../../components/MainAdminHeader';
import departmentService from '../../services/departmentService';
import userService from '../../services/userService';
import categoryService from '../../services/categoryService';
import transactionService from '../../services/transactionService';
import './Dashboard.css';

const MainAdminDashboard = () => {
  const { user } = useAuth();

  const [stats, setStats] = useState({
    departmentsCount: 0,
    deptAdminsCount: 0,
    categoriesCount: 0,
    totalIncome: 0,
    totalExpense: 0,
    netBalance: 0,
    totalTransactions: 0,
    loading: true,
  });

  useEffect(() => {
    loadDashboardMetrics();
  }, []);

  const loadDashboardMetrics = async () => {
    try {
      const [depts, admins, cats, summary] = await Promise.all([
        departmentService.getAllDepartments().catch(() => []),
        userService.getAllDeptAdmins().catch(() => []),
        categoryService.getAllCategories().catch(() => []),
        transactionService.getCashflowSummary().catch(() => ({
          totalIncome: 0,
          totalExpense: 0,
          netBalance: 0,
          totalTransactions: 0,
        })),
      ]);

      setStats({
        departmentsCount: depts.length,
        deptAdminsCount: admins.length,
        categoriesCount: cats.length,
        totalIncome: summary.totalIncome || 0,
        totalExpense: summary.totalExpense || 0,
        netBalance: summary.netBalance || 0,
        totalTransactions: summary.totalTransactions || 0,
        loading: false,
      });
    } catch {
      setStats((prev) => ({ ...prev, loading: false }));
    }
  };

  const formatCurrency = (val) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(val || 0);
  };

  return (
    <div className="dashboard-page">
      <MainAdminHeader />

      <main className="dashboard-content">
        <div className="welcome-section">
          <h2>Main Admin Overview</h2>
          <p>
            Welcome, <strong>{user?.fullName || 'Main Admin'}</strong>. Manage college-wide cashflows, departments, administrator accounts, and financial categories.
          </p>
        </div>

        {/* Live Cashflow KPI Cards */}
        <div className="metrics-row">
          <div className="metric-card income">
            <div className="metric-icon">📥</div>
            <div className="metric-data">
              <span className="metric-label">College Money IN</span>
              <span className="metric-value">
                {stats.loading ? '...' : formatCurrency(stats.totalIncome)}
              </span>
              <span className="metric-subtitle">Total income received</span>
            </div>
          </div>

          <div className="metric-card expense">
            <div className="metric-icon">📤</div>
            <div className="metric-data">
              <span className="metric-label">College Money OUT</span>
              <span className="metric-value">
                {stats.loading ? '...' : formatCurrency(stats.totalExpense)}
              </span>
              <span className="metric-subtitle">Total expenses paid</span>
            </div>
          </div>

          <div className="metric-card balance">
            <div className="metric-icon">⚖️</div>
            <div className="metric-data">
              <span className="metric-label">Net College Balance</span>
              <span className="metric-value">
                {stats.loading ? '...' : formatCurrency(stats.netBalance)}
              </span>
              <span className="metric-subtitle">{stats.totalTransactions} transactions recorded</span>
            </div>
          </div>
        </div>

        {/* Navigation & Management Cards Grid */}
        <div className="cards-grid">
          {/* Transactions Management Card */}
          <Link to={ROUTES.MAIN_ADMIN_TRANSACTIONS} className="dash-card">
            <div className="card-icon">💳</div>
            <div className="card-info">
              <h3>Transactions & Cashflow</h3>
              <p>
                Record Money IN / OUT, view ledgers & vouchers ({stats.loading ? '...' : `${stats.totalTransactions} total entries`})
              </p>
            </div>
            <span className="card-status ready">Manage Transactions →</span>
          </Link>

          {/* Departments Card */}
          <Link to={ROUTES.MAIN_ADMIN_DEPARTMENTS} className="dash-card">
            <div className="card-icon">🏛️</div>
            <div className="card-info">
              <h3>Departments</h3>
              <p>Manage college departments ({stats.loading ? '...' : `${stats.departmentsCount} active/total`})</p>
            </div>
            <span className="card-status ready">Manage Departments →</span>
          </Link>

          {/* Department Admins Card */}
          <Link to={ROUTES.MAIN_ADMIN_DEPT_ADMINS} className="dash-card">
            <div className="card-icon">👥</div>
            <div className="card-info">
              <h3>Department Admins</h3>
              <p>Manage admin credentials ({stats.loading ? '...' : `${stats.deptAdminsCount} admins`})</p>
            </div>
            <span className="card-status ready">Manage Admins →</span>
          </Link>

          {/* Categories Card */}
          <Link to={ROUTES.MAIN_ADMIN_CATEGORIES} className="dash-card">
            <div className="card-icon">🏷️</div>
            <div className="card-info">
              <h3>Income & Expense Categories</h3>
              <p>Configure IN & OUT categories ({stats.loading ? '...' : `${stats.categoriesCount} categories`})</p>
            </div>
            <span className="card-status ready">Manage Categories →</span>
          </Link>

          {/* Financial Reports & Analytics Card */}
          <Link to={ROUTES.MAIN_ADMIN_ANALYTICS} className="dash-card">
            <div className="card-icon">📊</div>
            <div className="card-info">
              <h3>Financial Reports & Analytics</h3>
              <p>View & export reports for Today, Last 3 Days, Last Week, Month, Year, or Custom Range</p>
            </div>
            <span className="card-status ready">View Reports →</span>
          </Link>
        </div>
      </main>
    </div>
  );
};

export default MainAdminDashboard;
