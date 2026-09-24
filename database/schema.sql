-- ============================================
-- College Cashflow Monitoring System
-- Database Schema
-- ============================================

CREATE DATABASE IF NOT EXISTS `college_cashflow`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `college_cashflow`;

-- 1. DEPARTMENTS TABLE
CREATE TABLE IF NOT EXISTS `departments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `description` TEXT NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_departments_code` (`code`),
    INDEX `idx_departments_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. USERS TABLE
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100) NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `role` ENUM('MAIN_ADMIN', 'DEPT_ADMIN') NOT NULL,
    `department_id` BIGINT NULL,
    `parent_admin_id` BIGINT NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_users_department`
        FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_users_parent_admin`
        FOREIGN KEY (`parent_admin_id`) REFERENCES `users` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_users_username` (`username`),
    INDEX `idx_users_role` (`role`),
    INDEX `idx_users_department_id` (`department_id`),
    INDEX `idx_users_parent_admin_id` (`parent_admin_id`),
    INDEX `idx_users_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `type` ENUM('IN', 'OUT') NOT NULL,
    `description` TEXT NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_categories_type` (`type`),
    INDEX `idx_categories_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. TRANSACTIONS TABLE (Week 2 — Money IN / OUT)
CREATE TABLE IF NOT EXISTS `transactions` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `transaction_type` ENUM('IN', 'OUT') NOT NULL,
    `amount`           DECIMAL(15, 2) NOT NULL,
    `payment_method`   ENUM('CASH', 'BANK_TRANSFER', 'UPI', 'CARD', 'CHEQUE', 'OTHER') NOT NULL,
    `category_id`      BIGINT NOT NULL,
    `department_id`    BIGINT NOT NULL,
    `description`      TEXT NULL,
    `reference_number` VARCHAR(100) NULL,
    `transaction_date` DATE NOT NULL,
    `created_by`       BIGINT NOT NULL,
    `created_at`       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_transactions_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_transactions_department`
        FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_transactions_creator`
        FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_transactions_date`     (`transaction_date`),
    INDEX `idx_transactions_type`     (`transaction_type`),
    INDEX `idx_transactions_dept`     (`department_id`),
    INDEX `idx_transactions_category` (`category_id`),
    INDEX `idx_transactions_payment`  (`payment_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. ATTACHMENTS TABLE (Future Expansion / Week 5)
CREATE TABLE IF NOT EXISTS `attachments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `transaction_id` BIGINT NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `file_path` VARCHAR(500) NOT NULL,
    `file_type` VARCHAR(100) NULL,
    `file_size` BIGINT NULL,
    `uploaded_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_attachments_transaction`
        FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_attachments_transaction` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
