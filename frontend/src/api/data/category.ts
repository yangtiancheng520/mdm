import api from '@/api/index'

/**
 * 表单视图映射接口
 */

export interface DataCategoryDto {
  id: number
  parentId: number | null
  name: string
  type: 'folder' | 'form'
  formId: number | null
  formName: string | null
  icon: string | null
  sort: number
  children?: DataCategoryDto[]
}

/**
 * 获取分类树
 */
export function getCategoryTree() {
  return api.get<DataCategoryDto[]>('/data-category/tree')
}

/**
 * 创建文件夹
 */
export function createFolder(data: {
  parentId?: number
  name: string
  icon?: string
}) {
  return api.post<DataCategoryDto>('/data-category/folder', data)
}

/**
 * 添加表单
 */
export function addForm(data: {
  parentId?: number
  formId: number
}) {
  return api.post<DataCategoryDto>('/data-category/form', data)
}

/**
 * 更新分类
 */
export function updateCategory(id: number, data: {
  name: string
  icon?: string
}) {
  return api.put<DataCategoryDto>(`/data-category/${id}`, data)
}

/**
 * 删除分类
 */
export function deleteCategory(id: number) {
  return api.delete<void>(`/data-category/${id}`)
}

/**
 * 更新排序
 */
export function updateSort(data: { id: number; parentId: number | null; sort: number }[]) {
  return api.put<void>('/data-category/sort', data)
}
