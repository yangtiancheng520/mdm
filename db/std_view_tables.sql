-- =============================================
-- 视图模型表结构
-- 创建时间：2026-06-06
-- =============================================

-- 1. 视图定义表
DROP TABLE IF EXISTS std_view;
CREATE TABLE std_view (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    view_code VARCHAR(100) NOT NULL COMMENT '视图编码',
    view_name VARCHAR(200) NOT NULL COMMENT '视图名称',
    category_id BIGINT COMMENT '分类ID（关联std_view_category）',

    -- 版本管理
    version INT DEFAULT 1 COMMENT '版本号',
    base_version_id BIGINT COMMENT '基础版本ID（关联上一版本）',
    is_latest TINYINT(1) DEFAULT 1 COMMENT '是否最新版本: 0-否 1-是',

    -- 布局配置
    layout_columns INT DEFAULT 2 COMMENT '默认列数',
    label_width INT DEFAULT 100 COMMENT '标签宽度',

    -- 功能开关
    enable_copy TINYINT(1) DEFAULT 1 COMMENT '启用复制: 0-否 1-是',
    enable_import TINYINT(1) DEFAULT 0 COMMENT '启用导入: 0-否 1-是',
    enable_export TINYINT(1) DEFAULT 1 COMMENT '启用导出: 0-否 1-是',

    -- 状态
    status VARCHAR(20) DEFAULT 'draft' COMMENT 'draft-草稿/published-已发布/archived-已归档',
    publish_time DATETIME COMMENT '发布时间',

    description TEXT COMMENT '描述',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_view_code (view_code),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_base_version_id (base_version_id),
    INDEX idx_is_latest (is_latest)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视图定义表';

-- 2. 实体定义表
DROP TABLE IF EXISTS std_view_entity;
CREATE TABLE std_view_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    view_id BIGINT NOT NULL COMMENT '视图ID',
    entity_code VARCHAR(100) NOT NULL COMMENT '实体编码',
    entity_name VARCHAR(200) NOT NULL COMMENT '实体名称',
    entity_type VARCHAR(20) NOT NULL COMMENT '实体类型: main-主实体/sub-子实体',

    -- 子实体配置
    sort INT DEFAULT 0 COMMENT '排序',
    min_rows INT DEFAULT 0 COMMENT '最小行数（子实体）',
    max_rows INT COMMENT '最大行数（子实体）',

    -- 功能配置
    enable_add TINYINT(1) DEFAULT 1 COMMENT '允许新增行: 0-否 1-是',
    enable_delete TINYINT(1) DEFAULT 1 COMMENT '允许删除行: 0-否 1-是',
    enable_copy TINYINT(1) DEFAULT 0 COMMENT '允许复制行: 0-否 1-是',
    enable_sort TINYINT(1) DEFAULT 0 COMMENT '允许排序: 0-否 1-是',

    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    description TEXT COMMENT '描述',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_view_entity (view_id, entity_code),
    INDEX idx_view_id (view_id),
    INDEX idx_entity_type (entity_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体定义表';

-- 3. 字段分组表
DROP TABLE IF EXISTS std_view_group;
CREATE TABLE std_view_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    group_code VARCHAR(100) NOT NULL COMMENT '分组编码',
    group_name VARCHAR(200) NOT NULL COMMENT '分组名称',
    sort INT DEFAULT 0 COMMENT '排序',

    -- 显示配置
    column_count INT DEFAULT 1 COMMENT '分组内列数',
    collapsible TINYINT(1) DEFAULT 1 COMMENT '可折叠: 0-否 1-是',
    default_collapsed TINYINT(1) DEFAULT 0 COMMENT '默认折叠: 0-展开 1-折叠',

    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_entity_group (entity_id, group_code),
    INDEX idx_entity_id (entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段分组表';

-- 4. 视图字段表
DROP TABLE IF EXISTS std_view_field;
CREATE TABLE std_view_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    field_code VARCHAR(100) NOT NULL COMMENT '字段编码',
    field_name VARCHAR(200) NOT NULL COMMENT '字段名称',

    -- 字段来源
    field_standard_id BIGINT COMMENT '字段标准ID（有值=引用字段标准，空=手工输入）',

    -- 基础属性（手工输入时必填，引用时自动带入）
    field_type VARCHAR(50) COMMENT '字段类型: text/number/boolean/date/datetime/select/ref/file',
    length INT COMMENT '长度',
    precision_val INT COMMENT '精度',

    -- 分组与排序
    group_id BIGINT COMMENT '分组ID',
    sort INT DEFAULT 0 COMMENT '排序',
    column_span INT DEFAULT 1 COMMENT '跨列数',

    -- 字段属性
    is_required TINYINT(1) DEFAULT 0 COMMENT '必填: 0-否 1-是',
    is_readonly TINYINT(1) DEFAULT 0 COMMENT '只读: 0-否 1-是',
    is_hidden TINYINT(1) DEFAULT 0 COMMENT '隐藏: 0-否 1-是',
    is_unique TINYINT(1) DEFAULT 0 COMMENT '唯一: 0-否 1-是',
    is_query TINYINT(1) DEFAULT 0 COMMENT '查询条件: 0-否 1-是',
    is_query_result TINYINT(1) DEFAULT 1 COMMENT '列表显示: 0-否 1-是',
    is_sortable TINYINT(1) DEFAULT 0 COMMENT '可排序: 0-否 1-是',

    -- 默认值
    default_value TEXT COMMENT '默认值',
    default_value_type VARCHAR(50) DEFAULT 'constant' COMMENT 'constant-常量/expression-表达式/function-函数',

    -- 参照配置（field_type='ref'时使用）
    ref_source VARCHAR(50) COMMENT '参照来源: value_domain/table/view/url',
    ref_id BIGINT COMMENT '参照ID',
    ref_filter TEXT COMMENT '参照过滤条件JSON',
    ref_cascade_field VARCHAR(100) COMMENT '级联父字段编码',

    -- 枚举配置（field_type='select'时使用）
    enum_code VARCHAR(100) COMMENT '枚举编码（关联值域）',

    -- 校验配置
    min_length INT COMMENT '最小长度',
    max_length INT COMMENT '最大长度',
    min_value DECIMAL(20,4) COMMENT '最小值',
    max_value DECIMAL(20,4) COMMENT '最大值',
    regex_pattern VARCHAR(500) COMMENT '正则校验',
    error_message VARCHAR(500) COMMENT '校验失败提示',

    -- 联动配置
    link_config TEXT COMMENT '联动配置JSON',

    -- 其他
    placeholder VARCHAR(200) COMMENT '占位提示',
    tooltip TEXT COMMENT '提示信息',

    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_entity_field (entity_id, field_code),
    INDEX idx_entity_id (entity_id),
    INDEX idx_group_id (group_id),
    INDEX idx_field_standard_id (field_standard_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视图字段表';

-- 5. 校验规则表
DROP TABLE IF EXISTS std_view_validation;
CREATE TABLE std_view_validation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    view_id BIGINT NOT NULL COMMENT '视图ID',
    rule_code VARCHAR(100) NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型: cross-跨字段/custom-自定义',

    -- 触发配置
    trigger_entity_id BIGINT COMMENT '触发实体ID',
    trigger_field_id BIGINT COMMENT '触发字段ID',
    trigger_condition TEXT COMMENT '触发条件表达式',

    -- 目标配置
    target_entity_id BIGINT COMMENT '目标实体ID',
    target_field_id BIGINT COMMENT '目标字段ID',
    action VARCHAR(50) COMMENT '动作: required/readonly/hidden/setvalue',
    action_value TEXT COMMENT '动作值',

    -- 提示
    error_message VARCHAR(500) COMMENT '错误提示',
    error_level VARCHAR(20) DEFAULT 'error' COMMENT 'error-错误/warning-警告',

    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_view_rule (view_id, rule_code),
    INDEX idx_view_id (view_id),
    INDEX idx_trigger_field (trigger_field_id),
    INDEX idx_target_field (target_field_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校验规则表';

-- =============================================
-- 表关系说明
-- =============================================
-- std_view (视图定义)
--     │
--     └── std_view_entity (实体定义)
--             │
--             ├── std_view_group (字段分组)
--             │       │
--             │       └── std_view_field (视图字段)
--             │               │
--             │               ├── field_standard_id → std_field_standard (字段标准库)
--             │               └── enum_code → std_value_domain (值域)
--             │
--             └── std_view_field (视图字段)
--
-- std_view_validation (校验规则) → std_view
