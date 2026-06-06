-- =============================================
-- MDM 数据库注释修复脚本
-- 修复表和字段注释的中文乱码问题
-- =============================================

USE mdm;

-- 1. 修复 sys_config 表
ALTER TABLE sys_config COMMENT '系统配置表';
ALTER TABLE sys_config
  MODIFY COLUMN config_key VARCHAR(100) NOT NULL COMMENT '配置键',
  MODIFY COLUMN config_value TEXT COMMENT '配置值',
  MODIFY COLUMN config_type VARCHAR(50) COMMENT '配置类型: string/number/json/boolean',
  MODIFY COLUMN config_group VARCHAR(50) COMMENT '配置分组',
  MODIFY COLUMN description VARCHAR(500) COMMENT '描述',
  MODIFY COLUMN is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密: 0-否 1-是',
  MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人',
  MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人',
  MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 2. 修复 sys_data_permission 表
ALTER TABLE sys_data_permission COMMENT '数据权限表';
ALTER TABLE sys_data_permission
  MODIFY COLUMN role_id BIGINT NOT NULL COMMENT '角色ID',
  MODIFY COLUMN data_type VARCHAR(50) COMMENT '数据类型: master_data_instance/user/organization',
  MODIFY COLUMN permission_type VARCHAR(50) NOT NULL COMMENT '权限类型: all-全部/org-本组织/suborg-本组织及下级/self-仅自己/custom-自定义',
  MODIFY COLUMN org_ids TEXT COMMENT '组织ID列表JSON: [1,2,3]',
  MODIFY COLUMN custom_rule TEXT COMMENT '自定义规则JSON: {conditions: [...]}',
  MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人',
  MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人',
  MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 3. 修复 sys_user_ext 表
ALTER TABLE sys_user_ext COMMENT '用户扩展表';
ALTER TABLE sys_user_ext
  MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '用户ID',
  MODIFY COLUMN org_id BIGINT COMMENT '组织ID',
  MODIFY COLUMN position_id BIGINT COMMENT '岗位ID',
  MODIFY COLUMN employee_no VARCHAR(50) COMMENT '工号',
  MODIFY COLUMN gender VARCHAR(10) COMMENT '性别: male/female',
  MODIFY COLUMN birthday DATE COMMENT '生日',
  MODIFY COLUMN entry_date DATE COMMENT '入职日期',
  MODIFY COLUMN leave_date DATE COMMENT '离职日期',
  MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 4. 修复 sys_rule_script 表
ALTER TABLE sys_rule_script COMMENT '规则脚本表';
ALTER TABLE sys_rule_script
  MODIFY COLUMN script_code VARCHAR(100) NOT NULL COMMENT '脚本编码',
  MODIFY COLUMN script_name VARCHAR(200) NOT NULL COMMENT '脚本名称',
  MODIFY COLUMN script_type VARCHAR(50) COMMENT '脚本类型: groovy/javascript',
  MODIFY COLUMN script_content TEXT COMMENT '脚本内容',
  MODIFY COLUMN input_params TEXT COMMENT '输入参数JSON: [{name, type, required}]',
  MODIFY COLUMN output_params TEXT COMMENT '输出参数JSON: [{name, type}]',
  MODIFY COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-启用/inactive-停用',
  MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人',
  MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人',
  MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  MODIFY COLUMN description TEXT COMMENT '描述';

-- 5. 修复 sys_schedule_task 表
ALTER TABLE sys_schedule_task COMMENT '定时任务表';
ALTER TABLE sys_schedule_task
  MODIFY COLUMN task_code VARCHAR(100) NOT NULL COMMENT '任务编码',
  MODIFY COLUMN task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
  MODIFY COLUMN task_type VARCHAR(50) COMMENT '任务类型: quality_check/distribution/data_sync/data_clean/custom',
  MODIFY COLUMN task_params TEXT COMMENT '任务参数JSON',
  MODIFY COLUMN cron_expression VARCHAR(100) COMMENT 'Cron表达式',
  MODIFY COLUMN task_class VARCHAR(500) COMMENT '任务执行类',
  MODIFY COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-启用/paused-暂停',
  MODIFY COLUMN last_execute_time DATETIME COMMENT '最后执行时间',
  MODIFY COLUMN next_execute_time DATETIME COMMENT '下次执行时间',
  MODIFY COLUMN execute_count INT DEFAULT 0 COMMENT '执行次数',
  MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人',
  MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人',
  MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  MODIFY COLUMN description TEXT COMMENT '描述';

-- 6. 修复 sys_schedule_task_log 表
ALTER TABLE sys_schedule_task_log COMMENT '定时任务执行日志表';
ALTER TABLE sys_schedule_task_log
  MODIFY COLUMN task_id BIGINT NOT NULL COMMENT '任务ID',
  MODIFY COLUMN task_code VARCHAR(100) COMMENT '任务编码',
  MODIFY COLUMN execute_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  MODIFY COLUMN execute_duration INT COMMENT '执行耗时(毫秒)',
  MODIFY COLUMN status VARCHAR(20) COMMENT '执行状态: success-成功/failed-失败',
  MODIFY COLUMN result_message TEXT COMMENT '执行结果信息',
  MODIFY COLUMN error_stack TEXT COMMENT '错误堆栈';

