-- =============================================
-- 修复 std_value_domain_item 表字段
-- 根据数据库实际情况添加缺失的字段
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 1. 先查看当前表结构
SELECT '========== 当前表结构 ==========' AS '';
DESC std_value_domain_item;

-- 2. 检查是否存在 item_code 字段
SELECT '========== 检查 item_code 字段 ==========' AS '';
SELECT COUNT(*) AS item_code_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME = 'std_value_domain_item'
  AND COLUMN_NAME = 'item_code';

-- 3. 如果 item_code 不存在，添加它
SET @item_code_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME = 'std_value_domain_item' AND COLUMN_NAME = 'item_code');
SET @sql = IF(@item_code_exists = 0,
    'ALTER TABLE std_value_domain_item ADD COLUMN item_code VARCHAR(100) COMMENT ''值域项编码'' AFTER domain_id',
    'SELECT ''item_code 字段已存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 检查是否存在 item_name 字段
SELECT '========== 检查 item_name 字段 ==========' AS '';
SELECT COUNT(*) AS item_name_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME = 'std_value_domain_item'
  AND COLUMN_NAME = 'item_name';

-- 5. 如果 item_name 不存在，检查是否有其他字段可以重命名
-- 如果存在 item_value，重命名为 item_name
SET @item_name_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME = 'std_value_domain_item' AND COLUMN_NAME = 'item_name');
SET @item_value_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME = 'std_value_domain_item' AND COLUMN_NAME = 'item_value');
SET @item_label_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME = 'std_value_domain_item' AND COLUMN_NAME = 'item_label');

-- 方案1: 如果有 item_value 但没有 item_name，重命名
SET @sql = IF(@item_name_exists = 0 AND @item_value_exists > 0,
    'ALTER TABLE std_value_domain_item CHANGE COLUMN item_value item_name VARCHAR(200) COMMENT ''项名称''',
    'SELECT ''item_name 字段已存在或 item_value 不存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 方案2: 如果有 item_label 但没有 item_name，重命名
SET @sql = IF(@item_name_exists = 0 AND @item_label_exists > 0,
    'ALTER TABLE std_value_domain_item CHANGE COLUMN item_label item_name VARCHAR(200) COMMENT ''项名称''',
    'SELECT ''item_name 字段已存在或 item_label 不存在'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 查看修复后的表结构
SELECT '========== 修复后的表结构 ==========' AS '';
DESC std_value_domain_item;

-- 7. 查看字段详情
SELECT
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '类型',
    IS_NULLABLE AS '允许空值',
    COLUMN_COMMENT AS '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME = 'std_value_domain_item'
ORDER BY ORDINAL_POSITION;

SELECT '修复完成！' AS message;
