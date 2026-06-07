-- =====================================================
-- 权限数据修复脚本
-- =====================================================

-- 1. 补充菜单权限：如果角色有子菜单权限，自动添加父菜单权限
INSERT INTO bas_role_permission (role_id, permission_id)
SELECT DISTINCT
    rp.role_id,
    p.parent_id
FROM bas_role_permission rp
JOIN bas_permission p ON p.id = rp.permission_id
WHERE p.type = 'menu'
  AND p.parent_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM bas_role_permission rp2
      WHERE rp2.role_id = rp.role_id
        AND rp2.permission_id = p.parent_id
  );

-- 2. 补充菜单权限：如果角色有按钮权限，自动添加父菜单权限
INSERT INTO bas_role_permission (role_id, permission_id)
SELECT DISTINCT
    rp.role_id,
    p.parent_id
FROM bas_role_permission rp
JOIN bas_permission p ON p.id = rp.permission_id
WHERE p.type = 'button'
  AND p.parent_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM bas_role_permission rp2
      WHERE rp2.role_id = rp.role_id
        AND rp2.permission_id = p.parent_id
  );

-- 3. 递归补充所有层级的父菜单（处理多级菜单的情况）
-- 重复执行直到没有新的父菜单需要添加
INSERT INTO bas_role_permission (role_id, permission_id)
SELECT DISTINCT
    rp.role_id,
    p2.parent_id
FROM bas_role_permission rp
JOIN bas_permission p1 ON p1.id = rp.permission_id
JOIN bas_permission p2 ON p2.id = p1.parent_id
WHERE p1.type = 'menu'
  AND p1.parent_id IS NOT NULL
  AND p2.parent_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM bas_role_permission rp2
      WHERE rp2.role_id = rp.role_id
        AND rp2.permission_id = p2.parent_id
  );

-- 4. 查看修复结果
SELECT
    '修复完成' as status,
    r.name as role_name,
    COUNT(DISTINCT rp.permission_id) as total_permissions,
    COUNT(DISTINCT CASE WHEN p.type = 'menu' THEN rp.permission_id END) as menu_permissions,
    COUNT(DISTINCT CASE WHEN p.type = 'button' THEN rp.permission_id END) as button_permissions
FROM bas_role r
LEFT JOIN bas_role_permission rp ON rp.role_id = r.id
LEFT JOIN bas_permission p ON p.id = rp.permission_id
GROUP BY r.id, r.name;
