-- =============================================
-- 规则模板状态流转优化
-- 状态：draft(草稿) -> published(启用) -> reset(重置为草稿)
-- =============================================

SET NAMES utf8mb4;
USE mdm;

-- 修改状态字段注释
ALTER TABLE qlt_rule_template MODIFY COLUMN status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft-草稿, published-启用';

-- 将现有模板状态改为启用
UPDATE qlt_rule_template SET status = 'published' WHERE status = 'active';
UPDATE qlt_rule_template SET status = 'draft' WHERE status = 'inactive';

-- 查看结果
SELECT id, template_code, template_name, status, is_system FROM qlt_rule_template;
SELECT '状态流转优化完成！' AS message;
