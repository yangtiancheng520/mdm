# 修复失败记录的血缘数据

## ✅ 已完成的修改

### 1. DistributionService.java
**修改：** 无论分发成功或失败，都保存血缘数据
```java
// 无论成功失败，都保存字段级血缘数据（用于追踪）
try {
    lineageService.saveFieldLineage(log, sourceData, mappedData, mappings);
} catch (Exception e) {
    logger.warn("保存字段血缘失败: {}", e.getMessage());
}
```

### 2. LogDistributionRepository.java
**新增：** 查询所有没有血缘数据的日志（包括失败的）
```java
List<LogDistribution> findLogsWithoutLineage();
List<LogDistribution> findLogsWithoutLineageByType(@Param("dataType") String dataType);
```

### 3. LineageRepairController.java
**修改：** 修复API支持处理失败的记录
```java
@PostMapping("/repair")
public ApiResponse<Map<String, Object>> repairLineageData(
    @RequestParam(required = false) String dataType,
    @RequestParam(required = false, defaultValue = "true") boolean includeFailed)
```

## 🔄 重启后端服务

**在IDEA中重启Spring Boot应用**

## 🧪 修复血缘数据

重启后，在浏览器控制台执行：

```javascript
// 修复所有缺失的血缘数据（包括失败的）
fetch('/api/distribution/lineage/repair', {method: 'POST'})
  .then(r => r.json())
  .then(d => {
    console.log('修复结果:', d.data)
    if (d.data.successCount > 0) {
      alert(`成功修复 ${d.data.successCount} 条血缘数据！`)
      location.reload()
    }
  })

// 或者只修复物料类型
fetch('/api/distribution/lineage/repair?dataType=MATERIAL', {method: 'POST'})
  .then(r => r.json())
  .then(d => console.log('修复结果:', d.data))
```

## 📊 验证结果

修复后查看血缘分析页面，应该能看到：
- 失败的分发记录
- 字段级的血缘数据
- 按发送时间降序排列

## 🎯 好处

修改后的逻辑：
1. ✅ 成功的记录保存血缘数据
2. ✅ 失败的记录也保存血缘数据（用于问题追踪）
3. ✅ 可以看到失败的字段和错误原因
4. ✅ 便于问题排查和分析
