# Entity 类 @Table 注解验证报告

生成时间: $(date '+%Y-%m-%d %H:%M:%S')

## 验证结果摘要

✅ **所有 Entity 类的 @Table 注解已正确更新！**

## 详细验证列表

### 1. 基础认证授权模块 (bas_ 前缀)

| Entity 类 | 旧表名 | 新表名 | 状态 |
|----------|--------|--------|------|
| User.java | users | bas_user | ✅ 已更新 |
| Role.java | roles | bas_role | ✅ 已更新 |
| Permission.java | permissions | bas_permission | ✅ 已更新 |
| UserRole.java | user_roles | bas_user_role | ✅ 已更新 |
| RolePermission.java | role_permissions | bas_role_permission | ✅ 已更新 |

### 2. 系统基础配置模块 (sys_ 前缀)

| Entity 类 | 表名 | 状态 |
|----------|------|------|
| Organization.java | sys_organization | ✅ 正确 |
| DataPermission.java | sys_data_permission | ✅ 正确 |
| RuleScript.java | sys_rule_script | ✅ 正确 |
| ScheduleTask.java | sys_schedule_task | ✅ 正确 |
| SystemConfig.java | sys_config | ✅ 正确 |
| IntegrationConfig.java | sys_integration_config | ✅ 正确 |
| Notification.java | sys_notification | ✅ 正确 |

## 验证项

- [x] 所有 Entity 类的 @Table 注解已正确设置
- [x] 未发现硬编码的旧表名
- [x] 表名命名规范符合设计文档要求

## 命名规范说明

- **bas_**: 基础认证授权模块 (Base Authentication)
- **sys_**: 系统基础配置模块 (System Configuration)
- **std_**: 数据标准与模型中心 (Standard)
- **frm_**: 表单与视图设计中心 (Form)
- **wfl_**: 流程与任务管理中心 (Workflow)
- **mst_**: 主数据生命周期管理 (Master Data)
- **ver_**: 版本与审计中心 (Version & Audit)

## 后续建议

1. 运行数据库重命名脚本: `db/mdm_rename_tables.sql`
2. 重启应用服务以确保 Hibernate 正确映射表名
3. 执行集成测试验证所有数据访问正常

