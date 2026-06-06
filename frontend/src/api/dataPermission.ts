import api from './index'

export interface DataPermission {
  id: number
  roleId: number
  roleName: string
  dataType: string
  dataTypeName: string
  permissionType: string
  permissionTypeName: string
  orgIds: number[]
  customRule: string
  createdBy: string
  createdAt: string
}

export interface DataPermissionForm {
  id?: number
  roleId: number
  dataType: string
  permissionType: string
  orgIds?: number[]
  customRule?: string
  createdBy?: string
}

// 获取数据权限列表
export function getDataPermissionList(params?: { roleId?: number; dataType?: string }) {
  return api.get('/data-permission/list', { params })
}

// 获取角色的数据权限
export function getDataPermissionByRole(roleId: number) {
  return api.get(`/data-permission/role/${roleId}`)
}

// 获取数据权限详情
export function getDataPermissionById(id: number) {
  return api.get(`/data-permission/${id}`)
}

// 创建数据权限
export function createDataPermission(data: DataPermissionForm) {
  return api.post('/data-permission/create', data)
}

// 更新数据权限
export function updateDataPermission(id: number, data: DataPermissionForm) {
  return api.put(`/data-permission/update/${id}`, data)
}

// 删除数据权限
export function deleteDataPermission(id: number) {
  return api.delete(`/data-permission/delete/${id}`)
}

// 保存角色的数据权限配置
export function saveRolePermissions(roleId: number, permissions: DataPermissionForm[]) {
  return api.post(`/data-permission/role/${roleId}/save`, permissions)
}
