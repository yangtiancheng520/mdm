-- 修复视图模型相关表的注释
-- 执行方式: mysql -u root -p mdm < fix_view_comments.sql

-- 1. std_view 视图定义表
ALTER TABLE std_view COMMENT '视图定义表';
ALTER TABLE std_view MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE std_view MODIFY COLUMN view_code VARCHAR(100) NOT NULL COMMENT '视图编码';
ALTER TABLE std_view MODIFY COLUMN view_name VARCHAR(200) NOT NULL COMMENT '视图名称';
ALTER TABLE std_view MODIFY COLUMN category_id BIGINT COMMENT '分类ID';
ALTER TABLE std_view MODIFY COLUMN version INT DEFAULT 1 COMMENT '版本号';
ALTER TABLE std_view MODIFY COLUMN base_version_id BIGINT COMMENT '基础版本ID';
ALTER TABLE std_view MODIFY COLUMN is_latest TINYINT(1) DEFAULT 1 COMMENT '是否最新版本';
ALTER TABLE std_view MODIFY COLUMN layout_columns INT DEFAULT 2 COMMENT '默认列数';
ALTER TABLE std_view MODIFY COLUMN label_width INT DEFAULT 100 COMMENT '标签宽度';
ALTER TABLE std_view MODIFY COLUMN enable_copy TINYINT(1) DEFAULT 1 COMMENT '启用复制';
ALTER TABLE std_view MODIFY COLUMN enable_import TINYINT(1) DEFAULT 0 COMMENT '启用导入';
ALTER TABLE std_view MODIFY COLUMN enable_export TINYINT(1) DEFAULT 1 COMMENT '启用导出';
ALTER TABLE std_view MODIFY COLUMN status VARCHAR(20) DEFAULT 'draft' COMMENT '状态';
ALTER TABLE std_view MODIFY COLUMN publish_time DATETIME COMMENT '发布时间';
ALTER TABLE std_view MODIFY COLUMN description TEXT COMMENT '描述';
ALTER TABLE std_view MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人';
ALTER TABLE std_view MODIFY COLUMN created_at DATETIME COMMENT '创建时间';
ALTER TABLE std_view MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人';
ALTER TABLE std_view MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- 2. std_view_entity 实体定义表
ALTER TABLE std_view_entity COMMENT '实体定义表';
ALTER TABLE std_view_entity MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE std_view_entity MODIFY COLUMN view_id BIGINT NOT NULL COMMENT '视图ID';
ALTER TABLE std_view_entity MODIFY COLUMN entity_code VARCHAR(100) NOT NULL COMMENT '实体编码';
ALTER TABLE std_view_entity MODIFY COLUMN entity_name VARCHAR(200) NOT NULL COMMENT '实体名称';
ALTER TABLE std_view_entity MODIFY COLUMN entity_type VARCHAR(20) NOT NULL COMMENT '实体类型';
ALTER TABLE std_view_entity MODIFY COLUMN sort INT DEFAULT 0 COMMENT '排序';
ALTER TABLE std_view_entity MODIFY COLUMN min_rows INT DEFAULT 0 COMMENT '最小行数';
ALTER TABLE std_view_entity MODIFY COLUMN max_rows INT COMMENT '最大行数';
ALTER TABLE std_view_entity MODIFY COLUMN enable_add TINYINT(1) DEFAULT 1 COMMENT '允许新增行';
ALTER TABLE std_view_entity MODIFY COLUMN enable_delete TINYINT(1) DEFAULT 1 COMMENT '允许删除行';
ALTER TABLE std_view_entity MODIFY COLUMN enable_copy TINYINT(1) DEFAULT 0 COMMENT '允许复制行';
ALTER TABLE std_view_entity MODIFY COLUMN enable_sort TINYINT(1) DEFAULT 0 COMMENT '允许排序';
ALTER TABLE std_view_entity MODIFY COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT '状态';
ALTER TABLE std_view_entity MODIFY COLUMN description TEXT COMMENT '描述';
ALTER TABLE std_view_entity MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人';
ALTER TABLE std_view_entity MODIFY COLUMN created_at DATETIME COMMENT '创建时间';
ALTER TABLE std_view_entity MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人';
ALTER TABLE std_view_entity MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- 3. std_view_group 字段分组表
ALTER TABLE std_view_group COMMENT '字段分组表';
ALTER TABLE std_view_group MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE std_view_group MODIFY COLUMN entity_id BIGINT NOT NULL COMMENT '实体ID';
ALTER TABLE std_view_group MODIFY COLUMN group_code VARCHAR(100) NOT NULL COMMENT '分组编码';
ALTER TABLE std_view_group MODIFY COLUMN group_name VARCHAR(200) NOT NULL COMMENT '分组名称';
ALTER TABLE std_view_group MODIFY COLUMN sort INT DEFAULT 0 COMMENT '排序';
ALTER TABLE std_view_group MODIFY COLUMN column_count INT DEFAULT 1 COMMENT '分组内列数';
ALTER TABLE std_view_group MODIFY COLUMN collapsible TINYINT(1) DEFAULT 1 COMMENT '可折叠';
ALTER TABLE std_view_group MODIFY COLUMN default_collapsed TINYINT(1) DEFAULT 0 COMMENT '默认折叠';
ALTER TABLE std_view_group MODIFY COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT '状态';
ALTER TABLE std_view_group MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人';
ALTER TABLE std_view_group MODIFY COLUMN created_at DATETIME COMMENT '创建时间';
ALTER TABLE std_view_group MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人';
ALTER TABLE std_view_group MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- 4. std_view_field 视图字段表
ALTER TABLE std_view_field COMMENT '视图字段表';
ALTER TABLE std_view_field MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE std_view_field MODIFY COLUMN entity_id BIGINT NOT NULL COMMENT '实体ID';
ALTER TABLE std_view_field MODIFY COLUMN field_code VARCHAR(100) NOT NULL COMMENT '字段编码';
ALTER TABLE std_view_field MODIFY COLUMN field_name VARCHAR(200) NOT NULL COMMENT '字段名称';
ALTER TABLE std_view_field MODIFY COLUMN field_standard_id BIGINT COMMENT '字段标准ID';
ALTER TABLE std_view_field MODIFY COLUMN field_type VARCHAR(50) COMMENT '字段类型';
ALTER TABLE std_view_field MODIFY COLUMN length INT COMMENT '长度';
ALTER TABLE std_view_field MODIFY COLUMN precision_val INT COMMENT '精度';
ALTER TABLE std_view_field MODIFY COLUMN group_id BIGINT COMMENT '分组ID';
ALTER TABLE std_view_field MODIFY COLUMN sort INT DEFAULT 0 COMMENT '排序';
ALTER TABLE std_view_field MODIFY COLUMN column_span INT DEFAULT 1 COMMENT '跨列数';
ALTER TABLE std_view_field MODIFY COLUMN is_required TINYINT(1) DEFAULT 0 COMMENT '必填';
ALTER TABLE std_view_field MODIFY COLUMN is_readonly TINYINT(1) DEFAULT 0 COMMENT '只读';
ALTER TABLE std_view_field MODIFY COLUMN is_hidden TINYINT(1) DEFAULT 0 COMMENT '隐藏';
ALTER TABLE std_view_field MODIFY COLUMN is_unique TINYINT(1) DEFAULT 0 COMMENT '唯一';
ALTER TABLE std_view_field MODIFY COLUMN is_query TINYINT(1) DEFAULT 0 COMMENT '查询条件';
ALTER TABLE std_view_field MODIFY COLUMN is_query_result TINYINT(1) DEFAULT 1 COMMENT '列表显示';
ALTER TABLE std_view_field MODIFY COLUMN is_sortable TINYINT(1) DEFAULT 0 COMMENT '可排序';
ALTER TABLE std_view_field MODIFY COLUMN default_value TEXT COMMENT '默认值';
ALTER TABLE std_view_field MODIFY COLUMN default_value_type VARCHAR(50) DEFAULT 'constant' COMMENT '默认值类型';
ALTER TABLE std_view_field MODIFY COLUMN ref_source VARCHAR(50) COMMENT '参照来源';
ALTER TABLE std_view_field MODIFY COLUMN ref_id BIGINT COMMENT '参照ID';
ALTER TABLE std_view_field MODIFY COLUMN ref_filter TEXT COMMENT '参照过滤条件';
ALTER TABLE std_view_field MODIFY COLUMN ref_cascade_field VARCHAR(100) COMMENT '级联父字段';
ALTER TABLE std_view_field MODIFY COLUMN enum_code VARCHAR(100) COMMENT '枚举编码';
ALTER TABLE std_view_field MODIFY COLUMN min_length INT COMMENT '最小长度';
ALTER TABLE std_view_field MODIFY COLUMN max_length INT COMMENT '最大长度';
ALTER TABLE std_view_field MODIFY COLUMN min_value DECIMAL(20,4) COMMENT '最小值';
ALTER TABLE std_view_field MODIFY COLUMN max_value DECIMAL(20,4) COMMENT '最大值';
ALTER TABLE std_view_field MODIFY COLUMN regex_pattern VARCHAR(500) COMMENT '正则校验';
ALTER TABLE std_view_field MODIFY COLUMN error_message VARCHAR(500) COMMENT '校验失败提示';
ALTER TABLE std_view_field MODIFY COLUMN link_config TEXT COMMENT '联动配置';
ALTER TABLE std_view_field MODIFY COLUMN placeholder VARCHAR(200) COMMENT '占位提示';
ALTER TABLE std_view_field MODIFY COLUMN tooltip TEXT COMMENT '提示信息';
ALTER TABLE std_view_field MODIFY COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT '状态';
ALTER TABLE std_view_field MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人';
ALTER TABLE std_view_field MODIFY COLUMN created_at DATETIME COMMENT '创建时间';
ALTER TABLE std_view_field MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人';
ALTER TABLE std_view_field MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- 5. std_view_validation 校验规则表
ALTER TABLE std_view_validation COMMENT '校验规则表';
ALTER TABLE std_view_validation MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE std_view_validation MODIFY COLUMN view_id BIGINT NOT NULL COMMENT '视图ID';
ALTER TABLE std_view_validation MODIFY COLUMN rule_code VARCHAR(100) NOT NULL COMMENT '规则编码';
ALTER TABLE std_view_validation MODIFY COLUMN rule_name VARCHAR(200) NOT NULL COMMENT '规则名称';
ALTER TABLE std_view_validation MODIFY COLUMN rule_type VARCHAR(50) NOT NULL COMMENT '规则类型';
ALTER TABLE std_view_validation MODIFY COLUMN trigger_entity_id BIGINT COMMENT '触发实体ID';
ALTER TABLE std_view_validation MODIFY COLUMN trigger_field_id BIGINT COMMENT '触发字段ID';
ALTER TABLE std_view_validation MODIFY COLUMN trigger_condition TEXT COMMENT '触发条件';
ALTER TABLE std_view_validation MODIFY COLUMN target_entity_id BIGINT COMMENT '目标实体ID';
ALTER TABLE std_view_validation MODIFY COLUMN target_field_id BIGINT COMMENT '目标字段ID';
ALTER TABLE std_view_validation MODIFY COLUMN action VARCHAR(50) COMMENT '动作';
ALTER TABLE std_view_validation MODIFY COLUMN action_value TEXT COMMENT '动作值';
ALTER TABLE std_view_validation MODIFY COLUMN error_message VARCHAR(500) COMMENT '错误提示';
ALTER TABLE std_view_validation MODIFY COLUMN error_level VARCHAR(20) DEFAULT 'error' COMMENT '错误级别';
ALTER TABLE std_view_validation MODIFY COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT '状态';
ALTER TABLE std_view_validation MODIFY COLUMN created_by VARCHAR(50) COMMENT '创建人';
ALTER TABLE std_view_validation MODIFY COLUMN created_at DATETIME COMMENT '创建时间';
ALTER TABLE std_view_validation MODIFY COLUMN updated_by VARCHAR(50) COMMENT '更新人';
ALTER TABLE std_view_validation MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';
