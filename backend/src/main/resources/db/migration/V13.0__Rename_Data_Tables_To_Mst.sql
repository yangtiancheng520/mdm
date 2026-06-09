-- =============================================
-- 重命名 data_ 表为 mst_ 前缀
-- =============================================

USE mdm;

-- 1. 重命名 data_category 表
RENAME TABLE data_category TO mst_data_category;

-- 2. 重命名 data_operation_log 表
RENAME TABLE data_operation_log TO mst_operation_log;

-- 验证结果
SELECT 'Tables with mst_ prefix:' as info;
SHOW TABLES LIKE 'mst%';

SELECT 'Tables with data_ prefix:' as info;
SHOW TABLES LIKE 'data%';
