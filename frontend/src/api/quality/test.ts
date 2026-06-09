import api from '../index'
import type { QualityRule } from './rule'

// 测试规则
export function testRule(data: QualityRule) {
  return api.post<{ code: number; data: {
    totalRecords: number
    passCount: number
    failCount: number
    passRate: number
    failRecords: Array<{
      recordId: number
      fieldValue: string
      reason: string
    }>
    success: boolean
    error?: string
  } }>('/quality/rule/test', data)
}
