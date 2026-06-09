-- =============================================
-- 查看采购组织字段的实际存储内容
-- =============================================

USE mdm;

-- 1. 查看值域选项（后端返回给前端的数据）
SELECT '========== 1. 值域选项（后端返回） ==========' AS '';
SELECT
    vd.domain_code AS '值域编码',
    vdi.item_code AS '编码(code)',
    vdi.item_value AS '项值(value/label)',
    LENGTH(vdi.item_code) AS '编码长度',
    LENGTH(vdi.item_value) AS '项值长度'
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
WHERE vd.domain_code = 'PURCHASE_ORG'
ORDER BY vdi.sort;

-- 2. 查看字段标准定义
SELECT '========== 2. 字段标准定义 ==========' AS '';
SELECT
    field_code AS '字段编码',
    field_name AS '字段名称',
    field_type AS '类型',
    length AS '字段长度',
    domain_code AS '关联值域'
FROM std_field_standard
WHERE field_code = 'PURCHASE_ORG';

-- 3. 查看值域定义
SELECT '========== 3. 值域定义 ==========' AS '';
SELECT
    domain_code AS '值域编码',
    domain_name AS '值域名称',
    data_type AS '数据类型',
    data_length AS '值域长度'
FROM std_value_domain
WHERE domain_code = 'PURCHASE_ORG';

SELECT '查询完成！' AS message;
