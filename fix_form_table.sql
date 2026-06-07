-- 修复 frm_form 表结构，添加缺失的字段
USE mdm;

-- 添加缺失的字段
ALTER TABLE frm_form
ADD COLUMN IF NOT EXISTS design_mode VARCHAR(20) DEFAULT 'auto' COMMENT 'auto-自动生成/blank-空白创建' AFTER view_id;

-- 如果上面的语法不支持，用这个
-- ALTER TABLE frm_form ADD COLUMN design_mode VARCHAR(20) DEFAULT 'auto' COMMENT 'auto-自动生成/blank-空白创建';

SELECT '表结构更新完成' AS message;
