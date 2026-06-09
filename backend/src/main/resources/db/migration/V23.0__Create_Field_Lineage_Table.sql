-- 字段级血缘日志表
CREATE TABLE IF NOT EXISTS dis_field_lineage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    -- 关联分发日志
    log_id BIGINT NOT NULL COMMENT '分发日志ID',

    -- 数据标识
    data_type VARCHAR(50) NOT NULL COMMENT '数据类型',
    data_id BIGINT NOT NULL COMMENT '数据ID',

    -- 字段映射信息
    mdm_field VARCHAR(100) NOT NULL COMMENT 'MDM字段编码',
    mdm_field_name VARCHAR(200) COMMENT 'MDM字段名称(发送方字段名)',
    sap_field VARCHAR(100) NOT NULL COMMENT '目标字段编码',
    sap_field_name VARCHAR(200) COMMENT '目标字段名称(消费方字段名)',

    -- 字段值
    source_value TEXT COMMENT '源字段值',
    target_value TEXT COMMENT '目标字段值(转换后)',

    -- 转换信息
    transform_type VARCHAR(20) COMMENT '转换类型',
    transform_rule TEXT COMMENT '转换规则',

    -- 状态
    status VARCHAR(20) NOT NULL COMMENT 'SUCCESS/FAILED/SKIPPED',
    error_msg VARCHAR(500) COMMENT '转换错误信息',

    -- 审计
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_log_id (log_id),
    INDEX idx_data (data_type, data_id),
    INDEX idx_mdm_field (mdm_field),
    INDEX idx_sap_field (sap_field)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段级血缘日志表';
