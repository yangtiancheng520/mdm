-- ============================================
-- SAP常见值域初始化脚本（完整版）
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================
-- 1. 插入值域（包含所有字段）
-- ============================================
INSERT INTO std_value_domain (id, domain_code, domain_name, data_type, data_length, domain_type, parent_id, level, status, sort, description, created_by, created_at, updated_by, updated_at) VALUES

-- 物料相关值域
(1, 'MATL_TYPE', '物料类型', 'varchar', 4, 'enum', NULL, 1, 'active', 1, 'SAP物料类型', 'admin', NOW(), 'admin', NOW()),
(2, 'IND_SECTOR', '行业领域', 'varchar', 1, 'enum', NULL, 1, 'active', 2, 'SAP行业领域', 'admin', NOW(), 'admin', NOW()),
(3, 'BASE_UOM', '基本计量单位', 'varchar', 3, 'enum', NULL, 1, 'active', 3, '基本计量单位', 'admin', NOW(), 'admin', NOW()),
(4, 'MATL_GROUP', '物料组', 'varchar', 9, 'enum', NULL, 1, 'active', 4, '物料分组', 'admin', NOW(), 'admin', NOW()),
(5, 'PUR_GROUP', '采购组', 'varchar', 3, 'enum', NULL, 1, 'active', 5, '采购组', 'admin', NOW(), 'admin', NOW()),
(6, 'PUR_ORG', '采购组织', 'varchar', 4, 'enum', NULL, 1, 'active', 6, '采购组织', 'admin', NOW(), 'admin', NOW()),
(7, 'MRP_TYPE', 'MRP类型', 'varchar', 2, 'enum', NULL, 1, 'active', 7, 'MRP类型', 'admin', NOW(), 'admin', NOW()),
(8, 'MRP_CTRL', 'MRP控制者', 'varchar', 3, 'enum', NULL, 1, 'active', 8, 'MRP控制者', 'admin', NOW(), 'admin', NOW()),
(9, 'PROC_TYPE', '采购类型', 'varchar', 1, 'enum', NULL, 1, 'active', 9, '采购类型', 'admin', NOW(), 'admin', NOW()),
(10, 'PRICE_CTRL', '价格控制', 'varchar', 1, 'enum', NULL, 1, 'active', 10, '价格控制', 'admin', NOW(), 'admin', NOW()),
(11, 'VAL_CLASS', '评估类', 'varchar', 4, 'enum', NULL, 1, 'active', 11, '评估类', 'admin', NOW(), 'admin', NOW()),

-- 通用值域
(12, 'COUNTRY', '国家代码', 'varchar', 3, 'enum', NULL, 1, 'active', 12, '国家代码', 'admin', NOW(), 'admin', NOW()),
(13, 'CURRENCY', '货币', 'varchar', 5, 'enum', NULL, 1, 'active', 13, '货币代码', 'admin', NOW(), 'admin', NOW()),
(14, 'PAYMENT_TERM', '付款条件', 'varchar', 4, 'enum', NULL, 1, 'active', 14, '付款条件', 'admin', NOW(), 'admin', NOW()),
(15, 'INCOTERM', '国际贸易条款', 'varchar', 3, 'enum', NULL, 1, 'active', 15, '国际贸易条款', 'admin', NOW(), 'admin', NOW()),
(16, 'LANGUAGE', '语言', 'varchar', 2, 'enum', NULL, 1, 'active', 16, '语言代码', 'admin', NOW(), 'admin', NOW()),
(17, 'REGION', '地区/省份', 'varchar', 3, 'enum', NULL, 1, 'active', 17, '地区/省份', 'admin', NOW(), 'admin', NOW()),

