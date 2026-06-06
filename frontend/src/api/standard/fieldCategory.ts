import api from '../index'

/**
 * 字段分类接口
 */

// 字段分类类型
export interface FieldCategory {
  id: number
  categoryCode: string
  categoryName: string
  parentId: number | null
  sort: number
  status: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
  description: string
  children?: FieldCategory[]
  hasChildren?: boolean
  label?: string
  value?: number
}

// 字段分类表单类型
export interface FieldCategoryForm {
  id?: number
  categoryCode: string
  categoryName: string
  parentId: number | null
  sort: number
  status: string
  description: string
  createdBy?: string
  updatedBy?: string
}

/**
 * 获取分类树
 */
export function getCategoryTree() {
  return api.get<FieldCategory[]>('/standard/field-category/tree')
}

/**
 * 获取启用的分类树
 */
export function getActiveCategoryTree() {
  return api.get<FieldCategory[]>('/standard/field-category/active-tree')
}

/**
 * 获取分类列表
 */
export function getCategoryList() {
  return api.get<FieldCategory[]>('/standard/field-category/list')
}

/**
 * 获取分类详情
 */
export function getCategoryById(id: number) {
  return api.get<FieldCategory>(`/standard/field-category/${id}`)
}

/**
 * 获取子分类
 */
export function getCategoryChildren(parentId: number) {
  return api.get<FieldCategory[]>(`/standard/field-category/children/${parentId}`)
}

/**
 * 创建分类
 */
export function createCategory(data: FieldCategoryForm) {
  return api.post<FieldCategory>('/standard/field-category', data)
}

/**
 * 更新分类
 */
export function updateCategory(id: number, data: FieldCategoryForm) {
  return api.put<FieldCategory>(`/standard/field-category/${id}`, data)
}

/**
 * 删除分类
 */
export function deleteCategory(id: number) {
  return api.delete<void>(`/standard/field-category/${id}`)
}

/**
 * 批量删除分类
 */
export function batchDeleteCategory(ids: number[]) {
  return api.post<void>('/standard/field-category/batch-delete', ids)
}

/**
 * 获取分类下的字段数量
 */
export function getFieldCountByCategory(id: number) {
  return api.get<number>(`/standard/field-category/field-count/${id}`)
}
