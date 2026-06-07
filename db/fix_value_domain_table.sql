-- ==========================================
-- 快速修复值域表结构 - 执行此脚本添加缺失字段
-- ==========================================

-- 1. 添加 data_type 字段（如果不存在）
ALTER TABLE std_value_domain
ADD COLUMN IF NOT EXISTS data_type VARCHAR(20) COMMENT '存储类型: CHAR/VARCHAR/INT/TINYINT/SMALLINT/BIGINT' AFTER domain_name;

-- 2. 添加 data_length 字段（如果不存在）
ALTER TABLE std_value_domain
ADD COLUMN IF NOT EXISTS data_length INT COMMENT '存储长度' AFTER data_type;

-- 3. 添加 options 字段（如果不存在）
ALTER TABLE std_value_domain
ADD COLUMN IF NOT EXISTS options TEXT COMMENT '选项列表JSON' AFTER data_length;

-- 4. 为字段标准表添加 domain_id 字段（如果不存在）
ALTER TABLE std_field_standard
ADD COLUMN IF NOT EXISTS domain_id BIGINT COMMENT '值域ID' AFTER reference_source;

-- 5. 为字段标准表添加 category_id 字段（如果不存在）
ALTER TABLE std_field_standard
ADD COLUMN IF NOT EXISTS category_id BIGINT COMMENT '分类ID' AFTER category;

-- 6. 设置默认值
UPDATE std_value_domain SET data_type = 'VARCHAR' WHERE data_type IS NULL OR data_type = '';

-- 7. 验证
SELECT '值域表结构修复完成！' AS message;
DESCRIBE std_value_domain;
DESCRIBE std_field_standard;
