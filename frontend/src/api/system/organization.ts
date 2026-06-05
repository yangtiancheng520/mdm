import api from '../index'

/**
 * 组织机构接口
 */

// 组织机构类型
export interface Organization {
  id: number
  orgCode: string
  orgName: string
  orgType: string
  parentId: number | null
  parentName?: string
  level: number
  path: string
  manager: string
  phone: string
  email: string
  sort: number
  status: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
  description: string
  children?: Organization[]
  hasChildren?: boolean
  label?: string
  value?: number
}

// 组织表单类型
export interface OrganizationForm {
  id?: number
  orgCode: string
  orgName: string
  orgType: string
  parentId: number | null
  manager: string
  phone: string
  email: string
  sort: number
  status: string
  description: string
  createdBy?: string
  updatedBy?: string
}

// 组织类型选项
export const ORG_TYPE_OPTIONS = [
  { label: '公司', value: 'company' },
  { label: '部门', value: 'department' },
  { label: '组', value: 'group' },
  { label: '岗位', value: 'position' }
]

/**
 * 获取组织树
 */
export function getOrganizationTree() {
  return api.get<Organization[]>('/organization/tree')
}

/**
 * 获取启用的组织树
 */
export function getActiveOrganizationTree() {
  return api.get<Organization[]>('/organization/tree/active')
}

/**
 * 获取组织列表
 */
export function getOrganizationList() {
  return api.get<Organization[]>('/organization/list')
}

/**
 * 获取组织详情
 */
export function getOrganizationById(id: number) {
  return api.get<Organization>(`/organization/${id}`)
}

/**
 * 获取子组织
 */
export function getOrganizationChildren(id: number) {
  return api.get<Organization[]>(`/organization/${id}/children`)
}

/**
 * 创建组织
 */
export function createOrganization(data: OrganizationForm) {
  return api.post<Organization>('/organization', data)
}

/**
 * 更新组织
 */
export function updateOrganization(id: number, data: OrganizationForm) {
  return api.put<Organization>(`/organization/${id}`, data)
}

/**
 * 删除组织
 */
export function deleteOrganization(id: number) {
  return api.delete<void>(`/organization/${id}`)
}

/**
 * 批量删除组织
 */
export function batchDeleteOrganizations(ids: number[]) {
  return api.delete<void>('/organization/batch', { data: ids })
}
