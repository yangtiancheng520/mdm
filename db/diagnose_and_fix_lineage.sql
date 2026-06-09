-- =============================================
-- 血缘数据完整诊断和修复
-- =============================================

USE mdm;

-- ==========================================
-- 第一步：诊断检查
-- ==========================================

SELECT '==================== 诊断开始 ====================' as '';

-- 1. 检查字段映射配置
SELECT '1. 字段映射配置检查' as 步骤;
SELECT
    data_type as 数据类型,
    COUNT(*) as 字段数量,
    GROUP_CONCAT(DISTINCT status) as 状态
FROM dis_field_mapping
GROUP BY data_type;

-- 2. 检查分发日志状态
SELECT '2. 分发日志状态检查' as 步骤;
SELECT
    status as 状态,
    COUNT(*) as 数量,
    SUM(CASE WHEN field_count > 0 THEN 1 ELSE 0 END) as 有血缘数量
FROM dis_log_distribution
GROUP BY status;

-- 3. 检查血缘数据
SELECT '3. 血缘数据检查' as 步骤;
SELECT
    COUNT(*) as 血缘记录总数,
    COUNT(DISTINCT log_id) as 关联日志数,
    COUNT(DISTINCT data_type) as 数据类型数
FROM dis_field_lineage;

-- 4. 查看最近10条分发日志详情
SELECT '4. 最近分发日志详情' as 步骤;
SELECT
    id,
    log_code,
    data_type,
    data_code,
    status,
    field_count as 字段数,
    success_field_count as 成功字段数,
    created_at
FROM dis_log_distribution
ORDER BY created_at DESC
LIMIT 10;

-- 5. 查看血缘数据详情
SELECT '5. 血缘数据详情（前5条）' as 步骤;
SELECT
    fl.id,
    fl.log_id,
    fl.data_type,
    fl.mdm_field,
    fl.sap_field,
    fl.source_value,
    fl.target_value,
    fl.status
FROM dis_field_lineage fl
ORDER BY fl.created_at DESC
LIMIT 5;

-- ==========================================
-- 第二步：检查问题
-- ==========================================

SELECT '==================== 问题检查 ====================' as '';

-- 问题1：成功的分发日志但没有血缘数据
SELECT '问题1: 成功分发但无血缘数据' as 检查项;
SELECT
    COUNT(*) as 数量,
    '需要修复' as 建议
FROM dis_log_distribution
WHERE status = 'SUCCESS'
  AND (field_count IS NULL OR field_count = 0);

-- 问题2：缺少字段映射的数据类型
SELECT '问题2: 缺少字段映射的数据类型' as 检查项;
SELECT
    dl.data_type,
    COUNT(DISTINCT dl.id) as 分发日志数,
    '需要添加字段映射' as 建议
FROM dis_log_distribution dl
WHERE NOT EXISTS (
    SELECT 1
    FROM dis_field_mapping fm
    WHERE fm.data_type = dl.data_type
      AND fm.status = 'active'
)
GROUP BY dl.data_type;

-- ==========================================
-- 第三步：修复操作
-- ==========================================

SELECT '==================== 开始修复 ====================' as '';

-- 修复1：确保所有字段映射状态为active
UPDATE dis_field_mapping
SET status = 'active'
WHERE status != 'active';

SELECT CONCAT('已激活 ', ROW_COUNT(), ' 条字段映射') as 修复结果;

-- 修复2：补充物料字段映射（如果不存在）
INSERT IGNORE INTO dis_field_mapping
(data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, sap_structure, field_type, is_required, is_key, transform_type, sort_order, status, created_by)
VALUES
('MATERIAL', '物料', 'MATNR', '物料编码', 'MATNR', '物料编号', 'MATERIAL_DATA', 'STRING', 1, 1, 'DIRECT', 1, 'active', 'system'),
('MATERIAL', '物料', 'MAKTX', '物料描述', 'MAKTX', '物料描述', 'MATERIAL_DATA', 'STRING', 1, 0, 'DIRECT', 2, 'active', 'system'),
('MATERIAL', '物料', 'MTART', '物料类型', 'MTART', '物料类型', 'MATERIAL_DATA', 'STRING', 0, 0, 'DIRECT', 3, 'active', 'system'),
('MATERIAL', '物料', 'MATKL', '物料组', 'MATKL', '物料组', 'MATERIAL_DATA', 'STRING', 0, 0, 'DIRECT', 4, 'active', 'system'),
('MATERIAL', '物料', 'MEINS', '基本单位', 'MEINS', '基本计量单位', 'MATERIAL_DATA', 'STRING', 1, 0, 'DIRECT', 5, 'active', 'system');

SELECT CONCAT('已添加 ', ROW_COUNT(), ' 条物料字段映射') as 修复结果;

-- 修复3：补充客户字段映射（如果不存在）
INSERT IGNORE INTO dis_field_mapping
(data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, sap_structure, field_type, is_required, is_key, transform_type, sort_order, status, created_by)
VALUES
('CUSTOMER', '客户', 'PARTNER_CODE', '客户编码', 'KUNNR', '客户编号', 'CUSTOMER_DATA', 'STRING', 1, 1, 'DIRECT', 1, 'active', 'system'),
('CUSTOMER', '客户', 'PARTNER_NAME', '客户名称', 'NAME1', '客户名称', 'CUSTOMER_DATA', 'STRING', 1, 0, 'DIRECT', 2, 'active', 'system'),
('CUSTOMER', '客户', 'CITY', '城市', 'ORT01', '城市', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 3, 'active', 'system'),
('CUSTOMER', '客户', 'COUNTRY', '国家', 'LAND1', '国家代码', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 4, 'active', 'system'),
('CUSTOMER', '客户', 'TELEPHONE', '电话', 'TELF1', '电话', 'CUSTOMER_DATA', 'STRING', 0, 0, 'DIRECT', 5, 'active', 'system');

SELECT CONCAT('已添加 ', ROW_COUNT(), ' 条客户字段映射') as 修复结果;

-- ==========================================
-- 第四步：验证结果
-- ==========================================

SELECT '==================== 验证结果 ====================' as '';

-- 验证1：字段映射配置
SELECT '验证1: 字段映射配置' as 验证项;
SELECT
    data_type as 数据类型,
    COUNT(*) as 字段数量
FROM dis_field_mapping
WHERE status = 'active'
GROUP BY data_type;

-- 验证2：需要修复的日志数量
SELECT '验证2: 需要修复的日志' as 验证项;
SELECT
    COUNT(*) as 数量,
    CASE
        WHEN COUNT(*) = 0 THEN '无需修复'
        ELSE '需要调用API修复'
    END as 状态
FROM dis_log_distribution
WHERE status = 'SUCCESS'
  AND (field_count IS NULL OR field_count = 0);

-- 验证3：血缘数据
SELECT '验证3: 血缘数据统计' as 验证项;
SELECT
    COUNT(*) as 血缘记录数,
    COUNT(DISTINCT log_id) as 关联日志数
FROM dis_field_lineage;

SELECT '==================== 修复完成 ====================' as '';
SELECT '请重启后端服务，然后调用修复API: POST /api/distribution/lineage/repair' as 下一步操作;
