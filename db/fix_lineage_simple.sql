-- 血缘数据诊断和修复
USE mdm;

-- 1. 字段映射配置
SELECT '1. Field Mapping Config' as step;
SELECT data_type, COUNT(*) as count, GROUP_CONCAT(DISTINCT status) as status
FROM dis_field_mapping
GROUP BY data_type;

-- 2. Distribution Log Status
SELECT '2. Distribution Log Status' as step;
SELECT status, COUNT(*) as count,
       SUM(CASE WHEN field_count > 0 THEN 1 ELSE 0 END) as has_lineage
FROM dis_log_distribution
GROUP BY status;

-- 3. Lineage Data
SELECT '3. Lineage Data' as step;
SELECT COUNT(*) as total_records,
       COUNT(DISTINCT log_id) as log_count,
       COUNT(DISTINCT data_type) as data_types
FROM dis_field_lineage;

-- 4. Recent Distribution Logs
SELECT '4. Recent Distribution Logs' as step;
SELECT id, log_code, data_type, data_code, status,
       field_count, success_field_count, created_at
FROM dis_log_distribution
ORDER BY created_at DESC
LIMIT 10;

-- 5. Need Repair Logs
SELECT '5. Logs Need Repair' as step;
SELECT COUNT(*) as count
FROM dis_log_distribution
WHERE status = 'SUCCESS'
  AND (field_count IS NULL OR field_count = 0);

-- Fix: Activate all field mappings
UPDATE dis_field_mapping SET status = 'active' WHERE status != 'active';
SELECT CONCAT('Activated ', ROW_COUNT(), ' field mappings') as result;

-- Fix: Add Material field mappings
INSERT IGNORE INTO dis_field_mapping
(data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, sap_structure, field_type, is_required, is_key, transform_type, sort_order, status, created_by)
VALUES
('MATERIAL', 'Material', 'MATNR', 'Material Code', 'MATNR', 'Material Number', 'MATERIAL_DATA', 'STRING', 1, 1, 'DIRECT', 1, 'active', 'system'),
('MATERIAL', 'Material', 'MAKTX', 'Material Description', 'MAKTX', 'Material Description', 'MATERIAL_DATA', 'STRING', 1, 0, 'DIRECT', 2, 'active', 'system'),
('MATERIAL', 'Material', 'MTART', 'Material Type', 'MTART', 'Material Type', 'MATERIAL_DATA', 'STRING', 0, 0, 'DIRECT', 3, 'active', 'system'),
('MATERIAL', 'Material', 'MATKL', 'Material Group', 'MATKL', 'Material Group', 'MATERIAL_DATA', 'STRING', 0, 0, 'DIRECT', 4, 'active', 'system'),
('MATERIAL', 'Material', 'MEINS', 'Base Unit', 'MEINS', 'Base Unit of Measure', 'MATERIAL_DATA', 'STRING', 1, 0, 'DIRECT', 5, 'active', 'system');
SELECT CONCAT('Added ', ROW_COUNT(), ' material field mappings') as result;

-- Fix: Add Customer field mappings
INSERT IGNORE INTO dis_field_mapping
(data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, sap_structure, field_type, is_required, is_key, transform_type, sort_order, status, created_by)
VALUES
('CUSTOMER', 'Customer', 'PARTNER_CODE', 'Customer Code', 'KUNNR', 'Customer Number', 'CUSTOMER_DATA', 'STRING', 1, 1, 'DIRECT', 1, 'active', 'system'),
('CUSTOMER', 'Customer', 'PARTNER_NAME', 'Customer Name', 'NAME1', 'Customer Name', 'CUSTOMER_DATA', 'STRING', 1, 0, 'DIRECT', 2, 'active', 'system'),
('CUSTOMER', 'Customer', 'CITY', 'City', 'ORT01', 'City', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 3, 'active', 'system'),
('CUSTOMER', 'Customer', 'COUNTRY', 'Country', 'LAND1', 'Country Code', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 4, 'active', 'system'),
('CUSTOMER', 'Customer', 'TELEPHONE', 'Phone', 'TELF1', 'Phone', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 5, 'active', 'system');
SELECT CONCAT('Added ', ROW_COUNT(), ' customer field mappings') as result;

-- Verify Results
SELECT 'Verification Results' as step;
SELECT data_type, COUNT(*) as field_count
FROM dis_field_mapping
WHERE status = 'active'
GROUP BY data_type;

SELECT 'Need repair count' as info, COUNT(*) as count
FROM dis_log_distribution
WHERE status = 'SUCCESS'
  AND (field_count IS NULL OR field_count = 0);

SELECT 'Lineage records' as info, COUNT(*) as count FROM dis_field_lineage;

SELECT 'Done! Please restart backend and call repair API' as next_step;
