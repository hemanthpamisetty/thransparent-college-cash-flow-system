import api from './api';

export const departmentService = {
  getAllDepartments: async () => {
    const response = await api.get('/departments');
    return response.data;
  },

  getDepartmentById: async (id) => {
    const response = await api.get(`/departments/${id}`);
    return response.data;
  },

  createDepartment: async (data) => {
    const response = await api.post('/departments', data);
    return response.data;
  },

  updateDepartment: async (id, data) => {
    const response = await api.put(`/departments/${id}`, data);
    return response.data;
  },

  toggleStatus: async (id, active) => {
    const response = await api.patch(`/departments/${id}/status`, { active });
    return response.data;
  },
};

export default departmentService;
