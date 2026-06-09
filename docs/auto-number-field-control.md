# 自动编号字段控制功能说明

## 修改时间
2026-06-08

## 需求描述
在主数据管理-数据维护模块中，点击创建页面时，如果字段设置了自动编号（autoNumber=true），则该字段应该被禁用，不允许用户手动输入编码，并显示提示"自动生成"。

## 实现方案

### 1. 新增辅助函数

在 `frontend/src/views/data/maintain/index.vue` 中新增了两个辅助函数：

#### getFieldPlaceholder(field: ViewField)
- **功能**: 获取字段占位符文本
- **逻辑**: 如果字段是自动编号字段，返回"自动生成"；否则返回"请输入{字段名}"
- **代码**:
```typescript
function getFieldPlaceholder(field: ViewField): string {
  if (field.autoNumber) {
    return '自动生成'
  }
  return `请输入${field.fieldName}`
}
```

#### isFieldDisabled(field: ViewField)
- **功能**: 判断字段是否应该被禁用
- **逻辑**:
  1. 如果字段本身是只读的（isReadonly=true），则禁用
  2. 如果是自动编号字段（autoNumber=true）且当前为新增模式（!editingInstance.value），则禁用
- **代码**:
```typescript
function isFieldDisabled(field: ViewField): boolean {
  // 如果字段本身是只读的，则禁用
  if (field.isReadonly) {
    return true
  }
  // 如果是自动编号字段，在新增模式下禁用（不允许手动输入）
  if (field.autoNumber && !editingInstance.value) {
    return true
  }
  return false
}
```

### 2. 修改所有输入组件

对主表的所有输入组件进行了统一修改，应用自动编号字段控制：

#### 修改前
```vue
<el-input
  v-model="formData[field.fieldCode]"
  :placeholder="`请输入${field.fieldName}`"
  :disabled="field.isReadonly"
/>
```

#### 修改后
```vue
<el-input
  v-model="formData[field.fieldCode]"
  :placeholder="getFieldPlaceholder(field)"
  :disabled="isFieldDisabled(field)"
/>
```

### 3. 涉及的组件类型

已修改的组件类型包括：
- ✅ 文本输入框（el-input）
- ✅ 多行文本框（el-input type="textarea"）
- ✅ 数字输入框（el-input-number）
- ✅ 日期选择器（el-date-picker）
- ✅ 日期时间选择器（el-date-picker type="datetime"）
- ✅ 下拉选择框（el-select）
- ✅ 开关（el-switch）

### 4. ViewField 接口定义

在 `frontend/src/api/standard/viewDefinition.ts` 中，ViewField 接口已包含自动编号相关字段：

```typescript
export interface ViewField {
  // ... 其他字段
  autoNumber?: boolean           // 是否启用自动编号
  encodingRuleId?: number | null // 编码规则ID
  encodingRuleName?: string | null // 编码规则名称
  // ... 其他字段
}
```

## 功能特点

1. **新增模式**: 自动编号字段被禁用，显示"自动生成"提示，用户无法手动输入
2. **编辑模式**: 自动编号字段保持可编辑状态（如果字段本身不是只读的），允许用户修改已保存的数据
3. **用户体验**: 用户能清楚知道哪些字段会自动生成，避免手动输入错误的编码

## 后端支持

后端需要确保：
1. 字段定义中包含 `autoNumber` 属性
2. 在新增数据时，后端根据 `encodingRuleId` 自动生成编码
3. 在编辑数据时，保留原有的编码值（除非用户手动修改）

## 测试建议

1. 创建一个表单，设置某个字段为自动编号字段
2. 点击"新增"按钮，验证：
   - 该字段是否被禁用
   - placeholder 是否显示"自动生成"
3. 保存数据后，验证：
   - 后端是否正确生成了编码
4. 编辑该数据，验证：
   - 该字段是否可以编辑（如果不是只读字段）
   - 原有的编码值是否正确显示

## 注意事项

1. 自动编号字段在新增时禁用，但在编辑时可以根据业务需求决定是否禁用
2. 如果自动编号字段同时设置了 `isReadonly=true`，则无论新增还是编辑都是禁用状态
3. 子表字段暂未应用此控制，如需要可以按照相同方式修改
