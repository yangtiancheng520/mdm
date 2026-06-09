import api from '@/api/index'

/**
 * 数据维护接口
 * 从物理表读写数据
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
 * 查询数据列表（从物理表）
 */
export function getInstanceList(params?: {
  categoryId?: number
  formId?: number
  viewId?: number
  status?: string
}) {
  return api.get<Map<string, any>[]>('/data/list', { params })
}

/**
 * 获取数据详情（从物理表）
 */
export function getInstanceById(formId: number, recordId: number) {
  return api.get<Map<string, any>>(`/data/${formId}/${recordId}`)
}

/**
 * 保存数据（到物理表）
 */
export function saveInstance(data: {
  categoryId: number
  formId: number
  data: Record<string, any>
}) {
  return api.post<number>('/data', data)  // 返回主表记录ID
}

/**
 * 更新数据（到物理表）
 */
export function updateInstance(categoryId: number, formId: number, recordId: number, data: Record<string, any>) {
  return api.put<void>(`/data/${categoryId}/${formId}/${recordId}`, { data })
}

/**
 * 删除数据（软删除）
 */
export function deleteInstance(categoryId: number, formId: number, recordId: number) {
  return api.delete<void>(`/data/${categoryId}/${formId}/${recordId}`)
}
