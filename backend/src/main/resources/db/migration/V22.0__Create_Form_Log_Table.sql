-- =============================================
-- 表单操作日志表
-- =============================================

DROP TABLE IF EXISTS log_form;
CREATE TABLE log_form (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    form_id BIGINT NOT NULL COMMENT '表单ID',

    -- 操作信息
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型: create/update/delete/publish/unpublish/save_design',
    operation_detail VARCHAR(500) COMMENT '操作详情',

    -- 版本信息
    from_version INT COMMENT '原版本号',
    to_version INT COMMENT '新版本号',

    -- 状态变更
    from_status VARCHAR(20) COMMENT '原状态',
    to_status VARCHAR(20) COMMENT '新状态',

    -- 变更数据快照
    change_data TEXT COMMENT '变更数据JSON（记录修改的字段）',

    -- 审计信息
    created_by VARCHAR(50) COMMENT '操作人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',

    INDEX idx_form_id (form_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单操作日志表';
