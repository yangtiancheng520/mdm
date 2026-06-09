-- =============================================
-- 修复质量规则状态并添加规则
-- =============================================

SET NAMES utf8mb4;
USE mdm;

-- 1. 先更新现有规则状态为 active
UPDATE qlt_rule SET status = 'active' WHERE status = 'published';

-- 2. 检查是否有规则，如果没有则创建
-- 获取视图和实体ID
SET @material_view_id = (SELECT id FROM std_view WHERE view_code = 'MATERIAL');
SET @customer_view_id = (SELECT id FROM std_view WHERE view_code = 'CUSTOMER');
SET @vendor_view_id = (SELECT id FROM std_view WHERE view_code = 'VENDOR');

SET @material_entity_id = (SELECT id FROM std_view_entity WHERE view_id = @material_view_id AND entity_type = 'main');
SET @customer_entity_id = (SELECT id FROM std_view_entity WHERE view_id = @customer_view_id AND entity_type = 'main');
SET @vendor_entity_id = (SELECT id FROM std_view_entity WHERE view_id = @vendor_view_id AND entity_type = 'main');

-- 获取表名
SET @material_table = (SELECT table_name FROM std_view_entity WHERE view_id = @material_view_id AND entity_type = 'main');
SET @customer_table = (SELECT table_name FROM std_view_entity WHERE view_id = @customer_view_id AND entity_type = 'main');
SET @vendor_table = (SELECT table_name FROM std_view_entity WHERE view_id = @vendor_view_id AND entity_type = 'main');

-- 查看当前配置
SELECT '物料视图' as view_type, @material_view_id as view_id, @material_entity_id as entity_id, @material_table as table_name;
SELECT '客户视图' as view_type, @customer_view_id as view_id, @customer_entity_id as entity_id, @customer_table as table_name;
SELECT '供应商视图' as view_type, @vendor_view_id as view_id, @vendor_entity_id as entity_id, @vendor_table as table_name;

-- 查看现有规则
SELECT id, rule_code, rule_name, view_id, table_name, field_code, status FROM qlt_rule;

-- =============================================
-- 如果供应商视图没有规则，手动添加
-- =============================================

-- 检查供应商规则数量
SET @vendor_rule_count = (SELECT COUNT(*) FROM qlt_rule WHERE view_id = @vendor_view_id);

-- 只有当没有规则时才插入
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, severity, status, description, created_by, created_at)
SELECT 'RULE_VEND_001', '供应商编号必填', 'completeness', 'not_null', '{"checkType":"not_null"}', @vendor_view_id, @vendor_entity_id, 'main', @vendor_table, 'LIFNR', '供应商编号', 'error', 'active', '供应商编号不能为空', 'system', NOW()
FROM DUAL WHERE @vendor_rule_count = 0;

INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, severity, status, description, created_by, created_at)
SELECT 'RULE_VEND_002', '供应商名称必填', 'completeness', 'not_null', '{"checkType":"not_null"}', @vendor_view_id, @vendor_entity_id, 'main', @vendor_table, 'NAME1', '供应商名称', 'error', 'active', '供应商名称不能为空', 'system', NOW()
FROM DUAL WHERE @vendor_rule_count = 0;

INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, severity, status, description, created_by, created_at)
SELECT 'RULE_VEND_003', '供应商类型必填', 'completeness', 'not_null', '{"checkType":"not_null"}', @vendor_view_id, @vendor_entity_id, 'main', @vendor_table, 'VTYPE', '供应商类型', 'warning', 'active', '供应商类型不能为空', 'system', NOW()
FROM DUAL WHERE @vendor_rule_count = 0;

INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, severity, status, description, created_by, created_at)
SELECT 'RULE_VEND_004', '供应商邮箱格式检查', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$","allowNull":true}', @vendor_view_id, @vendor_entity_id, 'main', @vendor_table, 'SMTP_ADDR', '邮箱', 'warning', 'active', '检查邮箱格式是否正确', 'system', NOW()
FROM DUAL WHERE @vendor_rule_count = 0;

-- =============================================
-- 为物料视图添加规则（如果没有）
-- =============================================

SET @material_rule_count = (SELECT COUNT(*) FROM qlt_rule WHERE view_id = @material_view_id);

INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, severity, status, description, created_by, created_at)
SELECT 'RULE_MAT_001', '物料编号必填', 'completeness', 'not_null', '{"checkType":"not_null"}', @material_view_id, @material_entity_id, 'main', @material_table, 'MATNR', '物料编号', 'error', 'active', '物料编号不能为空', 'system', NOW()
FROM DUAL WHERE @material_rule_count = 0;

INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, severity, status, description, created_by, created_at)
SELECT 'RULE_MAT_002', '物料描述必填', 'completeness', 'not_null', '{"checkType":"not_null"}', @material_view_id, @material_entity_id, 'main', @material_table, 'MAKTX', '物料描述', 'error', 'active', '物料描述不能为空', 'system', NOW()
FROM DUAL WHERE @material_rule_count = 0;

-- =============================================
-- 为客户视图添加规则（如果没有）
-- =============================================

SET @customer_rule_count = (SELECT COUNT(*) FROM qlt_rule WHERE view_id = @customer_view_id);

INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, severity, status, description, created_by, created_at)
SELECT 'RULE_CUST_001', '客户编号必填', 'completeness', 'not_null', '{"checkType":"not_null"}', @customer_view_id, @customer_entity_id, 'main', @customer_table, 'KUNNR', '客户编号', 'error', 'active', '客户编号不能为空', 'system', NOW()
FROM DUAL WHERE @customer_rule_count = 0;

INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, severity, status, description, created_by, created_at)
SELECT 'RULE_CUST_002', '客户名称必填', 'completeness', 'not_null', '{"checkType":"not_null"}', @customer_view_id, @customer_entity_id, 'main', @customer_table, 'NAME1', '客户名称', 'error', 'active', '客户名称不能为空', 'system', NOW()
FROM DUAL WHERE @customer_rule_count = 0;

-- 查看最终结果
SELECT '===== 最终规则列表 =====' as info;
SELECT id, rule_code, rule_name, view_id, table_name, field_code, field_name, severity, status FROM qlt_rule;
