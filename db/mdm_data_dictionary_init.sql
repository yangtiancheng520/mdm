-- =============================================
-- MDM 数据字典初始化脚本
-- 包含：字段分类、值域、值域项、数据字典（字段标准）
-- 参考：SAP 物料、客户、供应商常用字段
-- =============================================

SET NAMES utf8mb4;
USE mdm;

-- =============================================
-- 1. 初始化字段分类
-- =============================================

INSERT INTO std_field_category (category_code, category_name, parent_id, sort, status, created_at, updated_at) VALUES
-- 一级分类
('BASIC', '基本信息', NULL, 1, '启用', NOW(), NOW()),
('SALES', '销售信息', NULL, 2, '启用', NOW(), NOW()),
('PURCHASE', '采购信息', NULL, 3, '启用', NOW(), NOW()),
('FINANCE', '财务信息', NULL, 4, '启用', NOW(), NOW()),
('LOGISTICS', '物流信息', NULL, 5, '启用', NOW(), NOW()),
('MATERIAL', '物料属性', NULL, 6, '启用', NOW(), NOW()),
('PARTNER', '合作伙伴', NULL, 7, '启用', NOW(), NOW());

-- =============================================
-- 2. 初始化值域
-- =============================================

INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at) VALUES
('GENDER', '性别', 'string', 1, '启用', NOW(), NOW()),
('COUNTRY', '国家代码', 'string', 2, '启用', NOW(), NOW()),
('PROVINCE', '省份', 'string', 2, '启用', NOW(), NOW()),
('YES_NO', '是/否', 'string', 1, '启用', NOW(), NOW()),
('STATUS', '启用状态', 'string', 1, '启用', NOW(), NOW()),
('MATERIAL_TYPE', '物料类型', 'string', 4, '启用', NOW(), NOW()),
('MATERIAL_GROUP', '物料组', 'string', 6, '启用', NOW(), NOW()),
('BASE_UNIT', '基本单位', 'string', 3, '启用', NOW(), NOW()),
('CUSTOMER_GROUP', '客户组', 'string', 2, '启用', NOW(), NOW()),
('VENDOR_GROUP', '供应商组', 'string', 2, '启用', NOW(), NOW()),
('COMPANY_CODE', '公司代码', 'string', 4, '启用', NOW(), NOW()),
('SALES_ORG', '销售组织', 'string', 4, '启用', NOW(), NOW()),
('PURCHASE_ORG', '采购组织', 'string', 4, '启用', NOW(), NOW()),
('PAYMENT_TERM', '付款条款', 'string', 4, '启用', NOW(), NOW()),
('CURRENCY', '货币', 'string', 3, '启用', NOW(), NOW()),
('INDUSTRY', '行业分类', 'string', 2, '启用', NOW(), NOW()),
('CREDIT_LEVEL', '信用等级', 'string', 1, '启用', NOW(), NOW()),
('SHIP_MODE', '运输方式', 'string', 2, '启用', NOW(), NOW()),
('INCOTERM', '国际贸易术语', 'string', 3, '启用', NOW(), NOW()),
('LANGUAGE', '语言', 'string', 2, '启用', NOW(), NOW());

-- =============================================
-- 3. 初始化值域项
-- =============================================

-- 3.1 性别
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='GENDER'), 'M', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='GENDER'), 'F', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='GENDER'), 'U', 3, '启用', NOW());

-- 3.2 国家代码
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='COUNTRY'), 'CN', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='COUNTRY'), 'US', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='COUNTRY'), 'JP', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='COUNTRY'), 'DE', 4, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='COUNTRY'), 'UK', 5, '启用', NOW());

-- 3.3 省份
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='PROVINCE'), '北京', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PROVINCE'), '上海', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PROVINCE'), '广东', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PROVINCE'), '浙江', 4, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PROVINCE'), '江苏', 5, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PROVINCE'), '四川', 6, '启用', NOW());

-- 3.4 是/否
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='YES_NO'), '是', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='YES_NO'), '否', 2, '启用', NOW());

-- 3.5 启用状态
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='STATUS'), '启用', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='STATUS'), '停用', 2, '启用', NOW());

-- 3.6 物料类型
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_TYPE'), '成品', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_TYPE'), '半成品', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_TYPE'), '原材料', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_TYPE'), '包装材料', 4, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_TYPE'), '运营物资', 5, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_TYPE'), '服务', 6, '启用', NOW());

