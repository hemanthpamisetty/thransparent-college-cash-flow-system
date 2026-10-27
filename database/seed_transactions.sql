-- =============================================================================
-- Seed script for 200 realistic transaction records
-- =============================================================================
-- This script is idempotent: it first removes any previously inserted seed rows
-- (identified by reference_number pattern 'seed-200-%') and then inserts 200 new
-- records spanning dates from 2026-09-19 to the current date.
--
-- It uses random selection of existing departments, categories, and a main admin
-- user (active) to satisfy foreign‑key constraints.
--
-- Execute with:
--   mysql -u <user> -p college_cashflow < database/seed_transactions.sql
-- =============================================================================

-- Remove previously generated seed rows (if any)
DELETE FROM transactions
WHERE reference_number LIKE 'seed-200-%';

-- Create a stored procedure to generate the rows
DELIMITER //
CREATE PROCEDURE seed_transactions()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE max_i INT DEFAULT 200;
    WHILE i <= max_i DO
        INSERT INTO transactions (
            transaction_type,
            amount,
            payment_method,
            category_id,
            department_id,
            description,
            reference_number,
            transaction_date,
            created_by,
            created_at,
            updated_at
        ) VALUES (
            -- 60% IN, 40% OUT (adjust as desired)
            CASE WHEN RAND() < 0.6 THEN 'IN' ELSE 'OUT' END,
            -- Random amount between 100.00 and 5100.00
            ROUND(RAND() * 5000 + 100, 2),
            -- Random payment method
            ELT(FLOOR(RAND() * 6) + 1,
                'CASH',
                'BANK_TRANSFER',
                'UPI',
                'CARD',
                'CHEQUE',
                'OTHER'),
            -- Random existing category
            (SELECT id FROM categories ORDER BY RAND() LIMIT 1),
            -- Random existing department
            (SELECT id FROM departments ORDER BY RAND() LIMIT 1),
            -- Description – simple but varied
            CONCAT('Sample transaction ', i),
            -- Unique reference number for idempotency
            CONCAT('seed-200-', i),
            -- Random date in the required range
            DATE_ADD('2026-09-19', INTERVAL FLOOR(RAND() * (DATEDIFF(CURRENT_DATE, '2026-09-19') + 1)) DAY),
            -- Random active main admin user as creator
            (SELECT id FROM users WHERE role = 'MAIN_ADMIN' AND active = TRUE ORDER BY RAND() LIMIT 1),
            NOW(),
            NOW()
        );
        SET i = i + 1;
    END WHILE;
END//
DELIMITER ;

-- Run the procedure to populate the table
CALL seed_transactions();

-- Clean up: drop the procedure (optional, keeps schema tidy)
DROP PROCEDURE IF EXISTS seed_transactions;
