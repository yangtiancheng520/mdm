import api from '../index'

/**
 * 操作日志接口
 */

export interface OperationLog {
  id: number
  categoryId: number
  formId: number
  formName?: string
  viewId?: number
  operationType: string      // 操作类型
  operationDetail: string    // 操作详情
  fromStatus?: string        // 原状态
  toStatus?: string          // 目标状态
  mainRecordId: number
  operationData?: string     // 操作数据JSON
  qualityScore?: number      // 质检评分
  qualityIssues?: string     // 质检问题JSON
  operationReason?: string   // 操作原因
  status: string
  createdBy: string
  createdAt: string
  updatedBy?: string
  updatedAt?: string
  ipAddress?: string
  userAgent?: string
}

/**
 * 获取指定记录的操作日志
 */
export function getOperationLogs(formId: number, recordId: number) {
  return api.get<OperationLog[]>(`/data/${formId}/${recordId}/logs`)
}

/**
 * 获取操作日志列表（按分类或表单）
 */
export function getOperationLogList(params?: {
  categoryId?: number
  formId?: number
}) {
  return api.get<OperationLog[]>('/data/logs', { params })
}
