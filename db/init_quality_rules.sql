-- =============================================
-- MDM 质量规则初始化脚本
-- 基于客户、物料、供应商视图创建质量规则
-- =============================================

SET NAMES utf8mb4;
USE mdm;

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

-- 获取模板ID
SET @tpl_not_null = (SELECT id FROM qlt_rule_template WHERE template_code = 'TPL_NOT_NULL');
SET @tpl_email = (SELECT id FROM qlt_rule_template WHERE template_code = 'TPL_EMAIL');
SET @tpl_phone = (SELECT id FROM qlt_rule_template WHERE template_code = 'TPL_PHONE');
SET @tpl_domain = (SELECT id FROM qlt_rule_template WHERE template_code = 'TPL_DOMAIN');
SET @tpl_unique = (SELECT id FROM qlt_rule_template WHERE template_code = 'TPL_UNIQUE_GLOBAL');

-- =============================================
-- 物料视图质量规则
-- =============================================

-- 1. 物料编号唯一性检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_MAT_001', '物料编号唯一性检查', 'uniqueness', 'unique_global', '{"checkType":"unique_global","ignoreNull":true}', @material_view_id, @material_entity_id, 'main', @material_table, 'MATNR', '物料编号', @tpl_unique, 'error', 'published', '检查物料编号在全系统唯一', 'system');

-- 2. 物料描述非空检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_MAT_002', '物料描述非空检查', 'completeness', 'not_null', '{"checkType":"not_null","trimWhitespace":true}', @material_view_id, @material_entity_id, 'main', @material_table, 'MAKTX', '物料描述', @tpl_not_null, 'error', 'published', '检查物料描述不能为空', 'system');

-- 3. 物料类型值域检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_MAT_003', '物料类型值域检查', 'accuracy', 'domain', '{"checkType":"domain","domainCode":"MATL_TYPE","allowNull":true}', @material_view_id, @material_entity_id, 'main', @material_table, 'MTART', '物料类型', @tpl_domain, 'warning', 'published', '检查物料类型在有效值域范围内', 'system');

-- 4. 物料组值域检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_MAT_004', '物料组值域检查', 'accuracy', 'domain', '{"checkType":"domain","domainCode":"MATL_GROUP","allowNull":true}', @material_view_id, @material_entity_id, 'main', @material_table, 'MATKL', '物料组', @tpl_domain, 'warning', 'published', '检查物料组在有效值域范围内', 'system');

-- 5. 基本单位值域检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_MAT_005', '基本单位值域检查', 'accuracy', 'domain', '{"checkType":"domain","domainCode":"BASE_UOM","allowNull":true}', @material_view_id, @material_entity_id, 'main', @material_table, 'MEINS', '基本单位', @tpl_domain, 'warning', 'published', '检查基本单位在有效值域范围内', 'system');

-- 6. 行业领域值域检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_MAT_006', '行业领域值域检查', 'accuracy', 'domain', '{"checkType":"domain","domainCode":"IND_SECTOR","allowNull":true}', @material_view_id, @material_entity_id, 'main', @material_table, 'MBRSH', '行业领域', @tpl_domain, 'warning', 'published', '检查行业领域在有效值域范围内', 'system');

-- =============================================
-- 客户视图质量规则
-- =============================================

-- 7. 客户编号唯一性检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_CUST_001', '客户编号唯一性检查', 'uniqueness', 'unique_global', '{"checkType":"unique_global","ignoreNull":true}', @customer_view_id, @customer_entity_id, 'main', @customer_table, 'KUNNR', '客户编号', @tpl_unique, 'error', 'published', '检查客户编号在全系统唯一', 'system');

-- 8. 客户名称非空检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_CUST_002', '客户名称非空检查', 'completeness', 'not_null', '{"checkType":"not_null","trimWhitespace":true}', @customer_view_id, @customer_entity_id, 'main', @customer_table, 'NAME1', '客户名称', @tpl_not_null, 'error', 'published', '检查客户名称不能为空', 'system');

