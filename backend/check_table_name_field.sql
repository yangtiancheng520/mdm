-- 检查 std_view 表是否有 table_name 字段
SHOW COLUMNS FROM std_view LIKE 'table_name';

-- 查看 std_view_entity 表结构
SHOW COLUMNS FROM std_view_entity;

-- 查看已发布的视图，检查 table_name 字段值
SELECT id, view_code, view_name, status, table_name FROM std_view WHERE status = 'published' LIMIT 5;
