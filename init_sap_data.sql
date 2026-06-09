-- ============================================
-- MDM系统数据初始化脚本
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
(5, 'PLANNING', '计划信息', NULL, 5, 'active', '计划相关字段', 'admin', NOW(), 'admin', NOW()),
(6, 'WAREHOUSE', '仓库信息', NULL, 6, 'active', '仓库管理字段', 'admin', NOW(), 'admin', NOW()),
(7, 'QUALITY', '质量信息', NULL, 7, 'active', '质量管理字段', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 3. 初始化值域
-- ============================================
INSERT INTO std_value_domain (id, domain_code, domain_name, domain_type, status, description, created_by, created_at, updated_by, updated_at) VALUES
(1, 'MATL_TYPE', '物料类型', 'list', 'active', '物料类型', 'admin', NOW(), 'admin', NOW()),
(2, 'IND_SECTOR', '行业领域', 'list', 'active', '行业领域', 'admin', NOW(), 'admin', NOW()),
(3, 'BASE_UOM', '基本单位', 'list', 'active', '计量单位', 'admin', NOW(), 'admin', NOW()),
(4, 'MATL_GROUP', '物料组', 'list', 'active', '物料分组', 'admin', NOW(), 'admin', NOW()),
(5, 'PUR_GROUP', '采购组', 'list', 'active', '采购组', 'admin', NOW(), 'admin', NOW()),
(6, 'MRP_TYPE', 'MRP类型', 'list', 'active', 'MRP类型', 'admin', NOW(), 'admin', NOW()),
(7, 'PRICE_CTRL', '价格控制', 'list', 'active', '价格控制方式', 'admin', NOW(), 'admin', NOW()),
(8, 'COUNTRY', '国家', 'list', 'active', '国家代码', 'admin', NOW(), 'admin', NOW()),
(9, 'CURRENCY', '货币', 'list', 'active', '货币代码', 'admin', NOW(), 'admin', NOW()),
(10, 'PAYMENT_TERM', '付款条件', 'list', 'active', '付款条件', 'admin', NOW(), 'admin', NOW()),
(11, 'INCOTERM', '国际贸易条款', 'list', 'active', '国际贸易条款', 'admin', NOW(), 'admin', NOW()),
(12, 'CUST_GROUP', '客户组', 'list', 'active', '客户分组', 'admin', NOW(), 'admin', NOW()),
(13, 'SALES_ORG', '销售组织', 'list', 'active', '销售组织', 'admin', NOW(), 'admin', NOW()),
(14, 'DISTR_CHAN', '分销渠道', 'list', 'active', '分销渠道', 'admin', NOW(), 'admin', NOW()),
(15, 'YES_NO', '是/否', 'list', 'active', '是/否', 'admin', NOW(), 'admin', NOW());

-- 值域项
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
-- 物料类型
(1, 1, 'ROH', '原材料', 1, 'active', 'admin', NOW()),
(2, 1, 'HALB', '半成品', 2, 'active', 'admin', NOW()),
(3, 1, 'FERT', '成品', 3, 'active', 'admin', NOW()),
(4, 1, 'HAWA', '贸易商品', 4, 'active', 'admin', NOW()),
(5, 1, 'DIEN', '服务', 5, 'active', 'admin', NOW()),
(6, 1, 'NLAG', '非库存物料', 6, 'active', 'admin', NOW()),
-- 行业领域
(7, 2, 'M', '机械工程', 1, 'active', 'admin', NOW()),
(8, 2, 'C', '化学工业', 2, 'active', 'admin', NOW()),
(9, 2, 'P', '医药工业', 3, 'active', 'admin', NOW()),
(10, 2, 'E', '电子工业', 4, 'active', 'admin', NOW()),
-- 单位
(11, 3, 'EA', '件', 1, 'active', 'admin', NOW()),
(12, 3, 'KG', '千克', 2, 'active', 'admin', NOW()),
(13, 3, 'M', '米', 3, 'active', 'admin', NOW()),
(14, 3, 'L', '升', 4, 'active', 'admin', NOW()),
(15, 3, 'PC', '个', 5, 'active', 'admin', NOW()),
-- 价格控制
(16, 7, 'S', '标准价格', 1, 'active', 'admin', NOW()),
(17, 7, 'V', '移动平均价', 2, 'active', 'admin', NOW()),
-- 是/否
(18, 15, 'Y', '是', 1, 'active', 'admin', NOW()),
(19, 15, 'N', '否', 2, 'active', 'admin', NOW());

-- ============================================
-- 4. 初始化字段标准
-- ============================================
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, category_id, domain_id, is_required, default_value, description, status, created_by, created_at, updated_by, updated_at) VALUES
-- 物料基本字段
(1, 'MATNR', '物料编号', 'string', 18, NULL, 1, NULL, true, NULL, '物料唯一标识', 'active', 'admin', NOW(), 'admin', NOW()),
(2, 'MAKTX', '物料描述', 'string', 40, NULL, 1, NULL, true, NULL, '物料名称描述', 'active', 'admin', NOW(), 'admin', NOW()),
(3, 'MTART', '物料类型', 'string', 4, NULL, 1, 1, true, NULL, '物料类型代码', 'active', 'admin', NOW(), 'admin', NOW()),
(4, 'MBRSH', '行业领域', 'string', 1, NULL, 1, 2, true, NULL, '行业领域', 'active', 'admin', NOW(), 'admin', NOW()),
(5, 'MATKL', '物料组', 'string', 9, NULL, 1, 4, false, NULL, '物料分组', 'active', 'admin', NOW(), 'admin', NOW()),
(6, 'MEINS', '基本单位', 'string', 3, NULL, 1, 3, true, NULL, '基本计量单位', 'active', 'admin', NOW(), 'admin', NOW()),
(7, 'BSTME', '采购单位', 'string', 3, NULL, 2, 3, false, NULL, '采购计量单位', 'active', 'admin', NOW(), 'admin', NOW()),
(8, 'GEWEI', '重量单位', 'string', 3, NULL, 1, 3, false, NULL, '重量单位', 'active', 'admin', NOW(), 'admin', NOW()),
(9, 'BRGEW', '毛重', 'decimal', 13, 3, 1, NULL, false, NULL, '毛重', 'active', 'admin', NOW(), 'admin', NOW()),
(10, 'NTGEW', '净重', 'decimal', 13, 3, 1, NULL, false, NULL, '净重', 'active', 'admin', NOW(), 'admin', NOW()),
(11, 'VOLUM', '体积', 'decimal', 13, 3, 1, NULL, false, NULL, '体积', 'active', 'admin', NOW(), 'admin', NOW()),
(12, 'VOLEH', '体积单位', 'string', 3, NULL, 1, 3, false, NULL, '体积单位', 'active', 'admin', NOW(), 'admin', NOW()),

