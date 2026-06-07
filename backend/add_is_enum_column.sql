-- 添加 is_enum 字段到字段标准表
ALTER TABLE std_field_standard ADD COLUMN is_enum TINYINT DEFAULT 0 COMMENT '是否关联值域: 0-否 1-是';

-- 如果需要删除废弃字段（可选）
-- ALTER TABLE std_field_standard DROP COLUMN IF EXISTS is_required;
-- ALTER TABLE std_field_standard DROP COLUMN IF EXISTS validation_rule;
-- ALTER TABLE std_field_standard DROP COLUMN IF EXISTS reference_id;
-- ALTER TABLE std_field_standard DROP COLUMN IF EXISTS reference_source;
