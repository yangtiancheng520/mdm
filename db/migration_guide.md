# MDM 数据库表重命名迁移指南

## 📋 迁移概述

本次迁移将所有数据库表名从旧的命名规范迁移到新的模块化命名规范。

### 命名规范说明

| 前缀 | 模块 | 说明 |
|-----|------|------|
| **bas_** | 基础认证授权模块 | Base Authentication System |
| **sys_** | 系统基础配置模块 | System Configuration |
| **std_** | 数据标准与模型中心 | Standard Data Model |
| **frm_** | 表单与视图设计中心 | Form Design Center |
| **wfl_** | 流程与任务管理中心 | Workflow Management |
| **mst_** | 主数据生命周期管理 | Master Data Lifecycle |
| **ver_** | 版本与审计中心 | Version & Audit |

## 🔄 表名映射关系

### 1. 基础认证授权模块 (bas_)

| 旧表名 | 新表名 | 说明 |
|--------|--------|------|
| users | bas_user | 用户表 |
| roles | bas_role | 角色表 |
| permissions | bas_permission | 权限表 |
| user_roles | bas_user_role | 用户角色关联表 |
| role_permissions | bas_role_permission | 角色权限关联表 |

### 2. 数据标准与模型中心 (std_)

| 旧表名 | 新表名 | 说明 |
|--------|--------|------|
| md_field_standard | std_field | 字段标准库表 |
| md_data_standard | std_data | 数据标准视图表 |
| md_encoding_rule | std_encoding_rule | 编码规则表 |
| md_value_domain | std_value_domain | 值域表 |

### 3. 表单与视图设计中心 (frm_)

| 旧表名 | 新表名 | 说明 |
|--------|--------|------|
| md_form | frm_form | 表单管理表 |
| md_form_field | frm_field | 表单字段配置表 |

### 4. 流程与任务管理中心 (wfl_)

| 旧表名 | 新表名 | 说明 |
|--------|--------|------|
| md_workflow_definition | wfl_definition | 流程定义表 |
| md_workflow_instance | wfl_instance | 流程实例表 |
| md_task | wfl_task | 任务表 |

### 5. 主数据生命周期管理 (mst_)

| 旧表名 | 新表名 | 说明 |
|--------|--------|------|
| md_master_data_type | mst_type | 主数据类型表 |
| md_master_data_instance | mst_instance | 主数据实例表 |
| md_lifecycle_state | mst_lifecycle_state | 生命周期状态表 |

### 6. 版本与审计中心 (ver_)

| 旧表名 | 新表名 | 说明 |
|--------|--------|------|
| md_version_snapshot | ver_snapshot | 版本快照表 |
| md_audit_log | ver_audit_log | 审计日志表 |

## 📝 迁移步骤

### 步骤 1: 备份数据库

```bash
# 使用 mysqldump 备份整个数据库
mysqldump -u root -p mdm > mdm_backup_$(date +%Y%m%d_%H%M%S).sql

# 或者在 MySQL 中备份
mysql -u root -p
mysql> CREATE DATABASE mdm_backup AS mdm;
```

### 步骤 2: 执行表重命名脚本

```bash
# 连接到 MySQL
mysql -u root -p mdm < db/mdm_rename_tables.sql
```

### 步骤 3: 验证迁移结果

```bash
# 执行验证脚本
mysql -u root -p mdm < db/migration_verify.sql
```

### 步骤 4: 重启应用服务

```bash
# 重启后端服务
./stop-all.bat
./start-backend.bat

# 或使用启动脚本
./start-all.bat
```

## ✅ 验证清单

- [ ] 数据库备份完成
- [ ] 表重命名脚本执行成功
- [ ] 所有表名已更新
- [ ] 应用服务启动正常
- [ ] 用户登录功能正常
- [ ] 主数据管理功能正常
- [ ] 流程审批功能正常
- [ ] 数据查询功能正常

## ⚠️ 注意事项

1. **执行前必须备份数据库**
2. 建议在测试环境先执行迁移
3. 迁移期间停止应用服务
4. 迁移后需要重启应用服务
5. Entity 类已更新，无需修改代码
6. 外键约束会自动更新

## 🔧 回滚方案

如果迁移出现问题，可以使用以下方式回滚：

```sql
-- 方式1: 从备份恢复
DROP DATABASE mdm;
CREATE DATABASE mdm;
USE mdm;
SOURCE mdm_backup_YYYYMMDD_HHMMSS.sql;

-- 方式2: 执行反向重命名（需要手动编写）
```

## 📊 迁移影响范围

### 已更新的文件

#### 后端代码
- ✅ `backend/src/main/java/com/vueadmin/entity/User.java`
- ✅ `backend/src/main/java/com/vueadmin/entity/Role.java`
- ✅ `backend/src/main/java/com/vueadmin/entity/Permission.java`
- ✅ `backend/src/main/java/com/vueadmin/entity/UserRole.java`
- ✅ `backend/src/main/java/com/vueadmin/entity/RolePermission.java`
- ✅ `backend/src/main/java/com/vueadmin/entity/system/*.java`

#### 数据库脚本
- ✅ `db/mdm_schema.sql`
- ✅ `db/mdm_data.sql`
- ✅ `db/mdm_fix_all_comments.sql`
- ✅ `db/mdm_rename_tables.sql`

### 无需修改的文件

- ✅ Repository 接口（使用 JPA 自动映射）
- ✅ Service 层（无硬编码表名）
- ✅ Controller 层（无硬编码表名）

## 📞 技术支持

如有问题，请联系技术团队或查看以下文档：
- 实现计划文档：`docs/mdm-implementation-plan-part1.md`
- Entity 验证报告：`entity_verification_report.md`
