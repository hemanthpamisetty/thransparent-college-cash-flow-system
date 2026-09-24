# Project Plan — College Cashflow Monitoring System

## 5-Week Development Plan

| Week | Focus                                          | Status       |
|------|-------------------------------------------------|--------------|
| 1    | Project Foundation + Database + Authentication  | ✅ Completed  |
| 2    | Admin & Department Management                   | ✅ Completed  |
| 3    | Money IN/OUT Transaction Management             | ✅ Completed  |
| 4    | Dashboard + Daily/Monthly Reports               | 🔄 Next Up   |
| 5    | Documents + Security + Testing + Finalization   | ⏳ Pending    |

---

## Team Structure

| Member   | Primary Responsibility  |
|----------|------------------------|
| Member 1 | Backend + Database      |
| Member 2 | Frontend                |

Both members collaborate on: Integration, Testing, Documentation, Debugging

---

## Deliverables Status

### Week 1 — Foundation & Authentication
- [x] Project folder structure
- [x] React frontend (Vite)
- [x] Spring Boot backend (Maven)
- [x] MySQL database connection
- [x] Database schema (`users`, `departments`)
- [x] BCrypt password encryption
- [x] Spring Security configuration & JWT authentication
- [x] Main Admin & Dept Admin login APIs
- [x] Role-based routing & ProtectedRoute
- [x] CORS configuration

### Week 2 — Admin & Department Management
- [x] Department entity, repository, service, controller (CRUD & status toggle)
- [x] User management for Department Admins (CRUD & status toggle)
- [x] Category entity, repository, service, controller (`IN` / `OUT` categories)
- [x] Frontend Department Management UI
- [x] Frontend Dept Admin Management UI
- [x] Frontend Category Management UI

### Week 3 — Money IN / OUT Transaction Management
- [x] `Transaction` entity, `TransactionType` and `PaymentMethod` enums
- [x] `TransactionRepository` with multi-parameter filtering & cashflow aggregation
- [x] `TransactionService` with role-based department isolation
- [x] `TransactionController` REST endpoints (`/api/transactions`, `/api/transactions/summary`)
- [x] Frontend `transactionService.js`
- [x] Main Admin Transaction Management (KPI metric cards, filters, CRUD, receipt voucher)
- [x] Department Admin Transaction Management (isolated ledger, Money IN/OUT recording, receipt voucher)
- [x] Updated Main Admin & Dept Admin Dashboard overviews with live cashflow metrics

### Week 4 — Dashboard Analytics & Reports (Upcoming)
- [ ] Visual charts (Cashflow trend over time, income vs expense breakdowns)
- [ ] Daily cashflow summary report
- [ ] Monthly department-wise expenditure report
- [ ] Export to PDF / CSV

### Week 5 — Documents, Security, Testing & Finalization (Upcoming)
- [ ] File attachments for transaction receipts
- [ ] Unit and integration test suites
- [ ] Security hardening & comprehensive user guide
