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
import { Line, Doughnut } from 'react-chartjs-2';
import { useAuth } from '../../context/AuthContext';
import DeptAdminHeader from '../../components/DeptAdminHeader';
import reportService from '../../services/reportService';
import { exportToCSV, exportToPDF, formatCurrency, getDatePresets, formatLocalDate } from '../../utils/exportUtils';
import '../main-admin/Analytics.css';
import '../main-admin/Dashboard.css';

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

const DeptAdminAnalytics = () => {
  const { user } = useAuth();
  const today = formatLocalDate(new Date());
  const firstOfMonth = formatLocalDate(new Date(new Date().getFullYear(), new Date().getMonth(), 1));

  const [startDate, setStartDate] = useState(firstOfMonth);
  const [endDate, setEndDate] = useState(today);
  const [activePreset, setActivePreset] = useState('This Month');
  const [loading, setLoading] = useState(true);

  // Report data
  const [cashflowTrend, setCashflowTrend] = useState([]);
  const [incomeBreakdown, setIncomeBreakdown] = useState([]);
  const [expenseBreakdown, setExpenseBreakdown] = useState([]);

  const presets = getDatePresets();

  const loadAnalytics = useCallback(async () => {
    setLoading(true);
    try {
      const deptId = user?.departmentId || null;
      const [trend, incCat, expCat] = await Promise.all([
        reportService.getCashflowTrend(startDate, endDate, deptId).catch(() => []),
        reportService.getCategoryBreakdown(startDate, endDate, deptId, 'IN').catch(() => []),
        reportService.getCategoryBreakdown(startDate, endDate, deptId, 'OUT').catch(() => []),
      ]);
      setCashflowTrend(trend || []);
      setIncomeBreakdown(incCat || []);
      setExpenseBreakdown(expCat || []);
    } catch (err) {
      console.error('Error loading dept analytics:', err);
    } finally {
      setLoading(false);
    }
  }, [startDate, endDate, user?.departmentId]);

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

  // --- Export Handlers ---
  const handleExportTrendCSV = () => {
    exportToCSV(
      cashflowTrend.map((d) => ({
        date: d.date,
        income: d.totalIn,
        expense: d.totalOut,
        netFlow: d.netFlow,
      })),
      `${user?.departmentCode || 'dept'}-cashflow-trend-${startDate}-to-${endDate}`,
      [
        { key: 'date', label: 'Date' },
        { key: 'income', label: 'Money IN (₹)' },
        { key: 'expense', label: 'Money OUT (₹)' },
        { key: 'netFlow', label: 'Net Flow (₹)' },
      ]
    );
  };

  const handleExportPDF = () => {
    const reportData = [
      { metric: 'Total Money IN', value: formatCurrency(periodTotals.income) },
      { metric: 'Total Money OUT', value: formatCurrency(periodTotals.expense) },
      { metric: 'Net Balance', value: formatCurrency(periodTotals.net) },
      { metric: 'Days with Activity', value: `${cashflowTrend.length} days` },
    ];

    exportToPDF(
      `${user?.departmentName || 'Department'} Financial Report`,
      [
        { key: 'metric', label: 'Financial Summary Metric' },
        { key: 'value', label: 'Amount / Details' },
      ],
      reportData,
      `${user?.departmentCode || 'dept'}-financial-report-${startDate}-to-${endDate}`,
      { subtitle: `Timeframe: ${startDate} to ${endDate} (${activePreset || 'Custom Range'})` }
    );
  };

  return (
    <div className="analytics-page">
      <DeptAdminHeader />

      <main className="analytics-content">
        {/* Header */}
        <div className="analytics-header-section">
          <div className="analytics-title-group">
            <h2>📊 Department Financial Reports</h2>
            <p>
              Financial reports and cashflow breakdown for <strong>{user?.departmentName || 'Your Department'}</strong>
            </p>
          </div>
          <div className="export-btns">
            <button className="export-btn csv" onClick={handleExportTrendCSV}>📄 Export Trend CSV</button>
            <button className="export-btn pdf" onClick={handleExportPDF}>📑 Export Summary PDF</button>
          </div>
        </div>

        {/* Date Controls & Presets */}
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
        </div>

        {loading ? (
          <div className="analytics-loading">
            <div className="spinner" />
            Loading financial report...
          </div>
        ) : (
          <>
            {/* KPI Cards */}
            <div className="analytics-kpi-row">
              <div className="kpi-card income">
                <span className="kpi-icon">📥</span>
                <span className="kpi-label">Department Money IN</span>
                <span className="kpi-value">{formatCurrency(periodTotals.income)}</span>
              </div>
              <div className="kpi-card expense">
                <span className="kpi-icon">📤</span>
                <span className="kpi-label">Department Money OUT</span>
                <span className="kpi-value">{formatCurrency(periodTotals.expense)}</span>
              </div>
              <div className="kpi-card balance">
                <span className="kpi-icon">⚖️</span>
                <span className="kpi-label">Department Net Balance</span>
                <span className="kpi-value">{formatCurrency(periodTotals.net)}</span>
              </div>
              <div className="kpi-card count">
                <span className="kpi-icon">📋</span>
                <span className="kpi-label">Active Days</span>
                <span className="kpi-value">{cashflowTrend.length} days</span>
              </div>
            </div>

            {/* Cashflow Trend Chart */}
            <div className="charts-grid">
              <div className="chart-card">
                <h3 className="chart-title">Cashflow Trend ({activePreset || 'Selected Period'})</h3>
                <p className="chart-subtitle">Daily income vs expense for {startDate} to {endDate}</p>
                <div className="chart-container">
                  {cashflowTrend.length > 0 ? (
                    <Line data={trendChartData} options={trendChartOptions} />
                  ) : (
                    <div className="analytics-empty">
                      <div className="empty-icon">📉</div>
                      <p>No transaction data for {activePreset || `${startDate} to ${endDate}`}</p>
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
                      <p>No income recorded for this period</p>
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
                      <p>No expenses recorded for this period</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
};

export default DeptAdminAnalytics;
