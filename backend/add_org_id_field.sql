-- ==========================================
-- 手动执行此脚本以添加 org_id 字段
-- ==========================================

-- 1. 添加 org_id 字段到 bas_user 表
ALTER TABLE bas_user ADD COLUMN org_id BIGINT NULL COMMENT '组织ID';

-- 2. 添加索引以提升查询性能
CREATE INDEX idx_user_org_id ON bas_user(org_id);

-- 3. 验证字段已添加
-- 执行后应该能看到 org_id 字段
DESCRIBE bas_user;

-- ==========================================
-- 如果字段已存在，会报错，可以忽略
-- ==========================================
