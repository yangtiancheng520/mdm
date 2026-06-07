-- =============================================
-- 主数据管理模块 - 数据表创建
-- =============================================

USE mdm;

-- 数据分类表
CREATE TABLE IF NOT EXISTS data_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    parent_id BIGINT COMMENT '父级ID，null为顶级',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    type VARCHAR(20) NOT NULL COMMENT '类型：folder(文件夹)/form(表单)',
    form_id BIGINT COMMENT '关联表单ID（type=form时有效）',
    icon VARCHAR(50) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_form_id (form_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分类表';

-- 数据实例表
CREATE TABLE IF NOT EXISTS data_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    form_id BIGINT NOT NULL COMMENT '表单ID',
    view_id BIGINT COMMENT '视图ID',
    data_json TEXT COMMENT '表单数据JSON',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active(生效)/obsolete(作废)',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_category_id (category_id),
    INDEX idx_form_id (form_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据实例表';
