-- 查看MATERIAL视图的所有版本及其状态
SELECT 
    id,
    view_code,
    view_name,
    version,
    status,
    is_trunk,
    is_latest,
    base_version_id,
    publish_time,
    created_at,
    updated_at
FROM view_definition 
WHERE view_code = 'MATERIAL'
ORDER BY version DESC, updated_at DESC;

-- 查看该视图的实体数量
SELECT 
    vd.id as view_id,
    vd.version,
    vd.status,
    COUNT(DISTINCT ve.id) as entity_count,
    COUNT(vf.id) as field_count
FROM view_definition vd
LEFT JOIN view_entity ve ON ve.view_id = vd.id
LEFT JOIN view_field vf ON vf.entity_id = ve.id
WHERE vd.view_code = 'MATERIAL'
GROUP BY vd.id, vd.version, vd.status
ORDER BY vd.version DESC;
