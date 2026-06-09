-- =============================================
-- 数据分类初始化数据
-- =============================================

USE mdm;

-- 先查看现有表单数据
-- SELECT id, form_code, form_name, form_type, status FROM frm_form;

-- 插入数据分类文件夹和表单数据
-- 清空现有数据（如果需要）
-- DELETE FROM data_category;

-- 一级文件夹
INSERT INTO data_category (id, parent_id, name, type, form_id, icon, sort, created_at) VALUES
(1, NULL, '企业信息管理', 'folder', NULL, 'office-building', 0, NOW()),
(2, NULL, '供应商管理', 'folder', NULL, 'truck', 1, NOW()),
(3, NULL, '客户管理', 'folder', NULL, 'user', 2, NOW()),
(4, NULL, '物料管理', 'folder', NULL, 'box', 3, NOW());

-- 二级文件夹（企业信息管理下）
INSERT INTO data_category (id, parent_id, name, type, form_id, icon, sort, created_at) VALUES
(11, 1, '企业基础信息', 'folder', NULL, NULL, 0, NOW()),
(12, 1, '企业资质证照', 'folder', NULL, NULL, 1, NOW());

-- 二级文件夹（供应商管理下）
INSERT INTO data_category (id, parent_id, name, type, form_id, icon, sort, created_at) VALUES
(21, 2, '供应商基础信息', 'folder', NULL, NULL, 0, NOW()),
(22, 2, '供应商资质', 'folder', NULL, NULL, 1, NOW());

-- 添加表单到分类中（关联已发布的表单）
-- 注意：这里假设表单ID，实际使用时需要根据真实表单ID调整
-- 如果有已发布的表单，可以取消注释以下SQL并根据实际情况调整

-- 示例：将表单添加到对应文件夹
-- INSERT INTO data_category (parent_id, name, type, form_id, sort, created_at)
-- SELECT 11, form_name, 'form', id, 0, NOW()
-- FROM frm_form WHERE status = 'published' AND form_type = 'create' LIMIT 5;

-- 或者手动指定表单ID
-- INSERT INTO data_category (parent_id, name, type, form_id, sort, created_at) VALUES
-- (11, '企业信息新增表单', 'form', 1, 0, NOW()),
-- (12, '企业资质新增表单', 'form', 2, 0, NOW()),
-- (21, '供应商信息新增表单', 'form', 3, 0, NOW());

-- 更新ID自增起始值
ALTER TABLE data_category AUTO_INCREMENT = 100;

-- 查询结果验证
SELECT
    dc.id,
    dc.parent_id,
    dc.name,
    dc.type,
    dc.sort,
    CASE
        WHEN dc.parent_id IS NULL THEN '顶级'
        ELSE (SELECT name FROM data_category WHERE id = dc.parent_id)
    END AS parent_name
FROM data_category dc
ORDER BY
    COALESCE(dc.parent_id, 0),
    dc.sort;
