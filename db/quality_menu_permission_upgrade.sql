-- =============================================
-- MDM 质量管理模块 - 菜单权限优化脚本
-- 版本: v2.0
-- 日期: 2026-06-09
-- 说明: 完善质量管理菜单结构和权限配置
-- =============================================

USE mdm;

-- =============================================
-- 1. 新增规则模板菜单
-- =============================================

-- 检查是否已存在规则模板菜单，不存在则插入
INSERT INTO bas_permission (id, name, code, type, parent_id, path, icon, sort, status, created_at)
SELECT 505, '规则模板', 'quality:template', 'menu', 500, '/quality/template', 'Document', 2, 'active', NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM bas_permission WHERE id = 505
);

-- 调整其他菜单的排序
UPDATE bas_permission SET sort = 3 WHERE id = 502; -- 质量检测
UPDATE bas_permission SET sort = 4 WHERE id = 503; -- 问题管理
UPDATE bas_permission SET sort = 5 WHERE id = 504; -- 质量看板

-- =============================================
-- 2. 质量规则 - 三级按钮权限
-- =============================================

-- 删除已存在的按钮权限（避免重复）
DELETE FROM bas_permission WHERE parent_id = 501 AND type = 'button';

-- 新增质量规则按钮权限
INSERT INTO bas_permission (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(5011, '查看', 'quality:rule:view', 'button', 501, NULL, NULL, 1, 'active', NOW()),
(5012, '新增', 'quality:rule:create', 'button', 501, NULL, NULL, 2, 'active', NOW()),
(5013, '编辑', 'quality:rule:edit', 'button', 501, NULL, NULL, 3, 'active', NOW()),
(5014, '删除', 'quality:rule:delete', 'button', 501, NULL, NULL, 4, 'active', NOW()),
(5015, '启用/停用', 'quality:rule:toggle', 'button', 501, NULL, NULL, 5, 'active', NOW()),
(5016, '测试规则', 'quality:rule:test', 'button', 501, NULL, NULL, 6, 'active', NOW()),
(5017, '应用模板', 'quality:rule:apply-template', 'button', 501, NULL, NULL, 7, 'active', NOW());

-- =============================================
-- 3. 规则模板 - 三级按钮权限
-- =============================================

DELETE FROM bas_permission WHERE parent_id = 505 AND type = 'button';

INSERT INTO bas_permission (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(5051, '查看', 'quality:template:view', 'button', 505, NULL, NULL, 1, 'active', NOW()),
(5052, '新增', 'quality:template:create', 'button', 505, NULL, NULL, 2, 'active', NOW()),
(5053, '编辑', 'quality:template:edit', 'button', 505, NULL, NULL, 3, 'active', NOW()),
(5054, '删除', 'quality:template:delete', 'button', 505, NULL, NULL, 4, 'active', NOW()),
(5055, '启用/停用', 'quality:template:toggle', 'button', 505, NULL, NULL, 5, 'active', NOW());

-- =============================================
-- 4. 质量检测 - 三级按钮权限
-- =============================================

DELETE FROM bas_permission WHERE parent_id = 502 AND type = 'button';

INSERT INTO bas_permission (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(5021, '执行检测', 'quality:check:execute', 'button', 502, NULL, NULL, 1, 'active', NOW()),
(5022, '查看结果', 'quality:check:view', 'button', 502, NULL, NULL, 2, 'active', NOW()),
(5023, '查看详情', 'quality:check:detail', 'button', 502, NULL, NULL, 3, 'active', NOW()),
(5024, '删除', 'quality:check:delete', 'button', 502, NULL, NULL, 4, 'active', NOW());

-- =============================================
-- 5. 问题管理 - 三级按钮权限
-- =============================================

DELETE FROM bas_permission WHERE parent_id = 503 AND type = 'button';

INSERT INTO bas_permission (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(5031, '查看', 'quality:issue:view', 'button', 503, NULL, NULL, 1, 'active', NOW()),
(5032, '指派', 'quality:issue:assign', 'button', 503, NULL, NULL, 2, 'active', NOW()),
(5033, '解决', 'quality:issue:resolve', 'button', 503, NULL, NULL, 3, 'active', NOW()),
(5034, '忽略', 'quality:issue:ignore', 'button', 503, NULL, NULL, 4, 'active', NOW()),
(5035, '关闭', 'quality:issue:close', 'button', 503, NULL, NULL, 5, 'active', NOW()),
(5036, '批量指派', 'quality:issue:batch-assign', 'button', 503, NULL, NULL, 6, 'active', NOW()),
(5037, '批量解决', 'quality:issue:batch-resolve', 'button', 503, NULL, NULL, 7, 'active', NOW()),
(5038, '导出', 'quality:issue:export', 'button', 503, NULL, NULL, 8, 'active', NOW());

-- =============================================
-- 6. 质量看板 - 三级按钮权限
-- =============================================

DELETE FROM bas_permission WHERE parent_id = 504 AND type = 'button';

INSERT INTO bas_permission (id, name, code, type, parent_id, path, icon, sort, status, created_at) VALUES
(5041, '查看统计', 'quality:dashboard:statistics', 'button', 504, NULL, NULL, 1, 'active', NOW()),
(5042, '查看趋势', 'quality:dashboard:trend', 'button', 504, NULL, NULL, 2, 'active', NOW()),
(5043, '导出报告', 'quality:dashboard:export', 'button', 504, NULL, NULL, 3, 'active', NOW());

-- =============================================
-- 7. 为超级管理员分配新增的权限
-- =============================================

-- 删除旧的权限分配
DELETE FROM bas_role_permission WHERE permission_id >= 501 AND permission_id < 600;

-- 重新分配所有质量管理权限给超级管理员（角色ID=1）
INSERT INTO bas_role_permission (role_id, permission_id)
SELECT 1, id FROM bas_permission WHERE id >= 501 AND id < 600;

-- =============================================
-- 完成
-- =============================================

SELECT '质量管理菜单权限优化完成！' AS message;

-- 查看菜单结构
SELECT
    p1.id AS '一级菜单ID',
    p1.name AS '一级菜单名称',
    p2.id AS '二级菜单ID',
    p2.name AS '二级菜单名称',
    p3.id AS '三级按钮ID',
    p3.name AS '三级按钮名称',
    p3.code AS '权限编码'
FROM bas_permission p1
LEFT JOIN bas_permission p2 ON p2.parent_id = p1.id AND p2.type = 'menu'
LEFT JOIN bas_permission p3 ON p3.parent_id = p2.id AND p3.type = 'button'
WHERE p1.id = 500
ORDER BY p1.sort, p2.sort, p3.sort;

-- 查看权限统计
SELECT
    (SELECT COUNT(*) FROM bas_permission WHERE parent_id = 500 AND type = 'menu') AS '二级菜单数',
    (SELECT COUNT(*) FROM bas_permission WHERE parent_id IN (SELECT id FROM bas_permission WHERE parent_id = 500) AND type = 'button') AS '按钮权限数',
    (SELECT COUNT(*) FROM bas_role_permission WHERE permission_id >= 501 AND permission_id < 600) AS '已分配权限数';
