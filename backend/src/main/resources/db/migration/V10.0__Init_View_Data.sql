-- ==========================================
-- 初始化视图分类和视图数据
-- ==========================================

SET NAMES utf8mb4;

-- 1. 创建视图分类
INSERT INTO std_view_category (category_code, category_name, parent_id, sort, status, created_by, created_at, description) VALUES
('MAT', '物料管理', NULL, 1, '启用', 'admin', NOW(), '物料相关视图'),
('SD', '销售管理', NULL, 2, '启用', 'admin', NOW(), '销售相关视图'),
('MM', '采购管理', NULL, 3, '启用', 'admin', NOW(), '采购相关视图');

-- 2. 创建视图定义
INSERT INTO std_view (view_code, view_name, category_id, version, status, created_by, created_at, updated_by, updated_at, description) VALUES
('MATERIAL', '物料视图', 1, 1, 'draft', 'admin', NOW(), 'admin', NOW(), '物料主数据视图，包含基本信息、销售、采购、库存等多个子表'),
('CUSTOMER', '客户视图', 2, 1, 'draft', 'admin', NOW(), 'admin', NOW(), '客户主数据视图，包含基本信息、销售区域、银行信息等子表'),
('VENDOR', '供应商视图', 3, 1, 'draft', 'admin', NOW(), 'admin', NOW(), '供应商主数据视图，包含基本信息、采购组织、银行信息等子表');

-- ==========================================
-- 3. 物料视图 (view_id = 1)
-- ==========================================

-- 3.1 物料主表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, sort, status, created_by, created_at) VALUES
(1, 'MATERIAL_MAIN', '物料基本信息', 'main', 0, '启用', 'admin', NOW());

-- 3.2 物料子表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, sort, status, created_by, created_at) VALUES
(1, 'MATERIAL_SALES', '销售视图', 'sub', 1, '启用', 'admin', NOW()),
(1, 'MATERIAL_PURCHASE', '采购视图', 'sub', 2, '启用', 'admin', NOW()),
(1, 'MATERIAL_STORAGE', '库存视图', 'sub', 3, '启用', 'admin', NOW());

-- 3.3 物料主表字段 (entity_id = 1)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(1, 25, 'MATNR', '物料编码', 'string', 18, NULL, NULL, NULL, NULL, 1, 1, 1, '启用', 'admin', NOW()),
(1, 26, 'MAKTX', '物料描述', 'string', 40, NULL, NULL, NULL, NULL, 2, 1, 1, '启用', 'admin', NOW()),
(1, 19, 'NAME_EN', '英文名称', 'string', 80, NULL, NULL, NULL, NULL, 3, 0, 0, '启用', 'admin', NOW()),
(1, 27, 'MTART', '物料类型', 'string', 4, NULL, 65, 'MATERIAL_TYPE', '物料类型', 4, 1, 1, '启用', 'admin', NOW()),
(1, 28, 'MATKL', '物料组', 'string', 9, NULL, 66, 'MATERIAL_GROUP', '物料组', 5, 1, 0, '启用', 'admin', NOW()),
(1, 29, 'MEINS', '基本单位', 'string', 3, NULL, 67, 'BASE_UNIT', '基本单位', 6, 1, 0, '启用', 'admin', NOW()),
(1, 22, 'STATUS', '状态', 'string', 1, NULL, 64, 'STATUS', '启用状态', 7, 1, 1, '启用', 'admin', NOW()),
(1, 38, 'BISMT', '旧物料号', 'string', 18, NULL, NULL, NULL, NULL, 8, 0, 0, '启用', 'admin', NOW()),
(1, 21, 'DESCRIPTION', '描述', 'text', NULL, NULL, NULL, NULL, NULL, 9, 0, 0, '启用', 'admin', NOW()),
(1, 23, 'CREATE_DATE', '创建日期', 'date', NULL, NULL, NULL, NULL, NULL, 10, 0, 0, '启用', 'admin', NOW()),
(1, 24, 'CHANGE_DATE', '修改日期', 'date', NULL, NULL, NULL, NULL, NULL, 11, 0, 0, '启用', 'admin', NOW());

