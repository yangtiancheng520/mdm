-- =============================================
-- SAP分发配置示例数据
-- 包含供应商、客户、物料的字段映射配置
-- =============================================

USE mdm;

-- ==========================================
-- 1. 目标系统配置
-- ==========================================

-- 清理旧配置
DELETE FROM dis_system_config WHERE config_code IN ('SAP_PROD', 'SAP_TEST', 'SAP_DEV');

-- 插入SAP系统配置
INSERT INTO dis_system_config (config_name, config_code, system_type, system_type_name, connection_config, pool_size, timeout, status, is_default, created_by) VALUES
('SAP生产环境', 'SAP_PROD', 'SAP', 'SAP RFC/BAPI',
 '{"host":"10.0.0.10","systemNumber":"00","client":"800","user":"RFC_USER","password":"","language":"ZH"}',
 10, 30000, 'inactive', 0, 'system'),

('SAP测试环境', 'SAP_TEST', 'SAP', 'SAP RFC/BAPI',
 '{"host":"10.0.0.20","systemNumber":"00","client":"800","user":"RFC_USER","password":"","language":"ZH"}',
 10, 30000, 'inactive', 1, 'system');

-- ==========================================
-- 2. 字段映射配置 - 供应商 (VENDOR)
-- ==========================================

DELETE FROM dis_field_mapping WHERE data_type = 'VENDOR';

INSERT INTO dis_field_mapping (data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, sap_structure, field_type, is_required, is_key, transform_type, sort_order, status, created_by) VALUES
-- 基本信息
('VENDOR', '供应商', 'vendor_code', '供应商编码', 'VENDOR_NO', '供应商编号', 'GENERALDATA', 'STRING', 1, 1, 'DIRECT', 1, 'active', 'system'),
('VENDOR', '供应商', 'vendor_name', '供应商名称', 'NAME', '名称1', 'GENERALDATA', 'STRING', 1, 0, 'DIRECT', 2, 'active', 'system'),
('VENDOR', '供应商', 'vendor_name2', '供应商名称2', 'NAME_2', '名称2', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 3, 'active', 'system'),
('VENDOR', '供应商', 'country', '国家', 'COUNTRY', '国家代码', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 4, 'active', 'system'),
('VENDOR', '供应商', 'city', '城市', 'CITY', '城市', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 5, 'active', 'system'),
('VENDOR', '供应商', 'address', '地址', 'STREET', '街道', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 6, 'active', 'system'),
('VENDOR', '供应商', 'postal_code', '邮政编码', 'POSTL_COD1', '邮政编码', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 7, 'active', 'system'),
('VENDOR', '供应商', 'region', '地区', 'REGION', '地区', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 8, 'active', 'system'),
('VENDOR', '供应商', 'language', '语言', 'LANGU', '语言', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 9, 'active', 'system'),
('VENDOR', '供应商', 'telephone', '电话', 'TELEPHONE', '电话', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 10, 'active', 'system'),
('VENDOR', '供应商', 'fax', '传真', 'TELEFAX', '传真', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 11, 'active', 'system'),
('VENDOR', '供应商', 'email', '电子邮箱', 'SMTP_ADDR', '电子邮箱', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 12, 'active', 'system'),
('VENDOR', '供应商', 'tax_no', '税号', 'STCD1', '税号', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 13, 'active', 'system'),
-- 公司代码数据
('VENDOR', '供应商', 'company_code', '公司代码', 'COMP_CODE', '公司代码', 'COMPANY_DATA', 'STRING', 1, 0, 'DIRECT', 20, 'active', 'system'),
('VENDOR', '供应商', 'recon_account', '统驭科目', 'RECON_ACCOUNT', '统驭科目', 'COMPANY_DATA', 'STRING', 0, 0, 'DIRECT', 21, 'active', 'system'),
-- 采购组织数据
('VENDOR', '供应商', 'purchasing_org', '采购组织', 'PURCHASING_ORG', '采购组织', 'PURCHASING_DATA', 'STRING', 0, 0, 'DIRECT', 30, 'active', 'system'),
('VENDOR', '供应商', 'pur_group', '采购组', 'PUR_GROUP', '采购组', 'PURCHASING_DATA', 'STRING', 0, 0, 'DIRECT', 31, 'active', 'system'),
('VENDOR', '供应商', 'currency', '货币', 'CURRENCY', '货币', 'PURCHASING_DATA', 'STRING', 0, 0, 'DIRECT', 32, 'active', 'system'),
('VENDOR', '供应商', 'payment_terms', '付款条件', 'PAYMENT_TERMS', '付款条件', 'PURCHASING_DATA', 'STRING', 0, 0, 'DIRECT', 33, 'active', 'system'),
-- 银行数据
('VENDOR', '供应商', 'bank_country', '银行国家', 'BANK_CTRY', '银行国家', 'BANK_DATA', 'STRING', 0, 0, 'DIRECT', 40, 'active', 'system'),
('VENDOR', '供应商', 'bank_key', '银行代码', 'BANK_KEY', '银行代码', 'BANK_DATA', 'STRING', 0, 0, 'DIRECT', 41, 'active', 'system'),
('VENDOR', '供应商', 'bank_account', '银行账号', 'BANK_ACCT', '银行账号', 'BANK_DATA', 'STRING', 0, 0, 'DIRECT', 42, 'active', 'system');

