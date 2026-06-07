-- 创建数据标准视图表
CREATE TABLE IF NOT EXISTS std_data_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    standard_code VARCHAR(100) NOT NULL COMMENT '标准编码',
    standard_name VARCHAR(200) NOT NULL COMMENT '标准名称',
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
    UNIQUE INDEX idx_standard_code (standard_code),
    INDEX idx_status (status),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据标准视图表';
