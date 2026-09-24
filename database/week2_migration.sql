-- ============================================================
-- Week 2 Migration — College Cashflow Monitoring System
-- MySQL 8.0 Compatible Version
-- Run this ONCE on the existing college_cashflow database.
-- ============================================================

USE `college_cashflow`;

-- ============================================================
-- STEP 1: Truncate child tables to avoid FK violations
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `attachments`;
TRUNCATE TABLE `transactions`;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- STEP 2: Drop old FK on category_id (was ON DELETE SET NULL)
--         Use a procedure to drop it only if it exists.
-- ============================================================
DROP PROCEDURE IF EXISTS drop_fk_if_exists;
DELIMITER $$
CREATE PROCEDURE drop_fk_if_exists()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'transactions'
          AND CONSTRAINT_NAME = 'fk_transactions_category'
          AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE `transactions` DROP FOREIGN KEY `fk_transactions_category`;
    END IF;
END$$
DELIMITER ;
CALL drop_fk_if_exists();
DROP PROCEDURE IF EXISTS drop_fk_if_exists;

-- ============================================================
-- STEP 3: Drop old index idx_transactions_status if it exists
-- ============================================================
DROP PROCEDURE IF EXISTS drop_idx_if_exists;
DELIMITER $$
CREATE PROCEDURE drop_idx_if_exists()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'transactions'
          AND INDEX_NAME = 'idx_transactions_status'
    ) THEN
        ALTER TABLE `transactions` DROP INDEX `idx_transactions_status`;
    END IF;
    IF EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'transactions'
          AND INDEX_NAME = 'idx_transactions_type'
    ) THEN
        ALTER TABLE `transactions` DROP INDEX `idx_transactions_type`;
    END IF;
END$$
DELIMITER ;
CALL drop_idx_if_exists();
DROP PROCEDURE IF EXISTS drop_idx_if_exists;

-- ============================================================
-- STEP 4: Modify transaction_type enum INCOME/EXPENSE -> IN/OUT
-- ============================================================
ALTER TABLE `transactions`
    MODIFY COLUMN `transaction_type` ENUM('IN', 'OUT') NOT NULL;

-- ============================================================
-- STEP 5: Add payment_method column (after amount)
-- ============================================================
ALTER TABLE `transactions`
    ADD COLUMN `payment_method`
        ENUM('CASH', 'BANK_TRANSFER', 'UPI', 'CARD', 'CHEQUE', 'OTHER')
        NOT NULL DEFAULT 'CASH'
        AFTER `amount`;

-- ============================================================
-- STEP 6: Add reference_number column (after description)
-- ============================================================
ALTER TABLE `transactions`
    ADD COLUMN `reference_number` VARCHAR(100) NULL AFTER `description`;

-- ============================================================
-- STEP 7: Make category_id NOT NULL
-- ============================================================
ALTER TABLE `transactions`
    MODIFY COLUMN `category_id` BIGINT NOT NULL;

-- ============================================================
-- STEP 8: Re-add FK for category_id with ON DELETE RESTRICT
-- ============================================================
ALTER TABLE `transactions`
    ADD CONSTRAINT `fk_transactions_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE;

-- ============================================================
-- STEP 9: Drop the status column (not needed for Week 2)
-- ============================================================
DROP PROCEDURE IF EXISTS drop_status_col;
DELIMITER $$
CREATE PROCEDURE drop_status_col()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'transactions'
          AND COLUMN_NAME = 'status'
    ) THEN
        ALTER TABLE `transactions` DROP COLUMN `status`;
    END IF;
END$$
DELIMITER ;
CALL drop_status_col();
DROP PROCEDURE IF EXISTS drop_status_col;

-- ============================================================
-- STEP 10: Add new indexes
-- ============================================================
ALTER TABLE `transactions`
    ADD INDEX `idx_transactions_type`     (`transaction_type`),
    ADD INDEX `idx_transactions_category` (`category_id`),
    ADD INDEX `idx_transactions_payment`  (`payment_method`);

-- ============================================================
-- STEP 11: Show final structure to verify
-- ============================================================
DESCRIBE `transactions`;

-- ============================================================
-- Done. Transactions table is ready for Week 2.
-- ============================================================
