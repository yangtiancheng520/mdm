-- ============================================
-- SAP常见值域初始化脚本
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================
-- 1. 插入值域
-- ============================================
INSERT INTO std_value_domain (id, domain_code, domain_name, domain_type, status, sort, description, created_by, created_at, updated_by, updated_at) VALUES
-- 物料相关值域
(1, 'MATL_TYPE', '物料类型', 'list', 'active', 1, 'SAP物料类型', 'admin', NOW(), 'admin', NOW()),
(2, 'IND_SECTOR', '行业领域', 'list', 'active', 2, 'SAP行业领域', 'admin', NOW(), 'admin', NOW()),
(3, 'BASE_UOM', '基本计量单位', 'list', 'active', 3, '基本计量单位', 'admin', NOW(), 'admin', NOW()),
(4, 'MATL_GROUP', '物料组', 'list', 'active', 4, '物料分组', 'admin', NOW(), 'admin', NOW()),
(5, 'PUR_GROUP', '采购组', 'list', 'active', 5, '采购组', 'admin', NOW(), 'admin', NOW()),
(6, 'PUR_ORG', '采购组织', 'list', 'active', 6, '采购组织', 'admin', NOW(), 'admin', NOW()),
(7, 'MRP_TYPE', 'MRP类型', 'list', 'active', 7, 'MRP类型', 'admin', NOW(), 'admin', NOW()),
(8, 'MRP_CTRL', 'MRP控制者', 'list', 'active', 8, 'MRP控制者', 'admin', NOW(), 'admin', NOW()),
(9, 'PROC_TYPE', '采购类型', 'list', 'active', 9, '采购类型', 'admin', NOW(), 'admin', NOW()),
(10, 'PRICE_CTRL', '价格控制', 'list', 'active', 10, '价格控制', 'admin', NOW(), 'admin', NOW()),
(11, 'VAL_CLASS', '评估类', 'list', 'active', 11, '评估类', 'admin', NOW(), 'admin', NOW()),

-- 通用值域
(12, 'COUNTRY', '国家代码', 'list', 'active', 12, '国家代码', 'admin', NOW(), 'admin', NOW()),
(13, 'CURRENCY', '货币', 'list', 'active', 13, '货币代码', 'admin', NOW(), 'admin', NOW()),
(14, 'PAYMENT_TERM', '付款条件', 'list', 'active', 14, '付款条件', 'admin', NOW(), 'admin', NOW()),
(15, 'INCOTERM', '国际贸易条款', 'list', 'active', 15, '国际贸易条款', 'admin', NOW(), 'admin', NOW()),
(16, 'LANGUAGE', '语言', 'list', 'active', 16, '语言代码', 'admin', NOW(), 'admin', NOW()),
(17, 'REGION', '地区/省份', 'list', 'active', 17, '地区/省份', 'admin', NOW(), 'admin', NOW()),

-- 供应商相关值域
(18, 'VENDOR_GROUP', '供应商组', 'list', 'active', 18, '供应商组', 'admin', NOW(), 'admin', NOW()),
(19, 'VENDOR_SCHEME', '供应商方案', 'list', 'active', 19, '供应商方案', 'admin', NOW(), 'admin', NOW()),
(20, 'TAX_TYPE', '税分类', 'list', 'active', 20, '税分类', 'admin', NOW(), 'admin', NOW()),
(21, 'RECON_ACCT', '统驭科目', 'list', 'active', 21, '统驭科目', 'admin', NOW(), 'admin', NOW()),

-- 客户相关值域
(22, 'CUST_GROUP', '客户组', 'list', 'active', 22, '客户组', 'admin', NOW(), 'admin', NOW()),
(23, 'SALES_ORG', '销售组织', 'list', 'active', 23, '销售组织', 'admin', NOW(), 'admin', NOW()),
(24, 'DISTR_CHAN', '分销渠道', 'list', 'active', 24, '分销渠道', 'admin', NOW(), 'admin', NOW()),
(25, 'DIVISION', '产品组', 'list', 'active', 25, '产品组', 'admin', NOW(), 'admin', NOW()),
(26, 'SHIP_COND', '装运条件', 'list', 'active', 26, '装运条件', 'admin', NOW(), 'admin', NOW()),
(27, 'CUST_PRIC_PROC', '客户定价过程', 'list', 'active', 27, '客户定价过程', 'admin', NOW(), 'admin', NOW()),

-- 状态值域
(28, 'YES_NO', '是/否', 'list', 'active', 28, '是/否', 'admin', NOW(), 'admin', NOW()),
(29, 'STATUS', '状态', 'list', 'active', 29, '通用状态', 'admin', NOW(), 'admin', NOW()),
(30, 'DELETE_FLAG', '删除标记', 'list', 'active', 30, '删除标记', 'admin', NOW(), 'admin', NOW());

