-- =============================================
-- 更新分发管理菜单
-- =============================================

USE mdm;

-- 删除旧的分发管理子菜单
DELETE FROM bas_role_permission WHERE permission_id IN (601, 602, 603, 604, 605, 606);
DELETE FROM bas_permission WHERE parent_id = 600;

-- 插入新的分发管理子菜单
INSERT INTO bas_permission (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(601, '分发主题', 'distribution:topic', 'menu', 600, '/distribution/topic', 'Message', 1, 'active', NOW()),
(602, '订阅管理', 'distribution:subscription', 'menu', 600, '/distribution/subscription', 'User', 2, 'active', NOW()),
(603, '分发监控', 'distribution:monitor', 'menu', 600, '/distribution/monitor', 'View', 3, 'active', NOW()),
(604, '数据溯源', 'distribution:trace', 'menu', 600, '/distribution/trace', 'Guide', 4, 'active', NOW());

-- 为超级管理员分配新菜单权限
INSERT IGNORE INTO bas_role_permission (role_id, permission_id)
SELECT 1, id FROM bas_permission WHERE id IN (601, 602, 603, 604);

-- 添加按钮权限
INSERT IGNORE INTO bas_permission (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(6011, '新增', 'distribution:topic:create', 'button', 601, NULL, NULL, 1, 'active', NOW()),
(6012, '编辑', 'distribution:topic:edit', 'button', 601, NULL, NULL, 2, 'active', NOW()),
(6013, '删除', 'distribution:topic:delete', 'button', 601, NULL, NULL, 3, 'active', NOW()),
(6014, '启用停用', 'distribution:topic:toggle', 'button', 601, NULL, NULL, 4, 'active', NOW()),
(6021, '新增系统', 'distribution:subscription:create', 'button', 602, NULL, NULL, 1, 'active', NOW()),
(6022, '编辑系统', 'distribution:subscription:edit', 'button', 602, NULL, NULL, 2, 'active', NOW()),
(6023, '删除系统', 'distribution:subscription:delete', 'button', 602, NULL, NULL, 3, 'active', NOW()),
(6024, '测试连接', 'distribution:subscription:test', 'button', 602, NULL, NULL, 4, 'active', NOW()),
(6031, '执行分发', 'distribution:monitor:execute', 'button', 603, NULL, NULL, 1, 'active', NOW()),
(6041, '查看详情', 'distribution:trace:view', 'button', 604, NULL, NULL, 1, 'active', NOW()),
(6042, '重试', 'distribution:trace:retry', 'button', 604, NULL, NULL, 2, 'active', NOW());

-- 为超级管理员分配按钮权限
INSERT IGNORE INTO bas_role_permission (role_id, permission_id)
SELECT 1, id FROM bas_permission WHERE id IN (6011, 6012, 6013, 6014, 6021, 6022, 6023, 6024, 6031, 6041, 6042);

SELECT '菜单更新完成' AS message;
