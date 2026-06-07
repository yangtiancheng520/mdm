-- ==========================================
-- 统一状态字段值 - 将 'active'/'inactive' 改为 '启用'/'停用'
-- 数据库: mdm
-- 执行时间: 2026-06-07
-- ==========================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 1. 更新字段标准表 (std_field_standard)
UPDATE std_field_standard SET status = '启用' WHERE status = 'active';
UPDATE std_field_standard SET status = '停用' WHERE status = 'inactive';

-- 2. 更新值域表 (std_value_domain)
UPDATE std_value_domain SET status = '启用' WHERE status = 'active';
UPDATE std_value_domain SET status = '停用' WHERE status = 'inactive';

-- 3. 更新值域项表 (std_value_domain_item)
UPDATE std_value_domain_item SET status = '启用' WHERE status = 'active';
UPDATE std_value_domain_item SET status = '停用' WHERE status = 'inactive';

-- 4. 更新编码规则表 (std_encoding_rule)
UPDATE std_encoding_rule SET status = '启用' WHERE status = 'active';
UPDATE std_encoding_rule SET status = '停用' WHERE status = 'inactive';

-- 5. 更新字段分类表 (std_field_category)
UPDATE std_field_category SET status = '启用' WHERE status = 'active';
UPDATE std_field_category SET status = '停用' WHERE status = 'inactive';

-- 6. 更新视图分类表 (std_view_category)
UPDATE std_view_category SET status = '启用' WHERE status = 'active';
UPDATE std_view_category SET status = '停用' WHERE status = 'inactive';

-- 7. 更新用户表 (sys_user)
UPDATE sys_user SET status = '启用' WHERE status = 'active';
UPDATE sys_user SET status = '停用' WHERE status = 'inactive';

-- 8. 更新角色表 (sys_role)
UPDATE sys_role SET status = '启用' WHERE status = 'active';
UPDATE sys_role SET status = '停用' WHERE status = 'inactive';

-- 9. 更新权限表 (sys_permission)
UPDATE sys_permission SET status = '启用' WHERE status = 'active';
UPDATE sys_permission SET status = '停用' WHERE status = 'inactive';

-- 验证结果
SELECT 'std_field_standard' AS table_name, status, COUNT(*) AS count FROM std_field_standard GROUP BY status
UNION ALL
SELECT 'std_value_domain' AS table_name, status, COUNT(*) AS count FROM std_value_domain GROUP BY status
UNION ALL
SELECT 'std_value_domain_item' AS table_name, status, COUNT(*) AS count FROM std_value_domain_item GROUP BY status;

SELECT '状态字段统一完成！' AS message;