-- 3.4 物料销售子表字段 (entity_id = 2)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(2, 58, 'SALES_ORG', '销售组织', 'string', 4, NULL, 71, 'SALES_ORG', '销售组织', 1, 1, 1, '启用', 'admin', NOW()),
(2, 59, 'DISTR_CHANNEL', '分销渠道', 'string', 2, NULL, NULL, NULL, NULL, 2, 1, 0, '启用', 'admin', NOW()),
(2, 60, 'DIVISION', '产品组', 'string', 2, NULL, NULL, NULL, NULL, 3, 0, 0, '启用', 'admin', NOW()),
(2, 61, 'SALES_OFFICE', '销售办公室', 'string', 4, NULL, NULL, NULL, NULL, 4, 0, 0, '启用', 'admin', NOW()),
(2, 62, 'SALES_GROUP', '销售组', 'string', 3, NULL, NULL, NULL, NULL, 5, 0, 0, '启用', 'admin', NOW()),
(2, 91, 'MIN_ORDER_QTY', '最小订单量', 'decimal', 13, 2, NULL, NULL, NULL, 6, 0, 0, '启用', 'admin', NOW());

-- 3.5 物料采购子表字段 (entity_id = 3)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(3, 70, 'PURCHASE_ORG', '采购组织', 'string', 4, NULL, 72, 'PURCHASE_ORG', '采购组织', 1, 1, 1, '启用', 'admin', NOW()),
(3, 71, 'PURCHASE_GRP', '采购组', 'string', 3, NULL, NULL, NULL, NULL, 2, 1, 0, '启用', 'admin', NOW()),
(3, 74, 'GR_BASEDIV', '收货后结算', 'string', 1, NULL, 63, 'YES_NO', '是/否', 3, 0, 0, '启用', 'admin', NOW()),
(3, 75, 'AUTO_PO', '自动生成PO', 'string', 1, NULL, 63, 'YES_NO', '是/否', 4, 0, 0, '启用', 'admin', NOW()),
(3, 92, 'MIN_DELIVERY_QTY', '最小交货量', 'decimal', 13, 2, NULL, NULL, NULL, 5, 0, 0, '启用', 'admin', NOW()),
(3, 93, 'MAX_DELIVERY_QTY', '最大交货量', 'decimal', 13, 2, NULL, NULL, NULL, 6, 0, 0, '启用', 'admin', NOW());

-- 3.6 物料库存子表字段 (entity_id = 4)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(4, 36, 'WERKS', '工厂', 'string', 4, NULL, NULL, NULL, NULL, 1, 1, 1, '启用', 'admin', NOW()),
(4, 37, 'LGORT', '库存地点', 'string', 4, NULL, NULL, NULL, NULL, 2, 1, 0, '启用', 'admin', NOW()),
(4, 31, 'BRGEW', '毛重', 'decimal', 13, 3, NULL, NULL, NULL, 3, 0, 0, '启用', 'admin', NOW()),
(4, 32, 'NTGEW', '净重', 'decimal', 13, 3, NULL, NULL, NULL, 4, 0, 0, '启用', 'admin', NOW()),
(4, 30, 'GEWEI', '重量单位', 'string', 3, NULL, 67, 'BASE_UNIT', '基本单位', 5, 0, 0, '启用', 'admin', NOW()),
(4, 33, 'VOLUM', '体积', 'decimal', 13, 3, NULL, NULL, NULL, 6, 0, 0, '启用', 'admin', NOW()),
(4, 34, 'VOLEH', '体积单位', 'string', 3, NULL, NULL, NULL, NULL, 7, 0, 0, '启用', 'admin', NOW()),
(4, 35, 'GROES', '尺寸', 'string', 32, NULL, NULL, NULL, NULL, 8, 0, 0, '启用', 'admin', NOW());

-- ==========================================
-- 4. 客户视图 (view_id = 2)
-- ==========================================

-- 4.1 客户主表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, sort, status, created_by, created_at) VALUES
(2, 'CUSTOMER_MAIN', '客户基本信息', 'main', 0, '启用', 'admin', NOW());

-- 4.2 客户子表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, sort, status, created_by, created_at) VALUES
(2, 'CUSTOMER_SALES', '销售区域', 'sub', 1, '启用', 'admin', NOW()),
(2, 'CUSTOMER_BANK', '银行信息', 'sub', 2, '启用', 'admin', NOW()),
(2, 'CUSTOMER_CREDIT', '信用管理', 'sub', 3, '启用', 'admin', NOW());