-- 采购字段
(13, 'EKGRP', '采购组', 'string', 3, NULL, 2, 5, false, NULL, '采购组代码', 'active', 'admin', NOW(), 'admin', NOW()),
(14, 'EKORG', '采购组织', 'string', 4, NULL, 2, NULL, false, NULL, '采购组织', 'active', 'admin', NOW(), 'admin', NOW()),
(15, 'PLIFZ', '计划交货时间', 'integer', NULL, NULL, 2, NULL, false, NULL, '计划交货天数', 'active', 'admin', NOW(), 'admin', NOW()),
(16, 'WEBAZ', '收货处理时间', 'integer', NULL, NULL, 2, NULL, false, NULL, '收货处理天数', 'active', 'admin', NOW(), 'admin', NOW()),
(17, 'MINLS', '最小批量', 'decimal', 13, 3, 2, NULL, false, NULL, '最小订单批量', 'active', 'admin', NOW(), 'admin', NOW()),
(18, 'MAXLS', '最大批量', 'decimal', 13, 3, 2, NULL, false, NULL, '最大订单批量', 'active', 'admin', NOW(), 'admin', NOW()),
(19, 'BSTMI', '最小包装量', 'decimal', 13, 3, 2, NULL, false, NULL, '最小包装数量', 'active', 'admin', NOW(), 'admin', NOW()),
(20, 'KORDS', '供应商编号', 'string', 10, NULL, 2, NULL, false, NULL, '供应商编号', 'active', 'admin', NOW(), 'admin', NOW()),

