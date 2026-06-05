# MDM 主数据管理平台完整实现计划 - 第一部分：技术架构设计

## 一、技术架构设计概述

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端展示层 (Vue 3)                         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │ 数据标准 │ │ 表单设计 │ │ 流程管理 │ │ 主数据管理 │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │ 质量管理 │ │ 分发管理 │ │ 版本审计 │ │ 系统配置 │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↕ HTTP/HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                        后端服务层 (Spring Boot)                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Controller 控制层                      │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Service 服务层                         │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │   │
│  │  │标准服务 │ │表单服务 │ │流程服务 │ │主数据服务│       │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘       │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Engine 引擎层                          │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │   │
│  │  │表单引擎 │ │流程引擎 │ │规则引擎 │ │质量引擎 │       │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘       │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Repository 数据层                      │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────────┐
│                        数据存储层                                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │  MySQL   │ │  Redis   │ │RabbitMQ │ │  MinIO   │          │
│  │ 业务数据 │ │ 缓存数据 │ │ 消息队列 │ │ 文件存储 │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────────┐
│                      外部系统集成                                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                       │
│  │ 天眼查   │ │ 蓝凌LBPM │ │ 数据中台 │                       │
│  └──────────┘ └──────────┘ └──────────┘                       │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 技术栈清单

**后端技术栈**:
- Spring Boot 3.2.5
- Spring Data JPA + Hibernate
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.12+
- Quartz Scheduler
- Groovy 4.0+ (规则引擎)
- MinIO/OSS (文件存储)

**前端技术栈**:
- Vue 3.5+
- TypeScript 6.0+
- Vite 8.0+
- Element Plus 2.13+
- Pinia 3.0+
- Vue Router 4.0+
- Monaco Editor (代码编辑器)
- bpmn-js (流程设计器，可选)


## 二、数据库表结构设计

### 2.1 数据标准与模型中心模块

#### 2.1.1 字段标准库表

```sql
CREATE TABLE md_field_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    field_code VARCHAR(100) NOT NULL UNIQUE COMMENT '字段编码',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    field_type VARCHAR(50) NOT NULL COMMENT '字段类型: string/number/date/datetime/boolean/text/select/multi_select',
    length INT COMMENT '字段长度',
    precision INT COMMENT '精度',
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

-- 示例数据
INSERT INTO md_field_standard (field_code, field_name, field_type, length, description, status) VALUES
('COMPANY_NAME', '公司名称', 'string', 200, '公司全称', 'published'),
('UNIFIED_SOCIAL_CODE', '统一社会信用代码', 'string', 18, '18位统一社会信用代码', 'published'),
('REGISTERED_CAPITAL', '注册资本', 'number', NULL, '注册资本(万元)', 'published'),
('ESTABLISH_DATE', '成立日期', 'date', NULL, '企业成立日期', 'published');
```

#### 2.1.2 数据标准视图表

```sql
CREATE TABLE md_data_standard (
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
    approval_comment TEXT COMMENT '审批意见',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_standard_code (standard_code),
    INDEX idx_status (status),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据标准视图表';
```

#### 2.1.3 编码规则表

```sql
CREATE TABLE md_encoding_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_code VARCHAR(100) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_definition TEXT COMMENT '规则定义JSON: {segments: [{type, value, length, padding}]}',
    example VARCHAR(200) COMMENT '示例',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_rule_code (rule_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编码规则表';

-- 规则定义示例
-- {
--   "segments": [
--     {"type": "fixed", "value": "MD"},
--     {"type": "date", "format": "yyyyMMdd"},
--     {"type": "sequence", "length": 6, "padding": "0"}
--   ],
--   "resetCycle": "daily"
-- }
-- 生成结果: MD20260605000001
```

#### 2.1.4 值域表

```sql
CREATE TABLE md_value_domain (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    domain_code VARCHAR(100) NOT NULL UNIQUE COMMENT '值域编码',
    domain_name VARCHAR(200) NOT NULL COMMENT '值域名称',
    domain_type VARCHAR(50) COMMENT '值域类型: enum-枚举/range-范围/reference-引用',
    values_definition TEXT COMMENT '值定义JSON: [{value, label, parentValue, sort}]',
    parent_id BIGINT COMMENT '父级ID',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_domain_code (domain_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值域表';

-- 示例数据
INSERT INTO md_value_domain (domain_code, domain_name, domain_type, values_definition) VALUES
('ENTERPRISE_TYPE', '企业类型', 'enum', '[{"value":"1","label":"国有企业"},{"value":"2","label":"集体企业"},{"value":"3","label":"私营企业"}]'),
('CURRENCY', '币种', 'enum', '[{"value":"CNY","label":"人民币"},{"value":"USD","label":"美元"},{"value":"EUR","label":"欧元"}]');
```


