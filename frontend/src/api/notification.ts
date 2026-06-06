import api from './index'

export interface Notification {
  id: number
  notificationCode: string
  notificationType: string
  notificationTypeName: string
  title: string
  content: string
  sender: string
  receiver: string
  isRead: boolean
  readTime: string
  linkUrl: string
  linkParams: Record<string, any>
  priority: number
  priorityName: string
  status: string
  createdAt: string
}

export interface NotificationForm {
  notificationType: string
  title: string
  content: string
  sender?: string
  receiver: string
  linkUrl?: string
  linkParams?: Record<string, any>
  priority?: number
  expireTime?: string
}

// 获取消息列表
export function getNotificationList(params: { receiver: string; notificationType?: string; isRead?: boolean }) {
  return api.get('/notification/list', { params })
}

// 获取未读消息数量
export function getUnreadCount(receiver: string) {
  return api.get('/notification/unread-count', { params: { receiver } })
}

// 获取消息详情
export function getNotificationById(id: number) {
  return api.get(`/notification/${id}`)
}

// 发送消息
export function sendNotification(data: NotificationForm) {
  return api.post('/notification/send', data)
}

// 发送系统通知给所有用户
export function sendNotificationToAll(title: string, content: string, sender: string) {
  return api.post('/notification/send-all', { title, content, sender })
}

// 标记为已读
export function markNotificationAsRead(id: number) {
  return api.post(`/notification/read/${id}`)
}

// 标记所有为已读
export function markAllNotificationsAsRead(receiver: string) {
  return api.post('/notification/read-all', null, { params: { receiver } })
}

// 删除消息
export function deleteNotification(id: number) {
  return api.delete(`/notification/delete/${id}`)
}

// 清理过期消息
export function cleanExpiredNotifications() {
  return api.post('/notification/clean-expired')
}
