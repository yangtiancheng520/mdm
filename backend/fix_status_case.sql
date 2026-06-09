-- =============================================
-- 修复主数据状态值（统一为大写）
-- =============================================

USE mdm;

-- 显示修复前的数据
SELECT '修复前的数据：' AS '===';
SELECT
    TABLE_NAME,
    TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'mdm_%'
ORDER BY TABLE_NAME;

-- 修复所有 mdm_ 开头的物理表的状态值
-- 动态生成修复SQL并执行
SET @sql = NULL;
SELECT
    GROUP_CONCAT(
        CONCAT(
            'UPDATE ', TABLE_NAME,
            ' SET status = UPPER(status)',
            ' WHERE status IN (''draft'', ''pending'', ''active'', ''obsolete'');'
        )
        SEPARATOR ' '
    ) INTO @sql
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'mdm_%';

-- 执行修复SQL
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 显示修复后的结果
SELECT '修复后的数据：' AS '===';
SELECT
    TABLE_NAME,
    TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'mdm_%'
ORDER BY TABLE_NAME;

-- 显示修复的状态统计
SELECT '状态统计：' AS '===';
SET @stats_sql = NULL;
SELECT
    GROUP_CONCAT(
        CONCAT(
            'SELECT ''', TABLE_NAME, ''' AS table_name, status, COUNT(*) as count ',
            'FROM ', TABLE_NAME, ' GROUP BY status'
        )
        SEPARATOR ' UNION ALL '
    ) INTO @stats_sql
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'mdm_%';

PREPARE stmt FROM @stats_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
