-- =============================================
-- 回填历史版本视图实体的物理表名
-- =============================================

USE mdm;

-- 为历史版本的视图实体回填物理表名
UPDATE std_view_entity sve
JOIN std_view sv ON sve.view_id = sv.id
SET sve.table_name = CASE
    WHEN sve.entity_type = 'main' THEN CONCAT('mdm_', LOWER(sv.view_code))
    ELSE CONCAT('mdm_', LOWER(sv.view_code), '_', LOWER(sve.entity_code))
END
WHERE sv.status = 'history'
  AND sve.table_name IS NULL;

-- 验证更新结果
SELECT
    sv.id as view_id,
    sv.view_code,
    sv.status,
    sve.id as entity_id,
    sve.entity_code,
    sve.entity_type,
    sve.table_name
FROM std_view sv
LEFT JOIN std_view_entity sve ON sv.id = sve.view_id
WHERE sv.status = 'history'
ORDER BY sv.view_code, sve.entity_type DESC, sve.id;
