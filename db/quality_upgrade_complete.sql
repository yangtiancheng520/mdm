-- =============================================
-- MDM 质量管理模块完整优化 - 数据库升级脚本
-- 版本: v2.0
-- 日期: 2026-06-09
-- 说明: 一次性完成质量管理所有优化
-- =============================================

USE mdm;

-- =============================================
-- 1. 质量规则表优化
-- =============================================

-- 添加新字段（忽略错误如果字段已存在）
SET @sql = 'ALTER TABLE qlt_rule ADD COLUMN check_type VARCHAR(50) COMMENT ''检查类型'' AFTER rule_type';
SET @check = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mdm' AND TABLE_NAME='qlt_rule' AND COLUMN_NAME='check_type');
PREPARE stmt FROM @sql;
IF @check = 0 THEN
    EXECUTE stmt;
END IF;
DEALLOCATE PREPARE stmt;

SET @sql = 'ALTER TABLE qlt_rule ADD COLUMN check_config TEXT COMMENT ''检查配置JSON'' AFTER rule_config';
SET @check = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mdm' AND TABLE_NAME='qlt_rule' AND COLUMN_NAME='check_config');
PREPARE stmt FROM @sql;
IF @check = 0 THEN
    EXECUTE stmt;
END IF;
DEALLOCATE PREPARE stmt;

SET @sql = 'ALTER TABLE qlt_rule ADD COLUMN field_standard_id BIGINT COMMENT ''关联字段标准ID'' AFTER field_name';
SET @check = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mdm' AND TABLE_NAME='qlt_rule' AND COLUMN_NAME='field_standard_id');
PREPARE stmt FROM @sql;
IF @check = 0 THEN
    EXECUTE stmt;
END IF;
DEALLOCATE PREPARE stmt;

SET @sql = 'ALTER TABLE qlt_rule ADD COLUMN value_domain_id BIGINT COMMENT ''关联值域ID'' AFTER field_standard_id';
SET @check = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mdm' AND TABLE_NAME='qlt_rule' AND COLUMN_NAME='value_domain_id');
PREPARE stmt FROM @sql;
IF @check = 0 THEN
    EXECUTE stmt;
END IF;
DEALLOCATE PREPARE stmt;

SET @sql = 'ALTER TABLE qlt_rule ADD COLUMN template_id BIGINT COMMENT ''规则模板ID'' AFTER value_domain_id';
SET @check = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mdm' AND TABLE_NAME='qlt_rule' AND COLUMN_NAME='template_id');
PREPARE stmt FROM @sql;
IF @check = 0 THEN
    EXECUTE stmt;
END IF;
DEALLOCATE PREPARE stmt;

SET @sql = 'ALTER TABLE qlt_rule ADD COLUMN is_scheduled TINYINT DEFAULT 0 COMMENT ''是否定时检测'' AFTER status';
SET @check = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mdm' AND TABLE_NAME='qlt_rule' AND COLUMN_NAME='is_scheduled');
PREPARE stmt FROM @sql;
IF @check = 0 THEN
    EXECUTE stmt;
END IF;
DEALLOCATE PREPARE stmt;

SET @sql = 'ALTER TABLE qlt_rule ADD COLUMN cron_expression VARCHAR(100) COMMENT ''定时表达式'' AFTER is_scheduled';
SET @check = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mdm' AND TABLE_NAME='qlt_rule' AND COLUMN_NAME='cron_expression');
PREPARE stmt FROM @sql;
IF @check = 0 THEN
    EXECUTE stmt;
END IF;
DEALLOCATE PREPARE stmt;

SET @sql = 'ALTER TABLE qlt_rule ADD COLUMN last_check_time DATETIME COMMENT ''最后检测时间'' AFTER cron_expression';
SET @check = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mdm' AND TABLE_NAME='qlt_rule' AND COLUMN_NAME='last_check_time');
PREPARE stmt FROM @sql;
IF @check = 0 THEN
    EXECUTE stmt;
END IF;
DEALLOCATE PREPARE stmt;

SET @sql = 'ALTER TABLE qlt_rule ADD COLUMN last_pass_rate DECIMAL(5,2) COMMENT ''最后通过率'' AFTER last_check_time';
SET @check = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mdm' AND TABLE_NAME='qlt_rule' AND COLUMN_NAME='last_pass_rate');
PREPARE stmt FROM @sql;
IF @check = 0 THEN
    EXECUTE stmt;
END IF;
DEALLOCATE PREPARE stmt;

-- 添加索引
ALTER TABLE qlt_rule ADD INDEX idx_check_type (check_type);
ALTER TABLE qlt_rule ADD INDEX idx_template_id (template_id);
ALTER TABLE qlt_rule ADD INDEX idx_is_scheduled (is_scheduled);

-- =============================================
-- 2. 规则模板表
-- =============================================