-- 3.7 物料组
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_GROUP'), '电子产品', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_GROUP'), '机械配件', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_GROUP'), '化工原料', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_GROUP'), '包装材料', 4, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='MATERIAL_GROUP'), '办公用品', 5, '启用', NOW());

-- 3.8 基本单位
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='BASE_UNIT'), '件', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='BASE_UNIT'), '个', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='BASE_UNIT'), '千克', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='BASE_UNIT'), '米', 4, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='BASE_UNIT'), '升', 5, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='BASE_UNIT'), '套', 6, '启用', NOW());

-- 3.9 客户组
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='CUSTOMER_GROUP'), '战略客户', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='CUSTOMER_GROUP'), '重点客户', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='CUSTOMER_GROUP'), '一般客户', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='CUSTOMER_GROUP'), '潜在客户', 4, '启用', NOW());

-- 3.10 供应商组
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='VENDOR_GROUP'), '战略供应商', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='VENDOR_GROUP'), '合格供应商', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='VENDOR_GROUP'), '临时供应商', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='VENDOR_GROUP'), '潜在供应商', 4, '启用', NOW());

-- 3.11 公司代码
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='COMPANY_CODE'), '总公司', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='COMPANY_CODE'), '华东分公司', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='COMPANY_CODE'), '华南分公司', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='COMPANY_CODE'), '华北分公司', 4, '启用', NOW());

-- 3.12 销售组织
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='SALES_ORG'), '华东销售部', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='SALES_ORG'), '华南销售部', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='SALES_ORG'), '华北销售部', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='SALES_ORG'), '西南销售部', 4, '启用', NOW());

-- 3.13 采购组织
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='PURCHASE_ORG'), '集中采购部', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PURCHASE_ORG'), '华东采购部', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PURCHASE_ORG'), '华南采购部', 3, '启用', NOW());

-- 3.14 付款条款
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='PAYMENT_TERM'), '货到付款', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PAYMENT_TERM'), '30天月结', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PAYMENT_TERM'), '60天月结', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PAYMENT_TERM'), '90天月结', 4, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='PAYMENT_TERM'), '预付款30%', 5, '启用', NOW());

-- 3.15 货币
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='CURRENCY'), '人民币', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='CURRENCY'), '美元', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='CURRENCY'), '欧元', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='CURRENCY'), '日元', 4, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='CURRENCY'), '英镑', 5, '启用', NOW());

-- 3.16 行业分类
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='INDUSTRY'), '制造业', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='INDUSTRY'), '零售业', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='INDUSTRY'), '批发业', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='INDUSTRY'), '服务业', 4, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='INDUSTRY'), '金融业', 5, '启用', NOW());

-- 3.17 信用等级
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='CREDIT_LEVEL'), 'A级-优秀', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='CREDIT_LEVEL'), 'B级-良好', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='CREDIT_LEVEL'), 'C级-一般', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='CREDIT_LEVEL'), 'D级-较差', 4, '启用', NOW());

-- 3.18 运输方式
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='SHIP_MODE'), '公路运输', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='SHIP_MODE'), '铁路运输', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='SHIP_MODE'), '航空运输', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='SHIP_MODE'), '海运', 4, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='SHIP_MODE'), '快递', 5, '启用', NOW());

-- 3.19 国际贸易术语
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='INCOTERM'), '工厂交货', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='INCOTERM'), '离岸价', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='INCOTERM'), '到岸价', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='INCOTERM'), '完税交货', 4, '启用', NOW());

-- 3.20 语言
INSERT INTO std_value_domain_item (domain_id, item_value, sort, status, created_at) VALUES
((SELECT id FROM std_value_domain WHERE domain_code='LANGUAGE'), '中文', 1, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='LANGUAGE'), '英语', 2, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='LANGUAGE'), '日语', 3, '启用', NOW()),
((SELECT id FROM std_value_domain WHERE domain_code='LANGUAGE'), '德语', 4, '启用', NOW());

-- =============================================
-- 4. 初始化字段标准（数据字典）
-- =============================================

-- 获取分类ID
SET @cat_basic = (SELECT id FROM std_field_category WHERE category_code = 'BASIC');
SET @cat_sales = (SELECT id FROM std_field_category WHERE category_code = 'SALES');
SET @cat_purchase = (SELECT id FROM std_field_category WHERE category_code = 'PURCHASE');
SET @cat_finance = (SELECT id FROM std_field_category WHERE category_code = 'FINANCE');
SET @cat_logistics = (SELECT id FROM std_field_category WHERE category_code = 'LOGISTICS');
SET @cat_material = (SELECT id FROM std_field_category WHERE category_code = 'MATERIAL');
SET @cat_partner = (SELECT id FROM std_field_category WHERE category_code = 'PARTNER');

