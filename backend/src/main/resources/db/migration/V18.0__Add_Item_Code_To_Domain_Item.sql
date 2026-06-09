-- =============================================
-- 值域项表新增item_code字段
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 1. 添加item_code字段到item_value前面
ALTER TABLE std_value_domain_item
ADD COLUMN item_code VARCHAR(100) COMMENT '值域项编码' AFTER domain_id;

-- 2. 为item_code添加索引
ALTER TABLE std_value_domain_item
ADD INDEX idx_item_code (item_code);

-- 3. 查看修改后的表结构
DESC std_value_domain_item;

-- 4. 查看数据示例
SELECT id, domain_id, item_code, item_value, sort, status
FROM std_value_domain_item
LIMIT 10;

SELECT '值域项表已添加item_code字段！' AS message;
