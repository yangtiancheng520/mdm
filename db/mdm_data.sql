-- =============================================
-- MDM 主数据管理平台 - 初始化数据脚本
-- 版本: v1.0
-- 日期: 2026-06-05
-- =============================================

USE mdm;

-- =============================================
-- 1. 组织机构初始数据
-- =============================================

INSERT INTO sys_organization (id, org_code, org_name, org_type, parent_id, level, path, sort, status, created_by) VALUES
(1, 'HQ', '总部', 'company', NULL, 1, '/1', 1, 'active', 'admin'),
(2, 'TECH', '技术部', 'department', 1, 2, '/1/2', 1, 'active', 'admin'),
(3, 'SALES', '销售部', 'department', 1, 2, '/1/3', 2, 'active', 'admin'),
(4, 'FINANCE', '财务部', 'department', 1, 2, '/1/4', 3, 'active', 'admin'),
(5, 'HR', '人力资源部', 'department', 1, 2, '/1/5', 4, 'active', 'admin');

-- =============================================
-- 2. 字段标准库初始数据
-- =============================================

INSERT INTO md_field_standard (field_code, field_name, field_type, length, description, status, category, is_required, created_by) VALUES
-- 企业基本信息
('COMPANY_NAME', '公司名称', 'string', 200, '公司全称', 'published', 'enterprise', 1, 'admin'),
('UNIFIED_SOCIAL_CODE', '统一社会信用代码', 'string', 18, '18位统一社会信用代码', 'published', 'enterprise', 1, 'admin'),
('REGISTERED_CAPITAL', '注册资本', 'number', NULL, '注册资本(万元)', 'published', 'enterprise', 0, 'admin'),
('ESTABLISH_DATE', '成立日期', 'date', NULL, '企业成立日期', 'published', 'enterprise', 0, 'admin'),
('LEGAL_PERSON', '法定代表人', 'string', 50, '法定代表人姓名', 'published', 'enterprise', 0, 'admin'),
('BUSINESS_SCOPE', '经营范围', 'text', NULL, '经营范围描述', 'published', 'enterprise', 0, 'admin'),
('REGISTERED_ADDRESS', '注册地址', 'string', 500, '企业注册地址', 'published', 'enterprise', 0, 'admin'),

-- 联系信息
('CONTACT_NAME', '联系人', 'string', 50, '联系人姓名', 'published', 'contact', 0, 'admin'),
('CONTACT_PHONE', '联系电话', 'string', 20, '联系电话', 'published', 'contact', 0, 'admin'),
('CONTACT_EMAIL', '联系邮箱', 'string', 100, '联系邮箱', 'published', 'contact', 0, 'admin'),
('FAX', '传真', 'string', 20, '传真号码', 'published', 'contact', 0, 'admin'),
('WEBSITE', '网站', 'string', 200, '企业网站', 'published', 'contact', 0, 'admin'),

-- 银行信息
('BANK_NAME', '开户银行', 'string', 100, '开户银行名称', 'published', 'bank', 0, 'admin'),
('BANK_ACCOUNT', '银行账号', 'string', 30, '银行账号', 'published', 'bank', 0, 'admin'),
('BANK_ACCOUNT_NAME', '账户名称', 'string', 100, '银行账户名称', 'published', 'bank', 0, 'admin'),

-- 供应商信息
('SUPPLIER_CODE', '供应商编码', 'string', 50, '供应商编码', 'published', 'supplier', 1, 'admin'),
('SUPPLIER_NAME', '供应商名称', 'string', 200, '供应商名称', 'published', 'supplier', 1, 'admin'),
('SUPPLIER_TYPE', '供应商类型', 'select', 50, '供应商类型', 'published', 'supplier', 0, 'admin'),
('CREDIT_LEVEL', '信用等级', 'select', 20, '信用等级(A/B/C/D)', 'published', 'supplier', 0, 'admin'),

-- 客户信息
('CUSTOMER_CODE', '客户编码', 'string', 50, '客户编码', 'published', 'customer', 1, 'admin'),
('CUSTOMER_NAME', '客户名称', 'string', 200, '客户名称', 'published', 'customer', 1, 'admin'),
('CUSTOMER_TYPE', '客户类型', 'select', 50, '客户类型', 'published', 'customer', 0, 'admin'),
('INDUSTRY', '所属行业', 'select', 50, '所属行业', 'published', 'customer', 0, 'admin');