### 2.2 表单与视图设计中心模块

#### 2.2.1 表单管理表

```sql
CREATE TABLE md_form (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    form_code VARCHAR(100) NOT NULL UNIQUE COMMENT '表单编码',
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
    INDEX idx_form_code (form_code),
    INDEX idx_master_data_type_id (master_data_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单管理表';
```

#### 2.2.2 表单字段配置表

```sql
CREATE TABLE md_form_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    form_id BIGINT NOT NULL COMMENT '表单ID',
    field_standard_id BIGINT COMMENT '关联字段标准ID',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    field_label VARCHAR(200) COMMENT '字段标签',
    field_type VARCHAR(50) COMMENT '字段类型',
    field_config TEXT COMMENT '字段配置JSON: {placeholder, disabled, visible, rules}',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    is_visible TINYINT DEFAULT 1 COMMENT '是否可见: 0-否 1-是',
    is_editable TINYINT DEFAULT 1 COMMENT '是否可编辑: 0-否 1-是',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填: 0-否 1-是',
    validation_rules TEXT COMMENT '校验规则JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_form_id (form_id),
    INDEX idx_field_standard_id (field_standard_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单字段配置表';
```

#### 2.2.3 导入导出模板表

```sql
CREATE TABLE md_import_export_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_code VARCHAR(100) NOT NULL UNIQUE COMMENT '模板编码',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(20) COMMENT '模板类型: import-导入/export-导出',
    form_id BIGINT COMMENT '关联表单ID',
    template_file VARCHAR(500) COMMENT '模板文件路径',
    mapping_config TEXT COMMENT '字段映射配置JSON: [{excelColumn, fieldName, converter}]',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_template_code (template_code),
    INDEX idx_form_id (form_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入导出模板表';
```

### 2.3 流程与任务管理中心模块

#### 2.3.1 流程定义表

```sql
CREATE TABLE md_workflow_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    workflow_code VARCHAR(100) NOT NULL UNIQUE COMMENT '流程编码',
    workflow_name VARCHAR(200) NOT NULL COMMENT '流程名称',
    workflow_type VARCHAR(50) COMMENT '流程类型: approval-change-review',
    form_id BIGINT COMMENT '关联表单ID',
    workflow_definition TEXT COMMENT '流程定义JSON/BPMN',
    integration_type VARCHAR(50) COMMENT '集成类型: internal-内置/lbpm-蓝凌',
    external_workflow_id VARCHAR(100) COMMENT '外部流程ID',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    version INT DEFAULT 1 COMMENT '版本号',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_workflow_code (workflow_code),
    INDEX idx_form_id (form_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';
```

#### 2.3.2 流程实例表

```sql
CREATE TABLE md_workflow_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    workflow_definition_id BIGINT NOT NULL COMMENT '流程定义ID',
    business_key VARCHAR(200) COMMENT '业务键',
    business_data_id BIGINT COMMENT '业务数据ID',
    status VARCHAR(20) DEFAULT 'running' COMMENT 'running-运行中/completed-已完成/cancelled-已取消',
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
```

#### 2.3.3 任务表

```sql
CREATE TABLE md_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_code VARCHAR(100) NOT NULL UNIQUE COMMENT '任务编码',
    workflow_instance_id BIGINT COMMENT '流程实例ID',
    task_name VARCHAR(200) COMMENT '任务名称',
    task_type VARCHAR(50) COMMENT '任务类型: approval-review-approve',
    assignee VARCHAR(50) COMMENT '处理人',
    delegation_user VARCHAR(50) COMMENT '委托人',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending-待办/completed-已完成/cancelled-已取消',
    priority INT DEFAULT 0 COMMENT '优先级: 0-低 1-中 2-高',
    due_date DATETIME COMMENT '截止时间',
    complete_time DATETIME COMMENT '完成时间',
    parent_task_id BIGINT COMMENT '父任务ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_code (task_code),
    INDEX idx_workflow_instance_id (workflow_instance_id),
    INDEX idx_assignee (assignee),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';
```


### 2.4 主数据生命周期管理模块

#### 2.4.1 主数据类型表

