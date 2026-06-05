import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserMenu, type Menu } from '../api/menu'

export interface UserInfo {
  id: number
  account: string
  name: string
  email: string
  phone: string
  avatar: string
  roles: string[]
  permissions: string[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])
  const menus = ref<Menu[]>([])

  const isLogin = computed(() => !!token.value)

  // 从 localStorage 恢复用户信息
  function init() {
    const savedUserInfo = localStorage.getItem('userInfo')
    if (savedUserInfo) {
      try {
        const info = JSON.parse(savedUserInfo)
        userInfo.value = info
        permissions.value = info.permissions || []
      } catch {
        localStorage.removeItem('userInfo')
      }
    }

    // 恢复菜单
    const savedMenus = localStorage.getItem('menus')
    if (savedMenus) {
      try {
        menus.value = JSON.parse(savedMenus)
      } catch {
        localStorage.removeItem('menus')
      }
    }
  }

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    permissions.value = info.permissions || []
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function setMenus(menuList: Menu[]) {
    menus.value = menuList
    localStorage.setItem('menus', JSON.stringify(menuList))
  }

  // 获取用户菜单
  async function fetchMenus() {
    try {
      const res = await getUserMenu()
      setMenus(res.data || [])
      return res.data
    } catch (error) {
      console.error('获取菜单失败', error)
      return []
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    menus.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('menus')
  }

  function hasPermission(permission: string): boolean {
    if (permissions.value.includes('*')) return true
    return permissions.value.includes(permission)
  }

  // 初始化时恢复数据
  init()

  return {
    token,
    userInfo,
    permissions,
    menus,
    isLogin,
    setToken,
    setUserInfo,
    setMenus,
    fetchMenus,
    logout,
    hasPermission
  }
})
