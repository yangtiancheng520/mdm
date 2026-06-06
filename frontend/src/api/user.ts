import api from './index'

export interface User {
  id: number
  account: string
  name: string
  email: string
  phone: string
  avatar: string
  orgId?: number
  orgName?: string
  status: 'active' | 'inactive'
  roles: number[]
  createdAt: string
}

export interface UserForm {
  id?: number
  account: string
  password?: string
  name: string
  email: string
  phone: string
  orgId?: number | null
  status: 'active' | 'inactive'
  roles: number[]
}

// 获取用户列表
export function getUserList(params?: { account?: string; name?: string; status?: string; orgId?: number }) {
  return api.get('/user/list', { params })
}

// 创建用户
export function createUser(data: UserForm) {
  return api.post('/user/create', data)
}

// 更新用户
export function updateUser(id: number, data: UserForm) {
  return api.put(`/user/update/${id}`, data)
}

// 删除用户
export function deleteUser(id: number) {
  return api.delete(`/user/delete/${id}`)
}

// 批量删除用户
export function batchDeleteUsers(ids: number[]) {
  return api.post('/user/batch-delete', { ids })
}
