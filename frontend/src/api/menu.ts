import api from './index'

/**
 * 菜单接口
 */

export interface Menu {
  id: number
  name: string
  code: string
  path: string
  icon: string
  sort: number
  type: string
  parentId: number | null
  children?: Menu[]
}

/**
 * 获取当前用户的菜单
 */
export function getUserMenu() {
  return api.get<Menu[]>('/menu/user')
}

/**
 * 获取所有菜单（用于权限管理）
 */
export function getAllMenu() {
  return api.get<Menu[]>('/menu/all')
}
