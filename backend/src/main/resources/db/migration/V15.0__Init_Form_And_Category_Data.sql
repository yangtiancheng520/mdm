-- =============================================
-- 表单和数据分类初始化数据
-- =============================================

SET NAMES utf8mb4;
USE mdm;

-- ==========================================
-- 1. 初始化表单数据（基于视图）
-- ==========================================

-- 查看视图数据
-- SELECT id, view_code, view_name FROM std_view;

-- 物料视图相关表单 (view_id = 1)
INSERT INTO frm_form (form_code, form_name, form_type, view_id, design_mode, status, created_by, created_at, updated_by, updated_at, description) VALUES
('MAT_CREATE', '物料新增', 'create', 1, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '物料主数据新增表单'),
('MAT_EDIT', '物料编辑', 'edit', 1, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '物料主数据编辑表单'),
('MAT_VIEW', '物料查看', 'view', 1, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '物料主数据查看表单'),
('MAT_SEARCH', '物料查询', 'search', 1, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '物料主数据查询表单');

-- 客户视图相关表单 (view_id = 2)
INSERT INTO frm_form (form_code, form_name, form_type, view_id, design_mode, status, created_by, created_at, updated_by, updated_at, description) VALUES
('CUST_CREATE', '客户新增', 'create', 2, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '客户主数据新增表单'),
('CUST_EDIT', '客户编辑', 'edit', 2, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '客户主数据编辑表单'),
('CUST_VIEW', '客户查看', 'view', 2, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '客户主数据查看表单'),
('CUST_SEARCH', '客户查询', 'search', 2, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '客户主数据查询表单');

-- 供应商视图相关表单 (view_id = 3)
INSERT INTO frm_form (form_code, form_name, form_type, view_id, design_mode, status, created_by, created_at, updated_by, updated_at, description) VALUES
('VEND_CREATE', '供应商新增', 'create', 3, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '供应商主数据新增表单'),
('VEND_EDIT', '供应商编辑', 'edit', 3, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '供应商主数据编辑表单'),
('VEND_VIEW', '供应商查看', 'view', 3, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '供应商主数据查看表单'),
('VEND_SEARCH', '供应商查询', 'search', 3, 'auto', 'published', 'admin', NOW(), 'admin', NOW(), '供应商主数据查询表单');

-- ==========================================
-- 2. 初始化数据分类数据
-- ==========================================

-- 清空现有数据分类
DELETE FROM data_category;

-- 一级文件夹
INSERT INTO data_category (id, parent_id, name, type, form_id, icon, sort, created_by, created_at) VALUES
(1, NULL, '物料管理', 'folder', NULL, 'Box', 0, 'admin', NOW()),
(2, NULL, '客户管理', 'folder', NULL, 'User', 1, 'admin', NOW()),
(3, NULL, '供应商管理', 'folder', NULL, 'Van', 2, 'admin', NOW());

-- 物料管理下添加表单 (form_id: 1-4)
INSERT INTO data_category (id, parent_id, name, type, form_id, sort, created_by, created_at) VALUES
(11, 1, '物料新增', 'form', 1, 0, 'admin', NOW()),
(12, 1, '物料编辑', 'form', 2, 1, 'admin', NOW()),
(13, 1, '物料查看', 'form', 3, 2, 'admin', NOW()),
(14, 1, '物料查询', 'form', 4, 3, 'admin', NOW());

-- 客户管理下添加表单 (form_id: 5-8)
INSERT INTO data_category (id, parent_id, name, type, form_id, sort, created_by, created_at) VALUES
(21, 2, '客户新增', 'form', 5, 0, 'admin', NOW()),
(22, 2, '客户编辑', 'form', 6, 1, 'admin', NOW()),
(23, 2, '客户查看', 'form', 7, 2, 'admin', NOW()),
(24, 2, '客户查询', 'form', 8, 3, 'admin', NOW());

-- 供应商管理下添加表单 (form_id: 9-12)
INSERT INTO data_category (id, parent_id, name, type, form_id, sort, created_by, created_at) VALUES
(31, 3, '供应商新增', 'form', 9, 0, 'admin', NOW()),
(32, 3, '供应商编辑', 'form', 10, 1, 'admin', NOW()),
(33, 3, '供应商查看', 'form', 11, 2, 'admin', NOW()),
(34, 3, '供应商查询', 'form', 12, 3, 'admin', NOW());

-- 更新ID自增起始值
ALTER TABLE data_category AUTO_INCREMENT = 100;

-- ==========================================
-- 3. 验证数据
-- ==========================================

-- 查看表单数据
SELECT id, form_code, form_name, form_type, view_id, status FROM frm_form;

-- 查看数据分类
SELECT
    dc.id,
    dc.parent_id,
    dc.name,
    dc.type,
    dc.form_id,
    dc.sort,
    CASE
        WHEN dc.parent_id IS NULL THEN '顶级'
        ELSE (SELECT name FROM data_category WHERE id = dc.parent_id)
    END AS parent_name
FROM data_category dc
ORDER BY
    COALESCE(dc.parent_id, 0),
    dc.sort;
