import api from '../index'

// 视图分类相关接口

export interface ViewCategory {
  id?: number
  categoryCode: string
  categoryName: string
  parentId?: number
  parentName?: string
  sort?: number
  status?: string
  description?: string
  createdBy?: string
  createdAt?: string
  updatedBy?: string
  updatedAt?: string
  children?: ViewCategory[]
  hasChildren?: boolean
  viewCount?: number
}

// 获取分类树
export function getViewCategoryTree() {
  return api.get<ViewCategory[]>('/view-category/tree')
}

// 获取启用的分类树
export function getActiveViewCategoryTree() {
  return api.get<ViewCategory[]>('/view-category/active-tree')
}

// 获取所有分类（扁平列表）
export function getViewCategoryList() {
  return api.get<ViewCategory[]>('/view-category')
}

// 根据ID查询
export function getViewCategoryById(id: number) {
  return api.get<ViewCategory>(`/view-category/${id}`)
}

// 创建分类
export function createViewCategory(data: ViewCategory) {
  return api.post<ViewCategory>('/view-category', data)
}

// 更新分类
export function updateViewCategory(id: number, data: ViewCategory) {
  return api.put<ViewCategory>(`/view-category/${id}`, data)
}

// 删除分类
export function deleteViewCategory(id: number) {
  return api.delete(`/view-category/${id}`)
}

// 获取分类下的视图数量
export function getViewCategoryViewCount(id: number) {
  return api.get<number>(`/view-category/${id}/view-count`)
}
