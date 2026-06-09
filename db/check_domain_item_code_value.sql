-- =============================================
-- 检查值域项编码和项值是否一致
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 1. 查找编码和项值相同的值域项（问题数据）
SELECT
    vd.id AS '值域ID',
    vd.domain_code AS '值域编码',
    vd.domain_name AS '值域名称',
    vdi.id AS '项ID',
    vdi.item_code AS '编码',
    vdi.item_value AS '项值',
    CASE
        WHEN vdi.item_code = vdi.item_value THEN '❌ 相同'
        ELSE '✓ 不同'
    END AS '状态'
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
WHERE vdi.item_code = vdi.item_value
ORDER BY vd.domain_code, vdi.sort;

-- 2. 统计问题数量
SELECT
    COUNT(*) AS '编码项值相同的项数',
    (SELECT COUNT(*) FROM std_value_domain_item) AS '总项数',
    CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM std_value_domain_item), 2), '%') AS '问题比例'
FROM std_value_domain_item
WHERE item_code = item_value;

-- 3. 查看所有值域项数据
SELECT
    vd.domain_code AS '值域编码',
    vd.domain_name AS '值域名称',
    vdi.item_code AS '编码',
    vdi.item_value AS '项值',
    vdi.sort AS '排序',
    vdi.status AS '状态',
    CASE
        WHEN vdi.item_code = vdi.item_value THEN '⚠️ 需修复'
        ELSE '✓ 正常'
    END AS '检查结果'
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
ORDER BY vd.domain_code, vdi.sort;
