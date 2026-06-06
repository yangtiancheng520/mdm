-- =============================================
-- MDM 系统基础配置模块补充表结构
-- 版本: v1.1
-- 日期: 2026-06-06
-- =============================================

USE mdm;

-- 6.5 数据权限表
DROP TABLE IF EXISTS sys_data_permission;
CREATE TABLE sys_data_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    data_type VARCHAR(50) COMMENT '数据类型: master_data_instance/user/organization',
    permission_type VARCHAR(50) NOT NULL COMMENT '权限类型: all-全部/org-本组织/suborg-本组织及下级/self-仅自己/custom-自定义',
    org_ids TEXT COMMENT '组织ID列表JSON: [1,2,3]',
    custom_rule TEXT COMMENT '自定义规则JSON: {conditions: [...]}',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_id (role_id),
    INDEX idx_data_type (data_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据权限表';

-- 6.6 用户扩展表
DROP TABLE IF EXISTS sys_user_ext;
CREATE TABLE sys_user_ext (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    org_id BIGINT COMMENT '组织ID',
    position_id BIGINT COMMENT '岗位ID',
    employee_no VARCHAR(50) COMMENT '工号',
    gender VARCHAR(10) COMMENT '性别: male/female',
    birthday DATE COMMENT '生日',
    entry_date DATE COMMENT '入职日期',
    leave_date DATE COMMENT '离职日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_org_id (org_id),
    INDEX idx_employee_no (employee_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户扩展表';

-- 6.7 外部集成配置表
DROP TABLE IF EXISTS sys_integration_config;
CREATE TABLE sys_integration_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    integration_code VARCHAR(100) NOT NULL COMMENT '集成编码',
    integration_name VARCHAR(200) NOT NULL COMMENT '集成名称',
    integration_type VARCHAR(50) COMMENT '集成类型: tianyancha-天眼查/lbpm-流程引擎/data_platform-数据中台',
    api_endpoint VARCHAR(500) COMMENT 'API端点URL',
    auth_type VARCHAR(50) COMMENT '认证类型: none/basic/oauth2/apikey',
    auth_config TEXT COMMENT '认证配置JSON: {apiKey, secret, tokenUrl, ...}',
    request_config TEXT COMMENT '请求配置JSON: {timeout, retryCount, retryInterval}',
    mapping_config TEXT COMMENT '映射配置JSON: {fieldMappings: [...]}',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    UNIQUE KEY uk_integration_code (integration_code),
    INDEX idx_integration_type (integration_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部集成配置表';

-- 6.8 消息通知表
DROP TABLE IF EXISTS sys_notification;
CREATE TABLE sys_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    notification_code VARCHAR(100) NOT NULL COMMENT '通知编码',
    notification_type VARCHAR(50) NOT NULL COMMENT '通知类型: system-系统/task-任务/quality-质量/distribution-分发',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    sender VARCHAR(50) COMMENT '发送人',
    receiver VARCHAR(50) NOT NULL COMMENT '接收人',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    read_time DATETIME COMMENT '阅读时间',
    link_url VARCHAR(500) COMMENT '关联链接',
    link_params TEXT COMMENT '链接参数JSON',
    priority INT DEFAULT 0 COMMENT '优先级: 0-普通 1-重要 2-紧急',
    status VARCHAR(20) DEFAULT 'sent' COMMENT 'sent-已发送/read-已读/deleted-已删除',
    expire_time DATETIME COMMENT '过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_notification_code (notification_code),
    INDEX idx_receiver (receiver),
    INDEX idx_is_read (is_read),
    INDEX idx_notification_type (notification_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 6.9 定时任务执行日志表
DROP TABLE IF EXISTS sys_schedule_task_log;
CREATE TABLE sys_schedule_task_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    task_code VARCHAR(100) COMMENT '任务编码',
    execute_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    execute_duration INT COMMENT '执行耗时(毫秒)',
    status VARCHAR(20) COMMENT '执行状态: success-成功/failed-失败',
    result_message TEXT COMMENT '执行结果信息',
    error_stack TEXT COMMENT '错误堆栈',
    INDEX idx_task_id (task_id),
    INDEX idx_execute_time (execute_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行日志表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始化系统配置（先删除再插入）
DELETE FROM sys_config WHERE config_key IN ('system.name', 'system.version', 'data.permission.enabled', 'notification.retention.days', 'task.log.retention.days');
INSERT INTO sys_config (config_key, config_value, config_type, config_group, description) VALUES
('system.name', 'MDM主数据管理平台', 'string', 'system', '系统名称'),
('system.version', '1.0.0', 'string', 'system', '系统版本'),
('data.permission.enabled', 'true', 'boolean', 'permission', '是否启用数据权限'),
('notification.retention.days', '30', 'number', 'notification', '消息保留天数'),
('task.log.retention.days', '90', 'number', 'task', '任务日志保留天数');

-- 初始化数据权限（为管理员角色配置全部权限）
INSERT INTO sys_data_permission (role_id, data_type, permission_type, created_by) VALUES
(1, 'master_data_instance', 'all', 'system'),
(1, 'user', 'all', 'system'),
(1, 'organization', 'all', 'system');

SELECT '系统基础配置模块表结构补充完成!' AS message;
