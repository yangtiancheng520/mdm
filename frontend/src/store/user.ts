import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

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
        // 解析失败，清除
        localStorage.removeItem('userInfo')
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

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
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
    isLogin,
    setToken,
    setUserInfo,
    logout,
    hasPermission
  }
})
