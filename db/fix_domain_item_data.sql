-- =============================================
-- 值域项数据检查和修复脚本
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 1. 检查表结构
SELECT '========== 1. 表结构检查 ==========' AS '';
DESC std_value_domain_item;

-- 2. 检查 item_label 字段是否存在
SELECT '========== 2. 字段检查 ==========' AS '';
SELECT
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '类型',
    IS_NULLABLE AS '允许空值',
    COLUMN_COMMENT AS '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME = 'std_value_domain_item'
  AND COLUMN_NAME IN ('item_code', 'item_value', 'item_label')
ORDER BY ORDINAL_POSITION;

-- 3. 检查数据情况
SELECT '========== 3. 数据检查 ==========' AS '';
SELECT
    vd.domain_code AS '值域编码',
    vd.domain_name AS '值域名称',
    vdi.item_code AS '编码',
    vdi.item_value AS '值',
    vdi.item_label AS '显示名',
    CASE
        WHEN vdi.item_label IS NULL THEN '⚠️ 需要修复'
        WHEN vdi.item_label = vdi.item_code THEN '⚠️ 编码和显示名相同'
        ELSE '✓ 正常'
    END AS '状态'
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
ORDER BY vd.domain_code, vdi.sort
LIMIT 50;

-- 4. 统计需要修复的数据
SELECT '========== 4. 需要修复的数据统计 ==========' AS '';
SELECT
    COUNT(*) AS 'item_label为空的项数',
    (SELECT COUNT(*) FROM std_value_domain_item) AS '总项数'
FROM std_value_domain_item
WHERE item_label IS NULL;

-- 5. 如果需要，执行修复（取消注释以下SQL来执行）
-- SELECT '========== 5. 执行修复 ==========' AS '';
--
-- -- 方案1: 如果 item_label 为空，将 item_value 复制到 item_label
-- UPDATE std_value_domain_item
-- SET item_label = item_value
-- WHERE item_label IS NULL;
--
-- -- 方案2: 如果 item_code 和 item_label 相同，说明数据有问题
-- -- 需要根据实际情况手动修复
--
-- SELECT '修复完成！' AS '';
-- SELECT COUNT(*) AS '已修复的项数' FROM std_value_domain_item WHERE item_label IS NOT NULL;

-- 6. 验证修复结果
-- SELECT '========== 6. 验证结果 ==========' AS '';
-- SELECT
--     vd.domain_code AS '值域编码',
--     vdi.item_code AS '编码',
--     vdi.item_value AS '值',
--     vdi.item_label AS '显示名'
-- FROM std_value_domain vd
-- INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
-- ORDER BY vd.domain_code, vdi.sort
-- LIMIT 20;