-- MRP字段
(21, 'DISMM', 'MRP类型', 'string', 2, NULL, 5, 6, false, NULL, 'MRP类型', 'active', 'admin', NOW(), 'admin', NOW()),
(22, 'DISPO', 'MRP控制者', 'string', 3, NULL, 5, NULL, false, NULL, 'MRP控制者', 'active', 'admin', NOW(), 'admin', NOW()),
(23, 'DISLS', '批量大小', 'string', 2, NULL, 5, NULL, false, NULL, '批量大小', 'active', 'admin', NOW(), 'admin', NOW()),
(24, 'BESKZ', '采购类型', 'string', 1, NULL, 5, NULL, false, NULL, '采购类型', 'active', 'admin', NOW(), 'admin', NOW()),
(25, 'SOBSL', '特殊采购', 'string', 2, NULL, 5, NULL, false, NULL, '特殊采购类型', 'active', 'admin', NOW(), 'admin', NOW()),
(26, 'MINBE', '安全库存', 'decimal', 13, 3, 5, NULL, false, NULL, '安全库存', 'active', 'admin', NOW(), 'admin', NOW()),
(27, 'EISBE', '最小安全库存', 'decimal', 13, 3, 5, NULL, false, NULL, '最小安全库存', 'active', 'admin', NOW(), 'admin', NOW()),
(28, 'BSTFE', '固定批量', 'decimal', 13, 3, 5, NULL, false, NULL, '固定订货批量', 'active', 'admin', NOW(), 'admin', NOW()),
(29, 'WZEIT', '计划边际码', 'integer', NULL, NULL, 5, NULL, false, NULL, '计划边际码', 'active', 'admin', NOW(), 'admin', NOW()),

-- 财务字段
(30, 'BKLAS', '评估类', 'string', 4, NULL, 4, NULL, false, NULL, '评估类', 'active', 'admin', NOW(), 'admin', NOW()),
(31, 'VPRSV', '价格控制', 'string', 1, NULL, 4, 7, false, NULL, '价格控制', 'active', 'admin', NOW(), 'admin', NOW()),
(32, 'VERPR', '移动平均价', 'decimal', 15, 2, 4, NULL, false, NULL, '移动平均价', 'active', 'admin', NOW(), 'admin', NOW()),
(33, 'STPRS', '标准价格', 'decimal', 15, 2, 4, NULL, false, NULL, '标准价格', 'active', 'admin', NOW(), 'admin', NOW()),
(34, 'PEINH', '价格单位', 'integer', NULL, NULL, 4, NULL, false, NULL, '价格单位', 'active', 'admin', NOW(), 'admin', NOW()),
(35, 'WAERS', '货币', 'string', 5, NULL, 4, 9, false, NULL, '货币代码', 'active', 'admin', NOW(), 'admin', NOW()),

-- 供应商字段
(36, 'LIFNR', '供应商编号', 'string', 10, NULL, 1, NULL, true, NULL, '供应商唯一标识', 'active', 'admin', NOW(), 'admin', NOW()),
(37, 'NAME1', '供应商名称', 'string', 35, NULL, 1, NULL, true, NULL, '供应商名称', 'active', 'admin', NOW(), 'admin', NOW()),
(38, 'NAME2', '供应商名称2', 'string', 35, NULL, 1, NULL, false, NULL, '供应商名称2', 'active', 'admin', NOW(), 'admin', NOW()),
(39, 'ORT01', '城市', 'string', 35, NULL, 1, NULL, false, NULL, '城市', 'active', 'admin', NOW(), 'admin', NOW()),
(40, 'LAND1', '国家', 'string', 3, NULL, 1, 8, false, NULL, '国家代码', 'active', 'admin', NOW(), 'admin', NOW()),
(41, 'REGIO', '地区', 'string', 3, NULL, 1, NULL, false, NULL, '地区/省份', 'active', 'admin', NOW(), 'admin', NOW()),
(42, 'STRAS', '街道', 'string', 35, NULL, 1, NULL, false, NULL, '街道地址', 'active', 'admin', NOW(), 'admin', NOW()),
(43, 'PSTLZ', '邮政编码', 'string', 10, NULL, 1, NULL, false, NULL, '邮政编码', 'active', 'admin', NOW(), 'admin', NOW()),
(44, 'TELF1', '电话', 'string', 16, NULL, 1, NULL, false, NULL, '电话号码', 'active', 'admin', NOW(), 'admin', NOW()),
(45, 'TELF2', '电话2', 'string', 16, NULL, 1, NULL, false, NULL, '电话号码2', 'active', 'admin', NOW(), 'admin', NOW()),
(46, 'TELFX', '传真', 'string', 31, NULL, 1, NULL, false, NULL, '传真号码', 'active', 'admin', NOW(), 'admin', NOW()),
(47, 'STCD1', '税号', 'string', 16, NULL, 1, NULL, false, NULL, '税号', 'active', 'admin', NOW(), 'admin', NOW()),
(48, 'STCD2', '税号2', 'string', 11, NULL, 1, NULL, false, NULL, '税号2', 'active', 'admin', NOW(), 'admin', NOW()),

