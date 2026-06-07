-- =============================================
-- MDM 主数据管理平台 - 数据库初始化脚本
-- 版本: v1.0
-- 日期: 2026-06-05
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS mdm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mdm;

-- =============================================
-- 1. 数据标准与模型中心模块
-- =============================================

-- 1.1 字段标准库表
DROP TABLE IF EXISTS std_field;
CREATE TABLE std_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    field_code VARCHAR(100) NOT NULL COMMENT '字段编码',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    field_type VARCHAR(50) NOT NULL COMMENT '字段类型: string/number/date/datetime/boolean/text/select/multi_select',
    length INT COMMENT '字段长度',
    precision_val INT COMMENT '精度',
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
    UNIQUE KEY uk_field_code (field_code),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段标准库表';

-- 1.2 数据标准视图表
DROP TABLE IF EXISTS std_data;
CREATE TABLE std_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    standard_code VARCHAR(100) NOT NULL COMMENT '标准编码',
    standard_name VARCHAR(200) NOT NULL COMMENT '标准名称',
    standard_type VARCHAR(50) COMMENT '标准类型: master_data/reference_data/business_data',
    category_id BIGINT COMMENT '分类ID',
    fields_definition TEXT COMMENT '字段定义JSON',
    status VARCHAR(20) DEFAULT 'draft' COMMENT 'draft-草稿/published-已发布/archived-已归档',
    version INT DEFAULT 1 COMMENT '版本号',
    publish_time DATETIME COMMENT '发布时间',
    approval_status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending-待审/approved-通过/rejected-驳回',
    approval_by VARCHAR(50) COMMENT '审批人',
    approval_time DATETIME COMMENT '审批时间',
    approval_comment TEXT COMMENT '审批意见',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_standard_code (standard_code),
    INDEX idx_status (status),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据标准视图表';

