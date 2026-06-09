-- =============================================
-- 修复视图字段长度与值域项长度不一致的问题
-- 问题描述：视图字段定义的长度太小，无法存储中文值域项
-- =============================================

USE mdm;

-- 1. 修复供应商组字段长度 (原长度4，值域项为"战略供应商"等中文，需要20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'VENDOR_GROUP';

-- 2. 修复行业分类字段长度 (原长度4，值域项为"制造业"等中文，需要20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'INDUSTRY';

-- 3. 修复客户组字段长度 (原长度2，值域项为"战略客户"等中文，需要20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'CUSTOMER_GROUP';

-- 4. 修复销售组织字段长度 (原长度4，值域项为"华东销售部"等中文，需要20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'SALES_ORG';

-- 5. 修复采购组织字段长度 (原长度4，值域项为"集中采购部"等中文，需要20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'PURCHASE_ORG';

-- 6. 修复付款条款字段长度 (原长度4，值域项为"货到付款"等中文，需要20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'PAYMENT_TERM';

-- 7. 修复运输方式字段长度 (原长度2，值域项为"公路运输"等中文，需要20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'SHIP_MODE';

-- 8. 修复国际贸易术语字段长度 (原长度3，值域项为"工厂交货"等中文，需要20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'INCOTERM1';

-- 9. 修复国家字段长度 (原长度3，值域项为"CN"等，但显示名可能是中文，建议20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'COUNTRY';

-- 10. 修复省份字段长度 (原长度3，值域项为"北京"等中文，需要20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'REGION';

-- 11. 修复货币字段长度 (原长度3，值域项为"人民币"等中文，需要20)
UPDATE std_view_field SET length = 20 WHERE field_code = 'ORDER_CURRENCY';

-- 12. 同步更新值域表的data_length字段
UPDATE std_value_domain SET data_length = 20 WHERE domain_code IN (
    'VENDOR_GROUP', 'INDUSTRY', 'CUSTOMER_GROUP', 'SALES_ORG',
    'PURCHASE_ORG', 'PAYMENT_TERM', 'SHIP_MODE', 'INCOTERM',
    'COUNTRY', 'PROVINCE', 'CURRENCY', 'LANGUAGE', 'CREDIT_LEVEL',
    'COMPANY_CODE', 'MATERIAL_TYPE', 'MATERIAL_GROUP', 'BASE_UNIT'
);

-- 13. 同步更新字段标准表的length字段
UPDATE std_field_standard SET length = 20 WHERE field_code IN (
    'VENDOR_GROUP', 'INDUSTRY', 'CUSTOMER_GROUP', 'SALES_ORG',
    'PURCHASE_ORG', 'PAYMENT_TERM', 'SHIP_MODE', 'INCOTERM1',
    'COUNTRY', 'REGION', 'ORDER_CURRENCY'
);

-- 验证修改结果
SELECT field_code, field_name, length
FROM std_view_field
WHERE field_code IN (
    'VENDOR_GROUP', 'INDUSTRY', 'CUSTOMER_GROUP', 'SALES_ORG',
    'PURCHASE_ORG', 'PAYMENT_TERM', 'SHIP_MODE', 'INCOTERM1',
    'COUNTRY', 'REGION', 'ORDER_CURRENCY'
)
ORDER BY field_code;

SELECT domain_code, domain_name, data_length
FROM std_value_domain
WHERE domain_code IN (
    'VENDOR_GROUP', 'INDUSTRY', 'CUSTOMER_GROUP', 'SALES_ORG',
    'PURCHASE_ORG', 'PAYMENT_TERM', 'SHIP_MODE', 'INCOTERM',
    'COUNTRY', 'PROVINCE', 'CURRENCY'
)
ORDER BY domain_code;

SELECT '字段长度修复完成！' AS message;
