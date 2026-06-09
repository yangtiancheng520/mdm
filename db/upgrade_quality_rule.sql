-- =============================================
-- MDM 质量管理模块优化 - 数据库升级脚本
-- 版本: v1.1
-- 日期: 2026-06-09
-- 说明: 增强质量规则配置功能
-- =============================================

USE mdm;

-- =============================================
-- 1. 扩展质量规则表字段
-- =============================================

-- 添加检查类型字段
ALTER TABLE qlt_rule ADD COLUMN check_type VARCHAR(50) COMMENT '检查类型: not_null/not_empty/regex/domain/range/length/unique_global/unique_in_main/reference/timeliness/custom_script';

-- 添加检查配置字段
ALTER TABLE qlt_rule ADD COLUMN check_config TEXT COMMENT '检查配置JSON: {pattern, minValue, maxValue, domainCode, ...}';

-- 添加关联字段
ALTER TABLE qlt_rule ADD COLUMN field_standard_id BIGINT COMMENT '关联字段标准ID';
ALTER TABLE qlt_rule ADD COLUMN value_domain_id BIGINT COMMENT '关联值域ID';
ALTER TABLE qlt_rule ADD COLUMN template_id BIGINT COMMENT '规则模板ID';

-- 添加定时检测字段
ALTER TABLE qlt_rule ADD COLUMN is_scheduled TINYINT DEFAULT 0 COMMENT '是否定时检测: 0-否 1-是';
ALTER TABLE qlt_rule ADD COLUMN cron_expression VARCHAR(100) COMMENT '定时表达式';

-- 添加统计字段
ALTER TABLE qlt_rule ADD COLUMN last_check_time DATETIME COMMENT '最后检测时间';
ALTER TABLE qlt_rule ADD COLUMN last_pass_rate DECIMAL(5,2) COMMENT '最后通过率';

-- 添加索引
ALTER TABLE qlt_rule ADD INDEX idx_check_type (check_type);
ALTER TABLE qlt_rule ADD INDEX idx_template_id (template_id);
ALTER TABLE qlt_rule ADD INDEX idx_is_scheduled (is_scheduled);

-- =============================================
-- 2. 创建规则模板表
-- =============================================

DROP TABLE IF EXISTS qlt_rule_template;
CREATE TABLE qlt_rule_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_code VARCHAR(100) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) COMMENT '模板类型: completeness/uniqueness/accuracy/consistency/timeliness',
    check_type VARCHAR(50) COMMENT '检查类型',
    check_config TEXT COMMENT '检查配置JSON',
    severity VARCHAR(20) DEFAULT 'warning' COMMENT '严重级别: warning/error/critical',
    description TEXT COMMENT '描述',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_template_code (template_code),
    INDEX idx_template_type (template_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量规则模板表';

-- =============================================
-- 3. 初始化规则模板数据
-- =============================================

INSERT INTO qlt_rule_template (template_code, template_name, template_type, check_type, check_config, severity, description, created_by) VALUES
-- 完整性检查模板
('TPL_NOT_NULL', '非空检查', 'completeness', 'not_null',
 '{"checkType":"not_null","trimWhitespace":true,"allowZero":false}',
 'error', '检查字段值不能为空', 'system'),

('TPL_NOT_EMPTY', '非空字符串', 'completeness', 'not_empty',
 '{"checkType":"not_empty","minLength":1}',
 'error', '检查字段值不能为空字符串', 'system'),

('TPL_NOT_EMPTY_10', '非空字符串(最长10)', 'completeness', 'not_empty',
 '{"checkType":"not_empty","minLength":1,"maxLength":10}',
 'error', '检查字段值不能为空且最长10字符', 'system'),

('TPL_NOT_EMPTY_50', '非空字符串(最长50)', 'completeness', 'not_empty',
 '{"checkType":"not_empty","minLength":1,"maxLength":50}',
 'error', '检查字段值不能为空且最长50字符', 'system'),

('TPL_NOT_EMPTY_100', '非空字符串(最长100)', 'completeness', 'not_empty',
 '{"checkType":"not_empty","minLength":1,"maxLength":100}',
 'error', '检查字段值不能为空且最长100字符', 'system'),

-- 准确性检查模板 - 正则表达式
('TPL_EMAIL', '邮箱格式', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$","errorMessage":"邮箱格式不正确"}',
 'warning', '检查邮箱格式是否正确', 'system'),

('TPL_PHONE', '手机号格式', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^1[3-9]\\\\d{9}$","errorMessage":"手机号格式不正确"}',
 'warning', '检查手机号格式是否正确', 'system'),

('TPL_ID_CARD', '身份证号格式', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^[1-9]\\\\d{5}(18|19|20)\\\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\\\d|3[01])\\\\d{3}[0-9Xx]$","errorMessage":"身份证号格式不正确"}',
 'warning', '检查身份证号格式是否正确', 'system'),

