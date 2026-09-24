-- ============================================
-- College Cashflow Monitoring System
-- Development & Testing Seed Data
-- ============================================

USE `college_cashflow`;

-- Disable foreign key checks for clean truncation/insertion if re-running
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `attachments`;
TRUNCATE TABLE `transactions`;
TRUNCATE TABLE `categories`;
TRUNCATE TABLE `users`;
TRUNCATE TABLE `departments`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. SEED DEPARTMENTS
INSERT INTO `departments` (`id`, `name`, `code`, `description`, `active`, `created_at`, `updated_at`)
VALUES
(1, 'Computer Science & Engineering', 'CSE', 'Department of Computer Science & Engineering', TRUE, NOW(), NOW()),
(2, 'Electronics & Communication Engineering', 'ECE', 'Department of Electronics & Communication Engineering', TRUE, NOW(), NOW()),
(3, 'Electrical & Electronics Engineering', 'EEE', 'Department of Electrical & Electronics Engineering', TRUE, NOW(), NOW()),
(4, 'Mechanical Engineering', 'MECH', 'Department of Mechanical Engineering', TRUE, NOW(), NOW()),
(5, 'Civil Engineering', 'CIVIL', 'Department of Civil Engineering', TRUE, NOW(), NOW());

-- 2. SEED MAIN ADMIN (Password: Admin@123)
-- BCrypt Hash for 'Admin@123': $2a$10$z2GA5CY4mWkR.NF4VRTvCeApNvXrN95O1x6DKYMxMcUvyKrv1otKq
INSERT INTO `users` (`id`, `username`, `password_hash`, `email`, `full_name`, `role`, `department_id`, `parent_admin_id`, `active`, `created_at`, `updated_at`)
VALUES
(1, 'admin', '$2a$10$z2GA5CY4mWkR.NF4VRTvCeApNvXrN95O1x6DKYMxMcUvyKrv1otKq', 'admin@college.edu', 'Main Administrator', 'MAIN_ADMIN', NULL, NULL, TRUE, NOW(), NOW());

-- 3. SEED DEPARTMENT ADMINS (Password: Admin@123)
-- Belong to CSE, ECE, EEE with parent_admin_id = 1
INSERT INTO `users` (`id`, `username`, `password_hash`, `email`, `full_name`, `role`, `department_id`, `parent_admin_id`, `active`, `created_at`, `updated_at`)
VALUES
(2, 'cseadmin', '$2a$10$z2GA5CY4mWkR.NF4VRTvCeApNvXrN95O1x6DKYMxMcUvyKrv1otKq', 'cseadmin@college.edu', 'CSE Department Admin', 'DEPT_ADMIN', 1, 1, TRUE, NOW(), NOW()),
(3, 'eceadmin', '$2a$10$z2GA5CY4mWkR.NF4VRTvCeApNvXrN95O1x6DKYMxMcUvyKrv1otKq', 'eceadmin@college.edu', 'ECE Department Admin', 'DEPT_ADMIN', 2, 1, TRUE, NOW(), NOW()),
(4, 'eeeadmin', '$2a$10$z2GA5CY4mWkR.NF4VRTvCeApNvXrN95O1x6DKYMxMcUvyKrv1otKq', 'eeeadmin@college.edu', 'EEE Department Admin', 'DEPT_ADMIN', 3, 1, TRUE, NOW(), NOW());

-- 4. SEED CATEGORIES (IN = Income, OUT = Expense)
INSERT INTO `categories` (`id`, `name`, `type`, `description`, `active`, `created_at`, `updated_at`)
VALUES
(1, 'Student Fees', 'IN', 'Income received from student tuition and admission fees', TRUE, NOW(), NOW()),
(2, 'Donations', 'IN', 'Alumni and research donations', TRUE, NOW(), NOW()),
(3, 'Grants', 'IN', 'Government and educational grants', TRUE, NOW(), NOW()),
(4, 'Exam Fees', 'IN', 'Fees collected during examination season', TRUE, NOW(), NOW()),
(5, 'Other Income', 'IN', 'Miscellaneous incoming funds', TRUE, NOW(), NOW()),
(6, 'Electricity', 'OUT', 'Electricity utility expenses', TRUE, NOW(), NOW()),
(7, 'Maintenance', 'OUT', 'Campus and lab maintenance', TRUE, NOW(), NOW()),
(8, 'Equipment', 'OUT', 'Lab and classroom equipment purchases', TRUE, NOW(), NOW()),
(9, 'Stationery', 'OUT', 'Office and exam stationery supplies', TRUE, NOW(), NOW()),
(10, 'Salary', 'OUT', 'Faculty and staff salary expenses', TRUE, NOW(), NOW()),
(11, 'Transportation', 'OUT', 'College buses and fuel expenses', TRUE, NOW(), NOW()),
(12, 'Other Expense', 'OUT', 'Miscellaneous outgoing expenses', TRUE, NOW(), NOW());

-- 5. SEED SAMPLE TRANSACTIONS (Development / Testing Data)
-- created_by = 2 (cseadmin), 3 (eceadmin)
INSERT INTO `transactions`
    (`id`, `transaction_type`, `amount`, `payment_method`, `category_id`, `department_id`,
     `description`, `reference_number`, `transaction_date`, `created_by`, `created_at`, `updated_at`)
VALUES
-- CSE Money IN: Student Fees ₹50,000
(1, 'IN', 50000.00, 'BANK_TRANSFER', 1, 1,
 'Student examination fee collection for Semester 5',
 'TXN-CSE-001', '2026-09-01', 2, NOW(), NOW()),

-- CSE Money OUT: Equipment ₹20,000
(2, 'OUT', 20000.00, 'BANK_TRANSFER', 8, 1,
 'Purchase of laboratory equipment for Computer Lab',
 'EXP-CSE-001', '2026-09-03', 2, NOW(), NOW()),

-- ECE Money IN: Student Fees ₹40,000
(3, 'IN', 40000.00, 'UPI', 1, 2,
 'Student tuition fee collection for ECE Semester 3',
 'TXN-ECE-001', '2026-09-02', 3, NOW(), NOW()),

-- ECE Money OUT: Electricity ₹15,000
(4, 'OUT', 15000.00, 'CASH', 6, 2,
 'Monthly electricity bill for ECE block',
 'EXP-ECE-001', '2026-09-05', 3, NOW(), NOW()),

-- CSE Money IN: Grants ₹30,000
(5, 'IN', 30000.00, 'BANK_TRANSFER', 3, 1,
 'State government research grant for AI lab',
 'TXN-CSE-002', '2026-09-06', 2, NOW(), NOW()),

-- ECE Money OUT: Maintenance ₹8,000
(6, 'OUT', 8000.00, 'CHEQUE', 7, 2,
 'Lab maintenance and electrical fixtures repair',
 'EXP-ECE-002', '2026-09-06', 3, NOW(), NOW());

