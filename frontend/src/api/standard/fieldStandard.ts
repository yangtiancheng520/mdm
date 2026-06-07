import api from '../index'

/**
 * 字段标准库接口
 */

// 字段类型
export type FieldType =
  | 'string'      // 字符
  | 'integer'     // 整型
  | 'boolean'     // 布尔
  | 'decimal'     // 浮点型
  | 'date'        // 日期
  | 'datetime'    // 日期时间
  | 'time'        // 时间
  | 'text'        // 长文本

// 字段状态
export type FieldStatus = '启用' | '停用'

// 格式类型（预设校验格式）
export type FormatType = 'none' | 'mobile' | 'email' | 'idcard' | 'custom'

// 选项配置接口
export interface FieldOption {
  label: string
  value: string
  disabled?: boolean
}

// 字段标准接口
export interface FieldStandard {
  id: number
  fieldCode: string
  fieldName: string
  fieldType: FieldType
  // 基础配置
  length: number | null
  precision: number | null
  categoryId: number | null
  categoryName?: string | null
  category: string | null
  isEnum: boolean             // 是否关联值域
  domainId: number | null     // 值域ID
  domainCode?: string | null  // 值域编码
  domainName?: string | null  // 值域名称
  status: FieldStatus
  description: string | null
  // 扩展配置
  defaultValue: string | null    // 默认值
  // 系统字段
  createdBy: string | null
  createdAt: string
  updatedBy: string | null
  updatedAt: string
}

// 字段标准表单接口
export interface FieldStandardForm {
  id?: number
  fieldCode: string
  fieldName: string
  fieldType: FieldType
  // 基础配置
  length?: number | null
  precision?: number | null
  categoryId?: number | null
  category?: string | null
  isEnum?: boolean           // 是否关联值域
  domainId?: number | null   // 值域ID
  status: FieldStatus
  description?: string | null
  // 扩展配置
  defaultValue?: string | null
}

// 字段类型选项（不分组）
export const FIELD_TYPE_OPTIONS = [
  { value: 'string', label: '字符', description: '短文本字段，存放姓名、编码、手机号等' },
  { value: 'integer', label: '整型', description: '纯整数，用于数量、年龄、序号' },
  { value: 'boolean', label: '布尔', description: '二选一状态（是/否、启用/禁用）' },
  { value: 'decimal', label: '浮点型', description: '带小数数字，金额、重量、单价' },
  { value: 'date', label: '日期', description: '仅年月日格式（yyyy-MM-dd）' },
  { value: 'datetime', label: '日期时间', description: '年月日+时分秒' },
  { value: 'time', label: '时间', description: '只存时分秒（HH:mm:ss）' },
  { value: 'text', label: '长文本', description: '超长文本，商品详情、备注等' }
]

// 根据值获取字段类型配置
export function getFieldTypeConfig(value: FieldType) {
  return FIELD_TYPE_OPTIONS.find(opt => opt.value === value)
}

// 字段状态选项
export const FIELD_STATUS_OPTIONS = [
  { label: '启用', value: '启用' },
  { label: '停用', value: '停用' }
]

// 查询参数接口
export interface FieldStandardQuery {
  fieldCode?: string
  fieldName?: string
  status?: FieldStatus | ''
  category?: string
  page?: number
  size?: number
}

// 分页响应接口
export interface FieldStandardPageResponse {
  list: FieldStandard[]
  total: number
  page: number
  pageSize: number
}

/**
 * 获取字段标准列表（分页）
 */
export function getFieldStandardList(params?: FieldStandardQuery) {
  return api.get<FieldStandardPageResponse>('/field-standard/list', { params })
}

/**
 * 获取字段标准详情
 */
export function getFieldStandardById(id: number) {
  return api.get<FieldStandard>(`/field-standard/${id}`)
}

/**
 * 创建字段标准
 */
export function createFieldStandard(data: FieldStandardForm) {
  return api.post<FieldStandard>('/field-standard', data)
}

/**
 * 更新字段标准
 */
export function updateFieldStandard(id: number, data: FieldStandardForm) {
  return api.put<FieldStandard>(`/field-standard/${id}`, data)
}

/**
 * 删除字段标准
 */
export function deleteFieldStandard(id: number) {
  return api.delete<void>(`/field-standard/${id}`)
}

/**
 * 批量删除字段标准
 */
export function batchDeleteFieldStandard(ids: number[]) {
  return api.delete<void>('/field-standard/batch', { data: { ids } })
}

/**
 * 启用字段标准
 */
export function activateFieldStandard(id: number) {
  return api.put<void>(`/field-standard/${id}/activate`)
}

/**
 * 停用字段标准
 */
export function deactivateFieldStandard(id: number) {
  return api.put<void>(`/field-standard/${id}/deactivate`)
}

/**
 * 获取所有启用的字段标准（用于下拉选择）
 */
export function getActiveFieldStandardList() {
  return api.get<FieldStandard[]>('/field-standard/active')
}
