-- =============================================
-- MDM 数据库迁移验证脚本
-- 验证所有表是否已正确重命名
-- =============================================

USE mdm;

-- =============================================
-- 1. 验证基础认证授权模块表 (bas_)
-- =============================================

SELECT '验证基础认证授权模块表...' AS step;

SELECT
    TABLE_NAME,
    TABLE_COMMENT,
    CASE
        WHEN TABLE_NAME IN ('bas_user', 'bas_role', 'bas_permission', 'bas_user_role', 'bas_role_permission')
        THEN '✅ 正确'
        ELSE '❌ 错误'
    END AS status
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'bas_%'
ORDER BY TABLE_NAME;

-- =============================================
-- 2. 验证数据标准与模型中心表 (std_)
-- =============================================

SELECT '验证数据标准与模型中心表...' AS step;

SELECT
    TABLE_NAME,
    TABLE_COMMENT,
    CASE
        WHEN TABLE_NAME IN ('std_field', 'std_data', 'std_encoding_rule', 'std_value_domain')
        THEN '✅ 正确'
        ELSE '❌ 错误'
    END AS status
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'std_%'
ORDER BY TABLE_NAME;

-- =============================================
-- 3. 验证表单与视图设计中心表 (frm_)
-- =============================================

SELECT '验证表单与视图设计中心表...' AS step;

SELECT
    TABLE_NAME,
    TABLE_COMMENT,
    CASE
        WHEN TABLE_NAME IN ('frm_form', 'frm_field')
        THEN '✅ 正确'
        ELSE '❌ 错误'
    END AS status
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'frm_%'
ORDER BY TABLE_NAME;

-- =============================================
-- 4. 验证流程与任务管理中心表 (wfl_)
-- =============================================

SELECT '验证流程与任务管理中心表...' AS step;

SELECT
    TABLE_NAME,
    TABLE_COMMENT,
    CASE
        WHEN TABLE_NAME IN ('wfl_definition', 'wfl_instance', 'wfl_task')
        THEN '✅ 正确'
        ELSE '❌ 错误'
    END AS status
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'wfl_%'
ORDER BY TABLE_NAME;

-- =============================================
-- 5. 验证主数据生命周期管理表 (mst_)
-- =============================================

SELECT '验证主数据生命周期管理表...' AS step;

SELECT
    TABLE_NAME,
    TABLE_COMMENT,
    CASE
        WHEN TABLE_NAME IN ('mst_type', 'mst_instance', 'mst_lifecycle_state')
        THEN '✅ 正确'
        ELSE '❌ 错误'
    END AS status
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'mst_%'
ORDER BY TABLE_NAME;

-- =============================================
-- 6. 验证版本与审计中心表 (ver_)
-- =============================================

SELECT '验证版本与审计中心表...' AS step;

SELECT
    TABLE_NAME,
    TABLE_COMMENT,
    CASE
        WHEN TABLE_NAME IN ('ver_snapshot', 'ver_audit_log')
        THEN '✅ 正确'
        ELSE '❌ 错误'
    END AS status
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'ver_%'
ORDER BY TABLE_NAME;

-- =============================================
-- 7. 验证系统配置模块表 (sys_)
-- =============================================

SELECT '验证系统配置模块表...' AS step;

SELECT
    TABLE_NAME,
    TABLE_COMMENT,
    '✅ 正确' AS status
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND TABLE_NAME LIKE 'sys_%'
ORDER BY TABLE_NAME;

-- =============================================
-- 8. 检查是否有旧表名残留
-- =============================================

SELECT '检查旧表名残留...' AS step;

SELECT
    TABLE_NAME,
    '❌ 警告: 发现旧表名!' AS status
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm'
  AND (
    TABLE_NAME IN ('users', 'roles', 'permissions', 'user_roles', 'role_permissions')
    OR TABLE_NAME LIKE 'md_field_standard'
    OR TABLE_NAME LIKE 'md_data_standard'
    OR TABLE_NAME LIKE 'md_encoding_rule'
    OR TABLE_NAME LIKE 'md_value_domain'
    OR TABLE_NAME LIKE 'md_form%'
    OR TABLE_NAME LIKE 'md_workflow%'
    OR TABLE_NAME LIKE 'md_task'
    OR TABLE_NAME LIKE 'md_master_data%'
    OR TABLE_NAME LIKE 'md_lifecycle%'
    OR TABLE_NAME LIKE 'md_version%'
    OR TABLE_NAME LIKE 'md_audit%'
  );

-- =============================================
-- 9. 统计验证结果
-- =============================================

SELECT '统计验证结果...' AS step;

SELECT
    '总计表数量' AS item,
    COUNT(*) AS count,
    CASE
        WHEN COUNT(*) >= 20 THEN '✅ 正常'
        ELSE '⚠️ 表数量不足'
    END AS status
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm';

-- =============================================
-- 10. 生成迁移报告
-- =============================================

SELECT '生成迁移报告...' AS step;

SELECT
    '基础认证模块 (bas_)' AS module,
    COUNT(*) AS table_count,
    GROUP_CONCAT(TABLE_NAME ORDER BY TABLE_NAME SEPARATOR ', ') AS tables
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME LIKE 'bas_%'

UNION ALL

SELECT
    '数据标准模块 (std_)' AS module,
    COUNT(*) AS table_count,
    GROUP_CONCAT(TABLE_NAME ORDER BY TABLE_NAME SEPARATOR ', ') AS tables
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME LIKE 'std_%'

UNION ALL

SELECT
    '表单设计模块 (frm_)' AS module,
    COUNT(*) AS table_count,
    GROUP_CONCAT(TABLE_NAME ORDER BY TABLE_NAME SEPARATOR ', ') AS tables
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME LIKE 'frm_%'

UNION ALL

SELECT
    '流程管理模块 (wfl_)' AS module,
    COUNT(*) AS table_count,
    GROUP_CONCAT(TABLE_NAME ORDER BY TABLE_NAME SEPARATOR ', ') AS tables
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME LIKE 'wfl_%'

UNION ALL

SELECT
    '主数据模块 (mst_)' AS module,
    COUNT(*) AS table_count,
    GROUP_CONCAT(TABLE_NAME ORDER BY TABLE_NAME SEPARATOR ', ') AS tables
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME LIKE 'mst_%'

UNION ALL

SELECT
    '版本审计模块 (ver_)' AS module,
    COUNT(*) AS table_count,
    GROUP_CONCAT(TABLE_NAME ORDER BY TABLE_NAME SEPARATOR ', ') AS tables
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME LIKE 'ver_%'

UNION ALL

SELECT
    '系统配置模块 (sys_)' AS module,
    COUNT(*) AS table_count,
    GROUP_CONCAT(TABLE_NAME ORDER BY TABLE_NAME SEPARATOR ', ') AS tables
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'mdm' AND TABLE_NAME LIKE 'sys_%';

-- =============================================
-- 完成
-- =============================================

SELECT '========================================' AS separator;
SELECT '迁移验证完成！' AS message;
SELECT '请检查上述结果，确保所有表都已正确重命名。' AS note;
SELECT '========================================' AS separator;