-- ==========================================
-- 3. 字段映射配置 - 客户 (CUSTOMER)
-- ==========================================

DELETE FROM dis_field_mapping WHERE data_type = 'CUSTOMER';

INSERT INTO dis_field_mapping (data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, sap_structure, field_type, is_required, is_key, transform_type, sort_order, status, created_by) VALUES
-- 基本信息
('CUSTOMER', '客户', 'customer_code', '客户编码', 'CUSTOMER_NO', '客户编号', 'GENERALDATA', 'STRING', 1, 1, 'DIRECT', 1, 'active', 'system'),
('CUSTOMER', '客户', 'customer_name', '客户名称', 'NAME', '名称1', 'GENERALDATA', 'STRING', 1, 0, 'DIRECT', 2, 'active', 'system'),
('CUSTOMER', '客户', 'customer_name2', '客户名称2', 'NAME_2', '名称2', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 3, 'active', 'system'),
('CUSTOMER', '客户', 'country', '国家', 'COUNTRY', '国家代码', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 4, 'active', 'system'),
('CUSTOMER', '客户', 'city', '城市', 'CITY', '城市', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 5, 'active', 'system'),
('CUSTOMER', '客户', 'address', '地址', 'STREET', '街道', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 6, 'active', 'system'),
('CUSTOMER', '客户', 'postal_code', '邮政编码', 'POSTL_COD1', '邮政编码', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 7, 'active', 'system'),
('CUSTOMER', '客户', 'region', '地区', 'REGION', '地区', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 8, 'active', 'system'),
('CUSTOMER', '客户', 'language', '语言', 'LANGU', '语言', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 9, 'active', 'system'),
('CUSTOMER', '客户', 'telephone', '电话', 'TELEPHONE', '电话', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 10, 'active', 'system'),
('CUSTOMER', '客户', 'fax', '传真', 'TELEFAX', '传真', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 11, 'active', 'system'),
('CUSTOMER', '客户', 'email', '电子邮箱', 'SMTP_ADDR', '电子邮箱', 'GENERALDATA', 'STRING', 0, 0, 'DIRECT', 12, 'active', 'system'),
-- 公司代码数据
('CUSTOMER', '客户', 'company_code', '公司代码', 'COMP_CODE', '公司代码', 'COMPANY_DATA', 'STRING', 1, 0, 'DIRECT', 20, 'active', 'system'),
('CUSTOMER', '客户', 'recon_account', '统驭科目', 'RECON_ACCOUNT', '统驭科目', 'COMPANY_DATA', 'STRING', 0, 0, 'DIRECT', 21, 'active', 'system'),
-- 销售区域数据
('CUSTOMER', '客户', 'sales_org', '销售组织', 'SALES_ORG', '销售组织', 'SALES_DATA', 'STRING', 0, 0, 'DIRECT', 30, 'active', 'system'),
('CUSTOMER', '客户', 'distr_chan', '分销渠道', 'DISTR_CHAN', '分销渠道', 'SALES_DATA', 'STRING', 0, 0, 'DIRECT', 31, 'active', 'system'),
('CUSTOMER', '客户', 'division', '产品组', 'DIVISION', '产品组', 'SALES_DATA', 'STRING', 0, 0, 'DIRECT', 32, 'active', 'system'),
('CUSTOMER', '客户', 'currency', '货币', 'CURRENCY', '货币', 'SALES_DATA', 'STRING', 0, 0, 'DIRECT', 33, 'active', 'system'),
('CUSTOMER', '客户', 'payment_terms', '付款条件', 'PAYMENT_TERMS', '付款条件', 'SALES_DATA', 'STRING', 0, 0, 'DIRECT', 34, 'active', 'system');

-- ==========================================
-- 4. 字段映射配置 - 物料 (MATERIAL)
-- ==========================================

DELETE FROM dis_field_mapping WHERE data_type = 'MATERIAL';

