import React, { useState, useEffect } from 'react';
import MainAdminHeader from '../../components/MainAdminHeader';
import departmentService from '../../services/departmentService';
import './Dashboard.css';

const DepartmentManagement = () => {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingDept, setEditingDept] = useState(null);
  const [formData, setFormData] = useState({ name: '', code: '', description: '' });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchDepartments();
  }, []);

  const fetchDepartments = async () => {
    try {
      setLoading(true);
      const data = await departmentService.getAllDepartments();
      setDepartments(data);
    } catch (err) {
      showAlert('error', err.response?.data?.message || 'Failed to load departments');
    } finally {
      setLoading(false);
    }
  };

  const showAlert = (type, message) => {
    setAlert({ type, message });
    setTimeout(() => setAlert(null), 4000);
  };

  const handleOpenAddModal = () => {
    setEditingDept(null);
    setFormData({ name: '', code: '', description: '' });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (dept) => {
    setEditingDept(dept);
    setFormData({
      name: dept.name,
      code: dept.code,
      description: dept.description || '',
    });
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingDept(null);
    setFormData({ name: '', code: '', description: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      if (editingDept) {
        await departmentService.updateDepartment(editingDept.id, formData);
        showAlert('success', `Department '${formData.name}' updated successfully!`);
      } else {
        await departmentService.createDepartment(formData);
        showAlert('success', `Department '${formData.name}' created successfully!`);
      }
      handleCloseModal();
      fetchDepartments();
    } catch (err) {
      showAlert('error', err.response?.data?.message || 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (dept) => {
    const nextStatus = !dept.active;
    const action = nextStatus ? 'activate' : 'deactivate';
    if (!window.confirm(`Are you sure you want to ${action} ${dept.name}?`)) return;

    try {
      await departmentService.toggleStatus(dept.id, nextStatus);
      showAlert('success', `Department '${dept.name}' is now ${nextStatus ? 'Active' : 'Inactive'}`);
      fetchDepartments();
    } catch (err) {
      showAlert('error', err.response?.data?.message || 'Status update failed');
    }
  };

  const filteredDepartments = departments.filter((dept) => {
    const matchesSearch =
      dept.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      dept.code.toLowerCase().includes(searchTerm.toLowerCase());
    if (filterStatus === 'ACTIVE') return matchesSearch && dept.active;
    if (filterStatus === 'INACTIVE') return matchesSearch && !dept.active;
    return matchesSearch;
  });

  return (
    <div className="dashboard-page">
      <MainAdminHeader />

      <main className="dashboard-content">
        <div className="section-header-row">
          <div className="section-title-group">
            <h2>🏛️ Department Management</h2>
            <p>Create, update, and manage college academic & administrative departments</p>
          </div>
          <div className="header-action-group">
            <button className="primary-btn" onClick={handleOpenAddModal}>
              <span>➕</span> Add Department
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
              placeholder="Search by name or code..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="filter-pills">
            <button
              className={`pill-btn ${filterStatus === 'ALL' ? 'active' : ''}`}
              onClick={() => setFilterStatus('ALL')}
            >
              All ({departments.length})
            </button>
            <button
              className={`pill-btn ${filterStatus === 'ACTIVE' ? 'active' : ''}`}
              onClick={() => setFilterStatus('ACTIVE')}
            >
              Active ({departments.filter((d) => d.active).length})
            </button>
            <button
              className={`pill-btn ${filterStatus === 'INACTIVE' ? 'active' : ''}`}
              onClick={() => setFilterStatus('INACTIVE')}
            >
              Inactive ({departments.filter((d) => !d.active).length})
            </button>
          </div>
        </div>

        <div className="glass-table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Department Name</th>
                <th>Description</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="5" className="empty-table-msg">Loading departments...</td>
                </tr>
              ) : filteredDepartments.length === 0 ? (
                <tr>
                  <td colSpan="5" className="empty-table-msg">No departments found</td>
                </tr>
              ) : (
                filteredDepartments.map((dept) => (
                  <tr key={dept.id}>
                    <td>
                      <span className="code-badge">{dept.code}</span>
                    </td>
                    <td>
                      <strong>{dept.name}</strong>
                    </td>
                    <td>{dept.description || '—'}</td>
                    <td>
                      <span className={`status-pill ${dept.active ? 'active' : 'inactive'}`}>
                        ● {dept.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button
                          className="action-btn edit"
                          onClick={() => handleOpenEditModal(dept)}
                        >
                          ✏️ Edit
                        </button>
                        <button
                          className={`action-btn ${dept.active ? 'deactivate' : 'activate'}`}
                          onClick={() => handleToggleStatus(dept)}
                        >
                          {dept.active ? 'Deactivate' : 'Activate'}
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
              <h3>{editingDept ? 'Edit Department' : 'Add New Department'}</h3>
              <button className="modal-close-btn" onClick={handleCloseModal}>✕</button>
            </div>
            <form onSubmit={handleSubmit} className="modal-form">
              <div className="form-group">
                <label>Department Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Computer Science & Engineering"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Department Code *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. CSE"
                  value={formData.code}
                  onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  rows="3"
                  placeholder="Brief description about the department..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="secondary-btn" onClick={handleCloseModal}>
                  Cancel
                </button>
                <button type="submit" className="primary-btn" disabled={submitting}>
                  {submitting ? 'Saving...' : editingDept ? 'Update Department' : 'Create Department'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default DepartmentManagement;
