-- 检查表单管理菜单是否存在
SELECT id, name, code, path, parent_id, type
FROM permissions
WHERE code LIKE 'form%' OR name LIKE '%表单%'
ORDER BY id;
