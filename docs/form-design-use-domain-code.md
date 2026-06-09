# 表单设计页面使用 domainCode 替代 domainId 修改说明

## 修改时间
2026-06-09

## 修改目的

在表单设计页面中，改用 `domainCode`（值域编码）替代 `domainId`（值域ID）来判断和加载值域数据。

## 修改原因

1. **更直观**：`domainCode` 是业务编码，比数字 ID 更易读
2. **更稳定**：编码不会因数据迁移而改变，ID 可能会变
3. **便于调试**：在代码和日志中看到编码比看到数字 ID 更容易理解

## 修改内容

### 1. 后端保持不变

**保留字段：**
- `std_view_field.domain_id` - 保留，用于兼容性
- `std_view_field.domain_code` - 主要使用字段
- `std_view_field.domain_name` - 显示名称

**实体类：**
```java
// backend/src/main/java/com/vueadmin/entity/standard/ViewField.java
@Column(name = "domain_id")
private Long domainId;  // 保留

@Column(name = "domain_code", length = 100)
private String domainCode;  // 主要使用

@Column(name = "domain_name", length = 200)
private String domainName;
```

### 2. 前端修改

**文件：** `frontend/src/views/form/design/index.vue`

#### 修改1：导入值域编码查询接口

```typescript
// 修改前
import { getValueDomainById, type ValueDomain, type DomainOption } from '@/api/standard/valueDomain'

// 修改后
import { getValueDomainById, getValueDomainByCode, type ValueDomain, type DomainOption } from '@/api/standard/valueDomain'
```

#### 修改2：值域缓存使用 domainCode 作为键

```typescript
// 修改前
const domainMap = ref<Map<number, ValueDomain>>(new Map())

// 修改后
const domainMap = ref<Map<string, ValueDomain>>(new Map())
```

#### 修改3：加载值域数据使用 domainCode

```typescript
// 修改前
async function loadDomainData(domainId: number) {
  if (domainMap.value.has(domainId)) return
  const res = await getValueDomainById(domainId)
  domainMap.value.set(domainId, res.data)
}

// 修改后
async function loadDomainData(domainCode: string) {
  if (!domainCode || domainMap.value.has(domainCode)) return
  const res = await getValueDomainByCode(domainCode)
  domainMap.value.set(domainCode, res.data)
}
```

#### 修改4：获取值域选项使用 domainCode

```typescript
// 修改前
function getDomainOptions(domainId?: number): DomainOption[] {
  if (!domainId) return []
  return domainMap.value.get(domainId)?.options || []
}

// 修改后
function getDomainOptions(domainCode?: string): DomainOption[] {
  if (!domainCode) return []
  return domainMap.value.get(domainCode)?.options || []
}
```

#### 修改5：所有判断和调用使用 domainCode

**判断是否有值域：**
```typescript
// 修改前
if (field.domainId && !domainMap.value.has(field.domainId)) {
  loadDomainData(field.domainId)
}

// 修改后
if (field.domainCode && !domainMap.value.has(field.domainCode)) {
  loadDomainData(field.domainCode)
}
```

**控件类型判断：**
```typescript
// 修改前
componentType: getDefaultComponentType(field.fieldType, !!field.domainId)

// 修改后
componentType: getDefaultComponentType(field.fieldType, !!field.domainCode)
```

**模板中使用：**
```vue
<!-- 修改前 -->
<el-select v-if="getComponentType(comp) === 'select'">
  <el-option v-for="opt in getDomainOptions(comp.domainId)" />
</el-select>

<!-- 修改后 -->
<el-select v-if="getComponentType(comp) === 'select'">
  <el-option v-for="opt in getDomainOptions(comp.domainCode)" />
</el-select>
```

## 批量替换命令

```bash
# 替换 domainId 为 domainCode
sed -i 's/\.domainId && domainMap\.get(.*\.domainId)/\.domainCode \&\& domainMap.get(.....domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/field\.domainId && !domainMap\.value\.has(field\.domainId)/field.domainCode \&\& !domainMap.value.has(field.domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/loadDomainData(field\.domainId)/loadDomainData(field.domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/comp\.domainId && !domainMap\.value\.has(comp\.domainId)/comp.domainCode \&\& !domainMap.value.has(comp.domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/loadDomainData(comp\.domainId)/loadDomainData(comp.domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/getDomainOptions(comp\.domainId)/getDomainOptions(comp.domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/getDomainOptions(field\.domainId)/getDomainOptions(field.domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/getComponentTypeOptions(selectedComponent\.domainId)/getComponentTypeOptions(selectedComponent.domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/getComponentTypeOptions(selectedSubField\.domainId)/getComponentTypeOptions(selectedSubField.domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/domainMap\.get(selectedComponent\.domainId)/domainMap.get(selectedComponent.domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/domainMap\.get(selectedSubField\.domainId)/domainMap.get(selectedSubField.domainCode)/g' frontend/src/views/form/design/index.vue
sed -i 's/!!field\.domainId/!!field.domainCode/g' frontend/src/views/form/design/index.vue
sed -i 's/!!comp\.domainId/!!comp.domainCode/g' frontend/src/views/form/design/index.vue
sed -i 's/!!f\.domainId/!!f.domainCode/g' frontend/src/views/form/design/index.vue
```

