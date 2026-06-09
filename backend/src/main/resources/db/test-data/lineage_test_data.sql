-- ========================================
-- 血缘分析功能测试数据准备脚本
-- ========================================

-- 1. 检查字段映射配置表是否有数据
SELECT COUNT(*) as field_mapping_count FROM dis_field_mapping;

-- 2. 如果没有字段映射配置，插入测试数据
-- 清空现有配置（可选）
-- TRUNCATE TABLE dis_field_mapping;

-- 插入供应商字段映射配置
INSERT INTO dis_field_mapping (data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, transform_type, transform_rule, is_required, is_key, sort_order, status, created_at, updated_at) VALUES
('VENDOR', '供应商', 'PARTNER_CODE', '供应商编码', 'LIFNR', '供应商编号', 'DIRECT', NULL, 1, 1, 1, 'active', NOW(), NOW()),
('VENDOR', '供应商', 'PARTNER_NAME', '供应商名称', 'NAME1', '供应商名称', 'DIRECT', NULL, 1, 0, 2, 'active', NOW(), NOW()),
('VENDOR', '供应商', 'COUNTRY', '国家', 'LAND1', '国家代码', 'DIRECT', NULL, 0, 0, 3, 'active', NOW(), NOW()),
('VENDOR', '供应商', 'CITY', '城市', 'ORT01', '城市', 'DIRECT', NULL, 0, 0, 4, 'active', NOW(), NOW()),
('VENDOR', '供应商', 'VENDOR_TYPE', '供应商类型', 'KTOKK', '供应商类型', 'VALUE_MAP', '{"A":"10","B":"20","C":"30"}', 1, 0, 5, 'active', NOW(), NOW()),
('VENDOR', '供应商', 'BANK_NAME', '开户银行', 'BANKL', '银行代码', 'VALUE_MAP', '{"工商银行":"ICBC","建设银行":"CCB","农业银行":"ABC"}', 0, 0, 6, 'active', NOW(), NOW()),
('VENDOR', '供应商', 'BANK_ACCOUNT', '银行账号', 'BANKN', '银行账户', 'DIRECT', NULL, 0, 0, 7, 'active', NOW(), NOW()),
('VENDOR', '供应商', 'ADDRESS', '地址', 'STRAS', '详细地址', 'DIRECT', NULL, 0, 0, 8, 'active', NOW(), NOW()),
('VENDOR', '供应商', 'CONTACT_PERSON', '联系人', 'ANRED', '联系人姓名', 'DIRECT', NULL, 0, 0, 9, 'active', NOW(), NOW()),
('VENDOR', '供应商', 'PHONE', '联系电话', 'TELF1', '电话号码', 'DIRECT', NULL, 0, 0, 10, 'active', NOW(), NOW());

-- 插入物料字段映射配置
INSERT INTO dis_field_mapping (data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, transform_type, transform_rule, is_required, is_key, sort_order, status, created_at, updated_at) VALUES
('MATERIAL', '物料', 'MATNR', '物料编码', 'MATNR', '物料编号', 'DIRECT', NULL, 1, 1, 1, 'active', NOW(), NOW()),
('MATERIAL', '物料', 'MAKTX', '物料名称', 'MAKTX', '物料描述', 'DIRECT', NULL, 1, 0, 2, 'active', NOW(), NOW()),
('MATERIAL', '物料', 'MATKL', '物料组', 'MATKL', '物料组', 'VALUE_MAP', '{"01":"001","02":"002","03":"003"}', 0, 0, 3, 'active', NOW(), NOW()),
('MATERIAL', '物料', 'MEINS', '基本计量单位', 'MEINS', '基本计量单位', 'DIRECT', NULL, 0, 0, 4, 'active', NOW(), NOW()),
('MATERIAL', '物料', 'MTART', '物料类型', 'MTART', '物料类型', 'DIRECT', NULL, 0, 0, 5, 'active', NOW(), NOW());

