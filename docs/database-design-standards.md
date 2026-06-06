# MDM 主数据管理平台 - 数据库设计规范

> **版本：** v1.0  
> **创建日期：** 2026-06-06  
> **适用范围：** MDM主数据管理平台所有数据库表设计

---

## 一、表命名规范

### 1.1 命名规则

表名采用 **前缀_业务名称** 的格式，全部使用小写字母，单词之间使用下划线分隔。

**格式：** `{前缀}_{业务名称}`

**示例：**
- `std_field_standard` - 字段标准库表
- `sys_organization` - 组织机构表
- `bas_user` - 用户表

### 1.2 表名前缀规范

按照一级功能模块划分表前缀：

| 前缀 | 全称 | 模块名称 | 说明 | 示例 |
|------|------|----------|------|------|
| **bas_** | Basic | 基础核心模块 | 用户、角色、权限等最基础数据 | bas_user<br>bas_role<br>bas_permission |
| **sys_** | System | 系统配置中心 | 组织管理、权限管理、规则引擎、定时任务等系统配置 | sys_organization<br>sys_config<br>sys_rule_script<br>sys_schedule_task |
| **std_** | Standard | 数据标准与模型中心 | 字段标准库、数据标准、编码规则、值域管理 | std_field_standard<br>std_data_standard<br>std_encoding_rule<br>std_value_domain |
| **frm_** | Form | 表单与视图设计中心 | 表单管理、表单设计器、导入导出 | frm_form<br>frm_field<br>frm_template |
| **wfl_** | Workflow | 流程与任务管理中心 | 流程定义、任务管理、审批流程 | wfl_definition<br>wfl_task<br>wfl_instance |
| **mst_** | Master Data | 主数据生命周期管理 | 主数据类型、实例管理、状态机、关系管理 | mst_type<br>mst_instance<br>mst_lifecycle_state |
| **qlt_** | Quality | 数据质量管理 | 质量规则、检测引擎、问题处理 | qlt_rule<br>qlt_check<br>qlt_issue |
| **dst_** | Distribution | 主数据分发管理中心 | 订阅发布、分发引擎、数据溯源 | dst_subscription<br>dst_task<br>dst_trace |
| **ver_** | Version | 版本与审计中心 | 版本管理、审计日志 | ver_snapshot<br>ver_audit_log |

### 1.3 命名最佳实践

✅ **正确示例：**
- `std_field_standard` - 清晰表明是数据标准模块的字段标准表
- `sys_organization` - 明确是系统模块的组织表
- `bas_user_role` - 基础模块的用户角色关联表

❌ **错误示例：**
- `md_field_standard` - ❌ 不应使用 md_ 前缀
- `FieldStandard` - ❌ 不应使用大驼峰命名
- `field-standard` - ❌ 不应使用连字符
- `t_field_standard` - ❌ 不应使用无意义的前缀

---

## 二、字段命名规范

### 2.1 命名规则

- 使用小写字母
- 单词之间使用下划线分隔
- 名称应具有描述性，见名知义
- 避免使用数据库保留字

**示例：**
- `field_code` - 字段编码 ✅
- `fieldName` - ❌ 应使用 field_name
- `name` - ❌ 应使用更具体的名称，如 org_name

### 2.2 常用字段命名规范

| 字段类型 | 命名规范 | 说明 | 示例 |
|---------|---------|------|------|
| **主键** | `id` | 自增主键 | `id BIGINT PRIMARY KEY AUTO_INCREMENT` |
| **编码** | `{entity}_code` | 业务编码 | `org_code`, `field_code`, `rule_code` |
| **名称** | `{entity}_name` | 名称 | `org_name`, `field_name` |
| **类型** | `{entity}_type` | 类型 | `org_type`, `field_type` |
| **父级ID** | `parent_id` | 树形结构的父节点ID | `parent_id BIGINT` |
| **状态** | `status` | 状态字段 | `status VARCHAR(20) DEFAULT 'active'` |
| **排序** | `sort` | 排序字段 | `sort INT DEFAULT 0` |
| **创建人** | `created_by` | 创建人 | `created_by VARCHAR(50)` |
| **创建时间** | `created_at` | 创建时间 | `created_at DATETIME DEFAULT CURRENT_TIMESTAMP` |
| **更新人** | `updated_by` | 更新人 | `updated_by VARCHAR(50)` |
| **更新时间** | `updated_at` | 更新时间 | `updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` |
| **描述** | `description` | 描述信息 | `description TEXT` |

### 2.3 外键命名规范

外键字段命名：`{关联表名去掉前缀}_id`

**示例：**
- `org_id` - 关联组织表（std_organization）
- `domain_id` - 关联值域表（std_value_domain）
- `parent_id` - 自关联父级ID

### 2.4 布尔型字段命名