('TPL_TELEPHONE', '固定电话格式', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^0\\\\d{2,3}-?\\\\d{7,8}$","errorMessage":"固定电话格式不正确"}',
 'warning', '检查固定电话格式是否正确', 'system'),

('TPL_POSTCODE', '邮政编码格式', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^[1-9]\\\\d{5}$","errorMessage":"邮政编码格式不正确"}',
 'warning', '检查邮政编码格式是否正确', 'system'),

('TPL_URL', 'URL格式', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^https?:\\\\/\\\\/[\\\\w\\\\-]+(\\\\.[\\\\w\\\\-]+)+\\\\/[\\\\w\\\\-\\\\.\\\\/\\\\?%&=]*$","errorMessage":"URL格式不正确"}',
 'warning', '检查URL格式是否正确', 'system'),

('TPL_IP', 'IP地址格式', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^((25[0-5]|2[0-4]\\\\d|[01]?\\\\d\\\\d?)\\\\.){3}(25[0-5]|2[0-4]\\\\d|[01]?\\\\d\\\\d?)$","errorMessage":"IP地址格式不正确"}',
 'warning', '检查IP地址格式是否正确', 'system'),

('TPL_ALPHA', '仅字母', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^[A-Za-z]+$","errorMessage":"只能包含字母"}',
 'warning', '检查只能包含字母', 'system'),

('TPL_ALPHANUMERIC', '字母和数字', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^[A-Za-z0-9]+$","errorMessage":"只能包含字母和数字"}',
 'warning', '检查只能包含字母和数字', 'system'),

('TPL_CHINESE', '仅中文', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^[\\u4e00-\\u9fa5]+$","errorMessage":"只能包含中文"}',
 'warning', '检查只能包含中文', 'system'),

('TPL_NUMBER', '仅数字', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^\\\\d+$","errorMessage":"只能包含数字"}',
 'warning', '检查只能包含数字', 'system'),

('TPL_DECIMAL_2', '数字(保留2位小数)', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^\\\\d+(\\\\.\\\\d{1,2})?$","errorMessage":"只能包含数字，最多保留2位小数"}',
 'warning', '检查数字格式，最多保留2位小数', 'system'),

('TPL_CODE', '编码格式(字母数字下划线)', 'accuracy', 'regex',
 '{"checkType":"regex","pattern":"^[A-Za-z0-9_]+$","errorMessage":"只能包含字母、数字和下划线"}',
 'warning', '检查编码格式', 'system'),

-- 准确性检查模板 - 长度检查
('TPL_LENGTH_20', '长度限制20', 'accuracy', 'length',
 '{"checkType":"length","maxLength":20,"countType":"char"}',
 'warning', '检查字段长度不超过20字符', 'system'),

('TPL_LENGTH_50', '长度限制50', 'accuracy', 'length',
 '{"checkType":"length","maxLength":50,"countType":"char"}',
 'warning', '检查字段长度不超过50字符', 'system'),

('TPL_LENGTH_100', '长度限制100', 'accuracy', 'length',
 '{"checkType":"length","maxLength":100,"countType":"char"}',
 'warning', '检查字段长度不超过100字符', 'system'),

('TPL_LENGTH_200', '长度限制200', 'accuracy', 'length',
 '{"checkType":"length","maxLength":200,"countType":"char"}',
 'warning', '检查字段长度不超过200字符', 'system'),

('TPL_LENGTH_500', '长度限制500', 'accuracy', 'length',
 '{"checkType":"length","maxLength":500,"countType":"char"}',
 'warning', '检查字段长度不超过500字符', 'system'),

-- 准确性检查模板 - 范围检查
('TPL_RANGE_0_100', '范围0-100', 'accuracy', 'range',
 '{"checkType":"range","minValue":0,"maxValue":100,"minOperator":">=","maxOperator":"<="}',
 'warning', '检查数值在0-100范围内', 'system'),

