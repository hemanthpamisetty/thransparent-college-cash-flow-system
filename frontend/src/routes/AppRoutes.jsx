// ============================================
// AppRoutes.jsx — Application routing
// ============================================

import { Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES, ROLES } from '../utils/constants';
import ProtectedRoute from './ProtectedRoute';
import Login from '../pages/auth/Login';
import MainAdminDashboard from '../pages/main-admin/MainAdminDashboard';
import DepartmentManagement from '../pages/main-admin/DepartmentManagement';
import DeptAdminManagement from '../pages/main-admin/DeptAdminManagement';
import CategoryManagement from '../pages/main-admin/CategoryManagement';
import TransactionManagement from '../pages/main-admin/TransactionManagement';
import MainAdminAnalytics from '../pages/main-admin/MainAdminAnalytics';
import DeptAdminDashboard from '../pages/dept-admin/DeptAdminDashboard';
import DeptTransactionManagement from '../pages/dept-admin/DeptTransactionManagement';
import DeptAdminAnalytics from '../pages/dept-admin/DeptAdminAnalytics';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path={ROUTES.LOGIN} element={<Login />} />

      {/* Main Admin Routes */}
      <Route
        path={ROUTES.MAIN_ADMIN_DASHBOARD}
        element={
          <ProtectedRoute requiredRole={ROLES.MAIN_ADMIN}>
            <MainAdminDashboard />
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.MAIN_ADMIN_TRANSACTIONS}
        element={
          <ProtectedRoute requiredRole={ROLES.MAIN_ADMIN}>
            <TransactionManagement />
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.MAIN_ADMIN_DEPARTMENTS}
        element={
          <ProtectedRoute requiredRole={ROLES.MAIN_ADMIN}>
            <DepartmentManagement />
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.MAIN_ADMIN_DEPT_ADMINS}
        element={
          <ProtectedRoute requiredRole={ROLES.MAIN_ADMIN}>
            <DeptAdminManagement />
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.MAIN_ADMIN_ANALYTICS}
        element={
          <ProtectedRoute requiredRole={ROLES.MAIN_ADMIN}>
            <MainAdminAnalytics />
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.MAIN_ADMIN_CATEGORIES}
        element={
          <ProtectedRoute requiredRole={ROLES.MAIN_ADMIN}>
            <CategoryManagement />
          </ProtectedRoute>
        }
      />

      {/* Department Admin Routes */}
      <Route
        path={ROUTES.DEPT_ADMIN_DASHBOARD}
        element={
          <ProtectedRoute requiredRole={ROLES.DEPT_ADMIN}>
            <DeptAdminDashboard />
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.DEPT_ADMIN_TRANSACTIONS}
        element={
          <ProtectedRoute requiredRole={ROLES.DEPT_ADMIN}>
            <DeptTransactionManagement />
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.DEPT_ADMIN_ANALYTICS}
        element={
          <ProtectedRoute requiredRole={ROLES.DEPT_ADMIN}>
            <DeptAdminAnalytics />
          </ProtectedRoute>
        }
      />

      {/* Default — redirect to login */}
      <Route path="*" element={<Navigate to={ROUTES.LOGIN} replace />} />
    </Routes>
  );
};

export default AppRoutes;
