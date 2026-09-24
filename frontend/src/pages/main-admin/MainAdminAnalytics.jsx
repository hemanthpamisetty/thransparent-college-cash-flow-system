import React, { useState, useEffect, useCallback } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Filler,
  Tooltip,
  Legend,
} from 'chart.js';
import { Line, Bar, Doughnut } from 'react-chartjs-2';
import MainAdminHeader from '../../components/MainAdminHeader';
import reportService from '../../services/reportService';
import departmentService from '../../services/departmentService';
import { exportToCSV, exportToPDF, formatCurrency, getDatePresets, formatLocalDate } from '../../utils/exportUtils';
import './Analytics.css';
import './Dashboard.css';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Filler,
  Tooltip,
  Legend
);

const MainAdminAnalytics = () => {
  const today = formatLocalDate(new Date());
  const firstOfMonth = formatLocalDate(new Date(new Date().getFullYear(), new Date().getMonth(), 1));

  const [startDate, setStartDate] = useState(firstOfMonth);
  const [endDate, setEndDate] = useState(today);
  const [activePreset, setActivePreset] = useState('This Month');
  const [departmentId, setDepartmentId] = useState('');
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);

  // Report data
  const [cashflowTrend, setCashflowTrend] = useState([]);
  const [incomeBreakdown, setIncomeBreakdown] = useState([]);
  const [expenseBreakdown, setExpenseBreakdown] = useState([]);
  const [deptSummary, setDeptSummary] = useState([]);

  const presets = getDatePresets();

  useEffect(() => {
    departmentService.getAllDepartments().then(setDepartments).catch(() => {});
  }, []);

  const loadAnalytics = useCallback(async () => {
    setLoading(true);
    try {
      const deptId = departmentId || null;
      const [trend, incCat, expCat, deptSum] = await Promise.all([
        reportService.getCashflowTrend(startDate, endDate, deptId).catch(() => []),
        reportService.getCategoryBreakdown(startDate, endDate, deptId, 'IN').catch(() => []),
        reportService.getCategoryBreakdown(startDate, endDate, deptId, 'OUT').catch(() => []),
        reportService.getDepartmentSummary(startDate, endDate).catch(() => []),
      ]);
      setCashflowTrend(trend);
      setIncomeBreakdown(incCat);
      setExpenseBreakdown(expCat);
      setDeptSummary(deptSum);
    } catch {
      // silent
    } finally {
      setLoading(false);
    }
  }, [startDate, endDate, departmentId]);

  useEffect(() => {
    loadAnalytics();
  }, [loadAnalytics]);

  const handlePreset = (preset) => {
    setStartDate(preset.startDate);
    setEndDate(preset.endDate);
    setActivePreset(preset.label);
  };

  // Calculate period totals from trend data
  const periodTotals = cashflowTrend.reduce(
    (acc, day) => ({
      income: acc.income + (day.totalIn || 0),
      expense: acc.expense + (day.totalOut || 0),
      net: acc.net + (day.netFlow || 0),
    }),
    { income: 0, expense: 0, net: 0 }
  );

  // --- Chart Configurations ---

  const chartTextColor = 'rgba(255, 255, 255, 0.7)';
  const gridColor = 'rgba(255, 255, 255, 0.06)';

  // Line chart — Cashflow Trend
  const trendChartData = {
    labels: cashflowTrend.map((d) => {
      const dt = new Date(d.date);
      return dt.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
    }),
    datasets: [
      {
        label: 'Money IN',
        data: cashflowTrend.map((d) => d.totalIn || 0),
        borderColor: '#2ed573',
        backgroundColor: 'rgba(46, 213, 115, 0.1)',
        fill: true,
        tension: 0.4,
        pointRadius: 3,
        pointHoverRadius: 6,
      },
      {
        label: 'Money OUT',
        data: cashflowTrend.map((d) => d.totalOut || 0),
        borderColor: '#ff6b7a',
        backgroundColor: 'rgba(255, 107, 122, 0.1)',
        fill: true,
        tension: 0.4,
        pointRadius: 3,
        pointHoverRadius: 6,
      },
      {
        label: 'Net Flow',
        data: cashflowTrend.map((d) => d.netFlow || 0),
        borderColor: '#4f8cff',
        backgroundColor: 'rgba(79, 140, 255, 0.05)',
        fill: false,
        tension: 0.4,
        borderDash: [5, 5],
        pointRadius: 2,
        pointHoverRadius: 5,
      },
    ],
  };

  const trendChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { intersect: false, mode: 'index' },
    plugins: {
      legend: { labels: { color: chartTextColor, usePointStyle: true, padding: 16 } },
      tooltip: {
        backgroundColor: 'rgba(20, 31, 54, 0.95)',
        borderColor: 'rgba(79, 140, 255, 0.3)',
        borderWidth: 1,
        titleColor: '#fff',
        bodyColor: 'rgba(255,255,255,0.8)',
        padding: 12,
        callbacks: {
          label: (ctx) => `${ctx.dataset.label}: ${formatCurrency(ctx.parsed.y)}`,
        },
      },
    },
    scales: {
      x: { ticks: { color: chartTextColor, maxTicksLimit: 15 }, grid: { color: gridColor } },
      y: {
        ticks: {
          color: chartTextColor,
          callback: (val) => '₹' + (val >= 1000 ? (val / 1000).toFixed(0) + 'K' : val),
        },
        grid: { color: gridColor },
      },
    },
  };

  // Doughnut — Income Category Breakdown
  const incomeColors = ['#2ed573', '#00d2d3', '#1dd1a1', '#55efc4', '#00b894', '#00cec9', '#81ecec', '#a3d5d3'];
  const incDoughnutData = {
    labels: incomeBreakdown.map((c) => c.categoryName),
    datasets: [
      {
        data: incomeBreakdown.map((c) => c.totalAmount),
        backgroundColor: incomeBreakdown.map((_, i) => incomeColors[i % incomeColors.length]),
        borderColor: 'rgba(12, 18, 32, 0.8)',
        borderWidth: 2,
        hoverOffset: 8,
      },
    ],
  };

  // Doughnut — Expense Category Breakdown
  const expenseColors = ['#ff6b7a', '#ff6348', '#ff7675', '#e17055', '#d63031', '#fab1a0', '#fdcb6e', '#e77f67'];
  const expDoughnutData = {
    labels: expenseBreakdown.map((c) => c.categoryName),
    datasets: [
      {
        data: expenseBreakdown.map((c) => c.totalAmount),
        backgroundColor: expenseBreakdown.map((_, i) => expenseColors[i % expenseColors.length]),
        borderColor: 'rgba(12, 18, 32, 0.8)',
        borderWidth: 2,
        hoverOffset: 8,
      },
    ],
  };

  const doughnutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: { color: chartTextColor, usePointStyle: true, padding: 12, font: { size: 11 } },
      },
      tooltip: {
        backgroundColor: 'rgba(20, 31, 54, 0.95)',
        borderColor: 'rgba(79, 140, 255, 0.3)',
        borderWidth: 1,
        callbacks: {
          label: (ctx) => `${ctx.label}: ${formatCurrency(ctx.parsed)}`,
        },
      },
    },
  };

  // Bar chart — Department Comparison
  const deptBarData = {
    labels: deptSummary.map((d) => d.departmentName),
    datasets: [
      {
        label: 'Income',
        data: deptSummary.map((d) => d.totalIncome || 0),
        backgroundColor: 'rgba(46, 213, 115, 0.7)',
        borderColor: '#2ed573',
        borderWidth: 1,
        borderRadius: 4,
      },
      {
        label: 'Expense',
        data: deptSummary.map((d) => d.totalExpense || 0),
        backgroundColor: 'rgba(255, 107, 122, 0.7)',
        borderColor: '#ff6b7a',
        borderWidth: 1,
        borderRadius: 4,
      },
    ],
  };

  const deptBarOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { labels: { color: chartTextColor, usePointStyle: true, padding: 16 } },
      tooltip: {
        backgroundColor: 'rgba(20, 31, 54, 0.95)',
        callbacks: {
          label: (ctx) => `${ctx.dataset.label}: ${formatCurrency(ctx.parsed.y)}`,
        },
      },
    },
    scales: {
      x: { ticks: { color: chartTextColor }, grid: { color: gridColor } },
      y: {
        ticks: {
          color: chartTextColor,
          callback: (val) => '₹' + (val >= 1000 ? (val / 1000).toFixed(0) + 'K' : val),
        },
        grid: { color: gridColor },
      },
    },
  };

  // --- Export Handlers ---
  const handleExportTrendCSV = () => {
    exportToCSV(
      cashflowTrend.map((d) => ({
        date: d.date,
        income: d.totalIn,
        expense: d.totalOut,
        netFlow: d.netFlow,
      })),
      `cashflow-trend-${startDate}-to-${endDate}`,
      [
        { key: 'date', label: 'Date' },
        { key: 'income', label: 'Money IN (₹)' },
        { key: 'expense', label: 'Money OUT (₹)' },
        { key: 'netFlow', label: 'Net Flow (₹)' },
      ]
    );
  };

  const handleExportDeptCSV = () => {
    exportToCSV(
      deptSummary.map((d) => ({
        department: d.departmentName,
        code: d.departmentCode,
        income: d.totalIncome,
        expense: d.totalExpense,
        netBalance: d.netBalance,
        transactions: d.transactionCount,
      })),
      `department-summary-${startDate}-to-${endDate}`,
      [
        { key: 'department', label: 'Department' },
        { key: 'code', label: 'Code' },
        { key: 'income', label: 'Income (₹)' },
        { key: 'expense', label: 'Expense (₹)' },
        { key: 'netBalance', label: 'Net Balance (₹)' },
        { key: 'transactions', label: 'Transactions' },
      ]
    );
  };

  const handleExportDeptPDF = () => {
    exportToPDF(
      'Department-wise Financial Summary',
      [
        { key: 'department', label: 'Department' },
        { key: 'code', label: 'Code' },
        { key: 'income', label: 'Income (₹)' },
        { key: 'expense', label: 'Expense (₹)' },
        { key: 'netBalance', label: 'Net Balance (₹)' },
        { key: 'transactions', label: 'Transactions' },
      ],
      deptSummary.map((d) => ({
        department: d.departmentName,
        code: d.departmentCode,
        income: formatCurrency(d.totalIncome),
        expense: formatCurrency(d.totalExpense),
        netBalance: formatCurrency(d.netBalance),
        transactions: d.transactionCount,
      })),
      `department-summary-${startDate}-to-${endDate}`,
      { subtitle: `Period: ${startDate} to ${endDate}` }
    );
  };

  return (
    <div className="analytics-page">
      <MainAdminHeader />

      <main className="analytics-content">
        {/* Header */}
        <div className="analytics-header-section">
          <div className="analytics-title-group">
            <h2>📊 Financial Analytics</h2>
            <p>Visualize college-wide cashflow trends, category breakdowns, and departmental comparisons</p>
          </div>
          <div className="export-btns">
            <button className="export-btn csv" onClick={handleExportTrendCSV}>📄 Export Trend CSV</button>
            <button className="export-btn pdf" onClick={handleExportDeptPDF}>📑 Export Dept PDF</button>
          </div>
        </div>

        {/* Date Controls */}
        <div className="date-controls" style={{ marginBottom: 24 }}>
          <div className="date-range-group">
            <label>From</label>
            <input type="date" value={startDate} onChange={(e) => { setStartDate(e.target.value); setActivePreset(''); }} />
            <label>To</label>
            <input type="date" value={endDate} onChange={(e) => { setEndDate(e.target.value); setActivePreset(''); }} />
          </div>
          <div className="preset-btns">
            {presets.map((p) => (
              <button
                key={p.label}
                className={`preset-btn ${activePreset === p.label ? 'active' : ''}`}
                onClick={() => handlePreset(p)}
              >
                {p.label}
              </button>
            ))}
          </div>
          <div className="date-range-group">
            <label>Department</label>
            <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
              <option value="">All Departments</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>{d.name}</option>
              ))}
            </select>
          </div>
        </div>

        {loading ? (
          <div className="analytics-loading">
            <div className="spinner" />
            Loading analytics...
          </div>
        ) : (
          <>
            {/* KPI Cards */}
            <div className="analytics-kpi-row">
              <div className="kpi-card income">
                <span className="kpi-icon">📥</span>
                <span className="kpi-label">Total Money IN</span>
                <span className="kpi-value">{formatCurrency(periodTotals.income)}</span>
              </div>
              <div className="kpi-card expense">
                <span className="kpi-icon">📤</span>
                <span className="kpi-label">Total Money OUT</span>
                <span className="kpi-value">{formatCurrency(periodTotals.expense)}</span>
              </div>
              <div className="kpi-card balance">
                <span className="kpi-icon">⚖️</span>
                <span className="kpi-label">Net Balance</span>
                <span className="kpi-value">{formatCurrency(periodTotals.net)}</span>
              </div>
              <div className="kpi-card count">
                <span className="kpi-icon">📋</span>
                <span className="kpi-label">Data Points</span>
                <span className="kpi-value">{cashflowTrend.length} days</span>
              </div>
            </div>

            {/* Cashflow Trend Chart */}
            <div className="charts-grid">
              <div className="chart-card">
                <h3 className="chart-title">Cashflow Trend</h3>
                <p className="chart-subtitle">Daily income vs expense over the selected period</p>
                <div className="chart-container">
                  {cashflowTrend.length > 0 ? (
                    <Line data={trendChartData} options={trendChartOptions} />
                  ) : (
                    <div className="analytics-empty">
                      <div className="empty-icon">📉</div>
                      <p>No transaction data for the selected period</p>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Category Breakdowns */}
            <div className="charts-grid two-col">
              <div className="chart-card">
                <h3 className="chart-title">Income by Category</h3>
                <p className="chart-subtitle">Money IN distribution across categories</p>
                <div className="chart-container small">
                  {incomeBreakdown.length > 0 ? (
                    <Doughnut data={incDoughnutData} options={doughnutOptions} />
                  ) : (
                    <div className="analytics-empty">
                      <div className="empty-icon">📥</div>
                      <p>No income data</p>
                    </div>
                  )}
                </div>
              </div>
              <div className="chart-card">
                <h3 className="chart-title">Expenses by Category</h3>
                <p className="chart-subtitle">Money OUT distribution across categories</p>
                <div className="chart-container small">
                  {expenseBreakdown.length > 0 ? (
                    <Doughnut data={expDoughnutData} options={doughnutOptions} />
                  ) : (
                    <div className="analytics-empty">
                      <div className="empty-icon">📤</div>
                      <p>No expense data</p>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Department Comparison */}
            <div className="charts-grid">
              <div className="chart-card">
                <h3 className="chart-title">Department-wise Comparison</h3>
                <p className="chart-subtitle">Income vs expense across all departments</p>
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 8 }}>
                  <button className="export-btn csv" onClick={handleExportDeptCSV} style={{ fontSize: 11, padding: '4px 10px' }}>
                    📄 CSV
                  </button>
                </div>
                <div className="chart-container">
                  {deptSummary.length > 0 ? (
                    <Bar data={deptBarData} options={deptBarOptions} />
                  ) : (
                    <div className="analytics-empty">
                      <div className="empty-icon">🏛️</div>
                      <p>No department data</p>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Department Summary Table */}
            {deptSummary.length > 0 && (
              <div className="chart-card" style={{ marginTop: 0 }}>
                <h3 className="chart-title">Department Summary Table</h3>
                <p className="chart-subtitle">Detailed financial breakdown per department</p>
                <div className="glass-table-container" style={{ marginTop: 16 }}>
                  <table className="dept-summary-table">
                    <thead>
                      <tr>
                        <th>Department</th>
                        <th>Code</th>
                        <th>Income</th>
                        <th>Expense</th>
                        <th>Net Balance</th>
                        <th>Transactions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {deptSummary.map((d) => (
                        <tr key={d.departmentId}>
                          <td>{d.departmentName}</td>
                          <td><span className="code-badge">{d.departmentCode}</span></td>
                          <td className="income-val">{formatCurrency(d.totalIncome)}</td>
                          <td className="expense-val">{formatCurrency(d.totalExpense)}</td>
                          <td className={`balance-val ${(d.netBalance || 0) >= 0 ? 'positive' : 'negative'}`}>
                            {formatCurrency(d.netBalance)}
                          </td>
                          <td>{d.transactionCount}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
};

export default MainAdminAnalytics;
