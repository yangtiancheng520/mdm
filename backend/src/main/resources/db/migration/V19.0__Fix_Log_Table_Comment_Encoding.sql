-- =============================================
-- 修复 log_md_operation 表字段注释乱码问题
-- =============================================

USE mdm;

-- 删除并重新创建表（保留数据）
-- 先备份表结构和数据
CREATE TABLE IF NOT EXISTS log_md_operation_backup LIKE log_md_operation;
INSERT INTO log_md_operation_backup SELECT * FROM log_md_operation;

-- 删除原表
DROP TABLE IF EXISTS log_md_operation;

-- 重新创建表（统一字符集和注释）
CREATE TABLE log_md_operation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    form_id BIGINT NOT NULL COMMENT '表单ID',
    view_id BIGINT COMMENT '视图ID',
    operation_type VARCHAR(50) COMMENT '操作类型',
    operation_detail VARCHAR(500) COMMENT '操作详情',
    from_status VARCHAR(50) COMMENT '原状态',
    to_status VARCHAR(50) COMMENT '目标状态',
    main_record_id BIGINT COMMENT '主表记录ID',
    operation_data TEXT COMMENT '操作数据快照JSON',
    quality_score DECIMAL(5,2) COMMENT '质检评分',
    quality_issues TEXT COMMENT '质检问题详情JSON',
    operation_reason TEXT COMMENT '操作原因',
    status VARCHAR(20) DEFAULT 'active' COMMENT '日志状态',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME COMMENT '更新时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    INDEX idx_category_id (category_id),
    INDEX idx_form_id (form_id),
    INDEX idx_main_record_id (main_record_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主数据操作日志表';

-- 恢复数据
INSERT INTO log_md_operation
SELECT * FROM log_md_operation_backup;

-- 删除备份表
DROP TABLE IF EXISTS log_md_operation_backup;

-- 验证结果
SELECT '=== 表结构修复完成 ===' AS Info;

SHOW FULL COLUMNS FROM log_md_operation;

SELECT '=== 数据验证 ===' AS Info;
SELECT COUNT(*) AS record_count FROM log_md_operation;

SELECT '=== 修复完成 ===' AS Info;
