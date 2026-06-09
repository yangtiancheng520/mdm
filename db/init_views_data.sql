-- ==========================================
-- 初始化三个视图数据（物料视图、供应商视图、客户视图）
-- 状态：草稿
-- 根据现有数据字典字段生成
-- ==========================================

SET NAMES utf8mb4;
USE mdm;

-- ==========================================
-- 1. 创建视图分类
-- ==========================================
INSERT INTO std_view_category (category_code, category_name, parent_id, sort, status, created_by, created_at, description) VALUES
('MAT', '物料管理', NULL, 1, 'active', 'admin', NOW(), '物料相关视图'),
('SD', '销售管理', NULL, 2, 'active', 'admin', NOW(), '销售相关视图'),
('MM', '采购管理', NULL, 3, 'active', 'admin', NOW(), '采购相关视图');

SELECT '========== 视图分类创建完成 ==========' AS '';

-- ==========================================
-- 2. 创建视图定义（状态：草稿）
-- ==========================================
INSERT INTO std_view (view_code, view_name, category_id, version, status, layout_columns, label_width, enable_copy, enable_import, enable_export, created_by, created_at, updated_by, updated_at, description) VALUES
('MATERIAL', '物料视图', 1, 1, 'draft', 2, 120, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), '物料主数据视图，参照SAP物料主数据结构，包含基本信息、采购信息、MRP信息、财务信息、仓库信息等子表'),
('CUSTOMER', '客户视图', 2, 1, 'draft', 2, 120, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), '客户主数据视图，参照SAP客户主数据结构，包含基本信息、销售信息、财务信息等子表'),
('VENDOR', '供应商视图', 3, 1, 'draft', 2, 120, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), '供应商主数据视图，参照SAP供应商主数据结构，包含基本信息、采购信息、财务信息等子表');

SELECT '========== 视图定义创建完成 ==========' AS '';

-- ==========================================
-- 3. 物料视图 (view_id = 1)
-- ==========================================

-- 3.1 物料主表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, table_name, sort, min_rows, max_rows, enable_add, enable_delete, enable_copy, enable_sort, status, created_by, created_at, description) VALUES
(1, 'MATERIAL_MAIN', '物料基本信息', 'main', 'mdm_material_main', 0, 1, 1, 0, 0, 0, 0, 'active', 'admin', NOW(), '物料主数据基本信息');

-- 3.2 物料子表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, table_name, sort, min_rows, max_rows, enable_add, enable_delete, enable_copy, enable_sort, status, created_by, created_at, description) VALUES
(1, 'MATERIAL_PURCHASE', '采购信息', 'sub', 'mdm_material_purchase', 1, 0, 10, 1, 1, 1, 1, 'active', 'admin', NOW(), '物料采购视图，每个采购组织一条记录'),
(1, 'MATERIAL_MRP', 'MRP信息', 'sub', 'mdm_material_mrp', 2, 0, 10, 1, 1, 1, 1, 'active', 'admin', NOW(), '物料MRP信息'),
(1, 'MATERIAL_ACCOUNT', '财务信息', 'sub', 'mdm_material_account', 3, 0, 10, 1, 1, 1, 1, 'active', 'admin', NOW(), '物料财务评估信息'),
(1, 'MATERIAL_WH', '仓库信息', 'sub', 'mdm_material_wh', 4, 0, 100, 1, 1, 1, 1, 'active', 'admin', NOW(), '物料仓库/库存地点信息');

SELECT '========== 物料视图实体创建完成 ==========' AS '';

-- 3.3 物料主表字段分组 (entity_id = 1)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(1, 'BASIC', '基本信息', 1, 2, 1, 0, 'active', 'admin', NOW()),
(1, 'WEIGHT', '重量体积', 2, 2, 1, 0, 'active', 'admin', NOW()),
(1, 'COMMON', '通用信息', 3, 2, 1, 0, 'active', 'admin', NOW());

-- 3.4 物料主表字段 (entity_id = 1)
-- 获取数据字典字段ID
SET @fs_matnr = (SELECT id FROM std_field_standard WHERE field_code = 'MATNR');
SET @fs_maktx = (SELECT id FROM std_field_standard WHERE field_code = 'MAKTX');
SET @fs_maktg = (SELECT id FROM std_field_standard WHERE field_code = 'MAKTG');
SET @fs_mtart = (SELECT id FROM std_field_standard WHERE field_code = 'MTART');
SET @fs_matkl = (SELECT id FROM std_field_standard WHERE field_code = 'MATKL');
SET @fs_meins = (SELECT id FROM std_field_standard WHERE field_code = 'MEINS');
SET @fs_mbrsh = (SELECT id FROM std_field_standard WHERE field_code = 'MBRSH');
SET @fs_brgew = (SELECT id FROM std_field_standard WHERE field_code = 'BRGEW');
SET @fs_ntgew = (SELECT id FROM std_field_standard WHERE field_code = 'NTGEW');
SET @fs_gewei = (SELECT id FROM std_field_standard WHERE field_code = 'GEWEI');
SET @fs_volum = (SELECT id FROM std_field_standard WHERE field_code = 'VOLUM');
SET @fs_voleh = (SELECT id FROM std_field_standard WHERE field_code = 'VOLEH');
SET @fs_xchpf = (SELECT id FROM std_field_standard WHERE field_code = 'XCHPF');
SET @fs_status = (SELECT id FROM std_field_standard WHERE field_code = 'STATUS');
SET @fs_ernam = (SELECT id FROM std_field_standard WHERE field_code = 'ERNAM');
SET @fs_erdat = (SELECT id FROM std_field_standard WHERE field_code = 'ERDAT');
SET @fs_aenam = (SELECT id FROM std_field_standard WHERE field_code = 'AENAM');
SET @fs_laeda = (SELECT id FROM std_field_standard WHERE field_code = 'LAEDA');
SET @fs_loevm = (SELECT id FROM std_field_standard WHERE field_code = 'LOEVM');
SET @fs_sperr = (SELECT id FROM std_field_standard WHERE field_code = 'SPERR');
SET @fs_desc = (SELECT id FROM std_field_standard WHERE field_code = 'DESCRIPTION');

-- 获取分组ID
SET @grp_basic = (SELECT id FROM std_view_group WHERE entity_id = 1 AND group_code = 'BASIC');
SET @grp_weight = (SELECT id FROM std_view_group WHERE entity_id = 1 AND group_code = 'WEIGHT');
SET @grp_common = (SELECT id FROM std_view_group WHERE entity_id = 1 AND group_code = 'COMMON');

