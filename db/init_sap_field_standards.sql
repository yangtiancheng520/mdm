-- =============================================
-- SAP主数据字段标准初始化脚本
-- 包括：客户、供应商、物料主数据相关字段标准
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 获取值域ID
SET @GENDER = (SELECT id FROM std_value_domain WHERE domain_code = 'GENDER');
SET @COUNTRY = (SELECT id FROM std_value_domain WHERE domain_code = 'COUNTRY');
SET @PROVINCE = (SELECT id FROM std_value_domain WHERE domain_code = 'PROVINCE');
SET @LANGUAGE = (SELECT id FROM std_value_domain WHERE domain_code = 'LANGUAGE');
SET @CURRENCY = (SELECT id FROM std_value_domain WHERE domain_code = 'CURRENCY');
SET @YES_NO = (SELECT id FROM std_value_domain WHERE domain_code = 'YES_NO');
SET @STATUS = (SELECT id FROM std_value_domain WHERE domain_code = 'STATUS');
SET @CUSTOMER_GROUP = (SELECT id FROM std_value_domain WHERE domain_code = 'CUSTOMER_GROUP');
SET @SALES_ORG = (SELECT id FROM std_value_domain WHERE domain_code = 'SALES_ORG');
SET @DISTR_CHANNEL = (SELECT id FROM std_value_domain WHERE domain_code = 'DISTR_CHANNEL');
SET @PAYMENT_TERM = (SELECT id FROM std_value_domain WHERE domain_code = 'PAYMENT_TERM');
SET @CUSTOMER_TYPE = (SELECT id FROM std_value_domain WHERE domain_code = 'CUSTOMER_TYPE');
SET @VENDOR_GROUP = (SELECT id FROM std_value_domain WHERE domain_code = 'VENDOR_GROUP');
SET @PURCHASE_ORG = (SELECT id FROM std_value_domain WHERE domain_code = 'PURCHASE_ORG');
SET @PURCHASE_GRP = (SELECT id FROM std_value_domain WHERE domain_code = 'PURCHASE_GRP');
SET @CREDIT_LEVEL = (SELECT id FROM std_value_domain WHERE domain_code = 'CREDIT_LEVEL');
SET @MATERIAL_TYPE = (SELECT id FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE');
SET @MATERIAL_GROUP = (SELECT id FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP');
SET @BASE_UNIT = (SELECT id FROM std_value_domain WHERE domain_code = 'BASE_UNIT');
SET @INDUSTRY = (SELECT id FROM std_value_domain WHERE domain_code = 'INDUSTRY');

-- =============================================
-- 1. 客户主数据字段标准
-- =============================================

INSERT INTO std_field_standard (field_code, field_name, field_type, length, is_enum, domain_id, status, created_at, updated_at, description) VALUES
('CUSTOMER_CODE', '客户编码', 'string', 10, false, NULL, '启用', NOW(), NOW(), '客户唯一编码'),
('CUSTOMER_NAME', '客户名称', 'string', 100, false, NULL, '启用', NOW(), NOW(), '客户全称'),
('CUSTOMER_NAME2', '客户名称2', 'string', 100, false, NULL, '启用', NOW(), NOW(), '客户简称'),
('CUSTOMER_GROUP', '客户组', 'string', 9, true, @CUSTOMER_GROUP, '启用', NOW(), NOW(), '客户分组'),
('CUSTOMER_TYPE', '客户类型', 'string', 8, true, @CUSTOMER_TYPE, '启用', NOW(), NOW(), '国内/海外客户'),
('CUSTOMER_COUNTRY', '客户国家', 'string', 2, true, @COUNTRY, '启用', NOW(), NOW(), '国家代码'),
('CUSTOMER_PROVINCE', '客户省份', 'string', 2, true, @PROVINCE, '启用', NOW(), NOW(), '省份或地区'),
('CUSTOMER_CITY', '客户城市', 'string', 50, false, NULL, '启用', NOW(), NOW(), '城市名称'),
('CUSTOMER_ADDRESS', '客户地址', 'string', 200, false, NULL, '启用', NOW(), NOW(), '详细地址'),
('CUSTOMER_POSTAL_CODE', '客户邮编', 'string', 10, false, NULL, '启用', NOW(), NOW(), '邮政编码'),
('CUSTOMER_CONTACT', '客户联系人', 'string', 50, false, NULL, '启用', NOW(), NOW(), '联系人姓名'),
('CUSTOMER_PHONE', '客户电话', 'string', 20, false, NULL, '启用', NOW(), NOW(), '联系电话'),
('CUSTOMER_EMAIL', '客户邮箱', 'string', 100, false, NULL, '启用', NOW(), NOW(), '电子邮箱'),
('CUSTOMER_SALES_ORG', '客户销售组织', 'string', 8, true, @SALES_ORG, '启用', NOW(), NOW(), '销售组织'),
('CUSTOMER_DISTR_CHANNEL', '客户分销渠道', 'string', 2, true, @DISTR_CHANNEL, '启用', NOW(), NOW(), '分销渠道'),
('CUSTOMER_PAYMENT_TERM', '客户付款条款', 'string', 5, true, @PAYMENT_TERM, '启用', NOW(), NOW(), '付款条款'),
('CUSTOMER_CURRENCY', '客户货币', 'string', 3, true, @CURRENCY, '启用', NOW(), NOW(), '币种'),
('CUSTOMER_LANGUAGE', '客户语言', 'string', 2, true, @LANGUAGE, '启用', NOW(), NOW(), '语言'),
('CUSTOMER_TAX_NUMBER', '客户税号', 'string', 20, false, NULL, '启用', NOW(), NOW(), '纳税人识别号'),
('CUSTOMER_BANK_ACCOUNT', '客户银行账号', 'string', 30, false, NULL, '启用', NOW(), NOW(), '银行账号'),
('CUSTOMER_BANK_NAME', '客户开户银行', 'string', 100, false, NULL, '启用', NOW(), NOW(), '开户银行名称'),
('CUSTOMER_CREDIT_LIMIT', '客户信用额度', 'decimal', 15, false, NULL, '启用', NOW(), NOW(), '信用额度'),
('CUSTOMER_STATUS', '客户状态', 'string', 8, true, @STATUS, '启用', NOW(), NOW(), '启用状态');

-- =============================================
-- 2. 供应商主数据字段标准
-- =============================================

INSERT INTO std_field_standard (field_code, field_name, field_type, length, is_enum, domain_id, status, created_at, updated_at, description) VALUES
('VENDOR_CODE', '供应商编码', 'string', 10, false, NULL, '启用', NOW(), NOW(), '供应商唯一编码'),
('VENDOR_NAME', '供应商名称', 'string', 100, false, NULL, '启用', NOW(), NOW(), '供应商全称'),
('VENDOR_NAME2', '供应商名称2', 'string', 100, false, NULL, '启用', NOW(), NOW(), '供应商简称'),
('VENDOR_GROUP', '供应商组', 'string', 9, true, @VENDOR_GROUP, '启用', NOW(), NOW(), '供应商分组'),
('VENDOR_COUNTRY', '供应商国家', 'string', 2, true, @COUNTRY, '启用', NOW(), NOW(), '国家代码'),
('VENDOR_PROVINCE', '供应商省份', 'string', 2, true, @PROVINCE, '启用', NOW(), NOW(), '省份或地区'),
('VENDOR_CITY', '供应商城市', 'string', 50, false, NULL, '启用', NOW(), NOW(), '城市名称'),
('VENDOR_ADDRESS', '供应商地址', 'string', 200, false, NULL, '启用', NOW(), NOW(), '详细地址'),
('VENDOR_POSTAL_CODE', '供应商邮编', 'string', 10, false, NULL, '启用', NOW(), NOW(), '邮政编码'),
('VENDOR_CONTACT', '供应商联系人', 'string', 50, false, NULL, '启用', NOW(), NOW(), '联系人姓名'),
('VENDOR_PHONE', '供应商电话', 'string', 20, false, NULL, '启用', NOW(), NOW(), '联系电话'),
('VENDOR_EMAIL', '供应商邮箱', 'string', 100, false, NULL, '启用', NOW(), NOW(), '电子邮箱'),
('VENDOR_PURCHASE_ORG', '供应商采购组织', 'string', 7, true, @PURCHASE_ORG, '启用', NOW(), NOW(), '采购组织'),
('VENDOR_PURCHASE_GRP', '供应商采购组', 'string', 3, true, @PURCHASE_GRP, '启用', NOW(), NOW(), '采购组'),
('VENDOR_PAYMENT_TERM', '供应商付款条款', 'string', 5, true, @PAYMENT_TERM, '启用', NOW(), NOW(), '付款条款'),
('VENDOR_CURRENCY', '供应商货币', 'string', 3, true, @CURRENCY, '启用', NOW(), NOW(), '币种'),
('VENDOR_LANGUAGE', '供应商语言', 'string', 2, true, @LANGUAGE, '启用', NOW(), NOW(), '语言'),
('VENDOR_TAX_NUMBER', '供应商税号', 'string', 20, false, NULL, '启用', NOW(), NOW(), '纳税人识别号'),
('VENDOR_BANK_ACCOUNT', '供应商银行账号', 'string', 30, false, NULL, '启用', NOW(), NOW(), '银行账号'),
('VENDOR_BANK_NAME', '供应商开户银行', 'string', 100, false, NULL, '启用', NOW(), NOW(), '开户银行名称'),
('VENDOR_CREDIT_LEVEL', '供应商信用等级', 'string', 1, true, @CREDIT_LEVEL, '启用', NOW(), NOW(), '信用等级'),
('VENDOR_STATUS', '供应商状态', 'string', 8, true, @STATUS, '启用', NOW(), NOW(), '启用状态');

-- =============================================
-- 3. 物料主数据字段标准
-- =============================================

INSERT INTO std_field_standard (field_code, field_name, field_type, length, `precision`, is_enum, domain_id, status, created_at, updated_at, description) VALUES
('MATERIAL_CODE', '物料编码', 'string', 18, NULL, false, NULL, '启用', NOW(), NOW(), '物料唯一编码'),
('MATERIAL_NAME', '物料名称', 'string', 40, NULL, false, NULL, '启用', NOW(), NOW(), '物料描述'),
('MATERIAL_NAME2', '物料名称2', 'string', 40, NULL, false, NULL, '启用', NOW(), NOW(), '物料简称'),
('MATERIAL_TYPE', '物料类型', 'string', 4, NULL, true, @MATERIAL_TYPE, '启用', NOW(), NOW(), '物料类型'),
('MATERIAL_GROUP', '物料组', 'string', 4, NULL, true, @MATERIAL_GROUP, '启用', NOW(), NOW(), '物料组'),
('MATERIAL_BASE_UNIT', '物料基本单位', 'string', 3, NULL, true, @BASE_UNIT, '启用', NOW(), NOW(), '基本计量单位'),
('MATERIAL_INDUSTRY', '物料行业分类', 'string', 3, NULL, true, @INDUSTRY, '启用', NOW(), NOW(), '行业分类'),
('MATERIAL_GROSS_WEIGHT', '物料毛重', 'decimal', 13, 3, false, NULL, '启用', NOW(), NOW(), '毛重'),
('MATERIAL_NET_WEIGHT', '物料净重', 'decimal', 13, 3, false, NULL, '启用', NOW(), NOW(), '净重'),
('MATERIAL_WEIGHT_UNIT', '物料重量单位', 'string', 3, NULL, true, @BASE_UNIT, '启用', NOW(), NOW(), '重量单位'),
('MATERIAL_VOLUME', '物料体积', 'decimal', 13, 3, false, NULL, '启用', NOW(), NOW(), '体积'),
('MATERIAL_VOLUME_UNIT', '物料体积单位', 'string', 3, NULL, false, NULL, '启用', NOW(), NOW(), '体积单位'),
('MATERIAL_SIZE', '物料尺寸', 'string', 32, NULL, false, NULL, '启用', NOW(), NOW(), '尺寸规格'),
('MATERIAL_COLOR', '物料颜色', 'string', 20, NULL, false, NULL, '启用', NOW(), NOW(), '颜色'),
('MATERIAL_BRAND', '物料品牌', 'string', 50, NULL, false, NULL, '启用', NOW(), NOW(), '品牌'),
('MATERIAL_ORIGIN', '物料产地', 'string', 50, NULL, false, NULL, '启用', NOW(), NOW(), '原产地'),
('MATERIAL_STATUS', '物料状态', 'string', 8, NULL, true, @STATUS, '启用', NOW(), NOW(), '启用状态');

-- 查看初始化结果
SELECT '========== 字段标准初始化完成 ==========' AS '';

SELECT
    CASE
        WHEN field_code LIKE 'CUSTOMER%' THEN '客户主数据'
        WHEN field_code LIKE 'VENDOR%' THEN '供应商主数据'
        WHEN field_code LIKE 'MATERIAL%' THEN '物料主数据'
        ELSE '其他'
    END AS '字段分类',
    COUNT(*) AS '字段数量'
FROM std_field_standard
GROUP BY
    CASE
        WHEN field_code LIKE 'CUSTOMER%' THEN '客户主数据'
        WHEN field_code LIKE 'VENDOR%' THEN '供应商主数据'
        WHEN field_code LIKE 'MATERIAL%' THEN '物料主数据'
        ELSE '其他'
    END;

SELECT CONCAT('共创建 ', COUNT(*), ' 个字段标准') AS result FROM std_field_standard;

-- 查看关联值域的字段
SELECT fs.field_code, fs.field_name, vd.domain_name
FROM std_field_standard fs
LEFT JOIN std_value_domain vd ON fs.domain_id = vd.id
WHERE fs.domain_id IS NOT NULL
ORDER BY fs.field_code;
