-- =============================================
-- MDM 主数据管理平台 - 菜单权限初始化脚本
-- 版本: v1.0
-- 日期: 2026-06-05
-- =============================================

USE mdm;

-- 清空现有权限数据（保留基础权限）
DELETE FROM role_permissions;
DELETE FROM permissions WHERE id > 8;

-- =============================================
-- 一级菜单：8大模块
-- =============================================

-- 1. 数据标准与模型中心
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(100, '数据标准与模型中心', 'standard', 'menu', NULL, '/standard', 'Document', 10, 'active', NOW());

-- 2. 表单与视图设计中心
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(200, '表单与视图设计中心', 'form', 'menu', NULL, '/form', 'Edit', 20, 'active', NOW());

-- 3. 流程与任务管理中心
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(300, '流程与任务管理中心', 'workflow', 'menu', NULL, '/workflow', 'Connection', 30, 'active', NOW());

-- 4. 主数据管理
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(350, '主数据管理', 'data', 'menu', NULL, '/data', 'DataAnalysis', 35, 'active', NOW());

-- 5. 主数据生命周期管理
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(400, '主数据生命周期管理', 'masterdata', 'menu', NULL, '/masterdata', 'DataLine', 40, 'active', NOW());

-- 5. 数据质量管理
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(500, '数据质量管理', 'quality', 'menu', NULL, '/quality', 'CircleCheck', 50, 'active', NOW());

-- 6. 主数据分发管理中心
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(600, '主数据分发管理中心', 'distribution', 'menu', NULL, '/distribution', 'Share', 60, 'active', NOW());

-- 7. 版本与审计中心
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(700, '版本与审计中心', 'version', 'menu', NULL, '/version', 'Clock', 70, 'active', NOW());

-- 8. 系统基础配置中心
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(800, '系统基础配置中心', 'system', 'menu', NULL, '/system', 'Setting', 80, 'active', NOW());

-- =============================================
-- 二级菜单：各模块子功能
-- =============================================

-- 1. 数据标准与模型中心 (父ID: 100)
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(101, '字段标准库', 'standard:field', 'menu', 100, '/standard/field', 'Grid', 1, 'active', NOW()),
(102, '数据标准视图', 'standard:view', 'menu', 100, '/standard/view', 'Document', 2, 'active', NOW()),
(103, '编码规则', 'standard:encoding', 'menu', 100, '/standard/encoding', 'Key', 3, 'active', NOW()),
(104, '值域管理', 'standard:domain', 'menu', 100, '/standard/domain', 'List', 4, 'active', NOW()),
(105, '变更审批配置', 'standard:approval', 'menu', 100, '/standard/approval', 'Stamp', 5, 'active', NOW());

-- 2. 表单与视图设计中心 (父ID: 200)
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(201, '表单管理', 'form:manage', 'menu', 200, '/form/manage', 'Files', 1, 'active', NOW());

-- 3. 流程与任务管理中心 (父ID: 300)
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(301, '流程定义', 'workflow:define', 'menu', 300, '/workflow/define', 'Share', 1, 'active', NOW()),
(302, '待办任务', 'workflow:todo', 'menu', 300, '/workflow/todo', 'Bell', 2, 'active', NOW()),
(303, '已办任务', 'workflow:done', 'menu', 300, '/workflow/done', 'Finished', 3, 'active', NOW()),
(304, '流程监控', 'workflow:monitor', 'menu', 300, '/workflow/monitor', 'View', 4, 'active', NOW());

-- 4. 主数据管理 (父ID: 350)
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(351, '数据分类', 'data:category', 'menu', 350, '/data/category', 'Folder', 1, 'active', NOW()),
(352, '数据维护', 'data:maintain', 'menu', 350, '/data/maintain', 'Edit', 2, 'active', NOW());

-- 5. 主数据生命周期管理 (父ID: 400)
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(401, '主数据类型', 'masterdata:type', 'menu', 400, '/masterdata/type', 'Grid', 1, 'active', NOW()),
(402, '主数据实例', 'masterdata:instance', 'menu', 400, '/masterdata/instance', 'DataLine', 2, 'active', NOW()),
(403, '生命周期管理', 'masterdata:lifecycle', 'menu', 400, '/masterdata/lifecycle', 'Refresh', 3, 'active', NOW()),
(404, '关系管理', 'masterdata:relation', 'menu', 400, '/masterdata/relation', 'Link', 4, 'active', NOW());

-- 5. 数据质量管理 (父ID: 500)
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(501, '质量规则', 'quality:rule', 'menu', 500, '/quality/rule', 'SetUp', 1, 'active', NOW()),
(502, '质量检测', 'quality:check', 'menu', 500, '/quality/check', 'Search', 2, 'active', NOW()),
(503, '问题管理', 'quality:issue', 'menu', 500, '/quality/issue', 'Warning', 3, 'active', NOW()),
(504, '质量看板', 'quality:dashboard', 'menu', 500, '/quality/dashboard', 'DataBoard', 4, 'active', NOW());

