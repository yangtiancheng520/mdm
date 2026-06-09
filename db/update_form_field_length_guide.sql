-- =============================================
-- 更新表单字段长度（MySQL 5.7+ JSON函数）
-- =============================================

USE mdm;

SET NAMES utf8mb4;

-- 查看需要更新的表单
SELECT '========== Forms to Update ==========' AS '';

SELECT
    f.id,
    f.form_code,
    f.form_name,
    JSON_LENGTH(f.layout_config, '$.subTables') AS subtable_count
FROM frm_form f
WHERE f.layout_config IS NOT NULL
  AND f.layout_config != ''
  AND f.layout_config != '[]'
  AND f.layout_config != '{}';

-- 由于JSON嵌套复杂，建议使用以下方法：
-- 方法1：重新生成表单（推荐）
-- 方法2：使用Java工具类更新
-- 方法3：手动编辑JSON

SELECT '========== Recommended Actions ==========' AS '';
SELECT '方法1（推荐）：在前端重新设计表单，系统会自动从视图字段获取最新长度' AS 'Option 1';
SELECT '方法2：运行Java工具类 FormFieldLengthSyncUtil.syncFormFieldLengths()' AS 'Option 2';
SELECT '方法3：手动编辑表单的layout_config JSON' AS 'Option 3';

-- 如果要快速修复，可以清空layout_config让用户重新设计
-- UPDATE frm_form SET layout_config = NULL WHERE layout_config IS NOT NULL;
