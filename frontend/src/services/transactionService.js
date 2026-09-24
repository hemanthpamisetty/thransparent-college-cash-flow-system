import api from './api';

export const transactionService = {
  getTransactions: async (filters = {}) => {
    // Filter out undefined, null, or empty string params
    const cleanParams = Object.entries(filters).reduce((acc, [key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        acc[key] = value;
      }
      return acc;
    }, {});

    const response = await api.get('/transactions', { params: cleanParams });
    return response.data;
  },

  getTransactionById: async (id) => {
    const response = await api.get(`/transactions/${id}`);
    return response.data;
  },

  createTransaction: async (data) => {
    const response = await api.post('/transactions', data);
    return response.data;
  },

  updateTransaction: async (id, data) => {
    const response = await api.put(`/transactions/${id}`, data);
    return response.data;
  },

  deleteTransaction: async (id) => {
    const response = await api.delete(`/transactions/${id}`);
    return response.data;
  },

  getCashflowSummary: async (departmentId = null) => {
    const params = departmentId ? { departmentId } : {};
    const response = await api.get('/transactions/summary', { params });
    return response.data;
  },
};

export default transactionService;