-- 6. 主数据分发管理中心 (父ID: 600)
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(601, '分发主题', 'distribution:topic', 'menu', 600, '/distribution/topic', 'Message', 1, 'active', NOW()),
(602, '订阅管理', 'distribution:subscription', 'menu', 600, '/distribution/subscription', 'User', 2, 'active', NOW()),
(603, '分发监控', 'distribution:monitor', 'menu', 600, '/distribution/monitor', 'View', 3, 'active', NOW()),
(604, '数据溯源', 'distribution:trace', 'menu', 600, '/distribution/trace', 'Guide', 4, 'active', NOW());

-- 7. 版本与审计中心 (父ID: 700)
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(701, '版本管理', 'version:manage', 'menu', 700, '/version/manage', 'Files', 1, 'active', NOW()),
(702, '版本对比', 'version:compare', 'menu', 700, '/version/compare', 'Switch', 2, 'active', NOW()),
(703, '审计日志', 'version:audit', 'menu', 700, '/version/audit', 'Document', 3, 'active', NOW());

-- 8. 系统基础配置中心 (父ID: 800)
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(801, '用户管理', 'system:user', 'menu', 800, '/user', 'User', 1, 'active', NOW()),
(802, '角色管理', 'system:role', 'menu', 800, '/role', 'UserFilled', 2, 'active', NOW()),
(803, '权限管理', 'system:permission', 'menu', 800, '/permission', 'Lock', 3, 'active', NOW()),
(804, '组织管理', 'system:organization', 'menu', 800, '/organization', 'OfficeBuilding', 4, 'active', NOW()),
(805, '数据权限', 'system:data-permission', 'menu', 800, '/system/data-permission', 'Lock', 5, 'active', NOW()),
(806, '规则脚本', 'system:script', 'menu', 800, '/system/script', 'Document', 6, 'active', NOW()),
(807, '定时任务', 'system:schedule', 'menu', 800, '/system/schedule', 'Clock', 7, 'active', NOW()),
(808, '消息通知', 'system:notification', 'menu', 800, '/system/notification', 'Bell', 8, 'active', NOW()),
(809, '系统配置', 'system:config', 'menu', 800, '/system/config', 'Setting', 9, 'active', NOW()),
(810, '外部集成', 'system:integration', 'menu', 800, '/system/integration', 'Link', 10, 'active', NOW());

-- =============================================
-- 三级按钮权限：各功能的操作按钮
-- =============================================

-- 字段标准库按钮权限
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(1011, '查看', 'standard:field:view', 'button', 101, NULL, NULL, 1, 'active', NOW()),
(1012, '新增', 'standard:field:create', 'button', 101, NULL, NULL, 2, 'active', NOW()),
(1013, '编辑', 'standard:field:edit', 'button', 101, NULL, NULL, 3, 'active', NOW()),
(1014, '删除', 'standard:field:delete', 'button', 101, NULL, NULL, 4, 'active', NOW()),
(1015, '发布', 'standard:field:publish', 'button', 101, NULL, NULL, 5, 'active', NOW());

-- 数据标准视图按钮权限
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(1021, '查看', 'standard:view:view', 'button', 102, NULL, NULL, 1, 'active', NOW()),
(1022, '新增', 'standard:view:create', 'button', 102, NULL, NULL, 2, 'active', NOW()),
(1023, '编辑', 'standard:view:edit', 'button', 102, NULL, NULL, 3, 'active', NOW()),
(1024, '删除', 'standard:view:delete', 'button', 102, NULL, NULL, 4, 'active', NOW()),
(1025, '发布', 'standard:view:publish', 'button', 102, NULL, NULL, 5, 'active', NOW()),
(1026, '版本管理', 'standard:view:version', 'button', 102, NULL, NULL, 6, 'active', NOW());

-- 组织管理按钮权限
INSERT INTO permissions (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(8041, '查看', 'system:organization:view', 'button', 804, NULL, NULL, 1, 'active', NOW()),
(8042, '新增', 'system:organization:create', 'button', 804, NULL, NULL, 2, 'active', NOW()),
(8043, '编辑', 'system:organization:edit', 'button', 804, NULL, NULL, 3, 'active', NOW()),
(8044, '删除', 'system:organization:delete', 'button', 804, NULL, NULL, 4, 'active', NOW());

-- 用户管理按钮权限（更新原有权限）
UPDATE permissions SET parent_id = 801 WHERE code IN ('user:view', 'user:create', 'user:edit', 'user:delete');

-- =============================================
-- 为超级管理员角色分配所有权限
-- =============================================

-- 获取超级管理员角色ID (假设为1)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions WHERE id >= 100;

-- =============================================
-- 完成
-- =============================================

SELECT '菜单权限初始化完成!' AS message;
SELECT
    (SELECT COUNT(*) FROM permissions WHERE type = 'menu' AND parent_id IS NULL) AS '一级菜单',
    (SELECT COUNT(*) FROM permissions WHERE type = 'menu' AND parent_id IS NOT NULL) AS '二级菜单',
    (SELECT COUNT(*) FROM permissions WHERE type = 'button') AS '按钮权限',
    (SELECT COUNT(*) FROM permissions) AS '总权限数';
