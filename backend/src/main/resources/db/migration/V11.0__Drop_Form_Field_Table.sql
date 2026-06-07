-- =============================================
-- 删除表单设计器和导入导出模板相关内容
-- 说明：合并表单管理和表单设计器功能
-- =============================================

-- 1. 删除 frm_field 表
DROP TABLE IF EXISTS frm_field;

-- 2. 删除菜单表中表单设计器和导入导出模板菜单
DELETE FROM role_permissions WHERE permission_id IN (202, 203);
DELETE FROM permissions WHERE id IN (202, 203);
