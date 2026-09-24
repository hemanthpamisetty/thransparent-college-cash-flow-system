import api from './api';

export const categoryService = {
  getAllCategories: async (type = null) => {
    const params = type ? { type } : {};
    const response = await api.get('/categories', { params });
    return response.data;
  },

  getCategoryById: async (id) => {
    const response = await api.get(`/categories/${id}`);
    return response.data;
  },

  createCategory: async (data) => {
    const response = await api.post('/categories', data);
    return response.data;
  },

  updateCategory: async (id, data) => {
    const response = await api.put(`/categories/${id}`, data);
    return response.data;
  },

  toggleStatus: async (id, active) => {
    const response = await api.patch(`/categories/${id}/status`, { active });
    return response.data;
  },
};

export default categoryService;
