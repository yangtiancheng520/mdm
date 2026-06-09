-- =============================================
-- MDM 规则模板优化 - 区分系统模板和自定义模板
-- 版本: v3.0
-- 日期: 2026-06-09
-- 说明: 添加系统模板标识，优化模板管理
-- =============================================

SET NAMES utf8mb4;
USE mdm;

-- =============================================
-- 1. 添加系统模板标识字段
-- =============================================

-- 添加 is_system 字段（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'qlt_rule_template';
SET @columnname = 'is_system';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TINYINT DEFAULT 0 COMMENT ''是否系统模板'' AFTER status')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =============================================
-- 2. 添加分类字段
-- =============================================

SET @columnname = 'category';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(50) COMMENT ''模板分类'' AFTER template_type')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =============================================
-- 3. 添加标签字段
-- =============================================

SET @columnname = 'tags';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(200) COMMENT ''标签，逗号分隔'' AFTER category')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =============================================
-- 4. 标记现有预置模板为系统模板
-- =============================================

UPDATE qlt_rule_template
SET is_system = 1
WHERE created_by = 'system' OR template_code LIKE 'TPL_%';

-- =============================================
-- 5. 更新模板分类
-- =============================================

UPDATE qlt_rule_template SET category = '基础校验' WHERE template_code IN ('TPL_NOT_NULL', 'TPL_NOT_EMPTY');
UPDATE qlt_rule_template SET category = '格式校验' WHERE template_code IN ('TPL_EMAIL', 'TPL_PHONE', 'TPL_ID_CARD');
UPDATE qlt_rule_template SET category = '长度校验' WHERE template_code IN ('TPL_LENGTH_50', 'TPL_LENGTH_100');
UPDATE qlt_rule_template SET category = '范围校验' WHERE template_code IN ('TPL_RANGE_0_100', 'TPL_DOMAIN');
UPDATE qlt_rule_template SET category = '唯一性校验' WHERE template_code IN ('TPL_UNIQUE_GLOBAL', 'TPL_UNIQUE_IN_MAIN');

-- =============================================
-- 6. 更新标签
-- =============================================

UPDATE qlt_rule_template SET tags = '常用,必填' WHERE template_code = 'TPL_NOT_NULL';
UPDATE qlt_rule_template SET tags = '常用,联系方式' WHERE template_code IN ('TPL_EMAIL', 'TPL_PHONE');
UPDATE qlt_rule_template SET tags = '个人信息' WHERE template_code = 'TPL_ID_CARD';

-- =============================================
-- 7. 查看结果
-- =============================================

SELECT '========================================' AS '';
SELECT '规则模板优化完成！' AS message;
SELECT '========================================' AS '';

SELECT
    id,
    template_code AS template_code,
    template_name AS template_name,
    template_type AS template_type,
    category AS category,
    is_system AS is_system,
    status AS status
FROM qlt_rule_template
ORDER BY is_system DESC, template_type, id;
