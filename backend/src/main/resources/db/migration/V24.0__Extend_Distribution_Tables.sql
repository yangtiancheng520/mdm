-- 扩展分发日志表字段
-- 注意：如果字段已存在会报错，可以忽略或手动检查

-- 添加 data_type_name 字段
ALTER TABLE dis_log_distribution
ADD COLUMN data_type_name VARCHAR(100) COMMENT '数据类型名称(表单名)' AFTER data_type;

-- 添加 receive_time 字段
ALTER TABLE dis_log_distribution
ADD COLUMN receive_time DATETIME COMMENT '接收确认时间' AFTER end_time;

-- 添加 field_count 字段
ALTER TABLE dis_log_distribution
ADD COLUMN field_count INT DEFAULT 0 COMMENT '字段数量' AFTER receive_time;

-- 添加 success_field_count 字段
ALTER TABLE dis_log_distribution
ADD COLUMN success_field_count INT DEFAULT 0 COMMENT '成功字段数' AFTER field_count;
