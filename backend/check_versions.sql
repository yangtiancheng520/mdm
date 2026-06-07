-- 查询MATERIAL视图的所有版本
SELECT 
    id,
    view_code,
    view_name,
    version,
    status,
    is_trunk,
    is_latest,
    base_version_id,
    created_at,
    updated_at
FROM view_definition 
WHERE view_code = 'MATERIAL'
ORDER BY version DESC;
