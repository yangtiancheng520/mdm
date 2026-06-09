# SAP主数据初始化报告

## 初始化概述

已成功根据SAP的客户、供应商、物料主数据重新初始化值域和字段标准。

## 初始化统计

### 值域统计
- **值域总数**: 23个
- **值域项总数**: 93个

### 字段标准统计
- **字段标准总数**: 62个
- **客户主数据字段**: 23个
- **供应商主数据字段**: 22个
- **物料主数据字段**: 17个

## 详细列表

### 一、值域列表

#### 1. 通用值域（7个）
| 值域编码 | 值域名称 | 长度 | 值域项数 |
|---------|---------|------|---------|
| GENDER | 性别 | 1 | 2 |
| COUNTRY | 国家代码 | 2 | 5 |
| PROVINCE | 省份/地区 | 2 | 6 |
| LANGUAGE | 语言 | 2 | 4 |
| CURRENCY | 货币 | 3 | 5 |
| YES_NO | 是/否 | 1 | 2 |
| STATUS | 启用状态 | 8 | 2 |

#### 2. 客户主数据值域（5个）
| 值域编码 | 值域名称 | 长度 | 值域项数 |
|---------|---------|------|---------|
| CUSTOMER_GROUP | 客户组 | 9 | 4 |
| SALES_ORG | 销售组织 | 8 | 4 |
| DISTR_CHANNEL | 分销渠道 | 2 | 3 |
| PAYMENT_TERM | 付款条款 | 5 | 5 |
| CUSTOMER_TYPE | 客户类型 | 8 | 2 |

#### 3. 供应商主数据值域（4个）
| 值域编码 | 值域名称 | 长度 | 值域项数 |
|---------|---------|------|---------|
| VENDOR_GROUP | 供应商组 | 9 | 4 |
| PURCHASE_ORG | 采购组织 | 7 | 3 |
| PURCHASE_GRP | 采购组 | 3 | 3 |
| CREDIT_LEVEL | 信用等级 | 1 | 4 |

#### 4. 物料主数据值域（4个）
| 值域编码 | 值域名称 | 长度 | 值域项数 |
|---------|---------|------|---------|
| MATERIAL_TYPE | 物料类型 | 4 | 6 |
| MATERIAL_GROUP | 物料组 | 4 | 5 |
| BASE_UNIT | 基本单位 | 3 | 6 |
| INDUSTRY | 行业分类 | 3 | 5 |

#### 5. 其他业务值域（3个）
| 值域编码 | 值域名称 | 长度 | 值域项数 |
|---------|---------|------|---------|
| SHIP_MODE | 运输方式 | 5 | 5 |
| INCOTERM | 国际贸易术语 | 3 | 4 |
| COMPANY_CODE | 公司代码 | 2 | 4 |

### 二、字段标准列表

#### 1. 客户主数据字段（23个）
| 字段编码 | 字段名称 | 字段类型 | 长度 | 关联值域 |
|---------|---------|---------|------|---------|
| CUSTOMER_CODE | 客户编码 | string | 10 | - |
| CUSTOMER_NAME | 客户名称 | string | 100 | - |
| CUSTOMER_NAME2 | 客户名称2 | string | 100 | - |
| CUSTOMER_GROUP | 客户组 | string | 9 | 客户组 |
| CUSTOMER_TYPE | 客户类型 | string | 8 | 客户类型 |
| CUSTOMER_COUNTRY | 客户国家 | string | 2 | 国家代码 |
| CUSTOMER_PROVINCE | 客户省份 | string | 2 | 省份/地区 |
| CUSTOMER_CITY | 客户城市 | string | 50 | - |
| CUSTOMER_ADDRESS | 客户地址 | string | 200 | - |
| CUSTOMER_POSTAL_CODE | 客户邮编 | string | 10 | - |
| CUSTOMER_CONTACT | 客户联系人 | string | 50 | - |
| CUSTOMER_PHONE | 客户电话 | string | 20 | - |
| CUSTOMER_EMAIL | 客户邮箱 | string | 100 | - |
| CUSTOMER_SALES_ORG | 客户销售组织 | string | 8 | 销售组织 |
| CUSTOMER_DISTR_CHANNEL | 客户分销渠道 | string | 2 | 分销渠道 |
| CUSTOMER_PAYMENT_TERM | 客户付款条款 | string | 5 | 付款条款 |
| CUSTOMER_CURRENCY | 客户货币 | string | 3 | 货币 |
| CUSTOMER_LANGUAGE | 客户语言 | string | 2 | 语言 |
| CUSTOMER_TAX_NUMBER | 客户税号 | string | 20 | - |
| CUSTOMER_BANK_ACCOUNT | 客户银行账号 | string | 30 | - |
| CUSTOMER_BANK_NAME | 客户开户银行 | string | 100 | - |
| CUSTOMER_CREDIT_LIMIT | 客户信用额度 | decimal | 15 | - |
| CUSTOMER_STATUS | 客户状态 | string | 8 | 启用状态 |

