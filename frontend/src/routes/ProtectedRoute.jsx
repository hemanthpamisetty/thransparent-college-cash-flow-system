// ============================================
// ProtectedRoute.jsx — Route protection by role
// ============================================

import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ROUTES } from '../utils/constants';

/**
 * ProtectedRoute — wraps a route to require authentication and (optionally) a specific role.
 *
 * Props:
 *   - children: the component to render
 *   - requiredRole: optional role string (e.g. 'MAIN_ADMIN' or 'DEPT_ADMIN')
 */
const ProtectedRoute = ({ children, requiredRole }) => {
  const { isAuthenticated, user, loading } = useAuth();

  // While loading auth state from localStorage, show nothing
  if (loading) {
    return <div className="loading-screen">Loading...</div>;
  }

  // Not authenticated → go to login
  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  // Authenticated but wrong role → redirect to their correct dashboard
  if (requiredRole && user.role !== requiredRole) {
    if (user.role === 'MAIN_ADMIN') {
      return <Navigate to={ROUTES.MAIN_ADMIN_DASHBOARD} replace />;
    }
    if (user.role === 'DEPT_ADMIN') {
      return <Navigate to={ROUTES.DEPT_ADMIN_DASHBOARD} replace />;
    }
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  return children;
};

export default ProtectedRoute;
