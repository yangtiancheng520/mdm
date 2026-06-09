-- =============================================
-- 更新采购组织值域的数据长度定义
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 1. 查看当前值域定义
SELECT '========== 当前值域定义 ==========' AS '';
SELECT
    id,
    domain_code AS '值域编码',
    domain_name AS '值域名称',
    data_type AS '类型',
    data_length AS '当前长度'
FROM std_value_domain
WHERE domain_code = 'PURCHASE_ORG';

-- 2. 查看值域项编码的实际最大长度
SELECT '========== 值域项编码最大长度 ==========' AS '';
SELECT
    MAX(LENGTH(item_code)) AS '最大编码长度',
    GROUP_CONCAT(CONCAT(item_code, '(', LENGTH(item_code), ')') SEPARATOR ', ') AS '编码列表'
FROM std_value_domain_item
WHERE domain_id = (SELECT id FROM std_value_domain WHERE domain_code = 'PURCHASE_ORG');

-- 3. 更新值域的数据长度（改为10，足够容纳所有编码）
UPDATE std_value_domain
SET data_length = 10,
    updated_at = NOW()
WHERE domain_code = 'PURCHASE_ORG';

-- 4. 同时更新字段标准的长度
UPDATE std_field_standard
SET length = 10,
    updated_at = NOW()
WHERE field_code = 'PURCHASE_ORG';

-- 5. 更新视图字段的长度
UPDATE std_view_field
SET field_length = 10,
    updated_at = NOW()
WHERE field_code = 'PURCHASE_ORG';

-- 6. 验证更新结果
SELECT '========== 更新后的定义 ==========' AS '';
SELECT
    id,
    domain_code AS '值域编码',
    domain_name AS '值域名称',
    data_type AS '类型',
    data_length AS '新长度'
FROM std_value_domain
WHERE domain_code = 'PURCHASE_ORG';

SELECT '更新完成！采购组织值域长度已改为10' AS message;
