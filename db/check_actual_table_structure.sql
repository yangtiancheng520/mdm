-- =============================================
-- 检查 std_value_domain_item 表的实际字段
-- =============================================

USE mdm;

-- 1. 查看表结构
DESC std_value_domain_item;

-- 2. 查看所有字段详情
SELECT
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '类型',
    IS_NULLABLE AS '允许空值',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME = 'std_value_domain_item'
ORDER BY ORDINAL_POSITION;

-- 3. 查看前5条数据
SELECT * FROM std_value_domain_item LIMIT 5;
