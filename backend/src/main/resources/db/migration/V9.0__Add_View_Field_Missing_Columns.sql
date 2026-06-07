-- ==========================================
-- 为视图字段表添加缺失字段
-- ==========================================

-- 添加值域相关字段
ALTER TABLE std_view_field ADD COLUMN domain_id BIGINT COMMENT '值域ID' AFTER precision_val;
ALTER TABLE std_view_field ADD COLUMN domain_code VARCHAR(100) COMMENT '值域编码' AFTER domain_id;
ALTER TABLE std_view_field ADD COLUMN domain_name VARCHAR(200) COMMENT '值域名称' AFTER domain_code;

-- 添加自动编号相关字段
ALTER TABLE std_view_field ADD COLUMN auto_number TINYINT(1) DEFAULT 0 COMMENT '是否自动编号' AFTER is_sortable;
ALTER TABLE std_view_field ADD COLUMN encoding_rule_id BIGINT COMMENT '编码规则ID' AFTER auto_number;
ALTER TABLE std_view_field ADD COLUMN encoding_rule_name VARCHAR(200) COMMENT '编码规则名称' AFTER encoding_rule_id;

-- 添加索引
ALTER TABLE std_view_field ADD INDEX idx_domain_id (domain_id);
ALTER TABLE std_view_field ADD INDEX idx_encoding_rule_id (encoding_rule_id);

SELECT '字段添加成功！' AS message;
