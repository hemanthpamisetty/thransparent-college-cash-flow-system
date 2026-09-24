# Database Documentation — College Cashflow Monitoring System

## Database: `college_cashflow`
* Engine: MySQL 8
* Character Set: `utf8mb4`
* Collation: `utf8mb4_unicode_ci`

---

## 1. Tables Overview

| Table Name     | Purpose                                                  | Key Foreign Keys |
|----------------|----------------------------------------------------------|------------------|
| `departments`  | Academic and administrative departments                  | None             |
| `users`        | System administrators (Main Admin & Department Admins)   | `department_id` → `departments.id`<br>`parent_admin_id` → `users.id` |
| `categories`   | Transaction categories for Money IN and Money OUT        | None             |
| `transactions` | Money IN (Income) and Money OUT (Expense) cashflow records| `department_id` → `departments.id`<br>`category_id` → `categories.id`<br>`created_by` → `users.id` |
| `attachments`  | Receipts, invoices, and documents for transactions       | `transaction_id` → `transactions.id` |

---

## 2. Table Details

### `departments`
- `id` (BIGINT, PK, Auto-Increment)
- `name` (VARCHAR(100), UNIQUE, NOT NULL) — e.g. "Computer Science & Engineering"
- `code` (VARCHAR(50), UNIQUE, NOT NULL) — e.g. "CSE"
- `description` (TEXT)
- `active` (BOOLEAN, DEFAULT TRUE)
- `created_at`, `updated_at` (TIMESTAMP)

### `users`
- `id` (BIGINT, PK, Auto-Increment)
- `username` (VARCHAR(50), UNIQUE, NOT NULL)
- `password_hash` (VARCHAR(255), NOT NULL) — BCrypt hashed
- `email` (VARCHAR(100))
- `full_name` (VARCHAR(100), NOT NULL)
- `role` (ENUM('MAIN_ADMIN', 'DEPT_ADMIN'), NOT NULL)
- `department_id` (BIGINT, FK → `departments.id`) — NULL for MAIN_ADMIN
- `parent_admin_id` (BIGINT, FK → `users.id`) — NULL for MAIN_ADMIN, Main Admin ID for DEPT_ADMIN
- `active` (BOOLEAN, DEFAULT TRUE)
- `created_at`, `updated_at` (TIMESTAMP)

### `categories`
- `id` (BIGINT, PK, Auto-Increment)
- `name` (VARCHAR(100), NOT NULL)
- `type` (ENUM('INCOME', 'EXPENSE'), NOT NULL)
- `description` (TEXT)
- `active` (BOOLEAN, DEFAULT TRUE)

### `transactions`
- `id` (BIGINT, PK, Auto-Increment)
- `transaction_type` (ENUM('INCOME', 'EXPENSE'), NOT NULL)
- `amount` (DECIMAL(15, 2), NOT NULL)
- `transaction_date` (DATE, NOT NULL)
- `description` (TEXT)
- `category_id` (BIGINT, FK → `categories.id`)
- `department_id` (BIGINT, FK → `departments.id`)
- `created_by` (BIGINT, FK → `users.id`)
- `status` (ENUM('PENDING', 'APPROVED', 'REJECTED'), DEFAULT 'APPROVED')

### `attachments`
- `id` (BIGINT, PK, Auto-Increment)
- `transaction_id` (BIGINT, FK → `transactions.id`)
- `file_name` (VARCHAR(255), NOT NULL)
- `file_path` (VARCHAR(500), NOT NULL)
- `file_type` (VARCHAR(100))
- `file_size` (BIGINT)
- `uploaded_at` (TIMESTAMP)

---

## 3. Seed Accounts (Development & Testing)

| Username   | Password    | Role         | Assigned Department | Parent Admin |
|------------|-------------|--------------|---------------------|--------------|
| `admin`    | `Admin@123` | `MAIN_ADMIN` | None (Global)       | None         |
| `cseadmin` | `Admin@123` | `DEPT_ADMIN` | CSE (ID: 1)         | `admin` (1)  |
| `eceadmin` | `Admin@123` | `DEPT_ADMIN` | ECE (ID: 2)         | `admin` (1)  |
| `eeeadmin` | `Admin@123` | `DEPT_ADMIN` | EEE (ID: 3)         | `admin` (1)  |
