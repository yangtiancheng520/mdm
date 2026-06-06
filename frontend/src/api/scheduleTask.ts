import api from './index'

export interface ScheduleTask {
  id: number
  taskCode: string
  taskName: string
  taskType: string
  taskTypeName: string
  taskParams: string
  cronExpression: string
  cronDescription: string
  taskClass: string
  status: string
  lastExecuteTime: string
  nextExecuteTime: string
  executeCount: number
  createdBy: string
  createdAt: string
  description: string
}

export interface ScheduleTaskForm {
  id?: number
  taskCode: string
  taskName: string
  taskType: string
  taskParams?: string
  cronExpression: string
  taskClass?: string
  status?: string
  createdBy?: string
  description?: string
}

// 获取定时任务列表
export function getScheduleTaskList(params?: { taskCode?: string; taskName?: string; taskType?: string; status?: string }) {
  return api.get('/schedule-task/list', { params })
}

// 获取启用的任务
export function getActiveScheduleTasks() {
  return api.get('/schedule-task/active')
}

// 获取任务详情
export function getScheduleTaskById(id: number) {
  return api.get(`/schedule-task/${id}`)
}

// 创建定时任务
export function createScheduleTask(data: ScheduleTaskForm) {
  return api.post('/schedule-task/create', data)
}

// 更新定时任务
export function updateScheduleTask(id: number, data: ScheduleTaskForm) {
  return api.put(`/schedule-task/update/${id}`, data)
}

// 删除定时任务
export function deleteScheduleTask(id: number) {
  return api.delete(`/schedule-task/delete/${id}`)
}

// 启动任务
export function startScheduleTask(id: number) {
  return api.post(`/schedule-task/start/${id}`)
}

// 暂停任务
export function pauseScheduleTask(id: number) {
  return api.post(`/schedule-task/pause/${id}`)
}

// 立即执行
export function executeScheduleTaskNow(id: number) {
  return api.post(`/schedule-task/execute/${id}`)
}
