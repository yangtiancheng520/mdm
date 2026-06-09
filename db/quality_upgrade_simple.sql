-- =============================================
-- MDM 质量管理模块完整优化
-- =============================================

USE mdm;

-- 1. 规则模板表
CREATE TABLE IF NOT EXISTS qlt_rule_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_code VARCHAR(100) NOT NULL UNIQUE,
    template_name VARCHAR(200) NOT NULL,
    template_type VARCHAR(50),
    check_type VARCHAR(50),
    check_config TEXT,
    severity VARCHAR(20) DEFAULT 'warning',
    description TEXT,
    status VARCHAR(20) DEFAULT 'active',
    created_by VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_template_type (template_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量规则模板表';

-- 2. 质量评分表
CREATE TABLE IF NOT EXISTS qlt_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    score_date DATE NOT NULL,
    total_score DECIMAL(5,2),
    completeness_score DECIMAL(5,2),
    uniqueness_score DECIMAL(5,2),
    accuracy_score DECIMAL(5,2),
    consistency_score DECIMAL(5,2),
    timeliness_score DECIMAL(5,2),
    rule_count INT DEFAULT 0,
    pass_rule_count INT DEFAULT 0,
    check_count INT DEFAULT 0,
    issue_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_entity_date (entity_type, entity_id, score_date),
    INDEX idx_score_date (score_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量评分表';

-- 3. 为规则表添加新字段
INSERT INTO qlt_rule_template (template_code, template_name, template_type, check_type, check_config, severity, description, created_by) VALUES
('TPL_NOT_NULL', '非空检查', 'completeness', 'not_null', '{"checkType":"not_null","trimWhitespace":true}', 'error', '检查字段值不能为空', 'system'),
('TPL_NOT_EMPTY', '非空字符串', 'completeness', 'not_empty', '{"checkType":"not_empty","minLength":1}', 'error', '检查字段值不能为空字符串', 'system'),
('TPL_EMAIL', '邮箱格式', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$"}', 'warning', '检查邮箱格式', 'system'),
('TPL_PHONE', '手机号格式', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^1[3-9]\\\\d{9}$"}', 'warning', '检查手机号格式', 'system'),
('TPL_ID_CARD', '身份证号', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^[1-9]\\\\d{5}(18|19|20)\\\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\\\d|3[01])\\\\d{3}[0-9Xx]$"}', 'warning', '检查身份证号格式', 'system'),
('TPL_LENGTH_50', '长度限制50', 'accuracy', 'length', '{"checkType":"length","maxLength":50}', 'warning', '检查长度不超过50字符', 'system'),
('TPL_LENGTH_100', '长度限制100', 'accuracy', 'length', '{"checkType":"length","maxLength":100}', 'warning', '检查长度不超过100字符', 'system'),
('TPL_RANGE_0_100', '范围0-100', 'accuracy', 'range', '{"checkType":"range","minValue":0,"maxValue":100}', 'warning', '检查数值在0-100范围内', 'system'),
('TPL_UNIQUE_GLOBAL', '全局唯一', 'uniqueness', 'unique_global', '{"checkType":"unique_global","ignoreNull":true}', 'error', '检查字段值在全表唯一', 'system'),
('TPL_UNIQUE_IN_MAIN', '主表范围唯一', 'uniqueness', 'unique_in_main', '{"checkType":"unique_in_main","ignoreNull":true}', 'error', '检查子表字段在主表范围内唯一', 'system')
ON DUPLICATE KEY UPDATE template_name=VALUES(template_name);

SELECT '数据库优化完成！' AS message;
SELECT COUNT(*) AS '模板数量' FROM qlt_rule_template;
