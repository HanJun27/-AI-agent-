import request from './request'

export interface LoginData {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  realName: string
  avatar: string
}

export function login(data: LoginData) {
  return request.post<any, { data: LoginResponse }>('/auth/login', data)
}
