import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import DeptAdminHeader from '../../components/DeptAdminHeader';
import transactionService from '../../services/transactionService';
import categoryService from '../../services/categoryService';
import { TRANSACTION_TYPES, PAYMENT_METHODS } from '../../utils/constants';
import { getDatePresets } from '../../utils/exportUtils';
import '../main-admin/Dashboard.css';

const DeptTransactionManagement = () => {
  const { user } = useAuth();
  const [transactions, setTransactions] = useState([]);
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

  // Filters
  const [filters, setFilters] = useState({
    search: '',
    transactionType: '',
    categoryId: '',
    paymentMethod: '',
    startDate: '',
    endDate: '',
  });

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [viewingTransaction, setViewingTransaction] = useState(null);

  const initialFormData = {
    transactionType: TRANSACTION_TYPES.IN,
    amount: '',
    categoryId: '',
    paymentMethod: 'UPI',
    transactionDate: new Date().toISOString().split('T')[0],
    referenceNumber: '',
    description: '',
  };

  const [formData, setFormData] = useState(initialFormData);
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    fetchDeptData();
  }, [filters]);

  const loadCategories = async () => {
    try {
      const catsData = await categoryService.getAllCategories();
      setCategories(catsData);
    } catch (err) {
      console.error('Error loading categories:', err);
    }
  };

  const fetchDeptData = async () => {
    setLoading(true);
    try {
      const [txData, summaryData] = await Promise.all([
        transactionService.getTransactions({
          search: filters.search,
          transactionType: filters.transactionType || undefined,
          categoryId: filters.categoryId ? Number(filters.categoryId) : undefined,
          paymentMethod: filters.paymentMethod || undefined,
          startDate: filters.startDate || undefined,
          endDate: filters.endDate || undefined,
        }),
        transactionService.getCashflowSummary(),
      ]);
      setTransactions(txData);
      setSummary(summaryData);
    } catch (err) {
      showAlert('Failed to load department transactions: ' + (err.response?.data?.message || err.message), 'error');
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
        categoryId: Number(formData.categoryId),
        paymentMethod: formData.paymentMethod,
        transactionDate: formData.transactionDate,
        referenceNumber: formData.referenceNumber || null,
        description: formData.description || null,
      };

      if (isEditing) {
        await transactionService.updateTransaction(editingId, payload);
        showAlert('Department transaction updated successfully.');
      } else {
        await transactionService.createTransaction(payload);
        showAlert(`Recorded Money ${formData.transactionType} of ₹${formData.amount} successfully.`);
      }

      setShowModal(false);
      fetchDeptData();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Operation failed. Please verify your entries.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (tx) => {
    if (!window.confirm(`Are you sure you want to delete department transaction #${tx.id} (₹${tx.amount})?`)) {
      return;
    }

    try {
      await transactionService.deleteTransaction(tx.id);
      showAlert('Transaction deleted successfully.');
      fetchDeptData();
    } catch (err) {
      showAlert('Failed to delete transaction: ' + (err.response?.data?.message || err.message), 'error');
    }
  };

  const activeCategoriesForForm = categories.filter(
    (c) => c.active && c.type === formData.transactionType
  );

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
      <DeptAdminHeader />

      <main className="dashboard-content">
        <div className="section-header-row">
          <div className="section-title-group">
            <h2>{user?.departmentName || 'Department'} Financial Ledger</h2>
            <p>Track all income and expenditure activities for your assigned department.</p>
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

        {alert && (
          <div className={`alert-banner ${alert.type}`}>
            <span>{alert.message}</span>
            <button className="modal-close-btn" onClick={() => setAlert(null)}>✕</button>
          </div>
        )}

        {/* Department Cashflow KPI Cards */}
        <div className="metrics-row">
          <div className="metric-card income">
            <div className="metric-icon">📥</div>
            <div className="metric-data">
              <span className="metric-label">Department Income</span>
              <span className="metric-value">{formatCurrency(summary.totalIncome)}</span>
              <span className="metric-subtitle">{summary.incomeCount || 0} income entries</span>
            </div>
          </div>

          <div className="metric-card expense">
            <div className="metric-icon">📤</div>
            <div className="metric-data">
              <span className="metric-label">Department Expenses</span>
              <span className="metric-value">{formatCurrency(summary.totalExpense)}</span>
              <span className="metric-subtitle">{summary.expenseCount || 0} expense entries</span>
            </div>
          </div>

          <div className="metric-card balance">
            <div className="metric-icon">⚖️</div>
            <div className="metric-data">
              <span className="metric-label">Department Balance</span>
              <span className="metric-value">{formatCurrency(summary.netBalance)}</span>
              <span className="metric-subtitle">{summary.totalTransactions || 0} total transactions</span>
            </div>
          </div>
        </div>

        {/* Filter Bar */}
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
              Showing <strong>{transactions.length}</strong> department transactions
            </span>
            <button className="secondary-btn" onClick={handleResetFilters}>
              Reset Filters
            </button>
          </div>
        </div>

        {/* Transactions Table */}
        <div className="glass-table-container">
          {loading ? (
            <div className="empty-table-msg">Loading department ledger...</div>
          ) : transactions.length === 0 ? (
            <div className="empty-table-msg">
              No transactions recorded yet for this department. Click "Record Money IN" or "Record Money OUT" to add one!
            </div>
          ) : (
            <table className="custom-table">
              <thead>
                <tr>
                  <th>ID / Date</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Category</th>
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
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '540px' }}>
            <div className="modal-header">
              <h3>
                {isEditing
                  ? `Edit Department Transaction #${editingId}`
                  : `Record Money ${formData.transactionType} — ${user?.departmentName || 'Department'}`}
              </h3>
              <button className="modal-close-btn" onClick={() => setShowModal(false)}>✕</button>
            </div>

            <form onSubmit={handleSubmit} className="modal-form">
              {formError && <div className="alert-banner error">{formError}</div>}

              {/* Transaction Type Radio Selector */}
              <div className="form-group">
                <label>Flow Type *</label>
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

              {/* Amount & Date */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="form-group">
                  <label>Amount (₹) *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    name="amount"
                    placeholder="e.g. 15000"
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

              {/* Category & Payment Method */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
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
              </div>

              {/* Reference No */}
              <div className="form-group">
                <label>Reference / Receipt / Voucher No.</label>
                <input
                  type="text"
                  name="referenceNumber"
                  placeholder="e.g. UTR-23948 or REC-883"
                  value={formData.referenceNumber}
                  onChange={handleFormChange}
                />
              </div>

              {/* Description */}
              <div className="form-group">
                <label>Description / Notes</label>
                <textarea
                  rows="2"
                  name="description"
                  placeholder="Details for this department cashflow transaction..."
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
                  {submitting ? 'Saving...' : isEditing ? 'Update Transaction' : `Record Money ${formData.transactionType}`}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Transaction Receipt Modal */}
      {viewingTransaction && (
        <div className="modal-overlay" onClick={() => setViewingTransaction(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '500px' }}>
            <div className="modal-header">
              <h3>Department Voucher #{viewingTransaction.id}</h3>
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

export default DeptTransactionManagement;
