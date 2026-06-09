# 表单设计页面值域数据未加载问题修复

## 修改时间
2026-06-08

## 问题描述

表单名为 `FRM_VENDOR`，采购信息子表的采购组织字段在数据模型中维护了值域，但在表单管理-设计页面没有显示值域选项。

## 问题原因

### 值域数据加载机制

表单设计页面使用 `domainMap` 缓存值域数据：

```typescript
const domainMap = ref<Map<number, ValueDomain>>(new Map())

async function loadDomainData(domainId: number) {
  if (domainMap.value.has(domainId)) return
  const res = await getValueDomainById(domainId)
  domainMap.value.set(domainId, res.data)
}

function getDomainOptions(domainId?: number): DomainOption[] {
  if (!domainId) return []
  return domainMap.value.get(domainId)?.options || []
}
```

### 问题场景

**问题：** 虽然字段有 `domainId` 属性，但没有调用 `loadDomainData` 加载值域数据到缓存中。

**涉及场景：**

1. **批量添加子表字段**：点击"添加全部字段"按钮
2. **拖拽字段到子表**：从左侧字段库拖拽字段到子表
3. **加载已有表单**：编辑已保存的表单设计

### 问题表现

```
┌─────────────────────────────────┐
│ 采购组织                        │
│ ┌─────────────────────────────┐ │
│ │                              │ │ ← 应该显示下拉选项，但为空
│ └─────────────────────────────┘ │
└─────────────────────────────────┘

属性面板：
┌─────────────────────────────────┐
│ 值域: (无显示)                   │ ← 应该显示值域名称
│ 值域选项: (无显示)               │ ← 应该显示选项列表
└─────────────────────────────────┘
```

## 解决方案

在所有添加字段的场景中，都调用 `loadDomainData` 加载值域数据。

## 修改内容

**文件：** `frontend/src/views/form/design/index.vue`

### 1. 批量添加子表字段时加载值域

**场景：** 点击"添加全部字段"按钮

**修改前：**
```typescript
function handleAddSubTableWithAllFields(entity: ViewEntity) {
  // ... 添加字段逻辑
  subTables.value.push({
    entityId: entity.id!,
    entityName: entity.entityName,
    entityCode: entity.entityCode,
    fields: fields.map(f => ({
      ...f,
      componentType: getDefaultComponentType(f.fieldType, !!f.domainId),
      _uid: Date.now() + Math.random()
    }))
  })
  saveHistory()
  ElMessage.success(`已添加子表：${entity.entityName}，包含 ${fields.length} 个字段`)
}
```

**修改后：**
```typescript
function handleAddSubTableWithAllFields(entity: ViewEntity) {
  // ... 添加字段逻辑
  subTables.value.push({
    entityId: entity.id!,
    entityName: entity.entityName,
    entityCode: entity.entityCode,
    fields: fields.map(f => ({
      ...f,
      componentType: getDefaultComponentType(f.fieldType, !!f.domainId),
      _uid: Date.now() + Math.random()
    }))
  })

  // 加载所有字段的值域数据
  fields.forEach(field => {
    if (field.domainId && !domainMap.value.has(field.domainId)) {
      loadDomainData(field.domainId)
    }
  })

  saveHistory()
  ElMessage.success(`已添加子表：${entity.entityName}，包含 ${fields.length} 个字段`)
}
```

### 2. 拖拽字段到子表时加载值域

**场景：** 从左侧字段库拖拽字段到子表

**修改前：**
```typescript
function handleSubFieldDrop(event: DragEvent, subTableIndex: number) {
  // ... 添加字段逻辑
  sub.fields.push({
    ...field,
    componentType: getDefaultComponentType(field.fieldType, !!field.domainId),
    _uid: Date.now() + Math.random()
  })
  saveHistory()
  ElMessage.success(`已添加字段：${field.fieldName}`)
}
```

**修改后：**
```typescript
function handleSubFieldDrop(event: DragEvent, subTableIndex: number) {
  // ... 添加字段逻辑
  sub.fields.push({
    ...field,
    componentType: getDefaultComponentType(field.fieldType, !!field.domainId),
    _uid: Date.now() + Math.random()
  })

  // 加载值域数据
  if (field.domainId && !domainMap.value.has(field.domainId)) {
    loadDomainData(field.domainId)
  }

  saveHistory()
  ElMessage.success(`已添加字段：${field.fieldName}`)
}
```

### 3. 加载已有表单时加载值域

**场景：** 编辑已保存的表单设计

**修改前：**
```typescript
async function loadData() {
  // ... 加载表单数据

  // 初始化子表字段的控件类型
  subTables.value.forEach(sub => {
    sub.fields.forEach(field => {
      if (!field.componentType) {
        field.componentType = getDefaultComponentType(field.fieldType || 'string', !!field.domainId)
      }
    })
  })

  // 初始化历史状态
  saveHistory()
}
```

**修改后：**
```typescript
async function loadData() {
  // ... 加载表单数据

  // 初始化子表字段的控件类型并加载值域
  subTables.value.forEach(sub => {
    sub.fields.forEach(field => {
      if (!field.componentType) {
        field.componentType = getDefaultComponentType(field.fieldType || 'string', !!field.domainId)
      }
      // 加载值域数据
      if (field.domainId && !domainMap.value.has(field.domainId)) {
        loadDomainData(field.domainId)
      }
    })
  })

  // 加载主表字段的值域数据
  components.value.forEach(comp => {
    if (comp.domainId && !domainMap.value.has(comp.domainId)) {
      loadDomainData(comp.domainId)
    }
  })

  // 初始化历史状态
  saveHistory()
}
```