-- 供应商采购数据
(49, 'WAERS_V', '订单货币', 'string', 5, NULL, 2, 9, false, NULL, '订单货币', 'active', 'admin', NOW(), 'admin', NOW()),
(50, 'ZTERM_V', '付款条件', 'string', 4, NULL, 2, 10, false, NULL, '付款条件', 'active', 'admin', NOW(), 'admin', NOW()),
(51, 'INCO1', '国际贸易条款1', 'string', 3, NULL, 2, 11, false, NULL, '国际贸易条款', 'active', 'admin', NOW(), 'admin', NOW()),
(52, 'INCO2', '国际贸易条款2', 'string', 28, NULL, 2, NULL, false, NULL, '国际贸易条款地点', 'active', 'admin', NOW(), 'admin', NOW()),
(53, 'KALSK', '评估组', 'string', 2, NULL, 2, NULL, false, NULL, '供应商评估组', 'active', 'admin', NOW(), 'admin', NOW()),
(54, 'VERSG', '供应商组', 'string', 1, NULL, 2, NULL, false, NULL, '供应商组', 'active', 'admin', NOW(), 'admin', NOW()),

-- 供应商财务数据
(55, 'AKONT', '统驭科目', 'string', 10, NULL, 4, NULL, false, NULL, '统驭科目', 'active', 'admin', NOW(), 'admin', NOW()),
(56, 'ZTERM_F', '付款条件财务', 'string', 4, NULL, 4, 10, false, NULL, '付款条件', 'active', 'admin', NOW(), 'admin', NOW()),
(57, 'FDGRV', '财务组', 'string', 10, NULL, 4, NULL, false, NULL, '财务组', 'active', 'admin', NOW(), 'admin', NOW()),
(58, 'REPRF', '核对标记', 'string', 1, NULL, 4, NULL, false, NULL, '核对标记', 'active', 'admin', NOW(), 'admin', NOW()),

-- 客户字段
(59, 'KUNNR', '客户编号', 'string', 10, NULL, 1, NULL, true, NULL, '客户唯一标识', 'active', 'admin', NOW(), 'admin', NOW()),
(60, 'NAME1_C', '客户名称', 'string', 35, NULL, 1, NULL, true, NULL, '客户名称', 'active', 'admin', NOW(), 'admin', NOW()),
(61, 'NAME2_C', '客户名称2', 'string', 35, NULL, 1, NULL, false, NULL, '客户名称2', 'active', 'admin', NOW(), 'admin', NOW()),
(62, 'ORT01_C', '城市客户', 'string', 35, NULL, 1, NULL, false, NULL, '城市', 'active', 'admin', NOW(), 'admin', NOW()),
(63, 'LAND1_C', '国家客户', 'string', 3, NULL, 1, 8, false, NULL, '国家代码', 'active', 'admin', NOW(), 'admin', NOW()),
(64, 'REGIO_C', '地区客户', 'string', 3, NULL, 1, NULL, false, NULL, '地区/省份', 'active', 'admin', NOW(), 'admin', NOW()),
(65, 'STRAS_C', '街道客户', 'string', 35, NULL, 1, NULL, false, NULL, '街道地址', 'active', 'admin', NOW(), 'admin', NOW()),
(66, 'PSTLZ_C', '邮政编码客户', 'string', 10, NULL, 1, NULL, false, NULL, '邮政编码', 'active', 'admin', NOW(), 'admin', NOW()),
(67, 'TELF1_C', '电话客户', 'string', 16, NULL, 1, NULL, false, NULL, '电话号码', 'active', 'admin', NOW(), 'admin', NOW()),
(68, 'TELFX_C', '传真客户', 'string', 31, NULL, 1, NULL, false, NULL, '传真号码', 'active', 'admin', NOW(), 'admin', NOW()),
(69, 'STCD1_C', '税号客户', 'string', 16, NULL, 1, NULL, false, NULL, '税号', 'active', 'admin', NOW(), 'admin', NOW()),