-- ============================================
-- 2. 插入值域项
-- ============================================

-- 1. 物料类型
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(1, 1, 'ROH', '原材料', 1, 'active', 'admin', NOW()),
(2, 1, 'HALB', '半成品', 2, 'active', 'admin', NOW()),
(3, 1, 'FERT', '成品', 3, 'active', 'admin', NOW()),
(4, 1, 'HAWA', '贸易商品', 4, 'active', 'admin', NOW()),
(5, 1, 'DIEN', '服务', 5, 'active', 'admin', NOW()),
(6, 1, 'NLAG', '非库存物料', 6, 'active', 'admin', NOW()),
(7, 1, 'UNBW', '不可评估物料', 7, 'active', 'admin', NOW()),
(8, 1, 'LEER', '容器/包装', 8, 'active', 'admin', NOW());

-- 2. 行业领域
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(9, 2, 'M', '机械工程', 1, 'active', 'admin', NOW()),
(10, 2, 'C', '化学工业', 2, 'active', 'admin', NOW()),
(11, 2, 'P', '医药工业', 3, 'active', 'admin', NOW()),
(12, 2, 'E', '电子工业', 4, 'active', 'admin', NOW()),
(13, 2, 'A', '汽车工业', 5, 'active', 'admin', NOW()),
(14, 2, 'B', '建筑工业', 6, 'active', 'admin', NOW()),
(15, 2, 'D', '耐用消费品', 7, 'active', 'admin', NOW()),
(16, 2, 'F', '食品工业', 8, 'active', 'admin', NOW());

-- 3. 计量单位
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(17, 3, 'EA', '件', 1, 'active', 'admin', NOW()),
(18, 3, 'PC', '个', 2, 'active', 'admin', NOW()),
(19, 3, 'KG', '千克', 3, 'active', 'admin', NOW()),
(20, 3, 'G', '克', 4, 'active', 'admin', NOW()),
(21, 3, 'M', '米', 5, 'active', 'admin', NOW()),
(22, 3, 'L', '升', 6, 'active', 'admin', NOW()),
(23, 3, 'M2', '平方米', 7, 'active', 'admin', NOW()),
(24, 3, 'M3', '立方米', 8, 'active', 'admin', NOW()),
(25, 3, 'KM', '千米', 9, 'active', 'admin', NOW()),
(26, 3, 'T', '吨', 10, 'active', 'admin', NOW()),
(27, 3, 'SET', '套', 11, 'active', 'admin', NOW()),
(28, 3, 'BOX', '箱', 12, 'active', 'admin', NOW());

-- 4. 物料组（示例）
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(29, 4, '001', '钢材类', 1, 'active', 'admin', NOW()),
(30, 4, '002', '塑料类', 2, 'active', 'admin', NOW()),
(31, 4, '003', '电子元器件', 3, 'active', 'admin', NOW()),
(32, 4, '004', '机械零件', 4, 'active', 'admin', NOW()),
(33, 4, '005', '包装材料', 5, 'active', 'admin', NOW()),
(34, 4, '006', '化工原料', 6, 'active', 'admin', NOW());

-- 5. 采购组
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(35, 5, '001', '原材料采购组', 1, 'active', 'admin', NOW()),
(36, 5, '002', '设备采购组', 2, 'active', 'admin', NOW()),
(37, 5, '003', '辅料采购组', 3, 'active', 'admin', NOW()),
(38, 5, '004', '包装采购组', 4, 'active', 'admin', NOW()),
(39, 5, '005', 'MRO采购组', 5, 'active', 'admin', NOW());

-- 6. 采购组织
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(40, 6, '1000', '华东采购组织', 1, 'active', 'admin', NOW()),
(41, 6, '2000', '华南采购组织', 2, 'active', 'admin', NOW()),
(42, 6, '3000', '华北采购组织', 3, 'active', 'admin', NOW()),
(43, 6, '4000', '西部采购组织', 4, 'active', 'admin', NOW());

-- 7. MRP类型
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(44, 7, 'PD', 'PD - 净变化计划', 1, 'active', 'admin', NOW()),
(45, 7, 'PP', 'PP - 按期间计划', 2, 'active', 'admin', NOW()),
(46, 7, 'VB', 'VB - 基于消耗的计划', 3, 'active', 'admin', NOW()),
(47, 7, 'ND', 'ND - 无计划', 4, 'active', 'admin', NOW()),
(48, 7, 'M1', 'M1 - 主生产计划', 5, 'active', 'admin', NOW());

