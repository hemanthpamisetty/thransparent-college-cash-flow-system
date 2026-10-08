# College Cashflow Monitoring System

> A secure, role-based full-stack web application designed for comprehensive monitoring, auditing, and visual reporting of college finances (Money IN and Money OUT) across departments.

---

## 📌 Project Overview

The **College Cashflow Monitoring System** provides transparent financial operations for educational institutions. It enforces role-based access control allowing **Main Administrators** to have institution-wide oversight and management, while **Department Administrators** operate within strict data-isolated ledgers for their specific departments.

---

## 🚀 Key Features Implemented

### 1. 🔐 Authentication & Security
- **JWT-Based Authentication**: Secure stateless token generation and validation.
- **Role-Based Access Control (RBAC)**: Distinct permissions for `MAIN_ADMIN` and `DEPT_ADMIN`.
- **Password Hashing**: BCrypt encryption for user credentials.
- **Client Route Protection**: Frontend `ProtectedRoute` guarding routes by user role and token state.
- **Request Interception**: Automatic JWT attachment in HTTP headers via Axios interceptor and automatic handling of token expiration.

### 2. 🏛️ Department Management (Main Admin)
- Create, read, update, and activate/deactivate college departments (e.g., Computer Science, Electrical Engineering, Mechanical Engineering, Civil Engineering, MBA).
- View associated department administrator accounts and overall transaction activity.

### 3. 👥 User & Department Admin Management (Main Admin)
- Create and assign Department Admin accounts to specific departments.
- Reset passwords, edit administrator details, and toggle active/inactive account status.

### 4. 🏷️ Category Management (Main Admin)
- Manage transaction categories categorized by type:
  - **Income (Money IN)**: Tuition Fees, Lab Fees, Hostel Fees, Grants, Donations, etc.
  - **Expense (Money OUT)**: Lab Equipment, Faculty Salaries, Maintenance, Utilities, Library Books, Events, etc.
- Activate/deactivate categories dynamically.

### 5. 💳 Money IN / OUT Transaction Management
- **Full CRUD Support**: Add, view, edit, and delete cashflow transactions.
- **Multi-Parameter Filtering**: Filter transactions by date range, transaction type (`IN` / `OUT`), category, department, payment method, and search keywords.
- **Department Data Isolation**: Department Admins only see and manage transactions belonging to their assigned department. Main Admin has complete cross-department visibility.
- **Receipt Voucher Generator**: View and print formatted transaction vouchers directly from the browser.
- **Summary Metrics**: Real-time aggregation of Total Inflow, Total Outflow, and Net Cashflow.

### 6. 📊 Interactive Analytics & Reports
- **Cashflow Trends**: Track daily income, expense, and net balance over customizable date ranges.
- **Category Breakdown**: Group financial flow by categories to identify key revenue streams and spending drivers.
- **Department Summary (Main Admin)**: Compare income, expense, and net margin across all departments.
- **Daily & Monthly Aggregate Reports**: Month-by-month financial summaries for any fiscal year.
- **CSV Data Export**: One-click transaction and analytics export for offline reporting.

---

## 🛠️ Technology Stack

| Layer | Technology | Description |
|---|---|---|
| **Frontend** | React 18, Vite, React Router DOM | Modern SPA with fast development build |
| **Styling** | Custom Vanilla CSS & CSS Variables | Responsive, modern dark/glassmorphic interface |
| **HTTP Client** | Axios | Configured with token interceptors |
| **Icons** | Lucide React | Clean, modern UI icons |
| **Backend** | Java 21, Spring Boot 3.x | Robust enterprise REST API framework |
| **Security** | Spring Security 6, JJWT, BCrypt | Token authentication and method security |
| **ORM / Data** | Spring Data JPA, Hibernate | Declarative queries and data persistence |
| **Database** | MySQL 8.x | Relational schema with foreign keys and indexes |
| **Build Tools** | Maven (Backend), npm (Frontend) | Dependency management and build pipelines |

---

## 📁 Project Structure

```
college-cashflow-monitoring-system/
├── backend/                                # Spring Boot Application
│   ├── src/main/java/com/college/cashflow/
│   │   ├── config/                         # Security & CORS configuration
│   │   ├── controller/                     # REST API Controllers (Auth, Dept, User, Category, Transaction, Report)
│   │   ├── dto/                            # Request & Response Data Transfer Objects
│   │   ├── entity/                         # JPA Entities (User, Department, Category, Transaction)
│   │   ├── enums/                          # Role, TransactionType, PaymentMethod enums
│   │   ├── exception/                      # Global exception handling & custom exceptions
│   │   ├── repository/                     # Spring Data JPA Repositories
│   │   ├── security/                       # JWT service, filters, and UserDetailsService
│   │   └── service/                        # Business logic layer
│   ├── src/main/resources/
│   │   └── application.properties          # Backend configurations
│   ├── .env.example                        # Template for backend secrets
│   └── pom.xml                             # Maven dependencies
│
├── frontend/                               # React + Vite Application
│   ├── src/
│   │   ├── assets/                         # Static assets & styles
│   │   ├── components/                     # Shared UI components (Navbar, Sidebar, Modals, Cards)
│   │   ├── context/                        # React Context (AuthContext)
│   │   ├── pages/
│   │   │   ├── auth/                       # Login page
│   │   │   ├── main-admin/                 # Main Admin views (Dashboard, Departments, Users, Categories, Transactions, Analytics)
│   │   │   └── dept-admin/                 # Dept Admin views (Dashboard, Transactions, Analytics)
│   │   ├── routes/                         # Protected & public routing configuration
│   │   ├── services/                       # API service modules (Axios client, auth, dept, category, transaction, report)
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
│
├── database/                               # Database Schemas & Migrations
│   ├── schema.sql                          # Core database schema
│   ├── seed.sql                            # Sample initial test data
│   ├── week2_migration.sql                 # Incremental updates for categories & users
│   └── README.md                           # Database setup instructions
│
├── docs/                                   # Architectural & API Documentation
│   ├── api.md                              # Detailed REST API specifications
│   ├── architecture.md                     # System architecture design
│   ├── database.md                         # Entity relationship & schema details
│   ├── project-plan.md                     # Milestones and progress tracker
│   └── security.md                         # Security implementation details
│
└── README.md
```

