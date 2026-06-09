-- =============================================
-- 重命名操作日志表：log_operation -> log_md_operation
-- 明确表示这是主数据模块的操作日志
-- =============================================

USE mdm;

-- 检查原表是否存在
SET @old_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = 'mdm'
    AND TABLE_NAME = 'log_operation'
);

-- 检查新表是否已存在
SET @new_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = 'mdm'
    AND TABLE_NAME = 'log_md_operation'
);

-- 如果原表存在且新表不存在，则重命名
SET @sql = IF(@old_table_exists > 0 AND @new_table_exists = 0,
    'RENAME TABLE log_operation TO log_md_operation',
    'SELECT ''表已重命名或不存在'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证重命名结果
SELECT '=== 表重命名结果 ===' AS Info;

SELECT
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
AND TABLE_NAME IN ('log_operation', 'log_md_operation')
ORDER BY TABLE_NAME;

SELECT '=== 重命名完成 ===' AS Info;
