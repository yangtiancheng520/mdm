-- =============================================
-- 重新初始化字典分类表
-- 根据数据字典表(std_field_standard)中的实际字段重新分类
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- =============================================
-- 1. 清空现有的字典分类表
-- =============================================

-- 先清空字段标准表中的分类关联
UPDATE std_field_standard
SET category_id = NULL,
    category = NULL;

-- 清空字典分类表
TRUNCATE TABLE std_field_category;

SELECT '========== 已清空字典分类表 ==========' AS '';

-- =============================================
-- 2. 根据数据字典重新创建分类
-- =============================================

-- 插入新的字典分类(根据SAP主数据类型分类)
INSERT INTO std_field_category (category_code, category_name, parent_id, sort, status, created_at, updated_at, description) VALUES
-- 一级分类：按主数据类型分类
('CUSTOMER', '客户主数据', NULL, 1, '启用', NOW(), NOW(), '客户相关字段标准'),
('VENDOR', '供应商主数据', NULL, 2, '启用', NOW(), NOW(), '供应商相关字段标准'),
('MATERIAL', '物料主数据', NULL, 3, '启用', NOW(), NOW(), '物料相关字段标准'),
('BASIC', '基础信息', NULL, 4, '启用', NOW(), NOW(), '通用基础信息字段'),
('FINANCE', '财务信息', NULL, 5, '启用', NOW(), NOW(), '财务相关字段'),
('LOGISTICS', '物流信息', NULL, 6, '启用', NOW(), NOW(), '物流相关字段');

-- 二级分类：客户主数据细分
SET @customer_id = (SELECT id FROM std_field_category WHERE category_code = 'CUSTOMER');
INSERT INTO std_field_category (category_code, category_name, parent_id, sort, status, created_at, updated_at, description) VALUES
('CUSTOMER_BASIC', '客户基本信息', @customer_id, 1, '启用', NOW(), NOW(), '客户基本属性'),
('CUSTOMER_ADDRESS', '客户地址信息', @customer_id, 2, '启用', NOW(), NOW(), '客户地址相关'),
('CUSTOMER_CONTACT', '客户联系信息', @customer_id, 3, '启用', NOW(), NOW(), '客户联系方式'),
('CUSTOMER_SALES', '客户销售信息', @customer_id, 4, '启用', NOW(), NOW(), '客户销售相关'),
('CUSTOMER_FINANCE', '客户财务信息', @customer_id, 5, '启用', NOW(), NOW(), '客户财务相关');

-- 二级分类：供应商主数据细分
SET @vendor_id = (SELECT id FROM std_field_category WHERE category_code = 'VENDOR');
INSERT INTO std_field_category (category_code, category_name, parent_id, sort, status, created_at, updated_at, description) VALUES
('VENDOR_BASIC', '供应商基本信息', @vendor_id, 1, '启用', NOW(), NOW(), '供应商基本属性'),
('VENDOR_ADDRESS', '供应商地址信息', @vendor_id, 2, '启用', NOW(), NOW(), '供应商地址相关'),
('VENDOR_CONTACT', '供应商联系信息', @vendor_id, 3, '启用', NOW(), NOW(), '供应商联系方式'),
('VENDOR_PURCHASE', '供应商采购信息', @vendor_id, 4, '启用', NOW(), NOW(), '供应商采购相关'),
('VENDOR_FINANCE', '供应商财务信息', @vendor_id, 5, '启用', NOW(), NOW(), '供应商财务相关');

-- 二级分类：物料主数据细分
SET @material_id = (SELECT id FROM std_field_category WHERE category_code = 'MATERIAL');
INSERT INTO std_field_category (category_code, category_name, parent_id, sort, status, created_at, updated_at, description) VALUES
('MATERIAL_BASIC', '物料基本信息', @material_id, 1, '启用', NOW(), NOW(), '物料基本属性'),
('MATERIAL_PHYSICAL', '物料物理属性', @material_id, 2, '启用', NOW(), NOW(), '物料物理特性'),
('MATERIAL_CLASSIFY', '物料分类属性', @material_id, 3, '启用', NOW(), NOW(), '物料分类信息');

SELECT '========== 字典分类创建完成 ==========' AS '';

-- =============================================
-- 3. 更新字段标准的分类关联
-- =============================================

-- 3.1 客户主数据字段分类
-- 基本信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'CUSTOMER_BASIC'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('CUSTOMER_CODE', 'CUSTOMER_NAME', 'CUSTOMER_NAME2', 'CUSTOMER_GROUP', 'CUSTOMER_TYPE', 'CUSTOMER_STATUS');

-- 地址信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'CUSTOMER_ADDRESS'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('CUSTOMER_COUNTRY', 'CUSTOMER_PROVINCE', 'CUSTOMER_CITY', 'CUSTOMER_ADDRESS', 'CUSTOMER_POSTAL_CODE');

-- 联系信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'CUSTOMER_CONTACT'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('CUSTOMER_CONTACT', 'CUSTOMER_PHONE', 'CUSTOMER_EMAIL', 'CUSTOMER_LANGUAGE');

