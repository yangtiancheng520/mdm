-- =============================================
-- 扩展操作日志表 - 支持完整生命周期记录
-- =============================================

USE mdm;

-- 检查表是否存在
SET @table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = 'mdm'
    AND TABLE_NAME = 'log_operation'
);

-- 如果表不存在，先创建表
SET @create_table = IF(@table_exists = 0,
    'CREATE TABLE log_operation (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT ''主键'',
        category_id BIGINT NOT NULL COMMENT ''分类ID'',
        form_id BIGINT NOT NULL COMMENT ''表单ID'',
        view_id BIGINT COMMENT ''视图ID'',
        operation_type VARCHAR(50) COMMENT ''操作类型'',
        operation_detail VARCHAR(500) COMMENT ''操作详情'',
        from_status VARCHAR(50) COMMENT ''原状态'',
        to_status VARCHAR(50) COMMENT ''目标状态'',
        main_record_id BIGINT COMMENT ''主表记录ID'',
        operation_data TEXT COMMENT ''操作数据快照JSON'',
        quality_score DECIMAL(5,2) COMMENT ''质检评分'',
        quality_issues TEXT COMMENT ''质检问题详情JSON'',
        operation_reason TEXT COMMENT ''操作原因'',
        status VARCHAR(20) DEFAULT ''active'' COMMENT ''日志状态'',
        created_by VARCHAR(50) COMMENT ''创建人'',
        created_at DATETIME COMMENT ''创建时间'',
        updated_by VARCHAR(50) COMMENT ''更新人'',
        updated_at DATETIME COMMENT ''更新时间'',
        ip_address VARCHAR(50) COMMENT ''IP地址'',
        user_agent VARCHAR(500) COMMENT ''用户代理'',
        INDEX idx_category_id (category_id),
        INDEX idx_form_id (form_id),
        INDEX idx_main_record_id (main_record_id),
        INDEX idx_operation_type (operation_type),
        INDEX idx_created_at (created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''数据操作日志表''',
    'SELECT ''表已存在'' AS message'
);

PREPARE stmt FROM @create_table;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果表存在，添加新字段
-- 添加 operation_detail 字段
SET @sql = IF(@table_exists > 0 AND (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mdm'
    AND TABLE_NAME = 'log_operation'
    AND COLUMN_NAME = 'operation_detail'
) = 0,
    'ALTER TABLE log_operation ADD COLUMN operation_detail VARCHAR(500) COMMENT ''操作详情'' AFTER operation_type',
    'SELECT ''字段已存在或表不存在'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 from_status 字段
SET @sql = IF(@table_exists > 0 AND (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mdm'
    AND TABLE_NAME = 'log_operation'
    AND COLUMN_NAME = 'from_status'
) = 0,
    'ALTER TABLE log_operation ADD COLUMN from_status VARCHAR(50) COMMENT ''原状态'' AFTER operation_detail',
    'SELECT ''字段已存在或表不存在'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 to_status 字段
SET @sql = IF(@table_exists > 0 AND (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mdm'
    AND TABLE_NAME = 'log_operation'
    AND COLUMN_NAME = 'to_status'
) = 0,
    'ALTER TABLE log_operation ADD COLUMN to_status VARCHAR(50) COMMENT ''目标状态'' AFTER from_status',
    'SELECT ''字段已存在或表不存在'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 quality_score 字段
SET @sql = IF(@table_exists > 0 AND (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mdm'
    AND TABLE_NAME = 'log_operation'
    AND COLUMN_NAME = 'quality_score'
) = 0,
    'ALTER TABLE log_operation ADD COLUMN quality_score DECIMAL(5,2) COMMENT ''质检评分'' AFTER operation_data',
    'SELECT ''字段已存在或表不存在'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 quality_issues 字段
SET @sql = IF(@table_exists > 0 AND (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mdm'
    AND TABLE_NAME = 'log_operation'
    AND COLUMN_NAME = 'quality_issues'
) = 0,
    'ALTER TABLE log_operation ADD COLUMN quality_issues TEXT COMMENT ''质检问题详情JSON'' AFTER quality_score',
    'SELECT ''字段已存在或表不存在'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 operation_reason 字段
SET @sql = IF(@table_exists > 0 AND (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mdm'
    AND TABLE_NAME = 'log_operation'
    AND COLUMN_NAME = 'operation_reason'
) = 0,
    'ALTER TABLE log_operation ADD COLUMN operation_reason TEXT COMMENT ''操作原因'' AFTER quality_issues',
    'SELECT ''字段已存在或表不存在'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修改 operation_type 字段长度（如果需要）
SET @sql = IF(@table_exists > 0,
    'ALTER TABLE log_operation MODIFY COLUMN operation_type VARCHAR(50) COMMENT ''操作类型''',
    'SELECT ''表不存在'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 查看最终表结构
SELECT '=== 操作日志表结构 ===' AS Info;
DESC log_operation;

SELECT '=== 迁移完成 ===' AS Info;