-- 8. MRP控制者
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(49, 8, '001', 'MRP控制者001', 1, 'active', 'admin', NOW()),
(50, 8, '002', 'MRP控制者002', 2, 'active', 'admin', NOW()),
(51, 8, '003', 'MRP控制者003', 3, 'active', 'admin', NOW());

-- 9. 采购类型
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(52, 9, 'E', '外部采购', 1, 'active', 'admin', NOW()),
(53, 9, 'F', '内部生产', 2, 'active', 'admin', NOW()),
(54, 9, 'X', '两种方式均可', 3, 'active', 'admin', NOW());

-- 10. 价格控制
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(55, 10, 'S', '标准价格', 1, 'active', 'admin', NOW()),
(56, 10, 'V', '移动平均价', 2, 'active', 'admin', NOW());

-- 11. 评估类
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(57, 11, '3000', '原材料', 1, 'active', 'admin', NOW()),
(58, 11, '3100', '半成品', 2, 'active', 'admin', NOW()),
(59, 11, '3200', '成品', 3, 'active', 'admin', NOW()),
(60, 11, '3300', '贸易商品', 4, 'active', 'admin', NOW()),
(61, 11, '7900', '包装材料', 5, 'active', 'admin', NOW());

-- 12. 国家代码
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(62, 12, 'CN', '中国', 1, 'active', 'admin', NOW()),
(63, 12, 'US', '美国', 2, 'active', 'admin', NOW()),
(64, 12, 'DE', '德国', 3, 'active', 'admin', NOW()),
(65, 12, 'JP', '日本', 4, 'active', 'admin', NOW()),
(66, 12, 'UK', '英国', 5, 'active', 'admin', NOW()),
(67, 12, 'FR', '法国', 6, 'active', 'admin', NOW()),
(68, 12, 'IT', '意大利', 7, 'active', 'admin', NOW()),
(69, 12, 'KR', '韩国', 8, 'active', 'admin', NOW());

-- 13. 货币
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(70, 13, 'CNY', '人民币', 1, 'active', 'admin', NOW()),
(71, 13, 'USD', '美元', 2, 'active', 'admin', NOW()),
(72, 13, 'EUR', '欧元', 3, 'active', 'admin', NOW()),
(73, 13, 'JPY', '日元', 4, 'active', 'admin', NOW()),
(74, 13, 'GBP', '英镑', 5, 'active', 'admin', NOW()),
(75, 13, 'HKD', '港币', 6, 'active', 'admin', NOW());

-- 14. 付款条件
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(76, 14, '0001', '立即付款', 1, 'active', 'admin', NOW()),
(77, 14, 'ZB01', '月结30天', 2, 'active', 'admin', NOW()),
(78, 14, 'ZB02', '月结60天', 3, 'active', 'admin', NOW()),
(79, 14, 'ZB03', '月结90天', 4, 'active', 'admin', NOW()),
(80, 14, 'ZB04', '月结45天', 5, 'active', 'admin', NOW()),
(81, 14, 'ZB05', '预付30%', 6, 'active', 'admin', NOW());

-- 15. 国际贸易条款
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(82, 15, 'EXW', '工厂交货', 1, 'active', 'admin', NOW()),
(83, 15, 'FOB', '离岸价', 2, 'active', 'admin', NOW()),
(84, 15, 'CIF', '到岸价', 3, 'active', 'admin', NOW()),
(85, 15, 'DDP', '完税后交货', 4, 'active', 'admin', NOW()),
(86, 15, 'DAP', '目的地交货', 5, 'active', 'admin', NOW()),
(87, 15, 'CFR', '成本加运费', 6, 'active', 'admin', NOW());

-- 16. 语言
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(88, 16, 'ZH', '中文', 1, 'active', 'admin', NOW()),
(89, 16, 'EN', '英语', 2, 'active', 'admin', NOW()),
(90, 16, 'DE', '德语', 3, 'active', 'admin', NOW()),
(91, 16, 'JA', '日语', 4, 'active', 'admin', NOW()),
(92, 16, 'KO', '韩语', 5, 'active', 'admin', NOW());

-- 17. 地区/省份（中国）
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(93, 17, 'BJ', '北京市', 1, 'active', 'admin', NOW()),
(94, 17, 'SH', '上海市', 2, 'active', 'admin', NOW()),
(95, 17, 'GD', '广东省', 3, 'active', 'admin', NOW()),
(96, 17, 'JS', '江苏省', 4, 'active', 'admin', NOW()),
(97, 17, 'ZJ', '浙江省', 5, 'active', 'admin', NOW()),
(98, 17, 'SD', '山东省', 6, 'active', 'admin', NOW()),
(99, 17, 'SC', '四川省', 7, 'active', 'admin', NOW()),
(100, 17, 'HN', '河南省', 8, 'active', 'admin', NOW()),
(101, 17, 'HB', '湖北省', 9, 'active', 'admin', NOW()),
(102, 17, 'FJ', '福建省', 10, 'active', 'admin', NOW());

