import api from '../index'

// 数据标准视图相关接口

export interface FieldDefinition {
  fieldId: number
  fieldCode?: string
  fieldName?: string
  required: boolean
  defaultValue?: string
  sortOrder?: number
}

export interface DataStandard {
  id?: number
  standardCode: string
  standardName: string
  categoryId?: number
  categoryName?: string
  fieldsDefinition?: FieldDefinition[]
  status?: string
  version?: number
  publishTime?: string
  approvalStatus?: string
  approvalBy?: string
  approvalTime?: string
  approvalComment?: string
  createdBy?: string
  createdAt?: string
  updatedBy?: string
  updatedAt?: string
  description?: string
}

export interface DataStandardQuery {
  keyword?: string
  status?: string
  categoryId?: number
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

// 分页查询
export function getDataStandardList(params: DataStandardQuery) {
  return api({
    url: '/data-standard',
    method: 'get',
    params
  })
}

// 根据ID查询
export function getDataStandardById(id: number) {
  return api({
    url: `/data-standard/${id}`,
    method: 'get'
  })
}

// 创建数据标准
export function createDataStandard(data: DataStandard) {
  return api({
    url: '/data-standard',
    method: 'post',
    data
  })
}

// 更新数据标准
export function updateDataStandard(id: number, data: DataStandard) {
  return api({
    url: `/data-standard/${id}`,
    method: 'put',
    data
  })
}

// 删除数据标准
export function deleteDataStandard(id: number) {
  return api({
    url: `/data-standard/${id}`,
    method: 'delete'
  })
}

// 批量删除
export function batchDeleteDataStandard(ids: number[], updatedBy: string) {
  return api({
    url: '/data-standard/batch',
    method: 'delete',
    data: { ids, updatedBy }
  })
}

// 发布数据标准
export function publishDataStandard(id: number) {
  return api({
    url: `/data-standard/${id}/publish`,
    method: 'put'
  })
}

// 归档数据标准
export function archiveDataStandard(id: number) {
  return api({
    url: `/data-standard/${id}/archive`,
    method: 'put'
  })
}

// 审批数据标准
export function approveDataStandard(id: number, approvalBy: string, approved: boolean, comment?: string) {
  return api({
    url: `/data-standard/${id}/approve`,
    method: 'put',
    data: { approvalBy, approved, comment }
  })
}

// 获取统计信息
export function getDataStandardStatistics() {
  return api({
    url: '/data-standard/statistics',
    method: 'get'
  })
}
