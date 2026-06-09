import api from '../index'

// 问题类型
export interface QualityIssue {
  id?: number
  issueCode: string
  taskId?: number
  taskName?: string
  resultId?: number
  ruleId?: number
  ruleName?: string
  viewId?: number
  viewName?: string
  entityId?: number
  entityName?: string
  entityType?: 'main' | 'sub'
  tableName?: string
  recordId?: number
  mainRecordId?: number
  fieldCode?: string
  fieldName?: string
  fieldValue?: string
  issueType?: string
  issueLevel?: 'warning' | 'error' | 'critical'
  issueDesc?: string
  status: 'open' | 'processing' | 'resolved' | 'ignored' | 'closed'
  assignee?: string
  dueDate?: string
  resolvedBy?: string
  resolvedTime?: string
  resolution?: string
  createdAt?: string
  updatedAt?: string
}

// 问题项（用于分组显示）
export interface IssueItem {
  id: number
  issueCode: string
  fieldCode: string
  fieldName: string
  fieldValue: string
  fieldType: string
  domainCode?: string
  issueType: string
  issueLevel: string
  issueDesc: string
  status: string
  tableName: string
  recordId: number
  entityId: number
  entityType: string
}

// 按记录分组的问题
export interface IssueGroup {
  recordId: number
  recordName: string
  recordCode: string
  tableName: string
  viewId: number
  viewName: string
  entityType: string
  recordStatus: string
  totalIssues: number
  openIssues: number
  resolvedIssues: number
  issues: IssueItem[]
}

// 问题状态选项
export const ISSUE_STATUS_OPTIONS = [
  { value: 'open', label: '待处理' },
  { value: 'processing', label: '处理中' },
  { value: 'resolved', label: '已解决' },
  { value: 'ignored', label: '已忽略' },
  { value: 'closed', label: '已关闭' }
]

// 问题级别选项
export const ISSUE_LEVEL_OPTIONS = [
  { value: 'warning', label: '警告' },
  { value: 'error', label: '错误' },
  { value: 'critical', label: '严重' }
]

// 获取问题列表
export function getIssueList(params?: { status?: string; issueLevel?: string; viewId?: number }) {
  return api.get<{ code: number; data: QualityIssue[] }>('/quality/issue/list', { params })
}

// 获取待处理问题
export function getOpenIssues() {
  return api.get<{ code: number; data: QualityIssue[] }>('/quality/issue/open')
}

// 获取问题详情
export function getIssueById(id: number) {
  return api.get<{ code: number; data: QualityIssue }>(`/quality/issue/${id}`)
}

// 指派问题
export function assignIssue(id: number, assignee: string) {
  return api.post<{ code: number; data: QualityIssue }>(`/quality/issue/assign/${id}`, { assignee })
}

// 批量指派
export function batchAssign(ids: number[], assignee: string) {
  return api.post<{ code: number }>('/quality/issue/batch-assign', { ids, assignee })
}

// 解决问题
export function resolveIssue(id: number, resolution: string) {
  return api.post<{ code: number; data: QualityIssue }>(`/quality/issue/resolve/${id}`, { resolution })
}

// 忽略问题
export function ignoreIssue(id: number, reason: string) {
  return api.post<{ code: number; data: QualityIssue }>(`/quality/issue/ignore/${id}`, { reason })
}

// 关闭问题
export function closeIssue(id: number) {
  return api.post<{ code: number; data: QualityIssue }>(`/quality/issue/close/${id}`)
}

// 统计问题数量
export function countByStatus(status: string) {
  return api.get<{ code: number; data: number }>('/quality/issue/count', { params: { status } })
}

// 获取按记录分组的问题列表
export function getGroupedIssues(params?: { status?: string; issueLevel?: string; viewId?: number }) {
  return api.get<{ code: number; data: IssueGroup[] }>('/quality/issue/grouped', { params })
}

// 修改字段值并解决问题
export function updateFieldAndResolve(id: number, newValue: string) {
  return api.post<{ code: number; data: IssueItem }>(`/quality/issue/update-field/${id}`, { newValue })
}
