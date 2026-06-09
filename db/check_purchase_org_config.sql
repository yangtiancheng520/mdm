-- =============================================
-- 查看采购组织的完整配置
-- =============================================

USE mdm;

-- 1. 查看值域定义
SELECT '========== 1. 值域定义 ==========' AS '';
SELECT
    id,
    domain_code AS '值域编码',
    domain_name AS '值域名称',
    data_type AS '类型',
    data_length AS '值域长度限制'
FROM std_value_domain
WHERE domain_code = 'PURCHASE_ORG';

-- 2. 查看所有值域项
SELECT '========== 2. 所有值域项 ==========' AS '';
SELECT
    vdi.id,
    vdi.item_code AS '编码',
    vdi.item_value AS '项值',
    LENGTH(vdi.item_code) AS '编码长度',
    LENGTH(vdi.item_value) AS '项值长度',
    vdi.status AS '状态'
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
WHERE vd.domain_code = 'PURCHASE_ORG'
ORDER BY vdi.sort;

-- 3. 查看字段标准定义
SELECT '========== 3. 字段标准定义 ==========' AS '';
SELECT
    id,
    field_code AS '字段编码',
    field_name AS '字段名称',
    length AS '字段长度限制'
FROM std_field_standard
WHERE field_code = 'PURCHASE_ORG';

-- 4. 查看视图字段定义
SELECT '========== 4. 视图字段定义 ==========' AS '';
SELECT
    vf.id,
    vf.field_code AS '字段编码',
    vf.field_name AS '字段名称',
    vf.field_length AS '字段长度限制',
    ve.entity_name AS '所属实体'
FROM std_view_field vf
INNER JOIN std_view_entity ve ON vf.entity_id = ve.id
WHERE vf.field_code = 'PURCHASE_ORG';

-- 5. 检查是否有超长的编码
SELECT '========== 5. 超长编码检查 ==========' AS '';
SELECT
    vdi.item_code AS '超长编码',
    LENGTH(vdi.item_code) AS '编码长度',
    vd.data_length AS '限制长度',
    CONCAT('编码长度', LENGTH(vdi.item_code), '超过限制', vd.data_length) AS '问题'
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
WHERE vd.domain_code = 'PURCHASE_ORG'
  AND LENGTH(vdi.item_code) > vd.data_length;

SELECT '查询完成！' AS message;