#### 2. 供应商主数据字段（22个）
| 字段编码 | 字段名称 | 字段类型 | 长度 | 关联值域 |
|---------|---------|---------|------|---------|
| VENDOR_CODE | 供应商编码 | string | 10 | - |
| VENDOR_NAME | 供应商名称 | string | 100 | - |
| VENDOR_NAME2 | 供应商名称2 | string | 100 | - |
| VENDOR_GROUP | 供应商组 | string | 9 | 供应商组 |
| VENDOR_COUNTRY | 供应商国家 | string | 2 | 国家代码 |
| VENDOR_PROVINCE | 供应商省份 | string | 2 | 省份/地区 |
| VENDOR_CITY | 供应商城市 | string | 50 | - |
| VENDOR_ADDRESS | 供应商地址 | string | 200 | - |
| VENDOR_POSTAL_CODE | 供应商邮编 | string | 10 | - |
| VENDOR_CONTACT | 供应商联系人 | string | 50 | - |
| VENDOR_PHONE | 供应商电话 | string | 20 | - |
| VENDOR_EMAIL | 供应商邮箱 | string | 100 | - |
| VENDOR_PURCHASE_ORG | 供应商采购组织 | string | 7 | 采购组织 |
| VENDOR_PURCHASE_GRP | 供应商采购组 | string | 3 | 采购组 |
| VENDOR_PAYMENT_TERM | 供应商付款条款 | string | 5 | 付款条款 |
| VENDOR_CURRENCY | 供应商货币 | string | 3 | 货币 |
| VENDOR_LANGUAGE | 供应商语言 | string | 2 | 语言 |
| VENDOR_TAX_NUMBER | 供应商税号 | string | 20 | - |
| VENDOR_BANK_ACCOUNT | 供应商银行账号 | string | 30 | - |
| VENDOR_BANK_NAME | 供应商开户银行 | string | 100 | - |
| VENDOR_CREDIT_LEVEL | 供应商信用等级 | string | 1 | 信用等级 |
| VENDOR_STATUS | 供应商状态 | string | 8 | 启用状态 |

#### 3. 物料主数据字段（17个）
| 字段编码 | 字段名称 | 字段类型 | 长度 | 关联值域 |
|---------|---------|---------|------|---------|
| MATERIAL_CODE | 物料编码 | string | 18 | - |
| MATERIAL_NAME | 物料名称 | string | 40 | - |
| MATERIAL_NAME2 | 物料名称2 | string | 40 | - |
| MATERIAL_TYPE | 物料类型 | string | 4 | 物料类型 |
| MATERIAL_GROUP | 物料组 | string | 4 | 物料组 |
| MATERIAL_BASE_UNIT | 物料基本单位 | string | 3 | 基本单位 |
| MATERIAL_INDUSTRY | 物料行业分类 | string | 3 | 行业分类 |
| MATERIAL_GROSS_WEIGHT | 物料毛重 | decimal | 13.3 | - |
| MATERIAL_NET_WEIGHT | 物料净重 | decimal | 13.3 | - |
| MATERIAL_WEIGHT_UNIT | 物料重量单位 | string | 3 | 基本单位 |
| MATERIAL_VOLUME | 物料体积 | decimal | 13.3 | - |
| MATERIAL_VOLUME_UNIT | 物料体积单位 | string | 3 | - |
| MATERIAL_SIZE | 物料尺寸 | string | 32 | - |
| MATERIAL_COLOR | 物料颜色 | string | 20 | - |
| MATERIAL_BRAND | 物料品牌 | string | 50 | - |
| MATERIAL_ORIGIN | 物料产地 | string | 50 | - |
| MATERIAL_STATUS | 物料状态 | string | 8 | 启用状态 |

## 执行日期

2026-06-08

## 备注

- 所有值域和字段标准已根据SAP主数据规范初始化
- 值域项包含编码和值，符合SAP数据规范
- 字段标准已关联对应值域，支持下拉选择
- 可在前端页面查看和维护这些数据
