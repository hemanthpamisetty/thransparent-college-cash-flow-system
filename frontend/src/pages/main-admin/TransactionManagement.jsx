import React, { useState, useEffect } from 'react';
import MainAdminHeader from '../../components/MainAdminHeader';
import transactionService from '../../services/transactionService';
import departmentService from '../../services/departmentService';
import categoryService from '../../services/categoryService';
import { TRANSACTION_TYPES, PAYMENT_METHODS } from '../../utils/constants';
import { getDatePresets } from '../../utils/exportUtils';
import './Dashboard.css';

const TransactionManagement = () => {
  const [transactions, setTransactions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [categories, setCategories] = useState([]);
  const [summary, setSummary] = useState({
    totalIncome: 0,
    totalExpense: 0,
    netBalance: 0,
    totalTransactions: 0,
    incomeCount: 0,
    expenseCount: 0,
  });

  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);

  // Filters state
  const [filters, setFilters] = useState({
    search: '',
    departmentId: '',
    transactionType: '',
    categoryId: '',
    paymentMethod: '',
    startDate: '',
    endDate: '',
  });

  // Modal states
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [viewingTransaction, setViewingTransaction] = useState(null);

  const initialFormData = {
    transactionType: TRANSACTION_TYPES.IN,
    amount: '',
    departmentId: '',
    categoryId: '',
    paymentMethod: 'BANK_TRANSFER',
    transactionDate: new Date().toISOString().split('T')[0],
    referenceNumber: '',
    description: '',
  };

  const [formData, setFormData] = useState(initialFormData);
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadInitialData();
  }, []);

  useEffect(() => {
    fetchTransactions();
  }, [filters]);

  const loadInitialData = async () => {
    try {
      const [deptsData, catsData] = await Promise.all([
        departmentService.getAllDepartments().catch(() => []),
        categoryService.getAllCategories().catch(() => []),
      ]);
      setDepartments(deptsData);
      setCategories(catsData);
      await fetchSummary();
    } catch (err) {
      console.error('Error loading initial data:', err);
    }
  };

  const fetchSummary = async () => {
    try {
      const summaryData = await transactionService.getCashflowSummary(
        filters.departmentId ? Number(filters.departmentId) : null
      );
      setSummary(summaryData);
    } catch (err) {
      console.error('Error fetching summary:', err);
    }
  };

  const fetchTransactions = async () => {
    setLoading(true);
    try {
      const data = await transactionService.getTransactions({
        search: filters.search,
        departmentId: filters.departmentId ? Number(filters.departmentId) : undefined,
        transactionType: filters.transactionType || undefined,
        categoryId: filters.categoryId ? Number(filters.categoryId) : undefined,
        paymentMethod: filters.paymentMethod || undefined,
        startDate: filters.startDate || undefined,
        endDate: filters.endDate || undefined,
      });
      setTransactions(data);
      await fetchSummary();
    } catch (err) {
      showAlert('Failed to load transactions: ' + (err.response?.data?.message || err.message), 'error');
    } finally {
      setLoading(false);
    }
  };

  const showAlert = (message, type = 'success') => {
    setAlert({ message, type });
    setTimeout(() => setAlert(null), 5000);
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({
      ...prev,
      [name]: value,
      // Reset category filter if transaction type changes and category belongs to another type
      ...(name === 'transactionType' ? { categoryId: '' } : {}),
    }));
  };

  const datePresets = getDatePresets();

  const handleDatePreset = (preset) => {
    setFilters((prev) => ({
      ...prev,
      startDate: preset.startDate,
      endDate: preset.endDate,
    }));
  };

  const handleResetFilters = () => {
    setFilters({
      search: '',
      departmentId: '',
      transactionType: '',
      categoryId: '',
      paymentMethod: '',
      startDate: '',
      endDate: '',
    });
  };

  const openCreateModal = (type = TRANSACTION_TYPES.IN) => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      ...initialFormData,
      transactionType: type,
      departmentId: departments[0]?.id || '',
    });
    setFormError('');
    setShowModal(true);
  };

  const openEditModal = (tx) => {
    setIsEditing(true);
    setEditingId(tx.id);
    setFormData({
      transactionType: tx.transactionType,
      amount: tx.amount,
      departmentId: tx.departmentId,
      categoryId: tx.categoryId,
      paymentMethod: tx.paymentMethod,
      transactionDate: tx.transactionDate,
      referenceNumber: tx.referenceNumber || '',
      description: tx.description || '',
    });
    setFormError('');
    setShowModal(true);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
      ...(name === 'transactionType' ? { categoryId: '' } : {}),
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');

    if (!formData.amount || Number(formData.amount) <= 0) {
      setFormError('Please enter a valid amount greater than 0.');
      return;
    }
    if (!formData.departmentId) {
      setFormError('Please select a department.');
      return;
    }
    if (!formData.categoryId) {
      setFormError('Please select a category.');
      return;
    }
    if (!formData.transactionDate) {
      setFormError('Please select a transaction date.');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        transactionType: formData.transactionType,
        amount: parseFloat(formData.amount),
        departmentId: Number(formData.departmentId),
        categoryId: Number(formData.categoryId),
        paymentMethod: formData.paymentMethod,
        transactionDate: formData.transactionDate,
        referenceNumber: formData.referenceNumber || null,
        description: formData.description || null,
      };

      if (isEditing) {
        await transactionService.updateTransaction(editingId, payload);
        showAlert('Transaction updated successfully.');
      } else {
        await transactionService.createTransaction(payload);
        showAlert(`Recorded Money ${formData.transactionType} of ₹${formData.amount} successfully.`);
      }

      setShowModal(false);
      fetchTransactions();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Operation failed. Please check your inputs.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (tx) => {
    if (!window.confirm(`Are you sure you want to delete transaction #${tx.id} (₹${tx.amount})?`)) {
      return;
    }

    try {
      await transactionService.deleteTransaction(tx.id);
      showAlert('Transaction deleted successfully.');
      fetchTransactions();
    } catch (err) {
      showAlert('Failed to delete transaction: ' + (err.response?.data?.message || err.message), 'error');
    }
  };

  // Available categories matching current form transaction type
  const activeCategoriesForForm = categories.filter(
    (c) => c.active && c.type === formData.transactionType
  );

  // Available categories for filter dropdown
  const filterCategories = filters.transactionType
    ? categories.filter((c) => c.type === filters.transactionType)
    : categories;

  const formatCurrency = (val) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(val || 0);
  };

  return (
    <div className="dashboard-page">
      <MainAdminHeader />

      <main className="dashboard-content">
        {/* Section Header */}
        <div className="section-header-row">
          <div className="section-title-group">
            <h2>Financial Transactions & Cashflow</h2>
            <p>Monitor college-wide Money IN (Income) and Money OUT (Expense) cashflows in real-time.</p>
          </div>
          <div className="header-action-group">
            <button className="primary-btn" onClick={() => openCreateModal(TRANSACTION_TYPES.IN)}>
              <span>➕</span> Record Money IN
            </button>
            <button
              className="primary-btn"
              style={{ background: 'linear-gradient(135deg, #ff4757 0%, #d63031 100%)' }}
              onClick={() => openCreateModal(TRANSACTION_TYPES.OUT)}
            >
              <span>➖</span> Record Money OUT
            </button>
          </div>
        </div>

        {/* Alert Banner */}
        {alert && (
          <div className={`alert-banner ${alert.type}`}>
            <span>{alert.message}</span>
            <button className="modal-close-btn" onClick={() => setAlert(null)}>✕</button>
          </div>
        )}

        {/* Cashflow KPI Metric Cards */}
        <div className="metrics-row">
          <div className="metric-card income">
            <div className="metric-icon">📥</div>
            <div className="metric-data">
              <span className="metric-label">Total Money IN</span>
              <span className="metric-value">{formatCurrency(summary.totalIncome)}</span>
              <span className="metric-subtitle">{summary.incomeCount || 0} income entries</span>
            </div>
          </div>

          <div className="metric-card expense">
            <div className="metric-icon">📤</div>
            <div className="metric-data">
              <span className="metric-label">Total Money OUT</span>
              <span className="metric-value">{formatCurrency(summary.totalExpense)}</span>
              <span className="metric-subtitle">{summary.expenseCount || 0} expense entries</span>
            </div>
          </div>

          <div className="metric-card balance">
            <div className="metric-icon">⚖️</div>
            <div className="metric-data">
              <span className="metric-label">Net Cash Balance</span>
              <span className="metric-value">{formatCurrency(summary.netBalance)}</span>
              <span className="metric-subtitle">{summary.totalTransactions || 0} total transactions</span>
            </div>
          </div>
        </div>

        {/* Comprehensive Filter Panel */}
        <div className="advanced-filter-panel">
          <div className="filter-grid">
            <div className="filter-item">
              <label>Search Keyword</label>
              <input
                type="text"
                name="search"
                placeholder="Description / Ref no..."
                value={filters.search}
                onChange={handleFilterChange}
              />
            </div>

            <div className="filter-item">
              <label>Department</label>
              <select name="departmentId" value={filters.departmentId} onChange={handleFilterChange}>
                <option value="">All Departments</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.code} — {d.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-item">
              <label>Transaction Type</label>
              <select name="transactionType" value={filters.transactionType} onChange={handleFilterChange}>
                <option value="">All Types</option>
                <option value={TRANSACTION_TYPES.IN}>Money IN (Income)</option>
                <option value={TRANSACTION_TYPES.OUT}>Money OUT (Expense)</option>
              </select>
            </div>

            <div className="filter-item">
              <label>Category</label>
              <select name="categoryId" value={filters.categoryId} onChange={handleFilterChange}>
                <option value="">All Categories</option>
                {filterCategories.map((c) => (
                  <option key={c.id} value={c.id}>
                    [{c.type}] {c.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-item">
              <label>Payment Method</label>
              <select name="paymentMethod" value={filters.paymentMethod} onChange={handleFilterChange}>
                <option value="">All Methods</option>
                {PAYMENT_METHODS.map((pm) => (
                  <option key={pm.value} value={pm.value}>
                    {pm.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-item">
              <label>From Date</label>
              <input
                type="date"
                name="startDate"
                value={filters.startDate}
                onChange={handleFilterChange}
              />
            </div>

            <div className="filter-item">
              <label>To Date</label>
              <input
                type="date"
                name="endDate"
                value={filters.endDate}
                onChange={handleFilterChange}
              />
            </div>
          </div>

          {/* Quick Date Presets Row */}
          <div className="filter-preset-row" style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap', marginTop: '14px', paddingTop: '12px', borderTop: '1px solid rgba(255, 255, 255, 0.08)' }}>
            <span style={{ fontSize: '11px', color: 'rgba(255, 255, 255, 0.5)', textTransform: 'uppercase', letterSpacing: '0.4px', fontWeight: 600 }}>Quick Timeframes:</span>
            {datePresets.map((p) => {
              const isActive = filters.startDate === p.startDate && filters.endDate === p.endDate;
              return (
                <button
                  key={p.label}
                  type="button"
                  style={{
                    background: isActive ? 'rgba(79, 140, 255, 0.3)' : 'rgba(255, 255, 255, 0.06)',
                    border: `1px solid ${isActive ? '#4f8cff' : 'rgba(255, 255, 255, 0.12)'}`,
                    color: isActive ? '#ffffff' : 'rgba(255, 255, 255, 0.75)',
                    padding: '4px 12px',
                    borderRadius: '16px',
                    fontSize: '11px',
                    fontWeight: isActive ? 600 : 500,
                    cursor: 'pointer',
                    transition: 'all 0.2s ease',
                  }}
                  onClick={() => handleDatePreset(p)}
                >
                  {p.label}
                </button>
              );
            })}
            {(filters.startDate || filters.endDate) && (
              <button
                type="button"
                style={{
                  background: 'none',
                  border: 'none',
                  color: '#ff6b7a',
                  fontSize: '11px',
                  fontWeight: 600,
                  cursor: 'pointer',
                  marginLeft: 'auto',
                }}
                onClick={() => setFilters((prev) => ({ ...prev, startDate: '', endDate: '' }))}
              >
                ✕ Clear Dates
              </button>
            )}
          </div>

          <div className="filter-actions">
            <span style={{ fontSize: '12px', color: 'rgba(255, 255, 255, 0.6)' }}>
              Showing <strong>{transactions.length}</strong> matching transactions
            </span>
            <button className="secondary-btn" onClick={handleResetFilters}>
              Reset Filters
            </button>
          </div>
        </div>

        {/* Transactions Table */}
        <div className="glass-table-container">
          {loading ? (
            <div className="empty-table-msg">Loading transactions...</div>
          ) : transactions.length === 0 ? (
            <div className="empty-table-msg">
              No transactions match the selected filter criteria.
            </div>
          ) : (
            <table className="custom-table">
              <thead>
                <tr>
                  <th>ID / Date</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Category</th>
                  <th>Department</th>
                  <th>Payment Method</th>
                  <th>Reference No.</th>
                  <th>Recorded By</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((tx) => (
                  <tr key={tx.id}>
                    <td>
                      <div>
                        <strong>#{tx.id}</strong>
                        <div style={{ fontSize: '11px', color: 'rgba(255,255,255,0.45)' }}>
                          {tx.transactionDate}
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className={`type-pill ${tx.transactionType === 'IN' ? 'in' : 'out'}`}>
                        {tx.transactionType === 'IN' ? '↓ MONEY IN' : '↑ MONEY OUT'}
                      </span>
                    </td>
                    <td>
                      <span className={`amount-text ${tx.transactionType === 'IN' ? 'income' : 'expense'}`}>
                        {tx.transactionType === 'IN' ? '+' : '-'}{formatCurrency(tx.amount)}
                      </span>
                    </td>
                    <td>
                      <strong>{tx.categoryName}</strong>
                    </td>
                    <td>
                      <span className="code-badge">{tx.departmentCode}</span>
                    </td>
                    <td>
                      <span className="payment-tag">{tx.paymentMethod}</span>
                    </td>
                    <td>
                      <span style={{ fontFamily: 'monospace', fontSize: '12px' }}>
                        {tx.referenceNumber || '—'}
                      </span>
                    </td>
                    <td>
                      <span style={{ fontSize: '12px' }}>{tx.createdByFullName || tx.createdByUsername}</span>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button
                          className="btn-icon-view"
                          title="View Receipt"
                          onClick={() => setViewingTransaction(tx)}
                        >
                          👁️ View
                        </button>
                        <button
                          className="action-btn edit"
                          title="Edit"
                          onClick={() => openEditModal(tx)}
                        >
                          Edit
                        </button>
                        <button
                          className="btn-icon-danger"
                          title="Delete"
                          onClick={() => handleDelete(tx)}
                        >
                          ✕
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </main>

      {/* Record / Edit Transaction Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '560px' }}>
            <div className="modal-header">
              <h3>
                {isEditing
                  ? `Edit Transaction #${editingId}`
                  : `Record New Money ${formData.transactionType}`}
              </h3>
              <button className="modal-close-btn" onClick={() => setShowModal(false)}>✕</button>
            </div>

            <form onSubmit={handleSubmit} className="modal-form">
              {formError && <div className="alert-banner error">{formError}</div>}

              {/* Transaction Type Radio Selector */}
              <div className="form-group">
                <label>Transaction Flow Type *</label>
                <div style={{ display: 'flex', gap: '12px', marginTop: '4px' }}>
                  <button
                    type="button"
                    className={`pill-btn ${formData.transactionType === TRANSACTION_TYPES.IN ? 'active' : ''}`}
                    style={{ flex: 1, padding: '10px', fontSize: '13px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}
                    onClick={() => handleFormChange({ target: { name: 'transactionType', value: TRANSACTION_TYPES.IN } })}
                  >
                    <span>📥</span> Money IN (Income)
                  </button>
                  <button
                    type="button"
                    className={`pill-btn ${formData.transactionType === TRANSACTION_TYPES.OUT ? 'active' : ''}`}
                    style={{
                      flex: 1,
                      padding: '10px',
                      fontSize: '13px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '6px',
                      background: formData.transactionType === TRANSACTION_TYPES.OUT ? '#ff4757' : '',
                      borderColor: formData.transactionType === TRANSACTION_TYPES.OUT ? '#ff4757' : '',
                    }}
                    onClick={() => handleFormChange({ target: { name: 'transactionType', value: TRANSACTION_TYPES.OUT } })}
                  >
                    <span>📤</span> Money OUT (Expense)
                  </button>
                </div>
              </div>

              {/* Amount & Date in Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="form-group">
                  <label>Amount (₹) *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    name="amount"
                    placeholder="e.g. 25000"
                    value={formData.amount}
                    onChange={handleFormChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Transaction Date *</label>
                  <input
                    type="date"
                    name="transactionDate"
                    value={formData.transactionDate}
                    onChange={handleFormChange}
                    required
                  />
                </div>
              </div>

              {/* Department & Category in Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="form-group">
                  <label>Department *</label>
                  <select
                    name="departmentId"
                    value={formData.departmentId}
                    onChange={handleFormChange}
                    required
                  >
                    <option value="">Select Department</option>
                    {departments.filter((d) => d.active).map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.code} — {d.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Category *</label>
                  <select
                    name="categoryId"
                    value={formData.categoryId}
                    onChange={handleFormChange}
                    required
                  >
                    <option value="">Select Category</option>
                    {activeCategoriesForForm.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Payment Method & Reference No */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="form-group">
                  <label>Payment Method *</label>
                  <select
                    name="paymentMethod"
                    value={formData.paymentMethod}
                    onChange={handleFormChange}
                    required
                  >
                    {PAYMENT_METHODS.map((pm) => (
                      <option key={pm.value} value={pm.value}>
                        {pm.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Reference / Voucher / UTR No.</label>
                  <input
                    type="text"
                    name="referenceNumber"
                    placeholder="e.g. UTR-9837428 or CHQ-0012"
                    value={formData.referenceNumber}
                    onChange={handleFormChange}
                  />
                </div>
              </div>

              {/* Description */}
              <div className="form-group">
                <label>Description / Notes</label>
                <textarea
                  rows="2"
                  name="description"
                  placeholder="Details regarding this cashflow transaction..."
                  value={formData.description}
                  onChange={handleFormChange}
                />
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="secondary-btn"
                  onClick={() => setShowModal(false)}
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="primary-btn"
                  disabled={submitting}
                  style={
                    formData.transactionType === TRANSACTION_TYPES.OUT
                      ? { background: 'linear-gradient(135deg, #ff4757 0%, #d63031 100%)' }
                      : {}
                  }
                >
                  {submitting ? 'Saving...' : isEditing ? 'Update Transaction' : `Save Money ${formData.transactionType}`}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Transaction Receipt View Modal */}
      {viewingTransaction && (
        <div className="modal-overlay" onClick={() => setViewingTransaction(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '520px' }}>
            <div className="modal-header">
              <h3>Transaction Receipt Voucher</h3>
              <button className="modal-close-btn" onClick={() => setViewingTransaction(null)}>✕</button>
            </div>

            <div style={{ padding: '24px' }}>
              <div className="receipt-card">
                <div className="receipt-row">
                  <span className="receipt-label">Voucher / ID</span>
                  <span className="receipt-val">#{viewingTransaction.id}</span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label">Flow Type</span>
                  <span className="receipt-val">
                    <span className={`type-pill ${viewingTransaction.transactionType === 'IN' ? 'in' : 'out'}`}>
                      {viewingTransaction.transactionType === 'IN' ? 'MONEY IN (INCOME)' : 'MONEY OUT (EXPENSE)'}
                    </span>
                  </span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label">Amount</span>
                  <span
                    className={`receipt-val amount-text ${
                      viewingTransaction.transactionType === 'IN' ? 'income' : 'expense'
                    }`}
                    style={{ fontSize: '18px' }}
                  >
                    {formatCurrency(viewingTransaction.amount)}
                  </span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label">Department</span>
                  <span className="receipt-val">
                    {viewingTransaction.departmentName} ({viewingTransaction.departmentCode})
                  </span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label">Category</span>
                  <span className="receipt-val">{viewingTransaction.categoryName}</span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label">Payment Mode</span>
                  <span className="receipt-val">{viewingTransaction.paymentMethod}</span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label">Reference No.</span>
                  <span className="receipt-val" style={{ fontFamily: 'monospace' }}>
                    {viewingTransaction.referenceNumber || 'N/A'}
                  </span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label">Transaction Date</span>
                  <span className="receipt-val">{viewingTransaction.transactionDate}</span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label">Recorded By</span>
                  <span className="receipt-val">
                    {viewingTransaction.createdByFullName} (@{viewingTransaction.createdByUsername})
                  </span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label">System Timestamp</span>
                  <span className="receipt-val" style={{ fontSize: '11px', color: 'rgba(255,255,255,0.6)' }}>
                    {viewingTransaction.createdAt ? new Date(viewingTransaction.createdAt).toLocaleString() : '—'}
                  </span>
                </div>

                {viewingTransaction.description && (
                  <div style={{ marginTop: '8px', paddingTop: '8px', borderTop: '1px dashed rgba(255,255,255,0.08)' }}>
                    <span className="receipt-label" style={{ display: 'block', marginBottom: '4px' }}>
                      Notes / Description:
                    </span>
                    <p style={{ margin: 0, fontSize: '13px', color: 'rgba(255,255,255,0.85)', lineHeight: 1.4 }}>
                      {viewingTransaction.description}
                    </p>
                  </div>
                )}
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '16px', gap: '10px' }}>
                <button className="primary-btn" onClick={() => window.print()}>
                  🖨️ Print Receipt
                </button>
                <button className="secondary-btn" onClick={() => setViewingTransaction(null)}>
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TransactionManagement;
