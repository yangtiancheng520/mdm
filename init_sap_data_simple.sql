-- ============================================
-- MDM系统数据初始化脚本（简化版）
-- 清理旧数据并初始化SAP标准数据
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 清理旧数据
DELETE FROM std_view_field;
DELETE FROM std_view_entity;
DELETE FROM std_view;
DELETE FROM std_view_category;
DELETE FROM std_field_standard;
DELETE FROM std_field_category;
DELETE FROM std_value_domain_item;
DELETE FROM std_value_domain;

-- ============================================
-- 1. 初始化视图分类
-- ============================================
INSERT INTO std_view_category (id, category_code, category_name, parent_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(1, 'MASTER_DATA', '主数据视图', NULL, 1, 'active', '主数据管理相关视图', 'admin', NOW(), 'admin', NOW()),
(2, 'MATERIAL', '物料管理', 1, 1, 'active', '物料相关视图', 'admin', NOW(), 'admin', NOW()),
(3, 'VENDOR', '供应商管理', 1, 2, 'active', '供应商相关视图', 'admin', NOW(), 'admin', NOW()),
(4, 'CUSTOMER', '客户管理', 1, 3, 'active', '客户相关视图', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 2. 初始化字段分类
-- ============================================
INSERT INTO std_field_category (id, category_code, category_name, parent_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(1, 'BASIC', '基本信息', NULL, 1, 'active', '基本信息字段', 'admin', NOW(), 'admin', NOW()),
(2, 'PURCHASE', '采购信息', NULL, 2, 'active', '采购相关字段', 'admin', NOW(), 'admin', NOW()),
(3, 'SALES', '销售信息', NULL, 3, 'active', '销售相关字段', 'admin', NOW(), 'admin', NOW()),
(4, 'ACCOUNTING', '财务信息', NULL, 4, 'active', '财务会计字段', 'admin', NOW(), 'admin', NOW()),
(5, 'PLANNING', '计划信息', NULL, 5, 'active', '计划相关字段', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 3. 初始化值域
-- ============================================
INSERT INTO std_value_domain (id, domain_code, domain_name, domain_type, status, description, created_by, created_at, updated_by, updated_at) VALUES
(1, 'MATL_TYPE', '物料类型', 'list', 'active', '物料类型', 'admin', NOW(), 'admin', NOW()),
(2, 'IND_SECTOR', '行业领域', 'list', 'active', '行业领域', 'admin', NOW(), 'admin', NOW()),
(3, 'BASE_UOM', '基本单位', 'list', 'active', '计量单位', 'admin', NOW(), 'admin', NOW()),
(4, 'COUNTRY', '国家', 'list', 'active', '国家代码', 'admin', NOW(), 'admin', NOW()),
(5, 'CURRENCY', '货币', 'list', 'active', '货币代码', 'admin', NOW(), 'admin', NOW());

-- 值域项
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(1, 1, 'ROH', '原材料', 1, 'active', 'admin', NOW()),
(2, 1, 'HALB', '半成品', 2, 'active', 'admin', NOW()),
(3, 1, 'FERT', '成品', 3, 'active', 'admin', NOW()),
(4, 1, 'HAWA', '贸易商品', 4, 'active', 'admin', NOW()),
(5, 2, 'M', '机械工程', 1, 'active', 'admin', NOW()),
(6, 2, 'C', '化学工业', 2, 'active', 'admin', NOW()),
(7, 3, 'EA', '件', 1, 'active', 'admin', NOW()),
(8, 3, 'KG', '千克', 2, 'active', 'admin', NOW());

-- ============================================
-- 4. 创建三个草稿视图（不包含详细字段）
-- ============================================
INSERT INTO std_view (id, view_code, view_name, category_id, version, is_trunk, is_latest, layout_columns, label_width, enable_copy, enable_import, enable_export, status, description, created_by, created_at, updated_by, updated_at) VALUES
(1, 'MATERIAL', '物料视图', 2, 1, true, true, 2, 120, true, true, true, 'draft', 'SAP标准物料主数据视图', 'admin', NOW(), 'admin', NOW()),
(2, 'VENDOR', '供应商视图', 3, 1, true, true, 2, 120, true, true, true, 'draft', 'SAP标准供应商主数据视图', 'admin', NOW(), 'admin', NOW()),
(3, 'CUSTOMER', '客户视图', 4, 1, true, true, 2, 120, true, true, true, 'draft', 'SAP标准客户主数据视图', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 5. 创建视图实体
-- ============================================
INSERT INTO std_view_entity (id, view_id, entity_code, entity_name, entity_type, table_name, sort, min_rows, max_rows, enable_add, enable_delete, enable_copy, enable_sort, description, created_by, created_at, updated_by, updated_at) VALUES
(1, 1, 'MAIN', '物料主数据', 'main', 'mdm_material', 1, 1, 1, false, false, false, false, '物料主数据', 'admin', NOW(), 'admin', NOW()),
(2, 2, 'MAIN', '供应商主数据', 'main', 'mdm_vendor', 1, 1, 1, false, false, false, false, '供应商主数据', 'admin', NOW(), 'admin', NOW()),
(3, 3, 'MAIN', '客户主数据', 'main', 'mdm_customer', 1, 1, 1, false, false, false, false, '客户主数据', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 完成！
-- ============================================