-- 18. 供应商组
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(103, 18, '001', '国内供应商', 1, 'active', 'admin', NOW()),
(104, 18, '002', '国外供应商', 2, 'active', 'admin', NOW()),
(105, 18, '003', '一次性供应商', 3, 'active', 'admin', NOW()),
(106, 18, '004', '关联供应商', 4, 'active', 'admin', NOW());

-- 19. 供应商方案
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(107, 19, '0001', '标准供应商', 1, 'active', 'admin', NOW()),
(108, 19, '0002', '寄售供应商', 2, 'active', 'admin', NOW()),
(109, 19, '0003', '外包供应商', 3, 'active', 'admin', NOW());

-- 20. 税分类
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(110, 20, '0', '免税', 1, 'active', 'admin', NOW()),
(111, 20, '1', '一般纳税人 13%', 2, 'active', 'admin', NOW()),
(112, 20, '2', '小规模纳税人 3%', 3, 'active', 'admin', NOW()),
(113, 20, '3', '进口增值税', 4, 'active', 'admin', NOW());

-- 21. 统驭科目
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(114, 21, '212100', '应付账款-供应商', 1, 'active', 'admin', NOW()),
(115, 21, '212200', '预付账款', 2, 'active', 'admin', NOW()),
(116, 21, '112100', '应收账款-客户', 3, 'active', 'admin', NOW()),
(117, 21, '112200', '预收账款', 4, 'active', 'admin', NOW());

-- 22. 客户组
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(118, 22, '01', '国内客户', 1, 'active', 'admin', NOW()),
(119, 22, '02', '国外客户', 2, 'active', 'admin', NOW()),
(120, 22, '03', '分销商', 3, 'active', 'admin', NOW()),
(121, 22, '04', '终端客户', 4, 'active', 'admin', NOW()),
(122, 22, '05', 'OEM客户', 5, 'active', 'admin', NOW());

-- 23. 销售组织
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(123, 23, '1000', '华东销售组织', 1, 'active', 'admin', NOW()),
(124, 23, '2000', '华南销售组织', 2, 'active', 'admin', NOW()),
(125, 23, '3000', '华北销售组织', 3, 'active', 'admin', NOW()),
(126, 23, '4000', '海外销售组织', 4, 'active', 'admin', NOW());

-- 24. 分销渠道
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(127, 24, '10', '直销', 1, 'active', 'admin', NOW()),
(128, 24, '20', '分销', 2, 'active', 'admin', NOW()),
(129, 24, '30', '电商', 3, 'active', 'admin', NOW()),
(130, 24, '40', '代理', 4, 'active', 'admin', NOW());

-- 25. 产品组
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(131, 25, '01', '标准产品', 1, 'active', 'admin', NOW()),
(132, 25, '02', '定制产品', 2, 'active', 'admin', NOW()),
(133, 25, '03', '服务产品', 3, 'active', 'admin', NOW());

-- 26. 装运条件
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(134, 26, '01', '标准交货', 1, 'active', 'admin', NOW()),
(135, 26, '02', '加急交货', 2, 'active', 'admin', NOW()),
(136, 26, '03', '分批交货', 3, 'active', 'admin', NOW()),
(137, 26, '04', '自提', 4, 'active', 'admin', NOW());

-- 27. 客户定价过程
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(138, 27, '01', '标准定价', 1, 'active', 'admin', NOW()),
(139, 27, '02', '促销定价', 2, 'active', 'admin', NOW()),
(140, 27, '03', '合同定价', 3, 'active', 'admin', NOW());

-- 28. 是/否
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(141, 28, 'Y', '是', 1, 'active', 'admin', NOW()),
(142, 28, 'N', '否', 2, 'active', 'admin', NOW());

-- 29. 状态
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(143, 29, 'ACTIVE', '有效', 1, 'active', 'admin', NOW()),
(144, 29, 'INACTIVE', '无效', 2, 'active', 'admin', NOW()),
(145, 29, 'BLOCKED', '冻结', 3, 'active', 'admin', NOW());

-- 30. 删除标记
INSERT INTO std_value_domain_item (id, domain_id, item_code, item_value, sort, status, created_by, created_at) VALUES
(146, 30, ' ', '未删除', 1, 'active', 'admin', NOW()),
(147, 30, 'X', '已标记删除', 2, 'active', 'admin', NOW());

-- ============================================
-- 完成！
-- ============================================
