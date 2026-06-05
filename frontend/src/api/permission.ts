import api from './index'

export interface Permission {
  id: number
  name: string
  code: string
  type: 'menu' | 'button'
  parentId: number | null
  path: string
  icon: string
  sort: number
  status: 'active' | 'inactive'
  createdAt: string
}

export interface PermissionForm {
  id?: number
  name: string
  code: string
  type: 'menu' | 'button'
  parentId: number | null
  path: string
  icon: string
  sort: number
  status: 'active' | 'inactive'
}

export interface PermissionTree extends Permission {
  children: PermissionTree[]
}

// 获取权限列表
export function getPermissionList(params?: { name?: string }) {
  return api.get('/permission/list', { params })
}

// 获取权限树
export function getPermissionTree() {
  return api.get('/permission/tree')
}

// 创建权限
export function createPermission(data: PermissionForm) {
  return api.post('/permission/create', data)
}

// 更新权限
export function updatePermission(id: number, data: PermissionForm) {
  return api.put(`/permission/update/${id}`, data)
}

// 删除权限
export function deletePermission(id: number) {
  return api.delete(`/permission/delete/${id}`)
}
