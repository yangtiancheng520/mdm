-- 检查血缘数据
USE mdm;

-- 1. 检查字段映射配置
SELECT '字段映射配置:' as 类型, data_type, COUNT(*) as 数量 
FROM dis_field_mapping 
WHERE status = 'active' 
GROUP BY data_type;

-- 2. 检查分发日志
SELECT '分发日志:' as 类型, status, COUNT(*) as 数量 
FROM dis_log_distribution 
GROUP BY status;

-- 3. 检查血缘数据
SELECT '血缘数据:' as 类型, COUNT(*) as 数量 FROM dis_field_lineage;

-- 4. 查看最近的分发日志详情
SELECT 
    id, log_code, data_type, data_code, status, 
    field_count, success_field_count, created_at 
FROM dis_log_distribution 
ORDER BY created_at DESC 
LIMIT 5;

-- 5. 查看血缘数据详情
SELECT * FROM dis_field_lineage LIMIT 5;
