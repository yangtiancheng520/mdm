# 血缘分析页面无数据问题排查指南

## 🔍 问题现象

血缘分析页面按时间排序降序，显示没有数据。

## 📋 排查步骤

### 第一步：执行数据库诊断

**方式1：使用MySQL命令行**
```bash
mysql -u root -p mdm < db/diagnose_and_fix_lineage.sql
```

**方式2：使用数据库工具**
- 打开 Navicat/DBeaver/MySQL Workbench
- 连接到 mdm 数据库
- 执行 `db/diagnose_and_fix_lineage.sql`

### 第二步：检查诊断结果

诊断脚本会输出以下关键信息：

#### ✅ 应该看到的结果

```
字段映射配置检查:
- MATERIAL: 5条以上
- CUSTOMER: 5条以上
- VENDOR: 20条左右

分发日志状态检查:
- SUCCESS: 有数量，且"有血缘数量"大于0

血缘数据检查:
- 血缘记录总数: > 0
- 关联日志数: > 0
```

#### ❌ 可能的问题

```
问题1: 成功分发但无血缘数据 - 数量 > 0
→ 需要调用API修复

问题2: 缺少字段映射的数据类型
→ 需要添加字段映射配置
```

### 第三步：修复操作

#### 修复1：补充字段映射配置

SQL脚本已自动执行以下操作：
- 激活所有字段映射
- 添加物料(MATERIAL)字段映射
- 添加客户(CUSTOMER)字段映射

#### 修复2：重启后端服务

```bash
# 重启Spring Boot应用
# 在IDEA中点击重启按钮，或使用命令行
mvn spring-boot:run
```

#### 修复3：调用修复API

**方式A：使用curl命令**
```bash
# 检查状态
curl http://localhost:8080/api/distribution/lineage/status

# 修复所有血缘数据
curl -X POST http://localhost:8080/api/distribution/lineage/repair

# 只修复指定类型
curl -X POST "http://localhost:8080/api/distribution/lineage/repair?dataType=MATERIAL"
```

**方式B：使用浏览器**
1. 打开浏览器开发者工具 (F12)
2. 切换到 Console 标签
3. 执行以下代码：

```javascript
// 检查状态
fetch('/api/distribution/lineage/status')
  .then(r => r.json())
  .then(d => console.table(d.data))

// 修复血缘数据
fetch('/api/distribution/lineage/repair', {method: 'POST'})
  .then(r => r.json())
  .then(d => {
    console.log('修复结果:', d.data)
    if (d.data.successCount > 0) {
      alert(`成功修复 ${d.data.successCount} 条血缘数据！`)
      location.reload() // 刷新页面
    }
  })
```

**方式C：使用Postman**
- GET `http://localhost:8080/api/distribution/lineage/status`
- POST `http://localhost:8080/api/distribution/lineage/repair`

### 第四步：验证结果

#### 1. 再次执行诊断SQL

```sql
-- 检查血缘数据
SELECT COUNT(*) as 血缘记录数 FROM dis_field_lineage;
SELECT COUNT(DISTINCT log_id) as 关联日志数 FROM dis_field_lineage;

-- 查看最近的血缘数据
SELECT * FROM dis_field_lineage ORDER BY created_at DESC LIMIT 10;
```

#### 2. 刷新前端页面

- 打开血缘分析页面
- 点击"查询"按钮
- 应该能看到血缘数据列表

#### 3. 检查API返回

```javascript
// 在浏览器Console执行
fetch('/api/distribution/lineage/field-list?page=0&size=20')
  .then(r => r.json())
  .then(d => {
    console.log('返回数据:', d)
    console.log('记录数:', d.data?.content?.length || 0)
  })
```

## 🚨 常见问题

### Q1: SQL脚本执行失败？

**错误：** `Access denied for user 'root'@'localhost'`

**解决：** 检查MySQL密码，修改SQL文件中的连接信息

### Q2: API返回空数据？

**检查：**
```javascript
// 查看详细错误信息
fetch('/api/distribution/lineage/field-list?page=0&size=20')
  .then(r => r.json())
  .then(d => console.log('完整响应:', JSON.stringify(d, null, 2)))
```

**可能原因：**
1. 分发日志中没有SUCCESS状态的记录
2. 字段映射配置不存在
3. 血缘数据表为空

### Q3: 修复API调用失败？

**检查后端日志：**
```bash
tail -f logs/spring.log | grep -i "lineage"
```

**常见错误：**
- `NullPointerException` - 请求数据为空
- `字段映射配置不存在` - 需要先执行SQL脚本

### Q4: 血缘数据还是不显示？

**最终检查：**
```sql
-- 确认有SUCCESS状态的分发日志
SELECT COUNT(*) FROM dis_log_distribution WHERE status = 'SUCCESS';

-- 确认有字段映射配置
SELECT data_type, COUNT(*) FROM dis_field_mapping WHERE status = 'active' GROUP BY data_type;

-- 确认有血缘数据
SELECT COUNT(*) FROM dis_field_lineage;
```

如果以上三个查询都有数据，但前端还是不显示：
1. 清除浏览器缓存 (Ctrl+Shift+R)
2. 检查前端API调用是否正确
3. 查看浏览器Network标签的请求响应

## ✅ 成功标志

当看到以下结果时，说明问题已解决：

```sql
SELECT COUNT(*) as 血缘记录数 FROM dis_field_lineage;
-- 结果: 血缘记录数 > 0

SELECT COUNT(DISTINCT log_id) as 关联日志数 FROM dis_field_lineage;
-- 结果: 关联日志数 > 0
```

前端页面应该显示：
- 数据ID、数据编码、数据名称
- 发送方字段、接收方字段
- 源值、目标值
- 字段状态、发送时间

## 📞 需要帮助？

如果按照以上步骤仍然无法解决，请提供：
1. SQL诊断脚本执行结果截图
2. API调用返回的完整JSON
3. 后端日志错误信息

---

**快速操作清单：**
1. ✅ 执行 `db/diagnose_and_fix_lineage.sql`
2. ✅ 重启后端服务
3. ✅ 调用 `POST /api/distribution/lineage/repair`
4. ✅ 刷新前端页面验证