import api from './index'

/**
 * 首页监控数据接口
 */

// 监控总览数据
export interface DashboardOverview {
  // 数据字典
  fieldStandardTotal: number        // 字段标准总数
  fieldStandardActive: number       // 启用字段数
  fieldStandardDraft: number        // 草稿字段数
  categoryTotal: number            // 分类总数
  domainTotal: number               // 值域总数

  // 数据模型
  viewModelTotal: number            // 视图模型总数
  viewModelPublished: number        // 已发布视图数
  viewModelDraft: number            // 草稿视图数
  entityTotal: number                // 实体总数
  fieldTotal: number                // 字段总数

  // 表单
  formTotal: number                 // 表单总数
  formPublished: number             // 已发布表单数
  formDraft: number                 // 草稿表单数

  // 数据维护
  dataInstanceTotal: number         // 数据实例总数
  dataTodayNew: number              // 今日新增
  dataTodayUpdate: number           // 今日更新
  dataTodayDelete: number           // 今日删除

  // 质量管理
  qualityRuleTotal: number          // 质量规则总数
  qualityRuleActive: number         // 启用规则数
  qualityCheckTotal: number         // 检测任务总数
  qualityCheckToday: number         // 今日检测数
  qualityPassRate: number           // 质量通过率
  qualityIssueTotal: number         // 问题总数
  qualityIssuePending: number        // 待处理问题数

  // 数据分发
  distributionConfigTotal: number   // 分发配置数
  distributionConfigActive: number  // 活跃配置数
  distributionTaskTotal: number     // 分发任务总数
  distributionTaskToday: number    // 今日分发数
  distributionSuccessRate: number  // 分发成功率
  distributionPending: number       // 待分发数
}

// 趋势数据
export interface TrendData {
  date: string
  value: number
}

// 实时动态
export interface RealtimeActivity {
  id: number
  time: string
  module: string         // 模块名称
  action: string         // 操作描述
  type: 'success' | 'warning' | 'error' | 'info'
  user?: string
}

// 模块健康度
export interface ModuleHealth {
  module: string
  name: string
  health: number         // 0-100
  status: 'healthy' | 'warning' | 'critical'
  issues: number
  lastUpdate: string
}

// 质量问题分布
export interface QualityIssueDistribution {
  type: string
  name: string
  count: number
  percentage: number
}

// 分发状态统计
export interface DistributionStatusStats {
  status: string
  name: string
  count: number
  color: string
}

// 数据类型分布
export interface DataTypeDistribution {
  typeName: string
  count: number
  percentage: number
}

/**
 * 获取监控总览数据
 */
export function getDashboardOverview() {
  return api.get<DashboardOverview>('/dashboard/overview')
}

/**
 * 获取数据趋势
 */
export function getDataTrend(params: {
  module: 'field' | 'model' | 'form' | 'data' | 'quality' | 'distribution'
  days?: number
}) {
  return api.get<TrendData[]>('/dashboard/trend', { params })
}

/**
 * 获取实时动态
 */
export function getRealtimeActivities(limit?: number) {
  return api.get<RealtimeActivity[]>('/dashboard/activities', { params: { limit: limit || 20 } })
}

/**
 * 获取模块健康度
 */
export function getModuleHealth() {
  return api.get<ModuleHealth[]>('/dashboard/health')
}

/**
 * 获取质量问题分布
 */
export function getQualityIssueDistribution() {
  return api.get<QualityIssueDistribution[]>('/dashboard/quality/distribution')
}

/**
 * 获取分发状态统计
 */
export function getDistributionStatusStats() {
  return api.get<DistributionStatusStats[]>('/dashboard/distribution/stats')
}

/**
 * 获取数据类型分布
 */
export function getDataTypeDistribution() {
  return api.get<DataTypeDistribution[]>('/dashboard/data/distribution')
}