-- 插入客户字段映射配置
INSERT INTO dis_field_mapping (data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, transform_type, transform_rule, is_required, is_key, sort_order, status, created_at, updated_at) VALUES
('CUSTOMER', '客户', 'PARTNER_CODE', '客户编码', 'KUNNR', '客户编号', 'DIRECT', NULL, 1, 1, 1, 'active', NOW(), NOW()),
('CUSTOMER', '客户', 'PARTNER_NAME', '客户名称', 'NAME1', '客户名称', 'DIRECT', NULL, 1, 0, 2, 'active', NOW(), NOW()),
('CUSTOMER', '客户', 'COUNTRY', '国家', 'LAND1', '国家代码', 'DIRECT', NULL, 0, 0, 3, 'active', NOW(), NOW()),
('CUSTOMER', '客户', 'CITY', '城市', 'ORT01', '城市', 'DIRECT', NULL, 0, 0, 4, 'active', NOW(), NOW()),
('CUSTOMER', '客户', 'CUSTOMER_TYPE', '客户类型', 'KTOKD', '客户分类', 'VALUE_MAP', '{"VIP":"01","NORMAL":"02","KEY":"03"}', 0, 0, 5, 'active', NOW(), NOW());

-- 3. 检查分发日志表
SELECT COUNT(*) as log_count FROM dis_log_distribution;

-- 4. 插入测试分发日志（模拟成功的分发）
INSERT INTO dis_log_distribution (log_code, data_type, data_type_name, data_id, data_code, data_name, system_config_id, system_config_name, system_type, interface_name, operation, status, start_time, end_time, duration_ms, sap_key, sap_return_code, sap_message_type, sap_message, request_data, mapped_data, response_data, field_count, success_field_count, created_at, created_by) VALUES
('DIS20240615001', 'VENDOR', '供应商', 1, 'V001', '测试供应商001', 1, 'SAP生产环境', 'SAP', 'BAPI_VENDOR_CREATE', 'CREATE', 'SUCCESS', '2024-06-15 10:00:00', '2024-06-15 10:00:02', 2000, '0000100001', 'S', 'S', '供应商创建成功', '{"PARTNER_CODE":"V001","PARTNER_NAME":"测试供应商001","COUNTRY":"CN","CITY":"北京","VENDOR_TYPE":"A","BANK_NAME":"工商银行","BANK_ACCOUNT":"123456789","ADDRESS":"北京市朝阳区","CONTACT_PERSON":"张三","PHONE":"13800138000"}', '{"LIFNR":"0000100001","NAME1":"测试供应商001","LAND1":"CN","ORT01":"北京","KTOKK":"10","BANKL":"ICBC","BANKN":"123456789","STRAS":"北京市朝阳区","ANRED":"张三","TELF1":"13800138000"}', '{"RETURN":{"TYPE":"S","MESSAGE":"供应商创建成功"},"VENDORNO":"0000100001"}', 10, 10, NOW(), 'admin'),
('DIS20240615002', 'VENDOR', '供应商', 2, 'V002', '测试供应商002', 1, 'SAP生产环境', 'SAP', 'BAPI_VENDOR_CREATE', 'CREATE', 'SUCCESS', '2024-06-15 10:05:00', '2024-06-15 10:05:03', 3000, '0000100002', 'S', 'S', '供应商创建成功', '{"PARTNER_CODE":"V002","PARTNER_NAME":"测试供应商002","COUNTRY":"US","CITY":"上海","VENDOR_TYPE":"B","BANK_NAME":"建设银行","BANK_ACCOUNT":"987654321","ADDRESS":"上海市浦东新区","CONTACT_PERSON":"李四","PHONE":"13900139000"}', '{"LIFNR":"0000100002","NAME1":"测试供应商002","LAND1":"US","ORT01":"上海","KTOKK":"20","BANKL":"CCB","BANKN":"987654321","STRAS":"上海市浦东新区","ANRED":"李四","TELF1":"13900139000"}', '{"RETURN":{"TYPE":"S","MESSAGE":"供应商创建成功"},"VENDORNO":"0000100002"}', 10, 10, NOW(), 'admin'),
('DIS20240615003', 'MATERIAL', '物料', 1, 'M001', '测试物料001', 1, 'SAP生产环境', 'SAP', 'BAPI_MATERIAL_CREATE', 'CREATE', 'FAILED', '2024-06-15 10:10:00', '2024-06-15 10:10:01', 1000, NULL, 'E', 'E', '物料组不存在', '{"MATNR":"M001","MAKTX":"测试物料001","MATKL":"05","MEINS":"EA","MTART":"FERT"}', '{"MATNR":"M001","MAKTX":"测试物料001","MATKL":"05","MEINS":"EA","MTART":"FERT"}', '{"RETURN":{"TYPE":"E","MESSAGE":"物料组不存在"}}', 5, 3, NOW(), 'admin');

