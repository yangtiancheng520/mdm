# 自动编号字段完整实现说明

## 修改时间
2026-06-08

## 功能概述
实现主数据管理-数据维护模块中自动编号字段的完整功能，包括：
1. 前端：取消必填验证，禁用输入框，显示"自动生成"提示
2. 后端：跳过验证，自动生成编码，更新序列号表

---

## 一、前端实现

### 1. 取消必填验证

**文件：** `frontend/src/views/data/maintain/index.vue`

#### 修改点1：表单验证规则
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

#### 修改点2：子表验证规则
```typescript
function validateSubTableData(): string | null {
  for (const entity of subEntities.value) {
    // 过滤出必填且非隐藏且非自动编号的字段
    const fields = entity.fields?.filter(f => f.isRequired && !f.isHidden && !f.autoNumber) || []
    // ... 验证逻辑
  }
}
```

### 2. 禁用输入框并显示提示

#### 新增辅助函数
```typescript
// 获取字段占位符（自动编号字段显示"自动生成"）
function getFieldPlaceholder(field: ViewField): string {
  if (field.autoNumber) {
    return '自动生成'
  }
  return `请输入${field.fieldName}`
}

// 判断字段是否禁用（自动编号字段在新增时禁用）
function isFieldDisabled(field: ViewField): boolean {
  if (field.isReadonly) return true
  if (field.autoNumber && !editingInstance.value) return true
  return false
}
```

#### 应用到所有输入组件
```vue
<el-input
  v-model="formData[field.fieldCode]"
  :placeholder="getFieldPlaceholder(field)"
  :disabled="isFieldDisabled(field)"
/>
```

---

## 二、后端实现

### 1. 跳过必填验证

**文件：** `backend/src/main/java/com/vueadmin/service/data/PhysicalTableDataService.java`

```java
private void validateData(ViewEntityDto entity, Map<String, Object> data, String context) {
    for (var field : entity.getFields()) {
        // 1. 必填验证（跳过自动编号字段）
        if (Boolean.TRUE.equals(field.getIsRequired()) && !Boolean.TRUE.equals(field.getAutoNumber())) {
            if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                throw new BusinessException(context + "的「" + fieldName + "」为必填项");
            }
        }
    }
}
```

### 2. 自动生成编码

#### 注入编码生成服务
```java
private final EncodingGeneratorService encodingGeneratorService;
```

#### 插入前生成编码
```java
private Long insertMainTable(String tableName, ViewEntityDto entity, Map<String, Object> data,
                             String createdBy, Long viewId) {
    // 1. 生成自动编号字段的值
    generateAutoNumbers(entity, data);

    // 2. 插入数据...
}
```

#### 生成自动编号方法
```java
private void generateAutoNumbers(ViewEntityDto entity, Map<String, Object> data) {
    for (ViewFieldDto field : entity.getFields()) {
        if (Boolean.TRUE.equals(field.getAutoNumber()) && field.getEncodingRuleId() != null) {
            // 1. 获取编码规则
            String ruleCode = getRuleCode(field.getEncodingRuleId());

            // 2. 调用编码生成服务
            String generatedCode = encodingGeneratorService.generate(ruleCode, data);

            // 3. 设置到数据Map中
            data.put(field.getFieldCode(), generatedCode);
        }
    }
}
```

---

## 三、编码生成服务

### 核心组件

#### 1. EncodingGeneratorService（编码生成服务）
- **职责**：协调整个编码生成过程
- **流程**：
  1. 获取编码规则定义
  2. 解析规则配置
  3. 调用各个段解析器
  4. 组装最终编码

#### 2. SequenceResolver（序列号解析器）
- **职责**：管理和生成序列号
- **功能**：
  - 从 `std_encoding_sequence` 表获取序列号
  - 自动递增序列号
  - 支持按周期重置（每日/每周/每月/每年）
  - 更新表中的序列号

#### 3. std_encoding_sequence 表（序列号表）
**表结构：**
```sql
CREATE TABLE std_encoding_sequence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL,              -- 规则ID
    scope_key VARCHAR(200) NOT NULL,      -- 范围键（多维度独立序列）
    current_value BIGINT NOT NULL,        -- 当前序列值
    max_value BIGINT DEFAULT 999999,      -- 最大值
    reset_cycle VARCHAR(20),              -- 重置周期
    reset_date DATE,                      -- 上次重置日期
    updated_at DATETIME                   -- 更新时间
);
```

### 序列号生成流程

```java
@Transactional
public String resolve(Map<String, Object> config, EncodingContext context) {
    // 1. 获取或创建序列记录（加锁）
    EncodingSequence sequence = sequenceRepository
            .findByRuleIdAndScopeKey(context.getRuleId(), scopeKey)
            .orElseGet(() -> createSequence(...));

    // 2. 检查是否需要重置
    if (needReset(sequence, resetCycle)) {
        sequence.setCurrentValue(0L);
        sequence.setResetDate(LocalDate.now());
    }

    // 3. 递增序列号
    Long nextValue = sequence.getCurrentValue() + 1;
    sequence.setCurrentValue(nextValue);
    sequenceRepository.save(sequence);  // ← 更新表

    // 4. 格式化输出（如：000001）
    return formatSequence(nextValue, length, padding);
}
```

