import api from './index'

export interface LoginParams {
  account: string
  password: string
}

export interface LoginResult {
  token: string
  user: {
    id: number
    account: string
    name: string
    avatar: string
    roles: string[]
    permissions: string[]
  }
}

// 登录
export function login(data: LoginParams) {
  return api.post<LoginResult>('/auth/login', data)
}

// 获取用户信息
export function getUserInfo() {
  return api.get('/auth/info')
}

// 退出登录
export function logout() {
  return api.post('/auth/logout')
}