---

## 🔌 API Endpoints Summary

### Authentication (`/api/auth`)
- `POST /api/auth/login` — Authenticate user and receive JWT.
- `GET /api/auth/me` — Retrieve current authenticated user profile.

### Departments (`/api/departments`) — *MAIN_ADMIN only*
- `GET /api/departments` — List all departments.
- `POST /api/departments` — Create new department.
- `PUT /api/departments/{id}` — Update department details.
- `PATCH /api/departments/{id}/status` — Toggle active status.
- `DELETE /api/departments/{id}` — Delete department.

### Department Admins / Users (`/api/users`) — *MAIN_ADMIN only*
- `GET /api/users` — List all department admin users.
- `POST /api/users` — Register a new department admin.
- `PUT /api/users/{id}` — Update user details / credentials.
- `PATCH /api/users/{id}/status` — Toggle user active/inactive status.
- `DELETE /api/users/{id}` — Delete user account.

### Categories (`/api/categories`)
- `GET /api/categories` — List all categories (optional filter by `type=IN|OUT`).
- `POST /api/categories` — Create category (*MAIN_ADMIN*).
- `PUT /api/categories/{id}` — Update category (*MAIN_ADMIN*).
- `PATCH /api/categories/{id}/status` — Toggle category status (*MAIN_ADMIN*).
- `DELETE /api/categories/{id}` — Delete category (*MAIN_ADMIN*).

### Transactions (`/api/transactions`)
- `GET /api/transactions` — Get filtered transactions list with pagination/search.
- `GET /api/transactions/{id}` — Get single transaction details.
- `POST /api/transactions` — Record new transaction (`IN` or `OUT`).
- `PUT /api/transactions/{id}` — Update an existing transaction.
- `DELETE /api/transactions/{id}` — Delete transaction.
- `GET /api/transactions/summary` — Aggregate metrics (Total Inflow, Outflow, Net).

### Reports & Analytics (`/api/reports`)
- `GET /api/reports/cashflow-trend` — Daily time series of income and expenses.
- `GET /api/reports/category-breakdown` — Sums grouped by categories.
- `GET /api/reports/department-summary` — Department-wise financial breakdown (*MAIN_ADMIN*).
- `GET /api/reports/daily-summary` — Detailed single-day transactions and totals.
- `GET /api/reports/monthly-summary` — Monthly aggregate summary for a specific year.

---

## ⚡ Quick Start Guide

### Prerequisites
- **Java 21+** (JDK)
- **Node.js 18+** & **npm**
- **MySQL Server 8.0+**
- **Maven 3.8+**

---

### Step 1: Database Setup

1. Open your MySQL client (MySQL Workbench, CLI, or DBeaver).
2. Create the database and run the schema and seed scripts:

```sql
CREATE DATABASE college_cashflow;
USE college_cashflow;
```

3. Execute the SQL scripts in order:
   - `database/schema.sql`
   - `database/seed.sql`
   - `database/week2_migration.sql` *(if applying incremental migration)*

---

### Step 2: Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Configure your environment variables. Copy `.env.example` to `.env` or set `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/college_cashflow
   spring.datasource.username=root
   spring.datasource.password=YOUR_PASSWORD
   app.jwt.secret=your_secure_jwt_secret_key_at_least_256_bits_long
   ```
3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
   *Backend server will start at: `http://localhost:8080`*

---

### Step 3: Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install npm dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
   *Frontend application will start at: `http://localhost:5173`*

---

## 🔑 Default Test Credentials

| Role | Username | Password | Access Scope |
|---|---|---|---|
| **Main Admin** | `admin` | `Admin@123` | Full institution-wide control & reports |
| **Dept Admin (CSE)** | `cse_admin` | `Admin@123` | Computer Science transactions & analytics |
| **Dept Admin (ECE)** | `ece_admin` | `Admin@123` | Electronics & Comm. transactions & analytics |

---

## 📈 Development Roadmap & Status

- [x] **Week 1**: Project Foundation, MySQL Database, Spring Security, JWT Auth, Base UI Setup.
- [x] **Week 2**: Department CRUD, Dept Admin User Management, Category Management.
- [x] **Week 3**: Money IN/OUT Transactions, Multi-parameter Filters, Receipt Voucher Print, Dashboard Metrics.
- [x] **Week 4**: Analytics & Trend Reports, Category Breakdowns, Monthly/Daily Financial Summaries.
- [ ] **Week 5**: Transaction Attachment/Receipt Uploads, Comprehensive Automated Tests & System Hardening.
