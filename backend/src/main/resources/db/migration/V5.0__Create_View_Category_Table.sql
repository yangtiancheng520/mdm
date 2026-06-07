-- 创建视图分类表
CREATE TABLE IF NOT EXISTS std_view_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    category_code VARCHAR(100) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(200) NOT NULL COMMENT '分类名称',
    parent_id BIGINT COMMENT '父级ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    description TEXT COMMENT '描述',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_category_code (category_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视图分类表';

-- 插入默认分类
INSERT INTO std_view_category (category_code, category_name, parent_id, sort, status, description) VALUES
('MASTER_DATA', '主数据标准', NULL, 1, 'active', '主数据相关标准'),
('REFERENCE_DATA', '引用数据标准', NULL, 2, 'active', '引用数据相关标准'),
('BUSINESS_DATA', '业务数据标准', NULL, 3, 'active', '业务数据相关标准');
