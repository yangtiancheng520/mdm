-- =============================================
-- SAP主数据值域初始化脚本
-- 包括：客户、供应商、物料主数据相关值域
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- =============================================
-- 1. 通用值域
-- =============================================

-- 1.1 性别
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('GENDER', '性别', 'string', 1, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'M', '男', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'GENDER';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'F', '女', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'GENDER';

-- 1.2 国家代码
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('COUNTRY', '国家代码', 'string', 2, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'CN', '中国', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'COUNTRY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'US', '美国', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'COUNTRY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'JP', '日本', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'COUNTRY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'DE', '德国', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'COUNTRY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'UK', '英国', 5, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'COUNTRY';

-- 1.3 省份/地区
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('PROVINCE', '省份/地区', 'string', 2, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'BJ', '北京市', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PROVINCE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'SH', '上海市', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PROVINCE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'GD', '广东省', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PROVINCE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'ZJ', '浙江省', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PROVINCE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'JS', '江苏省', 5, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PROVINCE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'SC', '四川省', 6, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PROVINCE';

-- 1.4 语言
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('LANGUAGE', '语言', 'string', 2, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'ZH', '中文', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'LANGUAGE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'EN', '英语', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'LANGUAGE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'JA', '日语', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'LANGUAGE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'DE', '德语', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'LANGUAGE';

-- 1.5 货币
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('CURRENCY', '货币', 'string', 3, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'CNY', '人民币', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CURRENCY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'USD', '美元', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CURRENCY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'EUR', '欧元', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CURRENCY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'JPY', '日元', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CURRENCY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'GBP', '英镑', 5, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CURRENCY';

-- 1.6 是/否
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('YES_NO', '是/否', 'string', 1, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'Y', '是', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'YES_NO';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'N', '否', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'YES_NO';

-- 1.7 启用状态
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('STATUS', '启用状态', 'string', 8, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'ACTIVE', '启用', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'STATUS';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'INACTIVE', '停用', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'STATUS';

-- =============================================
-- 2. 客户主数据值域
-- =============================================

-- 2.1 客户组
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('CUSTOMER_GROUP', '客户组', 'string', 9, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'STRATEGIC', '战略客户', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CUSTOMER_GROUP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'KEY', '重点客户', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CUSTOMER_GROUP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'NORMAL', '一般客户', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CUSTOMER_GROUP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'POTENTIAL', '潜在客户', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CUSTOMER_GROUP';

-- 2.2 销售组织
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('SALES_ORG', '销售组织', 'string', 8, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'EC_SALES', '华东销售部', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'SALES_ORG';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'SC_SALES', '华南销售部', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'SALES_ORG';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'NC_SALES', '华北销售部', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'SALES_ORG';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'SW_SALES', '西南销售部', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'SALES_ORG';

-- 2.3 分销渠道
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('DISTR_CHANNEL', '分销渠道', 'string', 2, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'WH', '批发', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'DISTR_CHANNEL';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'RT', '零售', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'DISTR_CHANNEL';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'OL', '在线销售', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'DISTR_CHANNEL';

-- 2.4 付款条款
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('PAYMENT_TERM', '付款条款', 'string', 5, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'COD', '货到付款', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PAYMENT_TERM';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'N30', '30天月结', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PAYMENT_TERM';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'N60', '60天月结', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PAYMENT_TERM';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'N90', '90天月结', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PAYMENT_TERM';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'PRE30', '预付款30%', 5, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PAYMENT_TERM';

-- 2.5 客户类型
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('CUSTOMER_TYPE', '客户类型', 'string', 8, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'DOMESTIC', '国内客户', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CUSTOMER_TYPE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'OVERSEAS', '海外客户', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CUSTOMER_TYPE';

-- =============================================
-- 3. 供应商主数据值域
-- =============================================

-- 3.1 供应商组
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('VENDOR_GROUP', '供应商组', 'string', 9, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'STRATEGIC', '战略供应商', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'VENDOR_GROUP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'QUALIFIED', '合格供应商', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'VENDOR_GROUP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'TEMP', '临时供应商', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'VENDOR_GROUP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'POTENTIAL', '潜在供应商', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'VENDOR_GROUP';

-- 3.2 采购组织
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('PURCHASE_ORG', '采购组织', 'string', 7, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'CENTRAL', '集中采购部', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PURCHASE_ORG';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'EC_PUR', '华东采购部', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PURCHASE_ORG';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'SC_PUR', '华南采购部', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PURCHASE_ORG';

-- 3.3 采购组
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('PURCHASE_GRP', '采购组', 'string', 3, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'M01', '原材料采购组', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PURCHASE_GRP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'M02', '设备采购组', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PURCHASE_GRP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'M03', '服务采购组', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'PURCHASE_GRP';

-- 3.4 信用等级
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('CREDIT_LEVEL', '信用等级', 'string', 1, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'A', 'A级-优秀', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CREDIT_LEVEL';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'B', 'B级-良好', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CREDIT_LEVEL';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'C', 'C级-一般', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CREDIT_LEVEL';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'D', 'D级-较差', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'CREDIT_LEVEL';

-- =============================================
-- 4. 物料主数据值域
-- =============================================

-- 4.1 物料类型
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('MATERIAL_TYPE', '物料类型', 'string', 4, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'FERT', '成品', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'HALB', '半成品', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'ROH', '原材料', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'VERP', '包装材料', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'HIBE', '运营物资', 5, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'DIEN', '服务', 6, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE';

-- 4.2 物料组
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('MATERIAL_GROUP', '物料组', 'string', 4, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'ELEC', '电子产品', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'MECH', '机械配件', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'CHEM', '化工原料', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'PACK', '包装材料', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'OFFC', '办公用品', 5, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP';

-- 4.3 基本单位
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('BASE_UNIT', '基本单位', 'string', 3, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'PC', '件', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'BASE_UNIT';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'EA', '个', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'BASE_UNIT';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'KG', '千克', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'BASE_UNIT';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'M', '米', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'BASE_UNIT';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'L', '升', 5, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'BASE_UNIT';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'SET', '套', 6, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'BASE_UNIT';

-- 4.4 行业分类
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('INDUSTRY', '行业分类', 'string', 3, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'MFG', '制造业', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'INDUSTRY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'RTL', '零售业', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'INDUSTRY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'WHL', '批发业', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'INDUSTRY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'SVC', '服务业', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'INDUSTRY';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'FIN', '金融业', 5, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'INDUSTRY';

-- =============================================
-- 5. 其他业务值域
-- =============================================

-- 5.1 运输方式
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('SHIP_MODE', '运输方式', 'string', 5, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'TRUCK', '公路运输', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'SHIP_MODE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'RAIL', '铁路运输', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'SHIP_MODE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'AIR', '航空运输', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'SHIP_MODE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'SEA', '海运', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'SHIP_MODE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'EXPR', '快递', 5, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'SHIP_MODE';

-- 5.2 国际贸易术语
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('INCOTERM', '国际贸易术语', 'string', 3, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'EXW', '工厂交货', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'INCOTERM';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'FOB', '离岸价', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'INCOTERM';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'CIF', '到岸价', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'INCOTERM';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'DDP', '完税交货', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'INCOTERM';

-- 5.3 公司代码
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, status, created_at, updated_at)
VALUES ('COMPANY_CODE', '公司代码', 'string', 2, '启用', NOW(), NOW());

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'HQ', '总公司', 1, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'COMPANY_CODE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'EC', '华东分公司', 2, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'COMPANY_CODE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'SC', '华南分公司', 3, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'COMPANY_CODE';
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_at, updated_at)
SELECT id, 'NC', '华北分公司', 4, '启用', NOW(), NOW() FROM std_value_domain WHERE domain_code = 'COMPANY_CODE';

-- 查看初始化结果
SELECT '========== 值域初始化完成 ==========' AS '';
SELECT vd.domain_code, vd.domain_name, vd.data_length, COUNT(vdi.id) AS item_count
FROM std_value_domain vd
LEFT JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
GROUP BY vd.id
ORDER BY vd.domain_code;

SELECT CONCAT('共创建 ', COUNT(*), ' 个值域') AS result FROM std_value_domain;
SELECT CONCAT('共创建 ', COUNT(*), ' 个值域项') AS result FROM std_value_domain_item;
