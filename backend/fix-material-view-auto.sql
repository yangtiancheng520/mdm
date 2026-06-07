-- ========================================
-- MATERIAL 视图自动修复脚本
-- 功能：恢复到 V1 已发布状态，删除 V2
-- ========================================

-- 开始事务
START TRANSACTION;

-- 1. 查看修复前的状态
SELECT '=== 修复前的状态 ===' AS info;
SELECT id, view_code, view_name, version, status, is_latest, created_at
FROM view_definition
WHERE view_code = 'MATERIAL'
ORDER BY version DESC;

-- 2. 获取版本 ID
SET @v1_id = (SELECT id FROM view_definition WHERE view_code = 'MATERIAL' AND version = 1 LIMIT 1);
SET @v2_id = (SELECT id FROM view_definition WHERE view_code = 'MATERIAL' AND version = 2 LIMIT 1);

SELECT CONCAT('V1 ID: ', IFNULL(@v1_id, 'NULL')) AS info;
SELECT CONCAT('V2 ID: ', IFNULL(@v2_id, 'NULL')) AS info;

-- 3. 删除 V2（如果存在）
IF @v2_id IS NOT NULL THEN
    -- 删除 V2 的字段
    DELETE FROM view_field WHERE entity_id IN (SELECT id FROM view_entity WHERE view_id = @v2_id);
    SELECT CONCAT('已删除 V2 的字段，受影响行数: ', ROW_COUNT()) AS info;

    -- 删除 V2 的实体
    DELETE FROM view_entity WHERE view_id = @v2_id;
    SELECT CONCAT('已删除 V2 的实体，受影响行数: ', ROW_COUNT()) AS info;

    -- 删除 V2 的校验规则
    DELETE FROM view_validation WHERE view_id = @v2_id;
    SELECT CONCAT('已删除 V2 的校验规则，受影响行数: ', ROW_COUNT()) AS info;

    -- 删除 V2 的视图定义
    DELETE FROM view_definition WHERE id = @v2_id;
    SELECT CONCAT('已删除 V2 的视图定义，受影响行数: ', ROW_COUNT()) AS info;
END IF;

-- 4. 恢复 V1 为已发布状态和最新版本
IF @v1_id IS NOT NULL THEN
    UPDATE view_definition
    SET status = 'published',
        is_latest = 1,
        updated_at = NOW(),
        updated_by = 'admin'
    WHERE id = @v1_id;

    SELECT CONCAT('已恢复 V1 为已发布状态，受影响行数: ', ROW_COUNT()) AS info;
END IF;

-- 5. 查看修复后的状态
SELECT '=== 修复后的状态 ===' AS info;
SELECT id, view_code, view_name, version, status, is_latest, created_at, updated_at
FROM view_definition
WHERE view_code = 'MATERIAL'
ORDER BY version DESC;

-- 6. 查看实体和字段数量
SELECT '=== 实体和字段统计 ===' AS info;
SELECT
    vd.id,
    vd.version,
    vd.status,
    COUNT(DISTINCT ve.id) AS entity_count,
    COUNT(vf.id) AS field_count
FROM view_definition vd
LEFT JOIN view_entity ve ON ve.view_id = vd.id
LEFT JOIN view_field vf ON vf.entity_id = ve.id
WHERE vd.view_code = 'MATERIAL'
GROUP BY vd.id;

-- 提交事务
COMMIT;

SELECT '=== 修复完成 ===' AS info;
