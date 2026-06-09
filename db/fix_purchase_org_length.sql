-- =============================================
-- 修复采购组织字段长度
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 1. 查看当前采购组织字段定义
SELECT '========== 当前字段标准定义 ==========' AS '';
SELECT
    id,
    field_code AS '字段编码',
    field_name AS '字段名称',
    field_type AS '类型',
    length AS '长度'
FROM std_field_standard
WHERE field_code = 'PURCHASE_ORG';

-- 2. 查看值域中采购组织的编码长度
SELECT '========== 值域项编码长度 ==========' AS '';
SELECT
    vdi.item_code AS '编码',
    LENGTH(vdi.item_code) AS '编码长度',
    vdi.item_value AS '项值'
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
WHERE vd.domain_code = 'PURCHASE_ORG'
ORDER BY LENGTH(vdi.item_code) DESC
LIMIT 1;

-- 3. 更新字段标准中的长度
SELECT '========== 执行修复 ==========' AS '';
UPDATE std_field_standard
SET length = 10
WHERE field_code = 'PURCHASE_ORG';

-- 4. 更新视图字段中的长度
UPDATE std_view_field
SET field_length = 10
WHERE field_code = 'PURCHASE_ORG';

-- 5. 验证修复结果
SELECT '========== 修复后的定义 ==========' AS '';
SELECT
    id,
    field_code AS '字段编码',
    field_name AS '字段名称',
    field_type AS '类型',
    length AS '长度'
FROM std_field_standard
WHERE field_code = 'PURCHASE_ORG';

SELECT '修复完成！采购组织字段长度已更新为10个字符' AS message;