使用 `is_{含义}` 或 `has_{含义}` 格式：

**示例：**
- `is_required` - 是否必填
- `has_children` - 是否有子节点
- `is_active` - 是否启用

### 2.5 MySQL保留字处理

如果字段名是MySQL保留字，必须使用反引号括起来：

**示例：**
```sql
`precision` INT COMMENT '精度',
`level` INT COMMENT '层级',
`range` VARCHAR(100) COMMENT '范围'
```

---

## 三、字段类型规范

### 3.1 常用字段类型

| 数据类型 | 用途 | 示例 |
|---------|------|------|
| **BIGINT** | 主键、数量统计、金额（分） | `id BIGINT`, `total_count BIGINT` |
| **INT** | 整数、长度、排序 | `length INT`, `sort INT` |
| **VARCHAR(n)** | 变长字符串（n为最大长度） | `name VARCHAR(200)` |
| **TEXT** | 长文本、JSON数据 | `description TEXT`, `config TEXT` |
| **DATETIME** | 日期时间 | `created_at DATETIME` |
| **DATE** | 日期 | `birth_date DATE`, `establish_date DATE` |
| **TINYINT** | 布尔值、小整数 | `is_required TINYINT DEFAULT 0` |
| **DECIMAL(p,s)** | 精确小数（金额、比例） | `amount DECIMAL(18,2)` |

### 3.2 字段长度建议

| 字段类型 | 建议长度 | 示例 |
|---------|---------|------|
| 编码 | 50-100 | `org_code VARCHAR(100)` |
| 名称 | 100-200 | `org_name VARCHAR(200)` |
| 描述 | TEXT | `description TEXT` |
| 手机号 | 20 | `phone VARCHAR(20)` |
| 邮箱 | 100 | `email VARCHAR(100)` |
| URL | 500 | `url VARCHAR(500)` |

---

## 四、字段注释规范

### 4.1 注释要求

**所有表和字段都必须添加注释！**

### 4.2 注释格式

#### 4.2.1 表注释格式

表注释应简洁明了，说明表的业务含义。

```sql
COMMENT='表的中文名称'
```

**示例：**
```sql
CREATE TABLE std_field_standard (
    ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段标准库表';
```

#### 4.2.2 字段注释格式

字段注释应包含：
1. 字段的中文含义
2. 可选值说明（如果是枚举类型）
3. 单位说明（如果有单位）

**格式：** `COMMENT '中文说明'`

**示例：**
```sql
status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-启用/inactive-停用',
field_type VARCHAR(50) NOT NULL COMMENT '字段类型: string/number/date/datetime/boolean/text/select/multi_select',
amount DECIMAL(18,2) COMMENT '金额(元)'
```

### 4.3 枚举类型注释规范

枚举类型字段必须在注释中说明所有可选值：

**格式：** `COMMENT '说明: value1-含义1/value2-含义2/...'`

**示例：**
```sql
status VARCHAR(20) DEFAULT 'draft' COMMENT '状态: draft-草稿/published-已发布/archived-已归档',
org_type VARCHAR(50) COMMENT '组织类型: company-公司/department-部门/group-组/position-岗位',
data_type VARCHAR(50) COMMENT '数据类型: master_data-主数据/reference_data-参考数据/business_data-业务数据'
```

---

## 五、主键和索引规范

### 5.1 主键规范

**所有表必须有主键，推荐使用自增BIGINT类型。**

```sql
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID'
```

### 5.2 索引命名规范

| 索引类型 | 命名格式 | 示例 |
|---------|---------|------|
| **普通索引** | `idx_{字段名}` | `idx_org_code` |
| **唯一索引** | `uk_{字段名}` 或字段 UNIQUE 约束 | `uk_field_code` |
| **联合索引** | `idx_{字段1}_{字段2}` | `idx_org_type_status` |
| **外键索引** | 自动创建，或 `fk_{字段名}` | `fk_org_id` |

### 5.3 索引示例

```sql
CREATE TABLE std_field_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    field_code VARCHAR(100) NOT NULL COMMENT '字段编码',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态: draft-草稿/published-已发布/archived-已归档',
    category VARCHAR(50) COMMENT '字段分类',
    
    UNIQUE KEY `field_code` (`field_code`),
    INDEX idx_field_code (field_code),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段标准库表';
```

---

## 六、默认值规范

### 6.1 必填字段默认值

- 字符串类型：根据业务需要设置默认值
- 数字类型：设置合理的业务默认值
- 布尔类型：明确默认值

### 6.2 公共字段默认值

```sql
-- 状态字段
status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-启用/inactive-停用',

-- 排序字段
sort INT DEFAULT 0 COMMENT '排序',

-- 创建时间
created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

-- 更新时间
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

-- 版本号
version INT DEFAULT 1 COMMENT '版本号'
```

