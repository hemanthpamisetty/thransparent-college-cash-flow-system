import api from './api';

export const userService = {
  getAllDeptAdmins: async () => {
    const response = await api.get('/users/dept-admins');
    return response.data;
  },

  getDeptAdminById: async (id) => {
    const response = await api.get(`/users/dept-admins/${id}`);
    return response.data;
  },

  createDeptAdmin: async (data) => {
    const response = await api.post('/users/dept-admin', data);
    return response.data;
  },

  updateDeptAdmin: async (id, data) => {
    const response = await api.put(`/users/dept-admins/${id}`, data);
    return response.data;
  },

  toggleDeptAdminStatus: async (id, active) => {
    const response = await api.patch(`/users/dept-admins/${id}/status`, { active });
    return response.data;
  },
};

export default userService;