('TPL_RANGE_0_1000', '范围0-1000', 'accuracy', 'range',
 '{"checkType":"range","minValue":0,"maxValue":1000,"minOperator":">=","maxOperator":"<="}',
 'warning', '检查数值在0-1000范围内', 'system'),

('TPL_RANGE_PERCENT', '百分比范围0-100', 'accuracy', 'range',
 '{"checkType":"range","minValue":0,"maxValue":100,"minOperator":">=","maxOperator":"<="}',
 'error', '检查百分比在0-100范围内', 'system'),

('TPL_RANGE_POSITIVE', '正数(大于0)', 'accuracy', 'range',
 '{"checkType":"range","minValue":0,"minOperator":">"}',
 'error', '检查数值必须大于0', 'system'),

('TPL_RANGE_NON_NEGATIVE', '非负数(大于等于0)', 'accuracy', 'range',
 '{"checkType":"range","minValue":0,"minOperator":">="}',
 'error', '检查数值必须大于等于0', 'system'),

-- 准确性检查模板 - 值域检查
('TPL_DOMAIN_CHECK', '值域检查', 'accuracy', 'domain',
 '{"checkType":"domain","allowNull":true}',
 'warning', '检查字段值在指定值域内', 'system'),

-- 唯一性检查模板
('TPL_UNIQUE_GLOBAL', '全局唯一', 'uniqueness', 'unique_global',
 '{"checkType":"unique_global","ignoreNull":true,"caseSensitive":false}',
 'error', '检查字段值在全表唯一', 'system'),

('TPL_UNIQUE_GLOBAL_CASE', '全局唯一(区分大小写)', 'uniqueness', 'unique_global',
 '{"checkType":"unique_global","ignoreNull":true,"caseSensitive":true}',
 'error', '检查字段值在全表唯一(区分大小写)', 'system'),

('TPL_UNIQUE_IN_MAIN', '主表范围内唯一', 'uniqueness', 'unique_in_main',
 '{"checkType":"unique_in_main","ignoreNull":true}',
 'error', '检查子表字段在主表范围内唯一', 'system'),

-- 一致性检查模板
('TPL_REFERENCE_CHECK', '关联检查', 'consistency', 'reference',
 '{"checkType":"reference"}',
 'error', '检查字段值在关联表中存在', 'system');

-- =============================================
-- 4. 更新现有规则数据
-- =============================================

-- 为现有规则设置默认的检查类型（根据rule_type推断）
UPDATE qlt_rule SET check_type = 'not_null', check_config = '{"checkType":"not_null","trimWhitespace":true}'
WHERE rule_type = 'completeness' AND check_type IS NULL;

UPDATE qlt_rule SET check_type = 'unique_global', check_config = '{"checkType":"unique_global","ignoreNull":true}'
WHERE rule_type = 'uniqueness' AND check_type IS NULL;

UPDATE qlt_rule SET check_type = 'regex', check_config = '{"checkType":"regex"}'
WHERE rule_type = 'accuracy' AND check_type IS NULL;

-- =============================================
-- 5. 创建质量检测执行日志表
-- =============================================

DROP TABLE IF EXISTS qlt_check_log;
CREATE TABLE qlt_check_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '检测任务ID',
    rule_id BIGINT COMMENT '规则ID',
    entity_id BIGINT COMMENT '实体ID',
    check_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    check_duration INT COMMENT '检测耗时(毫秒)',
    total_records INT DEFAULT 0 COMMENT '检测记录数',
    pass_count INT DEFAULT 0 COMMENT '通过数',
    fail_count INT DEFAULT 0 COMMENT '失败数',
    pass_rate DECIMAL(5,2) COMMENT '通过率',
    status VARCHAR(20) COMMENT '执行状态: success-成功/failed-失败',
    error_message TEXT COMMENT '错误信息',
    INDEX idx_task_id (task_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_check_time (check_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量检测执行日志表';

-- =============================================
-- 6. 创建质量评分表
-- =============================================

DROP TABLE IF EXISTS qlt_score;
CREATE TABLE qlt_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型: view/entity/field',
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
-- 完成
-- =============================================

SELECT '质量规则优化 - 数据库升级完成!' AS message;

SELECT
    (SELECT COUNT(*) FROM qlt_rule_template) AS '规则模板数',
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME = 'qlt_rule' AND COLUMN_NAME = 'check_type') AS '新增字段数',
    (SELECT COUNT(*) FROM information_schema.TABLES
     WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME IN ('qlt_rule_template', 'qlt_check_log', 'qlt_score')) AS '新增表数';