-- 获取值域ID
SET @dom_material_type = (SELECT id FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE');
SET @dom_material_group = (SELECT id FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP');
SET @dom_base_unit = (SELECT id FROM std_value_domain WHERE domain_code = 'BASE_UNIT');
SET @dom_industry = (SELECT id FROM std_value_domain WHERE domain_code = 'INDUSTRY');
SET @dom_status = (SELECT id FROM std_value_domain WHERE domain_code = 'STATUS');
SET @dom_yes_no = (SELECT id FROM std_value_domain WHERE domain_code = 'YES_NO');

-- 插入物料主表字段
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
-- 基本信息分组
(1, @fs_matnr, 'MATNR', '物料编号', 'string', 18, NULL, NULL, NULL, NULL, @grp_basic, 1, 1, 1, 0, 0, 1, 1, 1, 1, 'active', 'admin', NOW()),
(1, @fs_maktx, 'MAKTX', '物料描述', 'string', 40, NULL, NULL, NULL, NULL, @grp_basic, 2, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(1, @fs_maktg, 'MAKTG', '物料描述2', 'string', 40, NULL, NULL, NULL, NULL, @grp_basic, 3, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(1, @fs_mtart, 'MTART', '物料类型', 'string', 4, NULL, @dom_material_type, 'MATERIAL_TYPE', '物料类型', @grp_basic, 4, 1, 1, 0, 0, 0, 1, 1, 1, 'active', 'admin', NOW()),
(1, @fs_matkl, 'MATKL', '物料组', 'string', 9, NULL, @dom_material_group, 'MATERIAL_GROUP', '物料组', @grp_basic, 5, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(1, @fs_meins, 'MEINS', '基本单位', 'string', 3, NULL, @dom_base_unit, 'BASE_UNIT', '基本单位', @grp_basic, 6, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(1, @fs_mbrsh, 'MBRSH', '行业领域', 'string', 1, NULL, @dom_industry, 'INDUSTRY', '行业分类', @grp_basic, 7, 1, 1, 0, 0, 0, 1, 0, 0, 'active', 'admin', NOW()),
-- 重量体积分组
(1, @fs_brgew, 'BRGEW', '毛重', 'decimal', 13, 3, NULL, NULL, NULL, @grp_weight, 1, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(1, @fs_ntgew, 'NTGEW', '净重', 'decimal', 13, 3, NULL, NULL, NULL, @grp_weight, 2, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(1, @fs_gewei, 'GEWEI', '重量单位', 'string', 3, NULL, @dom_base_unit, 'BASE_UNIT', '基本单位', @grp_weight, 3, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(1, @fs_volum, 'VOLUM', '体积', 'decimal', 13, 3, NULL, NULL, NULL, @grp_weight, 4, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(1, @fs_voleh, 'VOLEH', '体积单位', 'string', 3, NULL, @dom_base_unit, 'BASE_UNIT', '基本单位', @grp_weight, 5, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(1, @fs_xchpf, 'XCHPF', '批次管理', 'boolean', NULL, NULL, @dom_yes_no, 'YES_NO', '是/否', @grp_weight, 6, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
-- 通用信息分组
(1, @fs_status, 'STATUS', '状态', 'string', 20, NULL, @dom_status, 'STATUS', '启用状态', @grp_common, 1, 1, 1, 0, 0, 0, 1, 1, 1, 'active', 'admin', NOW()),
(1, @fs_ernam, 'ERNAM', '创建人', 'string', 12, NULL, NULL, NULL, NULL, @grp_common, 2, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(1, @fs_erdat, 'ERDAT', '创建日期', 'date', NULL, NULL, NULL, NULL, NULL, @grp_common, 3, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(1, @fs_aenam, 'AENAM', '修改人', 'string', 12, NULL, NULL, NULL, NULL, @grp_common, 4, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(1, @fs_laeda, 'LAEDA', '修改日期', 'date', NULL, NULL, NULL, NULL, NULL, @grp_common, 5, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(1, @fs_loevm, 'LOEVM', '删除标记', 'boolean', NULL, NULL, @dom_yes_no, 'YES_NO', '是/否', @grp_common, 6, 1, 0, 1, 1, 0, 0, 0, 0, 'active', 'admin', NOW()),
(1, @fs_sperr, 'SPERR', '冻结标记', 'boolean', NULL, NULL, @dom_yes_no, 'YES_NO', '是/否', @grp_common, 7, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(1, @fs_desc, 'DESCRIPTION', '描述', 'text', NULL, NULL, NULL, NULL, NULL, @grp_common, 8, 2, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW());

SELECT '========== 物料主表字段创建完成 ==========' AS '';

-- 3.5 物料采购子表字段 (entity_id = 2)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(2, 'PURCHASE_BASIC', '采购基本信息', 1, 2, 1, 0, 'active', 'admin', NOW());

SET @grp_purchase = (SELECT id FROM std_view_group WHERE entity_id = 2 AND group_code = 'PURCHASE_BASIC');

-- 获取数据字典采购字段
SET @fs_ekorg = (SELECT id FROM std_field_standard WHERE field_code = 'EKORG');
SET @fs_ekgrp = (SELECT id FROM std_field_standard WHERE field_code = 'EKGRP');
SET @fs_bstme = (SELECT id FROM std_field_standard WHERE field_code = 'BSTME');
SET @fs_bstmi = (SELECT id FROM std_field_standard WHERE field_code = 'BSTMI');
SET @fs_minls = (SELECT id FROM std_field_standard WHERE field_code = 'MINLS');
SET @fs_maxls = (SELECT id FROM std_field_standard WHERE field_code = 'MAXLS');
SET @fs_plifz = (SELECT id FROM std_field_standard WHERE field_code = 'PLIFZ');
SET @fs_webaz = (SELECT id FROM std_field_standard WHERE field_code = 'WEBAZ');
SET @fs_mabst = (SELECT id FROM std_field_standard WHERE field_code = 'MABST');
SET @fs_lfrab = (SELECT id FROM std_field_standard WHERE field_code = 'LFRAB');

-- 获取值域ID
SET @dom_purchase_org = (SELECT id FROM std_value_domain WHERE domain_code = 'PURCHASE_ORG');

INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
(2, @fs_ekorg, 'EKORG', '采购组织', 'string', 4, NULL, @dom_purchase_org, 'PURCHASE_ORG', '采购组织', @grp_purchase, 1, 1, 1, 0, 0, 0, 1, 1, 1, 'active', 'admin', NOW()),
(2, @fs_ekgrp, 'EKGRP', '采购组', 'string', 3, NULL, NULL, NULL, NULL, @grp_purchase, 2, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(2, @fs_bstme, 'BSTME', '采购单位', 'string', 3, NULL, @dom_base_unit, 'BASE_UNIT', '基本单位', @grp_purchase, 3, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(2, @fs_bstmi, 'BSTMI', '最小包装量', 'decimal', 13, 3, NULL, NULL, NULL, @grp_purchase, 4, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(2, @fs_minls, 'MINLS', '最小批量', 'decimal', 13, 3, NULL, NULL, NULL, @grp_purchase, 5, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(2, @fs_maxls, 'MAXLS', '最大批量', 'decimal', 13, 3, NULL, NULL, NULL, @grp_purchase, 6, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(2, @fs_plifz, 'PLIFZ', '计划交货时间', 'integer', NULL, NULL, NULL, NULL, NULL, @grp_purchase, 7, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(2, @fs_webaz, 'WEBAZ', '收货处理时间', 'integer', NULL, NULL, NULL, NULL, NULL, @grp_purchase, 8, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(2, @fs_mabst, 'MABST', '最大库存水平', 'decimal', 13, 3, NULL, NULL, NULL, @grp_purchase, 9, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(2, @fs_lfrab, 'LFRAB', '供应商配额', 'decimal', 5, 2, NULL, NULL, NULL, @grp_purchase, 10, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW());

SELECT '========== 物料采购子表字段创建完成 ==========' AS '';

-- 3.6 物料MRP子表字段 (entity_id = 3)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(3, 'MRP_BASIC', 'MRP基本信息', 1, 2, 1, 0, 'active', 'admin', NOW());

SET @grp_mrp = (SELECT id FROM std_view_group WHERE entity_id = 3 AND group_code = 'MRP_BASIC');

-- 获取MRP字段
SET @fs_dismm = (SELECT id FROM std_field_standard WHERE field_code = 'DISMM');
SET @fs_dispo = (SELECT id FROM std_field_standard WHERE field_code = 'DISPO');
SET @fs_beskz = (SELECT id FROM std_field_standard WHERE field_code = 'BESKZ');
SET @fs_disls = (SELECT id FROM std_field_standard WHERE field_code = 'DISLS');
SET @fs_bstfe = (SELECT id FROM std_field_standard WHERE field_code = 'BSTFE');
SET @fs_minbe = (SELECT id FROM std_field_standard WHERE field_code = 'MINBE');
SET @fs_eisbe = (SELECT id FROM std_field_standard WHERE field_code = 'EISBE');
SET @fs_plnmr = (SELECT id FROM std_field_standard WHERE field_code = 'PLNMR');
SET @fs_sobsl = (SELECT id FROM std_field_standard WHERE field_code = 'SOBSL');
SET @fs_vrbzt = (SELECT id FROM std_field_standard WHERE field_code = 'VRBZT');
SET @fs_vsmin = (SELECT id FROM std_field_standard WHERE field_code = 'VSMIN');
SET @fs_wzeit = (SELECT id FROM std_field_standard WHERE field_code = 'WZEIT');

INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
(3, @fs_dismm, 'DISMM', 'MRP类型', 'string', 2, NULL, NULL, NULL, NULL, @grp_mrp, 1, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(3, @fs_dispo, 'DISPO', 'MRP控制者', 'string', 3, NULL, NULL, NULL, NULL, @grp_mrp, 2, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(3, @fs_beskz, 'BESKZ', '采购类型', 'string', 1, NULL, NULL, NULL, NULL, @grp_mrp, 3, 1, 1, 0, 0, 0, 1, 0, 0, 'active', 'admin', NOW()),
(3, @fs_disls, 'DISLS', '批量大小', 'string', 2, NULL, NULL, NULL, NULL, @grp_mrp, 4, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(3, @fs_bstfe, 'BSTFE', '固定批量', 'decimal', 13, 3, NULL, NULL, NULL, @grp_mrp, 5, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(3, @fs_minbe, 'MINBE', '安全库存', 'decimal', 13, 3, NULL, NULL, NULL, @grp_mrp, 6, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(3, @fs_eisbe, 'EISBE', '最小安全库存', 'decimal', 13, 3, NULL, NULL, NULL, @grp_mrp, 7, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(3, @fs_plnmr, 'PLNMR', '计划模式', 'string', 1, NULL, NULL, NULL, NULL, @grp_mrp, 8, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(3, @fs_sobsl, 'SOBSL', '特殊采购', 'string', 2, NULL, NULL, NULL, NULL, @grp_mrp, 9, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(3, @fs_vrbzt, 'VRBZT', '消耗模式', 'string', 1, NULL, NULL, NULL, NULL, @grp_mrp, 10, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(3, @fs_vsmin, 'VSMIN', '反冲', 'boolean', NULL, NULL, @dom_yes_no, 'YES_NO', '是/否', @grp_mrp, 11, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(3, @fs_wzeit, 'WZEIT', '计划边际码', 'integer', NULL, NULL, NULL, NULL, NULL, @grp_mrp, 12, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW());

SELECT '========== 物料MRP子表字段创建完成 ==========' AS '';

-- 3.7 物料财务子表字段 (entity_id = 4)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(4, 'ACCOUNT_BASIC', '财务基本信息', 1, 2, 1, 0, 'active', 'admin', NOW());

SET @grp_account = (SELECT id FROM std_view_group WHERE entity_id = 4 AND group_code = 'ACCOUNT_BASIC');

-- 获取财务字段
SET @fs_bklas = (SELECT id FROM std_field_standard WHERE field_code = 'BKLAS');
SET @fs_bwtty = (SELECT id FROM std_field_standard WHERE field_code = 'BWTTY');
SET @fs_vprsv = (SELECT id FROM std_field_standard WHERE field_code = 'VPRSV');
SET @fs_stprs = (SELECT id FROM std_field_standard WHERE field_code = 'STPRS');
SET @fs_verpr = (SELECT id FROM std_field_standard WHERE field_code = 'VERPR');
SET @fs_peinh = (SELECT id FROM std_field_standard WHERE field_code = 'PEINH');
SET @fs_waers = (SELECT id FROM std_field_standard WHERE field_code = 'WAERS');
SET @fs_mlast = (SELECT id FROM std_field_standard WHERE field_code = 'MLAST');

-- 获取值域
SET @dom_currency = (SELECT id FROM std_value_domain WHERE domain_code = 'CURRENCY');

INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
(4, @fs_bklas, 'BKLAS', '评估类', 'string', 4, NULL, NULL, NULL, NULL, @grp_account, 1, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(4, @fs_bwtty, 'BWTTY', '评估类型', 'string', 1, NULL, NULL, NULL, NULL, @grp_account, 2, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(4, @fs_vprsv, 'VPRSV', '价格控制', 'string', 1, NULL, NULL, NULL, NULL, @grp_account, 3, 1, 1, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(4, @fs_stprs, 'STPRS', '标准价格', 'decimal', 15, 2, NULL, NULL, NULL, @grp_account, 4, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(4, @fs_verpr, 'VERPR', '移动平均价', 'decimal', 15, 2, NULL, NULL, NULL, @grp_account, 5, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(4, @fs_peinh, 'PEINH', '价格单位', 'integer', NULL, NULL, NULL, NULL, NULL, @grp_account, 6, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(4, @fs_waers, 'WAERS', '货币', 'string', 5, NULL, @dom_currency, 'CURRENCY', '货币', @grp_account, 7, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(4, @fs_mlast, 'MLAST', '价格确定', 'string', 1, NULL, NULL, NULL, NULL, @grp_account, 8, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW());

SELECT '========== 物料财务子表字段创建完成 ==========' AS '';

-- 3.8 物料仓库子表字段 (entity_id = 5)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(5, 'WH_BASIC', '仓库基本信息', 1, 2, 1, 0, 'active', 'admin', NOW());

SET @grp_wh = (SELECT id FROM std_view_group WHERE entity_id = 5 AND group_code = 'WH_BASIC');

INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
(5, NULL, 'WERKS', '工厂', 'string', 4, NULL, NULL, NULL, NULL, @grp_wh, 1, 1, 1, 0, 0, 0, 1, 1, 1, 'active', 'admin', NOW()),
(5, NULL, 'LGORT', '库存地点', 'string', 4, NULL, NULL, NULL, NULL, @grp_wh, 2, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW());

SELECT '========== 物料仓库子表字段创建完成 ==========' AS '';

-- ==========================================
-- 4. 客户视图 (view_id = 2)
-- ==========================================

-- 4.1 客户主表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, table_name, sort, min_rows, max_rows, enable_add, enable_delete, enable_copy, enable_sort, status, created_by, created_at, description) VALUES
(2, 'CUSTOMER_MAIN', '客户基本信息', 'main', 'mdm_customer_main', 0, 1, 1, 0, 0, 0, 0, 'active', 'admin', NOW(), '客户主数据基本信息');

-- 4.2 客户子表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, table_name, sort, min_rows, max_rows, enable_add, enable_delete, enable_copy, enable_sort, status, created_by, created_at, description) VALUES
(2, 'CUSTOMER_SALES', '销售信息', 'sub', 'mdm_customer_sales', 1, 0, 10, 1, 1, 1, 1, 'active', 'admin', NOW(), '客户销售区域信息，每个销售区域一条记录'),
(2, 'CUSTOMER_ACCOUNT', '财务信息', 'sub', 'mdm_customer_account', 2, 0, 10, 1, 1, 1, 1, 'active', 'admin', NOW(), '客户财务信息，每个公司代码一条记录');

SELECT '========== 客户视图实体创建完成 ==========' AS '';

-- 4.3 客户主表字段分组 (entity_id = 6)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(6, 'CUST_BASIC', '基本信息', 1, 2, 1, 0, 'active', 'admin', NOW()),
(6, 'CUST_ADDRESS', '地址信息', 2, 2, 1, 0, 'active', 'admin', NOW()),
(6, 'CUST_CONTACT', '联系信息', 3, 2, 1, 0, 'active', 'admin', NOW()),
(6, 'CUST_COMMON', '通用信息', 4, 2, 1, 0, 'active', 'admin', NOW());

-- 获取客户字段
SET @fs_kunnr = (SELECT id FROM std_field_standard WHERE field_code = 'KUNNR');
SET @fs_name1_c = (SELECT id FROM std_field_standard WHERE field_code = 'NAME1_C');
SET @fs_name2_c = (SELECT id FROM std_field_standard WHERE field_code = 'NAME2_C');
SET @fs_name3_c = (SELECT id FROM std_field_standard WHERE field_code = 'NAME3_C');
SET @fs_name4_c = (SELECT id FROM std_field_standard WHERE field_code = 'NAME4_C');
SET @fs_ort01_c = (SELECT id FROM std_field_standard WHERE field_code = 'ORT01_C');
SET @fs_pstlz_c = (SELECT id FROM std_field_standard WHERE field_code = 'PSTLZ_C');
SET @fs_regio_c = (SELECT id FROM std_field_standard WHERE field_code = 'REGIO_C');
SET @fs_land1_c = (SELECT id FROM std_field_standard WHERE field_code = 'LAND1_C');
SET @fs_stras_c = (SELECT id FROM std_field_standard WHERE field_code = 'STRAS_C');
SET @fs_pfach_c = (SELECT id FROM std_field_standard WHERE field_code = 'PFACH_C');
SET @fs_telf1_c = (SELECT id FROM std_field_standard WHERE field_code = 'TELF1_C');
SET @fs_telf2_c = (SELECT id FROM std_field_standard WHERE field_code = 'TELF2_C');
SET @fs_telfx_c = (SELECT id FROM std_field_standard WHERE field_code = 'TELFX_C');
SET @fs_smtp_c = (SELECT id FROM std_field_standard WHERE field_code = 'SMTP_C');
SET @fs_spras_c = (SELECT id FROM std_field_standard WHERE field_code = 'SPRAS_C');
SET @fs_stcd1_c = (SELECT id FROM std_field_standard WHERE field_code = 'STCD1_C');
SET @fs_stcd2_c = (SELECT id FROM std_field_standard WHERE field_code = 'STCD2_C');

-- 获取分组ID
SET @grp_cust_basic = (SELECT id FROM std_view_group WHERE entity_id = 6 AND group_code = 'CUST_BASIC');
SET @grp_cust_addr = (SELECT id FROM std_view_group WHERE entity_id = 6 AND group_code = 'CUST_ADDRESS');
SET @grp_cust_contact = (SELECT id FROM std_view_group WHERE entity_id = 6 AND group_code = 'CUST_CONTACT');
SET @grp_cust_common = (SELECT id FROM std_view_group WHERE entity_id = 6 AND group_code = 'CUST_COMMON');

-- 获取值域
SET @dom_country = (SELECT id FROM std_value_domain WHERE domain_code = 'COUNTRY');
SET @dom_province = (SELECT id FROM std_value_domain WHERE domain_code = 'PROVINCE');
SET @dom_language = (SELECT id FROM std_value_domain WHERE domain_code = 'LANGUAGE');

-- 插入客户主表字段
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
-- 基本信息分组
(6, @fs_kunnr, 'KUNNR', '客户编号', 'string', 10, NULL, NULL, NULL, NULL, @grp_cust_basic, 1, 1, 1, 0, 0, 1, 1, 1, 1, 'active', 'admin', NOW()),
(6, @fs_name1_c, 'NAME1', '客户名称', 'string', 35, NULL, NULL, NULL, NULL, @grp_cust_basic, 2, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(6, @fs_name2_c, 'NAME2', '客户名称2', 'string', 35, NULL, NULL, NULL, NULL, @grp_cust_basic, 3, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(6, @fs_name3_c, 'NAME3', '客户名称3', 'string', 35, NULL, NULL, NULL, NULL, @grp_cust_basic, 4, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(6, @fs_name4_c, 'NAME4', '客户名称4', 'string', 35, NULL, NULL, NULL, NULL, @grp_cust_basic, 5, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
-- 地址信息分组
(6, @fs_land1_c, 'LAND1', '国家', 'string', 3, NULL, @dom_country, 'COUNTRY', '国家代码', @grp_cust_addr, 1, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(6, @fs_regio_c, 'REGIO', '地区', 'string', 3, NULL, @dom_province, 'PROVINCE', '省份', @grp_cust_addr, 2, 1, 0, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(6, @fs_ort01_c, 'ORT01', '城市', 'string', 35, NULL, NULL, NULL, NULL, @grp_cust_addr, 3, 1, 0, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(6, @fs_stras_c, 'STRAS', '街道', 'string', 35, NULL, NULL, NULL, NULL, @grp_cust_addr, 4, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(6, @fs_pstlz_c, 'PSTLZ', '邮政编码', 'string', 10, NULL, NULL, NULL, NULL, @grp_cust_addr, 5, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(6, @fs_pfach_c, 'PFACH', '邮政信箱', 'string', 10, NULL, NULL, NULL, NULL, @grp_cust_addr, 6, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
-- 联系信息分组
(6, @fs_telf1_c, 'TELF1', '电话', 'string', 16, NULL, NULL, NULL, NULL, @grp_cust_contact, 1, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(6, @fs_telf2_c, 'TELF2', '电话2', 'string', 16, NULL, NULL, NULL, NULL, @grp_cust_contact, 2, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(6, @fs_telfx_c, 'TELFX', '传真', 'string', 31, NULL, NULL, NULL, NULL, @grp_cust_contact, 3, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(6, @fs_smtp_c, 'SMTP_ADDR', '邮箱', 'string', 130, NULL, NULL, NULL, NULL, @grp_cust_contact, 4, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(6, @fs_spras_c, 'SPRAS', '语言', 'string', 2, NULL, @dom_language, 'LANGUAGE', '语言', @grp_cust_contact, 5, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
-- 通用信息分组
(6, @fs_stcd1_c, 'STCD1', '税号', 'string', 16, NULL, NULL, NULL, NULL, @grp_cust_common, 1, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(6, @fs_stcd2_c, 'STCD2', '税号2', 'string', 11, NULL, NULL, NULL, NULL, @grp_cust_common, 2, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(6, @fs_status, 'STATUS', '状态', 'string', 20, NULL, @dom_status, 'STATUS', '启用状态', @grp_cust_common, 3, 1, 1, 0, 0, 0, 1, 1, 1, 'active', 'admin', NOW()),
(6, @fs_ernam, 'ERNAM', '创建人', 'string', 12, NULL, NULL, NULL, NULL, @grp_cust_common, 4, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(6, @fs_erdat, 'ERDAT', '创建日期', 'date', NULL, NULL, NULL, NULL, NULL, @grp_cust_common, 5, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(6, @fs_aenam, 'AENAM', '修改人', 'string', 12, NULL, NULL, NULL, NULL, @grp_cust_common, 6, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(6, @fs_laeda, 'LAEDA', '修改日期', 'date', NULL, NULL, NULL, NULL, NULL, @grp_cust_common, 7, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW());

SELECT '========== 客户主表字段创建完成 ==========' AS '';

-- 4.4 客户销售子表字段 (entity_id = 7)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(7, 'SALES_BASIC', '销售基本信息', 1, 2, 1, 0, 'active', 'admin', NOW());

SET @grp_cust_sales = (SELECT id FROM std_view_group WHERE entity_id = 7 AND group_code = 'SALES_BASIC');

-- 获取销售字段
SET @fs_vkorg = (SELECT id FROM std_field_standard WHERE field_code = 'VKORG');
SET @fs_vtweg = (SELECT id FROM std_field_standard WHERE field_code = 'VTWEG');
SET @fs_spart = (SELECT id FROM std_field_standard WHERE field_code = 'SPART');
SET @fs_kdgrp = (SELECT id FROM std_field_standard WHERE field_code = 'KDGRP');
SET @fs_bzirk = (SELECT id FROM std_field_standard WHERE field_code = 'BZIRK');
SET @fs_vwerk = (SELECT id FROM std_field_standard WHERE field_code = 'VWERK');
SET @fs_vsbed = (SELECT id FROM std_field_standard WHERE field_code = 'VSBED');
SET @fs_inco1_c = (SELECT id FROM std_field_standard WHERE field_code = 'INCO1_C');
SET @fs_inco2_c = (SELECT id FROM std_field_standard WHERE field_code = 'INCO2_C');
SET @fs_zterm_c = (SELECT id FROM std_field_standard WHERE field_code = 'ZTERM_C');
SET @fs_waers_c = (SELECT id FROM std_field_standard WHERE field_code = 'WAERS_C');
SET @fs_kalks = (SELECT id FROM std_field_standard WHERE field_code = 'KALKS');
SET @fs_lifsd = (SELECT id FROM std_field_standard WHERE field_code = 'LIFSD');
SET @fs_kzazu = (SELECT id FROM std_field_standard WHERE field_code = 'KZAZU');
SET @fs_ktgrd = (SELECT id FROM std_field_standard WHERE field_code = 'KTGRD');

-- 获取值域
SET @dom_sales_org = (SELECT id FROM std_value_domain WHERE domain_code = 'SALES_ORG');
SET @dom_payment_term = (SELECT id FROM std_value_domain WHERE domain_code = 'PAYMENT_TERM');
SET @dom_incoterm = (SELECT id FROM std_value_domain WHERE domain_code = 'INCOTERM');
SET @dom_customer_group = (SELECT id FROM std_value_domain WHERE domain_code = 'CUSTOMER_GROUP');

INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
(7, @fs_vkorg, 'VKORG', '销售组织', 'string', 4, NULL, @dom_sales_org, 'SALES_ORG', '销售组织', @grp_cust_sales, 1, 1, 1, 0, 0, 0, 1, 1, 1, 'active', 'admin', NOW()),
(7, @fs_vtweg, 'VTWEG', '分销渠道', 'string', 2, NULL, NULL, NULL, NULL, @grp_cust_sales, 2, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(7, @fs_spart, 'SPART', '产品组', 'string', 2, NULL, NULL, NULL, NULL, @grp_cust_sales, 3, 1, 0, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(7, @fs_kdgrp, 'KDGRP', '客户组', 'string', 2, NULL, @dom_customer_group, 'CUSTOMER_GROUP', '客户组', @grp_cust_sales, 4, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(7, @fs_bzirk, 'BZIRK', '销售地区', 'string', 4, NULL, NULL, NULL, NULL, @grp_cust_sales, 5, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(7, @fs_vwerk, 'VWERK', '交货工厂', 'string', 4, NULL, NULL, NULL, NULL, @grp_cust_sales, 6, 1, 0, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(7, @fs_vsbed, 'VSBED', '装运条件', 'string', 2, NULL, NULL, NULL, NULL, @grp_cust_sales, 7, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(7, @fs_inco1_c, 'INCO1', '国际贸易条款1', 'string', 3, NULL, @dom_incoterm, 'INCOTERM', '国际贸易术语', @grp_cust_sales, 8, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(7, @fs_inco2_c, 'INCO2', '国际贸易条款2', 'string', 28, NULL, NULL, NULL, NULL, @grp_cust_sales, 9, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(7, @fs_zterm_c, 'ZTERM', '付款条件', 'string', 4, NULL, @dom_payment_term, 'PAYMENT_TERM', '付款条款', @grp_cust_sales, 10, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(7, @fs_waers_c, 'WAERS', '货币', 'string', 5, NULL, @dom_currency, 'CURRENCY', '货币', @grp_cust_sales, 11, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(7, @fs_kalks, 'KALKS', '客户定价过程', 'string', 1, NULL, NULL, NULL, NULL, @grp_cust_sales, 12, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(7, @fs_lifsd, 'LIFSD', '交货冻结', 'string', 2, NULL, NULL, NULL, NULL, @grp_cust_sales, 13, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(7, @fs_kzazu, 'KZAZU', '订单组合', 'boolean', NULL, NULL, @dom_yes_no, 'YES_NO', '是/否', @grp_cust_sales, 14, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(7, @fs_ktgrd, 'KTGRD', '客户科目组', 'string', 2, NULL, NULL, NULL, NULL, @grp_cust_sales, 15, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW());

SELECT '========== 客户销售子表字段创建完成 ==========' AS '';

-- 4.5 客户财务子表字段 (entity_id = 8)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(8, 'CUST_ACCOUNT_BASIC', '财务基本信息', 1, 2, 1, 0, 'active', 'admin', NOW());

SET @grp_cust_account = (SELECT id FROM std_view_group WHERE entity_id = 8 AND group_code = 'CUST_ACCOUNT_BASIC');

-- 获取财务字段
SET @fs_akont_c = (SELECT id FROM std_field_standard WHERE field_code = 'AKONT_C');
SET @fs_zterm_fc = (SELECT id FROM std_field_standard WHERE field_code = 'ZTERM_FC');
SET @fs_fdgrv_c = (SELECT id FROM std_field_standard WHERE field_code = 'FDGRV_C');
SET @fs_zuawa_c = (SELECT id FROM std_field_standard WHERE field_code = 'ZUAWA_C');
SET @fs_busab = (SELECT id FROM std_field_standard WHERE field_code = 'BUSAB');
SET @fs_xausz_c = (SELECT id FROM std_field_standard WHERE field_code = 'XAUSZ_C');

-- 获取值域
SET @dom_company_code = (SELECT id FROM std_value_domain WHERE domain_code = 'COMPANY_CODE');

INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
(8, NULL, 'BUKRS', '公司代码', 'string', 4, NULL, @dom_company_code, 'COMPANY_CODE', '公司代码', @grp_cust_account, 1, 1, 1, 0, 0, 0, 1, 1, 1, 'active', 'admin', NOW()),
(8, @fs_akont_c, 'AKONT', '统驭科目', 'string', 10, NULL, NULL, NULL, NULL, @grp_cust_account, 2, 1, 1, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(8, @fs_zterm_fc, 'ZTERM', '付款条件', 'string', 4, NULL, @dom_payment_term, 'PAYMENT_TERM', '付款条款', @grp_cust_account, 3, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(8, @fs_fdgrv_c, 'FDGRV', '财务组', 'string', 10, NULL, NULL, NULL, NULL, @grp_cust_account, 4, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(8, @fs_zuawa_c, 'ZUAWA', '清账规则', 'string', 3, NULL, NULL, NULL, NULL, @grp_cust_account, 5, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(8, @fs_busab, 'BUSAB', '会计编号', 'string', 2, NULL, NULL, NULL, NULL, @grp_cust_account, 6, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(8, @fs_xausz_c, 'XAUSZ', '一次性付款', 'boolean', NULL, NULL, @dom_yes_no, 'YES_NO', '是/否', @grp_cust_account, 7, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW());

SELECT '========== 客户财务子表字段创建完成 ==========' AS '';

-- ==========================================
-- 5. 供应商视图 (view_id = 3)
-- ==========================================

-- 5.1 供应商主表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, table_name, sort, min_rows, max_rows, enable_add, enable_delete, enable_copy, enable_sort, status, created_by, created_at, description) VALUES
(3, 'VENDOR_MAIN', '供应商基本信息', 'main', 'mdm_vendor_main', 0, 1, 1, 0, 0, 0, 0, 'active', 'admin', NOW(), '供应商主数据基本信息');

-- 5.2 供应商子表
INSERT INTO std_view_entity (view_id, entity_code, entity_name, entity_type, table_name, sort, min_rows, max_rows, enable_add, enable_delete, enable_copy, enable_sort, status, created_by, created_at, description) VALUES
(3, 'VENDOR_PURCHASE', '采购信息', 'sub', 'mdm_vendor_purchase', 1, 0, 10, 1, 1, 1, 1, 'active', 'admin', NOW(), '供应商采购组织信息，每个采购组织一条记录'),
(3, 'VENDOR_ACCOUNT', '财务信息', 'sub', 'mdm_vendor_account', 2, 0, 10, 1, 1, 1, 1, 'active', 'admin', NOW(), '供应商财务信息，每个公司代码一条记录');

SELECT '========== 供应商视图实体创建完成 ==========' AS '';

-- 5.3 供应商主表字段分组 (entity_id = 9)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(9, 'VEND_BASIC', '基本信息', 1, 2, 1, 0, 'active', 'admin', NOW()),
(9, 'VEND_ADDRESS', '地址信息', 2, 2, 1, 0, 'active', 'admin', NOW()),
(9, 'VEND_CONTACT', '联系信息', 3, 2, 1, 0, 'active', 'admin', NOW()),
(9, 'VEND_COMMON', '通用信息', 4, 2, 1, 0, 'active', 'admin', NOW());

-- 获取供应商字段
SET @fs_lifnr = (SELECT id FROM std_field_standard WHERE field_code = 'LIFNR');
SET @fs_name1 = (SELECT id FROM std_field_standard WHERE field_code = 'NAME1');
SET @fs_name2 = (SELECT id FROM std_field_standard WHERE field_code = 'NAME2');
SET @fs_name3 = (SELECT id FROM std_field_standard WHERE field_code = 'NAME3');
SET @fs_name4 = (SELECT id FROM std_field_standard WHERE field_code = 'NAME4');
SET @fs_ort01 = (SELECT id FROM std_field_standard WHERE field_code = 'ORT01');
SET @fs_pstlz = (SELECT id FROM std_field_standard WHERE field_code = 'PSTLZ');
SET @fs_regio = (SELECT id FROM std_field_standard WHERE field_code = 'REGIO');
SET @fs_land1 = (SELECT id FROM std_field_standard WHERE field_code = 'LAND1');
SET @fs_stras = (SELECT id FROM std_field_standard WHERE field_code = 'STRAS');
SET @fs_pfach = (SELECT id FROM std_field_standard WHERE field_code = 'PFACH');
SET @fs_telf1 = (SELECT id FROM std_field_standard WHERE field_code = 'TELF1');
SET @fs_telf2 = (SELECT id FROM std_field_standard WHERE field_code = 'TELF2');
SET @fs_telfx = (SELECT id FROM std_field_standard WHERE field_code = 'TELFX');
SET @fs_smtp = (SELECT id FROM std_field_standard WHERE field_code = 'SMTP_ADDR');
SET @fs_spras = (SELECT id FROM std_field_standard WHERE field_code = 'SPRAS');
SET @fs_stcd1 = (SELECT id FROM std_field_standard WHERE field_code = 'STCD1');
SET @fs_stcd2 = (SELECT id FROM std_field_standard WHERE field_code = 'STCD2');
SET @fs_stcd3 = (SELECT id FROM std_field_standard WHERE field_code = 'STCD3');
SET @fs_stcd4 = (SELECT id FROM std_field_standard WHERE field_code = 'STCD4');

-- 获取分组ID
SET @grp_vend_basic = (SELECT id FROM std_view_group WHERE entity_id = 9 AND group_code = 'VEND_BASIC');
SET @grp_vend_addr = (SELECT id FROM std_view_group WHERE entity_id = 9 AND group_code = 'VEND_ADDRESS');
SET @grp_vend_contact = (SELECT id FROM std_view_group WHERE entity_id = 9 AND group_code = 'VEND_CONTACT');
SET @grp_vend_common = (SELECT id FROM std_view_group WHERE entity_id = 9 AND group_code = 'VEND_COMMON');

-- 插入供应商主表字段
INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
-- 基本信息分组
(9, @fs_lifnr, 'LIFNR', '供应商编号', 'string', 10, NULL, NULL, NULL, NULL, @grp_vend_basic, 1, 1, 1, 0, 0, 1, 1, 1, 1, 'active', 'admin', NOW()),
(9, @fs_name1, 'NAME1', '供应商名称', 'string', 35, NULL, NULL, NULL, NULL, @grp_vend_basic, 2, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(9, @fs_name2, 'NAME2', '供应商名称2', 'string', 35, NULL, NULL, NULL, NULL, @grp_vend_basic, 3, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(9, @fs_name3, 'NAME3', '供应商名称3', 'string', 35, NULL, NULL, NULL, NULL, @grp_vend_basic, 4, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(9, @fs_name4, 'NAME4', '供应商名称4', 'string', 35, NULL, NULL, NULL, NULL, @grp_vend_basic, 5, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
-- 地址信息分组
(9, @fs_land1, 'LAND1', '国家', 'string', 3, NULL, @dom_country, 'COUNTRY', '国家代码', @grp_vend_addr, 1, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(9, @fs_regio, 'REGIO', '地区', 'string', 3, NULL, @dom_province, 'PROVINCE', '省份', @grp_vend_addr, 2, 1, 0, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(9, @fs_ort01, 'ORT01', '城市', 'string', 35, NULL, NULL, NULL, NULL, @grp_vend_addr, 3, 1, 0, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(9, @fs_stras, 'STRAS', '街道', 'string', 35, NULL, NULL, NULL, NULL, @grp_vend_addr, 4, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(9, @fs_pstlz, 'PSTLZ', '邮政编码', 'string', 10, NULL, NULL, NULL, NULL, @grp_vend_addr, 5, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(9, @fs_pfach, 'PFACH', '邮政信箱', 'string', 10, NULL, NULL, NULL, NULL, @grp_vend_addr, 6, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
-- 联系信息分组
(9, @fs_telf1, 'TELF1', '电话', 'string', 16, NULL, NULL, NULL, NULL, @grp_vend_contact, 1, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(9, @fs_telf2, 'TELF2', '电话2', 'string', 16, NULL, NULL, NULL, NULL, @grp_vend_contact, 2, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(9, @fs_telfx, 'TELFX', '传真', 'string', 31, NULL, NULL, NULL, NULL, @grp_vend_contact, 3, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(9, @fs_smtp, 'SMTP_ADDR', '邮箱', 'string', 130, NULL, NULL, NULL, NULL, @grp_vend_contact, 4, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(9, @fs_spras, 'SPRAS', '语言', 'string', 2, NULL, @dom_language, 'LANGUAGE', '语言', @grp_vend_contact, 5, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
-- 通用信息分组
(9, @fs_stcd1, 'STCD1', '税号', 'string', 16, NULL, NULL, NULL, NULL, @grp_vend_common, 1, 1, 0, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(9, @fs_stcd2, 'STCD2', '税号2', 'string', 11, NULL, NULL, NULL, NULL, @grp_vend_common, 2, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(9, @fs_stcd3, 'STCD3', '税号3', 'string', 18, NULL, NULL, NULL, NULL, @grp_vend_common, 3, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(9, @fs_stcd4, 'STCD4', '税号4', 'string', 18, NULL, NULL, NULL, NULL, @grp_vend_common, 4, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(9, @fs_status, 'STATUS', '状态', 'string', 20, NULL, @dom_status, 'STATUS', '启用状态', @grp_vend_common, 5, 1, 1, 0, 0, 0, 1, 1, 1, 'active', 'admin', NOW()),
(9, @fs_ernam, 'ERNAM', '创建人', 'string', 12, NULL, NULL, NULL, NULL, @grp_vend_common, 6, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(9, @fs_erdat, 'ERDAT', '创建日期', 'date', NULL, NULL, NULL, NULL, NULL, @grp_vend_common, 7, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(9, @fs_aenam, 'AENAM', '修改人', 'string', 12, NULL, NULL, NULL, NULL, @grp_vend_common, 8, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(9, @fs_laeda, 'LAEDA', '修改日期', 'date', NULL, NULL, NULL, NULL, NULL, @grp_vend_common, 9, 1, 0, 1, 0, 0, 0, 0, 0, 'active', 'admin', NOW());

SELECT '========== 供应商主表字段创建完成 ==========' AS '';

-- 5.4 供应商采购子表字段 (entity_id = 10)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(10, 'VEND_PURCHASE_BASIC', '采购基本信息', 1, 2, 1, 0, 'active', 'admin', NOW());

SET @grp_vend_purchase = (SELECT id FROM std_view_group WHERE entity_id = 10 AND group_code = 'VEND_PURCHASE_BASIC');

-- 获取采购字段
SET @fs_ekorg_v = (SELECT id FROM std_field_standard WHERE field_code = 'EKORG_V');
SET @fs_ekgrp_v = (SELECT id FROM std_field_standard WHERE field_code = 'EKGRP_V');
SET @fs_versg = (SELECT id FROM std_field_standard WHERE field_code = 'VERSG');
SET @fs_waers_v = (SELECT id FROM std_field_standard WHERE field_code = 'WAERS_V');
SET @fs_inco1 = (SELECT id FROM std_field_standard WHERE field_code = 'INCO1');
SET @fs_inco2 = (SELECT id FROM std_field_standard WHERE field_code = 'INCO2');
SET @fs_zterm_v = (SELECT id FROM std_field_standard WHERE field_code = 'ZTERM_V');
SET @fs_bmein = (SELECT id FROM std_field_standard WHERE field_code = 'BMEIN');
SET @fs_umren = (SELECT id FROM std_field_standard WHERE field_code = 'UMREN');
SET @fs_umrez = (SELECT id FROM std_field_standard WHERE field_code = 'UMREZ');
SET @fs_webru = (SELECT id FROM std_field_standard WHERE field_code = 'WEBRU');
SET @fs_kalsk = (SELECT id FROM std_field_standard WHERE field_code = 'KALSK');
SET @fs_meprf = (SELECT id FROM std_field_standard WHERE field_code = 'MEPRF');

INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
(10, @fs_ekorg_v, 'EKORG', '采购组织', 'string', 4, NULL, @dom_purchase_org, 'PURCHASE_ORG', '采购组织', @grp_vend_purchase, 1, 1, 1, 0, 0, 0, 1, 1, 1, 'active', 'admin', NOW()),
(10, @fs_ekgrp_v, 'EKGRP', '采购组', 'string', 3, NULL, NULL, NULL, NULL, @grp_vend_purchase, 2, 1, 1, 0, 0, 0, 1, 1, 0, 'active', 'admin', NOW()),
(10, @fs_versg, 'VERSG', '供应商组', 'string', 1, NULL, NULL, NULL, NULL, @grp_vend_purchase, 3, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(10, @fs_waers_v, 'WAERS', '订单货币', 'string', 5, NULL, @dom_currency, 'CURRENCY', '货币', @grp_vend_purchase, 4, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(10, @fs_inco1, 'INCO1', '国际贸易条款1', 'string', 3, NULL, @dom_incoterm, 'INCOTERM', '国际贸易术语', @grp_vend_purchase, 5, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(10, @fs_inco2, 'INCO2', '国际贸易条款2', 'string', 28, NULL, NULL, NULL, NULL, @grp_vend_purchase, 6, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(10, @fs_zterm_v, 'ZTERM', '付款条件', 'string', 4, NULL, @dom_payment_term, 'PAYMENT_TERM', '付款条款', @grp_vend_purchase, 7, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(10, @fs_bmein, 'BMEIN', '订单单位', 'string', 3, NULL, @dom_base_unit, 'BASE_UNIT', '基本单位', @grp_vend_purchase, 8, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(10, @fs_umren, 'UMREN', '转换分母', 'integer', NULL, NULL, NULL, NULL, NULL, @grp_vend_purchase, 9, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(10, @fs_umrez, 'UMREZ', '转换分子', 'integer', NULL, NULL, NULL, NULL, NULL, @grp_vend_purchase, 10, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(10, @fs_webru, 'WEBRU', '收货点', 'string', 4, NULL, NULL, NULL, NULL, @grp_vend_purchase, 11, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(10, @fs_kalsk, 'KALSK', '评估组', 'string', 2, NULL, NULL, NULL, NULL, @grp_vend_purchase, 12, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(10, @fs_meprf, 'MEPRF', '价格日期控制', 'string', 1, NULL, NULL, NULL, NULL, @grp_vend_purchase, 13, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW());

SELECT '========== 供应商采购子表字段创建完成 ==========' AS '';

-- 5.5 供应商财务子表字段 (entity_id = 11)
INSERT INTO std_view_group (entity_id, group_code, group_name, sort, column_count, collapsible, default_collapsed, status, created_by, created_at) VALUES
(11, 'VEND_ACCOUNT_BASIC', '财务基本信息', 1, 2, 1, 0, 'active', 'admin', NOW());

SET @grp_vend_account = (SELECT id FROM std_view_group WHERE entity_id = 11 AND group_code = 'VEND_ACCOUNT_BASIC');

-- 获取财务字段
SET @fs_akont = (SELECT id FROM std_field_standard WHERE field_code = 'AKONT');
SET @fs_zterm_f = (SELECT id FROM std_field_standard WHERE field_code = 'ZTERM_F');
SET @fs_fdgrv = (SELECT id FROM std_field_standard WHERE field_code = 'FDGRV');
SET @fs_zuawa = (SELECT id FROM std_field_standard WHERE field_code = 'ZUAWA');
SET @fs_qsskz = (SELECT id FROM std_field_standard WHERE field_code = 'QSSKZ');
SET @fs_qsznr = (SELECT id FROM std_field_standard WHERE field_code = 'QSZNR');
SET @fs_reprf = (SELECT id FROM std_field_standard WHERE field_code = 'REPRF');
SET @fs_xausz = (SELECT id FROM std_field_standard WHERE field_code = 'XAUSZ');

INSERT INTO std_view_field (entity_id, field_standard_id, field_code, field_name, field_type, length, precision_val, domain_id, domain_code, domain_name, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_unique, is_query, is_query_result, is_sortable, status, created_by, created_at) VALUES
(11, NULL, 'BUKRS', '公司代码', 'string', 4, NULL, @dom_company_code, 'COMPANY_CODE', '公司代码', @grp_vend_account, 1, 1, 1, 0, 0, 0, 1, 1, 1, 'active', 'admin', NOW()),
(11, @fs_akont, 'AKONT', '统驭科目', 'string', 10, NULL, NULL, NULL, NULL, @grp_vend_account, 2, 1, 1, 0, 0, 0, 0, 1, 0, 'active', 'admin', NOW()),
(11, @fs_zterm_f, 'ZTERM', '付款条件', 'string', 4, NULL, @dom_payment_term, 'PAYMENT_TERM', '付款条款', @grp_vend_account, 3, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(11, @fs_fdgrv, 'FDGRV', '财务组', 'string', 10, NULL, NULL, NULL, NULL, @grp_vend_account, 4, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(11, @fs_zuawa, 'ZUAWA', '清账规则', 'string', 3, NULL, NULL, NULL, NULL, @grp_vend_account, 5, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(11, @fs_qsskz, 'QSSKZ', '预扣税类型', 'string', 2, NULL, NULL, NULL, NULL, @grp_vend_account, 6, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(11, @fs_qsznr, 'QSZNR', '预扣税证明', 'string', 10, NULL, NULL, NULL, NULL, @grp_vend_account, 7, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(11, @fs_reprf, 'REPRF', '核对标记', 'boolean', NULL, NULL, @dom_yes_no, 'YES_NO', '是/否', @grp_vend_account, 8, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW()),
(11, @fs_xausz, 'XAUSZ', '一次性付款', 'boolean', NULL, NULL, @dom_yes_no, 'YES_NO', '是/否', @grp_vend_account, 9, 1, 0, 0, 0, 0, 0, 0, 0, 'active', 'admin', NOW());

SELECT '========== 供应商财务子表字段创建完成 ==========' AS '';

-- ==========================================
-- 6. 验证结果
-- ==========================================
SELECT '========== 初始化完成 ==========' AS '';
SELECT '视图统计' AS 类型, COUNT(*) AS 数量 FROM std_view
UNION ALL
SELECT '视图实体统计' AS 类型, COUNT(*) AS 数量 FROM std_view_entity
UNION ALL
SELECT '视图分组统计' AS 类型, COUNT(*) AS 数量 FROM std_view_group
UNION ALL
SELECT '视图字段统计' AS 类型, COUNT(*) AS 数量 FROM std_view_field;

-- 显示视图详情
SELECT '========== 视图详情 ==========' AS '';
SELECT v.id, v.view_code, v.view_name, v.status, v.description FROM std_view v;

-- 显示各视图的实体数量
SELECT '========== 各视图实体数量 ==========' AS '';
SELECT v.view_name AS 视图名称,
       COUNT(e.id) AS 实体数量,
       SUM(CASE WHEN e.entity_type = 'main' THEN 1 ELSE 0 END) AS 主表数量,
       SUM(CASE WHEN e.entity_type = 'sub' THEN 1 ELSE 0 END) AS 子表数量
FROM std_view v
LEFT JOIN std_view_entity e ON e.view_id = v.id
GROUP BY v.id, v.view_name;

SELECT '初始化完成！已创建物料视图、客户视图、供应商视图三个视图，状态均为草稿' AS message;
