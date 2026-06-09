# 自动编号字段验证规则调整说明

## 修改时间
2026-06-08

## 需求描述
在主数据管理-数据维护模块的添加页面中，如果字段是自动编号字段（autoNumber=true），则取消该字段的必输验证，因为编码会由系统自动生成。

## 修改内容

### 1. 主表验证规则修改

**文件：** `frontend/src/views/data/maintain/index.vue`

#### 修改点1：表单验证规则（formRules）

**修改前：**
```typescript
const formRules = computed(() => {
  const rules: Record<string, any> = {}
  formFields.value.forEach(field => {
    if (field.isRequired) {
      rules[field.fieldCode] = [
        { required: true, message: `请输入${field.fieldName}`, trigger: 'blur' }
      ]
    }
  })
  return rules
})
```

**修改后：**
```typescript
const formRules = computed(() => {
  const rules: Record<string, any> = {}
  formFields.value.forEach(field => {
    // 自动编号字段不需要必输验证
    if (field.isRequired && !field.autoNumber) {
      rules[field.fieldCode] = [
        { required: true, message: `请输入${field.fieldName}`, trigger: 'blur' }
      ]
    }
  })
  return rules
})
```

#### 修改点2：表单项必填标识

**修改前：**
```vue
<el-form-item :label="field.fieldName" :required="field.isRequired">
```

**修改后：**
```vue
<el-form-item :label="field.fieldName" :required="field.isRequired && !field.autoNumber">
```

**效果：** 自动编号字段不会显示红色星号（*）

### 2. 子表验证规则修改

**文件：** `frontend/src/views/data/maintain/index.vue`

#### 修改点3：子表数据验证函数

**修改前：**
```typescript
function validateSubTableData(): string | null {
  for (const entity of subEntities.value) {
    const fields = entity.fields?.filter(f => f.isRequired && !f.isHidden) || []
    // ... 验证逻辑
  }
}
```

**修改后：**
```typescript
function validateSubTableData(): string | null {
  for (const entity of subEntities.value) {
    // 过滤出必填且非隐藏且非自动编号的字段
    const fields = entity.fields?.filter(f => f.isRequired && !f.isHidden && !f.autoNumber) || []
    // ... 验证逻辑
  }
}
```

## 功能效果

### 新增模式
1. ✅ 自动编号字段不显示红色星号（*）
2. ✅ 自动编号字段不进行必填验证
3. ✅ 自动编号字段显示占位符"自动生成"
4. ✅ 自动编号字段被禁用，用户无法手动输入

### 编辑模式
1. ✅ 自动编号字段如果本身不是只读的，可以编辑
2. ✅ 已保存的编码值会正确显示

### 保存验证
1. ✅ 保存时不会验证自动编号字段是否为空
2. ✅ 其他必填字段正常验证

## 业务逻辑

### 为什么取消必输验证？

1. **系统生成**：自动编号字段由后端根据编码规则自动生成，用户无需手动输入
2. **用户体验**：避免用户看到不必要的错误提示
3. **数据一致性**：防止用户手动输入重复或格式错误的编码

### 后端处理

后端需要在保存数据时：
1. 检测字段是否为自动编号（autoNumber=true）
2. 根据编码规则（encodingRuleId）生成编码
3. 将生成的编码保存到数据库

## 测试建议

### 测试场景1：新增数据
1. 创建包含自动编号字段的表单
2. 点击"新增"按钮
3. **验证：**
   - 自动编号字段不显示红色星号
   - 自动编号字段显示"自动生成"占位符
   - 自动编号字段被禁用
   - 不填写其他字段时，提示其他字段必填
   - 保存成功后，检查编码是否正确生成

### 测试场景2：编辑数据
1. 选择一条已有数据，点击"编辑"
2. **验证：**
   - 已保存的编码正确显示
   - 如果字段不是只读的，可以修改编码

### 测试场景3：子表数据
1. 在子表中添加自动编号字段
2. 添加子表行
3. **验证：**
   - 子表的自动编号字段也不验证必填

## 注意事项

1. **后端配合**：后端必须实现自动编号生成逻辑，否则字段值会为空
2. **编码规则**：确保字段配置了正确的编码规则（encodingRuleId）
3. **唯一性**：后端需确保生成的编码唯一
4. **性能**：批量创建时，编码生成需要考虑性能问题

## 相关文档

- [自动编号字段控制功能说明](./auto-number-field-control.md)
- [SAP JCo 安装指南](./backend/docs/SAP-JCo-Installation-Guide.md)

## 修改文件清单

- ✅ `frontend/src/views/data/maintain/index.vue` - 主要修改文件
- ✅ `docs/auto-number-field-validation.md` - 本说明文档
