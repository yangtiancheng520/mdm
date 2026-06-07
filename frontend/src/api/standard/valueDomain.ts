import api from '../index'

/**
 * 值域管理接口
 */

// 存储类型
export type DataType = 'string' | 'integer' | 'decimal' | 'boolean'

// 值域状态
export type DomainStatus = '启用' | '停用'

// 值域选项
export interface DomainOption {
  value: string    // 值
  label: string    // 显示名
  sort: number     // 排序
}

// 值域接口
export interface ValueDomain {
  id: number
  domainCode: string       // 值域编码
  domainName: string       // 值域名称
  dataType: DataType       // 存储类型
  dataLength: number | null // 长度
  options: DomainOption[]  // 选项列表
  status: DomainStatus     // 状态
  description: string | null
  createdBy: string | null
  createdAt: string
  updatedBy: string | null
  updatedAt: string
}

// 值域表单接口
export interface ValueDomainForm {
  id?: number
  domainCode: string
  domainName: string
  dataType: DataType
  dataLength: number | null
  options: DomainOption[]
  status: DomainStatus
  description?: string | null
}

// 存储类型选项（与数据字典一致）
export const DATA_TYPE_OPTIONS = [
  { label: '字符', value: 'string' },
  { label: '整型', value: 'integer' },
  { label: '浮点型', value: 'decimal' },
  { label: '布尔', value: 'boolean' }
]

// 状态选项
export const DOMAIN_STATUS_OPTIONS = [
  { label: '启用', value: '启用' },
  { label: '停用', value: '停用' }
]

// 查询参数接口
export interface ValueDomainQuery {
  domainCode?: string
  domainName?: string
  dataType?: DataType | ''
  status?: DomainStatus | ''
  page?: number
  size?: number
}

// 分页响应接口
export interface ValueDomainPageResponse {
  list: ValueDomain[]
  total: number
  page: number
  pageSize: number
}

/**
 * 获取值域列表（分页）
 */
export function getValueDomainList(params?: ValueDomainQuery) {
  return api.get<ValueDomainPageResponse>('/value-domain/list', { params })
}

/**
 * 获取值域详情
 */
export function getValueDomainById(id: number) {
  return api.get<ValueDomain>(`/value-domain/${id}`)
}

/**
 * 根据编码获取值域
 */
export function getValueDomainByCode(domainCode: string) {
  return api.get<ValueDomain>(`/value-domain/code/${domainCode}`)
}

/**
 * 创建值域
 */
export function createValueDomain(data: ValueDomainForm) {
  return api.post<ValueDomain>('/value-domain', data)
}

/**
 * 更新值域
 */
export function updateValueDomain(id: number, data: ValueDomainForm) {
  return api.put<ValueDomain>(`/value-domain/${id}`, data)
}

/**
 * 删除值域
 */
export function deleteValueDomain(id: number) {
  return api.delete<void>(`/value-domain/${id}`)
}

/**
 * 批量删除值域
 */
export function batchDeleteValueDomain(ids: number[]) {
  return api.delete<void>('/value-domain/batch', { data: { ids } })
}

/**
 * 获取所有启用的值域（用于下拉选择）
 */
export function getActiveValueDomainList() {
  return api.get<ValueDomain[]>('/value-domain/active')
}

/**
 * 更新值域项
 */
export function updateValueDomainOptions(id: number, options: DomainOption[]) {
  return api.put<void>(`/value-domain/${id}/options`, options)
}

/**
 * 校验选项值是否符合类型和长度要求
 */
export function validateOptionValue(value: string, dataType: DataType, dataLength: number | null): { valid: boolean; message: string } {
  if (!value) {
    return { valid: false, message: '值不能为空' }
  }

  switch (dataType) {
    case 'string':
      // 字符类型：校验长度不超过最大长度
      if (dataLength && value.length > dataLength) {
        return { valid: false, message: `值长度不能超过${dataLength}个字符` }
      }
      return { valid: true, message: '' }

    case 'integer':
      // 整型：校验是否为整数
      if (!/^-?\d+$/.test(value)) {
        return { valid: false, message: '值必须是整数' }
      }
      return { valid: true, message: '' }

    case 'decimal':
      // 浮点型：校验是否为数字
      if (!/^-?\d+(\.\d+)?$/.test(value)) {
        return { valid: false, message: '值必须是数字' }
      }
      return { valid: true, message: '' }

    case 'boolean':
      // 布尔：校验是否为 true/false 或 是/否
      const validBoolean = ['true', 'false', '是', '否', '1', '0'].includes(value.toLowerCase())
      if (!validBoolean) {
        return { valid: false, message: '值必须是 true/false 或 是/否' }
      }
      return { valid: true, message: '' }

    default:
      return { valid: true, message: '' }
  }
}