-- 7. 修复 sys_notification 表
ALTER TABLE sys_notification COMMENT '消息通知表';
ALTER TABLE sys_notification
  MODIFY COLUMN notification_code VARCHAR(100) NOT NULL COMMENT '通知编码',
  MODIFY COLUMN notification_type VARCHAR(50) NOT NULL COMMENT '通知类型: system-系统/task-任务/quality-质量/distribution-分发',
  MODIFY COLUMN title VARCHAR(200) NOT NULL COMMENT '标题',
  MODIFY COLUMN content TEXT COMMENT '内容',
  MODIFY COLUMN sender VARCHAR(50) COMMENT '发送人',
  MODIFY COLUMN receiver VARCHAR(50) NOT NULL COMMENT '接收人',
  MODIFY COLUMN is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  MODIFY COLUMN read_time DATETIME COMMENT '阅读时间',
  MODIFY COLUMN link_url VARCHAR(500) COMMENT '关联链接',
  MODIFY COLUMN link_params TEXT COMMENT '链接参数JSON',
  MODIFY COLUMN priority INT DEFAULT 0 COMMENT '优先级: 0-普通 1-重要 2-紧急',
  MODIFY COLUMN status VARCHAR(20) DEFAULT 'sent' COMMENT '状态: sent-已发送/read-已读/deleted-已删除',
  MODIFY COLUMN expire_time DATETIME COMMENT '过期时间',
  MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- 8. 修复 sys_integration_config 表
ALTER TABLE sys_integration_config COMMENT '外部集成配置表';
ALTER TABLE sys_integration_config
  MODIFY COLUMN integration_code VARCHAR(100) NOT NULL COMMENT '集成编码',
  MODIFY COLUMN integration_name VARCHAR(200) NOT NULL COMMENT '集成名称',
  MODIFY COLUMN integration_type VARCHAR(50) COMMENT '集成类型: tianyancha-天眼查/lbpm-流程引擎/data_platform-数据中台',
  MODIFY COLUMN api_endpoint VARCHAR(500) COMMENT 'API端点URL',
  MODIFY COLUMN auth_type VARCHAR(50) COMMENT '认证类型: none/basic/oauth2/apikey',
  MODIFY COLUMN auth_config TEXT COMMENT '认证配置JSON',
  MODIFY COLUMN request_config TEXT COMMENT '请求配置JSON: {timeout, retryCount}',
  MODIFY COLUMN mapping_config TEXT COMMENT '映射配置JSON',
  MODIFY COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-启用/inactive-停用',
  MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人',
  MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人',
  MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  MODIFY COLUMN description TEXT COMMENT '描述';

-- 9. 修复 sys_organization 表
ALTER TABLE sys_organization COMMENT '组织机构表';
ALTER TABLE sys_organization
  MODIFY COLUMN org_code VARCHAR(100) NOT NULL COMMENT '组织编码',
  MODIFY COLUMN org_name VARCHAR(200) NOT NULL COMMENT '组织名称',
  MODIFY COLUMN org_type VARCHAR(50) COMMENT '组织类型: company/department/group/position',
  MODIFY COLUMN parent_id BIGINT COMMENT '父级ID',
  MODIFY COLUMN level INT DEFAULT 1 COMMENT '层级',
  MODIFY COLUMN path VARCHAR(500) COMMENT '路径: /1/2/3',
  MODIFY COLUMN manager VARCHAR(50) COMMENT '负责人',
  MODIFY COLUMN phone VARCHAR(20) COMMENT '电话',
  MODIFY COLUMN email VARCHAR(100) COMMENT '邮箱',
  MODIFY COLUMN sort INT DEFAULT 0 COMMENT '排序',
  MODIFY COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-启用/inactive-停用',
  MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人',
  MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人',
  MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  MODIFY COLUMN description TEXT COMMENT '描述';

-- 10. 修复 users 表
ALTER TABLE users COMMENT '用户表';
ALTER TABLE users
  MODIFY COLUMN account VARCHAR(50) NOT NULL COMMENT '账号',
  MODIFY COLUMN password VARCHAR(100) NOT NULL COMMENT '密码',
  MODIFY COLUMN name VARCHAR(50) NOT NULL COMMENT '姓名',
  MODIFY COLUMN email VARCHAR(100) COMMENT '邮箱',
  MODIFY COLUMN phone VARCHAR(20) COMMENT '手机号',
  MODIFY COLUMN status VARCHAR(10) COMMENT '状态: active-启用/inactive-禁用',
  MODIFY COLUMN created_at DATETIME COMMENT '创建时间',
  MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- 11. 修复 roles 表
ALTER TABLE roles COMMENT '角色表';

-- 12. 修复 permissions 表
ALTER TABLE permissions COMMENT '权限表';

SELECT '数据库注释修复完成!' AS message;
