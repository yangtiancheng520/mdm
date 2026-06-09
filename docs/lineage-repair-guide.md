# 血缘数据问题排查与修复指南

## 📋 问题现象

在执行批量分发后，分发日志中有数据，但在血缘分析菜单中没有更新血缘数据。

## 🔍 问题原因

血缘数据没有保存的主要原因：

1. **缺少字段映射配置** - 如果没有配置字段映射，血缘数据无法生成
2. **分发状态为失败** - 只有成功的分发才会保存血缘数据
3. **历史数据缺失** - 早期分发的日志可能没有血缘数据

## 🛠️ 解决步骤

### 第一步：执行诊断SQL

执行 `db/fix_lineage_data.sql` 中的诊断查询，检查：

```sql
-- 检查字段映射配置
SELECT data_type, COUNT(*) FROM dis_field_mapping WHERE status = 'active' GROUP BY data_type;

-- 检查分发日志状态
SELECT status, COUNT(*) FROM dis_log_distribution GROUP BY status;

-- 检查血缘数据
SELECT COUNT(*) FROM dis_field_lineage;

-- 查看最近的分发日志是否有血缘数据
SELECT
    dl.id,
    dl.log_code,
    dl.data_type,
    dl.status,
    dl.field_count,
    (SELECT COUNT(*) FROM dis_field_lineage fl WHERE fl.log_id = dl.id) as 血缘记录数
FROM dis_log_distribution dl
WHERE dl.status = 'SUCCESS'
ORDER BY dl.created_at DESC
LIMIT 10;
```

### 第二步：补充字段映射配置

执行 `db/fix_lineage_data.sql` 中的字段映射插入语句：

```sql
-- 为物料类型添加字段映射
INSERT IGNORE INTO dis_field_mapping
(data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, ...)
VALUES
('MATERIAL', '物料', 'MATNR', '物料编码', 'MATNR', '物料编号', ...),
-- 更多字段...

-- 为客户类型添加字段映射
INSERT IGNORE INTO dis_field_mapping
(data_type, data_type_name, mdm_field, mdm_field_name, sap_field, sap_field_name, ...)
VALUES
('CUSTOMER', '客户', 'PARTNER_CODE', '客户编码', 'KUNNR', '客户编号', ...),
-- 更多字段...
```

### 第三步：修复历史数据的血缘

#### 方式1：使用后端API（推荐）

启动后端服务后，调用修复接口：

```bash
# 检查血缘数据状态
curl http://localhost:8080/api/distribution/lineage/status

# 修复所有缺失的血缘数据
curl -X POST http://localhost:8080/api/distribution/lineage/repair

# 只修复指定数据类型
curl -X POST "http://localhost:8080/api/distribution/lineage/repair?dataType=MATERIAL"
```

#### 方式2：重新分发

在分发监控页面，选择数据重新执行分发，新的分发会自动生成血缘数据。

### 第四步：验证结果

```sql
-- 验证字段映射配置
SELECT data_type, COUNT(*) as 字段数量
FROM dis_field_mapping
WHERE status = 'active'
GROUP BY data_type;

-- 验证血缘数据
SELECT
    COUNT(*) as 血缘记录总数,
    COUNT(DISTINCT log_id) as 关联日志数
FROM dis_field_lineage;

-- 验证最近10条分发日志的血缘情况
SELECT
    dl.id,
    dl.log_code,
    dl.data_type,
    dl.status,
    dl.field_count,
    (SELECT COUNT(*) FROM dis_field_lineage fl WHERE fl.log_id = dl.id) as 血缘记录数
FROM dis_log_distribution dl
ORDER BY dl.created_at DESC
LIMIT 10;
```

## 📊 API接口说明

### 1. 检查血缘数据状态

**接口：** `GET /api/distribution/lineage/status`

**返回示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "successLogCount": 100,        // 成功的分发日志总数
    "logsWitLineage": 80,          // 有血缘数据的日志数
    "logsNeedRepair": 20           // 需要补充血缘的日志数
  }
}
```

### 2. 修复血缘数据

**接口：** `POST /api/distribution/lineage/repair`

**参数：**
- `dataType` (可选): 数据类型，不传则修复所有类型

**返回示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 20,                   // 需要处理的日志总数
    "successCount": 18,            // 成功处理数量
    "failCount": 2,                // 失败数量
    "message": "成功补充 18 条，失败 2 条"
  }
}
```

## ⚠️ 注意事项

1. **字段映射必须存在**
   - 每种数据类型至少需要配置一条字段映射
   - 字段映射的 `status` 必须是 `active`

2. **血缘数据生成条件**
   - 分发状态必须为 `SUCCESS`
   - 有可用的字段映射配置
   - 分发日志包含请求数据（`request_data`）

3. **性能考虑**
   - 批量修复大量历史数据时，建议按数据类型分批执行
   - 可以在系统空闲时执行修复操作

## 🔧 常见问题

### Q1: 修复后血缘数据仍然为空？

**可能原因：**
- 字段映射配置中的字段名与实际数据字段不匹配
- 原始请求数据（`request_data`）为空

**解决方法：**
```sql
-- 检查字段映射和实际数据的字段名是否匹配
SELECT * FROM dis_field_mapping WHERE data_type = 'MATERIAL' AND status = 'active';

-- 查看原始请求数据
SELECT id, request_data FROM dis_log_distribution WHERE id = {日志ID};
```

### Q2: 部分日志修复失败？

**可能原因：**
- 该数据类型没有字段映射配置
- 请求数据格式错误

**解决方法：**
查看后端日志，定位具体失败原因：
```bash
tail -f logs/spring.log | grep "处理日志.*失败"
```

### Q3: 如何预防此类问题？

1. 在系统初始化时，确保所有数据类型都配置了字段映射
2. 定期检查血缘数据生成情况
3. 建立监控告警机制，及时发现血缘数据缺失

## 📝 相关文件

- **数据库脚本：** `db/fix_lineage_data.sql`
- **修复API：** `LineageRepairController.java`
- **Repository：** `LogDistributionRepository.java` (新增方法)
- **服务类：** `LineageService.java` (saveFieldLineage方法)

## ✅ 验证清单

- [ ] 字段映射配置完整（每种数据类型都有配置）
- [ ] 字段映射状态为 active
- [ ] 分发日志中有 SUCCESS 状态的记录
- [ ] 血缘数据表中有记录
- [ ] 前端血缘分析页面能正常显示数据
