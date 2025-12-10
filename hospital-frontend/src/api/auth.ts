import request from './request'

export interface LoginRequest {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  userType: number
  phone?: string
  patientId?: number
  doctorId?: number
}

export interface LoginResponse {
  code: number
  message: string
  data?: {
    token: string
    userInfo?: UserInfo
  }
}

export const authApi = {
  // 用户登录
  login(data: LoginRequest) {
    return request.post<LoginResponse>('/auth/login', data)
  },
  
  // 用户登出
  logout() {
    return request.post('/auth/logout')
  },
  
  // 获取当前用户信息
  getCurrentUser() {
    return request.get('/auth/current')
  }
}

