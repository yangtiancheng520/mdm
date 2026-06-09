-- =============================================
-- 删除未使用的 mst_ 开头的表
-- =============================================

USE mdm;

-- 删除 mst_instance 表
DROP TABLE IF EXISTS mst_instance;

-- 删除 mst_lifecycle_state 表
DROP TABLE IF EXISTS mst_lifecycle_state;

-- 删除 mst_type 表
DROP TABLE IF EXISTS mst_type;

-- 验证删除结果
SELECT 'Tables with mst_ prefix:' as info;
SHOW TABLES LIKE 'mst%';