INSERT INTO dis_field_mapping (data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, sap_structure, field_type, is_required, is_key, transform_type, sort_order, status, created_by) VALUES
-- 客户端数据
('MATERIAL', '物料', 'material_code', '物料编码', 'MATNR', '物料编号', 'CLIENTDATA', 'STRING', 1, 1, 'DIRECT', 1, 'active', 'system'),
('MATERIAL', '物料', 'material_name', '物料名称', 'MATL_DESC', '物料描述', 'MATERIALDESCRIPTION', 'STRING', 1, 0, 'DIRECT', 2, 'active', 'system'),
('MATERIAL', '物料', 'industry_sector', '行业领域', 'MBRSH', '行业领域', 'CLIENTDATA', 'STRING', 1, 0, 'DIRECT', 3, 'active', 'system'),
('MATERIAL', '物料', 'material_type', '物料类型', 'MTART', '物料类型', 'CLIENTDATA', 'STRING', 1, 0, 'DIRECT', 4, 'active', 'system'),
('MATERIAL', '物料', 'base_uom', '基本单位', 'MEINS', '基本计量单位', 'CLIENTDATA', 'STRING', 1, 0, 'DIRECT', 5, 'active', 'system'),
('MATERIAL', '物料', 'mat_group', '物料组', 'MATKL', '物料组', 'CLIENTDATA', 'STRING', 0, 0, 'DIRECT', 6, 'active', 'system'),
('MATERIAL', '物料', 'gross_weight', '毛重', 'BRGEW', '毛重', 'CLIENTDATA', 'NUMBER', 0, 0, 'DIRECT', 7, 'active', 'system'),
('MATERIAL', '物料', 'net_weight', '净重', 'NTGEW', '净重', 'CLIENTDATA', 'NUMBER', 0, 0, 'DIRECT', 8, 'active', 'system'),
('MATERIAL', '物料', 'weight_unit', '重量单位', 'GEWEI', '重量单位', 'CLIENTDATA', 'STRING', 0, 0, 'DIRECT', 9, 'active', 'system'),
('MATERIAL', '物料', 'language', '语言', 'LANGU', '语言', 'MATERIALDESCRIPTION', 'STRING', 0, 0, 'DIRECT', 10, 'active', 'system'),
-- 工厂数据
('MATERIAL', '物料', 'plant', '工厂', 'PLANT', '工厂', 'PLANTDATA', 'STRING', 0, 0, 'DIRECT', 20, 'active', 'system'),
('MATERIAL', '物料', 'mrp_type', 'MRP类型', 'MRP_TYPE', 'MRP类型', 'PLANTDATA', 'STRING', 0, 0, 'DIRECT', 21, 'active', 'system'),
('MATERIAL', '物料', 'mrp_controller', 'MRP控制者', 'MRP_CTRLER', 'MRP控制者', 'PLANTDATA', 'STRING', 0, 0, 'DIRECT', 22, 'active', 'system'),
('MATERIAL', '物料', 'lot_size', '批量大小', 'LOT_SIZE', '批量大小', 'PLANTDATA', 'NUMBER', 0, 0, 'DIRECT', 23, 'active', 'system'),
-- 仓库数据
('MATERIAL', '物料', 'storage_loc', '库存地点', 'STGE_LOC', '库存地点', 'STORAGELOCATIONDATA', 'STRING', 0, 0, 'DIRECT', 30, 'active', 'system');

-- ==========================================
-- 5. 分发主题配置
-- ==========================================

DELETE FROM dis_topic WHERE topic_code IN ('VENDOR_TO_SAP', 'CUSTOMER_TO_SAP', 'MATERIAL_TO_SAP');

INSERT INTO dis_topic (topic_code, topic_name, data_type, data_type_name, distribute_mode, batch_size, retry_count, retry_interval, status, description, created_by) VALUES
('VENDOR_TO_SAP', '供应商分发到SAP', 'VENDOR', '供应商', 'MANUAL', 50, 3, 5000, 'inactive', '将供应商主数据分发到SAP系统创建/更新供应商档案', 'system'),
('CUSTOMER_TO_SAP', '客户分发到SAP', 'CUSTOMER', '客户', 'MANUAL', 50, 3, 5000, 'inactive', '将客户主数据分发到SAP系统创建/更新客户档案', 'system'),
('MATERIAL_TO_SAP', '物料分发到SAP', 'MATERIAL', '物料', 'MANUAL', 50, 3, 5000, 'inactive', '将物料主数据分发到SAP系统创建/更新物料档案', 'system');

SELECT 'SAP分发配置初始化完成' AS message;

-- 显示配置统计
SELECT
    '系统配置' AS 类型,
    COUNT(*) AS 数量
FROM dis_system_config
WHERE system_type = 'SAP'
UNION ALL
SELECT
    '供应商映射字段' AS 类型,
    COUNT(*) AS 数量
FROM dis_field_mapping
WHERE data_type = 'VENDOR'
UNION ALL
SELECT
    '客户映射字段' AS 类型,
    COUNT(*) AS 数量
FROM dis_field_mapping
WHERE data_type = 'CUSTOMER'
UNION ALL
SELECT
    '物料映射字段' AS 类型,
    COUNT(*) AS 数量
FROM dis_field_mapping
WHERE data_type = 'MATERIAL'
UNION ALL
SELECT
    '分发主题' AS 类型,
    COUNT(*) AS 数量
FROM dis_topic;
