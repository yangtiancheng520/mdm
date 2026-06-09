-- =============================================
-- 初始化值域项的编码和值
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 1. 性别值域
UPDATE std_value_domain_item SET item_code = 'M', item_value = '男' WHERE domain_id = 60 AND item_value = 'M';
UPDATE std_value_domain_item SET item_code = 'F', item_value = '女' WHERE domain_id = 60 AND item_value = 'F';
UPDATE std_value_domain_item SET item_code = 'U', item_value = '未知' WHERE domain_id = 60 AND item_value = 'U';

-- 2. 国家代码值域
UPDATE std_value_domain_item SET item_code = 'CN', item_value = '中国' WHERE domain_id = 61 AND item_value = 'CN';
UPDATE std_value_domain_item SET item_code = 'US', item_value = '美国' WHERE domain_id = 61 AND item_value = 'US';
UPDATE std_value_domain_item SET item_code = 'JP', item_value = '日本' WHERE domain_id = 61 AND item_value = 'JP';
UPDATE std_value_domain_item SET item_code = 'DE', item_value = '德国' WHERE domain_id = 61 AND item_value = 'DE';
UPDATE std_value_domain_item SET item_code = 'UK', item_value = '英国' WHERE domain_id = 61 AND item_value = 'UK';

-- 3. 省份值域
UPDATE std_value_domain_item SET item_code = 'BJ', item_value = '北京市' WHERE domain_id = 62 AND item_value = '北京';
UPDATE std_value_domain_item SET item_code = 'SH', item_value = '上海市' WHERE domain_id = 62 AND item_value = '上海';
UPDATE std_value_domain_item SET item_code = 'GD', item_value = '广东省' WHERE domain_id = 62 AND item_value = '广东';
UPDATE std_value_domain_item SET item_code = 'ZJ', item_value = '浙江省' WHERE domain_id = 62 AND item_value = '浙江';
UPDATE std_value_domain_item SET item_code = 'JS', item_value = '江苏省' WHERE domain_id = 62 AND item_value = '江苏';
UPDATE std_value_domain_item SET item_code = 'SC', item_value = '四川省' WHERE domain_id = 62 AND item_value = '四川';

-- 4. 是/否值域
UPDATE std_value_domain_item SET item_code = 'Y', item_value = '是' WHERE domain_id = 63 AND item_value = '是';
UPDATE std_value_domain_item SET item_code = 'N', item_value = '否' WHERE domain_id = 63 AND item_value = '否';

-- 5. 启用状态值域
UPDATE std_value_domain_item SET item_code = 'ACTIVE', item_value = '启用' WHERE domain_id = 64 AND item_value = '启用';
UPDATE std_value_domain_item SET item_code = 'INACTIVE', item_value = '停用' WHERE domain_id = 64 AND item_value = '停用';

-- 6. 物料类型值域
UPDATE std_value_domain_item SET item_code = 'FERT', item_value = '成品' WHERE domain_id = 65 AND item_value = '成品';
UPDATE std_value_domain_item SET item_code = 'HALB', item_value = '半成品' WHERE domain_id = 65 AND item_value = '半成品';
UPDATE std_value_domain_item SET item_code = 'ROH', item_value = '原材料' WHERE domain_id = 65 AND item_value = '原材料';
UPDATE std_value_domain_item SET item_code = 'VERP', item_value = '包装材料' WHERE domain_id = 65 AND item_value = '包装材料';
UPDATE std_value_domain_item SET item_code = 'HIBE', item_value = '运营物资' WHERE domain_id = 65 AND item_value = '运营物资';
UPDATE std_value_domain_item SET item_code = 'DIEN', item_value = '服务' WHERE domain_id = 65 AND item_value = '服务';

-- 7. 物料组值域
UPDATE std_value_domain_item SET item_code = 'ELEC', item_value = '电子产品' WHERE domain_id = 66 AND item_value = '电子产品';
UPDATE std_value_domain_item SET item_code = 'MECH', item_value = '机械配件' WHERE domain_id = 66 AND item_value = '机械配件';
UPDATE std_value_domain_item SET item_code = 'CHEM', item_value = '化工原料' WHERE domain_id = 66 AND item_value = '化工原料';
UPDATE std_value_domain_item SET item_code = 'PACK', item_value = '包装材料' WHERE domain_id = 66 AND item_value = '包装材料';
UPDATE std_value_domain_item SET item_code = 'OFFC', item_value = '办公用品' WHERE domain_id = 66 AND item_value = '办公用品';

