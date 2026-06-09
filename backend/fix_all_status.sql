-- =============================================
-- 修复所有异常状态值
-- =============================================

USE mdm;

-- 1. 修复中文状态
UPDATE mdm_vendor SET status = 'ACTIVE' WHERE status = '已生效';
UPDATE mdm_vendor SET status = 'DRAFT' WHERE status = '草稿';
UPDATE mdm_vendor SET status = 'PENDING' WHERE status IN ('审批中', 'pending');

UPDATE mdm_customer SET status = 'ACTIVE' WHERE status = '已生效';
UPDATE mdm_customer SET status = 'DRAFT' WHERE status = '草稿';
UPDATE mdm_customer SET status = 'PENDING' WHERE status IN ('审批中', 'pending');

UPDATE mdm_material SET status = 'ACTIVE' WHERE status = '已生效';
UPDATE mdm_material SET status = 'DRAFT' WHERE status = '草稿';
UPDATE mdm_material SET status = 'PENDING' WHERE status IN ('审批中', 'pending');

-- 2. 修复 PENDING_QC 状态（这个是之前测试留下的，改为 PENDING）
UPDATE mdm_vendor SET status = 'PENDING' WHERE status = 'PENDING_QC';
UPDATE mdm_customer SET status = 'PENDING' WHERE status = 'PENDING_QC';
UPDATE mdm_material SET status = 'PENDING' WHERE status = 'PENDING_QC';

-- 3. 查看最终结果
SELECT '=== 最终状态统计 ===' AS 'Info';

SELECT 'mdm_vendor' AS table_name, status, COUNT(*) as count
FROM mdm_vendor
GROUP BY status
UNION ALL
SELECT 'mdm_customer' AS table_name, status, COUNT(*) as count
FROM mdm_customer
GROUP BY status
UNION ALL
SELECT 'mdm_material' AS table_name, status, COUNT(*) as count
FROM mdm_material
GROUP BY status;

-- 4. 查看所有数据详情
SELECT '=== mdm_vendor 详情 ===' AS 'Info';
SELECT id, status, created_by, created_at FROM mdm_vendor;

SELECT '=== mdm_customer 详情 ===' AS 'Info';
SELECT id, status, created_by, created_at FROM mdm_customer;

SELECT '=== mdm_material 详情 ===' AS 'Info';
SELECT id, status, created_by, created_at FROM mdm_material;
