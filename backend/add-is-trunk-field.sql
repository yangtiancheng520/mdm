-- ========================================
-- 版本管理优化：添加 is_trunk 字段
-- ========================================

-- 1. 添加 is_trunk 字段
ALTER TABLE std_view ADD COLUMN is_trunk TINYINT(1) DEFAULT 1 COMMENT '是否主干版本';

-- 2. 更新现有数据：
-- 已发布的版本设为主干
-- 其他版本设为非主干
UPDATE std_view SET is_trunk = 1 WHERE status = 'published';
UPDATE std_view SET is_trunk = 0 WHERE status IN ('draft', 'revising', 'disabled', 'history');

-- 3. 查看更新结果
SELECT '=== 更新后的数据 ===' AS info;
SELECT id, view_code, view_name, version, status, is_trunk, is_latest
FROM std_view
ORDER BY view_code, version DESC;

-- 4. 验证：确保每个视图至少有一个主干版本
SELECT '=== 验证主干版本 ===' AS info;
SELECT view_code, COUNT(*) AS trunk_count
FROM std_view
WHERE is_trunk = 1
GROUP BY view_code
HAVING trunk_count = 0;

-- 如果有视图没有主干版本，需要设置一个
-- UPDATE std_view SET is_trunk = 1 WHERE id = (SELECT MIN(id) FROM std_view WHERE view_code = 'xxx');

SELECT '=== 迁移完成 ===' AS info;
