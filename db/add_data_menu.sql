-- =============================================
-- 主数据管理模块 - 菜单增量脚本
-- 执行此脚本添加主数据管理菜单
-- =============================================

USE mdm;

-- 添加一级菜单：主数据管理
INSERT INTO bas_permission (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(350, '主数据管理', 'data', 'menu', NULL, '/data', 'DataAnalysis', 35, 'active', NOW())
ON DUPLICATE KEY UPDATE name = '主数据管理', path = '/data', icon = 'DataAnalysis';

-- 添加二级菜单：数据分类、数据维护
INSERT INTO bas_permission (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(351, '数据分类', 'data:category', 'menu', 350, '/data/category', 'Folder', 1, 'active', NOW()),
(352, '数据维护', 'data:maintain', 'menu', 350, '/data/maintain', 'Edit', 2, 'active', NOW())
ON DUPLICATE KEY UPDATE parent_id = 350, path = VALUES(path);

-- 为超级管理员角色分配权限（假设超级管理员角色ID为1）
INSERT IGNORE INTO bas_role_permission (role_id, permission_id) VALUES
(1, 350),
(1, 351),
(1, 352);

SELECT '菜单添加完成!' AS message;