-- 客户销售数据
(70, 'VKORG', '销售组织', 'string', 4, NULL, 3, 13, false, NULL, '销售组织', 'active', 'admin', NOW(), 'admin', NOW()),
(71, 'VTWEG', '分销渠道', 'string', 2, NULL, 3, 14, false, NULL, '分销渠道', 'active', 'admin', NOW(), 'admin', NOW()),
(72, 'SPART', '产品组', 'string', 2, NULL, 3, NULL, false, NULL, '产品组', 'active', 'admin', NOW(), 'admin', NOW()),
(73, 'KDGRP', '客户组', 'string', 2, NULL, 3, 12, false, NULL, '客户组', 'active', 'admin', NOW(), 'admin', NOW()),
(74, 'WAERS_C', '货币客户', 'string', 5, NULL, 3, 9, false, NULL, '货币', 'active', 'admin', NOW(), 'admin', NOW()),
(75, 'ZTERM_C', '付款条件客户', 'string', 4, NULL, 3, 10, false, NULL, '付款条件', 'active', 'admin', NOW(), 'admin', NOW()),
(76, 'VSBED', '装运条件', 'string', 2, NULL, 3, NULL, false, NULL, '装运条件', 'active', 'admin', NOW(), 'admin', NOW()),
(77, 'INCO1_C', '国际贸易条款1客户', 'string', 3, NULL, 3, 11, false, NULL, '国际贸易条款', 'active', 'admin', NOW(), 'admin', NOW()),
(78, 'INCO2_C', '国际贸易条款2客户', 'string', 28, NULL, 3, NULL, false, NULL, '国际贸易条款地点', 'active', 'admin', NOW(), 'admin', NOW()),
(79, 'KZAZU', '订单组合', 'string', 1, NULL, 3, NULL, false, NULL, '订单组合', 'active', 'admin', NOW(), 'admin', NOW()),

-- 客户财务数据
(80, 'AKONT_C', '统驭科目客户', 'string', 10, NULL, 4, NULL, false, NULL, '统驭科目', 'active', 'admin', NOW(), 'admin', NOW()),
(81, 'ZTERM_FC', '付款条件财务客户', 'string', 4, NULL, 4, 10, false, NULL, '付款条件', 'active', 'admin', NOW(), 'admin', NOW()),
(82, 'FDGRV_C', '财务组客户', 'string', 10, NULL, 4, NULL, false, NULL, '财务组', 'active', 'admin', NOW(), 'admin', NOW()),
(83, 'BUSAB', '会计编号', 'string', 2, NULL, 4, NULL, false, NULL, '会计编号', 'active', 'admin', NOW(), 'admin', NOW()),