-- 4.3 客户主表字段 (entity_id = 5)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(5, 39, 'PARTNER_CODE', '客户编码', 'string', 10, NULL, NULL, NULL, NULL, 1, 1, 1, '启用', 'admin', NOW()),
(5, 40, 'PARTNER_NAME', '客户名称', 'string', 80, NULL, NULL, NULL, NULL, 2, 1, 1, '启用', 'admin', NOW()),
(5, 19, 'NAME_EN', '英文名称', 'string', 80, NULL, NULL, NULL, NULL, 3, 0, 0, '启用', 'admin', NOW()),
(5, 20, 'SHORT_TEXT', '简称', 'string', 20, NULL, NULL, NULL, NULL, 4, 0, 0, '启用', 'admin', NOW()),
(5, 63, 'CUSTOMER_GROUP', '客户组', 'string', 2, NULL, 68, 'CUSTOMER_GROUP', '客户组', 5, 1, 0, '启用', 'admin', NOW()),
(5, 54, 'INDUSTRY', '行业分类', 'string', 4, NULL, 75, 'INDUSTRY', '行业分类', 6, 0, 1, '启用', 'admin', NOW()),
(5, 22, 'STATUS', '状态', 'string', 1, NULL, 64, 'STATUS', '启用状态', 7, 1, 1, '启用', 'admin', NOW()),
(5, 42, 'STREET', '街道', 'string', 60, NULL, NULL, NULL, NULL, 8, 0, 0, '启用', 'admin', NOW()),
(5, 43, 'CITY', '城市', 'string', 40, NULL, NULL, NULL, NULL, 9, 0, 0, '启用', 'admin', NOW()),
(5, 44, 'POSTAL_CODE', '邮政编码', 'string', 10, NULL, NULL, NULL, NULL, 10, 0, 0, '启用', 'admin', NOW()),
(5, 45, 'COUNTRY', '国家', 'string', 3, NULL, 61, 'COUNTRY', '国家代码', 11, 0, 0, '启用', 'admin', NOW()),
(5, 46, 'REGION', '地区/省份', 'string', 3, NULL, 62, 'PROVINCE', '省份', 12, 0, 0, '启用', 'admin', NOW()),
(5, 47, 'TELEPHONE', '电话', 'string', 30, NULL, NULL, NULL, NULL, 13, 0, 0, '启用', 'admin', NOW()),
(5, 49, 'EMAIL', '电子邮箱', 'string', 240, NULL, NULL, NULL, NULL, 14, 0, 0, '启用', 'admin', NOW()),
(5, 50, 'WEBSITE', '网站', 'string', 120, NULL, NULL, NULL, NULL, 15, 0, 0, '启用', 'admin', NOW()),
(5, 51, 'TAX_NUMBER', '税号', 'string', 18, NULL, NULL, NULL, NULL, 16, 0, 0, '启用', 'admin', NOW());

-- 4.4 客户销售区域子表字段 (entity_id = 6)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(6, 58, 'SALES_ORG', '销售组织', 'string', 4, NULL, 71, 'SALES_ORG', '销售组织', 1, 1, 1, '启用', 'admin', NOW()),
(6, 59, 'DISTR_CHANNEL', '分销渠道', 'string', 2, NULL, NULL, NULL, NULL, 2, 1, 0, '启用', 'admin', NOW()),
(6, 60, 'DIVISION', '产品组', 'string', 2, NULL, NULL, NULL, NULL, 3, 0, 0, '启用', 'admin', NOW()),
(6, 65, 'INCOTERM1', '国际贸易术语', 'string', 3, NULL, 78, 'INCOTERM', '国际贸易术语', 4, 0, 0, '启用', 'admin', NOW()),
(6, 66, 'INCOTERM2', '国际贸易术语地点', 'string', 28, NULL, NULL, NULL, NULL, 5, 0, 0, '启用', 'admin', NOW()),
(6, 68, 'SHIPPING_COND', '装运条件', 'string', 2, NULL, NULL, NULL, NULL, 6, 0, 0, '启用', 'admin', NOW()),
(6, 87, 'SHIP_MODE', '运输方式', 'string', 2, NULL, 77, 'SHIP_MODE', '运输方式', 7, 0, 0, '启用', 'admin', NOW()),
(6, 78, 'PAYMENT_TERM', '付款条款', 'string', 4, NULL, 73, 'PAYMENT_TERM', '付款条款', 8, 0, 0, '启用', 'admin', NOW());

