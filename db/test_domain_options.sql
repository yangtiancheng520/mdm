-- =============================================
-- 测试值域选项接口返回的数据
-- =============================================

USE mdm;

-- 1. 查看采购组织值域定义
SELECT '========== 采购组织值域 ==========' AS '';
SELECT
    id,
    domain_code AS '值域编码',
    domain_name AS '值域名称',
    data_length AS '长度限制'
FROM std_value_domain
WHERE domain_code = 'PURCHASE_ORG';

-- 2. 查看采购组织值域项
SELECT '========== 采购组织值域项 ==========' AS '';
SELECT
    vdi.item_code AS '编码',
    vdi.item_value AS '项值',
    LENGTH(vdi.item_code) AS '编码长度',
    LENGTH(vdi.item_value) AS '项值长度'
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
WHERE vd.domain_code = 'PURCHASE_ORG'
ORDER BY vdi.sort;

-- 3. 模拟后端返回的数据格式
SELECT '========== 后端应该返回的数据 ==========' AS '';
SELECT
    vdi.item_code AS code,
    vdi.item_value AS value,
    vdi.item_value AS label,
    vdi.sort
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
WHERE vd.domain_code = 'PURCHASE_ORG'
ORDER BY vdi.sort;

SELECT '查询完成！' AS message;
