-- =============================================
-- 为视图实体表添加物理表名字段
-- =============================================

USE mdm;

-- 添加 table_name 字段到 std_view_entity 表
ALTER TABLE std_view_entity
ADD COLUMN table_name VARCHAR(100) COMMENT '物理表名' AFTER entity_type;

-- 为所有视图实体回填物理表名（包括 published、disabled、history）
UPDATE std_view_entity sve
JOIN std_view sv ON sve.view_id = sv.id
SET sve.table_name = CASE
    WHEN sve.entity_type = 'main' THEN CONCAT('mdm_', LOWER(sv.view_code))
    ELSE CONCAT('mdm_', LOWER(sv.view_code), '_', LOWER(sve.entity_code))
END
WHERE sv.status IN ('published', 'disabled', 'history');

-- 创建索引
CREATE INDEX idx_table_name ON std_view_entity(table_name);
