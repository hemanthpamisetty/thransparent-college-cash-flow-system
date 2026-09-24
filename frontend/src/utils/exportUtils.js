// ============================================
// exportUtils.js — CSV and PDF export utilities
// ============================================

import jsPDF from 'jspdf';
import 'jspdf-autotable';

/**
 * Export an array of objects as a CSV file download.
 * @param {Array<Object>} data - Array of row objects
 * @param {string} filename - File name (without extension)
 * @param {Array<{key: string, label: string}>} columns - Column definitions
 */
export const exportToCSV = (data, filename, columns) => {
  if (!data || data.length === 0) return;

  const headers = columns.map((col) => col.label);
  const rows = data.map((item) =>
    columns.map((col) => {
      const val = item[col.key];
      // Escape commas and quotes in CSV values
      if (val === null || val === undefined) return '';
      const str = String(val);
      if (str.includes(',') || str.includes('"') || str.includes('\n')) {
        return `"${str.replace(/"/g, '""')}"`;
      }
      return str;
    })
  );

  const csvContent = [headers.join(','), ...rows.map((r) => r.join(','))].join('\n');
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);

  const link = document.createElement('a');
  link.href = url;
  link.download = `${filename}.csv`;
  link.click();
  URL.revokeObjectURL(url);
};

/**
 * Export data as a formatted PDF document.
 * @param {string} title - Report title
 * @param {Array<{key: string, label: string}>} columns - Column definitions
 * @param {Array<Object>} data - Array of row objects
 * @param {string} filename - File name (without extension)
 * @param {Object} options - Additional options (subtitle, orientation)
 */
export const exportToPDF = (title, columns, data, filename, options = {}) => {
  const { subtitle = '', orientation = 'landscape' } = options;

  const doc = new jsPDF({ orientation, unit: 'mm', format: 'a4' });

  // Title
  doc.setFontSize(18);
  doc.setTextColor(30, 58, 95);
  doc.text(title, 14, 20);

  // Subtitle
  if (subtitle) {
    doc.setFontSize(11);
    doc.setTextColor(100, 100, 100);
    doc.text(subtitle, 14, 28);
  }

  // Timestamp
  doc.setFontSize(9);
  doc.setTextColor(140, 140, 140);
  doc.text(`Generated: ${new Date().toLocaleString('en-IN')}`, 14, subtitle ? 34 : 28);

  // Table
  const headers = columns.map((col) => col.label);
  const rows = data.map((item) =>
    columns.map((col) => {
      const val = item[col.key];
      return val !== null && val !== undefined ? String(val) : '';
    })
  );

  doc.autoTable({
    head: [headers],
    body: rows,
    startY: subtitle ? 40 : 34,
    theme: 'grid',
    headStyles: {
      fillColor: [30, 58, 95],
      textColor: [255, 255, 255],
      fontSize: 10,
      fontStyle: 'bold',
    },
    bodyStyles: {
      fontSize: 9,
      textColor: [50, 50, 50],
    },
    alternateRowStyles: {
      fillColor: [240, 245, 250],
    },
    styles: {
      cellPadding: 3,
      lineWidth: 0.1,
      lineColor: [200, 200, 200],
    },
    margin: { left: 14, right: 14 },
  });

  doc.save(`${filename}.pdf`);
};

/**
 * Format a number as INR currency string.
 */
export const formatCurrency = (val) => {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(val || 0);
};

/**
 * Format date to YYYY-MM-DD using local timezone (preventing UTC offset issues)
 */
export const formatLocalDate = (d) => {
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/**
 * Get date range presets: Today, Yesterday, Last 3 Days, Last 1 Week, This Month, Last Month, This Year, Last Year.
 */
export const getDatePresets = () => {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = today.getMonth();

  const todayStr = formatLocalDate(today);

  const yesterday = new Date(today);
  yesterday.setDate(today.getDate() - 1);
  const yesterdayStr = formatLocalDate(yesterday);

  const last3Days = new Date(today);
  last3Days.setDate(today.getDate() - 2);

  const last7Days = new Date(today);
  last7Days.setDate(today.getDate() - 6);

  const thisMonthStart = new Date(yyyy, mm, 1);
  const lastMonthStart = new Date(yyyy, mm - 1, 1);
  const lastMonthEnd = new Date(yyyy, mm, 0);

  const thisYearStart = new Date(yyyy, 0, 1);
  const lastYearStart = new Date(yyyy - 1, 0, 1);
  const lastYearEnd = new Date(yyyy - 1, 11, 31);

  return [
    { label: 'Today', startDate: todayStr, endDate: todayStr },
    { label: 'Yesterday', startDate: yesterdayStr, endDate: yesterdayStr },
    { label: 'Last 3 Days', startDate: formatLocalDate(last3Days), endDate: todayStr },
    { label: 'Last 1 Week', startDate: formatLocalDate(last7Days), endDate: todayStr },
    { label: 'This Month', startDate: formatLocalDate(thisMonthStart), endDate: todayStr },
    { label: 'Last Month', startDate: formatLocalDate(lastMonthStart), endDate: formatLocalDate(lastMonthEnd) },
    { label: 'This Year', startDate: formatLocalDate(thisYearStart), endDate: todayStr },
    { label: 'Last Year', startDate: formatLocalDate(lastYearStart), endDate: formatLocalDate(lastYearEnd) },
  ];
};
