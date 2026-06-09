#!/bin/bash
echo "测试血缘数据API..."

# 测试1：检查血缘状态
echo "=== 1. 检查血缘数据状态 ==="
curl -s http://localhost:8080/api/distribution/lineage/status | python -m json.tool

# 测试2：获取字段级血缘列表
echo -e "\n=== 2. 获取字段级血缘列表 ==="
curl -s http://localhost:8080/api/distribution/lineage/field-list | python -m json.tool

# 测试3：修复血缘数据
echo -e "\n=== 3. 修复血缘数据 ==="
curl -s -X POST http://localhost:8080/api/distribution/lineage/repair | python -m json.tool