## 修改效果

### 修改前

**判断逻辑：**
```typescript
if (field.domainId) {
  // 使用数字ID判断
}
```

**加载值域：**
```typescript
const res = await getValueDomainById(123)  // 使用ID
```

**缓存键：**
```typescript
domainMap.set(123, domainData)  // 使用ID作为键
```

### 修改后

**判断逻辑：**
```typescript
if (field.domainCode) {
  // 使用编码判断
}
```

**加载值域：**
```typescript
const res = await getValueDomainByCode('PURCHASE_ORG')  // 使用编码
```

**缓存键：**
```typescript
domainMap.set('PURCHASE_ORG', domainData)  // 使用编码作为键
```

## 优点

### 1. 更直观

**修改前：**
```typescript
if (field.domainId === 123) {  // 123 是什么值域？
  // ...
}
```

**修改后：**
```typescript
if (field.domainCode === 'PURCHASE_ORG') {  // 清晰：采购组织
  // ...
}
```

### 2. 更稳定

- `domainId` 可能因数据迁移、环境不同而改变
- `domainCode` 是业务编码，不会改变

### 3. 便于调试

**日志输出：**

修改前：
```
加载值域: 123
```

修改后：
```
加载值域: PURCHASE_ORG
```

### 4. 代码可读性

**修改前：**
```vue
<el-option v-for="opt in getDomainOptions(123)" />
```

**修改后：**
```vue
<el-option v-for="opt in getDomainOptions('PURCHASE_ORG')" />
```

## 兼容性

### 后端兼容

- **保留 `domainId`**：数据库字段保留，用于兼容旧代码
- **主要使用 `domainCode`**：新代码优先使用编码

### 前端兼容

- **FormComponentDto**：保留 `domainId` 字段，但主要使用 `domainCode`
- **ViewField**：保留 `domainId` 字段，但主要使用 `domainCode`

## 注意事项

### 1. domainCode 必须唯一

确保 `std_value_domain.domain_code` 在数据库中是唯一的：

```sql
ALTER TABLE std_value_domain ADD UNIQUE INDEX uk_domain_code (domain_code);
```

### 2. domainCode 不能为空

如果字段有值域，`domainCode` 必须有值：

```sql
-- 检查是否有字段 domainId 有值但 domainCode 为空
SELECT * FROM std_view_field 
WHERE domain_id IS NOT NULL AND (domain_code IS NULL OR domain_code = '');
```

### 3. 数据一致性

确保 `domainCode` 和 `domainId` 对应同一个值域：

```sql
-- 检查一致性
SELECT vf.*, vd.id as vd_id, vd.domain_code as vd_code
FROM std_view_field vf
LEFT JOIN std_value_domain vd ON vf.domain_id = vd.id
WHERE vf.domain_id IS NOT NULL AND vf.domain_code != vd.domain_code;
```

## 测试场景

### 场景1：添加字段到表单
1. 从左侧字段库拖拽有值域的字段到表单
2. **验证：**
   - 使用 `domainCode` 判断是否有值域
   - 调用 `getValueDomainByCode(domainCode)` 加载值域
   - 缓存键为 `domainCode`

### 场景2：批量添加子表字段
1. 点击"添加全部字段"
2. **验证：**
   - 所有字段的值域都使用 `domainCode` 加载
   - 值域选项正确显示

### 场景3：编辑已有表单
1. 打开已保存的表单设计
2. **验证：**
   - 所有字段的值域使用 `domainCode` 加载
   - 下拉选项正确显示

### 场景4：值域缓存
1. 添加多个使用相同值域的字段
2. **验证：**
   - 只请求一次值域数据（缓存）
   - 缓存键为 `domainCode`

## 相关接口

### 后端接口

```java
// 根据 ID 查询
GET /api/value-domain/{id}

// 根据编码查询（主要使用）
GET /api/value-domain/code/{domainCode}
```

### 前端 API

```typescript
// 根据 ID 查询
export function getValueDomainById(id: number) {
  return api.get<ValueDomain>(`/value-domain/${id}`)
}

// 根据编码查询（主要使用）
export function getValueDomainByCode(domainCode: string) {
  return api.get<ValueDomain>(`/value-domain/code/${domainCode}`)
}
```

## 修改文件清单

- ✅ `frontend/src/views/form/design/index.vue` - 表单设计页面（主要修改）
- ✅ `docs/form-design-use-domain-code.md` - 本说明文档

## 相关文档

- [表单设计值域加载问题修复](./form-design-domain-loading-fix.md)
- [值域配置说明](./value-domain-config.md)
- [表单设计器使用说明](./form-designer-guide.md)