---

## 四、数据流程图

```
用户点击"新增"
    ↓
前端显示禁用的输入框（显示"自动生成"）
    ↓
用户填写其他字段，点击"保存"
    ↓
前端不验证自动编号字段
    ↓
后端接收数据
    ↓
后端跳过自动编号字段的验证
    ↓
后端调用 generateAutoNumbers()
    ↓
EncodingGeneratorService.generate()
    ↓
解析编码规则定义
    ↓
调用各个段解析器（如：前缀、日期、序列号）
    ↓
SequenceResolver.resolve()
    ↓
从 std_encoding_sequence 表获取序列号
    ↓
序列号 +1
    ↓
更新 std_encoding_sequence 表
    ↓
返回格式化的编码（如：SUP20260608000001）
    ↓
设置到数据Map中
    ↓
插入到物理表
    ↓
返回成功
```

---

## 五、测试场景

### 场景1：新增数据
1. 打开新增页面
2. **验证：**
   - 自动编号字段显示"自动生成"
   - 字段被禁用，无法输入
   - 无红色星号
3. 不填写自动编号字段
4. 点击保存
5. **验证：**
   - 保存成功
   - 自动编号字段已生成编码
   - `std_encoding_sequence` 表序列号+1

### 场景2：连续新增
1. 新增第一条数据，生成编码：SUP20260608000001
2. 新增第二条数据，生成编码：SUP20260608000002
3. **验证：** 序列号连续递增

### 场景3：按日期重置
1. 编码规则设置为每日重置
2. 2026-06-08 生成：SUP20260608000001
3. 2026-06-09 生成：SUP20260609000001
4. **验证：** 日期变化后序列号重置

---

## 六、配置示例

### 编码规则配置
```json
{
  "segments": [
    {
      "type": "constant",
      "config": {"value": "SUP"}
    },
    {
      "type": "date",
      "config": {"format": "yyyyMMdd"}
    },
    {
      "type": "sequence",
      "config": {
        "length": 6,
        "padding": "0",
        "start": 1,
        "resetCycle": "daily"
      }
    }
  ],
  "settings": {
    "maxLength": 20,
    "unique": true
  }
}
```

### 生成结果
- 第一次：SUP20260608000001
- 第二次：SUP20260608000002
- 第三次：SUP20260608000003

---

## 七、关键点总结

### ✅ 已实现功能

1. **前端**
   - ✅ 自动编号字段不验证必填
   - ✅ 自动编号字段禁用输入
   - ✅ 显示"自动生成"提示
   - ✅ 不显示红色星号

2. **后端**
   - ✅ 跳过自动编号字段的必填验证
   - ✅ 自动生成编码
   - ✅ 支持多种编码段（常量、日期、序列号、变量等）
   - ✅ 序列号自动递增
   - ✅ 支持按周期重置序列号
   - ✅ 更新 std_encoding_sequence 表

3. **数据一致性**
   - ✅ 事务保证序列号不冲突
   - ✅ 支持多维度独立序列（scope_key）
   - ✅ 序列号连续性保证

### 🎯 核心表

1. **std_encoding_rule**：编码规则定义表
2. **std_encoding_sequence**：序列号表（自动更新）
3. **主数据物理表**：存储生成的编码

### 📝 注意事项

1. **事务管理**：序列号生成必须在事务中，确保并发安全
2. **规则配置**：确保字段关联了正确的编码规则（encodingRuleId）
3. **序列号表**：`std_encoding_sequence` 表会被自动更新，无需手动维护
4. **唯一性**：后端需要确保生成的编码唯一（可在规则中配置unique=true）

---

## 八、相关文档

- [自动编号字段控制功能说明](./auto-number-field-control.md)
- [自动编号字段验证规则调整说明](./auto-number-field-validation.md)
- [编码规则API文档](./encoding-rule-api.md)

## 九、修改文件清单

- ✅ `frontend/src/views/data/maintain/index.vue` - 前端主文件
- ✅ `backend/src/main/java/com/vueadmin/service/data/PhysicalTableDataService.java` - 数据服务
- ✅ `backend/src/main/java/com/vueadmin/service/standard/encoding/EncodingGeneratorService.java` - 编码生成服务（已存在）
- ✅ `backend/src/main/java/com/vueadmin/service/standard/encoding/resolver/SequenceResolver.java` - 序列号解析器（已存在）
- ✅ `backend/src/main/java/com/vueadmin/entity/standard/EncodingSequence.java` - 序列号实体（已存在）
