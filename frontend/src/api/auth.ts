import request from './request'
import type { LoginRequest, LoginResponse } from '@/types/user'

export function login(data: LoginRequest) {
  return request.post<any, { code: number; message: string; data: LoginResponse }>('/auth/login', data)
}

export function getCurrentUser() {
  return request.get<any, { code: number; message: string; data: LoginResponse }>('/auth/me')
}
