import api from '@/api/index'

/**
 * 数据实例接口
 */

export interface DataInstanceDto {
  id: number
  categoryId: number
  formId: number
  formName: string
  viewId: number | null
  dataJson: string
  status: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
}

/**
 * 查询数据列表
 */
export function getInstanceList(params?: {
  categoryId?: number
  formId?: number
}) {
  return api.get<DataInstanceDto[]>('/data-instance/list', { params })
}

/**
 * 获取数据详情
 */
export function getInstanceById(id: number) {
  return api.get<DataInstanceDto>(`/data-instance/${id}`)
}

/**
 * 保存数据
 */
export function saveInstance(data: {
  categoryId: number
  formId: number
  data: Record<string, any>
}) {
  return api.post<DataInstanceDto>('/data-instance', data)
}

/**
 * 更新数据
 */
export function updateInstance(id: number, data: Record<string, any>) {
  return api.put<DataInstanceDto>(`/data-instance/${id}`, { data })
}

/**
 * 删除数据
 */
export function deleteInstance(id: number) {
  return api.delete<void>(`/data-instance/${id}`)
}
