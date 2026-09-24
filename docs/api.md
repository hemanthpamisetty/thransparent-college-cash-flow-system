# API Documentation — College Cashflow Monitoring System

## Base URL
`http://localhost:8081/api`

---

## 1. Authentication Endpoints

### 1.1 Main Admin Login
- **Endpoint**: `POST /api/auth/main-admin/login`
- **Access**: Public
- **Request Body**:
```json
{
  "username": "admin",
  "password": "Admin@123"
}
```
- **Response** `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "fullName": "Main Administrator",
  "role": "MAIN_ADMIN",
  "departmentId": null,
  "departmentName": null
}
```

### 1.2 Department Admin Login
- **Endpoint**: `POST /api/auth/dept-admin/login`
- **Access**: Public
- **Request Body**:
```json
{
  "adminUsername": "admin",
  "departmentAdminUsername": "cseadmin",
  "password": "Admin@123"
}
```
- **Response** `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "cseadmin",
  "fullName": "CSE Department Admin",
  "role": "DEPT_ADMIN",
  "departmentId": 1,
  "departmentName": "Computer Science & Engineering"
}
```

---

## 2. Department Endpoints

### 2.1 List All Departments
- **Endpoint**: `GET /api/departments`
- **Access**: Authenticated users

### 2.2 Get Department by ID
- **Endpoint**: `GET /api/departments/{id}`
- **Access**: Authenticated users

### 2.3 Create Department
- **Endpoint**: `POST /api/departments`
- **Access**: `MAIN_ADMIN` only (`403 Forbidden` for `DEPT_ADMIN`)
- **Request Body**:
```json
{
  "name": "Information Technology",
  "code": "IT",
  "description": "Department of Information Technology"
}
```

### 2.4 Update Department
- **Endpoint**: `PUT /api/departments/{id}`
- **Access**: `MAIN_ADMIN` only

### 2.5 Toggle Department Status
- **Endpoint**: `PATCH /api/departments/{id}/status`
- **Access**: `MAIN_ADMIN` only
- **Request Body**:
```json
{
  "active": false
}
```

---

## 3. Department Admin Endpoints

### 3.1 List All Department Admins
- **Endpoint**: `GET /api/users/dept-admins`
- **Access**: `MAIN_ADMIN` only

### 3.2 Get Department Admin by ID
- **Endpoint**: `GET /api/users/dept-admins/{id}`
- **Access**: `MAIN_ADMIN` only

### 3.3 Create Department Admin
- **Endpoint**: `POST /api/users/dept-admin`
- **Access**: `MAIN_ADMIN` only
- **Request Body**:
```json
{
  "username": "itadmin",
  "password": "Admin@123",
  "fullName": "IT Department Admin",
  "email": "itadmin@college.edu",
  "departmentId": 1
}
```
*(Server automatically assigns `role=DEPT_ADMIN` and `parentAdmin=authenticated Main Admin`)*

### 3.4 Update Department Admin
- **Endpoint**: `PUT /api/users/dept-admins/{id}`
- **Access**: `MAIN_ADMIN` only

### 3.5 Toggle Admin Status
- **Endpoint**: `PATCH /api/users/dept-admins/{id}/status`
- **Access**: `MAIN_ADMIN` only

---

## 4. Category Endpoints

### 4.1 List Categories
- **Endpoint**: `GET /api/categories?type=IN|OUT`
- **Access**: Authenticated users

### 4.2 Create Category
- **Endpoint**: `POST /api/categories`
- **Access**: `MAIN_ADMIN` only
- **Request Body**:
```json
{
  "name": "Lab Equipment",
  "type": "OUT",
  "description": "Hardware and lab equipment expenses"
}
```

### 4.3 Update Category
- **Endpoint**: `PUT /api/categories/{id}`
- **Access**: `MAIN_ADMIN` only

### 4.4 Toggle Category Status
- **Endpoint**: `PATCH /api/categories/{id}/status`
- **Access**: `MAIN_ADMIN` only

---

## 5. Transaction Endpoints (Money IN / Money OUT)

### 5.1 List & Filter Transactions
- **Endpoint**: `GET /api/transactions`
- **Access**: `MAIN_ADMIN`, `DEPT_ADMIN` (Dept Admin strictly scoped to own department)
- **Query Parameters**:
  - `departmentId` (optional, filtered for Main Admin; ignored/enforced for Dept Admin)
  - `transactionType` (optional, `IN` or `OUT`)
  - `categoryId` (optional, category ID)
  - `paymentMethod` (optional, `CASH`, `BANK_TRANSFER`, `UPI`, `CARD`, `CHEQUE`, `OTHER`)
  - `startDate` (optional, `YYYY-MM-DD`)
  - `endDate` (optional, `YYYY-MM-DD`)
  - `search` (optional, keyword search in description / reference number)
- **Response** `200 OK`:
```json
[
  {
    "id": 1,
    "transactionType": "IN",
    "amount": 50000.00,
    "paymentMethod": "BANK_TRANSFER",
    "categoryId": 1,
    "categoryName": "Government Grant",
    "categoryType": "IN",
    "departmentId": 1,
    "departmentName": "Computer Science & Engineering",
    "departmentCode": "CSE",
    "description": "AI Lab Research Grant",
    "referenceNumber": "UTR-9823412",
    "transactionDate": "2026-09-07",
    "createdById": 2,
    "createdByUsername": "cseadmin",
    "createdByFullName": "CSE Department Admin",
    "createdAt": "2026-09-07T12:00:00",
    "updatedAt": "2026-09-07T12:00:00"
  }
]
```

### 5.2 Get Transaction by ID
- **Endpoint**: `GET /api/transactions/{id}`
- **Access**: `MAIN_ADMIN`, `DEPT_ADMIN` (Dept Admin can only view transactions from own department)

### 5.3 Record Transaction (Money IN / OUT)
- **Endpoint**: `POST /api/transactions`
- **Access**: `MAIN_ADMIN`, `DEPT_ADMIN`
- **Request Body**:
```json
{
  "transactionType": "IN",
  "amount": 25000.00,
  "paymentMethod": "UPI",
  "categoryId": 1,
  "departmentId": 1,
  "transactionDate": "2026-09-07",
  "referenceNumber": "UPI-88492817",
  "description": "Student Tech Fest Registration Fees"
}
```
*(For `DEPT_ADMIN`, `departmentId` is automatically validated and assigned to the user's assigned department).*

### 5.4 Update Transaction
- **Endpoint**: `PUT /api/transactions/{id}`
- **Access**: `MAIN_ADMIN`, `DEPT_ADMIN` (Dept Admin can only update own department records)

### 5.5 Delete Transaction
- **Endpoint**: `DELETE /api/transactions/{id}`
- **Access**: `MAIN_ADMIN`, `DEPT_ADMIN` (Dept Admin can only delete own department records)
- **Response**: `204 No Content`

---

## 6. Cashflow Summary & Aggregates

### 6.1 Get Cashflow Summary
- **Endpoint**: `GET /api/transactions/summary`
- **Access**: `MAIN_ADMIN`, `DEPT_ADMIN`
- **Query Parameters**:
  - `departmentId` (optional for Main Admin to get departmental totals; auto-scoped for Dept Admin)
- **Response** `200 OK`:
```json
{
  "totalIncome": 250000.00,
  "totalExpense": 85000.00,
  "netBalance": 165000.00,
  "totalTransactions": 18,
  "incomeCount": 12,
  "expenseCount": 6
}
```