-- 8. 基本单位值域
UPDATE std_value_domain_item SET item_code = 'PC', item_value = '件' WHERE domain_id = 67 AND item_value = '件';
UPDATE std_value_domain_item SET item_code = 'EA', item_value = '个' WHERE domain_id = 67 AND item_value = '个';
UPDATE std_value_domain_item SET item_code = 'KG', item_value = '千克' WHERE domain_id = 67 AND item_value = '千克';
UPDATE std_value_domain_item SET item_code = 'M', item_value = '米' WHERE domain_id = 67 AND item_value = '米';
UPDATE std_value_domain_item SET item_code = 'L', item_value = '升' WHERE domain_id = 67 AND item_value = '升';
UPDATE std_value_domain_item SET item_code = 'SET', item_value = '套' WHERE domain_id = 67 AND item_value = '套';

-- 9. 客户组值域
UPDATE std_value_domain_item SET item_code = 'STRATEGIC', item_value = '战略客户' WHERE domain_id = 68 AND item_value = '战略客户';
UPDATE std_value_domain_item SET item_code = 'KEY', item_value = '重点客户' WHERE domain_id = 68 AND item_value = '重点客户';
UPDATE std_value_domain_item SET item_code = 'NORMAL', item_value = '一般客户' WHERE domain_id = 68 AND item_value = '一般客户';
UPDATE std_value_domain_item SET item_code = 'POTENTIAL', item_value = '潜在客户' WHERE domain_id = 68 AND item_value = '潜在客户';

-- 10. 供应商组值域
UPDATE std_value_domain_item SET item_code = 'STRATEGIC', item_value = '战略供应商' WHERE domain_id = 69 AND item_value = '战略供应商';
UPDATE std_value_domain_item SET item_code = 'QUALIFIED', item_value = '合格供应商' WHERE domain_id = 69 AND item_value = '合格供应商';
UPDATE std_value_domain_item SET item_code = 'TEMP', item_value = '临时供应商' WHERE domain_id = 69 AND item_value = '临时供应商';
UPDATE std_value_domain_item SET item_code = 'POTENTIAL', item_value = '潜在供应商' WHERE domain_id = 69 AND item_value = '潜在供应商';

-- 11. 公司代码值域
UPDATE std_value_domain_item SET item_code = 'HQ', item_value = '总公司' WHERE domain_id = 70 AND item_value = '总公司';
UPDATE std_value_domain_item SET item_code = 'EC', item_value = '华东分公司' WHERE domain_id = 70 AND item_value = '华东分公司';
UPDATE std_value_domain_item SET item_code = 'SC', item_value = '华南分公司' WHERE domain_id = 70 AND item_value = '华南分公司';
UPDATE std_value_domain_item SET item_code = 'NC', item_value = '华北分公司' WHERE domain_id = 70 AND item_value = '华北分公司';

-- 12. 销售组织值域
UPDATE std_value_domain_item SET item_code = 'EC_SALES', item_value = '华东销售部' WHERE domain_id = 71 AND item_value = '华东销售部';
UPDATE std_value_domain_item SET item_code = 'SC_SALES', item_value = '华南销售部' WHERE domain_id = 71 AND item_value = '华南销售部';
UPDATE std_value_domain_item SET item_code = 'NC_SALES', item_value = '华北销售部' WHERE domain_id = 71 AND item_value = '华北销售部';
UPDATE std_value_domain_item SET item_code = 'SW_SALES', item_value = '西南销售部' WHERE domain_id = 71 AND item_value = '西南销售部';

-- 13. 采购组织值域
UPDATE std_value_domain_item SET item_code = 'CENTRAL', item_value = '集中采购部' WHERE domain_id = 72 AND item_value = '集中采购部';
UPDATE std_value_domain_item SET item_code = 'EC_PUR', item_value = '华东采购部' WHERE domain_id = 72 AND item_value = '华东采购部';
UPDATE std_value_domain_item SET item_code = 'SC_PUR', item_value = '华南采购部' WHERE domain_id = 72 AND item_value = '华南采购部';

-- 14. 付款条款值域
UPDATE std_value_domain_item SET item_code = 'COD', item_value = '货到付款' WHERE domain_id = 73 AND item_value = '货到付款';
UPDATE std_value_domain_item SET item_code = 'N30', item_value = '30天月结' WHERE domain_id = 73 AND item_value = '30天月结';
UPDATE std_value_domain_item SET item_code = 'N60', item_value = '60天月结' WHERE domain_id = 73 AND item_value = '60天月结';
UPDATE std_value_domain_item SET item_code = 'N90', item_value = '90天月结' WHERE domain_id = 73 AND item_value = '90天月结';
UPDATE std_value_domain_item SET item_code = 'PRE30', item_value = '预付款30%' WHERE domain_id = 73 AND item_value = '预付款30%';

