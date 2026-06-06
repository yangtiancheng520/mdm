-- ==========================================
-- 数据标准与模型中心模块 - 数据库表创建脚本
-- ==========================================

-- 1. 字段标准库表
CREATE TABLE IF NOT EXISTS std_field_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    field_code VARCHAR(100) NOT NULL UNIQUE COMMENT '字段编码',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    field_type VARCHAR(50) NOT NULL COMMENT '字段类型: string/number/date/datetime/boolean/text/select/multi_select',
    length INT COMMENT '字段长度',
    `precision` INT COMMENT '精度',
    default_value VARCHAR(500) COMMENT '默认值',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填: 0-否 1-是',
    validation_rule TEXT COMMENT '校验规则JSON',
    reference_id BIGINT COMMENT '引用中台字段ID',
    reference_source VARCHAR(50) COMMENT '引用来源: data_platform/manual',
    category VARCHAR(50) COMMENT '字段分类',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态: draft-草稿/published-已发布/archived-已归档',
    version INT DEFAULT 1 COMMENT '版本号',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_field_code (field_code),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段标准库表';

-- 2. 数据标准视图表
CREATE TABLE IF NOT EXISTS std_data_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    standard_code VARCHAR(100) NOT NULL UNIQUE COMMENT '标准编码',
    standard_name VARCHAR(200) NOT NULL COMMENT '标准名称',
    standard_type VARCHAR(50) COMMENT '标准类型: master_data-reference_data-business_data',
    category_id BIGINT COMMENT '分类ID',
    fields_definition TEXT COMMENT '字段定义JSON: [{fieldId, required, defaultValue}]',
    status VARCHAR(20) DEFAULT 'draft' COMMENT 'draft-草稿/published-已发布/archived-已归档',
    version INT DEFAULT 1 COMMENT '版本号',
    publish_time DATETIME COMMENT '发布时间',
    approval_status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending-待审/approved-通过/rejected-驳回',
    approval_by VARCHAR(50) COMMENT '审批人',
    approval_time DATETIME COMMENT '审批时间',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_standard_code (standard_code),
    INDEX idx_status (status),
    INDEX idx_standard_type (standard_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据标准视图表';

-- 3. 编码规则表
CREATE TABLE IF NOT EXISTS std_encoding_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_code VARCHAR(100) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) COMMENT '规则类型: prefix-sequence-date-custom',
    prefix VARCHAR(50) COMMENT '前缀',
    date_format VARCHAR(50) COMMENT '日期格式: yyyyMM/ddMM/yyyy',
    sequence_length INT COMMENT '序列号长度',
    current_value BIGINT DEFAULT 0 COMMENT '当前值',
    reset_cycle VARCHAR(20) COMMENT '重置周期: day/month/year/never',
    preview_example VARCHAR(200) COMMENT '示例',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_rule_code (rule_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编码规则表';

-- 4. 值域表
CREATE TABLE IF NOT EXISTS std_value_domain (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    domain_code VARCHAR(100) NOT NULL UNIQUE COMMENT '值域编码',
    domain_name VARCHAR(200) NOT NULL COMMENT '值域名称',
    domain_type VARCHAR(50) COMMENT '值域类型: enum-reference-custom',
    parent_id BIGINT COMMENT '父级ID',
    level INT DEFAULT 1 COMMENT '层级',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    sort INT DEFAULT 0 COMMENT '排序',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_domain_code (domain_code),
    INDEX idx_status (status),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值域表';

-- 5. 值域项表
CREATE TABLE IF NOT EXISTS std_value_domain_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    domain_id BIGINT NOT NULL COMMENT '值域ID',
    item_code VARCHAR(100) NOT NULL COMMENT '项编码',
    item_name VARCHAR(200) NOT NULL COMMENT '项名称',
    parent_item_id BIGINT COMMENT '父级项ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_domain_id (domain_id),
    INDEX idx_item_code (item_code),
    INDEX idx_parent_item_id (parent_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值域项表';

-- ==========================================
-- 插入示例数据
-- ==========================================

-- 插入字段标准示例数据
INSERT INTO std_field_standard (field_code, field_name, field_type, length, description, status) VALUES
('COMPANY_NAME', '公司名称', 'string', 200, '公司全称', 'published'),
('UNIFIED_SOCIAL_CODE', '统一社会信用代码', 'string', 18, '18位统一社会信用代码', 'published'),
('REGISTERED_CAPITAL', '注册资本', 'number', NULL, '注册资本(万元)', 'published'),
('ESTABLISH_DATE', '成立日期', 'date', NULL, '企业成立日期', 'published'),
('LEGAL_PERSON', '法定代表人', 'string', 50, '法定代表人姓名', 'published'),
('BUSINESS_SCOPE', '经营范围', 'text', NULL, '企业经营范围', 'published'),
('REGISTERED_ADDRESS', '注册地址', 'string', 500, '企业注册地址', 'published'),
('CONTACT_PHONE', '联系电话', 'string', 20, '企业联系电话', 'published'),
('EMAIL', '电子邮箱', 'string', 100, '企业电子邮箱', 'published'),
('COMPANY_TYPE', '企业类型', 'select', 50, '企业类型', 'published');

-- 插入编码规则示例数据
INSERT INTO std_encoding_rule (rule_code, rule_name, rule_type, prefix, date_format, sequence_length, preview_example, status) VALUES
('RULE_COMPANY', '公司编码规则', 'prefix-date-sequence', 'COM', 'yyyyMM', 6, 'COM202601000001', 'active'),
('RULE_DEPARTMENT', '部门编码规则', 'prefix-sequence', 'DEPT', NULL, 4, 'DEPT0001', 'active'),
('RULE_EMPLOYEE', '员工编码规则', 'prefix-date-sequence', 'EMP', 'yyyy', 8, 'EMP202600000001', 'active'),
('RULE_SUPPLIER', '供应商编码规则', 'prefix-sequence', 'SUP', NULL, 6, 'SUP000001', 'active');

-- 插入值域示例数据
INSERT INTO std_value_domain (domain_code, domain_name, domain_type, status, sort) VALUES
('COMPANY_TYPE', '企业类型', 'enum', 'active', 1),
('INDUSTRY_TYPE', '行业类型', 'enum', 'active', 2),
('REGISTER_STATUS', '登记状态', 'enum', 'active', 3),
('CURRENCY_TYPE', '货币类型', 'enum', 'active', 4);

-- 插入值域项示例数据
INSERT INTO std_value_domain_item (domain_id, item_code, item_name, sort, status) VALUES
(1, 'LIMITED_COMPANY', '有限责任公司', 1, 'active'),
(1, 'JOINT_STOCK', '股份有限公司', 2, 'active'),
(1, 'PARTNERSHIP', '合伙企业', 3, 'active'),
(1, 'SOLE_PROPRIETORSHIP', '个人独资企业', 4, 'active'),
(1, 'INDIVIDUAL', '个体工商户', 5, 'active');

-- ==========================================
-- 执行成功提示
-- ==========================================
SELECT '数据标准与模型中心数据库表创建成功！' AS message;
