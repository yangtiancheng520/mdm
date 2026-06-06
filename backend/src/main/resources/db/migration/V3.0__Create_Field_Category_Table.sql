-- 创建字段分类表
CREATE TABLE std_field_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT COMMENT '父级分类ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',

    UNIQUE KEY uk_category_code (category_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段分类表';

-- 修改字段标准库表，添加category_id字段
ALTER TABLE std_field_standard
ADD COLUMN category_id BIGINT COMMENT '分类ID' AFTER reference_source;

-- 添加索引
ALTER TABLE std_field_standard
ADD INDEX idx_category_id (category_id);

-- 迁移现有数据：将category字段的值迁移到category_id（需要先创建分类）
-- 这里先创建默认分类
INSERT INTO std_field_category (category_code, category_name, parent_id, sort, status, description)
SELECT DISTINCT
    category AS category_code,
    category AS category_name,
    NULL AS parent_id,
    0 AS sort,
    'active' AS status,
    CONCAT('从字段标准库迁移的分类：', category) AS description
FROM std_field_standard
WHERE category IS NOT NULL AND category != ''
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);

-- 更新字段标准库的category_id
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fs.category = fc.category_code
SET fs.category_id = fc.id
WHERE fs.category IS NOT NULL AND fs.category != '';

-- 初始化示例分类数据（如果表为空）
INSERT INTO std_field_category (category_code, category_name, parent_id, sort, status, description) VALUES
('BASIC', '基础信息', NULL, 1, 'active', '基础标识字段，如编码、名称等'),
('CONTACT', '联系信息', NULL, 2, 'active', '联系方式相关字段'),
('FINANCIAL', '财务信息', NULL, 3, 'active', '财务相关字段'),
('ADMIN', '管理属性', NULL, 4, 'active', '管理相关字段'),
('IDENTITY', '标识类', 1, 1, 'active', '编码、ID等标识字段'),
('NAME', '名称类', 1, 2, 'active', '各类名称字段')
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);