-- 15. 货币值域
UPDATE std_value_domain_item SET item_code = 'CNY', item_value = '人民币' WHERE domain_id = 74 AND item_value = '人民币';
UPDATE std_value_domain_item SET item_code = 'USD', item_value = '美元' WHERE domain_id = 74 AND item_value = '美元';
UPDATE std_value_domain_item SET item_code = 'EUR', item_value = '欧元' WHERE domain_id = 74 AND item_value = '欧元';
UPDATE std_value_domain_item SET item_code = 'JPY', item_value = '日元' WHERE domain_id = 74 AND item_value = '日元';
UPDATE std_value_domain_item SET item_code = 'GBP', item_value = '英镑' WHERE domain_id = 74 AND item_value = '英镑';

-- 16. 行业分类值域
UPDATE std_value_domain_item SET item_code = 'MFG', item_value = '制造业' WHERE domain_id = 75 AND item_value = '制造业';
UPDATE std_value_domain_item SET item_code = 'RTL', item_value = '零售业' WHERE domain_id = 75 AND item_value = '零售业';
UPDATE std_value_domain_item SET item_code = 'WHL', item_value = '批发业' WHERE domain_id = 75 AND item_value = '批发业';
UPDATE std_value_domain_item SET item_code = 'SVC', item_value = '服务业' WHERE domain_id = 75 AND item_value = '服务业';
UPDATE std_value_domain_item SET item_code = 'FIN', item_value = '金融业' WHERE domain_id = 75 AND item_value = '金融业';

-- 17. 信用等级值域
UPDATE std_value_domain_item SET item_code = 'A', item_value = 'A级-优秀' WHERE domain_id = 76 AND item_value = 'A级-优秀';
UPDATE std_value_domain_item SET item_code = 'B', item_value = 'B级-良好' WHERE domain_id = 76 AND item_value = 'B级-良好';
UPDATE std_value_domain_item SET item_code = 'C', item_value = 'C级-一般' WHERE domain_id = 76 AND item_value = 'C级-一般';
UPDATE std_value_domain_item SET item_code = 'D', item_value = 'D级-较差' WHERE domain_id = 76 AND item_value = 'D级-较差';

-- 18. 运输方式值域
UPDATE std_value_domain_item SET item_code = 'TRUCK', item_value = '公路运输' WHERE domain_id = 77 AND item_value = '公路运输';
UPDATE std_value_domain_item SET item_code = 'RAIL', item_value = '铁路运输' WHERE domain_id = 77 AND item_value = '铁路运输';
UPDATE std_value_domain_item SET item_code = 'AIR', item_value = '航空运输' WHERE domain_id = 77 AND item_value = '航空运输';
UPDATE std_value_domain_item SET item_code = 'SEA', item_value = '海运' WHERE domain_id = 77 AND item_value = '海运';
UPDATE std_value_domain_item SET item_code = 'EXPR', item_value = '快递' WHERE domain_id = 77 AND item_value = '快递';

-- 19. 国际贸易术语值域
UPDATE std_value_domain_item SET item_code = 'EXW', item_value = '工厂交货' WHERE domain_id = 78 AND item_value = '工厂交货';
UPDATE std_value_domain_item SET item_code = 'FOB', item_value = '离岸价' WHERE domain_id = 78 AND item_value = '离岸价';
UPDATE std_value_domain_item SET item_code = 'CIF', item_value = '到岸价' WHERE domain_id = 78 AND item_value = '到岸价';
UPDATE std_value_domain_item SET item_code = 'DDP', item_value = '完税交货' WHERE domain_id = 78 AND item_value = '完税交货';

-- 20. 语言值域
UPDATE std_value_domain_item SET item_code = 'ZH', item_value = '中文' WHERE domain_id = 79 AND item_value = '中文';
UPDATE std_value_domain_item SET item_code = 'EN', item_value = '英语' WHERE domain_id = 79 AND item_value = '英语';
UPDATE std_value_domain_item SET item_code = 'JA', item_value = '日语' WHERE domain_id = 79 AND item_value = '日语';
UPDATE std_value_domain_item SET item_code = 'DE', item_value = '德语' WHERE domain_id = 79 AND item_value = '德语';

-- 查看初始化结果
SELECT '========== 初始化完成 ==========' AS '';

SELECT
    vd.domain_code AS '值域编码',
    vd.domain_name AS '值域名称',
    vdi.item_code AS '项编码',
    vdi.item_value AS '项值',
    vdi.sort AS '排序'
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
ORDER BY vd.domain_code, vdi.sort
LIMIT 50;

SELECT CONCAT('已初始化 ', COUNT(*), ' 个值域项') AS '结果'
FROM std_value_domain_item
WHERE item_code IS NOT NULL;