-- 4.5 客户银行信息子表字段 (entity_id = 7)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(7, 82, 'BANK_COUNTRY', '银行国家', 'string', 3, NULL, 61, 'COUNTRY', '国家代码', 1, 1, 0, '启用', 'admin', NOW()),
(7, 83, 'BANK_KEY', '银行代码', 'string', 15, NULL, NULL, NULL, NULL, 2, 1, 0, '启用', 'admin', NOW()),
(7, 84, 'BANK_ACCOUNT', '银行账号', 'string', 18, NULL, NULL, NULL, NULL, 3, 1, 0, '启用', 'admin', NOW()),
(7, 85, 'BANK_NAME', '银行名称', 'string', 60, NULL, NULL, NULL, NULL, 4, 0, 0, '启用', 'admin', NOW()),
(7, 86, 'ACCOUNT_HOLDER', '账户持有人', 'string', 60, NULL, NULL, NULL, NULL, 5, 0, 0, '启用', 'admin', NOW());

-- 4.6 客户信用管理子表字段 (entity_id = 8)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(8, 76, 'COMPANY_CODE', '公司代码', 'string', 4, NULL, 70, 'COMPANY_CODE', '公司代码', 1, 1, 1, '启用', 'admin', NOW()),
(8, 79, 'CREDIT_LIMIT', '信用额度', 'decimal', 15, 2, NULL, NULL, NULL, 2, 0, 0, '启用', 'admin', NOW()),
(8, 80, 'CREDIT_LEVEL', '信用等级', 'string', 1, NULL, 76, 'CREDIT_LEVEL', '信用等级', 3, 0, 0, '启用', 'admin', NOW());

-- ==========================================
-- 5. 供应商视图 (view_id = 3)
-- ==========================================

-- 5.1 供应商主表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, sort, status, created_by, created_at) VALUES
(3, 'VENDOR_MAIN', '供应商基本信息', 'main', 0, '启用', 'admin', NOW());

-- 5.2 供应商子表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, sort, status, created_by, created_at) VALUES
(3, 'VENDOR_PURCHASE', '采购组织', 'sub', 1, '启用', 'admin', NOW()),
(3, 'VENDOR_BANK', '银行信息', 'sub', 2, '启用', 'admin', NOW());

-- 5.3 供应商主表字段 (entity_id = 9)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(9, 39, 'PARTNER_CODE', '供应商编码', 'string', 10, NULL, NULL, NULL, NULL, 1, 1, 1, '启用', 'admin', NOW()),
(9, 40, 'PARTNER_NAME', '供应商名称', 'string', 80, NULL, NULL, NULL, NULL, 2, 1, 1, '启用', 'admin', NOW()),
(9, 19, 'NAME_EN', '英文名称', 'string', 80, NULL, NULL, NULL, NULL, 3, 0, 0, '启用', 'admin', NOW()),
(9, 20, 'SHORT_TEXT', '简称', 'string', 20, NULL, NULL, NULL, NULL, 4, 0, 0, '启用', 'admin', NOW()),
(9, 72, 'VENDOR_GROUP', '供应商组', 'string', 4, NULL, 69, 'VENDOR_GROUP', '供应商组', 5, 1, 0, '启用', 'admin', NOW()),
(9, 54, 'INDUSTRY', '行业分类', 'string', 4, NULL, 75, 'INDUSTRY', '行业分类', 6, 0, 1, '启用', 'admin', NOW()),
(9, 22, 'STATUS', '状态', 'string', 1, NULL, 64, 'STATUS', '启用状态', 7, 1, 1, '启用', 'admin', NOW()),
(9, 42, 'STREET', '街道', 'string', 60, NULL, NULL, NULL, NULL, 8, 0, 0, '启用', 'admin', NOW()),
(9, 43, 'CITY', '城市', 'string', 40, NULL, NULL, NULL, NULL, 9, 0, 0, '启用', 'admin', NOW()),
(9, 44, 'POSTAL_CODE', '邮政编码', 'string', 10, NULL, NULL, NULL, NULL, 10, 0, 0, '启用', 'admin', NOW()),
(9, 45, 'COUNTRY', '国家', 'string', 3, NULL, 61, 'COUNTRY', '国家代码', 11, 0, 0, '启用', 'admin', NOW()),
(9, 46, 'REGION', '地区/省份', 'string', 3, NULL, 62, 'PROVINCE', '省份', 12, 0, 0, '启用', 'admin', NOW()),
(9, 47, 'TELEPHONE', '电话', 'string', 30, NULL, NULL, NULL, NULL, 13, 0, 0, '启用', 'admin', NOW()),
(9, 49, 'EMAIL', '电子邮箱', 'string', 240, NULL, NULL, NULL, NULL, 14, 0, 0, '启用', 'admin', NOW()),
(9, 50, 'WEBSITE', '网站', 'string', 120, NULL, NULL, NULL, NULL, 15, 0, 0, '启用', 'admin', NOW()),
(9, 51, 'TAX_NUMBER', '税号', 'string', 18, NULL, NULL, NULL, NULL, 16, 0, 0, '启用', 'admin', NOW()),
(9, 52, 'LEGAL_REP', '法人代表', 'string', 30, NULL, NULL, NULL, NULL, 17, 0, 0, '启用', 'admin', NOW()),
(9, 53, 'REGISTERED_CAPITAL', '注册资本', 'decimal', 15, 2, NULL, NULL, NULL, 18, 0, 0, '启用', 'admin', NOW()),
(9, 55, 'EMPLOYEES', '员工人数', 'integer', NULL, NULL, NULL, NULL, NULL, 19, 0, 0, '启用', 'admin', NOW()),
(9, 56, 'ESTABLISHED_DATE', '成立日期', 'date', NULL, NULL, NULL, NULL, NULL, 20, 0, 0, '启用', 'admin', NOW());

