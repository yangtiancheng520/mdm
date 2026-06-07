-- =============================================
-- 表单管理模块表结构
-- =============================================

-- 1. 表单管理表
DROP TABLE IF EXISTS frm_form;
CREATE TABLE frm_form (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    form_code VARCHAR(100) NOT NULL COMMENT '表单编码',
    form_name VARCHAR(200) NOT NULL COMMENT '表单名称',
    form_type VARCHAR(50) NOT NULL COMMENT '表单类型: create/edit/view/search',
    view_id BIGINT COMMENT '关联视图ID（空白表单可为空）',

    -- 设计模式
    design_mode VARCHAR(20) DEFAULT 'auto' COMMENT 'auto-自动生成/blank-空白创建',
    style_template VARCHAR(50) COMMENT '样式模板: single/dual/triple/master-detail/grouped/tabs',

    -- 布局配置
    layout_config TEXT COMMENT '布局配置JSON',

    -- 功能开关
    enable_copy TINYINT DEFAULT 0 COMMENT '启用复制: 0-否 1-是',
    enable_import TINYINT DEFAULT 0 COMMENT '启用导入: 0-否 1-是',
    enable_export TINYINT DEFAULT 0 COMMENT '启用导出: 0-否 1-是',

    -- 状态
    status VARCHAR(20) DEFAULT 'draft' COMMENT 'draft-草稿/published-已发布',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认表单: 0-否 1-是',
    version INT DEFAULT 1 COMMENT '版本号',

    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',

    UNIQUE KEY uk_form_code (form_code),
    INDEX idx_view_id (view_id),
    INDEX idx_form_type (form_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单管理表';

-- 2. 表单分组表
DROP TABLE IF EXISTS frm_group;
CREATE TABLE frm_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    form_id BIGINT NOT NULL COMMENT '表单ID',
    group_code VARCHAR(100) NOT NULL COMMENT '分组编码',
    group_name VARCHAR(200) NOT NULL COMMENT '分组名称',
    group_type VARCHAR(20) DEFAULT 'group' COMMENT 'group-分组/tab-标签页/table-子表',
    sort INT DEFAULT 0 COMMENT '排序',
    is_collapsed TINYINT DEFAULT 0 COMMENT '是否折叠: 0-否 1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_form_id (form_id),
    UNIQUE KEY uk_form_group_code (form_id, group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单分组表';

-- 3. 表单组件表
DROP TABLE IF EXISTS frm_component;
CREATE TABLE frm_component (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    form_id BIGINT NOT NULL COMMENT '表单ID',

    -- 组件来源（必须来自数据模型）
    view_field_id BIGINT COMMENT '关联视图字段ID',
    field_code VARCHAR(100) NOT NULL COMMENT '字段编码',
    field_name VARCHAR(200) NOT NULL COMMENT '字段名称',
    field_type VARCHAR(50) NOT NULL COMMENT '字段类型',
    domain_id BIGINT COMMENT '值域ID',

    -- 布局位置
    group_id BIGINT COMMENT '分组ID',
    row_index INT NOT NULL DEFAULT 0 COMMENT '行位置',
    col_index INT NOT NULL DEFAULT 0 COMMENT '列位置',
    col_span INT DEFAULT 1 COMMENT '跨列数',
    row_span INT DEFAULT 1 COMMENT '跨行数',

    -- 字段属性（可覆盖视图字段配置）
    is_required TINYINT DEFAULT 0 COMMENT '是否必填: 0-否 1-是',
    is_readonly TINYINT DEFAULT 0 COMMENT '是否只读: 0-否 1-是',
    is_hidden TINYINT DEFAULT 0 COMMENT '是否隐藏: 0-否 1-是',
    default_value TEXT COMMENT '默认值',
    placeholder VARCHAR(200) COMMENT '占位符',

    -- 校验配置
    validation_rules TEXT COMMENT '校验规则JSON',

    -- 样式配置
    label_width INT COMMENT '标签宽度',
    component_width VARCHAR(50) COMMENT '组件宽度: 100%/50%/auto',

    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_form_id (form_id),
    INDEX idx_view_field_id (view_field_id),
    INDEX idx_group_id (group_id),
    INDEX idx_position (form_id, row_index, col_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单组件表';
