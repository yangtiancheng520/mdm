import api from './index'

export interface Role {
  id: number
  name: string
  code: string
  description: string
  permissions: number[]
  status: 'active' | 'inactive'
  createdAt: string
}

export interface RoleForm {
  id?: number
  name: string
  code: string
  description: string
  permissions: number[]
  status: 'active' | 'inactive'
}

// 获取角色列表
export function getRoleList(params?: { name?: string }) {
  return api.get('/role/list', { params })
}

// 创建角色
export function createRole(data: RoleForm) {
  return api.post('/role/create', data)
}

// 更新角色
export function updateRole(id: number, data: RoleForm) {
  return api.put(`/role/update/${id}`, data)
}

// 删除角色
export function deleteRole(id: number) {
  return api.delete(`/role/delete/${id}`)
}

// 获取所有角色（下拉用）
export function getAllRoles() {
  return api.get('/role/all')
}
