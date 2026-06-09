-- =============================================
-- Check value domain length vs item max length
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 1. Query all domain defined length vs item actual max length
SELECT
    vd.id AS DomainID,
    vd.domain_code AS DomainCode,
    vd.domain_name AS DomainName,
    vd.data_type AS DataType,
    vd.data_length AS DefinedLength,
    IFNULL(MAX(LENGTH(vdi.item_value)), 0) AS MaxItemLength,
    CASE
        WHEN vd.data_type != 'string' THEN 'Non-String Type'
        WHEN vd.data_length IS NULL THEN 'Warning: No Length Defined'
        WHEN MAX(LENGTH(vdi.item_value)) IS NULL THEN 'No Items'
        WHEN MAX(LENGTH(vdi.item_value)) > vd.data_length THEN 'Error: Length Mismatch'
        ELSE 'OK'
    END AS Status
FROM std_value_domain vd
LEFT JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id AND vdi.status = '启用'
GROUP BY vd.id, vd.domain_code, vd.domain_name, vd.data_type, vd.data_length
ORDER BY
    CASE
        WHEN vd.data_type = 'string' AND vd.data_length IS NOT NULL
             AND MAX(LENGTH(vdi.item_value)) > vd.data_length
        THEN 0
        ELSE 1
    END,
    vd.domain_code;

-- 2. Only show problematic domains (string type and length mismatch)
SELECT
    vd.id AS DomainID,
    vd.domain_code AS DomainCode,
    vd.domain_name AS DomainName,
    vd.data_length AS DefinedLength,
    MAX(LENGTH(vdi.item_value)) AS MaxItemLength,
    CONCAT('Need Update To: ', MAX(LENGTH(vdi.item_value))) AS Suggestion
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id AND vdi.status = '启用'
WHERE vd.data_type = 'string'
  AND vd.data_length IS NOT NULL
  AND vd.data_length < MAX(LENGTH(vdi.item_value))
GROUP BY vd.id, vd.domain_code, vd.domain_name, vd.data_length;

-- 3. Show all string type domain details (including each item value and length)
SELECT
    vd.domain_code AS DomainCode,
    vd.domain_name AS DomainName,
    vd.data_length AS DefinedLength,
    vdi.item_value AS ItemValue,
    LENGTH(vdi.item_value) AS ActualLength,
    CASE
        WHEN LENGTH(vdi.item_value) > vd.data_length THEN 'Error: Too Long'
        ELSE 'OK'
    END AS Status
FROM std_value_domain vd
INNER JOIN std_value_domain_item vdi ON vd.id = vdi.domain_id
WHERE vd.data_type = 'string'
ORDER BY vd.domain_code, LENGTH(vdi.item_value) DESC;
