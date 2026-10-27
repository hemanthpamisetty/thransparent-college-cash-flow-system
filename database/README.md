# Database — College Cashflow Monitoring System

## Database: `college_cashflow`
* **Engine**: MySQL 8.0
* **Character Set**: `utf8mb4`

## Tables Created

| Table        | Purpose                                                      | Status      |
|--------------|--------------------------------------------------------------|-------------|
| `departments`| College departments (CSE, ECE, EEE, MECH, CIVIL)            | ✅ Active   |
| `users`      | System administrators (Main Admin & Department Admins)       | ✅ Active   |
| `categories` | Income and Expense categories                                | ✅ Prepared |
| `transactions`| Financial cashflow records (Money IN / Money OUT)           | ✅ Prepared |
| `attachments`| Supporting bills, receipts, and documents                    | ✅ Prepared |

## Seed Accounts (Development & Testing)

| Username   | Password    | Role         | Assigned Department |
|------------|-------------|--------------|---------------------|
| `admin`    | `Admin@123` | `MAIN_ADMIN` | Global Access       |
| `cseadmin` | `Admin@123` | `DEPT_ADMIN` | CSE (ID: 1)         |
| `eceadmin` | `Admin@123` | `DEPT_ADMIN` | ECE (ID: 2)         |
| `eeeadmin` | `Admin@123` | `DEPT_ADMIN` | EEE (ID: 3)         |

## Database Initialization & Setup

Execute the schema first, then optionally apply the development seed data:

> ⚠️ **Warning**: `seed.sql` truncates existing records before inserting seed data. Do not run `seed.sql` if you wish to retain existing transactions.
> ⚠️ **Notice**: `week2_migration.sql` is an incremental migration script for upgrading legacy Week 1 databases only. Do NOT run it on a fresh database.

```powershell
# Using mysql client (prompts securely for password):
mysql -u root -p -e "source database/schema.sql"
mysql -u root -p college_cashflow -e "source database/seed.sql"

# Or if using the explicit MySQL 8.0 installation path:
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "source database/schema.sql"
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p college_cashflow -e "source database/seed.sql"
```
