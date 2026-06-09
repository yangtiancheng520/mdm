-- =============================================
-- Fix value domain length mismatch
-- Please run check_value_domain_length.sql first
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- Start transaction
START TRANSACTION;

-- 1. Show domains to be updated
SELECT '========== Domains To Be Updated ==========' AS '';

SELECT
    vd.id AS DomainID,
    vd.domain_code AS DomainCode,
    vd.domain_name AS DomainName,
    vd.data_length AS BeforeUpdate,
    t.max_length AS AfterUpdate
FROM std_value_domain vd
INNER JOIN (
    SELECT
        domain_id,
        MAX(LENGTH(item_value)) AS max_length
    FROM std_value_domain_item
    WHERE status = '启用'
    GROUP BY domain_id
) t ON vd.id = t.domain_id
WHERE vd.data_type = 'string'
  AND vd.data_length IS NOT NULL
  AND vd.data_length < t.max_length;

-- 2. Execute update
UPDATE std_value_domain vd
INNER JOIN (
    SELECT
        domain_id,
        MAX(LENGTH(item_value)) AS max_length
    FROM std_value_domain_item
    WHERE status = '启用'
    GROUP BY domain_id
) t ON vd.id = t.domain_id
SET vd.data_length = t.max_length
WHERE vd.data_type = 'string'
  AND vd.data_length IS NOT NULL
  AND vd.data_length < t.max_length;

-- Show affected rows
SELECT ROW_COUNT() AS 'UpdatedCount';

-- 3. Verify after update
SELECT '========== Verification After Update ==========' AS '';

SELECT
    vd.id AS DomainID,
    vd.domain_code AS DomainCode,
    vd.domain_name AS DomainName,
    vd.data_length AS CurrentLength,
    IFNULL(MAX(LENGTH(vdi.item_value)), 0) AS MaxItemLength,
    'Fixed' AS Status
FROM std_value_domain vd
LEFT JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id AND vdi.status = '启用'
WHERE vd.data_type = 'string'
GROUP BY vd.id, vd.domain_code, vd.domain_name, vd.data_length
HAVING vd.data_length >= IFNULL(MAX(LENGTH(vdi.item_value)), 0)
ORDER BY vd.domain_code;

-- Commit transaction (uncomment below if confirmed)
-- COMMIT;

-- Rollback transaction (uncomment below if issues found)
-- ROLLBACK;

-- Tips
SELECT '========== Execution Complete ==========' AS '';
SELECT 'Please verify the changes above, then execute COMMIT to save changes' AS 'Tip';
SELECT 'If issues found, please execute ROLLBACK to revert changes' AS 'Tip';
