-- =============================================
-- 检查采购组织字段长度定义
-- =============================================

USE mdm;

-- 1. 查看字段标准中的采购组织定义
SELECT '========== 字段标准中的采购组织定义 ==========' AS '';
SELECT
    id,
    field_code AS '字段编码',
    field_name AS '字段名称',
    field_type AS '类型',
    length AS '长度',
    domain_code AS '值域编码'
FROM std_field_standard
WHERE field_code = 'PURCHASE_ORG' OR field_name LIKE '%采购组织%'
ORDER BY id;

-- 2. 查看值域中的采购组织定义
SELECT '========== 值域中的采购组织定义 ==========' AS '';
SELECT
    id,
    domain_code AS '值域编码',
    domain_name AS '值域名称',
    data_type AS '类型',
    data_length AS '长度',
    status AS '状态'
FROM std_value_domain
WHERE domain_code = 'PURCHASE_ORG';

-- 3. 查看值域项中的采购组织选项
SELECT '========== 采购组织选项 ==========' AS '';
SELECT
    vdi.item_code AS '编码',
    vdi.item_value AS '项值',
    LENGTH(vdi.item_code) AS '编码长度',
    LENGTH(vdi.item_value) AS '项值长度'
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
WHERE vd.domain_code = 'PURCHASE_ORG'
ORDER BY vdi.sort;

-- 4. 查看视图字段中的采购组织定义
SELECT '========== 视图字段中的采购组织定义 ==========' AS '';
SELECT
    vf.id,
    vf.field_code AS '字段编码',
    vf.field_name AS '字段名称',
    vf.field_type AS '类型',
    vf.field_length AS '长度',
    vf.domain_code AS '值域编码',
    ve.entity_name AS '实体名称'
FROM std_view_field vf
INNER JOIN std_view_entity ve ON vf.entity_id = ve.id
WHERE vf.field_code = 'PURCHASE_ORG' OR vf.field_name LIKE '%采购组织%'
ORDER BY vf.id;

SELECT '检查完成！' AS message;