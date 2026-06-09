-- =============================================
-- 初始化字段标准的字典分类
-- 根据字段编码前缀关联到对应的字典分类
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 查看当前字段标准的分类情况
SELECT '========== 初始化前字段分类情况 ==========' AS '';
SELECT field_code, field_name, category, category_id
FROM std_field_standard
ORDER BY field_code;

-- 更新字段标准的分类
-- 1. 客户主数据字段 -> PARTNER (合作伙伴)
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'PARTNER'
SET fs.category = fc.category_name,
    fs.category_id = fc.id
WHERE fs.field_code LIKE 'CUSTOMER%';

-- 2. 供应商主数据字段 -> PARTNER (合作伙伴)
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'PARTNER'
SET fs.category = fc.category_name,
    fs.category_id = fc.id
WHERE fs.field_code LIKE 'VENDOR%';

-- 3. 物料主数据字段 -> MATERIAL (物料属性)
UPDATE std_field_standard fs
INNER JOIN std_field_category fc ON fc.category_code = 'MATERIAL'
SET fs.category = fc.category_name,
    fs.category_id = fc.id
WHERE fs.field_code LIKE 'MATERIAL%';

-- 查看初始化结果
SELECT '========== 初始化后字段分类情况 ==========' AS '';

SELECT
    fs.field_code,
    fs.field_name,
    fs.category AS '字典分类',
    fc.category_code AS '分类编码',
    fc.category_name AS '分类名称'
FROM std_field_standard fs
LEFT JOIN std_field_category fc ON fs.category_id = fc.id
ORDER BY fc.category_code, fs.field_code;

-- 统计各分类的字段数量
SELECT
    fc.category_name AS '字典分类',
    COUNT(fs.id) AS '字段数量'
FROM std_field_category fc
LEFT JOIN std_field_standard fs ON fs.category_id = fc.id
GROUP BY fc.id, fc.category_name
ORDER BY fc.sort;

-- 查看未分类的字段
SELECT field_code, field_name, category, category_id
FROM std_field_standard
WHERE category_id IS NULL;

SELECT CONCAT('已更新 ', COUNT(*), ' 个字段的分类') AS result
FROM std_field_standard
WHERE category_id IS NOT NULL;