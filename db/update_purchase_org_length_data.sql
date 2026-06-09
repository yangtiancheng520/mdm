-- =============================================
-- 更新采购组织字段标准的长度定义
-- 注意：这是更新数据，不是修改表结构
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 1. 查看当前定义
SELECT '========== 当前采购组织字段标准定义 ==========' AS '';
SELECT
    id,
    field_code AS '字段编码',
    field_name AS '字段名称',
    length AS '当前长度'
FROM std_field_standard
WHERE field_code = 'PURCHASE_ORG';

-- 2. 更新长度
UPDATE std_field_standard
SET length = 10,
    updated_at = NOW()
WHERE field_code = 'PURCHASE_ORG';

-- 3. 同时更新视图字段中的长度定义
UPDATE std_view_field
SET field_length = 10,
    updated_at = NOW()
WHERE field_code = 'PURCHASE_ORG';

-- 4. 验证更新结果
SELECT '========== 更新后的定义 ==========' AS '';
SELECT
    id,
    field_code AS '字段编码',
    field_name AS '字段名称',
    length AS '新长度'
FROM std_field_standard
WHERE field_code = 'PURCHASE_ORG';

SELECT '更新完成！采购组织字段长度已从4改为10' AS message;
