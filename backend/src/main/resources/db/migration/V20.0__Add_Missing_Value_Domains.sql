-- 补充缺失的值域数据
-- 这些值域在字段表中被引用，但在值域表中不存在

-- 1. 插入值域定义
INSERT INTO std_value_domain (domain_code, domain_name, domain_type, data_type, data_length, status, sort, created_by, created_at, updated_by, updated_at) VALUES
('PURCHASE_ORG', '采购组织', 'enum', 'string', 50, 'active', 10, 'admin', NOW(), 'admin', NOW()),
('BASE_UNIT', '基本单位', 'enum', 'string', 10, 'active', 20, 'admin', NOW(), 'admin', NOW()),
('COMPANY_CODE', '公司代码', 'enum', 'string', 10, 'active', 30, 'admin', NOW(), 'admin', NOW());

-- 2. 插入值域项数据

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

-- 查询结果
SELECT '值域数据补充完成！' AS message;
SELECT * FROM std_value_domain WHERE domain_code IN ('PURCHASE_ORG', 'BASE_UNIT', 'COMPANY_CODE');
SELECT COUNT(*) AS item_count FROM std_value_domain_item WHERE domain_id IN (
    SELECT id FROM std_value_domain WHERE domain_code IN ('PURCHASE_ORG', 'BASE_UNIT', 'COMPANY_CODE')
);