-- =============================================
-- 3. 值域初始数据
-- =============================================

INSERT INTO md_value_domain (domain_code, domain_name, domain_type, values_definition, status, created_by) VALUES
('ENTERPRISE_TYPE', '企业类型', 'enum', '[{"value":"1","label":"国有企业","sort":1},{"value":"2","label":"集体企业","sort":2},{"value":"3","label":"私营企业","sort":3},{"value":"4","label":"股份制企业","sort":4},{"value":"5","label":"外商投资企业","sort":5},{"value":"6","label":"港澳台投资企业","sort":6}]', 'active', 'admin'),

('CURRENCY', '币种', 'enum', '[{"value":"CNY","label":"人民币","sort":1},{"value":"USD","label":"美元","sort":2},{"value":"EUR","label":"欧元","sort":3},{"value":"GBP","label":"英镑","sort":4},{"value":"JPY","label":"日元","sort":5}]', 'active', 'admin'),

('SUPPLIER_TYPE', '供应商类型', 'enum', '[{"value":"1","label":"原材料供应商","sort":1},{"value":"2","label":"设备供应商","sort":2},{"value":"3","label":"服务供应商","sort":3},{"value":"4","label":"其他","sort":4}]', 'active', 'admin'),

('CUSTOMER_TYPE', '客户类型', 'enum', '[{"value":"1","label":"企业客户","sort":1},{"value":"2","label":"个人客户","sort":2},{"value":"3","label":"政府机构","sort":3},{"value":"4","label":"其他","sort":4}]', 'active', 'admin'),

('CREDIT_LEVEL', '信用等级', 'enum', '[{"value":"A","label":"A级(优秀)","sort":1},{"value":"B","label":"B级(良好)","sort":2},{"value":"C","label":"C级(一般)","sort":3},{"value":"D","label":"D级(较差)","sort":4}]', 'active', 'admin'),

('INDUSTRY', '所属行业', 'enum', '[{"value":"1","label":"制造业","sort":1},{"value":"2","label":"建筑业","sort":2},{"value":"3","label":"批发零售业","sort":3},{"value":"4","label":"信息技术","sort":4},{"value":"5","label":"金融业","sort":5},{"value":"6","label":"房地产业","sort":6},{"value":"7","label":"其他","sort":7}]', 'active', 'admin'),

('STATUS', '状态', 'enum', '[{"value":"active","label":"有效","sort":1},{"value":"inactive","label":"无效","sort":2}]', 'active', 'admin'),

('LIFECYCLE_STATUS', '生命周期状态', 'enum', '[{"value":"draft","label":"草稿","sort":1},{"value":"active","label":"生效","sort":2},{"value":"frozen","label":"冻结","sort":3},{"value":"archived","label":"归档","sort":4}]', 'active', 'admin');

-- =============================================
-- 4. 编码规则初始数据
-- =============================================

INSERT INTO md_encoding_rule (rule_code, rule_name, rule_definition, example, status, created_by, description) VALUES
('SUPPLIER_CODE', '供应商编码规则', '{"segments":[{"type":"fixed","value":"SUP"},{"type":"date","format":"yyyyMMdd"},{"type":"sequence","length":4,"padding":"0"}],"resetCycle":"daily"}', 'SUP202606050001', 'active', 'admin', '供应商编码自动生成规则'),

('CUSTOMER_CODE', '客户编码规则', '{"segments":[{"type":"fixed","value":"CUS"},{"type":"date","format":"yyyyMMdd"},{"type":"sequence","length":4,"padding":"0"}],"resetCycle":"daily"}', 'CUS202606050001', 'active', 'admin', '客户编码自动生成规则'),

('MASTER_DATA_CODE', '主数据编码规则', '{"segments":[{"type":"fixed","value":"MD"},{"type":"date","format":"yyyyMMdd"},{"type":"sequence","length":6,"padding":"0"}],"resetCycle":"daily"}', 'MD20260605000001', 'active', 'admin', '通用主数据编码规则');

-- =============================================
-- 5. 主数据类型初始数据
-- =============================================

