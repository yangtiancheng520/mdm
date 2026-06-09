-- 根据 domain_code 更新 std_view_field 表的 domain_id
-- 从 std_value_domain 表中获取对应的 ID

UPDATE std_view_field vf
INNER JOIN std_value_domain vd ON vf.domain_code = vd.domain_code
SET vf.domain_id = vd.id
WHERE vf.domain_code IS NOT NULL AND vf.domain_code != '';

-- 查看更新结果
SELECT
    vf.id,
    vf.field_code,
    vf.field_name,
    vf.domain_code,
    vf.domain_id,
    vd.domain_name
FROM std_view_field vf
LEFT JOIN std_value_domain vd ON vf.domain_id = vd.id
WHERE vf.domain_code IS NOT NULL AND vf.domain_code != ''
LIMIT 20;
