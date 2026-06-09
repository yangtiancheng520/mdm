import api from './index'

// ==================== 系统配置管理 ====================

/**
 * 获取系统配置列表
 */
export function getConfigList() {
  return api.get('/distribution/config/list')
}

/**
 * 获取系统配置详情
 */
export function getConfig(id: number) {
  return api.get(`/distribution/config/${id}`)
}

/**
 * 创建系统配置
 */
export function createConfig(data: any) {
  return api.post('/distribution/config', data)
}

/**
 * 更新系统配置
 */
export function updateConfig(id: number, data: any) {
  return api.put(`/distribution/config/${id}`, data)
}

/**
 * 删除系统配置
 */
export function deleteConfig(id: number) {
  return api.delete(`/distribution/config/${id}`)
}

/**
 * 测试连接
 */
export function testConnection(id: number) {
  return api.post(`/distribution/config/test/${id}`)
}

// ==================== 字段映射管理 ====================

/**
 * 获取字段映射列表
 */
export function getMappingList(params?: { dataType?: string; systemConfigId?: number }) {
  return api.get('/distribution/mapping/list', { params })
}

/**
 * 获取启用的字段映射
 */
export function getActiveMappings(dataType: string) {
  return api.get('/distribution/mapping/active', { params: { dataType } })
}

/**
 * 保存字段映射
 */
export function saveMappings(data: any[]) {
  return api.post('/distribution/mapping/save', data)
}

/**
 * 删除字段映射
 */
export function deleteMapping(id: number) {
  return api.delete(`/distribution/mapping/${id}`)
}

// ==================== 分发执行 ====================

/**
 * 执行分发
 */
export function distribute(dataType: string, dataId: number, systemConfigId: number, data: any) {
  return api.post('/distribution/execute', data, {
    params: { dataType, dataId, systemConfigId }
  })
}

/**
 * 批量分发
 */
export function batchDistribute(dataType: string, systemConfigId: number, dataList: any[]) {
  return api.post('/distribution/execute/batch', dataList, {
    params: { dataType, systemConfigId }
  })
}

/**
 * 重试分发
 */
export function retry(logId: number) {
  return api.post(`/distribution/retry/${logId}`)
}

// ==================== 分发日志 ====================

/**
 * 获取分发日志列表
 */
export function getLogList(params?: {
  dataType?: string
  status?: string
  systemConfigId?: number
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}) {
  return api.get('/distribution/log/list', { params })
}

/**
 * 获取分发日志详情
 */
export function getLog(id: number) {
  return api.get(`/distribution/log/${id}`)
}

/**
 * 获取数据的分发历史
 */
export function getLogHistory(dataType: string, dataId: number) {
  return api.get('/distribution/log/history', { params: { dataType, dataId } })
}

/**
 * 获取分发统计
 */
export function getLogStats(params?: { startTime?: string; endTime?: string }) {
  return api.get('/distribution/log/stats', { params })
}

// ==================== 可分发数据查询 ====================

/**
 * 获取可分发的数据列表
 */
export function getDistributableDataList(params: {
  dataType: string
  keyword?: string
  status?: string
  page?: number
  size?: number
}) {
  return api.get('/distribution/data/list', { params })
}

/**
 * 获取数据详情
 */
export function getDataDetail(dataType: string, dataId: number) {
  return api.get('/distribution/data/detail', { params: { dataType, dataId } })
}

// ==================== 血缘分析 ====================

/**
 * 获取扁平化的字段级血缘列表
 */
export function getFieldLineageFlatList(params?: {
  dataType?: string
  status?: string
  systemConfigId?: number
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}) {
  return api.get('/distribution/lineage/field-list', { params })
}

/**
 * 血缘分析搜索
 */
export function searchLineage(params?: {
  dataType?: string
  status?: string
  systemConfigId?: number
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}) {
  return api.get('/distribution/lineage/search', { params })
}

/**
 * 获取字段级血缘详情
 */
export function getFieldLineage(logId: number) {
  return api.get(`/distribution/lineage/field/${logId}`)
}

/**
 * 获取数据的完整血缘
 */
export function getDataLineage(dataType: string, dataId: number) {
  return api.get('/distribution/lineage/data', { params: { dataType, dataId } })
}

/**
 * 获取字段级血缘列表
 */
export function getFieldLineageList(logId: number) {
  return api.get(`/distribution/lineage/fields/${logId}`)
}
