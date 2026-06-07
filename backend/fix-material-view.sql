-- ========================================
-- MATERIAL 视图修复脚本
-- ========================================

-- 1. 查看当前所有版本
SELECT '=== 当前所有版本 ===' AS info;
SELECT id, view_code, view_name, version, status, is_latest, created_at, updated_at
FROM view_definition
WHERE view_code = 'MATERIAL'
ORDER BY version DESC;

-- 2. 查看实体和字段数量
SELECT '=== 各版本的实体和字段数量 ===' AS info;
SELECT
    vd.id,
    vd.view_code,
    vd.version,
    vd.status,
    vd.is_latest,
    COUNT(DISTINCT ve.id) AS entity_count,
    COUNT(vf.id) AS field_count
FROM view_definition vd
LEFT JOIN view_entity ve ON ve.view_id = vd.id
LEFT JOIN view_field vf ON vf.entity_id = ve.id
WHERE vd.view_code = 'MATERIAL'
GROUP BY vd.id
ORDER BY vd.version DESC;

-- ========================================
-- 修复方案（请根据上面的查询结果选择执行）
-- ========================================

-- 方案1: 保留 V1（原始已发布版本），删除 V2
-- 执行前请确认 V1 的 ID

-- SET @v1_id = (SELECT id FROM view_definition WHERE view_code = 'MATERIAL' AND version = 1);
-- SET @v2_id = (SELECT id FROM view_definition WHERE view_code = 'MATERIAL' AND version = 2);

-- -- 删除 V2 的字段和实体
-- DELETE FROM view_field WHERE entity_id IN (SELECT id FROM view_entity WHERE view_id = @v2_id);
-- DELETE FROM view_entity WHERE view_id = @v2_id;
-- DELETE FROM view_validation WHERE view_id = @v2_id;
-- DELETE FROM view_definition WHERE id = @v2_id;

-- -- 恢复 V1 为已发布状态和最新版本
-- UPDATE view_definition SET status = 'published', is_latest = 1 WHERE id = @v1_id;

-- SELECT '=== 修复后的结果 ===' AS info;
-- SELECT id, view_code, view_name, version, status, is_latest FROM view_definition WHERE view_code = 'MATERIAL';

-- ========================================
-- 方案2: 保留 V2，恢复为已发布状态
-- ========================================

-- SET @v2_id = (SELECT id FROM view_definition WHERE view_code = 'MATERIAL' AND version = 2);

-- -- 删除 V1
-- SET @v1_id = (SELECT id FROM view_definition WHERE view_code = 'MATERIAL' AND version = 1);
-- DELETE FROM view_field WHERE entity_id IN (SELECT id FROM view_entity WHERE view_id = @v1_id);
-- DELETE FROM view_entity WHERE view_id = @v1_id;
-- DELETE FROM view_validation WHERE view_id = @v1_id;
-- DELETE FROM view_definition WHERE id = @v1_id;

-- -- 将 V2 改为已发布状态
-- UPDATE view_definition SET status = 'published', is_latest = 1 WHERE id = @v2_id;

-- SELECT '=== 修复后的结果 ===' AS info;
-- SELECT id, view_code, view_name, version, status, is_latest FROM view_definition WHERE view_code = 'MATERIAL';
