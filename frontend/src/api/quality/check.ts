import api from '../index'

// 检测任务类型
export interface QualityCheckTask {
  id?: number
  taskCode: string
  taskName: string
  viewId?: number
  viewName?: string
  entityIds?: string
  ruleIds?: string
  status: 'pending' | 'running' | 'completed' | 'failed'
  totalRecords?: number
  totalRules?: number
  passCount?: number
  failCount?: number
  passRate?: number
  mainTotal?: number
  mainPass?: number
  mainFail?: number
  subTotal?: number
  subPass?: number
  subFail?: number
  startTime?: string
  endTime?: string
  durationMs?: number
  errorMessage?: string
  createdBy?: string
  createdAt?: string
  // 指定检测的记录列表
  records?: CheckRecord[]
}

// 检测记录
export interface CheckRecord {
  viewId: number
  tableName: string
  recordId: number
}

// 检测结果类型
export interface QualityCheckResult {
  id?: number
  taskId: number
  ruleId: number
  ruleName?: string
  entityId?: number
  entityName?: string
  entityType?: 'main' | 'sub'
  tableName?: string
  recordId?: number
  mainRecordId?: number
  fieldCode?: string
  fieldName?: string
  fieldValue?: string
  isPassed: number
  issueType?: string
  issueLevel?: string
  issueMessage?: string
  createdAt?: string
}

// 任务状态选项
export const TASK_STATUS_OPTIONS = [
  { value: 'pending', label: '待执行' },
  { value: 'running', label: '执行中' },
  { value: 'completed', label: '已完成' },
  { value: 'failed', label: '失败' }
]

// 获取检测任务列表
export function getTaskList(params?: { viewId?: number; status?: string }) {
  return api.get<{ code: number; data: QualityCheckTask[] }>('/quality/check/task/list', { params })
}

// 获取任务详情
export function getTaskById(id: number) {
  return api.get<{ code: number; data: QualityCheckTask }>(`/quality/check/task/${id}`)
}

// 执行检测任务
export function executeCheck(data: QualityCheckTask) {
  return api.post<{ code: number; data: QualityCheckTask }>('/quality/check/execute', data)
}

// 获取检测结果
export function getResults(taskId: number) {
  return api.get<{ code: number; data: QualityCheckResult[] }>(`/quality/check/result/${taskId}`)
}

// 获取检测失败的结果
export function getFailedResults(taskId: number) {
  return api.get<{ code: number; data: QualityCheckResult[] }>(`/quality/check/result/${taskId}/failed`)
}

// 删除检测任务
export function deleteTask(id: number) {
  return api.delete<{ code: number }>(`/quality/check/task/${id}`)
}

// 待确认记录
export interface PendingRecord {
  recordId: number
  recordCode: string
  recordName: string
  tableName: string
  totalRules: number
  passCount: number
  failCount: number
  allPassed: boolean
  currentStatus: string
}

// 获取待确认记录列表
export function getPendingRecords(taskId: number) {
  return api.get<{ code: number; data: PendingRecord[] }>(`/quality/check/task/${taskId}/pending-records`)
}

// 确认记录状态
export function confirmRecords(data: {
  recordIds: number[]
  tableName: string
  targetStatus: 'ACTIVE_QUALIFIED' | 'ACTIVE_UNQUALIFIED'
  taskId?: number
}) {
  return api.post<{ code: number }>('/quality/check/confirm', data)
}
