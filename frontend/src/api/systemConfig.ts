import api from './index'

export interface SystemConfig {
  id: number
  configKey: string
  configValue: string
  configType: string
  configGroup: string
  description: string
  isEncrypted: boolean
  createdBy: string
  createdAt: string
}

export interface SystemConfigForm {
  id?: number
  configKey: string
  configValue: string
  configType?: string
  configGroup?: string
  description?: string
  isEncrypted?: boolean
  createdBy?: string
}

// 获取系统配置列表
export function getSystemConfigList(params?: { configKey?: string; configGroup?: string }) {
  return api.get('/system-config/list', { params })
}

// 获取所有分组
export function getConfigGroups() {
  return api.get('/system-config/groups')
}

// 获取分组配置
export function getConfigByGroup(configGroup: string) {
  return api.get(`/system-config/group/${configGroup}`)
}

// 获取配置详情
export function getSystemConfigById(id: number) {
  return api.get(`/system-config/${id}`)
}

// 根据Key获取配置
export function getConfigByKey(configKey: string) {
  return api.get(`/system-config/key/${configKey}`)
}

// 获取所有配置
export function getAllConfigs() {
  return api.get('/system-config/all')
}

// 创建系统配置
export function createSystemConfig(data: SystemConfigForm) {
  return api.post('/system-config/create', data)
}

// 更新系统配置
export function updateSystemConfig(id: number, data: SystemConfigForm) {
  return api.put(`/system-config/update/${id}`, data)
}

// 更新配置值
export function updateConfigValue(configKey: string, configValue: string) {
  return api.put('/system-config/value', { configKey, configValue })
}

// 批量更新配置
export function batchUpdateConfigs(configs: Record<string, string>) {
  return api.post('/system-config/batch-update', configs)
}

// 删除系统配置
export function deleteSystemConfig(id: number) {
  return api.delete(`/system-config/delete/${id}`)
}

// 刷新缓存
export function refreshConfigCache() {
  return api.post('/system-config/refresh-cache')
}
