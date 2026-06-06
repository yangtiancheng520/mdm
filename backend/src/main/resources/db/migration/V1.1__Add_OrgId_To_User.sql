-- 为用户表添加组织ID字段
-- 执行时间: 2026-06-06
-- 说明: 支持用户归属组织功能

-- 添加 org_id 字段
ALTER TABLE bas_user ADD COLUMN org_id BIGINT NULL COMMENT '组织ID';

-- 添加外键索引（可选，提升查询性能）
CREATE INDEX idx_user_org_id ON bas_user(org_id);

-- 添加外键约束（可选，确保数据完整性）
-- ALTER TABLE bas_user ADD CONSTRAINT fk_user_org FOREIGN KEY (org_id) REFERENCES sys_organization(id);

-- 说明：
-- 1. org_id 允许为空，表示用户可以不属于任何组织
-- 2. 添加了索引以提升按组织查询用户的性能
-- 3. 外键约束已注释，可根据实际需求启用
