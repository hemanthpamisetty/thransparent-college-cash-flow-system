import React, { useState, useEffect } from 'react';
import MainAdminHeader from '../../components/MainAdminHeader';
import userService from '../../services/userService';
import departmentService from '../../services/departmentService';
import './Dashboard.css';

const DeptAdminManagement = () => {
  const [deptAdmins, setDeptAdmins] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterDept, setFilterDept] = useState('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingAdmin, setEditingAdmin] = useState(null);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    fullName: '',
    email: '',
    departmentId: '',
  });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [adminsData, deptsData] = await Promise.all([
        userService.getAllDeptAdmins(),
        departmentService.getAllDepartments(),
      ]);
      setDeptAdmins(adminsData);
      setDepartments(deptsData);
    } catch (err) {
      showAlert('error', err.response?.data?.message || 'Failed to load department administrators');
    } finally {
      setLoading(false);
    }
  };

  const showAlert = (type, message) => {
    setAlert({ type, message });
    setTimeout(() => setAlert(null), 4000);
  };

  const handleOpenAddModal = () => {
    setEditingAdmin(null);
    const firstActiveDept = departments.find((d) => d.active);
    setFormData({
      username: '',
      password: '',
      fullName: '',
      email: '',
      departmentId: firstActiveDept ? firstActiveDept.id : '',
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (admin) => {
    setEditingAdmin(admin);
    setFormData({
      username: admin.username,
      password: '', // blank unless updating
      fullName: admin.fullName,
      email: admin.email || '',
      departmentId: admin.departmentId || '',
    });
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingAdmin(null);
    setFormData({
      username: '',
      password: '',
      fullName: '',
      email: '',
      departmentId: '',
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      if (editingAdmin) {
        const payload = {
          fullName: formData.fullName,
          email: formData.email,
          departmentId: Number(formData.departmentId),
        };
        if (formData.password) {
          payload.password = formData.password;
        }
        await userService.updateDeptAdmin(editingAdmin.id, payload);
        showAlert('success', `Admin '${formData.fullName}' updated successfully!`);
      } else {
        const payload = {
          ...formData,
          departmentId: Number(formData.departmentId),
        };
        await userService.createDeptAdmin(payload);
        showAlert('success', `Department Admin '${formData.username}' created successfully!`);
      }
      handleCloseModal();
      loadData();
    } catch (err) {
      showAlert('error', err.response?.data?.message || 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (admin) => {
    const nextStatus = !admin.active;
    const action = nextStatus ? 'activate' : 'deactivate';
    if (!window.confirm(`Are you sure you want to ${action} ${admin.fullName} (${admin.username})?`)) return;

    try {
      await userService.toggleDeptAdminStatus(admin.id, nextStatus);
      showAlert('success', `Admin '${admin.username}' is now ${nextStatus ? 'Active' : 'Inactive'}`);
      loadData();
    } catch (err) {
      showAlert('error', err.response?.data?.message || 'Status update failed');
    }
  };

  const activeDepartments = departments.filter((d) => d.active);

  const filteredAdmins = deptAdmins.filter((admin) => {
    const matchesSearch =
      admin.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      admin.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (admin.departmentName && admin.departmentName.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (admin.email && admin.email.toLowerCase().includes(searchTerm.toLowerCase()));

    if (filterDept !== 'ALL') {
      return matchesSearch && admin.departmentId === Number(filterDept);
    }
    return matchesSearch;
  });

  return (
    <div className="dashboard-page">
      <MainAdminHeader />

      <main className="dashboard-content">
        <div className="section-header-row">
          <div className="section-title-group">
            <h2>👥 Department Administrators</h2>
            <p>Manage credentials and assignments for department-level administrators</p>
          </div>
          <div className="header-action-group">
            <button className="primary-btn" onClick={handleOpenAddModal}>
              <span>➕</span> Add Dept Admin
            </button>
          </div>
        </div>

        {alert && (
          <div className={`alert-banner ${alert.type}`}>
            <span>{alert.message}</span>
            <button className="modal-close-btn" onClick={() => setAlert(null)}>✕</button>
          </div>
        )}

        <div className="filter-stats-bar">
          <div className="search-box">
            <input
              type="text"
              placeholder="Search by name, username, dept..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="form-group" style={{ margin: 0, minWidth: '200px' }}>
            <select
              value={filterDept}
              onChange={(e) => setFilterDept(e.target.value)}
              style={{ padding: '8px 12px', fontSize: '13px' }}
            >
              <option value="ALL">All Departments ({deptAdmins.length})</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name} ({d.code})
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="glass-table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Admin Name</th>
                <th>Username</th>
                <th>Department</th>
                <th>Email</th>
                <th>Parent Admin</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="7" className="empty-table-msg">Loading administrators...</td>
                </tr>
              ) : filteredAdmins.length === 0 ? (
                <tr>
                  <td colSpan="7" className="empty-table-msg">No department administrators found</td>
                </tr>
              ) : (
                filteredAdmins.map((admin) => (
                  <tr key={admin.id}>
                    <td>
                      <strong>{admin.fullName}</strong>
                    </td>
                    <td>
                      <span className="code-badge">{admin.username}</span>
                    </td>
                    <td>
                      <span className="type-pill in">
                        {admin.departmentName ? `${admin.departmentName} (${admin.departmentCode || ''})` : 'Unassigned'}
                      </span>
                    </td>
                    <td>{admin.email || '—'}</td>
                    <td>
                      <small style={{ color: 'rgba(255,255,255,0.5)' }}>
                        {admin.parentAdminUsername || 'admin'}
                      </small>
                    </td>
                    <td>
                      <span className={`status-pill ${admin.active ? 'active' : 'inactive'}`}>
                        ● {admin.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button
                          className="action-btn edit"
                          onClick={() => handleOpenEditModal(admin)}
                        >
                          ✏️ Edit
                        </button>
                        <button
                          className={`action-btn ${admin.active ? 'deactivate' : 'activate'}`}
                          onClick={() => handleToggleStatus(admin)}
                        >
                          {admin.active ? 'Deactivate' : 'Activate'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>

      {/* Add / Edit Modal */}
      {isModalOpen && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editingAdmin ? `Edit ${editingAdmin.username}` : 'Add Department Admin'}</h3>
              <button className="modal-close-btn" onClick={handleCloseModal}>✕</button>
            </div>
            <form onSubmit={handleSubmit} className="modal-form">
              {!editingAdmin && (
                <div className="form-group">
                  <label>Username *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. cse_admin"
                    value={formData.username}
                    onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  />
                  <span className="form-help-text">Unique username used for department login</span>
                </div>
              )}

              <div className="form-group">
                <label>{editingAdmin ? 'New Password (leave empty to keep current)' : 'Password *'}</label>
                <input
                  type="password"
                  required={!editingAdmin}
                  placeholder={editingAdmin ? 'Enter new password if changing' : 'e.g. Admin@123'}
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Full Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Dr. John Doe"
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Email Address</label>
                <input
                  type="email"
                  placeholder="e.g. admin.cse@college.edu"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Assigned Department *</label>
                <select
                  required
                  value={formData.departmentId}
                  onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
                >
                  <option value="" disabled>Select Department</option>
                  {activeDepartments.map((dept) => (
                    <option key={dept.id} value={dept.id}>
                      {dept.name} ({dept.code})
                    </option>
                  ))}
                </select>
                <span className="form-help-text">
                  Role: <strong>DEPT_ADMIN</strong> (assigned automatically)
                </span>
              </div>

              <div className="modal-actions">
                <button type="button" className="secondary-btn" onClick={handleCloseModal}>
                  Cancel
                </button>
                <button type="submit" className="primary-btn" disabled={submitting}>
                  {submitting ? 'Saving...' : editingAdmin ? 'Update Admin' : 'Create Admin'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default DeptAdminManagement;
