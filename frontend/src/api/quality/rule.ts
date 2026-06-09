import api from '../index'

// 质量规则类型定义
export interface QualityRule {
  id?: number
  ruleCode: string
  ruleName: string
  ruleType: string
  viewId: number
  viewName?: string
  entityId: number
  entityName?: string
  entityType: 'main' | 'sub'
  tableName: string
  fieldId?: number
  fieldCode?: string
  fieldName?: string
  ruleConfig?: string
  threshold?: number
  severity?: 'warning' | 'error' | 'critical'
  status?: 'active' | 'inactive'
  createdBy?: string
  createdAt?: string
  updatedBy?: string
  updatedAt?: string
  description?: string
}

// 规则类型选项
export const RULE_TYPES = [
  { value: 'completeness', label: '完整性' },
  { value: 'uniqueness', label: '唯一性' },
  { value: 'accuracy', label: '准确性' },
  { value: 'consistency', label: '一致性' },
  { value: 'timeliness', label: '及时性' }
]

// 严重级别选项
export const SEVERITY_OPTIONS = [
  { value: 'warning', label: '警告' },
  { value: 'error', label: '错误' },
  { value: 'critical', label: '严重' }
]

// 获取规则列表
export function getRuleList(params?: {
  viewId?: number
  entityId?: number
  ruleType?: string
  status?: string
}) {
  return api.get<{ code: number; data: QualityRule[] }>('/quality/rule/list', { params })
}

// 获取规则详情
export function getRuleById(id: number) {
  return api.get<{ code: number; data: QualityRule }>(`/quality/rule/${id}`)
}

// 创建规则
export function createRule(data: QualityRule) {
  return api.post<{ code: number; data: QualityRule }>('/quality/rule/create', data)
}

// 更新规则
export function updateRule(id: number, data: QualityRule) {
  return api.put<{ code: number; data: QualityRule }>(`/quality/rule/update/${id}`, data)
}

// 删除规则
export function deleteRule(id: number) {
  return api.delete<{ code: number }>(`/quality/rule/delete/${id}`)
}

// 批量删除规则
export function batchDeleteRules(ids: number[]) {
  return api.post<{ code: number }>('/quality/rule/batch-delete', ids)
}

// 启用/停用规则
export function toggleRuleStatus(id: number) {
  return api.post<{ code: number; data: QualityRule }>(`/quality/rule/toggle/${id}`)
}

// 获取视图实体信息（用于规则配置）
export function getViewEntities(viewId: number) {
  return api.get<{ code: number; data: any }>(`/quality/rule/view-entities/${viewId}`)
}

// 根据实体ID列表获取规则
export function getRulesByEntityIds(entityIds: number[]) {
  return api.post<{ code: number; data: QualityRule[] }>('/quality/rule/by-entities', entityIds)
}