```sql
CREATE TABLE md_master_data_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    type_code VARCHAR(100) NOT NULL UNIQUE COMMENT '类型编码',
    type_name VARCHAR(200) NOT NULL COMMENT '类型名称',
    parent_type_id BIGINT COMMENT '父类型ID',
    category VARCHAR(50) COMMENT '分类: enterprise-product-supplier-customer',
    icon VARCHAR(200) COMMENT '图标',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    lifecycle_config TEXT COMMENT '生命周期配置JSON: {states:[{code,name,actions}]}',
    form_config TEXT COMMENT '表单配置JSON: {createFormId,editFormId,viewFormId}',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_type_code (type_code),
    INDEX idx_parent_type_id (parent_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主数据类型表';

-- 示例数据
INSERT INTO md_master_data_type (type_code, type_name, category, description) VALUES
('ENTERPRISE', '企业信息', 'enterprise', '企业基础信息管理'),
('SUPPLIER', '供应商信息', 'supplier', '供应商信息管理');
```

#### 2.4.2 主数据实例表

```sql
CREATE TABLE md_master_data_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    type_id BIGINT NOT NULL COMMENT '数据类型ID',
    data_code VARCHAR(200) NOT NULL COMMENT '数据编码',
    data_name VARCHAR(500) NOT NULL COMMENT '数据名称',
    data_value TEXT COMMENT '数据值JSON: {fieldCode: value}',
    lifecycle_status VARCHAR(50) DEFAULT 'draft' COMMENT 'draft-草稿/active-激活/frozen-冻结/archived-归档',
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
    INDEX idx_lifecycle_status (lifecycle_status),
    INDEX idx_effective_date (effective_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主数据实例表';
```

#### 2.4.3 状态机配置表

```sql
CREATE TABLE md_lifecycle_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    type_id BIGINT NOT NULL COMMENT '数据类型ID',
    state_code VARCHAR(50) NOT NULL COMMENT '状态编码',
    state_name VARCHAR(100) NOT NULL COMMENT '状态名称',
    state_type VARCHAR(50) COMMENT '状态类型: initial-初始/normal-正常/final-终态',
    allowed_actions TEXT COMMENT '允许的操作JSON: [{action, next_state}]',
    transitions TEXT COMMENT '状态转换配置JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_type_state (type_id, state_code),
    INDEX idx_type_id (type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='状态机配置表';
```

#### 2.4.4 主数据关系表

```sql
CREATE TABLE md_master_data_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    source_type_id BIGINT NOT NULL COMMENT '源数据类型ID',
    source_data_id BIGINT NOT NULL COMMENT '源数据ID',
    target_type_id BIGINT NOT NULL COMMENT '目标数据类型ID',
    target_data_id BIGINT NOT NULL COMMENT '目标数据ID',
    relation_type VARCHAR(50) NOT NULL COMMENT '关系类型: parent-child-reference',
    relation_name VARCHAR(200) COMMENT '关系名称',
    effective_date DATE COMMENT '生效日期',
    expiry_date DATE COMMENT '失效日期',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-有效/inactive-无效',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_source (source_type_id, source_data_id),
    INDEX idx_target (target_type_id, target_data_id),
    INDEX idx_relation_type (relation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主数据关系表';
```

### 2.5 数据质量管理模块

#### 2.5.1 质量规则表

```sql
CREATE TABLE md_quality_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_code VARCHAR(100) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) COMMENT '规则类型: completeness-完整性/accuracy-准确性/consistency-一致性/timeliness-及时性/uniqueness-唯一性',
    rule_definition TEXT COMMENT '规则定义JSON: {checkType, expression, fields}',
    field_standard_id BIGINT COMMENT '关联字段标准ID',
    check_frequency VARCHAR(50) COMMENT '检测频率: realtime-实时/scheduled-定时',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    threshold DECIMAL(5,2) COMMENT '阈值百分比',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    integration_source VARCHAR(50) COMMENT '中台集成来源',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_rule_code (rule_code),
    INDEX idx_rule_type (rule_type),
    INDEX idx_field_standard_id (field_standard_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量规则表';
```

#### 2.5.2 质量检测任务表

```sql
CREATE TABLE md_quality_check_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_code VARCHAR(100) NOT NULL UNIQUE COMMENT '任务编码',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    check_type VARCHAR(50) COMMENT '检测类型: realtime-实时/scheduled-定时',
    check_scope TEXT COMMENT '检测范围JSON: {typeIds, dataIds}',
    total_count INT DEFAULT 0 COMMENT '总记录数',
    pass_count INT DEFAULT 0 COMMENT '通过数',
    fail_count INT DEFAULT 0 COMMENT '失败数',
    pass_rate DECIMAL(5,2) COMMENT '通过率',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending-待执行/running-执行中/completed-已完成/failed-失败',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_code (task_code),
    INDEX idx_rule_id (rule_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量检测任务表';
```


