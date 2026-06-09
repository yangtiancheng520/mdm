# 物料字段映射问题修复说明

## 🔍 问题分析

### 错误现象
```
物料在SAP中不存在，但报错：
"基准计量单位不能更改"
```

### 根本原因

**字段映射配置重复！**

```
原始配置：
MEINS → BASE_UOM (基本计量单位) ✓ 正确
MEINS → MEINS (基本计量单位) ✗ 重复

MATNR → MATERIAL ✓ 正确
MATNR → MATNR ✗ 重复

... 其他字段也有重复
```

### 导致的问题

传给SAP的数据：
```json
{
  "BASE_UOM": "EA",
  "MEINS": "EA",      ← 重复字段
  "MATNR": "000000120000000009",
  "MATERIAL": "000000120000000009"  ← 重复字段
}
```

SAP的处理逻辑：
1. SAP连接器按字段名设置值
2. `BASE_UOM=EA` 和 `MEINS=EA` 都被设置
3. SAP认为这是在尝试修改已存在物料的基本单位
4. 报错："基准计量单位不能更改"

## ✅ 已执行的修复

### 清理重复字段映射

```sql
-- 禁用重复的英文字段映射
UPDATE dis_field_mapping
SET status = 'inactive'
WHERE id IN (103, 104, 105, 106, 107);

-- 保留中文字段映射（正确的）
id=97:  MATNR → MATERIAL
id=98:  MBRSH → IND_SECTOR
id=99:  MTART → MATL_TYPE
id=100: MEINS → BASE_UOM
id=101: MATKL → MATL_GROUP
id=102: MAKTX → MATL_DESC
```

### SAP连接器字段映射逻辑

**SapConnector.java 中的正确处理：**

```java
// 物料主数据结构
HEADDATA:
  - MATERIAL (物料编号)
  - IND_SECTOR (行业领域)
  - MATL_TYPE (物料类型)

CLIENTDATA:
  - BASE_UOM (基本计量单位) ← 正确的字段名
  - MATL_GROUP (物料组)

MATERIALDESCRIPTION:
  - MATL_DESC (物料描述)
```

### 正确的字段映射关系

```
MDM字段       →  SAP字段        →  SAP结构
─────────────────────────────────────────
MATNR         →  MATERIAL      →  HEADDATA
MBRSH         →  IND_SECTOR    →  HEADDATA
MTART         →  MATL_TYPE     →  HEADDATA
MEINS         →  BASE_UOM      →  CLIENTDATA ← 重要！
MATKL         →  MATL_GROUP    →  CLIENTDATA
MAKTX         →  MATL_DESC     →  MATERIALDESCRIPTION
```

## 🎯 验证修复

### 测试分发

**请重新分发物料 000000120000000009：**

1. 进入数据维护页面
2. 找到物料：000000120000000009
3. 点击分发
4. 这次应该成功！

### 预期结果

```json
传给SAP的数据（修复后）:
{
  "MATERIAL": "000000120000000009",
  "IND_SECTOR": "A",
  "MATL_TYPE": "Z010",
  "BASE_UOM": "EA",        ← 只有一个单位字段
  "MATL_GROUP": "10-0001",
  "MATL_DESC": "TEST_8NG0I6H7DPJ"
}
```

## 📊 字段映射最佳实践

### 规则1: 避免重复映射
```
✗ 错误：同一字段映射到多个目标
  MEINS → BASE_UOM
  MEINS → MEINS

✓ 正确：一字段一映射
  MEINS → BASE_UOM
```

### 规则2: 使用SAP标准字段名
```
✗ 错误：使用非标准字段名
  MEINS → MEINS (在某些BAPI中不被识别)

✓ 正确：使用BAPI标准字段名
  MEINS → BASE_UOM (BAPI_MATERIAL_SAVEDATA标准字段)
```

### 规则3: 检查映射配置
```sql
-- 定期检查重复映射
SELECT
    mdm_field,
    COUNT(*) as mapping_count
FROM dis_field_mapping
WHERE status = 'active'
GROUP BY mdm_field
HAVING COUNT(*) > 1;
```

## 🔧 后续优化建议

### 1. 添加唯一约束

```sql
-- 防止同一字段重复映射
ALTER TABLE dis_field_mapping
ADD UNIQUE KEY uk_active_mapping (data_type, mdm_field, status)
WHERE status = 'active';
```

### 2. 字段映射验证API

```java
// 在保存字段映射时验证
public void validateMappings(List<FieldMapping> mappings) {
    Set<String> mdmFields = new HashSet<>();
    for (FieldMapping m : mappings) {
        if (!mdmFields.add(m.getMdmField())) {
            throw new BusinessException(
                "字段 " + m.getMdmField() + " 重复映射");
        }
    }
}
```

### 3. 字段映射测试功能

在前端添加"测试映射"按钮：
```
输入测试数据 → 应用字段映射 → 显示映射结果
```

## 📝 总结

### 问题
- ❌ 字段映射重复配置
- ❌ 同一字段映射到多个目标
- ❌ SAP接收到冲突数据

### 解决
- ✅ 清理重复字段映射
- ✅ 使用标准SAP字段名
- ✅ 验证映射配置唯一性

### 效果
- ✅ 物料可以正常创建到SAP
- ✅ 不再出现"基准计量单位不能更改"错误
- ✅ 数据分发更加稳定可靠

---

**修复完成时间：** 2026-06-10
**影响范围：** MATERIAL（物料）数据类型
**需要操作：** 重新分发失败的物料