DROP TABLE IF EXISTS qlt_rule_template;
CREATE TABLE qlt_rule_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_code VARCHAR(100) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) COMMENT '模板类型',
    check_type VARCHAR(50) COMMENT '检查类型',
    check_config TEXT COMMENT '检查配置JSON',
    severity VARCHAR(20) DEFAULT 'warning' COMMENT '严重级别',
    description TEXT COMMENT '描述',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_template_code (template_code),
    INDEX idx_template_type (template_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量规则模板表';

-- =============================================
-- 3. 质量评分表
-- =============================================

DROP TABLE IF EXISTS qlt_score;
CREATE TABLE qlt_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    score_date DATE NOT NULL COMMENT '评分日期',
    total_score DECIMAL(5,2) COMMENT '总分',
    completeness_score DECIMAL(5,2) COMMENT '完整性得分',
    uniqueness_score DECIMAL(5,2) COMMENT '唯一性得分',
    accuracy_score DECIMAL(5,2) COMMENT '准确性得分',
    consistency_score DECIMAL(5,2) COMMENT '一致性得分',
    timeliness_score DECIMAL(5,2) COMMENT '及时性得分',
    rule_count INT DEFAULT 0 COMMENT '规则数量',
    pass_rule_count INT DEFAULT 0 COMMENT '通过规则数',
    check_count INT DEFAULT 0 COMMENT '检测次数',
    issue_count INT DEFAULT 0 COMMENT '问题数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_entity_date (entity_type, entity_id, score_date),
    INDEX idx_entity_type (entity_type),
    INDEX idx_score_date (score_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量评分表';

-- =============================================
-- 4. 初始化规则模板数据
-- =============================================

-- 完整性检查模板
INSERT INTO qlt_rule_template (template_code, template_name, template_type, check_type, check_config, severity, description, created_by) VALUES
('TPL_NOT_NULL', '非空检查', 'completeness', 'not_null', '{"checkType":"not_null","trimWhitespace":true}', 'error', '检查字段值不能为空', 'system'),
('TPL_NOT_EMPTY', '非空字符串', 'completeness', 'not_empty', '{"checkType":"not_empty","minLength":1}', 'error', '检查字段值不能为空字符串', 'system');

-- 准确性检查模板
INSERT INTO qlt_rule_template (template_code, template_name, template_type, check_type, check_config, severity, description, created_by) VALUES
('TPL_EMAIL', '邮箱格式', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$"}', 'warning', '检查邮箱格式', 'system'),
('TPL_PHONE', '手机号格式', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^1[3-9]\\\\d{9}$"}', 'warning', '检查手机号格式', 'system'),
('TPL_ID_CARD', '身份证号', 'accuracy', 'regex', '{"checkType":"regex","pattern":"^[1-9]\\\\d{5}(18|19|20)\\\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\\\d|3[01])\\\\d{3}[0-9Xx]$"}', 'warning', '检查身份证号格式', 'system'),
('TPL_LENGTH_50', '长度限制50', 'accuracy', 'length', '{"checkType":"length","maxLength":50}', 'warning', '检查长度不超过50字符', 'system'),
('TPL_LENGTH_100', '长度限制100', 'accuracy', 'length', '{"checkType":"length","maxLength":100}', 'warning', '检查长度不超过100字符', 'system'),
('TPL_RANGE_0_100', '范围0-100', 'accuracy', 'range', '{"checkType":"range","minValue":0,"maxValue":100}', 'warning', '检查数值在0-100范围内', 'system'),
('TPL_DOMAIN', '值域检查', 'accuracy', 'domain', '{"checkType":"domain"}', 'warning', '检查值在指定值域内', 'system');

-- 唯一性检查模板
INSERT INTO qlt_rule_template (template_code, template_name, template_type, check_type, check_config, severity, description, created_by) VALUES
('TPL_UNIQUE_GLOBAL', '全局唯一', 'uniqueness', 'unique_global', '{"checkType":"unique_global","ignoreNull":true}', 'error', '检查字段值在全表唯一', 'system'),
('TPL_UNIQUE_IN_MAIN', '主表范围唯一', 'uniqueness', 'unique_in_main', '{"checkType":"unique_in_main","ignoreNull":true}', 'error', '检查子表字段在主表范围内唯一', 'system');

-- =============================================
-- 5. 更新现有规则
-- =============================================

UPDATE qlt_rule SET
    check_type = CASE rule_type
        WHEN 'completeness' THEN 'not_null'
        WHEN 'uniqueness' THEN 'unique_global'
        WHEN 'accuracy' THEN 'regex'
        ELSE 'not_null'
    END,
    check_config = CASE rule_type
        WHEN 'completeness' THEN '{"checkType":"not_null","trimWhitespace":true}'
        WHEN 'uniqueness' THEN '{"checkType":"unique_global","ignoreNull":true}'
        WHEN 'accuracy' THEN '{"checkType":"regex"}'
        ELSE '{"checkType":"not_null"}'
    END
WHERE check_type IS NULL;

-- =============================================
-- 完成
-- =============================================

SELECT '质量管理模块数据库优化完成！' AS message;
SELECT COUNT(*) AS '规则模板数量' FROM qlt_rule_template;
