import api from './index'

// ==================== 分发主题管理 ====================

/**
 * 获取主题列表
 */
export function getTopicList(params?: {
  dataType?: string
  status?: string
  keyword?: string
  page?: number
  size?: number
}) {
  return api.get('/distribution/topic/list', { params })
}

/**
 * 获取主题详情
 */
export function getTopic(id: number) {
  return api.get(`/distribution/topic/${id}`)
}

/**
 * 创建主题
 */
export function createTopic(data: any) {
  return api.post('/distribution/topic', data)
}

/**
 * 更新主题
 */
export function updateTopic(id: number, data: any) {
  return api.put(`/distribution/topic/${id}`, data)
}

/**
 * 删除主题
 */
export function deleteTopic(id: number) {
  return api.delete(`/distribution/topic/${id}`)
}

/**
 * 切换主题状态
 */
export function toggleTopicStatus(id: number) {
  return api.post(`/distribution/topic/${id}/toggle`)
}

/**
 * 获取主题统计
 */
export function getTopicStats() {
  return api.get('/distribution/topic/stats')
}

/**
 * 获取数据类型选项
 */
export function getDataTypes() {
  return api.get('/distribution/topic/data-types')
}

// ==================== 订阅管理 ====================

/**
 * 获取主题的订阅列表
 */
export function getSubscriptions(topicId: number) {
  return api.get(`/distribution/topic/${topicId}/subscriptions`)
}

/**
 * 添加订阅
 */
export function addSubscription(topicId: number, data: any) {
  return api.post(`/distribution/topic/${topicId}/subscription`, data)
}

/**
 * 更新订阅
 */
export function updateSubscription(id: number, data: any) {
  return api.put(`/distribution/topic/subscription/${id}`, data)
}

/**
 * 删除订阅
 */
export function deleteSubscription(id: number) {
  return api.delete(`/distribution/topic/subscription/${id}`)
}
