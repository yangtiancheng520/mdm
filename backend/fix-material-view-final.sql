-- ========================================
-- MATERIAL 视图修复脚本
-- 功能：恢复到 V1 已发布状态，删除 V2
-- ========================================

-- 1. 查看修复前的状态
SELECT '=== 修复前的状态 ===' AS info;
SELECT id, view_code, view_name, version, status, is_latest, created_at
FROM std_view
WHERE view_code = 'MATERIAL'
ORDER BY version DESC;

-- 2. 删除 V2 的所有数据
-- 先删除字段
DELETE vf FROM std_view_field vf
INNER JOIN std_view_entity ve ON vf.entity_id = ve.id
INNER JOIN std_view vd ON ve.view_id = vd.id
WHERE vd.view_code = 'MATERIAL' AND vd.version = 2;

SELECT CONCAT('已删除 V2 的字段，受影响行数: ', ROW_COUNT()) AS info;

-- 删除实体
DELETE ve FROM std_view_entity ve
INNER JOIN std_view vd ON ve.view_id = vd.id
WHERE vd.view_code = 'MATERIAL' AND vd.version = 2;

SELECT CONCAT('已删除 V2 的实体，受影响行数: ', ROW_COUNT()) AS info;

-- 删除校验规则
DELETE vv FROM std_view_validation vv
INNER JOIN std_view vd ON vv.view_id = vd.id
WHERE vd.view_code = 'MATERIAL' AND vd.version = 2;

SELECT CONCAT('已删除 V2 的校验规则，受影响行数: ', ROW_COUNT()) AS info;

-- 删除视图定义
DELETE FROM std_view
WHERE view_code = 'MATERIAL' AND version = 2;

SELECT CONCAT('已删除 V2 的视图定义，受影响行数: ', ROW_COUNT()) AS info;

-- 3. 恢复 V1 为已发布状态和最新版本
UPDATE std_view
SET status = 'published',
    is_latest = 1,
    updated_at = NOW(),
    updated_by = 'admin'
WHERE view_code = 'MATERIAL' AND version = 1;

SELECT CONCAT('已恢复 V1 为已发布状态，受影响行数: ', ROW_COUNT()) AS info;

-- 4. 查看修复后的状态
SELECT '=== 修复后的状态 ===' AS info;
SELECT id, view_code, view_name, version, status, is_latest, created_at, updated_at
FROM std_view
WHERE view_code = 'MATERIAL'
ORDER BY version DESC;

-- 5. 查看实体和字段数量
SELECT '=== 实体和字段统计 ===' AS info;
SELECT
    vd.id,
    vd.version,
    vd.status,
    COUNT(DISTINCT ve.id) AS entity_count,
    COUNT(vf.id) AS field_count
FROM std_view vd
LEFT JOIN std_view_entity ve ON ve.view_id = vd.id
LEFT JOIN std_view_field vf ON vf.entity_id = ve.id
WHERE vd.view_code = 'MATERIAL'
GROUP BY vd.id;

SELECT '=== 修复完成 ===' AS info;
