import React, { useState, useEffect } from 'react';
import MainAdminHeader from '../../components/MainAdminHeader';
import categoryService from '../../services/categoryService';
import './Dashboard.css';

const CategoryManagement = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    type: 'IN',
    description: '',
  });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const data = await categoryService.getAllCategories();
      setCategories(data);
    } catch (err) {
      showAlert('error', err.response?.data?.message || 'Failed to load categories');
    } finally {
      setLoading(false);
    }
  };

  const showAlert = (type, message) => {
    setAlert({ type, message });
    setTimeout(() => setAlert(null), 4000);
  };

  const handleOpenAddModal = () => {
    setEditingCategory(null);
    setFormData({ name: '', type: 'IN', description: '' });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (cat) => {
    setEditingCategory(cat);
    setFormData({
      name: cat.name,
      type: cat.type,
      description: cat.description || '',
    });
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingCategory(null);
    setFormData({ name: '', type: 'IN', description: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      if (editingCategory) {
        await categoryService.updateCategory(editingCategory.id, formData);
        showAlert('success', `Category '${formData.name}' updated successfully!`);
      } else {
        await categoryService.createCategory(formData);
        showAlert('success', `Category '${formData.name}' created successfully!`);
      }
      handleCloseModal();
      fetchCategories();
    } catch (err) {
      showAlert('error', err.response?.data?.message || 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (cat) => {
    const nextStatus = !cat.active;
    const action = nextStatus ? 'activate' : 'deactivate';
    if (!window.confirm(`Are you sure you want to ${action} category '${cat.name}'?`)) return;

    try {
      await categoryService.toggleStatus(cat.id, nextStatus);
      showAlert('success', `Category '${cat.name}' is now ${nextStatus ? 'Active' : 'Inactive'}`);
      fetchCategories();
    } catch (err) {
      showAlert('error', err.response?.data?.message || 'Status update failed');
    }
  };

  const filteredCategories = categories.filter((cat) => {
    const matchesSearch =
      cat.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (cat.description && cat.description.toLowerCase().includes(searchTerm.toLowerCase()));

    if (filterType === 'IN') return matchesSearch && cat.type === 'IN';
    if (filterType === 'OUT') return matchesSearch && cat.type === 'OUT';
    if (filterType === 'ACTIVE') return matchesSearch && cat.active;
    if (filterType === 'INACTIVE') return matchesSearch && !cat.active;
    return matchesSearch;
  });

  return (
    <div className="dashboard-page">
      <MainAdminHeader />

      <main className="dashboard-content">
        <div className="section-header-row">
          <div className="section-title-group">
            <h2>🏷️ Transaction Categories</h2>
            <p>Manage standard categories for incoming income and outgoing expenses</p>
          </div>
          <div className="header-action-group">
            <button className="primary-btn" onClick={handleOpenAddModal}>
              <span>➕</span> Add Category
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
              placeholder="Search category name, description..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="filter-pills">
            <button
              className={`pill-btn ${filterType === 'ALL' ? 'active' : ''}`}
              onClick={() => setFilterType('ALL')}
            >
              All ({categories.length})
            </button>
            <button
              className={`pill-btn ${filterType === 'IN' ? 'active' : ''}`}
              onClick={() => setFilterType('IN')}
            >
              💚 Income / IN ({categories.filter((c) => c.type === 'IN').length})
            </button>
            <button
              className={`pill-btn ${filterType === 'OUT' ? 'active' : ''}`}
              onClick={() => setFilterType('OUT')}
            >
              🔻 Expense / OUT ({categories.filter((c) => c.type === 'OUT').length})
            </button>
            <button
              className={`pill-btn ${filterType === 'ACTIVE' ? 'active' : ''}`}
              onClick={() => setFilterType('ACTIVE')}
            >
              Active ({categories.filter((c) => c.active).length})
            </button>
          </div>
        </div>

        <div className="glass-table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Category Name</th>
                <th>Type</th>
                <th>Description</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="5" className="empty-table-msg">Loading categories...</td>
                </tr>
              ) : filteredCategories.length === 0 ? (
                <tr>
                  <td colSpan="5" className="empty-table-msg">No categories found</td>
                </tr>
              ) : (
                filteredCategories.map((cat) => (
                  <tr key={cat.id}>
                    <td>
                      <strong>{cat.name}</strong>
                    </td>
                    <td>
                      <span className={`type-pill ${cat.type === 'IN' ? 'in' : 'out'}`}>
                        {cat.type === 'IN' ? '💰 IN (Income)' : '💸 OUT (Expense)'}
                      </span>
                    </td>
                    <td>{cat.description || '—'}</td>
                    <td>
                      <span className={`status-pill ${cat.active ? 'active' : 'inactive'}`}>
                        ● {cat.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button
                          className="action-btn edit"
                          onClick={() => handleOpenEditModal(cat)}
                        >
                          ✏️ Edit
                        </button>
                        <button
                          className={`action-btn ${cat.active ? 'deactivate' : 'activate'}`}
                          onClick={() => handleToggleStatus(cat)}
                        >
                          {cat.active ? 'Deactivate' : 'Activate'}
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
              <h3>{editingCategory ? 'Edit Category' : 'Add New Category'}</h3>
              <button className="modal-close-btn" onClick={handleCloseModal}>✕</button>
            </div>
            <form onSubmit={handleSubmit} className="modal-form">
              <div className="form-group">
                <label>Category Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Student Fees, Electricity"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Category Type *</label>
                <select
                  value={formData.type}
                  onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                >
                  <option value="IN">IN — Money coming INTO college (Income)</option>
                  <option value="OUT">OUT — Money going OUT of college (Expense)</option>
                </select>
                <span className="form-help-text">
                  Defines whether funds categorized here represent college income or departmental expenditure.
                </span>
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  rows="3"
                  placeholder="Describe what transactions fall under this category..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="secondary-btn" onClick={handleCloseModal}>
                  Cancel
                </button>
                <button type="submit" className="primary-btn" disabled={submitting}>
                  {submitting ? 'Saving...' : editingCategory ? 'Update Category' : 'Create Category'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CategoryManagement;
