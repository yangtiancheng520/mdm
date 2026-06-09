-- =============================================
-- 质量管理模块 - 数据表创建
-- =============================================

USE mdm;

-- 质量规则表
CREATE TABLE IF NOT EXISTS qlt_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_code VARCHAR(100) NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',

    -- 规则分类
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型: completeness(完整性)/uniqueness(唯一性)/accuracy(准确性)/consistency(一致性)/timeliness(及时性)',

    -- 关联视图
    view_id BIGINT NOT NULL COMMENT '关联视图ID',

    -- 关联实体（主表或子表）
    entity_id BIGINT NOT NULL COMMENT '关联实体ID',
    entity_type VARCHAR(20) NOT NULL COMMENT '实体类型: main(主表)/sub(子表)',

    -- 物理表信息（冗余存储，加速查询）
    table_name VARCHAR(100) NOT NULL COMMENT '物理表名',

    -- 关联字段
    field_id BIGINT COMMENT '关联字段ID',
    field_code VARCHAR(100) COMMENT '字段编码',
    field_name VARCHAR(200) COMMENT '字段名称',

    -- 规则配置
    rule_config TEXT COMMENT '规则配置JSON',

    -- 阈值与严重级别
    threshold DECIMAL(5,2) DEFAULT 100.00 COMMENT '合格阈值(%)',
    severity VARCHAR(20) DEFAULT 'warning' COMMENT 'warning(警告)/error(错误)/critical(严重)',

    -- 状态
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active(启用)/inactive(停用)',

    -- 审计字段
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',

    UNIQUE KEY uk_rule_code (rule_code),
    INDEX idx_view_id (view_id),
    INDEX idx_entity_id (entity_id),
    INDEX idx_table_name (table_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量规则表';

-- 检测任务表
CREATE TABLE IF NOT EXISTS qlt_check_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_code VARCHAR(100) NOT NULL COMMENT '任务编码',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',

    -- 检测范围
    view_id BIGINT COMMENT '视图ID',
    entity_ids TEXT COMMENT '实体ID列表，逗号分隔',
    rule_ids TEXT COMMENT '规则ID列表，逗号分隔',

    -- 任务状态
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending(待执行)/running(执行中)/completed(已完成)/failed(失败)',

    -- 统计结果
    total_records INT DEFAULT 0 COMMENT '检测记录总数',
    total_rules INT DEFAULT 0 COMMENT '检测规则数',
    pass_count INT DEFAULT 0 COMMENT '通过数',
    fail_count INT DEFAULT 0 COMMENT '失败数',
    pass_rate DECIMAL(5,2) COMMENT '通过率(%)',

    -- 主表统计
    main_total INT DEFAULT 0 COMMENT '主表记录数',
    main_pass INT DEFAULT 0 COMMENT '主表通过数',
    main_fail INT DEFAULT 0 COMMENT '主表失败数',

    -- 子表统计
    sub_total INT DEFAULT 0 COMMENT '子表记录数',
    sub_pass INT DEFAULT 0 COMMENT '子表通过数',
    sub_fail INT DEFAULT 0 COMMENT '子表失败数',

    -- 执行时间
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms INT COMMENT '执行耗时(毫秒)',
    error_message TEXT COMMENT '错误信息',

    -- 审计
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_task_code (task_code),
    INDEX idx_view_id (view_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测任务表';

-- 检测结果明细表
CREATE TABLE IF NOT EXISTS qlt_check_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',

    -- 数据定位
    entity_id BIGINT COMMENT '实体ID',
    entity_type VARCHAR(20) COMMENT '实体类型: main/sub',
    table_name VARCHAR(100) COMMENT '物理表名',
    record_id BIGINT COMMENT '记录ID',

    -- 主表关联（子表记录时需要）
    main_record_id BIGINT COMMENT '主表记录ID',

    -- 字段信息
    field_code VARCHAR(100) COMMENT '字段编码',
    field_value TEXT COMMENT '字段值',

    -- 检测结果
    is_passed TINYINT DEFAULT 0 COMMENT '是否通过: 0-否 1-是',
    issue_type VARCHAR(50) COMMENT '问题类型',
    issue_level VARCHAR(20) COMMENT '问题级别: warning/error/critical',
    issue_message TEXT COMMENT '问题描述',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_task_id (task_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_entity_id (entity_id),
    INDEX idx_record_id (record_id),
    INDEX idx_is_passed (is_passed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测结果明细表';

-- 问题管理表
CREATE TABLE IF NOT EXISTS qlt_issue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    issue_code VARCHAR(100) NOT NULL COMMENT '问题编号',
    task_id BIGINT COMMENT '来源任务ID',
    result_id BIGINT COMMENT '来源结果ID',
    rule_id BIGINT COMMENT '规则ID',

    -- 视图与实体
    view_id BIGINT COMMENT '视图ID',
    entity_id BIGINT COMMENT '实体ID',
    entity_type VARCHAR(20) COMMENT 'main/sub',
    table_name VARCHAR(100) COMMENT '物理表名',

    -- 数据定位
    record_id BIGINT COMMENT '记录ID',
    main_record_id BIGINT COMMENT '主表记录ID（子表问题时）',
    field_code VARCHAR(100) COMMENT '字段编码',
    field_value TEXT COMMENT '当前值',

    -- 问题描述
    issue_type VARCHAR(50) COMMENT '问题类型',
    issue_level VARCHAR(20) COMMENT 'warning/error/critical',
    issue_desc TEXT COMMENT '问题描述',

    -- 状态流转
    status VARCHAR(20) DEFAULT 'open' COMMENT 'open(待处理)/processing(处理中)/resolved(已解决)/ignored(已忽略)/closed(已关闭)',
    assignee VARCHAR(50) COMMENT '处理人',
    due_date DATE COMMENT '截止日期',

    -- 解决信息
    resolved_by VARCHAR(50) COMMENT '解决人',
    resolved_time DATETIME COMMENT '解决时间',
    resolution TEXT COMMENT '解决方案',

    -- 审计
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_issue_code (issue_code),
    INDEX idx_task_id (task_id),
    INDEX idx_view_id (view_id),
    INDEX idx_status (status),
    INDEX idx_assignee (assignee),
    INDEX idx_issue_level (issue_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题管理表';
