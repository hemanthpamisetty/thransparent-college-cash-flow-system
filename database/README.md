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

## Re-running Scripts (If Needed)

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pHemu@123 college_cashflow -e "source database/schema.sql"
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pHemu@123 college_cashflow -e "source database/seed.sql"
```
