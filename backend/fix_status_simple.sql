-- =============================================
-- 修复主数据状态值（统一为大写）
-- =============================================

USE mdm;

-- 1. 查看修复前的状态
SELECT '=== 修复前的状态 ===' AS 'Info';

-- 查看mdm_vendor表的状态
SELECT 'mdm_vendor' AS table_name, status, COUNT(*) as count
FROM mdm_vendor
GROUP BY status
UNION ALL
SELECT 'mdm_customer' AS table_name, status, COUNT(*) as count
FROM mdm_customer
GROUP BY status
UNION ALL
SELECT 'mdm_material' AS table_name, status, COUNT(*) as count
FROM mdm_material
GROUP BY status;

-- 2. 修复状态值（小写转大写）
SELECT '=== 开始修复 ===' AS 'Info';

UPDATE mdm_vendor SET status = UPPER(status) WHERE status REGEXP '^[a-z]';
UPDATE mdm_customer SET status = UPPER(status) WHERE status REGEXP '^[a-z]';
UPDATE mdm_material SET status = UPPER(status) WHERE status REGEXP '^[a-z]';

-- 3. 查看修复后的状态
SELECT '=== 修复后的状态 ===' AS 'Info';

SELECT 'mdm_vendor' AS table_name, status, COUNT(*) as count
FROM mdm_vendor
GROUP BY status
UNION ALL
SELECT 'mdm_customer' AS table_name, status, COUNT(*) as count
FROM mdm_customer
GROUP BY status
UNION ALL
SELECT 'mdm_material' AS table_name, status, COUNT(*) as count
FROM mdm_material
GROUP BY status;

SELECT '=== 修复完成 ===' AS 'Info';