---

## 七、字符集和排序规则

### 7.1 数据库字符集

**推荐使用：** `utf8mb4` 字符集，支持中文和emoji表情。

```sql
CREATE DATABASE mdm 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### 7.2 表字符集

```sql
CREATE TABLE std_field_standard (
    ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段标准库表';
```

---

## 八、表设计模板

### 8.1 标准表模板

```sql
CREATE TABLE {前缀}_{表名} (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    
    -- 业务字段
    {field}_code VARCHAR(100) NOT NULL UNIQUE COMMENT '{字段}编码',
    {field}_name VARCHAR(200) NOT NULL COMMENT '{字段}名称',
    {field}_type VARCHAR(50) COMMENT '{字段}类型: type1-含义1/type2-含义2',
    
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

### 8.2 关联表模板

```sql
CREATE TABLE {前缀}_{表1}_{表2} (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    {表1}_id BIGINT NOT NULL COMMENT '{表1中文名}ID',
    {表2}_id BIGINT NOT NULL COMMENT '{表2中文名}ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_{表1}_id ({表1}_id),
    INDEX idx_{表2}_id ({表2}_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{表1中文名}{表2中文名}关联表';
```

---

## 九、表结构示例

### 9.1 字段标准库表（完整示例）

```sql
CREATE TABLE std_field_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    field_code VARCHAR(100) NOT NULL UNIQUE COMMENT '字段编码',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    field_type VARCHAR(50) NOT NULL COMMENT '字段类型: string/number/date/datetime/boolean/text/select/multi_select',
    length INT COMMENT '字段长度',
    `precision` INT COMMENT '精度',
    default_value VARCHAR(500) COMMENT '默认值',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填: 0-否 1-是',
    validation_rule TEXT COMMENT '校验规则JSON',
    reference_id BIGINT COMMENT '引用中台字段ID',
    reference_source VARCHAR(50) COMMENT '引用来源: data_platform/manual',
    category VARCHAR(50) COMMENT '字段分类',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态: draft-草稿/published-已发布/archived-已归档',
    version INT DEFAULT 1 COMMENT '版本号',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    
    UNIQUE KEY `field_code` (`field_code`),
    INDEX idx_field_code (field_code),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段标准库表';
```

### 9.2 组织机构表（完整示例）

```sql
CREATE TABLE sys_organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    org_code VARCHAR(100) NOT NULL UNIQUE COMMENT '组织编码',
    org_name VARCHAR(200) NOT NULL COMMENT '组织名称',
    org_type VARCHAR(50) COMMENT '组织类型: company-公司/department-部门/group-组/position-岗位',
    parent_id BIGINT COMMENT '父级ID',
    level INT DEFAULT 1 COMMENT '层级',
    path VARCHAR(500) COMMENT '路径: /1/2/3',
    manager VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-启用/inactive-停用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述',
    
    UNIQUE KEY `org_code` (`org_code`),
    INDEX idx_parent_id (parent_id),
    INDEX idx_path (path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织机构表';
```

---

## 十、数据库设计检查清单

在提交数据库设计时，请确保：

### 10.1 表设计检查

- [ ] 表名使用正确的前缀（std_、sys_、bas_等）
- [ ] 表名使用小写字母和下划线
- [ ] 表有清晰的中文注释
- [ ] 表使用 utf8mb4 字符集

### 10.2 字段设计检查

- [ ] 所有字段都有中文注释
- [ ] 主键使用 `id BIGINT PRIMARY KEY AUTO_INCREMENT`
- [ ] 枚举类型字段注释说明了所有可选值
- [ ] 外键字段命名规范（关联表去掉前缀_id）
- [ ] MySQL保留字使用了反引号
- [ ] 有合适的默认值

### 10.3 索引设计检查

- [ ] 主键已设置
- [ ] 唯一约束字段有唯一索引
- [ ] 常用查询字段有索引
- [ ] 索引命名规范（idx_、uk_、fk_）

### 10.4 公共字段检查

- [ ] 包含 created_by 和 created_at
- [ ] 包含 updated_by 和 updated_at
- [ ] 包含 status 字段（如适用）
- [ ] 包含 description 字段（如适用）

---

## 十一、版本历史

| 版本 | 日期 | 修改内容 | 修改人 |
|------|------|---------|--------|
| v1.0 | 2026-06-06 | 初始版本，制定数据库设计规范 | MDM团队 |

---

## 十二、参考资料

- MySQL 8.0 官方文档：https://dev.mysql.com/doc/refman/8.0/en/
- 阿里巴巴 Java 开发手册（数据库规范）
- 《GB/T 18391 信息技术 数据元的规范与标准化》

---

**文档维护：** MDM开发团队  
**最后更新：** 2026-06-06