-- 5. 插入字段级血缘数据（第一条日志）
INSERT INTO dis_field_lineage (log_id, data_type, data_id, mdm_field, mdm_field_name, sap_field, sap_field_name, source_value, target_value, transform_type, transform_rule, status, created_at) VALUES
(1, 'VENDOR', 1, 'PARTNER_CODE', '供应商编码', 'LIFNR', '供应商编号', 'V001', '0000100001', 'DIRECT', NULL, 'SUCCESS', NOW()),
(1, 'VENDOR', 1, 'PARTNER_NAME', '供应商名称', 'NAME1', '供应商名称', '测试供应商001', '测试供应商001', 'DIRECT', NULL, 'SUCCESS', NOW()),
(1, 'VENDOR', 1, 'COUNTRY', '国家', 'LAND1', '国家代码', 'CN', 'CN', 'DIRECT', NULL, 'SUCCESS', NOW()),
(1, 'VENDOR', 1, 'CITY', '城市', 'ORT01', '城市', '北京', '北京', 'DIRECT', NULL, 'SUCCESS', NOW()),
(1, 'VENDOR', 1, 'VENDOR_TYPE', '供应商类型', 'KTOKK', '供应商类型', 'A', '10', 'VALUE_MAP', '{"A":"10","B":"20","C":"30"}', 'SUCCESS', NOW()),
(1, 'VENDOR', 1, 'BANK_NAME', '开户银行', 'BANKL', '银行代码', '工商银行', 'ICBC', 'VALUE_MAP', '{"工商银行":"ICBC","建设银行":"CCB","农业银行":"ABC"}', 'SUCCESS', NOW()),
(1, 'VENDOR', 1, 'BANK_ACCOUNT', '银行账号', 'BANKN', '银行账户', '123456789', '123456789', 'DIRECT', NULL, 'SUCCESS', NOW()),
(1, 'VENDOR', 1, 'ADDRESS', '地址', 'STRAS', '详细地址', '北京市朝阳区', '北京市朝阳区', 'DIRECT', NULL, 'SUCCESS', NOW()),
(1, 'VENDOR', 1, 'CONTACT_PERSON', '联系人', 'ANRED', '联系人姓名', '张三', '张三', 'DIRECT', NULL, 'SUCCESS', NOW()),
(1, 'VENDOR', 1, 'PHONE', '联系电话', 'TELF1', '电话号码', '13800138000', '13800138000', 'DIRECT', NULL, 'SUCCESS', NOW());

