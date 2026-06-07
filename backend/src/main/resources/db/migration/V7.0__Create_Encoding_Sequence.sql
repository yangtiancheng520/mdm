-- 编码序列号表（支持多维度独立的序列号管理）
CREATE TABLE IF NOT EXISTS std_encoding_sequence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    scope_key VARCHAR(200) NOT NULL COMMENT '范围键(组合字段值)',
    current_value BIGINT DEFAULT 0 COMMENT '当前值',
    max_value BIGINT DEFAULT 999999 COMMENT '最大值',
    reset_cycle VARCHAR(20) COMMENT '重置周期',
    reset_date DATE COMMENT '重置日期',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_rule_scope (rule_id, scope_key),
    INDEX idx_rule_id (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编码序列号表';

-- 更新编码规则表结构（添加新字段）
ALTER TABLE std_encoding_rule
ADD COLUMN IF NOT EXISTS rule_definition TEXT COMMENT '规则定义JSON' AFTER rule_name;

ALTER TABLE std_encoding_rule
ADD COLUMN IF NOT EXISTS scope_type VARCHAR(50) COMMENT '适用范围类型' AFTER rule_definition;

ALTER TABLE std_encoding_rule
ADD COLUMN IF NOT EXISTS scope_config TEXT COMMENT '范围配置JSON' AFTER scope_type;
