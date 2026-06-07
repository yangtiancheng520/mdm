-- 修复已停用但应该是历史版本的数据
-- 当一个视图编码有多个版本，且其中有已发布的版本时，
-- 已停用的旧版本应该改为历史版本

UPDATE view_definition vd1
SET status = 'history'
WHERE status = 'disabled'
AND EXISTS (
    -- 检查是否存在同一视图编码的已发布版本
    SELECT 1 
    FROM view_definition vd2 
    WHERE vd2.view_code = vd1.view_code 
    AND vd2.status = 'published'
    AND vd2.id != vd1.id
);

-- 查看修复结果
SELECT 
    id,
    view_code,
    view_name,
    version,
    status,
    is_trunk,
    is_latest,
    updated_at
FROM view_definition 
WHERE view_code = 'MATERIAL'
ORDER BY version DESC, updated_at DESC;
