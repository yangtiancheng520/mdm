# 数据字典分类初始化报告

## 初始化概述

已成功将数据字典的字段分类初始化，关联到字典分类表。

## 初始化统计

- ✅ **已更新字段**: 62个
- ✅ **涉及分类**: 2个（物料属性、合作伙伴）

## 字典分类表

系统中定义了以下字典分类：

| 分类编码 | 分类名称 | 字段数量 |
|---------|---------|---------|
| BASIC | 基本信息 | 0 |
| SALES | 销售信息 | 0 |
| PURCHASE | 采购信息 | 0 |
| FINANCE | 财务信息 | 0 |
| LOGISTICS | 物流信息 | 0 |
| MATERIAL | 物料属性 | 17 |
| PARTNER | 合作伙伴 | 45 |

## 字段分类详情

### 1. 物料属性（17个字段）

所有 `MATERIAL_*` 前缀的字段：

| 字段编码 | 字段名称 |
|---------|---------|
| MATERIAL_BASE_UNIT | 物料基本单位 |
| MATERIAL_BRAND | 物料品牌 |
| MATERIAL_CODE | 物料编码 |
| MATERIAL_COLOR | 物料颜色 |
| MATERIAL_GROSS_WEIGHT | 物料毛重 |
| MATERIAL_GROUP | 物料组 |
| MATERIAL_INDUSTRY | 物料行业分类 |
| MATERIAL_NAME | 物料名称 |
| MATERIAL_NAME2 | 物料名称2 |
| MATERIAL_NET_WEIGHT | 物料净重 |
| MATERIAL_ORIGIN | 物料产地 |
| MATERIAL_SIZE | 物料尺寸 |
| MATERIAL_STATUS | 物料状态 |
| MATERIAL_TYPE | 物料类型 |
| MATERIAL_VOLUME | 物料体积 |
| MATERIAL_VOLUME_UNIT | 物料体积单位 |
| MATERIAL_WEIGHT_UNIT | 物料重量单位 |

### 2. 合作伙伴（45个字段）

#### 客户主数据字段（23个）

所有 `CUSTOMER_*` 前缀的字段：

| 字段编码 | 字段名称 |
|---------|---------|
| CUSTOMER_ADDRESS | 客户地址 |
| CUSTOMER_BANK_ACCOUNT | 客户银行账号 |
| CUSTOMER_BANK_NAME | 客户开户银行 |
| CUSTOMER_CITY | 客户城市 |
| CUSTOMER_CODE | 客户编码 |
| CUSTOMER_CONTACT | 客户联系人 |
| CUSTOMER_COUNTRY | 客户国家 |
| CUSTOMER_CREDIT_LIMIT | 客户信用额度 |
| CUSTOMER_CURRENCY | 客户货币 |
| CUSTOMER_DISTR_CHANNEL | 客户分销渠道 |
| CUSTOMER_EMAIL | 客户邮箱 |
| CUSTOMER_GROUP | 客户组 |
| CUSTOMER_LANGUAGE | 客户语言 |
| CUSTOMER_NAME | 客户名称 |
| CUSTOMER_NAME2 | 客户名称2 |
| CUSTOMER_PAYMENT_TERM | 客户付款条款 |
| CUSTOMER_PHONE | 客户电话 |
| CUSTOMER_POSTAL_CODE | 客户邮编 |
| CUSTOMER_PROVINCE | 客户省份 |
| CUSTOMER_SALES_ORG | 客户销售组织 |
| CUSTOMER_STATUS | 客户状态 |
| CUSTOMER_TAX_NUMBER | 客户税号 |
| CUSTOMER_TYPE | 客户类型 |

#### 供应商主数据字段（22个）

所有 `VENDOR_*` 前缀的字段：

| 字段编码 | 字段名称 |
|---------|---------|
| VENDOR_ADDRESS | 供应商地址 |
| VENDOR_BANK_ACCOUNT | 供应商银行账号 |
| VENDOR_BANK_NAME | 供应商开户银行 |
| VENDOR_CITY | 供应商城市 |
| VENDOR_CODE | 供应商编码 |
| VENDOR_CONTACT | 供应商联系人 |
| VENDOR_COUNTRY | 供应商国家 |
| VENDOR_CREDIT_LEVEL | 供应商信用等级 |
| VENDOR_CURRENCY | 供应商货币 |
| VENDOR_EMAIL | 供应商邮箱 |
| VENDOR_GROUP | 供应商组 |
| VENDOR_LANGUAGE | 供应商语言 |
| VENDOR_NAME | 供应商名称 |
| VENDOR_NAME2 | 供应商名称2 |
| VENDOR_PAYMENT_TERM | 供应商付款条款 |
| VENDOR_PHONE | 供应商电话 |
| VENDOR_POSTAL_CODE | 供应商邮编 |
| VENDOR_PROVINCE | 供应商省份 |
| VENDOR_PURCHASE_GRP | 供应商采购组 |
| VENDOR_PURCHASE_ORG | 供应商采购组织 |
| VENDOR_STATUS | 供应商状态 |
| VENDOR_TAX_NUMBER | 供应商税号 |

## 分类规则

根据字段编码前缀自动关联分类：

1. **MATERIAL_*** → 物料属性
2. **CUSTOMER_*** → 合作伙伴
3. **VENDOR_*** → 合作伙伴

## 执行日期

2026-06-08

## 备注

- 所有字段已成功关联到对应的字典分类
- 可在前端数据字典页面查看和筛选分类
- 后续新增字段可按照相同规则设置分类