-- 通用字段
(84, 'LOEVM', '删除标记', 'string', 1, NULL, 1, NULL, false, NULL, '删除标记', 'active', 'admin', NOW(), 'admin', NOW()),
(85, 'SPERR', '冻结标记', 'string', 1, NULL, 1, NULL, false, NULL, '冻结标记', 'active', 'admin', NOW(), 'admin', NOW()),
(86, 'ERNAM', '创建人', 'string', 12, NULL, 1, NULL, false, NULL, '创建人', 'active', 'admin', NOW(), 'admin', NOW()),
(87, 'ERDAT', '创建日期', 'date', NULL, NULL, 1, NULL, false, NULL, '创建日期', 'active', 'admin', NOW(), 'admin', NOW()),
(88, 'LAEDA', '修改日期', 'date', NULL, NULL, 1, NULL, false, NULL, '修改日期', 'active', 'admin', NOW(), 'admin', NOW()),
(89, 'AENAM', '修改人', 'string', 12, NULL, 1, NULL, false, NULL, '修改人', 'active', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 5. 创建物料视图（草稿状态）
-- ============================================
INSERT INTO std_view (id, view_code, view_name, category_id, version, is_trunk, is_latest, layout_columns, label_width, enable_copy, enable_import, enable_export, status, description, created_by, created_at, updated_by, updated_at) VALUES
(1, 'MATERIAL', '物料视图', 2, 1, true, true, 2, 120, true, true, true, 'draft', 'SAP标准物料主数据视图，包含物料的基本信息、采购数据、MRP数据、财务数据等', 'admin', NOW(), 'admin', NOW());

-- 物料视图主实体
INSERT INTO std_view_entity (id, view_id, entity_code, entity_name, entity_type, table_name, sort, min_rows, max_rows, enable_add, enable_delete, enable_copy, enable_sort, description, created_by, created_at, updated_by, updated_at) VALUES
(1, 1, 'MAIN', '物料主数据', 'main', 'mdm_material', 1, 1, 1, false, false, false, false, '物料主数据', 'admin', NOW(), 'admin', NOW());

-- 物料视图字段 - 基本信息
INSERT INTO std_view_field (id, entity_id, field_code, field_name, field_standard_id, field_type, length, `precision`, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_query, is_query_result, is_sortable, status, created_by, created_at, updated_by, updated_at) VALUES
(1, 1, 'MATNR', '物料编号', 1, 'string', 18, NULL, NULL, 1, 1, true, false, false, true, true, true, true, 'active', 'admin', NOW(), 'admin', NOW()),
(2, 1, 'MAKTX', '物料描述', 2, 'string', 40, NULL, NULL, 2, 1, true, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(3, 1, 'MTART', '物料类型', 3, 'string', 4, NULL, NULL, 3, 1, true, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(4, 1, 'MBRSH', '行业领域', 4, 'string', 1, NULL, NULL, 4, 1, true, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(5, 1, 'MATKL', '物料组', 5, 'string', 9, NULL, NULL, 5, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(6, 1, 'MEINS', '基本单位', 6, 'string', 3, NULL, NULL, 6, 1, true, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(7, 1, 'GEWEI', '重量单位', 8, 'string', 3, NULL, NULL, 7, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(8, 1, 'BRGEW', '毛重', 9, 'decimal', 13, 3, NULL, 8, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(9, 1, 'NTGEW', '净重', 10, 'decimal', 13, 3, NULL, 9, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(10, 1, 'VOLUM', '体积', 11, 'decimal', 13, 3, NULL, 10, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(11, 1, 'VOLEH', '体积单位', 12, 'string', 3, NULL, NULL, 11, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
-- 物料视图字段 - 采购信息
(12, 1, 'EKGRP', '采购组', 13, 'string', 3, NULL, NULL, 12, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(13, 1, 'EKORG', '采购组织', 14, 'string', 4, NULL, NULL, 13, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(14, 1, 'BSTME', '采购单位', 7, 'string', 3, NULL, NULL, 14, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(15, 1, 'PLIFZ', '计划交货时间', 15, 'integer', NULL, NULL, NULL, 15, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(16, 1, 'WEBAZ', '收货处理时间', 16, 'integer', NULL, NULL, NULL, 16, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(17, 1, 'MINLS', '最小批量', 17, 'decimal', 13, 3, NULL, 17, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(18, 1, 'MAXLS', '最大批量', 18, 'decimal', 13, 3, NULL, 18, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(19, 1, 'BSTMI', '最小包装量', 19, 'decimal', 13, 3, NULL, 19, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
-- 物料视图字段 - MRP信息
(20, 1, 'DISMM', 'MRP类型', 21, 'string', 2, NULL, NULL, 20, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(21, 1, 'DISPO', 'MRP控制者', 22, 'string', 3, NULL, NULL, 21, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(22, 1, 'DISLS', '批量大小', 23, 'string', 2, NULL, NULL, 22, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(23, 1, 'BESKZ', '采购类型', 24, 'string', 1, NULL, NULL, 23, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(24, 1, 'SOBSL', '特殊采购', 25, 'string', 2, NULL, NULL, 24, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(25, 1, 'MINBE', '安全库存', 26, 'decimal', 13, 3, NULL, 25, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(26, 1, 'EISBE', '最小安全库存', 27, 'decimal', 13, 3, NULL, 26, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(27, 1, 'BSTFE', '固定批量', 28, 'decimal', 13, 3, NULL, 27, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(28, 1, 'WZEIT', '计划边际码', 29, 'integer', NULL, NULL, NULL, 28, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
-- 物料视图字段 - 财务信息
(29, 1, 'BKLAS', '评估类', 30, 'string', 4, NULL, NULL, 29, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(30, 1, 'VPRSV', '价格控制', 31, 'string', 1, NULL, NULL, 30, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(31, 1, 'VERPR', '移动平均价', 32, 'decimal', 15, 2, NULL, 31, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(32, 1, 'STPRS', '标准价格', 33, 'decimal', 15, 2, NULL, 32, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(33, 1, 'PEINH', '价格单位', 34, 'integer', NULL, NULL, NULL, 33, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(34, 1, 'WAERS', '货币', 35, 'string', 5, NULL, NULL, 34, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 6. 创建供应商视图（草稿状态）
-- ============================================
INSERT INTO std_view (id, view_code, view_name, category_id, version, is_trunk, is_latest, layout_columns, label_width, enable_copy, enable_import, enable_export, status, description, created_by, created_at, updated_by, updated_at) VALUES
(2, 'VENDOR', '供应商视图', 3, 1, true, true, 2, 120, true, true, true, 'draft', 'SAP标准供应商主数据视图，包含基本信息、采购数据、财务数据', 'admin', NOW(), 'admin', NOW());

-- 供应商视图主实体
INSERT INTO std_view_entity (id, view_id, entity_code, entity_name, entity_type, table_name, sort, min_rows, max_rows, enable_add, enable_delete, enable_copy, enable_sort, description, created_by, created_at, updated_by, updated_at) VALUES
(2, 2, 'MAIN', '供应商主数据', 'main', 'mdm_vendor', 1, 1, 1, false, false, false, false, '供应商主数据', 'admin', NOW(), 'admin', NOW());

-- 供应商视图字段
INSERT INTO std_view_field (id, entity_id, field_code, field_name, field_standard_id, field_type, length, `precision`, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_query, is_query_result, is_sortable, status, created_by, created_at, updated_by, updated_at) VALUES
(35, 2, 'LIFNR', '供应商编号', 36, 'string', 10, NULL, NULL, 1, 1, true, false, false, true, true, true, true, 'active', 'admin', NOW(), 'admin', NOW()),
(36, 2, 'NAME1', '供应商名称', 37, 'string', 35, NULL, NULL, 2, 1, true, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(37, 2, 'NAME2', '供应商名称2', 38, 'string', 35, NULL, NULL, 3, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(38, 2, 'LAND1', '国家', 40, 'string', 3, NULL, NULL, 4, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(39, 2, 'REGIO', '地区', 41, 'string', 3, NULL, NULL, 5, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(40, 2, 'ORT01', '城市', 39, 'string', 35, NULL, NULL, 6, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(41, 2, 'STRAS', '街道', 42, 'string', 35, NULL, NULL, 7, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(42, 2, 'PSTLZ', '邮政编码', 43, 'string', 10, NULL, NULL, 8, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(43, 2, 'TELF1', '电话', 44, 'string', 16, NULL, NULL, 9, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(44, 2, 'TELF2', '电话2', 45, 'string', 16, NULL, NULL, 10, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(45, 2, 'TELFX', '传真', 46, 'string', 31, NULL, NULL, 11, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(46, 2, 'STCD1', '税号', 47, 'string', 16, NULL, NULL, 12, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(47, 2, 'STCD2', '税号2', 48, 'string', 11, NULL, NULL, 13, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(48, 2, 'EKORG', '采购组织', 14, 'string', 4, NULL, NULL, 14, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(49, 2, 'EKGRP', '采购组', 13, 'string', 3, NULL, NULL, 15, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(50, 2, 'WAERS_V', '订单货币', 49, 'string', 5, NULL, NULL, 16, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(51, 2, 'ZTERM_V', '付款条件', 50, 'string', 4, NULL, NULL, 17, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(52, 2, 'INCO1', '国际贸易条款1', 51, 'string', 3, NULL, NULL, 18, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(53, 2, 'INCO2', '国际贸易条款2', 52, 'string', 28, NULL, NULL, 19, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(54, 2, 'KALSK', '评估组', 53, 'string', 2, NULL, NULL, 20, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(55, 2, 'VERSG', '供应商组', 54, 'string', 1, NULL, NULL, 21, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(56, 2, 'AKONT', '统驭科目', 55, 'string', 10, NULL, NULL, 22, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(57, 2, 'ZTERM_F', '付款条件财务', 56, 'string', 4, NULL, NULL, 23, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(58, 2, 'FDGRV', '财务组', 57, 'string', 10, NULL, NULL, 24, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(59, 2, 'REPRF', '核对标记', 58, 'string', 1, NULL, NULL, 25, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 7. 创建客户视图（草稿状态）
-- ============================================
INSERT INTO std_view (id, view_code, view_name, category_id, version, is_trunk, is_latest, layout_columns, label_width, enable_copy, enable_import, enable_export, status, description, created_by, created_at, updated_by, updated_at) VALUES
(3, 'CUSTOMER', '客户视图', 4, 1, true, true, 2, 120, true, true, true, 'draft', 'SAP标准客户主数据视图，包含基本信息、销售数据、财务数据', 'admin', NOW(), 'admin', NOW());

-- 客户视图主实体
INSERT INTO std_view_entity (id, view_id, entity_code, entity_name, entity_type, table_name, sort, min_rows, max_rows, enable_add, enable_delete, enable_copy, enable_sort, description, created_by, created_at, updated_by, updated_at) VALUES
(3, 3, 'MAIN', '客户主数据', 'main', 'mdm_customer', 1, 1, 1, false, false, false, false, '客户主数据', 'admin', NOW(), 'admin', NOW());

-- 客户视图字段
INSERT INTO std_view_field (id, entity_id, field_code, field_name, field_standard_id, field_type, length, `precision`, group_id, sort, column_span, is_required, is_readonly, is_hidden, is_query, is_query_result, is_sortable, status, created_by, created_at, updated_by, updated_at) VALUES
(60, 3, 'KUNNR', '客户编号', 59, 'string', 10, NULL, NULL, 1, 1, true, false, false, true, true, true, true, 'active', 'admin', NOW(), 'admin', NOW()),
(61, 3, 'NAME1_C', '客户名称', 60, 'string', 35, NULL, NULL, 2, 1, true, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(62, 3, 'NAME2_C', '客户名称2', 61, 'string', 35, NULL, NULL, 3, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(63, 3, 'LAND1_C', '国家', 63, 'string', 3, NULL, NULL, 4, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(64, 3, 'REGIO_C', '地区', 64, 'string', 3, NULL, NULL, 5, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(65, 3, 'ORT01_C', '城市', 62, 'string', 35, NULL, NULL, 6, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(66, 3, 'STRAS_C', '街道', 65, 'string', 35, NULL, NULL, 7, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(67, 3, 'PSTLZ_C', '邮政编码', 66, 'string', 10, NULL, NULL, 8, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(68, 3, 'TELF1_C', '电话', 67, 'string', 16, NULL, NULL, 9, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(69, 3, 'TELFX_C', '传真', 68, 'string', 31, NULL, NULL, 10, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(70, 3, 'STCD1_C', '税号', 69, 'string', 16, NULL, NULL, 11, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(71, 3, 'VKORG', '销售组织', 70, 'string', 4, NULL, NULL, 12, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(72, 3, 'VTWEG', '分销渠道', 71, 'string', 2, NULL, NULL, 13, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(73, 3, 'SPART', '产品组', 72, 'string', 2, NULL, NULL, 14, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(74, 3, 'KDGRP', '客户组', 73, 'string', 2, NULL, NULL, 15, 1, false, false, false, false, true, true, false, 'active', 'admin', NOW(), 'admin', NOW()),
(75, 3, 'WAERS_C', '货币', 74, 'string', 5, NULL, NULL, 16, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(76, 3, 'ZTERM_C', '付款条件', 75, 'string', 4, NULL, NULL, 17, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(77, 3, 'VSBED', '装运条件', 76, 'string', 2, NULL, NULL, 18, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(78, 3, 'INCO1_C', '国际贸易条款1', 77, 'string', 3, NULL, NULL, 19, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(79, 3, 'INCO2_C', '国际贸易条款2', 78, 'string', 28, NULL, NULL, 20, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(80, 3, 'KZAZU', '订单组合', 79, 'string', 1, NULL, NULL, 21, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(81, 3, 'AKONT_C', '统驭科目', 80, 'string', 10, NULL, NULL, 22, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(82, 3, 'ZTERM_FC', '付款条件财务', 81, 'string', 4, NULL, NULL, 23, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(83, 3, 'FDGRV_C', '财务组', 82, 'string', 10, NULL, NULL, 24, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW()),
(84, 3, 'BUSAB', '会计编号', 83, 'string', 2, NULL, NULL, 25, 1, false, false, false, false, false, false, false, 'active', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 完成！
-- ============================================