-- 获取值域ID
SET @domain_gender = (SELECT id FROM std_value_domain WHERE domain_code = 'GENDER');
SET @domain_country = (SELECT id FROM std_value_domain WHERE domain_code = 'COUNTRY');
SET @domain_province = (SELECT id FROM std_value_domain WHERE domain_code = 'PROVINCE');
SET @domain_yes_no = (SELECT id FROM std_value_domain WHERE domain_code = 'YES_NO');
SET @domain_status = (SELECT id FROM std_value_domain WHERE domain_code = 'STATUS');
SET @domain_material_type = (SELECT id FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE');
SET @domain_material_group = (SELECT id FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP');
SET @domain_base_unit = (SELECT id FROM std_value_domain WHERE domain_code = 'BASE_UNIT');
SET @domain_customer_group = (SELECT id FROM std_value_domain WHERE domain_code = 'CUSTOMER_GROUP');
SET @domain_vendor_group = (SELECT id FROM std_value_domain WHERE domain_code = 'VENDOR_GROUP');
SET @domain_company = (SELECT id FROM std_value_domain WHERE domain_code = 'COMPANY_CODE');
SET @domain_sales_org = (SELECT id FROM std_value_domain WHERE domain_code = 'SALES_ORG');
SET @domain_purchase_org = (SELECT id FROM std_value_domain WHERE domain_code = 'PURCHASE_ORG');
SET @domain_payment = (SELECT id FROM std_value_domain WHERE domain_code = 'PAYMENT_TERM');
SET @domain_currency = (SELECT id FROM std_value_domain WHERE domain_code = 'CURRENCY');
SET @domain_industry = (SELECT id FROM std_value_domain WHERE domain_code = 'INDUSTRY');
SET @domain_credit = (SELECT id FROM std_value_domain WHERE domain_code = 'CREDIT_LEVEL');
SET @domain_ship = (SELECT id FROM std_value_domain WHERE domain_code = 'SHIP_MODE');
SET @domain_incoterm = (SELECT id FROM std_value_domain WHERE domain_code = 'INCOTERM');
SET @domain_language = (SELECT id FROM std_value_domain WHERE domain_code = 'LANGUAGE');

-- ========== 基本信息 ==========
INSERT INTO std_field_standard (field_code, field_name, field_type, length, is_enum, domain_id, category_id, status, created_at, updated_at) VALUES
('CODE', '编码', 'string', 18, 0, NULL, @cat_basic, '启用', NOW(), NOW()),
('NAME', '名称', 'string', 80, 0, NULL, @cat_basic, '启用', NOW(), NOW()),
('NAME_EN', '英文名称', 'string', 80, 0, NULL, @cat_basic, '启用', NOW(), NOW()),
('SHORT_TEXT', '简称', 'string', 20, 0, NULL, @cat_basic, '启用', NOW(), NOW()),
('DESCRIPTION', '描述', 'text', NULL, 0, NULL, @cat_basic, '启用', NOW(), NOW()),
('STATUS', '状态', 'string', 1, 1, @domain_status, @cat_basic, '启用', NOW(), NOW()),
('CREATE_DATE', '创建日期', 'date', NULL, 0, NULL, @cat_basic, '启用', NOW(), NOW()),
('CHANGE_DATE', '修改日期', 'date', NULL, 0, NULL, @cat_basic, '启用', NOW(), NOW());

-- ========== 物料属性 ==========
INSERT INTO std_field_standard (field_code, field_name, field_type, length, is_enum, domain_id, category_id, status, created_at, updated_at) VALUES
('MATNR', '物料编码', 'string', 18, 0, NULL, @cat_material, '启用', NOW(), NOW()),
('MAKTX', '物料描述', 'string', 40, 0, NULL, @cat_material, '启用', NOW(), NOW()),
('MTART', '物料类型', 'string', 4, 1, @domain_material_type, @cat_material, '启用', NOW(), NOW()),
('MATKL', '物料组', 'string', 9, 1, @domain_material_group, @cat_material, '启用', NOW(), NOW()),
('MEINS', '基本单位', 'string', 3, 1, @domain_base_unit, @cat_material, '启用', NOW(), NOW()),
('GEWEI', '重量单位', 'string', 3, 1, @domain_base_unit, @cat_material, '启用', NOW(), NOW()),
('BRGEW', '毛重', 'decimal', 13, 0, NULL, @cat_material, '启用', NOW(), NOW()),
('NTGEW', '净重', 'decimal', 13, 0, NULL, @cat_material, '启用', NOW(), NOW()),
('VOLUM', '体积', 'decimal', 13, 0, NULL, @cat_material, '启用', NOW(), NOW()),
('VOLEH', '体积单位', 'string', 3, 0, NULL, @cat_material, '启用', NOW(), NOW()),
('GROES', '尺寸', 'string', 32, 0, NULL, @cat_material, '启用', NOW(), NOW()),
('WERKS', '工厂', 'string', 4, 0, NULL, @cat_material, '启用', NOW(), NOW()),
('LGORT', '库存地点', 'string', 4, 0, NULL, @cat_material, '启用', NOW(), NOW()),
('BISMT', '旧物料号', 'string', 18, 0, NULL, @cat_material, '启用', NOW(), NOW());

-- ========== 合作伙伴（客户/供应商通用） ==========
INSERT INTO std_field_standard (field_code, field_name, field_type, length, is_enum, domain_id, category_id, status, created_at, updated_at) VALUES
('PARTNER_CODE', '合作伙伴编码', 'string', 10, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('PARTNER_NAME', '合作伙伴名称', 'string', 80, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('PARTNER_NAME2', '合作伙伴名称2', 'string', 80, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('STREET', '街道', 'string', 60, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('CITY', '城市', 'string', 40, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('POSTAL_CODE', '邮政编码', 'string', 10, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('COUNTRY', '国家', 'string', 3, 1, @domain_country, @cat_partner, '启用', NOW(), NOW()),
('REGION', '地区/省份', 'string', 3, 1, @domain_province, @cat_partner, '启用', NOW(), NOW()),
('TELEPHONE', '电话', 'string', 30, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('FAX', '传真', 'string', 30, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('EMAIL', '电子邮箱', 'string', 240, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('WEBSITE', '网站', 'string', 120, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('TAX_NUMBER', '税号', 'string', 18, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('LEGAL_REP', '法人代表', 'string', 30, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('REGISTERED_CAPITAL', '注册资本', 'decimal', 15, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('INDUSTRY', '行业分类', 'string', 4, 1, @domain_industry, @cat_partner, '启用', NOW(), NOW()),
('EMPLOYEES', '员工人数', 'integer', NULL, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('ESTABLISHED_DATE', '成立日期', 'date', NULL, 0, NULL, @cat_partner, '启用', NOW(), NOW()),
('LANGUAGE', '语言', 'string', 2, 1, @domain_language, @cat_partner, '启用', NOW(), NOW());

-- ========== 销售信息 ==========
INSERT INTO std_field_standard (field_code, field_name, field_type, length, is_enum, domain_id, category_id, status, created_at, updated_at) VALUES
('SALES_ORG', '销售组织', 'string', 4, 1, @domain_sales_org, @cat_sales, '启用', NOW(), NOW()),
('DISTR_CHANNEL', '分销渠道', 'string', 2, 0, NULL, @cat_sales, '启用', NOW(), NOW()),
('DIVISION', '产品组', 'string', 2, 0, NULL, @cat_sales, '启用', NOW(), NOW()),
('SALES_OFFICE', '销售办公室', 'string', 4, 0, NULL, @cat_sales, '启用', NOW(), NOW()),
('SALES_GROUP', '销售组', 'string', 3, 0, NULL, @cat_sales, '启用', NOW(), NOW()),
('CUSTOMER_GROUP', '客户组', 'string', 2, 1, @domain_customer_group, @cat_sales, '启用', NOW(), NOW()),
('PRICE_GROUP', '价格组', 'string', 2, 0, NULL, @cat_sales, '启用', NOW(), NOW()),
('INCOTERM1', '国际贸易术语', 'string', 3, 1, @domain_incoterm, @cat_sales, '启用', NOW(), NOW()),
('INCOTERM2', '国际贸易术语地点', 'string', 28, 0, NULL, @cat_sales, '启用', NOW(), NOW()),
('DELIVERY_PRIOR', '交货优先级', 'string', 2, 0, NULL, @cat_sales, '启用', NOW(), NOW()),
('SHIPPING_COND', '装运条件', 'string', 2, 0, NULL, @cat_sales, '启用', NOW(), NOW()),
('DELIVERY_PLANT', '交货工厂', 'string', 4, 0, NULL, @cat_sales, '启用', NOW(), NOW());

-- ========== 采购信息 ==========
INSERT INTO std_field_standard (field_code, field_name, field_type, length, is_enum, domain_id, category_id, status, created_at, updated_at) VALUES
('PURCHASE_ORG', '采购组织', 'string', 4, 1, @domain_purchase_org, @cat_purchase, '启用', NOW(), NOW()),
('PURCHASE_GRP', '采购组', 'string', 3, 0, NULL, @cat_purchase, '启用', NOW(), NOW()),
('VENDOR_GROUP', '供应商组', 'string', 4, 1, @domain_vendor_group, @cat_purchase, '启用', NOW(), NOW()),
('ORDER_CURRENCY', '订单货币', 'string', 3, 1, @domain_currency, @cat_purchase, '启用', NOW(), NOW()),
('GR_BASEDIV', '收货后结算标识', 'string', 1, 1, @domain_yes_no, @cat_purchase, '启用', NOW(), NOW()),
('AUTO_PO', '自动生成PO', 'string', 1, 1, @domain_yes_no, @cat_purchase, '启用', NOW(), NOW());

-- ========== 财务信息 ==========
INSERT INTO std_field_standard (field_code, field_name, field_type, length, is_enum, domain_id, category_id, status, created_at, updated_at) VALUES
('COMPANY_CODE', '公司代码', 'string', 4, 1, @domain_company, @cat_finance, '启用', NOW(), NOW()),
('RECON_ACCOUNT', '统驭科目', 'string', 10, 0, NULL, @cat_finance, '启用', NOW(), NOW()),
('PAYMENT_TERM', '付款条款', 'string', 4, 1, @domain_payment, @cat_finance, '启用', NOW(), NOW()),
('CREDIT_LIMIT', '信用额度', 'decimal', 15, 0, NULL, @cat_finance, '启用', NOW(), NOW()),
('CREDIT_LEVEL', '信用等级', 'string', 1, 1, @domain_credit, @cat_finance, '启用', NOW(), NOW()),
('TAX_TYPE', '税分类', 'string', 4, 0, NULL, @cat_finance, '启用', NOW(), NOW()),
('BANK_COUNTRY', '银行国家', 'string', 3, 1, @domain_country, @cat_finance, '启用', NOW(), NOW()),
('BANK_KEY', '银行代码', 'string', 15, 0, NULL, @cat_finance, '启用', NOW(), NOW()),
('BANK_ACCOUNT', '银行账号', 'string', 18, 0, NULL, @cat_finance, '启用', NOW(), NOW()),
('BANK_NAME', '银行名称', 'string', 60, 0, NULL, @cat_finance, '启用', NOW(), NOW()),
('ACCOUNT_HOLDER', '账户持有人', 'string', 60, 0, NULL, @cat_finance, '启用', NOW(), NOW());

-- ========== 物流信息 ==========
INSERT INTO std_field_standard (field_code, field_name, field_type, length, is_enum, domain_id, category_id, status, created_at, updated_at) VALUES
('SHIP_MODE', '运输方式', 'string', 2, 1, @domain_ship, @cat_logistics, '启用', NOW(), NOW()),
('LOADING_GRP', '装载组', 'string', 4, 0, NULL, @cat_logistics, '启用', NOW(), NOW()),
('TRANS_GRP', '运输组', 'string', 4, 0, NULL, @cat_logistics, '启用', NOW(), NOW()),
('PACKING_GRP', '包装组', 'string', 4, 0, NULL, @cat_logistics, '启用', NOW(), NOW()),
('MIN_ORDER_QTY', '最小订单量', 'decimal', 13, 0, NULL, @cat_logistics, '启用', NOW(), NOW()),
('MIN_DELIVERY_QTY', '最小交货量', 'decimal', 13, 0, NULL, @cat_logistics, '启用', NOW(), NOW()),
('MAX_DELIVERY_QTY', '最大交货量', 'decimal', 13, 0, NULL, @cat_logistics, '启用', NOW(), NOW());

-- =============================================
-- 完成
-- =============================================
SELECT '数据字典初始化完成!' AS message;
SELECT CONCAT('分类数量: ', (SELECT COUNT(*) FROM std_field_category)) AS category_count;
SELECT CONCAT('值域数量: ', (SELECT COUNT(*) FROM std_value_domain)) AS domain_count;
SELECT CONCAT('值域项数量: ', (SELECT COUNT(*) FROM std_value_domain_item)) AS domain_item_count;
SELECT CONCAT('字段数量: ', (SELECT COUNT(*) FROM std_field_standard)) AS field_count;
