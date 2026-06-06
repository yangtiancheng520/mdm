# MDM 数据库设计规范 - 快速参考

> 📋 一页速查表，用于日常开发快速参考

---

## 表名前缀速查

| 前缀 | 模块 | 说明 |
|------|------|------|
| `bas_` | 基础核心 | 用户、角色、权限 |
| `sys_` | 系统配置 | 组织、配置、规则、任务 |
| `std_` | 数据标准 | 字段标准、编码规则、值域 |
| `frm_` | 表单管理 | 表单、字段配置 |
| `wfl_` | 流程管理 | 流程定义、任务、实例 |
| `mst_` | 主数据 | 类型、实例、状态 |
| `qlt_` | 数据质量 | 规则、检测、问题 |
| `dst_` | 分发管理 | 订阅、分发、溯源 |
| `ver_` | 版本审计 | 快照、审计日志 |

---

## 字段命名速查

### 公共字段（必须）

```sql
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
created_by VARCHAR(50) COMMENT '创建人',
created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updated_by VARCHAR(50) COMMENT '更新人',
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
```

### 常用字段

| 用途 | 命名 | 示例 |
|------|------|------|
| 编码 | `{entity}_code` | `org_code`, `field_code` |
| 名称 | `{entity}_name` | `org_name`, `field_name` |
| 类型 | `{entity}_type` | `org_type`, `field_type` |
| 父级 | `parent_id` | `parent_id BIGINT` |
| 状态 | `status` | `status VARCHAR(20)` |
| 排序 | `sort` | `sort INT DEFAULT 0` |
| 描述 | `description` | `description TEXT` |

---

## 注释规范

### 表注释
```sql
COMMENT='字段标准库表'
```

### 字段注释
```sql
-- 普通字段
field_name VARCHAR(200) COMMENT '字段名称',

-- 枚举字段（必须列出所有值）
status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-启用/inactive-停用',

-- 有单位的字段
amount DECIMAL(18,2) COMMENT '金额(元)',
```

---

## 索引命名

| 类型 | 格式 | 示例 |
|------|------|------|
| 普通 | `idx_{字段}` | `idx_org_code` |
| 唯一 | `uk_{字段}` 或 UNIQUE 约束 | `uk_field_code` |
| 联合 | `idx_{字段1}_{字段2}` | `idx_org_type_status` |

---

## 字段类型选择

| 类型 | 用途 |
|------|------|
| `BIGINT` | 主键、数量、金额（分） |
| `INT` | 整数、长度、排序 |
| `VARCHAR(n)` | 变长字符串 |
| `TEXT` | 长文本、JSON |
| `DATETIME` | 日期时间 |
| `TINYINT` | 布尔值、小整数 |
| `DECIMAL(18,2)` | 精确小数、金额 |

---

## 建表模板

```sql
CREATE TABLE {前缀}_{表名} (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    
    -- 业务字段
    {field}_code VARCHAR(100) NOT NULL UNIQUE COMMENT '{字段}编码',
    {field}_name VARCHAR(200) NOT NULL COMMENT '{字段}名称',
    
    -- 公共字段
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-启用/inactive-停用',
    sort INT DEFAULT 0 COMMENT '排序',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    
    -- 索引
    INDEX idx_{field}_code ({field}_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{表中文名}';
```

---

## 检查清单 ✓

提交前确认：

- [ ] 表名使用正确前缀
- [ ] 表有中文注释
- [ ] 所有字段有注释
- [ ] 枚举字段注释列出了所有值
- [ ] 有 created_by/at 和 updated_by/at
- [ ] 主键是 BIGINT AUTO_INCREMENT
- [ ] 字符集是 utf8mb4

---

## 常见错误 ❌

```sql
-- ❌ 错误：前缀不对
CREATE TABLE md_field_standard ...

-- ❌ 错误：没有注释
field_name VARCHAR(200),

-- ❌ 错误：枚举没有列出可选值
status VARCHAR(20) COMMENT '状态',

-- ❌ 错误：字段名不规范
fieldName VARCHAR(200),

-- ✅ 正确
CREATE TABLE std_field_standard (
    field_name VARCHAR(200) NOT NULL COMMENT '字段名称',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态: draft-草稿/published-已发布',
    ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段标准库表';
```

---

**详细规范请参考：** [database-design-standards.md](./database-design-standards.md)
