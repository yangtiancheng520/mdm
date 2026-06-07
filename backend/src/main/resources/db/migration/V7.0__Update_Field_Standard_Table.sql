-- =============================================
-- MDM 数据字典表结构更新脚本
-- 版本: v7.0
-- 日期: 2026-06-07
-- 说明: 添加 is_enum 字段，优化字段标准表结构
-- =============================================

-- 字段标准表结构更新
DROP TABLE IF EXISTS std_field_standard;
CREATE TABLE std_field_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    field_code VARCHAR(100) NOT NULL COMMENT '字段编码',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    field_type VARCHAR(50) NOT NULL COMMENT '字段类型: string/integer/decimal/boolean/date/datetime/time/text',
    length INT COMMENT '字段长度（string/decimal类型）',
    precision_val INT COMMENT '小数精度（decimal类型）',
    default_value VARCHAR(500) COMMENT '默认值',
    is_enum TINYINT DEFAULT 0 COMMENT '是否关联值域: 0-否 1-是',
    domain_id BIGINT COMMENT '值域ID（关联std_value_domain）',
    category_id BIGINT COMMENT '分类ID（关联std_field_category）',
    category VARCHAR(50) COMMENT '分类名称（冗余字段）',
    status VARCHAR(20) DEFAULT '启用' COMMENT '状态: 启用/停用',
    version INT DEFAULT 1 COMMENT '版本号',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_field_code (field_code),
    INDEX idx_field_type (field_type),
    INDEX idx_category_id (category_id),
    INDEX idx_domain_id (domain_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段标准表';

-- 值域表结构
DROP TABLE IF EXISTS std_value_domain;
CREATE TABLE std_value_domain (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    domain_code VARCHAR(100) NOT NULL COMMENT '值域编码',
    domain_name VARCHAR(200) NOT NULL COMMENT '值域名称',
    data_type VARCHAR(20) DEFAULT 'string' COMMENT '数据类型: string/integer/decimal/boolean',
    data_length INT COMMENT '长度（string类型）',
    options TEXT COMMENT '选项列表JSON: [{value, label, sort}]',
    status VARCHAR(20) DEFAULT '启用' COMMENT '状态: 启用/停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_domain_code (domain_code),
    INDEX idx_data_type (data_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值域表';

-- 值域项表（可选，用于大数据量值域）
DROP TABLE IF EXISTS std_value_domain_item;
CREATE TABLE std_value_domain_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    domain_id BIGINT NOT NULL COMMENT '值域ID',
    item_value VARCHAR(200) COMMENT '值',
    item_label VARCHAR(200) COMMENT '显示名',
    parent_item_id BIGINT COMMENT '父级项ID（树形结构）',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT '启用' COMMENT '状态: 启用/停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    INDEX idx_domain_id (domain_id),
    INDEX idx_parent_item_id (parent_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值域项表';

-- 字段分类表
DROP TABLE IF EXISTS std_field_category;
CREATE TABLE std_field_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    category_code VARCHAR(100) COMMENT '分类编码',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT COMMENT '父级ID',
    level INT DEFAULT 1 COMMENT '层级',
    path VARCHAR(500) COMMENT '路径',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT '启用' COMMENT '状态: 启用/停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_category_code (category_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段分类表';

-- =============================================
-- 完成
-- =============================================
SELECT 'MDM 数据字典表结构更新完成!' AS message;
