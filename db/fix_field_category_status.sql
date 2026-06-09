-- ==========================================
-- 修复字段分类表状态值
-- 将 '启用'/'停用' 改回 'active'/'inactive'
-- 原因: 后端代码查询时使用的是 'active'
-- ==========================================

USE mdm;

-- 更新状态值
UPDATE std_field_category SET status = 'active' WHERE status = '启用';
UPDATE std_field_category SET status = 'inactive' WHERE status = '停用';

-- 验证结果
SELECT '========== 修复完成 ==========' AS '';
SELECT id, category_code, category_name, status, sort FROM std_field_category ORDER BY sort;