-- 5.4 供应商采购组织子表字段 (entity_id = 10)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(10, 70, 'PURCHASE_ORG', '采购组织', 'string', 4, NULL, 72, 'PURCHASE_ORG', '采购组织', 1, 1, 1, '启用', 'admin', NOW()),
(10, 71, 'PURCHASE_GRP', '采购组', 'string', 3, NULL, NULL, NULL, NULL, 2, 1, 0, '启用', 'admin', NOW()),
(10, 73, 'ORDER_CURRENCY', '订单货币', 'string', 3, NULL, 74, 'CURRENCY', '货币', 3, 0, 0, '启用', 'admin', NOW()),
(10, 74, 'GR_BASEDIV', '收货后结算', 'string', 1, NULL, 63, 'YES_NO', '是/否', 4, 0, 0, '启用', 'admin', NOW()),
(10, 75, 'AUTO_PO', '自动生成PO', 'string', 1, NULL, 63, 'YES_NO', '是/否', 5, 0, 0, '启用', 'admin', NOW()),
(10, 78, 'PAYMENT_TERM', '付款条款', 'string', 4, NULL, 73, 'PAYMENT_TERM', '付款条款', 6, 0, 0, '启用', 'admin', NOW()),
(10, 65, 'INCOTERM1', '国际贸易术语', 'string', 3, NULL, 78, 'INCOTERM', '国际贸易术语', 7, 0, 0, '启用', 'admin', NOW()),
(10, 87, 'SHIP_MODE', '运输方式', 'string', 2, NULL, 77, 'SHIP_MODE', '运输方式', 8, 0, 0, '启用', 'admin', NOW());

-- 5.5 供应商银行信息子表字段 (entity_id = 11)
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, sort, is_required, is_query, status, created_by, created_at) VALUES
(11, 82, 'BANK_COUNTRY', '银行国家', 'string', 3, NULL, 61, 'COUNTRY', '国家代码', 1, 1, 0, '启用', 'admin', NOW()),
(11, 83, 'BANK_KEY', '银行代码', 'string', 15, NULL, NULL, NULL, NULL, 2, 1, 0, '启用', 'admin', NOW()),
(11, 84, 'BANK_ACCOUNT', '银行账号', 'string', 18, NULL, NULL, NULL, NULL, 3, 1, 0, '启用', 'admin', NOW()),
(11, 85, 'BANK_NAME', '银行名称', 'string', 60, NULL, NULL, NULL, NULL, 4, 0, 0, '启用', 'admin', NOW()),
(11, 86, 'ACCOUNT_HOLDER', '账户持有人', 'string', 60, NULL, NULL, NULL, NULL, 5, 0, 0, '启用', 'admin', NOW());

SELECT '初始化完成！' AS message;
