-- =============================================
-- 诊断和修复血缘数据问题
-- =============================================

USE mdm;

-- ==========================================
-- 1. 诊断查询
-- ==========================================

-- 1.1 检查字段映射配置
SELECT '字段映射配置' as 类型, data_type as 数据类型, COUNT(*) as 数量
FROM dis_field_mapping
WHERE status = 'active'
GROUP BY data_type;

-- 1.2 检查分发日志
SELECT '分发日志统计' as 类型, status as 状态, COUNT(*) as 数量
FROM dis_log_distribution
GROUP BY status;

-- 1.3 检查血缘数据
SELECT '血缘数据' as 类型, COUNT(*) as 数量 FROM dis_field_lineage;

-- 1.4 检查最近成功的分发日志是否有字段血缘
SELECT
    dl.id as 日志ID,
    dl.log_code as 日志编码,
    dl.data_type as 数据类型,
    dl.data_code as 数据编码,
    dl.status as 分发状态,
    dl.field_count as 字段数量,
    dl.success_field_count as 成功字段数,
    (SELECT COUNT(*) FROM dis_field_lineage fl WHERE fl.log_id = dl.id) as 血缘记录数
FROM dis_log_distribution dl
WHERE dl.status = 'SUCCESS'
ORDER BY dl.created_at DESC
LIMIT 10;

-- ==========================================
-- 2. 如果缺少字段映射配置，插入示例配置
-- ==========================================

-- 2.1 检查物料(MATERIAL)字段映射
INSERT IGNORE INTO dis_field_mapping
(data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, sap_structure, field_type, is_required, is_key, transform_type, sort_order, status, created_by)
VALUES
-- 物料基础数据
('MATERIAL', '物料', 'MATNR', '物料编码', 'MATNR', '物料编号', 'MATERIAL_DATA', 'STRING', 1, 1, 'DIRECT', 1, 'active', 'system'),
('MATERIAL', '物料', 'MAKTX', '物料描述', 'MAKTX', '物料描述', 'MATERIAL_DATA', 'STRING', 1, 0, 'DIRECT', 2, 'active', 'system'),
('MATERIAL', '物料', 'MTART', '物料类型', 'MTART', '物料类型', 'MATERIAL_DATA', 'STRING', 0, 0, 'DIRECT', 3, 'active', 'system'),
('MATERIAL', '物料', 'MATKL', '物料组', 'MATKL', '物料组', 'MATERIAL_DATA', 'STRING', 0, 0, 'DIRECT', 4, 'active', 'system'),
('MATERIAL', '物料', 'MEINS', '基本单位', 'MEINS', '基本计量单位', 'MATERIAL_DATA', 'STRING', 1, 0, 'DIRECT', 5, 'active', 'system'),
('MATERIAL', '物料', 'GEWEI', '重量单位', 'GEWEI', '重量单位', 'MATERIAL_DATA', 'STRING', 0, 0, 'DIRECT', 6, 'active', 'system'),
('MATERIAL', '物料', 'NTGEW', '净重', 'NTGEW', '净重', 'MATERIAL_DATA', 'NUMBER', 0, 0, 'DIRECT', 7, 'active', 'system'),
('MATERIAL', '物料', 'BRGEW', '毛重', 'BRGEW', '毛重', 'MATERIAL_DATA', 'NUMBER', 0, 0, 'DIRECT', 8, 'active', 'system'),
('MATERIAL', '物料', 'VOLUM', '体积', 'VOLUM', '体积', 'MATERIAL_DATA', 'NUMBER', 0, 0, 'DIRECT', 9, 'active', 'system'),
('MATERIAL', '物料', 'VOLEH', '体积单位', 'VOLEH', '体积单位', 'MATERIAL_DATA', 'STRING', 0, 0, 'DIRECT', 10, 'active', 'system');

-- 2.2 检查客户(CUSTOMER)字段映射
INSERT IGNORE INTO dis_field_mapping
(data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, sap_structure, field_type, is_required, is_key, transform_type, sort_order, status, created_by)
VALUES
('CUSTOMER', '客户', 'PARTNER_CODE', '客户编码', 'KUNNR', '客户编号', 'CUSTOMER_DATA', 'STRING', 1, 1, 'DIRECT', 1, 'active', 'system'),
('CUSTOMER', '客户', 'PARTNER_NAME', '客户名称', 'NAME1', '客户名称', 'CUSTOMER_DATA', 'STRING', 1, 0, 'DIRECT', 2, 'active', 'system'),
('CUSTOMER', '客户', 'CITY', '城市', 'ORT01', '城市', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 3, 'active', 'system'),
('CUSTOMER', '客户', 'COUNTRY', '国家', 'LAND1', '国家代码', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 4, 'active', 'system'),
('CUSTOMER', '客户', 'REGION', '地区', 'REGIO', '地区', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 5, 'active', 'system'),
('CUSTOMER', '客户', 'STREET', '街道', 'STRAS', '街道', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 6, 'active', 'system'),
('CUSTOMER', '客户', 'POSTAL_CODE', '邮政编码', 'PSTLZ', '邮政编码', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 7, 'active', 'system'),
('CUSTOMER', '客户', 'TELEPHONE', '电话', 'TELF1', '电话', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 8, 'active', 'system'),
('CUSTOMER', '客户', 'EMAIL', '邮箱', 'SMTP_ADDR', '电子邮箱', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 9, 'active', 'system');

-- 2.3 确保供应商字段映射已存在（从初始化脚本中已有，这里确保状态正确）
UPDATE dis_field_mapping
SET status = 'active'
WHERE data_type = 'VENDOR' AND status != 'active';

SELECT '字段映射配置已更新' as 结果;

-- ==========================================
-- 3. 为历史分发日志补充血缘数据
-- ==========================================

-- 注意：这需要通过Java代码执行，因为需要处理字段映射转换逻辑
-- 这里只展示需要处理的数据

SELECT
    '需要补充血缘的日志' as 提示,
    COUNT(*) as 数量
FROM dis_log_distribution dl
WHERE dl.status = 'SUCCESS'
  AND dl.field_count IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM dis_field_lineage fl WHERE fl.log_id = dl.id
  );

SELECT '请重新执行分发或使用后端API重新生成血缘数据' as 后续步骤;

-- ==========================================
-- 4. 验证结果
-- ==========================================

-- 4.1 验证字段映射配置
SELECT
    '字段映射配置验证' as 类型,
    data_type as 数据类型,
    COUNT(*) as 字段数量
FROM dis_field_mapping
WHERE status = 'active'
GROUP BY data_type;

-- 4.2 验证血缘数据
SELECT
    '血缘数据验证' as 类型,
    COUNT(*) as 总数
FROM dis_field_lineage;