## 修复效果

### 修复前

```
采购组织字段：
┌─────────────────────────────────┐
│ 采购组织                        │
│ ┌─────────────────────────────┐ │
│ │                              │ │ ← 空的输入框
│ └─────────────────────────────┘ │
└─────────────────────────────────┘

属性面板：
┌─────────────────────────────────┐
│ 值域:                           │
│ 值域选项:                        │
└─────────────────────────────────┘
```

### 修复后

```
采购组织字段：
┌─────────────────────────────────┐
│ 采购组织                        │
│ ┌─────────────────────────────┐ │
│ │ 采购组织A          ▼        │ │ ← 显示下拉选项
│ └─────────────────────────────┘ │
│   采购组织A                      │
│   采购组织B                      │
│   采购组织C                      │
└─────────────────────────────────┘

属性面板：
┌─────────────────────────────────┐
│ 值域: 采购组织                   │
│ 值域选项:                        │
│   ┌──────┐ ┌──────┐ ┌──────┐   │
│   │组织A│ │组织B│ │组织C│   │
│   └──────┘ └──────┘ └──────┘   │
└─────────────────────────────────┘
```

## 数据流程

### 值域数据加载流程

```
字段添加/加载
    ↓
检查 field.domainId
    ↓
检查 domainMap.has(domainId)
    ↓
调用 loadDomainData(domainId)
    ↓
请求后端 API: getValueDomainById(domainId)
    ↓
缓存到 domainMap
    ↓
组件渲染时通过 getDomainOptions(domainId) 获取选项
```

### 值域数据缓存

**优点：**
- ✅ 避免重复请求
- ✅ 提高渲染性能
- ✅ 保持数据一致性

**机制：**
```typescript
// 检查缓存中是否存在
if (domainMap.value.has(domainId)) return

// 加载并缓存
const res = await getValueDomainById(domainId)
domainMap.value.set(domainId, res.data)
```

## 测试场景

### 场景1：新增表单并添加子表
1. 创建新表单，关联数据模型
2. 左侧展开子表实体
3. 点击"添加全部字段"
4. **验证：**
   - 子表所有字段正确显示
   - 有值域的字段显示为下拉框
   - 下拉框有正确的选项

### 场景2：拖拽字段到子表
1. 创建新表单
2. 从左侧字段库拖拽有值域的字段到子表
3. **验证：**
   - 字段正确显示
   - 值域选项正确加载
   - 属性面板显示值域信息

### 场景3：编辑已有表单
1. 打开已保存的表单设计（包含有值域的子表字段）
2. **验证：**
   - 所有字段正确显示
   - 值域数据正确加载
   - 下拉选项正确显示

### 场景4：多个字段共享同一值域
1. 添加多个使用相同值域的字段
2. **验证：**
   - 只请求一次值域数据（缓存）
   - 所有字段都正确显示选项

## 注意事项

### 1. 异步加载

值域数据是异步加载的，可能有短暂延迟：

```typescript
// 加载值域数据（异步）
if (field.domainId && !domainMap.value.has(field.domainId)) {
  loadDomainData(field.domainId) // 异步，不等待
}

// 组件立即渲染（可能此时值域还未加载完成）
// Vue 的响应式系统会在值域加载完成后自动更新视图
```

### 2. 缓存复用

同一值域在多个字段中使用时，只加载一次：

```typescript
// 第一次：加载
if (!domainMap.value.has(domainId)) {
  loadDomainData(domainId) // 发起请求
}

// 第二次：跳过（已缓存）
if (!domainMap.value.has(domainId)) {
  loadDomainData(domainId) // 直接返回，不请求
}
```

### 3. 错误处理

值域加载失败时的处理：

```typescript
async function loadDomainData(domainId: number) {
  if (domainMap.value.has(domainId)) return
  try {
    const res = await getValueDomainById(domainId)
    domainMap.value.set(domainId, res.data)
  } catch (error) {
    console.warn('加载值域数据失败', error)
    // 失败时不清空缓存，避免重复请求
  }
}
```

## 相关字段属性

### ViewField 接口

```typescript
interface ViewField {
  id: number
  fieldCode: string
  fieldName: string
  domainId?: number        // 值域ID
  domainCode?: string      // 值域编码
  domainName?: string      // 值域名称
  componentType?: string   // 控件类型
  // ... 其他属性
}
```

### 控件类型判断

```typescript
function getDefaultComponentType(fieldType: string, hasDomain: boolean): string {
  if (hasDomain) {
    return 'select' // 有值域默认为下拉框
  }
  // 根据字段类型返回对应控件
  switch (fieldType) {
    case 'number': return 'inputNumber'
    case 'date': return 'datePicker'
    case 'boolean': return 'switch'
    default: return 'input'
  }
}
```

## 修改文件清单

- ✅ `frontend/src/views/form/design/index.vue` - 表单设计页面
- ✅ `docs/form-design-domain-loading-fix.md` - 本说明文档

## 相关文档

- [表单设计器使用说明](./form-designer-guide.md)
- [值域配置说明](./value-domain-config.md)
- [子表字段配置说明](./subtable-field-config.md)