-- 供应商相关值域
(18, 'VENDOR_GROUP', '供应商组', 'varchar', 4, 'enum', NULL, 1, 'active', 18, '供应商组', 'admin', NOW(), 'admin', NOW()),
(19, 'VENDOR_SCHEME', '供应商方案', 'varchar', 4, 'enum', NULL, 1, 'active', 19, '供应商方案', 'admin', NOW(), 'admin', NOW()),
(20, 'TAX_TYPE', '税分类', 'varchar', 1, 'enum', NULL, 1, 'active', 20, '税分类', 'admin', NOW(), 'admin', NOW()),
(21, 'RECON_ACCT', '统驭科目', 'varchar', 10, 'enum', NULL, 1, 'active', 21, '统驭科目', 'admin', NOW(), 'admin', NOW()),

-- 客户相关值域
(22, 'CUST_GROUP', '客户组', 'varchar', 2, 'enum', NULL, 1, 'active', 22, '客户组', 'admin', NOW(), 'admin', NOW()),
(23, 'SALES_ORG', '销售组织', 'varchar', 4, 'enum', NULL, 1, 'active', 23, '销售组织', 'admin', NOW(), 'admin', NOW()),
(24, 'DISTR_CHAN', '分销渠道', 'varchar', 2, 'enum', NULL, 1, 'active', 24, '分销渠道', 'admin', NOW(), 'admin', NOW()),
(25, 'DIVISION', '产品组', 'varchar', 2, 'enum', NULL, 1, 'active', 25, '产品组', 'admin', NOW(), 'admin', NOW()),
(26, 'SHIP_COND', '装运条件', 'varchar', 2, 'enum', NULL, 1, 'active', 26, '装运条件', 'admin', NOW(), 'admin', NOW()),
(27, 'CUST_PRIC_PROC', '客户定价过程', 'varchar', 2, 'enum', NULL, 1, 'active', 27, '客户定价过程', 'admin', NOW(), 'admin', NOW()),

