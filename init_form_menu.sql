-- =============================================
-- 补充表单管理菜单和表结构
-- =============================================

USE mdm;

-- 1. 确保表单与视图设计中心一级菜单存在
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(200, '表单与视图设计中心', 'form', 'menu', NULL, '/form', 'Edit', 20, 'active', NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), path = VALUES(path);

-- 2. 确保表单管理二级菜单存在
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(201, '表单管理', 'form:manage', 'menu', 200, '/form/manage', 'Files', 1, 'active', NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), path = VALUES(path);

-- 3. 给超级管理员角色分配表单管理权限
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES (1, 200), (1, 201);

-- 4. 创建表单管理表（如果不存在）
CREATE TABLE IF NOT EXISTS frm_form (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    form_code VARCHAR(100) NOT NULL COMMENT '表单编码',
    form_name VARCHAR(200) NOT NULL COMMENT '表单名称',
    form_type VARCHAR(50) NOT NULL COMMENT '表单类型: create/edit/view/search',
    view_id BIGINT COMMENT '关联视图ID',
    design_mode VARCHAR(20) DEFAULT 'auto' COMMENT 'auto-自动生成/blank-空白创建',
    style_template VARCHAR(50) COMMENT '样式模板',
    layout_config TEXT COMMENT '布局配置JSON',
    enable_copy TINYINT DEFAULT 0,
    enable_import TINYINT DEFAULT 0,
    enable_export TINYINT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'draft',
    is_default TINYINT DEFAULT 0,
    version INT DEFAULT 1,
    created_by VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    description TEXT,
    UNIQUE KEY uk_form_code (form_code),
    INDEX idx_view_id (view_id),
    INDEX idx_form_type (form_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单管理表';

CREATE TABLE IF NOT EXISTS frm_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    form_id BIGINT NOT NULL,
    group_code VARCHAR(100) NOT NULL,
    group_name VARCHAR(200) NOT NULL,
    group_type VARCHAR(20) DEFAULT 'group',
    sort INT DEFAULT 0,
    is_collapsed TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_form_id (form_id),
    UNIQUE KEY uk_form_group_code (form_id, group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单分组表';

CREATE TABLE IF NOT EXISTS frm_component (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    form_id BIGINT NOT NULL,
    view_field_id BIGINT,
    field_code VARCHAR(100) NOT NULL,
    field_name VARCHAR(200) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    domain_id BIGINT,
    group_id BIGINT,
    row_index INT NOT NULL DEFAULT 0,
    col_index INT NOT NULL DEFAULT 0,
    col_span INT DEFAULT 1,
    row_span INT DEFAULT 1,
    is_required TINYINT DEFAULT 0,
    is_readonly TINYINT DEFAULT 0,
    is_hidden TINYINT DEFAULT 0,
    default_value TEXT,
    placeholder VARCHAR(200),
    validation_rules TEXT,
    label_width INT,
    component_width VARCHAR(50),
    sort INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_form_id (form_id),
    INDEX idx_view_field_id (view_field_id),
    INDEX idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单组件表';

-- 完成
SELECT '表单管理菜单和表结构初始化完成!' AS message;