### 2.6 主数据分发管理中心模块

#### 2.6.1 分发主题表

```sql
CREATE TABLE md_distribution_topic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    topic_code VARCHAR(100) NOT NULL UNIQUE COMMENT '主题编码',
    topic_name VARCHAR(200) NOT NULL COMMENT '主题名称',
    topic_type VARCHAR(50) COMMENT '主题类型: master_data_change-reference_data_change',
    master_data_type_id BIGINT COMMENT '关联主数据类型ID',
    filter_rule TEXT COMMENT '过滤规则JSON: {conditions: [...]}',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_topic_code (topic_code),
    INDEX idx_master_data_type_id (master_data_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分发主题表';
```

#### 2.6.2 订阅配置表

```sql
CREATE TABLE md_distribution_subscription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    subscription_code VARCHAR(100) NOT NULL UNIQUE COMMENT '订阅编码',
    topic_id BIGINT NOT NULL COMMENT '主题ID',
    subscriber_name VARCHAR(200) COMMENT '订阅方名称',
    subscriber_system VARCHAR(100) COMMENT '订阅系统',
    distribution_mode VARCHAR(50) COMMENT '分发模式: push-推送/pull-拉取',
    distribution_timing VARCHAR(50) COMMENT '分发时机: realtime-实时/scheduled-定时',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    target_endpoint VARCHAR(500) COMMENT '目标端点URL',
    auth_type VARCHAR(50) COMMENT '认证类型: none/basic/oauth2/apikey',
    auth_config TEXT COMMENT '认证配置JSON',
    field_mapping TEXT COMMENT '字段映射JSON: [{sourceField, targetField, converter}]',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_subscription_code (subscription_code),
    INDEX idx_topic_id (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅配置表';
```

#### 2.6.3 分发任务表

```sql
CREATE TABLE md_distribution_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_code VARCHAR(100) NOT NULL UNIQUE COMMENT '任务编码',
    subscription_id BIGINT COMMENT '订阅ID',
    master_data_id BIGINT COMMENT '主数据ID',
    operation_type VARCHAR(50) COMMENT '操作类型: create/update/freeze/unfreeze/archive',
    distribution_mode VARCHAR(50) COMMENT '分发模式: push/pull',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending-待分发/processing-处理中/success-成功/failed-失败',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retry INT DEFAULT 3 COMMENT '最大重试次数',
    request_data TEXT COMMENT '请求数据JSON',
    response_data TEXT COMMENT '响应数据JSON',
    error_message TEXT COMMENT '错误信息',
    distribute_time DATETIME COMMENT '分发时间',
    complete_time DATETIME COMMENT '完成时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_code (task_code),
    INDEX idx_subscription_id (subscription_id),
    INDEX idx_master_data_id (master_data_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分发任务表';
```

### 2.7 版本与审计中心模块

#### 2.7.1 版本快照表

```sql
CREATE TABLE md_version_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型: field_standard/data_standard/form/master_data',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    version INT NOT NULL COMMENT '版本号',
    snapshot_data TEXT COMMENT '快照数据JSON',
    change_summary TEXT COMMENT '变更摘要',
    operation_type VARCHAR(50) COMMENT '操作类型: create/update/delete/publish',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_entity_version (entity_type, entity_id, version),
    INDEX idx_entity_type (entity_type),
    INDEX idx_entity_id (entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='版本快照表';
```

#### 2.7.2 审计日志表

```sql
CREATE TABLE md_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    log_type VARCHAR(50) NOT NULL COMMENT '日志类型: login/operation/data_change',
    module VARCHAR(50) COMMENT '模块: standard/form/workflow/masterdata/distribution/system',
    operation VARCHAR(50) COMMENT '操作: create/read/update/delete/approve/reject',
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
```

### 2.8 系统基础配置中心模块

#### 2.8.1 组织机构表

```sql
CREATE TABLE sys_organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    org_code VARCHAR(100) NOT NULL UNIQUE COMMENT '组织编码',
    org_name VARCHAR(200) NOT NULL COMMENT '组织名称',
    org_type VARCHAR(50) COMMENT '组织类型: company/department/group/position',
    parent_id BIGINT COMMENT '父级ID',
    level INT DEFAULT 1 COMMENT '层级',
    path VARCHAR(500) COMMENT '路径: /1/2/3',
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
    INDEX idx_org_code (org_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_path (path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织机构表';
```

