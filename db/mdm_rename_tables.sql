-- =============================================
-- MDM 数据库表重命名脚本
-- 统一表前缀命名规范
-- 执行前请备份数据库！
-- =============================================

USE mdm;

-- =============================================
-- 1. 基础认证授权模块 (bas_)
-- =============================================

-- 1.1 users -> bas_user
RENAME TABLE users TO bas_user;
ALTER TABLE bas_user COMMENT '用户表';
ALTER TABLE bas_user
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN account VARCHAR(50) NOT NULL COMMENT '账号',
  MODIFY COLUMN password VARCHAR(100) NOT NULL COMMENT '密码',
  MODIFY COLUMN name VARCHAR(50) NOT NULL COMMENT '姓名',
  MODIFY COLUMN email VARCHAR(100) COMMENT '邮箱',
  MODIFY COLUMN phone VARCHAR(20) COMMENT '手机号',
  MODIFY COLUMN status VARCHAR(10) COMMENT '状态: active-启用/inactive-禁用',
  MODIFY COLUMN created_at DATETIME COMMENT '创建时间',
  MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- 1.2 roles -> bas_role
RENAME TABLE roles TO bas_role;
ALTER TABLE bas_role COMMENT '角色表';

-- 1.3 permissions -> bas_permission
RENAME TABLE permissions TO bas_permission;
ALTER TABLE bas_permission COMMENT '权限表';

-- 1.4 user_roles -> bas_user_role
RENAME TABLE user_roles TO bas_user_role;
ALTER TABLE bas_user_role COMMENT '用户角色关联表';
ALTER TABLE bas_user_role
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN user_id INT NOT NULL COMMENT '用户ID',
  MODIFY COLUMN role_id INT NOT NULL COMMENT '角色ID';

-- 1.5 role_permissions -> bas_role_permission
RENAME TABLE role_permissions TO bas_role_permission;
ALTER TABLE bas_role_permission COMMENT '角色权限关联表';
ALTER TABLE bas_role_permission
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN role_id INT NOT NULL COMMENT '角色ID',
  MODIFY COLUMN permission_id INT NOT NULL COMMENT '权限ID';

-- =============================================
-- 2. 数据标准与模型中心 (std_)
-- =============================================

-- 2.1 md_field_standard -> std_field
RENAME TABLE md_field_standard TO std_field;
ALTER TABLE std_field COMMENT '字段标准库表';

-- 2.2 md_data_standard -> std_data
RENAME TABLE md_data_standard TO std_data;
ALTER TABLE std_data COMMENT '数据标准视图表';

-- 2.3 md_encoding_rule -> std_encoding_rule
RENAME TABLE md_encoding_rule TO std_encoding_rule;
ALTER TABLE std_encoding_rule COMMENT '编码规则表';

-- 2.4 md_value_domain -> std_value_domain
RENAME TABLE md_value_domain TO std_value_domain;
ALTER TABLE std_value_domain COMMENT '值域表';

-- =============================================
-- 3. 表单与视图设计中心 (frm_)
-- =============================================

-- 3.1 md_form -> frm_form
RENAME TABLE md_form TO frm_form;
ALTER TABLE frm_form COMMENT '表单管理表';

-- 3.2 md_form_field -> frm_field
RENAME TABLE md_form_field TO frm_field;
ALTER TABLE frm_field COMMENT '表单字段配置表';

-- =============================================
-- 4. 流程与任务管理中心 (wfl_)
-- =============================================

-- 4.1 md_workflow_definition -> wfl_definition
RENAME TABLE md_workflow_definition TO wfl_definition;
ALTER TABLE wfl_definition COMMENT '流程定义表';

-- 4.2 md_workflow_instance -> wfl_instance
RENAME TABLE md_workflow_instance TO wfl_instance;
ALTER TABLE wfl_instance COMMENT '流程实例表';

-- 4.3 md_task -> wfl_task
RENAME TABLE md_task TO wfl_task;
ALTER TABLE wfl_task COMMENT '任务表';

-- =============================================
-- 5. 主数据生命周期管理 (mst_)
-- =============================================

-- 5.1 md_master_data_type -> mst_type
RENAME TABLE md_master_data_type TO mst_type;
ALTER TABLE mst_type COMMENT '主数据类型表';

-- 5.2 md_master_data_instance -> mst_instance
RENAME TABLE md_master_data_instance TO mst_instance;
ALTER TABLE mst_instance COMMENT '主数据实例表';

-- 5.3 md_lifecycle_state -> mst_lifecycle_state
RENAME TABLE md_lifecycle_state TO mst_lifecycle_state;
ALTER TABLE mst_lifecycle_state COMMENT '生命周期状态表';

-- =============================================
-- 6. 版本与审计中心 (ver_)
-- =============================================

-- 6.1 md_version_snapshot -> ver_snapshot
RENAME TABLE md_version_snapshot TO ver_snapshot;
ALTER TABLE ver_snapshot COMMENT '版本快照表';

-- 6.2 md_audit_log -> ver_audit_log
RENAME TABLE md_audit_log TO ver_audit_log;
ALTER TABLE ver_audit_log COMMENT '审计日志表';

-- =============================================
-- 系统基础配置模块 (sys_) 保持不变
-- sys_organization, sys_config, sys_data_permission 等
-- =============================================

SELECT '表重命名完成!' AS message;
