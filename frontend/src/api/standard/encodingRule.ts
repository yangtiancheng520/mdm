import api from '../index'

/**
 * 规则段类型
 */
export type SegmentType = 'fixed' | 'date' | 'sequence' | 'conditional' | 'field' | 'category' | 'checkDigit' | 'random' | 'expression'

/**
 * 规则段配置
 */
export interface SegmentConfig {
  type: SegmentType
  name: string
  config: Record<string, any>
}

/**
 * 全局设置
 */
export interface RuleSettings {
  resetCycle?: 'daily' | 'weekly' | 'monthly' | 'yearly' | 'never'
  scopeFields?: string[]
  maxLength?: number
  allowManual?: boolean
}

/**
 * 规则定义
 */
export interface RuleDefinition {
  segments: SegmentConfig[]
  settings?: RuleSettings
}

/**
 * 编码规则
 */
export interface EncodingRule {
  id: number
  ruleCode: string
  ruleName: string
  ruleDefinition: RuleDefinition
  scopeType?: string
  scopeConfig?: string
  status: string
  example?: string
  description?: string
  createdBy?: string
  createdAt?: string
  updatedBy?: string
  updatedAt?: string
}

/**
 * 编码规则表单
 */
export interface EncodingRuleForm {
  id?: number
  ruleCode: string
  ruleName: string
  ruleDefinition: RuleDefinition
  scopeType?: string
  scopeConfig?: string
  status?: string
  example?: string
  description?: string
  createdBy?: string
  updatedBy?: string
}

/**
 * 获取编码规则列表
 */
export function getEncodingRuleList(params?: {
  ruleCode?: string
  ruleName?: string
  status?: string
  page?: number
  size?: number
}) {
  return api.get('/encoding-rule/list', { params })
}

/**
 * 获取所有启用的规则
 */
export function getActiveEncodingRules() {
  return api.get('/encoding-rule/active')
}

/**
 * 根据ID获取规则
 */
export function getEncodingRuleById(id: number) {
  return api.get(`/encoding-rule/${id}`)
}

/**
 * 根据编码获取规则
 */
export function getEncodingRuleByCode(ruleCode: string) {
  return api.get(`/encoding-rule/code/${ruleCode}`)
}

/**
 * 创建编码规则
 */
export function createEncodingRule(data: EncodingRuleForm) {
  return api.post('/encoding-rule/create', data)
}

/**
 * 更新编码规则
 */
export function updateEncodingRule(id: number, data: EncodingRuleForm) {
  return api.put(`/encoding-rule/update/${id}`, data)
}

/**
 * 删除编码规则
 */
export function deleteEncodingRule(id: number) {
  return api.delete(`/encoding-rule/delete/${id}`)
}

/**
 * 批量删除编码规则
 */
export function batchDeleteEncodingRule(ids: number[]) {
  return api.post('/encoding-rule/batch-delete', ids)
}

/**
 * 生成编码
 */
export function generateCode(ruleCode: string, businessData?: Record<string, any>) {
  return api.post(`/encoding-rule/generate/${ruleCode}`, businessData)
}

/**
 * 预览编码
 */
export function previewCode(ruleCode: string, businessData?: Record<string, any>) {
  return api.post(`/encoding-rule/preview/${ruleCode}`, businessData)
}

/**
 * 规则段类型选项（含使用说明和示例）
 */
export const SEGMENT_TYPE_OPTIONS: {
  value: SegmentType
  label: string
  description: string
  example: string
  exampleResult: string
}[] = [
  {
    value: 'fixed',
    label: '固定值',
    description: '固定字符串，常用于前缀或分隔符',
    example: '值：SUP',
    exampleResult: 'SUP'
  },
  {
    value: 'date',
    label: '日期',
    description: '按格式输出当前日期',
    example: '格式：yyyyMMdd',
    exampleResult: '20260607'
  },
  {
    value: 'sequence',
    label: '序列号',
    description: '流水号，支持独立序列和周期重置',
    example: '长度：4，填充：0',
    exampleResult: '0001'
  },
  {
    value: 'conditional',
    label: '条件值',
    description: '根据业务字段值返回不同编码',
    example: '产成品→5，原材料→1',
    exampleResult: '5'
  },
  {
    value: 'field',
    label: '字段引用',
    description: '引用业务数据中的字段值',
    example: '引用：plantCode',
    exampleResult: 'BJ01'
  },
  {
    value: 'category',
    label: '分类编码',
    description: '分类字段值映射到编码',
    example: 'A001→01，B001→11',
    exampleResult: '01'
  }
]

/**
 * 重置周期选项
 */
export const RESET_CYCLE_OPTIONS = [
  { value: 'never', label: '从不重置' },
  { value: 'daily', label: '每日重置' },
  { value: 'weekly', label: '每周重置' },
  { value: 'monthly', label: '每月重置' },
  { value: 'yearly', label: '每年重置' }
]

/**
 * 校验位算法选项
 */
export const CHECK_DIGIT_ALGORITHM_OPTIONS = [
  { value: 'mod10', label: 'Mod10' },
  { value: 'mod11', label: 'Mod11' },
  { value: 'luhn', label: 'Luhn' },
  { value: 'iso7064', label: 'ISO 7064' }
]
