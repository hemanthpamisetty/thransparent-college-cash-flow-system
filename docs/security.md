# Security Documentation — College Cashflow Monitoring System

## Role-Based Access Control (RBAC)

| Role | Scope | Permissions |
|------|-------|-------------|
| `MAIN_ADMIN` | College-Wide | Full access: Departments, Dept Admins, Categories, Financial overview |
| `DEPT_ADMIN` | Single Department | Scoped to assigned department only. Strictly blocked from management endpoints |

## Defense in Depth

1. **Frontend Guards**:
   - `ProtectedRoute.jsx` redirects unauthorized roles away from `/main-admin/*` routes.
2. **Backend API Authorization**:
   - Controller methods annotated with `@PreAuthorize("hasRole('MAIN_ADMIN')")`.
   - Spring Security rejects unauthorized role invocations with `403 Forbidden`.
3. **Hierarchy & Parent Admin Integrity**:
   - Client input for `role` and `parentAdminId` is ignored/disallowed.
   - The server validates the authenticated Main Admin and automatically associates `parent_admin_id`.
4. **Password Security**:
   - All passwords are encrypted with BCrypt (`BCryptPasswordEncoder`).
   - `password_hash` is never included in any API response DTO.
