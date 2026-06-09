-- =============================================
-- SAP测试环境配置 - MZ-QAS
-- =============================================

USE mdm;

-- 删除已存在的测试配置（如果有）
DELETE FROM dis_system_config WHERE config_code = 'MZ-QAS';

-- 插入SAP测试环境配置
INSERT INTO dis_system_config (
    config_name,
    config_code,
    system_type,
    system_type_name,
    connection_config,
    pool_size,
    timeout,
    status,
    is_default,
    created_by,
    remark
) VALUES (
    'SAP测试环境-MZ-QAS',
    'MZ-QAS',
    'SAP',
    'SAP RFC/BAPI',
    '{"name":"MZ-QAS","ashost":"172.30.2.32","sysnr":"00","client":"500","user":"yangtc","passwd":"19830101","lang":"ZH"}',
    10,
    30000,
    'active',
    1,
    'yangtc',
    'SAP测试环境连接配置'
);

SELECT 'SAP测试环境配置插入成功' AS message;
SELECT * FROM dis_system_config WHERE config_code = 'MZ-QAS';
