import api from '../index'

/**
 * 字段标准库接口
 */

// 字段类型
export type FieldType = 'string' | 'number' | 'date' | 'datetime' | 'boolean' | 'text' | 'select' | 'multi_select'

// 字段状态
export type FieldStatus = 'draft' | 'published' | 'archived'

// 字段标准接口
export interface FieldStandard {
  id: number
  fieldCode: string
  fieldName: string
  fieldType: FieldType
  length: number | null
  precision: number | null
  defaultValue: string | null
  required: boolean
  categoryId: number | null
  categoryName?: string | null
  category: string | null
  status: FieldStatus
  description: string | null
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
  length?: number | null
  precision?: number | null
  defaultValue?: string | null
  required: boolean
  categoryId?: number | null
  category?: string | null
  status: FieldStatus
  description?: string | null
}

// 字段类型选项
export const FIELD_TYPE_OPTIONS = [
  { label: '字符串', value: 'string' },
  { label: '数字', value: 'number' },
  { label: '日期', value: 'date' },
  { label: '日期时间', value: 'datetime' },
  { label: '布尔值', value: 'boolean' },
  { label: '长文本', value: 'text' },
  { label: '单选', value: 'select' },
  { label: '多选', value: 'multi_select' }
]

// 字段状态选项
export const FIELD_STATUS_OPTIONS = [
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' },
  { label: '已归档', value: 'archived' }
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
 * 发布字段标准
 */
export function publishFieldStandard(id: number) {
  return api.put<void>(`/field-standard/${id}/publish`)
}

/**
 * 归档字段标准
 */
export function archiveFieldStandard(id: number) {
  return api.put<void>(`/field-standard/${id}/archive`)
}

/**
 * 获取所有启用的字段标准（用于下拉选择）
 */
export function getActiveFieldStandardList() {
  return api.get<FieldStandard[]>('/field-standard/active')
}
