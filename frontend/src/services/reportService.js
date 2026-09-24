import api from './api';

export const reportService = {
  /**
   * Get daily cashflow trend (income vs expense per day).
   */
  getCashflowTrend: async (startDate, endDate, departmentId = null) => {
    const params = { startDate, endDate };
    if (departmentId) params.departmentId = departmentId;
    const response = await api.get('/reports/cashflow-trend', { params });
    return response.data;
  },

  /**
   * Get income/expense totals grouped by category.
   */
  getCategoryBreakdown: async (startDate, endDate, departmentId = null, transactionType = null) => {
    const params = { startDate, endDate };
    if (departmentId) params.departmentId = departmentId;
    if (transactionType) params.transactionType = transactionType;
    const response = await api.get('/reports/category-breakdown', { params });
    return response.data;
  },

  /**
   * Get per-department income/expense summary (Main Admin only).
   */
  getDepartmentSummary: async (startDate, endDate) => {
    const params = { startDate, endDate };
    const response = await api.get('/reports/department-summary', { params });
    return response.data;
  },

  /**
   * Get detailed daily summary for a specific date.
   */
  getDailySummary: async (date, departmentId = null) => {
    const params = { date };
    if (departmentId) params.departmentId = departmentId;
    const response = await api.get('/reports/daily-summary', { params });
    return response.data;
  },

  /**
   * Get monthly summary for a given year.
   */
  getMonthlySummary: async (year, departmentId = null) => {
    const params = { year };
    if (departmentId) params.departmentId = departmentId;
    const response = await api.get('/reports/monthly-summary', { params });
    return response.data;
  },
};

export default reportService;