-- 状态值域
(28, 'YES_NO', '是/否', 'varchar', 1, 'enum', NULL, 1, 'active', 28, '是/否', 'admin', NOW(), 'admin', NOW()),
(29, 'STATUS', '状态', 'varchar', 20, 'enum', NULL, 1, 'active', 29, '通用状态', 'admin', NOW(), 'admin', NOW()),
(30, 'DELETE_FLAG', '删除标记', 'varchar', 1, 'enum', NULL, 1, 'active', 30, '删除标记', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 2. 插入值域项（包含所有字段）
-- ============================================

-- 1. 物料类型 (MATL_TYPE)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(1, 1, 'ROH', '原材料', NULL, 1, 'active', '原材料', 'admin', NOW(), 'admin', NOW()),
(2, 1, 'HALB', '半成品', NULL, 2, 'active', '半成品', 'admin', NOW(), 'admin', NOW()),
(3, 1, 'FERT', '成品', NULL, 3, 'active', '成品', 'admin', NOW(), 'admin', NOW()),
(4, 1, 'HAWA', '贸易商品', NULL, 4, 'active', '贸易商品', 'admin', NOW(), 'admin', NOW()),
(5, 1, 'DIEN', '服务', NULL, 5, 'active', '服务', 'admin', NOW(), 'admin', NOW()),
(6, 1, 'NLAG', '非库存物料', NULL, 6, 'active', '非库存物料', 'admin', NOW(), 'admin', NOW()),
(7, 1, 'UNBW', '不可评估物料', NULL, 7, 'active', '不可评估物料', 'admin', NOW(), 'admin', NOW()),
(8, 1, 'LEER', '容器/包装', NULL, 8, 'active', '容器/包装', 'admin', NOW(), 'admin', NOW());

-- 2. 行业领域 (IND_SECTOR)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(9, 2, 'M', '机械工程', NULL, 1, 'active', '机械工程', 'admin', NOW(), 'admin', NOW()),
(10, 2, 'C', '化学工业', NULL, 2, 'active', '化学工业', 'admin', NOW(), 'admin', NOW()),
(11, 2, 'P', '医药工业', NULL, 3, 'active', '医药工业', 'admin', NOW(), 'admin', NOW()),
(12, 2, 'E', '电子工业', NULL, 4, 'active', '电子工业', 'admin', NOW(), 'admin', NOW()),
(13, 2, 'A', '汽车工业', NULL, 5, 'active', '汽车工业', 'admin', NOW(), 'admin', NOW()),
(14, 2, 'B', '建筑工业', NULL, 6, 'active', '建筑工业', 'admin', NOW(), 'admin', NOW()),
(15, 2, 'D', '耐用消费品', NULL, 7, 'active', '耐用消费品', 'admin', NOW(), 'admin', NOW()),
(16, 2, 'F', '食品工业', NULL, 8, 'active', '食品工业', 'admin', NOW(), 'admin', NOW());

-- 3. 计量单位 (BASE_UOM)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(17, 3, 'EA', '件', NULL, 1, 'active', '件', 'admin', NOW(), 'admin', NOW()),
(18, 3, 'PC', '个', NULL, 2, 'active', '个', 'admin', NOW(), 'admin', NOW()),
(19, 3, 'KG', '千克', NULL, 3, 'active', '千克', 'admin', NOW(), 'admin', NOW()),
(20, 3, 'G', '克', NULL, 4, 'active', '克', 'admin', NOW(), 'admin', NOW()),
(21, 3, 'M', '米', NULL, 5, 'active', '米', 'admin', NOW(), 'admin', NOW()),
(22, 3, 'L', '升', NULL, 6, 'active', '升', 'admin', NOW(), 'admin', NOW()),
(23, 3, 'M2', '平方米', NULL, 7, 'active', '平方米', 'admin', NOW(), 'admin', NOW()),
(24, 3, 'M3', '立方米', NULL, 8, 'active', '立方米', 'admin', NOW(), 'admin', NOW()),
(25, 3, 'KM', '千米', NULL, 9, 'active', '千米', 'admin', NOW(), 'admin', NOW()),
(26, 3, 'T', '吨', NULL, 10, 'active', '吨', 'admin', NOW(), 'admin', NOW()),
(27, 3, 'SET', '套', NULL, 11, 'active', '套', 'admin', NOW(), 'admin', NOW()),
(28, 3, 'BOX', '箱', NULL, 12, 'active', '箱', 'admin', NOW(), 'admin', NOW());

-- 4. 物料组 (MATL_GROUP)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(29, 4, '001', '钢材类', NULL, 1, 'active', '钢材类', 'admin', NOW(), 'admin', NOW()),
(30, 4, '002', '塑料类', NULL, 2, 'active', '塑料类', 'admin', NOW(), 'admin', NOW()),
(31, 4, '003', '电子元器件', NULL, 3, 'active', '电子元器件', 'admin', NOW(), 'admin', NOW()),
(32, 4, '004', '机械零件', NULL, 4, 'active', '机械零件', 'admin', NOW(), 'admin', NOW()),
(33, 4, '005', '包装材料', NULL, 5, 'active', '包装材料', 'admin', NOW(), 'admin', NOW()),
(34, 4, '006', '化工原料', NULL, 6, 'active', '化工原料', 'admin', NOW(), 'admin', NOW());

-- 5. 采购组 (PUR_GROUP)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(35, 5, '001', '原材料采购组', NULL, 1, 'active', '原材料采购组', 'admin', NOW(), 'admin', NOW()),
(36, 5, '002', '设备采购组', NULL, 2, 'active', '设备采购组', 'admin', NOW(), 'admin', NOW()),
(37, 5, '003', '辅料采购组', NULL, 3, 'active', '辅料采购组', 'admin', NOW(), 'admin', NOW()),
(38, 5, '004', '包装采购组', NULL, 4, 'active', '包装采购组', 'admin', NOW(), 'admin', NOW()),
(39, 5, '005', 'MRO采购组', NULL, 5, 'active', 'MRO采购组', 'admin', NOW(), 'admin', NOW());

-- 6. 采购组织 (PUR_ORG)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(40, 6, '1000', '华东采购组织', NULL, 1, 'active', '华东采购组织', 'admin', NOW(), 'admin', NOW()),
(41, 6, '2000', '华南采购组织', NULL, 2, 'active', '华南采购组织', 'admin', NOW(), 'admin', NOW()),
(42, 6, '3000', '华北采购组织', NULL, 3, 'active', '华北采购组织', 'admin', NOW(), 'admin', NOW()),
(43, 6, '4000', '西部采购组织', NULL, 4, 'active', '西部采购组织', 'admin', NOW(), 'admin', NOW());

-- 7. MRP类型 (MRP_TYPE)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(44, 7, 'PD', 'PD - 净变化计划', NULL, 1, 'active', '净变化计划', 'admin', NOW(), 'admin', NOW()),
(45, 7, 'PP', 'PP - 按期间计划', NULL, 2, 'active', '按期间计划', 'admin', NOW(), 'admin', NOW()),
(46, 7, 'VB', 'VB - 基于消耗的计划', NULL, 3, 'active', '基于消耗的计划', 'admin', NOW(), 'admin', NOW()),
(47, 7, 'ND', 'ND - 无计划', NULL, 4, 'active', '无计划', 'admin', NOW(), 'admin', NOW()),
(48, 7, 'M1', 'M1 - 主生产计划', NULL, 5, 'active', '主生产计划', 'admin', NOW(), 'admin', NOW());

-- 8. MRP控制者 (MRP_CTRL)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(49, 8, '001', 'MRP控制者001', NULL, 1, 'active', 'MRP控制者001', 'admin', NOW(), 'admin', NOW()),
(50, 8, '002', 'MRP控制者002', NULL, 2, 'active', 'MRP控制者002', 'admin', NOW(), 'admin', NOW()),
(51, 8, '003', 'MRP控制者003', NULL, 3, 'active', 'MRP控制者003', 'admin', NOW(), 'admin', NOW());

-- 9. 采购类型 (PROC_TYPE)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(52, 9, 'E', '外部采购', NULL, 1, 'active', '外部采购', 'admin', NOW(), 'admin', NOW()),
(53, 9, 'F', '内部生产', NULL, 2, 'active', '内部生产', 'admin', NOW(), 'admin', NOW()),
(54, 9, 'X', '两种方式均可', NULL, 3, 'active', '两种方式均可', 'admin', NOW(), 'admin', NOW());

-- 10. 价格控制 (PRICE_CTRL)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(55, 10, 'S', '标准价格', NULL, 1, 'active', '标准价格', 'admin', NOW(), 'admin', NOW()),
(56, 10, 'V', '移动平均价', NULL, 2, 'active', '移动平均价', 'admin', NOW(), 'admin', NOW());

-- 11. 评估类 (VAL_CLASS)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(57, 11, '3000', '原材料', NULL, 1, 'active', '原材料', 'admin', NOW(), 'admin', NOW()),
(58, 11, '3100', '半成品', NULL, 2, 'active', '半成品', 'admin', NOW(), 'admin', NOW()),
(59, 11, '3200', '成品', NULL, 3, 'active', '成品', 'admin', NOW(), 'admin', NOW()),
(60, 11, '3300', '贸易商品', NULL, 4, 'active', '贸易商品', 'admin', NOW(), 'admin', NOW()),
(61, 11, '7900', '包装材料', NULL, 5, 'active', '包装材料', 'admin', NOW(), 'admin', NOW());

-- 12. 国家代码 (COUNTRY)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(62, 12, 'CN', '中国', NULL, 1, 'active', '中国', 'admin', NOW(), 'admin', NOW()),
(63, 12, 'US', '美国', NULL, 2, 'active', '美国', 'admin', NOW(), 'admin', NOW()),
(64, 12, 'DE', '德国', NULL, 3, 'active', '德国', 'admin', NOW(), 'admin', NOW()),
(65, 12, 'JP', '日本', NULL, 4, 'active', '日本', 'admin', NOW(), 'admin', NOW()),
(66, 12, 'UK', '英国', NULL, 5, 'active', '英国', 'admin', NOW(), 'admin', NOW()),
(67, 12, 'FR', '法国', NULL, 6, 'active', '法国', 'admin', NOW(), 'admin', NOW()),
(68, 12, 'IT', '意大利', NULL, 7, 'active', '意大利', 'admin', NOW(), 'admin', NOW()),
(69, 12, 'KR', '韩国', NULL, 8, 'active', '韩国', 'admin', NOW(), 'admin', NOW());

-- 13. 货币 (CURRENCY)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(70, 13, 'CNY', '人民币', NULL, 1, 'active', '人民币', 'admin', NOW(), 'admin', NOW()),
(71, 13, 'USD', '美元', NULL, 2, 'active', '美元', 'admin', NOW(), 'admin', NOW()),
(72, 13, 'EUR', '欧元', NULL, 3, 'active', '欧元', 'admin', NOW(), 'admin', NOW()),
(73, 13, 'JPY', '日元', NULL, 4, 'active', '日元', 'admin', NOW(), 'admin', NOW()),
(74, 13, 'GBP', '英镑', NULL, 5, 'active', '英镑', 'admin', NOW(), 'admin', NOW()),
(75, 13, 'HKD', '港币', NULL, 6, 'active', '港币', 'admin', NOW(), 'admin', NOW());

-- 14. 付款条件 (PAYMENT_TERM)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(76, 14, '0001', '立即付款', NULL, 1, 'active', '立即付款', 'admin', NOW(), 'admin', NOW()),
(77, 14, 'ZB01', '月结30天', NULL, 2, 'active', '月结30天', 'admin', NOW(), 'admin', NOW()),
(78, 14, 'ZB02', '月结60天', NULL, 3, 'active', '月结60天', 'admin', NOW(), 'admin', NOW()),
(79, 14, 'ZB03', '月结90天', NULL, 4, 'active', '月结90天', 'admin', NOW(), 'admin', NOW()),
(80, 14, 'ZB04', '月结45天', NULL, 5, 'active', '月结45天', 'admin', NOW(), 'admin', NOW()),
(81, 14, 'ZB05', '预付30%', NULL, 6, 'active', '预付30%', 'admin', NOW(), 'admin', NOW());

-- 15. 国际贸易条款 (INCOTERM)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(82, 15, 'EXW', '工厂交货', NULL, 1, 'active', '工厂交货', 'admin', NOW(), 'admin', NOW()),
(83, 15, 'FOB', '离岸价', NULL, 2, 'active', '离岸价', 'admin', NOW(), 'admin', NOW()),
(84, 15, 'CIF', '到岸价', NULL, 3, 'active', '到岸价', 'admin', NOW(), 'admin', NOW()),
(85, 15, 'DDP', '完税后交货', NULL, 4, 'active', '完税后交货', 'admin', NOW(), 'admin', NOW()),
(86, 15, 'DAP', '目的地交货', NULL, 5, 'active', '目的地交货', 'admin', NOW(), 'admin', NOW()),
(87, 15, 'CFR', '成本加运费', NULL, 6, 'active', '成本加运费', 'admin', NOW(), 'admin', NOW());

-- 16. 语言 (LANGUAGE)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(88, 16, 'ZH', '中文', NULL, 1, 'active', '中文', 'admin', NOW(), 'admin', NOW()),
(89, 16, 'EN', '英语', NULL, 2, 'active', '英语', 'admin', NOW(), 'admin', NOW()),
(90, 16, 'DE', '德语', NULL, 3, 'active', '德语', 'admin', NOW(), 'admin', NOW()),
(91, 16, 'JA', '日语', NULL, 4, 'active', '日语', 'admin', NOW(), 'admin', NOW()),
(92, 16, 'KO', '韩语', NULL, 5, 'active', '韩语', 'admin', NOW(), 'admin', NOW());

-- 17. 地区/省份 (REGION)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(93, 17, 'BJ', '北京市', NULL, 1, 'active', '北京市', 'admin', NOW(), 'admin', NOW()),
(94, 17, 'SH', '上海市', NULL, 2, 'active', '上海市', 'admin', NOW(), 'admin', NOW()),
(95, 17, 'GD', '广东省', NULL, 3, 'active', '广东省', 'admin', NOW(), 'admin', NOW()),
(96, 17, 'JS', '江苏省', NULL, 4, 'active', '江苏省', 'admin', NOW(), 'admin', NOW()),
(97, 17, 'ZJ', '浙江省', NULL, 5, 'active', '浙江省', 'admin', NOW(), 'admin', NOW()),
(98, 17, 'SD', '山东省', NULL, 6, 'active', '山东省', 'admin', NOW(), 'admin', NOW()),
(99, 17, 'SC', '四川省', NULL, 7, 'active', '四川省', 'admin', NOW(), 'admin', NOW()),
(100, 17, 'HN', '河南省', NULL, 8, 'active', '河南省', 'admin', NOW(), 'admin', NOW()),
(101, 17, 'HB', '湖北省', NULL, 9, 'active', '湖北省', 'admin', NOW(), 'admin', NOW()),
(102, 17, 'FJ', '福建省', NULL, 10, 'active', '福建省', 'admin', NOW(), 'admin', NOW());

-- 18. 供应商组 (VENDOR_GROUP)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(103, 18, '001', '国内供应商', NULL, 1, 'active', '国内供应商', 'admin', NOW(), 'admin', NOW()),
(104, 18, '002', '国外供应商', NULL, 2, 'active', '国外供应商', 'admin', NOW(), 'admin', NOW()),
(105, 18, '003', '一次性供应商', NULL, 3, 'active', '一次性供应商', 'admin', NOW(), 'admin', NOW()),
(106, 18, '004', '关联供应商', NULL, 4, 'active', '关联供应商', 'admin', NOW(), 'admin', NOW());

-- 19. 供应商方案 (VENDOR_SCHEME)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(107, 19, '0001', '标准供应商', NULL, 1, 'active', '标准供应商', 'admin', NOW(), 'admin', NOW()),
(108, 19, '0002', '寄售供应商', NULL, 2, 'active', '寄售供应商', 'admin', NOW(), 'admin', NOW()),
(109, 19, '0003', '外包供应商', NULL, 3, 'active', '外包供应商', 'admin', NOW(), 'admin', NOW());

-- 20. 税分类 (TAX_TYPE)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(110, 20, '0', '免税', NULL, 1, 'active', '免税', 'admin', NOW(), 'admin', NOW()),
(111, 20, '1', '一般纳税人 13%', NULL, 2, 'active', '一般纳税人 13%', 'admin', NOW(), 'admin', NOW()),
(112, 20, '2', '小规模纳税人 3%', NULL, 3, 'active', '小规模纳税人 3%', 'admin', NOW(), 'admin', NOW()),
(113, 20, '3', '进口增值税', NULL, 4, 'active', '进口增值税', 'admin', NOW(), 'admin', NOW());

-- 21. 统驭科目 (RECON_ACCT)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(114, 21, '212100', '应付账款-供应商', NULL, 1, 'active', '应付账款-供应商', 'admin', NOW(), 'admin', NOW()),
(115, 21, '212200', '预付账款', NULL, 2, 'active', '预付账款', 'admin', NOW(), 'admin', NOW()),
(116, 21, '112100', '应收账款-客户', NULL, 3, 'active', '应收账款-客户', 'admin', NOW(), 'admin', NOW()),
(117, 21, '112200', '预收账款', NULL, 4, 'active', '预收账款', 'admin', NOW(), 'admin', NOW());

-- 22. 客户组 (CUST_GROUP)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(118, 22, '01', '国内客户', NULL, 1, 'active', '国内客户', 'admin', NOW(), 'admin', NOW()),
(119, 22, '02', '国外客户', NULL, 2, 'active', '国外客户', 'admin', NOW(), 'admin', NOW()),
(120, 22, '03', '分销商', NULL, 3, 'active', '分销商', 'admin', NOW(), 'admin', NOW()),
(121, 22, '04', '终端客户', NULL, 4, 'active', '终端客户', 'admin', NOW(), 'admin', NOW()),
(122, 22, '05', 'OEM客户', NULL, 5, 'active', 'OEM客户', 'admin', NOW(), 'admin', NOW());

-- 23. 销售组织 (SALES_ORG)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(123, 23, '1000', '华东销售组织', NULL, 1, 'active', '华东销售组织', 'admin', NOW(), 'admin', NOW()),
(124, 23, '2000', '华南销售组织', NULL, 2, 'active', '华南销售组织', 'admin', NOW(), 'admin', NOW()),
(125, 23, '3000', '华北销售组织', NULL, 3, 'active', '华北销售组织', 'admin', NOW(), 'admin', NOW()),
(126, 23, '4000', '海外销售组织', NULL, 4, 'active', '海外销售组织', 'admin', NOW(), 'admin', NOW());

-- 24. 分销渠道 (DISTR_CHAN)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(127, 24, '10', '直销', NULL, 1, 'active', '直销', 'admin', NOW(), 'admin', NOW()),
(128, 24, '20', '分销', NULL, 2, 'active', '分销', 'admin', NOW(), 'admin', NOW()),
(129, 24, '30', '电商', NULL, 3, 'active', '电商', 'admin', NOW(), 'admin', NOW()),
(130, 24, '40', '代理', NULL, 4, 'active', '代理', 'admin', NOW(), 'admin', NOW());

-- 25. 产品组 (DIVISION)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(131, 25, '01', '标准产品', NULL, 1, 'active', '标准产品', 'admin', NOW(), 'admin', NOW()),
(132, 25, '02', '定制产品', NULL, 2, 'active', '定制产品', 'admin', NOW(), 'admin', NOW()),
(133, 25, '03', '服务产品', NULL, 3, 'active', '服务产品', 'admin', NOW(), 'admin', NOW());

-- 26. 装运条件 (SHIP_COND)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(134, 26, '01', '标准交货', NULL, 1, 'active', '标准交货', 'admin', NOW(), 'admin', NOW()),
(135, 26, '02', '加急交货', NULL, 2, 'active', '加急交货', 'admin', NOW(), 'admin', NOW()),
(136, 26, '03', '分批交货', NULL, 3, 'active', '分批交货', 'admin', NOW(), 'admin', NOW()),
(137, 26, '04', '自提', NULL, 4, 'active', '自提', 'admin', NOW(), 'admin', NOW());

-- 27. 客户定价过程 (CUST_PRIC_PROC)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(138, 27, '01', '标准定价', NULL, 1, 'active', '标准定价', 'admin', NOW(), 'admin', NOW()),
(139, 27, '02', '促销定价', NULL, 2, 'active', '促销定价', 'admin', NOW(), 'admin', NOW()),
(140, 27, '03', '合同定价', NULL, 3, 'active', '合同定价', 'admin', NOW(), 'admin', NOW());

-- 28. 是/否 (YES_NO)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(141, 28, 'Y', '是', NULL, 1, 'active', '是', 'admin', NOW(), 'admin', NOW()),
(142, 28, 'N', '否', NULL, 2, 'active', '否', 'admin', NOW(), 'admin', NOW());

-- 29. 状态 (STATUS)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(143, 29, 'ACTIVE', '有效', NULL, 1, 'active', '有效', 'admin', NOW(), 'admin', NOW()),
(144, 29, 'INACTIVE', '无效', NULL, 2, 'active', '无效', 'admin', NOW(), 'admin', NOW()),
(145, 29, 'BLOCKED', '冻结', NULL, 3, 'active', '冻结', 'admin', NOW(), 'admin', NOW());

-- 30. 删除标记 (DELETE_FLAG)
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, parent_item_id, sort, status, description, created_by, created_at, updated_by, updated_at) VALUES
(146, 30, '', '未删除', NULL, 1, 'active', '未删除', 'admin', NOW(), 'admin', NOW()),
(147, 30, 'X', '已标记删除', NULL, 2, 'active', '已标记删除', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 完成！共30个值域，147个值域项
-- ============================================
