-- =============================================
-- 改造 data_instance 表为数据操作日志表
-- =============================================

USE mdm;

-- 1. 重命名表
RENAME TABLE data_instance TO data_operation_log;

-- 2. 修改 data_json 字段为 operation_data
ALTER TABLE data_operation_log
CHANGE COLUMN data_json operation_data TEXT COMMENT '操作数据快照JSON';

-- 3. 添加新字段
ALTER TABLE data_operation_log
ADD COLUMN operation_type VARCHAR(20) COMMENT '操作类型：create/update/delete' AFTER view_id,
ADD COLUMN main_record_id BIGINT COMMENT '主表记录ID' AFTER operation_type,
ADD COLUMN ip_address VARCHAR(50) COMMENT 'IP地址' AFTER updated_at,
ADD COLUMN user_agent VARCHAR(500) COMMENT '用户代理' AFTER ip_address;

-- 4. 修改索引
DROP INDEX idx_category_id ON data_operation_log;
DROP INDEX idx_form_id ON data_operation_log;

CREATE INDEX idx_main_record_id ON data_operation_log(main_record_id);
CREATE INDEX idx_operation_type ON data_operation_log(operation_type);
CREATE INDEX idx_created_at ON data_operation_log(created_at);

-- 5. 更新表注释
ALTER TABLE data_operation_log COMMENT '数据操作日志表';

-- 6. 为历史数据设置默认操作类型
UPDATE data_operation_log
SET operation_type = CASE
    WHEN status = 'obsolete' THEN 'delete'
    ELSE 'create'
END
WHERE operation_type IS NULL;
