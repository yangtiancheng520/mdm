-- ============================================
-- 字段分类和字段标准初始化脚本
-- 包含SAP物料、客户、供应商相关字段
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================
-- 1. 创建字段分类
-- ============================================

-- 先清空旧数据
DELETE FROM std_field_standard;
DELETE FROM std_field_category;

-- 插入字段分类
INSERT INTO std_field_category (id, category_code, category_name, parent_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
-- 一级分类
(1, 'MATERIAL', '物料字段', NULL, 1, 'active', 'SAP物料主数据相关字段', 'admin', NOW(), 'admin', NOW()),
(2, 'VENDOR', '供应商字段', NULL, 2, 'active', 'SAP供应商主数据相关字段', 'admin', NOW(), 'admin', NOW()),
(3, 'CUSTOMER', '客户字段', NULL, 3, 'active', 'SAP客户主数据相关字段', 'admin', NOW(), 'admin', NOW()),
(4, 'COMMON', '通用字段', NULL, 4, 'active', '通用字段', 'admin', NOW(), 'admin', NOW()),

-- 物料二级分类
(5, 'MAT_BASIC', '基本信息', 1, 1, 'active', '物料基本信息字段', 'admin', NOW(), 'admin', NOW()),
(6, 'MAT_PURCHASE', '采购信息', 1, 2, 'active', '物料采购信息字段', 'admin', NOW(), 'admin', NOW()),
(7, 'MAT_MRP', 'MRP信息', 1, 3, 'active', '物料MRP信息字段', 'admin', NOW(), 'admin', NOW()),
(8, 'MAT_ACCOUNT', '财务信息', 1, 4, 'active', '物料财务信息字段', 'admin', NOW(), 'admin', NOW()),
(9, 'MAT_WH', '仓库信息', 1, 5, 'active', '物料仓库信息字段', 'admin', NOW(), 'admin', NOW()),
(10, 'MAT_QUALITY', '质量信息', 1, 6, 'active', '物料质量信息字段', 'admin', NOW(), 'admin', NOW()),

-- 供应商二级分类
(11, 'VEND_BASIC', '基本信息', 2, 1, 'active', '供应商基本信息字段', 'admin', NOW(), 'admin', NOW()),
(12, 'VEND_PURCHASE', '采购信息', 2, 2, 'active', '供应商采购信息字段', 'admin', NOW(), 'admin', NOW()),
(13, 'VEND_ACCOUNT', '财务信息', 2, 3, 'active', '供应商财务信息字段', 'admin', NOW(), 'admin', NOW()),

-- 客户二级分类
(14, 'CUST_BASIC', '基本信息', 3, 1, 'active', '客户基本信息字段', 'admin', NOW(), 'admin', NOW()),
(15, 'CUST_SALES', '销售信息', 3, 2, 'active', '客户销售信息字段', 'admin', NOW(), 'admin', NOW()),
(16, 'CUST_ACCOUNT', '财务信息', 3, 3, 'active', '客户财务信息字段', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 2. 创建字段标准
-- ============================================

-- ================== 物料字段 ==================

-- 物料基本信息字段
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(1, 'MATNR', '物料编号', 'string', 18, NULL, 1, NULL, 5, '基本信息', 0, 'published', 1, '物料唯一标识编码', 'admin', NOW(), 'admin', NOW()),
(2, 'MAKTX', '物料描述', 'string', 40, NULL, 1, NULL, 5, '基本信息', 0, 'published', 1, '物料名称描述', 'admin', NOW(), 'admin', NOW()),
(3, 'MAKTG', '物料描述2', 'string', 40, NULL, 0, NULL, 5, '基本信息', 0, 'published', 1, '物料附加描述', 'admin', NOW(), 'admin', NOW()),
(4, 'MTART', '物料类型', 'string', 4, NULL, 1, 1, 5, '基本信息', 1, 'published', 1, '物料类型代码', 'admin', NOW(), 'admin', NOW()),
(5, 'MBRSH', '行业领域', 'string', 1, NULL, 1, 2, 5, '基本信息', 1, 'published', 1, '行业领域', 'admin', NOW(), 'admin', NOW()),
(6, 'MATKL', '物料组', 'string', 9, NULL, 0, 4, 5, '基本信息', 1, 'published', 1, '物料分组', 'admin', NOW(), 'admin', NOW()),
(7, 'MEINS', '基本单位', 'string', 3, NULL, 1, 3, 5, '基本信息', 1, 'published', 1, '基本计量单位', 'admin', NOW(), 'admin', NOW()),
(8, 'BSTME', '采购单位', 'string', 3, NULL, 0, 3, 5, '基本信息', 1, 'published', 1, '采购计量单位', 'admin', NOW(), 'admin', NOW()),
(9, 'GEWEI', '重量单位', 'string', 3, NULL, 0, 3, 5, '基本信息', 1, 'published', 1, '重量单位', 'admin', NOW(), 'admin', NOW()),
(10, 'BRGEW', '毛重', 'decimal', 13, 3, 0, NULL, 5, '基本信息', 0, 'published', 1, '毛重', 'admin', NOW(), 'admin', NOW()),
(11, 'NTGEW', '净重', 'decimal', 13, 3, 0, NULL, 5, '基本信息', 0, 'published', 1, '净重', 'admin', NOW(), 'admin', NOW()),
(12, 'VOLUM', '体积', 'decimal', 13, 3, 0, NULL, 5, '基本信息', 0, 'published', 1, '体积', 'admin', NOW(), 'admin', NOW()),
(13, 'VOLEH', '体积单位', 'string', 3, NULL, 0, 3, 5, '基本信息', 1, 'published', 1, '体积单位', 'admin', NOW(), 'admin', NOW()),
(14, 'FLIEH', '生产调度员', 'string', 3, NULL, 0, NULL, 5, '基本信息', 0, 'published', 1, '生产调度员', 'admin', NOW(), 'admin', NOW()),
(15, 'XCHPF', '批次管理', 'boolean', NULL, NULL, 0, 28, 5, '基本信息', 1, 'published', 1, '是否批次管理', 'admin', NOW(), 'admin', NOW());

-- 物料采购信息字段
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(16, 'EKGRP', '采购组', 'string', 3, NULL, 0, 5, 6, '采购信息', 1, 'published', 1, '采购组代码', 'admin', NOW(), 'admin', NOW()),
(17, 'EKORG', '采购组织', 'string', 4, NULL, 0, 6, 6, '采购信息', 1, 'published', 1, '采购组织', 'admin', NOW(), 'admin', NOW()),
(18, 'PLIFZ', '计划交货时间', 'integer', NULL, NULL, 0, NULL, 6, '采购信息', 0, 'published', 1, '计划交货天数', 'admin', NOW(), 'admin', NOW()),
(19, 'WEBAZ', '收货处理时间', 'integer', NULL, NULL, 0, NULL, 6, '采购信息', 0, 'published', 1, '收货处理天数', 'admin', NOW(), 'admin', NOW()),
(20, 'MINLS', '最小批量', 'decimal', 13, 3, 0, NULL, 6, '采购信息', 0, 'published', 1, '最小订单批量', 'admin', NOW(), 'admin', NOW()),
(21, 'MAXLS', '最大批量', 'decimal', 13, 3, 0, NULL, 6, '采购信息', 0, 'published', 1, '最大订单批量', 'admin', NOW(), 'admin', NOW()),
(22, 'BSTMI', '最小包装量', 'decimal', 13, 3, 0, NULL, 6, '采购信息', 0, 'published', 1, '最小包装数量', 'admin', NOW(), 'admin', NOW()),
(23, 'MABST', '最大库存水平', 'decimal', 13, 3, 0, NULL, 6, '采购信息', 0, 'published', 1, '最大库存水平', 'admin', NOW(), 'admin', NOW()),
(24, 'LFRAB', '供应商配额', 'decimal', 5, 2, 0, NULL, 6, '采购信息', 0, 'published', 1, '供应商配额比例', 'admin', NOW(), 'admin', NOW());

-- 物料MRP信息字段
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(25, 'DISMM', 'MRP类型', 'string', 2, NULL, 0, 7, 7, 'MRP信息', 1, 'published', 1, 'MRP类型', 'admin', NOW(), 'admin', NOW()),
(26, 'DISPO', 'MRP控制者', 'string', 3, NULL, 0, 8, 7, 'MRP信息', 1, 'published', 1, 'MRP控制者', 'admin', NOW(), 'admin', NOW()),
(27, 'DISLS', '批量大小', 'string', 2, NULL, 0, NULL, 7, 'MRP信息', 0, 'published', 1, '批量大小', 'admin', NOW(), 'admin', NOW()),
(28, 'BESKZ', '采购类型', 'string', 1, NULL, 0, 9, 7, 'MRP信息', 1, 'published', 1, '采购类型', 'admin', NOW(), 'admin', NOW()),
(29, 'SOBSL', '特殊采购', 'string', 2, NULL, 0, NULL, 7, 'MRP信息', 0, 'published', 1, '特殊采购类型', 'admin', NOW(), 'admin', NOW()),
(30, 'MINBE', '安全库存', 'decimal', 13, 3, 0, NULL, 7, 'MRP信息', 0, 'published', 1, '安全库存', 'admin', NOW(), 'admin', NOW()),
(31, 'EISBE', '最小安全库存', 'decimal', 13, 3, 0, NULL, 7, 'MRP信息', 0, 'published', 1, '最小安全库存', 'admin', NOW(), 'admin', NOW()),
(32, 'BSTFE', '固定批量', 'decimal', 13, 3, 0, NULL, 7, 'MRP信息', 0, 'published', 1, '固定订货批量', 'admin', NOW(), 'admin', NOW()),
(33, 'WZEIT', '计划边际码', 'integer', NULL, NULL, 0, NULL, 7, 'MRP信息', 0, 'published', 1, '计划边际码', 'admin', NOW(), 'admin', NOW()),
(34, 'PLNMR', '计划模式', 'string', 1, NULL, 0, NULL, 7, 'MRP信息', 0, 'published', 1, '计划模式', 'admin', NOW(), 'admin', NOW()),
(35, 'VRBZT', '消耗模式', 'string', 1, NULL, 0, NULL, 7, 'MRP信息', 0, 'published', 1, '消耗模式', 'admin', NOW(), 'admin', NOW()),
(36, 'VSMIN', '反冲', 'boolean', NULL, NULL, 0, 28, 7, 'MRP信息', 1, 'published', 1, '是否反冲', 'admin', NOW(), 'admin', NOW());

-- 物料财务信息字段
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(37, 'BKLAS', '评估类', 'string', 4, NULL, 0, 11, 8, '财务信息', 1, 'published', 1, '评估类', 'admin', NOW(), 'admin', NOW()),
(38, 'VPRSV', '价格控制', 'string', 1, NULL, 0, 10, 8, '财务信息', 1, 'published', 1, '价格控制', 'admin', NOW(), 'admin', NOW()),
(39, 'VERPR', '移动平均价', 'decimal', 15, 2, 0, NULL, 8, '财务信息', 0, 'published', 1, '移动平均价', 'admin', NOW(), 'admin', NOW()),
(40, 'STPRS', '标准价格', 'decimal', 15, 2, 0, NULL, 8, '财务信息', 0, 'published', 1, '标准价格', 'admin', NOW(), 'admin', NOW()),
(41, 'PEINH', '价格单位', 'integer', NULL, NULL, 0, NULL, 8, '财务信息', 0, 'published', 1, '价格单位', 'admin', NOW(), 'admin', NOW()),
(42, 'WAERS', '货币', 'string', 5, NULL, 0, 13, 8, '财务信息', 1, 'published', 1, '货币代码', 'admin', NOW(), 'admin', NOW()),
(43, 'BWTTY', '评估类型', 'string', 1, NULL, 0, NULL, 8, '财务信息', 0, 'published', 1, '评估类型', 'admin', NOW(), 'admin', NOW()),
(44, 'MLAST', '价格确定', 'string', 1, NULL, 0, NULL, 8, '财务信息', 0, 'published', 1, '价格确定', 'admin', NOW(), 'admin', NOW());

-- ================== 供应商字段 ==================

-- 供应商基本信息字段
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(45, 'LIFNR', '供应商编号', 'string', 10, NULL, 1, NULL, 11, '基本信息', 0, 'published', 1, '供应商唯一标识', 'admin', NOW(), 'admin', NOW()),
(46, 'NAME1', '供应商名称', 'string', 35, NULL, 1, NULL, 11, '基本信息', 0, 'published', 1, '供应商名称', 'admin', NOW(), 'admin', NOW()),
(47, 'NAME2', '供应商名称2', 'string', 35, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '供应商名称2', 'admin', NOW(), 'admin', NOW()),
(48, 'NAME3', '供应商名称3', 'string', 35, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '供应商名称3', 'admin', NOW(), 'admin', NOW()),
(49, 'NAME4', '供应商名称4', 'string', 35, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '供应商名称4', 'admin', NOW(), 'admin', NOW()),
(50, 'ORT01', '城市', 'string', 35, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '城市', 'admin', NOW(), 'admin', NOW()),
(51, 'LAND1', '国家', 'string', 3, NULL, 0, 12, 11, '基本信息', 1, 'published', 1, '国家代码', 'admin', NOW(), 'admin', NOW()),
(52, 'REGIO', '地区', 'string', 3, NULL, 0, 17, 11, '基本信息', 1, 'published', 1, '地区/省份', 'admin', NOW(), 'admin', NOW()),
(53, 'STRAS', '街道', 'string', 35, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '街道地址', 'admin', NOW(), 'admin', NOW()),
(54, 'PSTLZ', '邮政编码', 'string', 10, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '邮政编码', 'admin', NOW(), 'admin', NOW()),
(55, 'PFACH', '邮政信箱', 'string', 10, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '邮政信箱', 'admin', NOW(), 'admin', NOW()),
(56, 'TELF1', '电话', 'string', 16, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '电话号码', 'admin', NOW(), 'admin', NOW()),
(57, 'TELF2', '电话2', 'string', 16, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '电话号码2', 'admin', NOW(), 'admin', NOW()),
(58, 'TELFX', '传真', 'string', 31, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '传真号码', 'admin', NOW(), 'admin', NOW()),
(59, 'TELBX', '电传', 'string', 15, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '电传号码', 'admin', NOW(), 'admin', NOW()),
(60, 'TELEX', '电报', 'string', 30, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '电报号码', 'admin', NOW(), 'admin', NOW()),
(61, 'SMTP_ADDR', '邮箱', 'string', 130, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '电子邮件地址', 'admin', NOW(), 'admin', NOW()),
(62, 'STCD1', '税号', 'string', 16, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '税号', 'admin', NOW(), 'admin', NOW()),
(63, 'STCD2', '税号2', 'string', 11, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '税号2', 'admin', NOW(), 'admin', NOW()),
(64, 'STCD3', '税号3', 'string', 18, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '税号3', 'admin', NOW(), 'admin', NOW()),
(65, 'STCD4', '税号4', 'string', 18, NULL, 0, NULL, 11, '基本信息', 0, 'published', 1, '税号4', 'admin', NOW(), 'admin', NOW()),
(66, 'SPRAS', '语言', 'string', 2, NULL, 0, 16, 11, '基本信息', 1, 'published', 1, '语言代码', 'admin', NOW(), 'admin', NOW());

-- 供应商采购信息字段
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(67, 'EKORG_V', '采购组织', 'string', 4, NULL, 0, 6, 12, '采购信息', 1, 'published', 1, '采购组织', 'admin', NOW(), 'admin', NOW()),
(68, 'EKGRP_V', '采购组', 'string', 3, NULL, 0, 5, 12, '采购信息', 1, 'published', 1, '采购组', 'admin', NOW(), 'admin', NOW()),
(69, 'WAERS_V', '订单货币', 'string', 5, NULL, 0, 13, 12, '采购信息', 1, 'published', 1, '订单货币', 'admin', NOW(), 'admin', NOW()),
(70, 'ZTERM_V', '付款条件', 'string', 4, NULL, 0, 14, 12, '采购信息', 1, 'published', 1, '付款条件', 'admin', NOW(), 'admin', NOW()),
(71, 'INCO1', '国际贸易条款1', 'string', 3, NULL, 0, 15, 12, '采购信息', 1, 'published', 1, '国际贸易条款', 'admin', NOW(), 'admin', NOW()),
(72, 'INCO2', '国际贸易条款2', 'string', 28, NULL, 0, NULL, 12, '采购信息', 0, 'published', 1, '国际贸易条款地点', 'admin', NOW(), 'admin', NOW()),
(73, 'KALSK', '评估组', 'string', 2, NULL, 0, NULL, 12, '采购信息', 0, 'published', 1, '供应商评估组', 'admin', NOW(), 'admin', NOW()),
(74, 'VERSG', '供应商组', 'string', 1, NULL, 0, 18, 12, '采购信息', 1, 'published', 1, '供应商组', 'admin', NOW(), 'admin', NOW()),
(75, 'BMEIN', '订单单位', 'string', 3, NULL, 0, 3, 12, '采购信息', 1, 'published', 1, '订单单位', 'admin', NOW(), 'admin', NOW()),
(76, 'UMREZ', '转换分子', 'integer', NULL, NULL, 0, NULL, 12, '采购信息', 0, 'published', 1, '转换分子', 'admin', NOW(), 'admin', NOW()),
(77, 'UMREN', '转换分母', 'integer', NULL, NULL, 0, NULL, 12, '采购信息', 0, 'published', 1, '转换分母', 'admin', NOW(), 'admin', NOW()),
(78, 'WEBRU', '收货点', 'string', 4, NULL, 0, NULL, 12, '采购信息', 0, 'published', 1, '收货点', 'admin', NOW(), 'admin', NOW()),
(79, 'MEPRF', '价格日期控制', 'string', 1, NULL, 0, NULL, 12, '采购信息', 0, 'published', 1, '价格日期控制', 'admin', NOW(), 'admin', NOW());

-- 供应商财务信息字段
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(80, 'AKONT', '统驭科目', 'string', 10, NULL, 0, 21, 13, '财务信息', 1, 'published', 1, '统驭科目', 'admin', NOW(), 'admin', NOW()),
(81, 'ZTERM_F', '付款条件财务', 'string', 4, NULL, 0, 14, 13, '财务信息', 1, 'published', 1, '付款条件', 'admin', NOW(), 'admin', NOW()),
(82, 'FDGRV', '财务组', 'string', 10, NULL, 0, NULL, 13, '财务信息', 0, 'published', 1, '财务组', 'admin', NOW(), 'admin', NOW()),
(83, 'REPRF', '核对标记', 'boolean', NULL, NULL, 0, 28, 13, '财务信息', 1, 'published', 1, '核对标记', 'admin', NOW(), 'admin', NOW()),
(84, 'ZUAWA', '清账规则', 'string', 3, NULL, 0, NULL, 13, '财务信息', 0, 'published', 1, '清账规则', 'admin', NOW(), 'admin', NOW()),
(85, 'QSSKZ', '预扣税类型', 'string', 2, NULL, 0, 20, 13, '财务信息', 1, 'published', 1, '预扣税类型', 'admin', NOW(), 'admin', NOW()),
(86, 'QSZNR', '预扣税证明', 'string', 10, NULL, 0, NULL, 13, '财务信息', 0, 'published', 1, '预扣税证明', 'admin', NOW(), 'admin', NOW()),
(87, 'XAUSZ', '一次性付款', 'boolean', NULL, NULL, 0, 28, 13, '财务信息', 1, 'published', 1, '是否一次性付款', 'admin', NOW(), 'admin', NOW());

-- ================== 客户字段 ==================

-- 客户基本信息字段
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(88, 'KUNNR', '客户编号', 'string', 10, NULL, 1, NULL, 14, '基本信息', 0, 'published', 1, '客户唯一标识', 'admin', NOW(), 'admin', NOW()),
(89, 'NAME1_C', '客户名称', 'string', 35, NULL, 1, NULL, 14, '基本信息', 0, 'published', 1, '客户名称', 'admin', NOW(), 'admin', NOW()),
(90, 'NAME2_C', '客户名称2', 'string', 35, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '客户名称2', 'admin', NOW(), 'admin', NOW()),
(91, 'NAME3_C', '客户名称3', 'string', 35, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '客户名称3', 'admin', NOW(), 'admin', NOW()),
(92, 'NAME4_C', '客户名称4', 'string', 35, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '客户名称4', 'admin', NOW(), 'admin', NOW()),
(93, 'ORT01_C', '城市', 'string', 35, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '城市', 'admin', NOW(), 'admin', NOW()),
(94, 'LAND1_C', '国家', 'string', 3, NULL, 0, 12, 14, '基本信息', 1, 'published', 1, '国家代码', 'admin', NOW(), 'admin', NOW()),
(95, 'REGIO_C', '地区', 'string', 3, NULL, 0, 17, 14, '基本信息', 1, 'published', 1, '地区/省份', 'admin', NOW(), 'admin', NOW()),
(96, 'STRAS_C', '街道', 'string', 35, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '街道地址', 'admin', NOW(), 'admin', NOW()),
(97, 'PSTLZ_C', '邮政编码', 'string', 10, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '邮政编码', 'admin', NOW(), 'admin', NOW()),
(98, 'PFACH_C', '邮政信箱', 'string', 10, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '邮政信箱', 'admin', NOW(), 'admin', NOW()),
(99, 'TELF1_C', '电话', 'string', 16, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '电话号码', 'admin', NOW(), 'admin', NOW()),
(100, 'TELF2_C', '电话2', 'string', 16, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '电话号码2', 'admin', NOW(), 'admin', NOW()),
(101, 'TELFX_C', '传真', 'string', 31, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '传真号码', 'admin', NOW(), 'admin', NOW()),
(102, 'SMTP_C', '邮箱', 'string', 130, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '电子邮件地址', 'admin', NOW(), 'admin', NOW()),
(103, 'STCD1_C', '税号', 'string', 16, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '税号', 'admin', NOW(), 'admin', NOW()),
(104, 'STCD2_C', '税号2', 'string', 11, NULL, 0, NULL, 14, '基本信息', 0, 'published', 1, '税号2', 'admin', NOW(), 'admin', NOW()),
(105, 'SPRAS_C', '语言', 'string', 2, NULL, 0, 16, 14, '基本信息', 1, 'published', 1, '语言代码', 'admin', NOW(), 'admin', NOW());

-- 客户销售信息字段
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(106, 'VKORG', '销售组织', 'string', 4, NULL, 0, 23, 15, '销售信息', 1, 'published', 1, '销售组织', 'admin', NOW(), 'admin', NOW()),
(107, 'VTWEG', '分销渠道', 'string', 2, NULL, 0, 24, 15, '销售信息', 1, 'published', 1, '分销渠道', 'admin', NOW(), 'admin', NOW()),
(108, 'SPART', '产品组', 'string', 2, NULL, 0, 25, 15, '销售信息', 1, 'published', 1, '产品组', 'admin', NOW(), 'admin', NOW()),
(109, 'KDGRP', '客户组', 'string', 2, NULL, 0, 22, 15, '销售信息', 1, 'published', 1, '客户组', 'admin', NOW(), 'admin', NOW()),
(110, 'WAERS_C', '货币', 'string', 5, NULL, 0, 13, 15, '销售信息', 1, 'published', 1, '货币', 'admin', NOW(), 'admin', NOW()),
(111, 'ZTERM_C', '付款条件', 'string', 4, NULL, 0, 14, 15, '销售信息', 1, 'published', 1, '付款条件', 'admin', NOW(), 'admin', NOW()),
(112, 'VSBED', '装运条件', 'string', 2, NULL, 0, 26, 15, '销售信息', 1, 'published', 1, '装运条件', 'admin', NOW(), 'admin', NOW()),
(113, 'INCO1_C', '国际贸易条款1', 'string', 3, NULL, 0, 15, 15, '销售信息', 1, 'published', 1, '国际贸易条款', 'admin', NOW(), 'admin', NOW()),
(114, 'INCO2_C', '国际贸易条款2', 'string', 28, NULL, 0, NULL, 15, '销售信息', 0, 'published', 1, '国际贸易条款地点', 'admin', NOW(), 'admin', NOW()),
(115, 'KZAZU', '订单组合', 'boolean', NULL, NULL, 0, 28, 15, '销售信息', 1, 'published', 1, '订单组合', 'admin', NOW(), 'admin', NOW()),
(116, 'KTGRD', '客户科目组', 'string', 2, NULL, 0, NULL, 15, '销售信息', 0, 'published', 1, '客户科目组', 'admin', NOW(), 'admin', NOW()),
(117, 'BZIRK', '销售地区', 'string', 4, NULL, 0, NULL, 15, '销售信息', 0, 'published', 1, '销售地区', 'admin', NOW(), 'admin', NOW()),
(118, 'KALKS', '客户定价过程', 'string', 1, NULL, 0, 27, 15, '销售信息', 1, 'published', 1, '客户定价过程', 'admin', NOW(), 'admin', NOW()),
(119, 'VWERK', '交货工厂', 'string', 4, NULL, 0, NULL, 15, '销售信息', 0, 'published', 1, '交货工厂', 'admin', NOW(), 'admin', NOW()),
(120, 'LIFSD', '交货冻结', 'string', 2, NULL, 0, NULL, 15, '销售信息', 0, 'published', 1, '交货冻结', 'admin', NOW(), 'admin', NOW());

-- 客户财务信息字段
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(121, 'AKONT_C', '统驭科目', 'string', 10, NULL, 0, 21, 16, '财务信息', 1, 'published', 1, '统驭科目', 'admin', NOW(), 'admin', NOW()),
(122, 'ZTERM_FC', '付款条件财务', 'string', 4, NULL, 0, 14, 16, '财务信息', 1, 'published', 1, '付款条件', 'admin', NOW(), 'admin', NOW()),
(123, 'FDGRV_C', '财务组', 'string', 10, NULL, 0, NULL, 16, '财务信息', 0, 'published', 1, '财务组', 'admin', NOW(), 'admin', NOW()),
(124, 'BUSAB', '会计编号', 'string', 2, NULL, 0, NULL, 16, '财务信息', 0, 'published', 1, '会计编号', 'admin', NOW(), 'admin', NOW()),
(125, 'XAUSZ_C', '一次性付款', 'boolean', NULL, NULL, 0, 28, 16, '财务信息', 1, 'published', 1, '是否一次性付款', 'admin', NOW(), 'admin', NOW()),
(126, 'ZUAWA_C', '清账规则', 'string', 3, NULL, 0, NULL, 16, '财务信息', 0, 'published', 1, '清账规则', 'admin', NOW(), 'admin', NOW());

-- ================== 通用字段 ==================
INSERT INTO std_field_standard (id, field_code, field_name, field_type, length, `precision`, is_required, domain_id, category_id, category, is_enum, status, version, description, created_by, created_at, updated_by, updated_at) VALUES
(127, 'LOEVM', '删除标记', 'boolean', NULL, NULL, 0, 30, 4, '通用字段', 1, 'published', 1, '删除标记', 'admin', NOW(), 'admin', NOW()),
(128, 'SPERR', '冻结标记', 'boolean', NULL, NULL, 0, 28, 4, '通用字段', 1, 'published', 1, '冻结标记', 'admin', NOW(), 'admin', NOW()),
(129, 'ERNAM', '创建人', 'string', 12, NULL, 0, NULL, 4, '通用字段', 0, 'published', 1, '创建人', 'admin', NOW(), 'admin', NOW()),
(130, 'ERDAT', '创建日期', 'date', NULL, NULL, 0, NULL, 4, '通用字段', 0, 'published', 1, '创建日期', 'admin', NOW(), 'admin', NOW()),
(131, 'LAEDA', '修改日期', 'date', NULL, NULL, 0, NULL, 4, '通用字段', 0, 'published', 1, '修改日期', 'admin', NOW(), 'admin', NOW()),
(132, 'AENAM', '修改人', 'string', 12, NULL, 0, NULL, 4, '通用字段', 0, 'published', 1, '修改人', 'admin', NOW(), 'admin', NOW()),
(133, 'STATUS', '状态', 'string', 20, NULL, 0, 29, 4, '通用字段', 1, 'published', 1, '状态', 'admin', NOW(), 'admin', NOW()),
(134, 'DESCRIPTION', '描述', 'text', NULL, NULL, 0, NULL, 4, '通用字段', 0, 'published', 1, '描述说明', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 完成！
-- ============================================