#### 2.8.2 用户扩展表

```sql
CREATE TABLE sys_user_ext (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    org_id BIGINT COMMENT '组织ID',
    position_id BIGINT COMMENT '岗位ID',
    employee_no VARCHAR(50) COMMENT '工号',
    gender VARCHAR(10) COMMENT '性别: male/female',
    birthday DATE COMMENT '生日',
    entry_date DATE COMMENT '入职日期',
    leave_date DATE COMMENT '离职日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_org_id (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户扩展表';
```

#### 2.8.3 数据权限表

```sql
CREATE TABLE sys_data_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    data_type VARCHAR(50) COMMENT '数据类型: master_data_instance/user',
    permission_type VARCHAR(50) COMMENT '权限类型: all/org/suborg/self/custom',
    org_ids TEXT COMMENT '组织ID列表JSON',
    custom_rule TEXT COMMENT '自定义规则JSON',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据权限表';
```

#### 2.8.4 规则脚本表

```sql
CREATE TABLE sys_rule_script (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    script_code VARCHAR(100) NOT NULL UNIQUE COMMENT '脚本编码',
    script_name VARCHAR(200) NOT NULL COMMENT '脚本名称',
    script_type VARCHAR(50) COMMENT '脚本类型: groovy/javascript',
    script_content TEXT COMMENT '脚本内容',
    input_params TEXT COMMENT '输入参数JSON: [{name, type, required}]',
    output_params TEXT COMMENT '输出参数JSON: [{name, type}]',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_script_code (script_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则脚本表';
```

#### 2.8.5 定时任务表

```sql
CREATE TABLE sys_schedule_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_code VARCHAR(100) NOT NULL UNIQUE COMMENT '任务编码',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) COMMENT '任务类型: quality_check/distribution/data_sync',
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
    INDEX idx_task_code (task_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';
```

#### 2.8.6 系统配置表

```sql
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(50) COMMENT '配置类型: string/number/json/boolean',
    config_group VARCHAR(50) COMMENT '配置分组: system/email/storage',
    description VARCHAR(500) COMMENT '描述',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密: 0-否 1-是',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key),
    INDEX idx_config_group (config_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

#### 2.8.7 外部集成配置表

```sql
CREATE TABLE sys_integration_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    integration_code VARCHAR(100) NOT NULL UNIQUE COMMENT '集成编码',
    integration_name VARCHAR(200) NOT NULL COMMENT '集成名称',
    integration_type VARCHAR(50) COMMENT '集成类型: tianyancha/lbpm/data_platform',
    api_endpoint VARCHAR(500) COMMENT 'API端点',
    auth_type VARCHAR(50) COMMENT '认证类型: none/basic/oauth2/apikey',
    auth_config TEXT COMMENT '认证配置JSON',
    request_config TEXT COMMENT '请求配置JSON: {timeout, retryCount}',
    mapping_config TEXT COMMENT '映射配置JSON',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_integration_code (integration_code),
    INDEX idx_integration_type (integration_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部集成配置表';
```

### 2.9 数据库表关系图

```
┌─────────────────┐
│ sys_organization│◄─────────────┐
└────────┬────────┘              │
         │                       │
         ▼                       │
┌─────────────────┐        ┌─────┴───────┐
│   sys_user_ext  │        │    User     │
└─────────────────┘        └─────┬───────┘
                                 │
                                 ▼
┌─────────────────┐        ┌─────────────┐
│sys_data_permission│◄─────│    Role     │
└─────────────────┘        └─────────────┘

┌─────────────────┐        ┌──────────────────┐
│md_field_standard│◄───────┤md_data_standard  │
└────────┬────────┘        └──────────────────┘
         │
         ▼
┌─────────────────┐        ┌──────────────────┐
│  md_form_field  │◄───────┤     md_form      │
└─────────────────┘        └────────┬─────────┘
                                    │
                                    ▼
┌──────────────────────┐    ┌──────────────────┐
│ md_master_data_type  │◄───┤md_master_data_   │
└──────────┬───────────┘    │    instance      │
           │                └──────────────────┘
           ▼
┌──────────────────────┐    ┌──────────────────┐
│md_lifecycle_state    │    │md_master_data_   │
└──────────────────────┘    │    relation      │
                            └──────────────────┘
```
