-- =============================================
-- 数据字典字段分类分配报告
-- =============================================

USE mdm;

-- 1. 总体统计
SELECT '========================================' AS '';
SELECT '数据字典字段分类分配报告' AS '';
SELECT '========================================' AS '';

SELECT
    CONCAT('总字段数: ', COUNT(*)) AS统计信息
FROM std_field_standard;

SELECT
    CONCAT('已分配分类: ', COUNT(*)) AS 统计信息
FROM std_field_standard
WHERE category_id IS NOT NULL;

SELECT
    CONCAT('未分类字段: ', COUNT(*)) AS 统计信息
FROM std_field_standard
WHERE category_id IS NULL;

-- 2. 各主分类统计
SELECT '========================================' AS '';
SELECT '一、主分类统计' AS '';
SELECT '========================================' AS '';

SELECT
    p.category_name AS '主分类',
    COUNT(fs.id) AS '字段数量'
FROM std_field_category p
LEFT JOIN std_field_category c ON c.parent_id = p.id
LEFT JOIN std_field_standard fs ON fs.category_id = c.id
WHERE p.parent_id IS NULL
GROUP BY p.id, p.category_name
ORDER BY p.sort;

-- 3. 详细分类统计
SELECT '========================================' AS '';
SELECT '二、详细分类统计' AS '';
SELECT '========================================' AS '';

-- 客户主数据
SELECT '【客户主数据】' AS '';
SELECT
    c.category_name AS '子分类',
    COUNT(fs.id) AS '字段数量',
    GROUP_CONCAT(fs.field_name ORDER BY fs.field_code SEPARATOR '、') AS '字段列表'
FROM std_field_category c
INNER JOIN std_field_standard fs ON fs.category_id = c.id
WHERE c.parent_id = (SELECT id FROM std_field_category WHERE category_code = 'CUSTOMER')
GROUP BY c.id, c.category_name
ORDER BY c.sort;

-- 供应商主数据
SELECT '【供应商主数据】' AS '';
SELECT
    c.category_name AS '子分类',
    COUNT(fs.id) AS '字段数量',
    GROUP_CONCAT(fs.field_name ORDER BY fs.field_code SEPARATOR '、') AS '字段列表'
FROM std_field_category c
INNER JOIN std_field_standard fs ON fs.category_id = c.id
WHERE c.parent_id = (SELECT id FROM std_field_category WHERE category_code = 'VENDOR')
GROUP BY c.id, c.category_name
ORDER BY c.sort;

-- 物料主数据
SELECT '【物料主数据】' AS '';
SELECT
    c.category_name AS '子分类',
    COUNT(fs.id) AS '字段数量',
    GROUP_CONCAT(fs.field_name ORDER BY fs.field_code SEPARATOR '、') AS '字段列表'
FROM std_field_category c
INNER JOIN std_field_standard fs ON fs.category_id = c.id
WHERE c.parent_id = (SELECT id FROM std_field_category WHERE category_code = 'MATERIAL')
GROUP BY c.id, c.category_name
ORDER BY c.sort;

-- 4. 枚举字段统计
SELECT '========================================' AS '';
SELECT '三、枚举字段统计' AS '';
SELECT '========================================' AS '';

SELECT
    p.category_name AS '主分类',
    c.category_name AS '子分类',
    fs.field_name AS '字段名称',
    vd.domain_name AS '关联值域'
FROM std_field_standard fs
INNER JOIN std_field_category c ON fs.category_id = c.id
INNER JOIN std_field_category p ON c.parent_id = p.id
INNER JOIN std_value_domain vd ON fs.domain_id = vd.id
WHERE fs.is_enum = 1
ORDER BY p.sort, c.sort, fs.field_code;

SELECT CONCAT('枚举字段总数: ', COUNT(*)) AS 统计信息
FROM std_field_standard
WHERE is_enum = 1;
