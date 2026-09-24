// ============================================
// DeptAdminDashboard.jsx — Department Admin dashboard page
// ============================================

import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';
import { ROUTES } from '../../utils/constants';
import DeptAdminHeader from '../../components/DeptAdminHeader';
import transactionService from '../../services/transactionService';
import '../main-admin/Dashboard.css';

const DeptAdminDashboard = () => {
  const { user } = useAuth();

  const [summary, setSummary] = useState({
    totalIncome: 0,
    totalExpense: 0,
    netBalance: 0,
    totalTransactions: 0,
    incomeCount: 0,
    expenseCount: 0,
    loading: true,
  });

  useEffect(() => {
    loadDeptSummary();
  }, []);

  const loadDeptSummary = async () => {
    try {
      const data = await transactionService.getCashflowSummary();
      setSummary({
        ...data,
        loading: false,
      });
    } catch {
      setSummary((prev) => ({ ...prev, loading: false }));
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
      <DeptAdminHeader />

      <main className="dashboard-content">
        <div className="welcome-section">
          <h2>Department Admin Dashboard</h2>
          <p className="dept-name-display">
            Department: <strong>{user?.departmentName || 'N/A'}</strong> ({user?.departmentCode || 'N/A'})
          </p>
        </div>

        {/* Live Department Cashflow KPI Cards */}
        <div className="metrics-row">
          <div className="metric-card income">
            <div className="metric-icon">📥</div>
            <div className="metric-data">
              <span className="metric-label">Department Income</span>
              <span className="metric-value">
                {summary.loading ? '...' : formatCurrency(summary.totalIncome)}
              </span>
              <span className="metric-subtitle">{summary.incomeCount || 0} income records</span>
            </div>
          </div>

          <div className="metric-card expense">
            <div className="metric-icon">📤</div>
            <div className="metric-data">
              <span className="metric-label">Department Expenses</span>
              <span className="metric-value">
                {summary.loading ? '...' : formatCurrency(summary.totalExpense)}
              </span>
              <span className="metric-subtitle">{summary.expenseCount || 0} expense records</span>
            </div>
          </div>

          <div className="metric-card balance">
            <div className="metric-icon">⚖️</div>
            <div className="metric-data">
              <span className="metric-label">Department Balance</span>
              <span className="metric-value">
                {summary.loading ? '...' : formatCurrency(summary.netBalance)}
              </span>
              <span className="metric-subtitle">{summary.totalTransactions || 0} transactions</span>
            </div>
          </div>
        </div>

        {/* Cards Grid */}
        <div className="cards-grid">
          <Link to={ROUTES.DEPT_ADMIN_TRANSACTIONS} className="dash-card">
            <div className="card-icon">💳</div>
            <div className="card-info">
              <h3>Transactions Ledger</h3>
              <p>View, search, filter, and inspect vouchers for your department</p>
            </div>
            <span className="card-status ready">View Ledger →</span>
          </Link>

          <Link to={ROUTES.DEPT_ADMIN_TRANSACTIONS} className="dash-card">
            <div className="card-icon">💵</div>
            <div className="card-info">
              <h3>Record Money IN</h3>
              <p>Record student fees, grants, donations, or sponsorships</p>
            </div>
            <span className="card-status ready">Record Income →</span>
          </Link>

          <Link to={ROUTES.DEPT_ADMIN_TRANSACTIONS} className="dash-card">
            <div className="card-icon">💸</div>
            <div className="card-info">
              <h3>Record Money OUT</h3>
              <p>Record department expenses, lab equipment, maintenance, or events</p>
            </div>
            <span className="card-status ready">Record Expense →</span>
          </Link>

          <Link to={ROUTES.DEPT_ADMIN_ANALYTICS} className="dash-card">
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

export default DeptAdminDashboard;