INSERT INTO md_master_data_type (type_code, type_name, category, description, lifecycle_config, status, created_by) VALUES
('ENTERPRISE', '企业信息', 'enterprise', '企业基础信息管理', '{"states":[{"code":"draft","name":"草稿","type":"initial"},{"code":"active","name":"生效","type":"normal"},{"code":"frozen","name":"冻结","type":"normal"},{"code":"archived","name":"归档","type":"final"}],"transitions":[{"from":"draft","to":"active","action":"submit"},{"from":"active","to":"frozen","action":"freeze"},{"from":"frozen","to":"active","action":"unfreeze"},{"from":"active","to":"archived","action":"archive"}]}', 'active', 'admin'),

('SUPPLIER', '供应商信息', 'supplier', '供应商信息管理', '{"states":[{"code":"draft","name":"草稿","type":"initial"},{"code":"active","name":"生效","type":"normal"},{"code":"frozen","name":"冻结","type":"normal"},{"code":"archived","name":"归档","type":"final"}],"transitions":[{"from":"draft","to":"active","action":"submit"},{"from":"active","to":"frozen","action":"freeze"},{"from":"frozen","to":"active","action":"unfreeze"},{"from":"active","to":"archived","action":"archive"}]}', 'active', 'admin'),

('CUSTOMER', '客户信息', 'customer', '客户信息管理', '{"states":[{"code":"draft","name":"草稿","type":"initial"},{"code":"active","name":"生效","type":"normal"},{"code":"frozen","name":"冻结","type":"normal"},{"code":"archived","name":"归档","type":"final"}],"transitions":[{"from":"draft","to":"active","action":"submit"},{"from":"active","to":"frozen","action":"freeze"},{"from":"frozen","to":"active","action":"unfreeze"},{"from":"active","to":"archived","action":"archive"}]}', 'active', 'admin');

-- =============================================
-- 6. 流程定义初始数据
-- =============================================

INSERT INTO md_workflow_definition (workflow_code, workflow_name, workflow_type, integration_type, status, created_by, description) VALUES
('SUPPLIER_APPROVAL', '供应商审批流程', 'approval', 'internal', 'active', 'admin', '供应商新增/变更审批流程'),
('CUSTOMER_APPROVAL', '客户审批流程', 'approval', 'internal', 'active', 'admin', '客户新增/变更审批流程'),
('DATA_CHANGE_APPROVAL', '主数据变更审批流程', 'change', 'internal', 'active', 'admin', '主数据变更审批流程');

-- =============================================
-- 7. 系统配置初始数据
-- =============================================

INSERT INTO sys_config (config_key, config_value, config_type, config_group, description, created_by) VALUES
-- 系统基础配置
('system.name', 'MDM主数据管理平台', 'string', 'system', '系统名称', 'admin'),
('system.version', '1.0.0', 'string', 'system', '系统版本', 'admin'),
('system.logo', '/assets/logo.png', 'string', 'system', '系统Logo', 'admin'),

-- 文件上传配置
('upload.max_size', '104857600', 'number', 'upload', '最大上传文件大小(字节)', 'admin'),
('upload.allow_types', 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx', 'string', 'upload', '允许上传的文件类型', 'admin'),

-- 数据配置
('data.page_size', '20', 'number', 'data', '默认分页大小', 'admin'),
('data.max_page_size', '100', 'number', 'data', '最大分页大小', 'admin');

-- =============================================
-- 8. 规则脚本初始数据
-- =============================================

INSERT INTO sys_rule_script (script_code, script_name, script_type, script_content, status, created_by, description) VALUES
('UNIFIED_SOCIAL_CODE_VALIDATE', '统一社会信用代码校验', 'groovy', '''
// 校验统一社会信用代码格式
def validate(code) {
    if (code == null || code.length() != 18) {
        return false
    }
    // 前17位必须是数字或大写字母
    def pattern = ~"^[0-9A-Z]{18}$"
    return code.matches(pattern)
}
return validate(input)
''', 'active', 'admin', '校验统一社会信用代码18位格式'),

('SUPPLIER_CODE_GENERATE', '供应商编码生成', 'groovy', '''
// 生成供应商编码: SUP + 日期 + 序号
def prefix = "SUP"
def dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new Date())
def seq = sequence.next("SUPPLIER", 4)
return prefix + dateStr + seq
''', 'active', 'admin', '自动生成供应商编码');

-- =============================================
-- 完成
-- =============================================
SELECT 'MDM 初始化数据插入完成!' AS message;
