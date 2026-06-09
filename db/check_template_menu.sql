-- 检查规则模板菜单是否存在
SELECT
    id,
    name,
    code,
    type,
    parent_id,
    path,
    sort,
    status
FROM bas_permission
WHERE id = 505 OR name LIKE '%规则模板%' OR path LIKE '%template%';

-- 查看质量管理下的所有菜单
SELECT
    p1.id AS '一级ID',
    p1.name AS '一级菜单',
    p2.id AS '二级ID',
    p2.name AS '二级菜单',
    p2.path AS '路径',
    p2.sort AS '排序',
    p2.status AS '状态'
FROM bas_permission p1
LEFT JOIN bas_permission p2 ON p2.parent_id = p1.id AND p2.type = 'menu'
WHERE p1.id = 500 OR p1.name LIKE '%质量%'
ORDER BY p2.sort;