-- 销售信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'CUSTOMER_SALES'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('CUSTOMER_SALES_ORG', 'CUSTOMER_DISTR_CHANNEL', 'CUSTOMER_CURRENCY');

-- 财务信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'CUSTOMER_FINANCE'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('CUSTOMER_PAYMENT_TERM', 'CUSTOMER_TAX_NUMBER', 'CUSTOMER_BANK_ACCOUNT', 'CUSTOMER_BANK_NAME', 'CUSTOMER_CREDIT_LIMIT');

-- 3.2 供应商主数据字段分类
-- 基本信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'VENDOR_BASIC'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('VENDOR_CODE', 'VENDOR_NAME', 'VENDOR_NAME2', 'VENDOR_GROUP', 'VENDOR_STATUS');

-- 地址信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'VENDOR_ADDRESS'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('VENDOR_COUNTRY', 'VENDOR_PROVINCE', 'VENDOR_CITY', 'VENDOR_ADDRESS', 'VENDOR_POSTAL_CODE');

-- 联系信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'VENDOR_CONTACT'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('VENDOR_CONTACT', 'VENDOR_PHONE', 'VENDOR_EMAIL', 'VENDOR_LANGUAGE');

-- 采购信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'VENDOR_PURCHASE'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('VENDOR_PURCHASE_ORG', 'VENDOR_PURCHASE_GRP', 'VENDOR_CURRENCY');

-- 财务信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'VENDOR_FINANCE'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('VENDOR_PAYMENT_TERM', 'VENDOR_TAX_NUMBER', 'VENDOR_BANK_ACCOUNT', 'VENDOR_BANK_NAME', 'VENDOR_CREDIT_LEVEL');

-- 3.3 物料主数据字段分类
-- 基本信息
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'MATERIAL_BASIC'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('MATERIAL_CODE', 'MATERIAL_NAME', 'MATERIAL_NAME2', 'MATERIAL_STATUS');

-- 物理属性
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'MATERIAL_PHYSICAL'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('MATERIAL_GROSS_WEIGHT', 'MATERIAL_NET_WEIGHT', 'MATERIAL_WEIGHT_UNIT',
                        'MATERIAL_VOLUME', 'MATERIAL_VOLUME_UNIT', 'MATERIAL_SIZE',
                        'MATERIAL_COLOR', 'MATERIAL_BRAND', 'MATERIAL_ORIGIN');

-- 分类属性
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'MATERIAL_CLASSIFY'
SET fs.category_id = fc.id,
    fs.category = fc.category_name
WHERE fs.field_code IN ('MATERIAL_TYPE', 'MATERIAL_GROUP', 'MATERIAL_BASE_UNIT', 'MATERIAL_INDUSTRY');

SELECT '========== 字段分类关联完成 ==========' AS '';

-- =============================================
-- 4. 查看分类结果统计
-- =============================================

-- 4.1 查看所有分类
SELECT '========== 字典分类列表 ==========' AS '';
SELECT
    fc.id,
    fc.category_code AS '分类编码',
    fc.category_name AS '分类名称',
    IFNULL(p.category_name, '-') AS '父分类',
    fc.sort AS '排序',
    fc.status AS '状态',
    fc.description AS '描述'
FROM std_field_category fc
LEFT JOIN std_field_category p ON fc.parent_id = p.id
ORDER BY IFNULL(fc.parent_id, 0), fc.sort;

-- 4.2 统计各分类的字段数量
SELECT '========== 各分类字段统计 ==========' AS '';
SELECT
    fc.category_name AS '分类名称',
    COUNT(fs.id) AS '字段数量'
FROM std_field_category fc
LEFT JOIN std_field_standard fs ON fs.category_id = fc.id
WHERE fc.parent_id IS NOT NULL  -- 只显示二级分类
GROUP BY fc.id, fc.category_name
ORDER BY fc.sort;

-- 4.3 查看详细的字段分类情况
SELECT '========== 字段分类详情 ==========' AS '';
SELECT
    p.category_name AS '主分类',
    fc.category_name AS '子分类',
    fs.field_code AS '字段编码',
    fs.field_name AS '字段名称',
    fs.field_type AS '字段类型'
FROM std_field_standard fs
INNER JOIN std_field_category fc ON fs.category_id = fc.id
LEFT JOIN std_field_category p ON fc.parent_id = p.id
ORDER BY p.sort, fc.sort, fs.field_code;

-- 4.4 查看未分类的字段
SELECT '========== 未分类字段 ==========' AS '';
SELECT field_code, field_name, category, category_id
FROM std_field_standard
WHERE category_id IS NULL;

SELECT CONCAT('分类初始化完成! 共创建 ',
    (SELECT COUNT(*) FROM std_field_category), ' 个分类, 已分类 ',
    (SELECT COUNT(*) FROM std_field_standard WHERE category_id IS NOT NULL), ' 个字段') AS result;
