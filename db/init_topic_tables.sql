-- =============================================
-- 分发主题和订阅表结构
-- =============================================

USE mdm;

-- 1. 分发主题表
CREATE TABLE IF NOT EXISTS dis_topic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    topic_code VARCHAR(50) NOT NULL COMMENT '主题编码',
    topic_name VARCHAR(100) NOT NULL COMMENT '主题名称',
    data_type VARCHAR(50) NOT NULL COMMENT '数据类型: VENDOR/MATERIAL/CUSTOMER',
    data_type_name VARCHAR(100) COMMENT '数据类型名称',
    distribute_mode VARCHAR(20) DEFAULT 'MANUAL' COMMENT '分发模式: MANUAL-手动/REALTIME-实时/SCHEDULE-定时',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式(定时模式)',
    batch_size INT DEFAULT 100 COMMENT '批量大小',
    retry_count INT DEFAULT 3 COMMENT '重试次数',
    retry_interval INT DEFAULT 5000 COMMENT '重试间隔(毫秒)',
    filter_condition TEXT COMMENT '数据过滤条件JSON',
    status VARCHAR(20) DEFAULT 'inactive' COMMENT 'active-启用/inactive-停用',
    description VARCHAR(500) COMMENT '描述',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_topic_code (topic_code),
    INDEX idx_data_type (data_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分发主题表';

-- 2. 分发订阅表
CREATE TABLE IF NOT EXISTS dis_subscription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    topic_id BIGINT NOT NULL COMMENT '主题ID',
    topic_name VARCHAR(100) COMMENT '主题名称',
    system_config_id BIGINT NOT NULL COMMENT '系统配置ID',
    system_config_name VARCHAR(100) COMMENT '系统配置名称',
    system_type VARCHAR(20) COMMENT '系统类型: SAP/HTTP/DATABASE',
    mapping_id BIGINT COMMENT '字段映射配置ID',
    mapping_name VARCHAR(100) COMMENT '字段映射名称',
    priority INT DEFAULT 0 COMMENT '分发优先级，数字越小优先级越高',
    sync_mode VARCHAR(20) DEFAULT 'SYNC' COMMENT 'SYNC-同步/ASYNC-异步',
    enable_create TINYINT DEFAULT 1 COMMENT '是否分发新增数据',
    enable_update TINYINT DEFAULT 1 COMMENT '是否分发更新数据',
    enable_delete TINYINT DEFAULT 0 COMMENT '是否分发删除数据',
    filter_rule TEXT COMMENT '订阅过滤规则JSON',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    last_sync_time DATETIME COMMENT '最后同步时间',
    last_sync_status VARCHAR(20) COMMENT '最后同步状态',
    last_sync_count INT COMMENT '最后同步数量',
    total_sync_count INT DEFAULT 0 COMMENT '累计同步次数',
    total_success_count INT DEFAULT 0 COMMENT '累计成功次数',
    total_fail_count INT DEFAULT 0 COMMENT '累计失败次数',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_topic_id (topic_id),
    INDEX idx_system_config_id (system_config_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分发订阅表';

-- 3. 初始化示例数据
INSERT IGNORE INTO dis_topic (topic_code, topic_name, data_type, data_type_name, distribute_mode, status, description, created_by) VALUES
('VENDOR_SAP', '供应商分发到SAP', 'VENDOR', '供应商', 'MANUAL', 'inactive', '将供应商主数据分发到SAP系统', 'system'),
('MATERIAL_MES', '物料分发到MES', 'MATERIAL', '物料', 'MANUAL', 'inactive', '将物料主数据分发到MES系统', 'system');

SELECT '分发主题表创建完成' AS message;
