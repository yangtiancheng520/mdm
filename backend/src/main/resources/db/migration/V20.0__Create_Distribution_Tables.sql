-- =============================================
-- 分发管理模块 - 数据表创建
-- =============================================

USE mdm;

-- ==========================================
-- 1. 目标系统连接配置表
-- ==========================================
CREATE TABLE IF NOT EXISTS dis_system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_code VARCHAR(50) NOT NULL COMMENT '配置编码',

    -- 系统类型
    system_type VARCHAR(20) NOT NULL COMMENT '系统类型: SAP/HTTP/DATABASE/WEBSERVICE/MQ',
    system_type_name VARCHAR(50) COMMENT '系统类型名称',

    -- 连接配置（JSON存储，不同类型存不同字段）
    -- SAP: {"host":"10.0.0.1","systemNumber":"00","client":"800","user":"SAPUSER","password":"xxx","language":"ZH"}
    -- HTTP: {"url":"http://api.example.com","method":"POST","authType":"Bearer","token":"xxx","timeout":30000,"headers":{}}
    -- DATABASE: {"dbType":"MySQL","url":"jdbc:mysql://localhost:3306/db","user":"root","password":"xxx"}
    connection_config TEXT NOT NULL COMMENT '连接配置JSON',

    -- 连接池/超时配置
    pool_size INT DEFAULT 10 COMMENT '连接池大小',
    timeout INT DEFAULT 30000 COMMENT '超时时间(毫秒)',

    -- 状态
    status VARCHAR(20) DEFAULT 'inactive' COMMENT 'active(启用)/inactive(停用)',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认配置: 0-否 1-是',

    -- 测试连接
    last_test_time DATETIME COMMENT '最后测试时间',
    last_test_result VARCHAR(20) COMMENT 'success/failed',
    last_test_msg VARCHAR(500) COMMENT '测试结果消息',

    -- 审计字段
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',

    UNIQUE KEY uk_config_code (config_code),
    INDEX idx_system_type (system_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标系统连接配置表';

-- ==========================================
-- 2. 字段映射配置表
-- ==========================================
CREATE TABLE IF NOT EXISTS dis_field_mapping (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    -- 数据类型
    data_type VARCHAR(50) NOT NULL COMMENT '数据类型: VENDOR(供应商)/MATERIAL(物料)/CUSTOMER(客户)',
    data_type_name VARCHAR(100) COMMENT '数据类型名称',

    -- 关联配置
    system_config_id BIGINT COMMENT '关联系统配置ID',

    -- MDM字段
    mdm_field VARCHAR(100) NOT NULL COMMENT 'MDM字段名',
    mdm_field_name VARCHAR(200) COMMENT 'MDM字段中文名',

    -- SAP字段
    sap_field VARCHAR(100) NOT NULL COMMENT 'SAP字段名',
    sap_field_name VARCHAR(200) COMMENT 'SAP字段中文名',
    sap_structure VARCHAR(100) COMMENT 'SAP结构名(如VENDOR_DATA)',

    -- 字段属性
    field_type VARCHAR(20) DEFAULT 'STRING' COMMENT '字段类型: STRING/NUMBER/DATE/BOOLEAN',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填: 0-否 1-是',
    is_key TINYINT DEFAULT 0 COMMENT '是否主键: 0-否 1-是',

    -- 转换规则
    transform_type VARCHAR(20) DEFAULT 'DIRECT' COMMENT '转换类型: DIRECT(直接)/VALUE_MAP(值域映射)/FIXED(固定值)/EXPRESSION(表达式)',
    transform_rule TEXT COMMENT '转换规则JSON',
    default_value VARCHAR(200) COMMENT '默认值',

    -- 排序与状态
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active(启用)/inactive(停用)',

    -- 审计字段
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(200) COMMENT '备注',

    INDEX idx_data_type (data_type),
    INDEX idx_system_config_id (system_config_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段映射配置表';

-- ==========================================
-- 3. 分发日志表
-- ==========================================
CREATE TABLE IF NOT EXISTS dis_log_distribution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    log_code VARCHAR(100) NOT NULL COMMENT '日志编码',

    -- 数据信息
    data_type VARCHAR(50) NOT NULL COMMENT '数据类型: VENDOR/MATERIAL/CUSTOMER',
    data_id BIGINT NOT NULL COMMENT '数据ID',
    data_code VARCHAR(100) COMMENT '数据编码',
    data_name VARCHAR(200) COMMENT '数据名称',

    -- 系统配置
    system_config_id BIGINT COMMENT '系统配置ID',
    system_config_name VARCHAR(100) COMMENT '系统配置名称',
    system_type VARCHAR(20) COMMENT '系统类型: SAP/HTTP/DATABASE',

    -- BAPI/接口信息
    interface_name VARCHAR(100) NOT NULL COMMENT '接口名称(BAPI函数名/HTTP路径)',
    operation VARCHAR(20) COMMENT '操作类型: CREATE/UPDATE/DELETE',

    -- 执行状态
    status VARCHAR(20) NOT NULL COMMENT 'PENDING(待执行)/RUNNING(执行中)/SUCCESS(成功)/FAILED(失败)',
    error_code VARCHAR(50) COMMENT '错误编码',
    error_msg TEXT COMMENT '错误信息',

    -- 请求数据
    request_data TEXT COMMENT '请求数据JSON',
    mapped_data TEXT COMMENT '映射后数据JSON',

    -- 响应数据
    response_data TEXT COMMENT '响应数据JSON',
    sap_return_code VARCHAR(20) COMMENT 'SAP返回码',
    sap_message VARCHAR(500) COMMENT 'SAP返回消息',
    sap_message_type VARCHAR(10) COMMENT 'SAP消息类型: S(成功)/E(错误)/W(警告)',

    -- SAP返回的数据
    sap_key VARCHAR(100) COMMENT 'SAP返回的主键值',

    -- 执行时间
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms INT COMMENT '执行耗时(毫秒)',

    -- 重试信息
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retry INT DEFAULT 3 COMMENT '最大重试次数',
    parent_log_id BIGINT COMMENT '父日志ID(重试时关联)',

    -- 审计
    created_by VARCHAR(50) COMMENT '操作人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_log_code (log_code),
    INDEX idx_data_type_id (data_type, data_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_system_config_id (system_config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分发日志表';

-- ==========================================
-- 4. 分发批次表（批量分发时使用）
-- ==========================================
CREATE TABLE IF NOT EXISTS dis_distribution_batch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    batch_code VARCHAR(100) NOT NULL COMMENT '批次编码',

    -- 批次信息
    data_type VARCHAR(50) NOT NULL COMMENT '数据类型',
    system_config_id BIGINT COMMENT '系统配置ID',

    -- 统计
    total_count INT DEFAULT 0 COMMENT '总数',
    success_count INT DEFAULT 0 COMMENT '成功数',
    failed_count INT DEFAULT 0 COMMENT '失败数',
    pending_count INT DEFAULT 0 COMMENT '待执行数',

    -- 状态
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending(待执行)/running(执行中)/completed(已完成)/partial(部分成功)',

    -- 执行时间
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms INT COMMENT '执行耗时(毫秒)',

    -- 审计
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_batch_code (batch_code),
    INDEX idx_data_type (data_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分发批次表';

-- ==========================================
-- 初始化数据
-- ==========================================

-- 插入默认系统配置
INSERT INTO dis_system_config (config_name, config_code, system_type, system_type_name, connection_config, status, is_default, created_by)
VALUES
('SAP测试环境', 'SAP_TEST', 'SAP', 'SAP RFC/BAPI', '{"host":"127.0.0.1","systemNumber":"00","client":"800","user":"SAPUSER","password":"","language":"ZH"}', 'inactive', 1, 'system'),
('HTTP接口示例', 'HTTP_DEMO', 'HTTP', 'HTTP/REST API', '{"url":"http://api.example.com/vendor","method":"POST","authType":"None","timeout":30000,"headers":{"Content-Type":"application/json"}}', 'inactive', 0, 'system');

-- 插入供应商字段映射（示例）
INSERT INTO dis_field_mapping (data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, sap_structure, field_type, is_required, is_key, transform_type, sort_order, created_by) VALUES
('VENDOR', '供应商', 'vendor_code', '供应商编码', 'LIFNR', '供应商编号', 'VENDOR_DATA', 'STRING', 1, 1, 'DIRECT', 1, 'system'),
('VENDOR', '供应商', 'vendor_name', '供应商名称', 'NAME1', '供应商名称', 'VENDOR_DATA', 'STRING', 1, 0, 'DIRECT', 2, 'system'),
('VENDOR', '供应商', 'vendor_name2', '供应商名称2', 'NAME2', '供应商名称2', 'VENDOR_DATA', 'STRING', 0, 0, 'DIRECT', 3, 'system'),
('VENDOR', '供应商', 'country', '国家', 'LAND1', '国家代码', 'VENDOR_DATA', 'STRING', 0, 0, 'DIRECT', 4, 'system'),
('VENDOR', '供应商', 'city', '城市', 'ORT01', '城市', 'VENDOR_DATA', 'STRING', 0, 0, 'DIRECT', 5, 'system'),
('VENDOR', '供应商', 'address', '地址', 'STRAS', '街道', 'VENDOR_DATA', 'STRING', 0, 0, 'DIRECT', 6, 'system'),
('VENDOR', '供应商', 'postal_code', '邮政编码', 'PSTLZ', '邮政编码', 'VENDOR_DATA', 'STRING', 0, 0, 'DIRECT', 7, 'system'),
('VENDOR', '供应商', 'region', '地区', 'REGIO', '地区', 'VENDOR_DATA', 'STRING', 0, 0, 'DIRECT', 8, 'system'),
('VENDOR', '供应商', 'telephone', '电话', 'TELF1', '电话', 'VENDOR_DATA', 'STRING', 0, 0, 'DIRECT', 9, 'system'),
('VENDOR', '供应商', 'fax', '传真', 'TELFX', '传真', 'VENDOR_DATA', 'STRING', 0, 0, 'DIRECT', 10, 'system'),
('VENDOR', '供应商', 'email', '电子邮箱', 'SMTP_ADDR', '电子邮箱', 'VENDOR_DATA', 'STRING', 0, 0, 'DIRECT', 11, 'system'),
('VENDOR', '供应商', 'tax_no', '税号', 'STCD1', '税号', 'VENDOR_DATA', 'STRING', 0, 0, 'DIRECT', 12, 'system'),
('VENDOR', '供应商', 'vendor_type', '供应商类型', 'VEND_TYPE', '供应商类型', 'VENDOR_DATA', 'STRING', 0, 0, 'VALUE_MAP', 13, 'system'),
('VENDOR', '供应商', 'company_code', '公司代码', 'BUKRS', '公司代码', 'COMPANY_DATA', 'STRING', 1, 0, 'DIRECT', 14, 'system'),
('VENDOR', '供应商', 'purchasing_org', '采购组织', 'EKORG', '采购组织', 'PURCHASING_DATA', 'STRING', 0, 0, 'DIRECT', 15, 'system'),
('VENDOR', '供应商', 'currency', '货币', 'WAERS', '货币', 'PURCHASING_DATA', 'STRING', 0, 0, 'DIRECT', 16, 'system'),
('VENDOR', '供应商', 'payment_terms', '付款条件', 'ZTERM', '付款条件', 'PURCHASING_DATA', 'STRING', 0, 0, 'DIRECT', 17, 'system'),
('VENDOR', '供应商', 'bank_country', '银行国家', 'BANKS', '银行国家代码', 'BANK_DATA', 'STRING', 0, 0, 'DIRECT', 18, 'system'),
('VENDOR', '供应商', 'bank_key', '银行代码', 'BANKL', '银行代码', 'BANK_DATA', 'STRING', 0, 0, 'DIRECT', 19, 'system'),
('VENDOR', '供应商', 'bank_account', '银行账号', 'BANKN', '银行账号', 'BANK_DATA', 'STRING', 0, 0, 'DIRECT', 20, 'system');

SELECT '分发管理表创建完成' AS message;
