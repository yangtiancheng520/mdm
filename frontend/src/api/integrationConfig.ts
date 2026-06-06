import api from './index'

export interface IntegrationConfig {
  id: number
  integrationCode: string
  integrationName: string
  integrationType: string
  integrationTypeName: string
  apiEndpoint: string
  authType: string
  authTypeName: string
  authConfig: string
  requestConfig: string
  mappingConfig: string
  status: string
  createdBy: string
  createdAt: string
  description: string
}

export interface IntegrationConfigForm {
  id?: number
  integrationCode: string
  integrationName: string
  integrationType: string
  apiEndpoint: string
  authType?: string
  authConfig?: string
  requestConfig?: string
  mappingConfig?: string
  status?: string
  createdBy?: string
  description?: string
}

// 获取集成配置列表
export function getIntegrationConfigList(params?: { integrationCode?: string; integrationName?: string; integrationType?: string; status?: string }) {
  return api.get('/integration/list', { params })
}

// 获取启用的配置
export function getActiveIntegrationConfigs() {
  return api.get('/integration/active')
}

// 根据类型获取配置
export function getIntegrationConfigByType(integrationType: string) {
  return api.get(`/integration/type/${integrationType}`)
}

// 获取配置详情
export function getIntegrationConfigById(id: number) {
  return api.get(`/integration/${id}`)
}

// 根据编码获取配置
export function getIntegrationConfigByCode(integrationCode: string) {
  return api.get(`/integration/code/${integrationCode}`)
}

// 创建集成配置
export function createIntegrationConfig(data: IntegrationConfigForm) {
  return api.post('/integration/create', data)
}

// 更新集成配置
export function updateIntegrationConfig(id: number, data: IntegrationConfigForm) {
  return api.put(`/integration/update/${id}`, data)
}

// 删除集成配置
export function deleteIntegrationConfig(id: number) {
  return api.delete(`/integration/delete/${id}`)
}

// 测试连接
export function testIntegrationConnection(id: number) {
  return api.post(`/integration/test/${id}`)
}

// 调用API
export function callIntegrationApi(integrationCode: string, params?: Record<string, any>) {
  return api.post(`/integration/call/${integrationCode}`, params)
}
