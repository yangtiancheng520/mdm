-- 检查字段映射配置
SELECT '字段映射配置数量:' as info, COUNT(*) as count FROM dis_field_mapping WHERE status = 'active';

-- 检查血缘数据
SELECT '血缘数据数量:' as info, COUNT(*) as count FROM dis_field_lineage;

-- 检查分发日志
SELECT '分发日志数量:' as info, COUNT(*) as count FROM dis_log_distribution WHERE status = 'SUCCESS';

-- 查看最近的分发日志
SELECT id, log_code, data_type, data_code, status, field_count, success_field_count, created_at 
FROM dis_log_distribution 
ORDER BY created_at DESC 
LIMIT 5;