-- 9. 客户邮箱格式检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_CUST_003', '客户邮箱格式检查', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$","allowNull":true}', @customer_view_id, @customer_entity_id, 'main', @customer_table, 'SMTP_ADDR', '邮箱', @tpl_email, 'warning', 'published', '检查客户邮箱格式正确', 'system');

-- 10. 客户电话格式检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_CUST_004', '客户电话格式检查', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^1[3-9]\\\\d{9}$","allowNull":true}', @customer_view_id, @customer_entity_id, 'main', @customer_table, 'TELF1', '电话', @tpl_phone, 'warning', 'published', '检查客户电话格式正确（中国手机号）', 'system');

-- 11. 客户国家值域检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_CUST_005', '客户国家值域检查', 'accuracy', 'domain', '{"checkType":"domain","domainCode":"COUNTRY","allowNull":true}', @customer_view_id, @customer_entity_id, 'main', @customer_table, 'LAND1', '国家', @tpl_domain, 'warning', 'published', '检查国家代码在有效值域范围内', 'system');

-- 12. 客户地区值域检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_CUST_006', '客户地区值域检查', 'accuracy', 'domain', '{"checkType":"domain","domainCode":"REGION","allowNull":true}', @customer_view_id, @customer_entity_id, 'main', @customer_table, 'REGIO', '地区', @tpl_domain, 'warning', 'published', '检查地区代码在有效值域范围内', 'system');

-- =============================================
-- 供应商视图质量规则
-- =============================================

-- 13. 供应商编号唯一性检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_VEND_001', '供应商编号唯一性检查', 'uniqueness', 'unique_global', '{"checkType":"unique_global","ignoreNull":true}', @vendor_view_id, @vendor_entity_id, 'main', @vendor_table, 'LIFNR', '供应商编号', @tpl_unique, 'error', 'published', '检查供应商编号在全系统唯一', 'system');

-- 14. 供应商名称非空检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_VEND_002', '供应商名称非空检查', 'completeness', 'not_null', '{"checkType":"not_null","trimWhitespace":true}', @vendor_view_id, @vendor_entity_id, 'main', @vendor_table, 'NAME1', '供应商名称', @tpl_not_null, 'error', 'published', '检查供应商名称不能为空', 'system');

-- 15. 供应商邮箱格式检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_VEND_003', '供应商邮箱格式检查', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$","allowNull":true}', @vendor_view_id, @vendor_entity_id, 'main', @vendor_table, 'SMTP_ADDR', '邮箱', @tpl_email, 'warning', 'published', '检查供应商邮箱格式正确', 'system');

-- 16. 供应商电话格式检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_VEND_004', '供应商电话格式检查', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^1[3-9]\\\\d{9}$","allowNull":true}', @vendor_view_id, @vendor_entity_id, 'main', @vendor_table, 'TELF1', '电话', @tpl_phone, 'warning', 'published', '检查供应商电话格式正确（中国手机号）', 'system');

-- 17. 供应商国家值域检查
INSERT INTO qlt_rule (rule_code, rule_name, rule_type, check_type, check_config, view_id, entity_id, entity_type, table_name, field_code, field_name, template_id, severity, status, description, created_by)
VALUES ('RULE_VEND_005', '供应商国家值域检查', 'accuracy', 'domain', '{"checkType":"domain","domainCode":"COUNTRY","allowNull":true}', @vendor_view_id, @vendor_entity_id, 'main', @vendor_table, 'LAND1', '国家', @tpl_domain, 'warning', 'published', '检查国家代码在有效值域范围内', 'system');

-- =============================================
-- 查看结果
-- =============================================

SELECT '========================================' AS '';
SELECT '质量规则初始化完成！' AS message;
SELECT '========================================' AS '';

SELECT
    rule_code AS '规则编码',
    rule_name AS '规则名称',
    rule_type AS '规则类型',
    field_name AS '检查字段',
    severity AS '严重级别',
    status AS '状态'
FROM qlt_rule
ORDER BY rule_code;