-- 插入字段级血缘数据（第二条日志）
INSERT INTO dis_field_lineage (log_id, data_type, data_id, mdm_field, mdm_field_name, sap_field, sap_field_name, source_value, target_value, transform_type, transform_rule, status, created_at) VALUES
(2, 'VENDOR', 2, 'PARTNER_CODE', '供应商编码', 'LIFNR', '供应商编号', 'V002', '0000100002', 'DIRECT', NULL, 'SUCCESS', NOW()),
(2, 'VENDOR', 2, 'PARTNER_NAME', '供应商名称', 'NAME1', '供应商名称', '测试供应商002', '测试供应商002', 'DIRECT', NULL, 'SUCCESS', NOW()),
(2, 'VENDOR', 2, 'COUNTRY', '国家', 'LAND1', '国家代码', 'US', 'US', 'DIRECT', NULL, 'SUCCESS', NOW()),
(2, 'VENDOR', 2, 'CITY', '城市', 'ORT01', '城市', '上海', '上海', 'DIRECT', NULL, 'SUCCESS', NOW()),
(2, 'VENDOR', 2, 'VENDOR_TYPE', '供应商类型', 'KTOKK', '供应商类型', 'B', '20', 'VALUE_MAP', '{"A":"10","B":"20","C":"30"}', 'SUCCESS', NOW()),
(2, 'VENDOR', 2, 'BANK_NAME', '开户银行', 'BANKL', '银行代码', '建设银行', 'CCB', 'VALUE_MAP', '{"工商银行":"ICBC","建设银行":"CCB","农业银行":"ABC"}', 'SUCCESS', NOW()),
(2, 'VENDOR', 2, 'BANK_ACCOUNT', '银行账号', 'BANKN', '银行账户', '987654321', '987654321', 'DIRECT', NULL, 'SUCCESS', NOW()),
(2, 'VENDOR', 2, 'ADDRESS', '地址', 'STRAS', '详细地址', '上海市浦东新区', '上海市浦东新区', 'DIRECT', NULL, 'SUCCESS', NOW()),
(2, 'VENDOR', 2, 'CONTACT_PERSON', '联系人', 'ANRED', '联系人姓名', '李四', '李四', 'DIRECT', NULL, 'SUCCESS', NOW()),
(2, 'VENDOR', 2, 'PHONE', '联系电话', 'TELF1', '电话号码', '13900139000', '13900139000', 'DIRECT', NULL, 'SUCCESS', NOW());

-- 插入字段级血缘数据（第三条日志 - 失败案例）
INSERT INTO dis_field_lineage (log_id, data_type, data_id, mdm_field, mdm_field_name, sap_field, sap_field_name, source_value, target_value, transform_type, transform_rule, status, created_at) VALUES
(3, 'MATERIAL', 1, 'MATNR', '物料编码', 'MATNR', '物料编号', 'M001', 'M001', 'DIRECT', NULL, 'SUCCESS', NOW()),
(3, 'MATERIAL', 1, 'MAKTX', '物料名称', 'MAKTX', '物料描述', '测试物料001', '测试物料001', 'DIRECT', NULL, 'SUCCESS', NOW()),
(3, 'MATERIAL', 1, 'MATKL', '物料组', 'MATKL', '物料组', '05', '05', 'VALUE_MAP', '{"01":"001","02":"002","03":"003"}', 'FAILED', NOW()),
(3, 'MATERIAL', 1, 'MEINS', '基本计量单位', 'MEINS', '基本计量单位', 'EA', 'EA', 'DIRECT', NULL, 'SUCCESS', NOW()),
(3, 'MATERIAL', 1, 'MTART', '物料类型', 'MTART', '物料类型', 'FERT', 'FERT', 'DIRECT', NULL, 'SUCCESS', NOW());

-- 6. 验证数据
SELECT '=== 字段映射配置 ===' as info;
SELECT data_type, COUNT(*) as count FROM dis_field_mapping GROUP BY data_type;

SELECT '=== 分发日志 ===' as info;
SELECT id, log_code, data_type, data_code, data_name, status FROM dis_log_distribution;

SELECT '=== 字段级血缘 ===' as info;
SELECT log_id, COUNT(*) as field_count FROM dis_field_lineage GROUP BY log_id;

SELECT '=== 完成 ===' as info;
