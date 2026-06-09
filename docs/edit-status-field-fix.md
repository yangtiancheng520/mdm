# 编辑功能 status 字段冲突修复

## 修改时间
2026-06-08

## 问题描述

编辑保存数据后，查询不到数据。原因是 `status` 字段被前端修改，导致查询条件 `WHERE status = 'active'` 无法匹配。

## 问题原因

### 数据流程

1. **查询数据**
   ```sql
   SELECT * FROM table WHERE status = 'active'
   ```
   只查询 `status = 'active'` 的数据

2. **编辑数据**
   ```typescript
   formData.value = { ...row }
   ```
   前端将整行数据（包括 `status`、`created_by`、`created_at` 等系统字段）复制到表单

3. **保存数据**
   ```typescript
   await updateInstance(categoryId, formId, recordId, allData)
   ```
   将包含系统字段的数据发送给后端

4. **更新数据**
   ```java
   for (var field : entity.getFields()) {
       if (data.containsKey(field.getFieldCode())) {
           sql.append(field.getFieldCode()).append(" = ?, ");
           params.add(data.get(field.getFieldCode()));
       }
   }
   ```
   后端将所有字段（包括 `status`）都更新到数据库

### 冲突点

- **前端**：编辑时复制了整行数据，包括系统字段
- **后端**：更新时没有过滤系统字段，导致 `status` 被修改
- **查询**：只查询 `status = 'active'` 的数据，如果 `status` 被修改则查不到

## 解决方案

### 方案对比

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| 前端过滤 | 减少网络传输 | 前端需要维护字段列表 | ⭐⭐⭐ |
| 后端过滤 | 更安全，统一控制 | 无 | ⭐⭐⭐⭐⭐ |
| 数据库触发器 | 自动处理 | 复杂，难维护 | ⭐⭐ |

### 采用方案：后端过滤

在 `updateMainTable` 方法中，过滤掉系统字段，不允许前端修改。

## 修改内容

### 后端修改

**文件：** `backend/src/main/java/com/vueadmin/service/data/PhysicalTableDataService.java`

**修改前：**
```java
private void updateMainTable(String tableName, ViewEntityDto entity, Map<String, Object> data,
                             Long recordId, String updatedBy) {
    StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
    List<Object> params = new ArrayList<>();

    for (var field : entity.getFields()) {
        if (data.containsKey(field.getFieldCode())) {
            sql.append(field.getFieldCode()).append(" = ?, ");
            params.add(data.get(field.getFieldCode()));
        }
    }

    // ... 更新逻辑
}
```

**修改后：**
```java
private void updateMainTable(String tableName, ViewEntityDto entity, Map<String, Object> data,
                             Long recordId, String updatedBy) {
    StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
    List<Object> params = new ArrayList<>();

    // 系统自动管理的字段，不允许前端修改
    Set<String> systemFields = Set.of("id", "status", "created_by", "created_at", "updated_by", "updated_at", "view_id");

    for (var field : entity.getFields()) {
        String fieldCode = field.getFieldCode();
        // 跳过系统字段
        if (systemFields.contains(fieldCode.toLowerCase())) {
            continue;
        }
        if (data.containsKey(fieldCode)) {
            sql.append(fieldCode).append(" = ?, ");
            params.add(data.get(fieldCode));
        }
    }

    // ... 更新逻辑
}
```

### 系统字段列表

以下字段由系统自动管理，不允许前端修改：

| 字段名 | 说明 | 管理方式 |
|--------|------|----------|
| `id` | 主键ID | 数据库自动生成 |
| `status` | 数据状态 | 系统自动管理（active/inactive/deleted） |
| `created_by` | 创建人 | 创建时自动设置 |
| `created_at` | 创建时间 | 创建时自动设置 |
| `updated_by` | 更新人 | 更新时自动设置 |
| `updated_at` | 更新时间 | 更新时自动设置 |
| `view_id` | 视图ID | 创建时自动设置 |

## 验证逻辑

### 插入数据（新增）

```java
// 系统字段自动设置
columns.append("status, created_by, created_at");
values.append("'active', ?, NOW()");
```

- ✅ `status` 自动设置为 'active'
- ✅ `created_by` 设置为当前用户
- ✅ `created_at` 设置为当前时间

### 更新数据（编辑）

```java
// 跳过系统字段
if (systemFields.contains(fieldCode.toLowerCase())) {
    continue;
}

// 只更新业务字段
sql.append(fieldCode).append(" = ?, ");
```

- ✅ `status` 不会被修改，保持原值
- ✅ `created_by` 不会被修改
- ✅ `created_at` 不会被修改
- ✅ `updated_by` 自动设置为当前用户
- ✅ `updated_at` 自动设置为当前时间

### 查询数据

```java
// 只查询有效数据
sql.append(" WHERE status = 'active' ORDER BY created_at DESC");
```

- ✅ 只查询 `status = 'active'` 的数据
- ✅ 编辑后仍能查询到数据

## 测试场景

### 场景1：新增数据
1. 点击"新增"
2. 填写数据
3. 保存
4. **验证：**
   - `status` = 'active'
   - 能查询到数据

### 场景2：编辑数据
1. 选择一条数据，点击"编辑"
2. 修改数据
3. 保存
4. **验证：**
   - `status` 仍为 'active'
   - 能查询到数据
   - `created_by` 和 `created_at` 未被修改
   - `updated_by` 和 `updated_at` 已更新

### 场景3：前端传递系统字段
1. 前端在编辑时传递了 `status = 'inactive'`
2. 保存
3. **验证：**
   - `status` 仍为 'active'（未被修改）
   - 能查询到数据

## 相关问题修复

### 问题1：API 路径参数缺失

**错误：** `Request method 'PUT' is not supported`

**原因：** 前端 API 调用缺少 `categoryId` 参数

**修复：**
```typescript
// 修改前
updateInstance(formId, recordId, data)

// 修改后
updateInstance(categoryId, formId, recordId, data)
```

## 注意事项

1. **系统字段保护**
   - 系统字段只能在创建时设置，更新时不能修改
   - 前端可以传递系统字段，但后端会忽略

2. **数据状态管理**
   - `status` 字段由系统管理，表示数据状态
   - 删除操作：设置 `status = 'deleted'`（软删除）
   - 查询操作：只查询 `status = 'active'` 的数据

3. **审计字段**
   - `created_by` 和 `created_at` 记录创建信息
   - `updated_by` 和 `updated_at` 记录最后更新信息
   - 这些字段用于数据审计和追溯

## 修改文件清单

- ✅ `backend/src/main/java/com/vueadmin/service/data/PhysicalTableDataService.java` - 后端数据服务
- ✅ `frontend/src/api/data/instance.ts` - 前端 API 接口
- ✅ `frontend/src/views/data/maintain/index.vue` - 前端页面
- ✅ `docs/edit-status-field-fix.md` - 本说明文档

## 相关文档

- [自动编号字段完整实现说明](./auto-number-complete-implementation.md)
- [自动编号字段验证规则调整说明](./auto-number-field-validation.md)
