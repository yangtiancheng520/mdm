-- 补充缺失的值域定义
-- 这些值域在字段表中被引用，但在值域表中不存在

-- 1. 插入值域定义
INSERT INTO std_value_domain (domain_code, domain_name, domain_type, data_type, data_length, status, sort, created_by, created_at, updated_by, updated_at) VALUES
('PURCHASE_ORG', '采购组织', 'enum', 'string', 50, 'active', 10, 'admin', NOW(), 'admin', NOW()),
('BASE_UNIT', '基本单位', 'enum', 'string', 10, 'active', 20, 'admin', NOW(), 'admin', NOW()),
('COMPANY_CODE', '公司代码', 'enum', 'string', 10, 'active', 30, 'admin', NOW(), 'admin', NOW()),
('CUSTOMER_GROUP', '客户组', 'enum', 'string', 20, 'active', 40, 'admin', NOW(), 'admin', NOW()),
('INDUSTRY', '行业类型', 'enum', 'string', 50, 'active', 50, 'admin', NOW(), 'admin', NOW()),
('MATERIAL_GROUP', '物料组', 'enum', 'string', 50, 'active', 60, 'admin', NOW(), 'admin', NOW()),
('MATERIAL_TYPE', '物料类型', 'enum', 'string', 50, 'active', 70, 'admin', NOW(), 'admin', NOW()),
('PROVINCE', '省份', 'enum', 'string', 50, 'active', 80, 'admin', NOW(), 'admin', NOW())
ON DUPLICATE KEY UPDATE domain_name = VALUES(domain_name);

-- 2. 插入值域项数据（示例数据，可根据实际需求修改）

-- 采购组织选项
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'ORG001', '采购组织A', 1, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'PURCHASE_ORG';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'ORG002', '采购组织B', 2, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'PURCHASE_ORG';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'ORG003', '采购组织C', 3, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'PURCHASE_ORG';

-- 基本单位选项
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'EA', '件', 1, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'BASE_UNIT';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'KG', '千克', 2, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'BASE_UNIT';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'M', '米', 3, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'BASE_UNIT';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'L', '升', 4, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'BASE_UNIT';

-- 公司代码选项
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, '1000', '总公司', 1, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'COMPANY_CODE';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, '2000', '分公司A', 2, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'COMPANY_CODE';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, '3000', '分公司B', 3, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'COMPANY_CODE';

-- 客户组选项
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, '01', '战略客户', 1, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'CUSTOMER_GROUP';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, '02', '重点客户', 2, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'CUSTOMER_GROUP';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, '03', '一般客户', 3, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'CUSTOMER_GROUP';

-- 行业类型选项
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'MANUFACTURE', '制造业', 1, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'INDUSTRY';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'SERVICE', '服务业', 2, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'INDUSTRY';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'RETAIL', '零售业', 3, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'INDUSTRY';

-- 物料组选项
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, '01', '原材料', 1, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, '02', '半成品', 2, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, '03', '成品', 3, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'MATERIAL_GROUP';

-- 物料类型选项
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'ROH', '原材料', 1, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'HALB', '半成品', 2, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'FERT', '成品', 3, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'MATERIAL_TYPE';

-- 省份选项
INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'BJ', '北京市', 1, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'PROVINCE';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'SH', '上海市', 2, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'PROVINCE';

INSERT INTO std_value_domain_item (domain_id, item_code, item_value, sort, status, created_by, created_at)
SELECT id, 'GD', '广东省', 3, 'active', 'admin', NOW()
FROM std_value_domain WHERE domain_code = 'PROVINCE';

-- 3. 再次更新 domain_id
UPDATE std_view_field vf
INNER JOIN std_value_domain vd ON vf.domain_code = vd.domain_code
SET vf.domain_id = vd.id
WHERE vf.domain_code IS NOT NULL AND vf.domain_code != '';

-- 查询结果
SELECT '值域数据补充完成！' AS message;
SELECT domain_code, domain_name, (SELECT COUNT(*) FROM std_value_domain_item WHERE domain_id = std_value_domain.id) AS item_count
FROM std_value_domain
WHERE domain_code IN ('PURCHASE_ORG', 'BASE_UNIT', 'COMPANY_CODE', 'CUSTOMER_GROUP', 'INDUSTRY', 'MATERIAL_GROUP', 'MATERIAL_TYPE', 'PROVINCE');