-- 1.3 编码规则表
DROP TABLE IF EXISTS std_encoding_rule;
CREATE TABLE std_encoding_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_code VARCHAR(100) NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_definition TEXT COMMENT '规则定义JSON',
    example VARCHAR(200) COMMENT '示例',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_rule_code (rule_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编码规则表';

-- 1.4 值域表
DROP TABLE IF EXISTS std_value_domain;
CREATE TABLE std_value_domain (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    domain_code VARCHAR(100) NOT NULL COMMENT '值域编码',
    domain_name VARCHAR(200) NOT NULL COMMENT '值域名称',
    domain_type VARCHAR(50) COMMENT '值域类型: enum-枚举/range-范围/reference-引用',
    values_definition TEXT COMMENT '值定义JSON',
    parent_id BIGINT COMMENT '父级ID',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_domain_code (domain_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值域表';

-- =============================================
-- 2. 表单与视图设计中心模块
-- =============================================

-- 2.1 表单管理表
DROP TABLE IF EXISTS frm_form;
CREATE TABLE frm_form (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    form_code VARCHAR(100) NOT NULL COMMENT '表单编码',
    form_name VARCHAR(200) NOT NULL COMMENT '表单名称',
    form_type VARCHAR(50) COMMENT '表单类型: create/edit/view/search',
    master_data_type_id BIGINT COMMENT '关联主数据类型ID',
    form_schema TEXT COMMENT '表单Schema JSON',
    version INT DEFAULT 1 COMMENT '版本号',
    status VARCHAR(20) DEFAULT 'draft' COMMENT 'draft-草稿/published-已发布',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认表单: 0-否 1-是',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_form_code (form_code),
    INDEX idx_master_data_type_id (master_data_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单管理表';

-- =============================================
-- 3. 流程与任务管理中心模块
-- =============================================

-- 3.1 流程定义表
DROP TABLE IF EXISTS wfl_definition;
CREATE TABLE wfl_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    workflow_code VARCHAR(100) NOT NULL COMMENT '流程编码',
    workflow_name VARCHAR(200) NOT NULL COMMENT '流程名称',
    workflow_type VARCHAR(50) COMMENT '流程类型: approval/change/review',
    form_id BIGINT COMMENT '关联表单ID',
    workflow_definition TEXT COMMENT '流程定义JSON',
    integration_type VARCHAR(50) COMMENT '集成类型: internal/lbpm',
    external_workflow_id VARCHAR(100) COMMENT '外部流程ID',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    version INT DEFAULT 1 COMMENT '版本号',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_workflow_code (workflow_code),
    INDEX idx_form_id (form_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';

-- 3.2 流程实例表
DROP TABLE IF EXISTS wfl_instance;
CREATE TABLE wfl_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    workflow_definition_id BIGINT NOT NULL COMMENT '流程定义ID',
    business_key VARCHAR(200) COMMENT '业务键',
    business_data_id BIGINT COMMENT '业务数据ID',
    status VARCHAR(20) DEFAULT 'running' COMMENT 'running/completed/cancelled',
    start_user VARCHAR(50) COMMENT '发起人',
    start_time DATETIME COMMENT '发起时间',
    end_time DATETIME COMMENT '结束时间',
    current_node VARCHAR(200) COMMENT '当前节点',
    variables TEXT COMMENT '流程变量JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_workflow_definition_id (workflow_definition_id),
    INDEX idx_business_key (business_key),
    INDEX idx_status (status),
    INDEX idx_start_user (start_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例表';

-- 3.3 任务表
DROP TABLE IF EXISTS wfl_task;
CREATE TABLE wfl_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_code VARCHAR(100) NOT NULL COMMENT '任务编码',
    workflow_instance_id BIGINT COMMENT '流程实例ID',
    task_name VARCHAR(200) COMMENT '任务名称',
    task_type VARCHAR(50) COMMENT '任务类型: approval/review/approve',
    assignee VARCHAR(50) COMMENT '处理人',
    delegation_user VARCHAR(50) COMMENT '委托人',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/completed/cancelled',
    priority INT DEFAULT 0 COMMENT '优先级',
    due_date DATETIME COMMENT '截止时间',
    complete_time DATETIME COMMENT '完成时间',
    parent_task_id BIGINT COMMENT '父任务ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_task_code (task_code),
    INDEX idx_workflow_instance_id (workflow_instance_id),
    INDEX idx_assignee (assignee),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- =============================================
-- 4. 主数据生命周期管理模块
-- =============================================

-- 4.1 主数据类型表
DROP TABLE IF EXISTS mst_type;
CREATE TABLE mst_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    type_code VARCHAR(100) NOT NULL COMMENT '类型编码',
    type_name VARCHAR(200) NOT NULL COMMENT '类型名称',
    parent_type_id BIGINT COMMENT '父类型ID',
    category VARCHAR(50) COMMENT '分类: enterprise/product/supplier/customer',
    icon VARCHAR(200) COMMENT '图标',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    lifecycle_config TEXT COMMENT '生命周期配置JSON',
    form_config TEXT COMMENT '表单配置JSON',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_type_code (type_code),
    INDEX idx_parent_type_id (parent_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主数据类型表';

-- 4.2 主数据实例表
DROP TABLE IF EXISTS mst_instance;
CREATE TABLE mst_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    type_id BIGINT NOT NULL COMMENT '数据类型ID',
    data_code VARCHAR(200) NOT NULL COMMENT '数据编码',
    data_name VARCHAR(500) NOT NULL COMMENT '数据名称',
    data_value TEXT COMMENT '数据值JSON',
    lifecycle_status VARCHAR(50) DEFAULT 'draft' COMMENT 'draft/active/frozen/archived',
    version INT DEFAULT 1 COMMENT '版本号',
    parent_id BIGINT COMMENT '父记录ID',
    effective_date DATE COMMENT '生效日期',
    expiry_date DATE COMMENT '失效日期',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_type_code (type_id, data_code),
    INDEX idx_type_id (type_id),
    INDEX idx_data_code (data_code),
    INDEX idx_lifecycle_status (lifecycle_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主数据实例表';

-- 4.3 生命周期状态表
DROP TABLE IF EXISTS mst_lifecycle_state;
CREATE TABLE mst_lifecycle_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    type_id BIGINT NOT NULL COMMENT '数据类型ID',
    state_code VARCHAR(50) NOT NULL COMMENT '状态编码',
    state_name VARCHAR(100) NOT NULL COMMENT '状态名称',
    state_type VARCHAR(50) COMMENT '状态类型: initial/normal/final',
    allowed_actions TEXT COMMENT '允许的操作JSON',
    transitions TEXT COMMENT '状态转换配置JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_type_state (type_id, state_code),
    INDEX idx_type_id (type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生命周期状态表';

-- =============================================
-- 5. 版本与审计中心模块
-- =============================================

-- 5.1 版本快照表
DROP TABLE IF EXISTS ver_snapshot;
CREATE TABLE ver_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    version INT NOT NULL COMMENT '版本号',
    snapshot_data TEXT COMMENT '快照数据JSON',
    change_summary TEXT COMMENT '变更摘要',
    operation_type VARCHAR(50) COMMENT '操作类型',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_entity_version (entity_type, entity_id, version),
    INDEX idx_entity_type (entity_type),
    INDEX idx_entity_id (entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='版本快照表';

-- 5.2 审计日志表
DROP TABLE IF EXISTS ver_audit_log;
CREATE TABLE ver_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    log_type VARCHAR(50) NOT NULL COMMENT '日志类型',
    module VARCHAR(50) COMMENT '模块',
    operation VARCHAR(50) COMMENT '操作',
    entity_type VARCHAR(50) COMMENT '实体类型',
    entity_id BIGINT COMMENT '实体ID',
    entity_name VARCHAR(200) COMMENT '实体名称',
    operation_detail TEXT COMMENT '操作详情JSON',
    before_data TEXT COMMENT '操作前数据JSON',
    after_data TEXT COMMENT '操作后数据JSON',
    operator VARCHAR(50) COMMENT '操作人',
    operator_ip VARCHAR(50) COMMENT '操作IP',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_log_type (log_type),
    INDEX idx_module (module),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_operator (operator),
    INDEX idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- =============================================
-- 6. 系统基础配置中心模块
-- =============================================

-- 6.1 组织机构表
DROP TABLE IF EXISTS sys_organization;
CREATE TABLE sys_organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    org_code VARCHAR(100) NOT NULL COMMENT '组织编码',
    org_name VARCHAR(200) NOT NULL COMMENT '组织名称',
    org_type VARCHAR(50) COMMENT '组织类型: company/department/group/position',
    parent_id BIGINT COMMENT '父级ID',
    level INT DEFAULT 1 COMMENT '层级',
    path VARCHAR(500) COMMENT '路径',
    manager VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_org_code (org_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_path (path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织机构表';

-- 6.2 规则脚本表
DROP TABLE IF EXISTS sys_rule_script;
CREATE TABLE sys_rule_script (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    script_code VARCHAR(100) NOT NULL COMMENT '脚本编码',
    script_name VARCHAR(200) NOT NULL COMMENT '脚本名称',
    script_type VARCHAR(50) COMMENT '脚本类型: groovy/javascript',
    script_content TEXT COMMENT '脚本内容',
    input_params TEXT COMMENT '输入参数JSON',
    output_params TEXT COMMENT '输出参数JSON',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_script_code (script_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则脚本表';

-- 6.3 定时任务表
DROP TABLE IF EXISTS sys_schedule_task;
CREATE TABLE sys_schedule_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_code VARCHAR(100) NOT NULL COMMENT '任务编码',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) COMMENT '任务类型',
    task_params TEXT COMMENT '任务参数JSON',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    task_class VARCHAR(500) COMMENT '任务执行类',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/paused-暂停',
    last_execute_time DATETIME COMMENT '最后执行时间',
    next_execute_time DATETIME COMMENT '下次执行时间',
    execute_count INT DEFAULT 0 COMMENT '执行次数',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_task_code (task_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- 6.4 系统配置表
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(50) COMMENT '配置类型',
    config_group VARCHAR(50) COMMENT '配置分组',
    description VARCHAR(500) COMMENT '描述',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密: 0-否 1-是',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key),
    INDEX idx_config_group (config_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =============================================
-- 完成
-- =============================================
SELECT 'MDM 数据库表结构创建完成!' AS message;
