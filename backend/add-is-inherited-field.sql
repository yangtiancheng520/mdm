-- ========================================
-- 字段表新增 is_inherited 字段
-- ========================================

-- 添加字段
ALTER TABLE std_view_field
ADD COLUMN is_inherited TINYINT(1) DEFAULT 0
COMMENT '是否继承字段（0-新增，1-继承）';

-- 查看结果
SELECT '=== 字段表结构更新完成 ===' AS info;
SHOW COLUMNS FROM std_view_field LIKE 'is_inherited';

-- 更新现有数据：所有现有字段都是继承字段
UPDATE std_view_field SET is_inherited = 1 WHERE is_inherited = 0;

SELECT '=== 更新完成 ===' AS info;
