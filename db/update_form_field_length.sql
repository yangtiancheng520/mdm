-- =============================================
-- 更新表单字段长度为数据字典长度
-- 由于表单字段存储在JSON中，需要使用存储过程处理
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 创建临时表存储字段标准长度
DROP TEMPORARY TABLE IF EXISTS temp_field_lengths;
CREATE TEMPORARY TABLE temp_field_lengths (
    field_code VARCHAR(100),
    field_name VARCHAR(100),
    field_length INT,
    PRIMARY KEY (field_code)
);

INSERT INTO temp_field_lengths
SELECT field_code, field_name, length
FROM std_field_standard
WHERE field_code IN (
    'COMPANY_CODE', 'CREDIT_LEVEL', 'CUSTOMER_GROUP', 'INCOTERM1',
    'PAYMENT_TERM', 'PURCHASE_ORG', 'SALES_ORG', 'SHIP_MODE',
    'BANK_COUNTRY', 'COUNTRY', 'GEWEI', 'INDUSTRY', 'MATKL', 'MEINS', 'REGION'
);

-- 查看需要更新的字段
SELECT '========== Fields to Update ==========' AS '';

SELECT
    field_code,
    field_name,
    field_length
FROM temp_field_lengths
ORDER BY field_code;

-- 由于MySQL JSON更新复杂，我们需要通过应用程序处理
-- 这里创建一个更新脚本供Java后端执行

SELECT '========== Form Fields Need Update ==========' AS '';

SELECT
    f.id,
    f.form_code,
    f.form_name,
    '需要通过程序更新JSON中的length字段' AS note
FROM frm_form f
WHERE f.layout_config IS NOT NULL
  AND f.layout_config != ''
  AND f.layout_config != '[]'
  AND f.layout_config != '{}';

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_field_lengths;

SELECT '========== Note ==========' AS '';
SELECT '表单字段长度存储在layout_config的JSON中，建议通过后端程序更新' AS '提示';
SELECT '可以重置表单的layout_config，让系统重新从视图字段获取最新长度' AS '建议';
