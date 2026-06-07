-- ==========================================
-- 值域管理增强 - 数据库迁移脚本
-- ==========================================

-- 1. 为字段标准表添加值域ID字段
ALTER TABLE std_field_standard
ADD COLUMN category_id BIGINT COMMENT '分类ID' AFTER category,
ADD COLUMN domain_id BIGINT COMMENT '值域ID，用于枚举类型字段' AFTER reference_source;

-- 添加索引
ALTER TABLE std_field_standard
ADD INDEX idx_category_id (category_id),
ADD INDEX idx_domain_id (domain_id);

-- 2. 更新值域表结构（如果不存在新字段则添加）
-- 检查并添加 data_type 字段
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'std_value_domain'
                   AND COLUMN_NAME = 'data_type');
SET @sql := IF(@exist_col = 0,
    'ALTER TABLE std_value_domain ADD COLUMN data_type VARCHAR(20) NOT NULL DEFAULT ''VARCHAR'' COMMENT ''存储类型: CHAR/VARCHAR/INT/TINYINT/SMALLINT/BIGINT'' AFTER domain_name',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 data_length 字段
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'std_value_domain'
                   AND COLUMN_NAME = 'data_length');
SET @sql := IF(@exist_col = 0,
    'ALTER TABLE std_value_domain ADD COLUMN data_length INT COMMENT ''存储长度'' AFTER data_type',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 options 字段（JSON格式存储选项）
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'std_value_domain'
                   AND COLUMN_NAME = 'options');
SET @sql := IF(@exist_col = 0,
    'ALTER TABLE std_value_domain ADD COLUMN options TEXT COMMENT ''选项列表JSON'' AFTER data_length',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 更新现有值域数据，将值域项合并到options字段
UPDATE std_value_domain vd
SET options = (
    SELECT JSON_ARRAYAGG(
        JSON_OBJECT('value', item_code, 'label', item_name, 'sort', COALESCE(item.sort, 0))
    )
    FROM std_value_domain_item item
    WHERE item.domain_id = vd.id
)
WHERE EXISTS (
    SELECT 1 FROM std_value_domain_item item WHERE item.domain_id = vd.id
);

-- 4. 为值域表设置默认数据类型
UPDATE std_value_domain SET data_type = 'VARCHAR' WHERE data_type IS NULL OR data_type = '';

-- 5. 插入一些常用的值域示例数据（如果不存在）
INSERT INTO std_value_domain (domain_code, domain_name, data_type, data_length, options, status, description) VALUES
('GENDER', '性别', 'CHAR', 1, '[{"value":"M","label":"男","sort":1},{"value":"F","label":"女","sort":2}]', 'active', '性别选项'),
('YES_NO', '是否', 'TINYINT', NULL, '[{"value":"1","label":"是","sort":1},{"value":"0","label":"否","sort":2}]', 'active', '是否选项'),
('STATUS', '状态', 'VARCHAR', 20, '[{"value":"active","label":"启用","sort":1},{"value":"inactive","label":"停用","sort":2}]', 'active', '通用状态'),
('ENABLE_STATUS', '启用状态', 'TINYINT', NULL, '[{"value":"1","label":"启用","sort":1},{"value":"0","label":"禁用","sort":2}]', 'active', '启用禁用状态')
ON DUPLICATE KEY UPDATE domain_name = VALUES(domain_name);

-- ==========================================
-- 执行成功提示
-- ==========================================
SELECT '值域管理增强迁移成功！' AS message;
