-- =============================================
-- 迁移脚本：修复 std_value_domain_item 表字段
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 1. 查看当前表结构
SELECT '========== 当前表结构 ==========' AS '';
DESC std_value_domain_item;

-- 2. 查看所有字段
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME = 'std_value_domain_item'
ORDER BY ORDINAL_POSITION;

-- 3. 添加或重命名字段
-- 如果 item_name 不存在，但有 item_value，则重命名
-- 如果 item_name 不存在，也没有 item_value，则添加新字段

-- 检查并执行修复
SET @dbname = 'mdm';
SET @tablename = 'std_value_domain_item';

-- 检查 item_name 是否存在
SET @item_name_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = 'item_name'
);

-- 检查 item_value 是否存在
SET @item_value_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = 'item_value'
);

-- 检查 item_label 是否存在
SET @item_label_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = 'item_label'
);

SELECT
    @item_name_exists AS 'item_name存在',
    @item_value_exists AS 'item_value存在',
    @item_label_exists AS 'item_label存在';

-- 执行修复逻辑
-- 如果 item_name 不存在
IF @item_name_exists = 0 THEN
    -- 如果 item_value 存在，重命名为 item_name
    IF @item_value_exists > 0 THEN
        ALTER TABLE std_value_domain_item CHANGE COLUMN item_value item_name VARCHAR(200) COMMENT '项名称';
        SELECT '已将 item_value 重命名为 item_name' AS '修复结果';
    -- 如果 item_label 存在，重命名为 item_name
    ELSEIF @item_label_exists > 0 THEN
        ALTER TABLE std_value_domain_item CHANGE COLUMN item_label item_name VARCHAR(200) COMMENT '项名称';
        SELECT '已将 item_label 重命名为 item_name' AS '修复结果';
    -- 都不存在，添加新字段
    ELSE
        ALTER TABLE std_value_domain_item ADD COLUMN item_name VARCHAR(200) COMMENT '项名称' AFTER item_code;
        SELECT '已添加 item_name 字段' AS '修复结果';
    END IF;
ELSE
    SELECT 'item_name 字段已存在，无需修复' AS '修复结果';
END IF;

-- 4. 查看修复后的表结构
SELECT '========== 修复后的表结构 ==========' AS '';
DESC std_value_domain_item;

-- 5. 验证字段
SELECT
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '类型',
    COLUMN_COMMENT AS '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME = 'std_value_domain_item'
  AND COLUMN_NAME IN ('item_code', 'item_name', 'item_value', 'item_label')
ORDER BY ORDINAL_POSITION;

SELECT '修复完成！' AS message;
