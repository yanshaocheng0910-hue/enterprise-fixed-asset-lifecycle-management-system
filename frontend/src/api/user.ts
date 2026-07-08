import request from './request'
import type {
  SysUser,
  UserPageRequest,
  UserCreateRequest,
  UserUpdateRequest,
  UserStatusRequest,
  UserRoleRequest,
  ChangePasswordRequest,
  PageResult,
  SysRole
} from '@/types/user'

export function getUserPage(params: UserPageRequest) {
  return request.get<any, { code: number; message: string; data: PageResult<SysUser> }>('/user/page', { params })
}

export function getUserDetail(id: number) {
  return request.get<any, { code: number; message: string; data: SysUser }>(`/user/${id}`)
}

export function createUser(data: UserCreateRequest) {
  return request.post<any, { code: number; message: string; data: number }>('/user', data)
}

export function updateUser(id: number, data: UserUpdateRequest) {
  return request.put<any, { code: number; message: string }>(`/user/${id}`, data)
}

export function updateUserStatus(id: number, data: UserStatusRequest) {
  return request.put<any, { code: number; message: string }>(`/user/${id}/status`, data)
}

export function deleteUser(id: number) {
  return request.delete<any, { code: number; message: string }>(`/user/${id}`)
}

export function assignUserRoles(id: number, data: UserRoleRequest) {
  return request.put<any, { code: number; message: string }>(`/user/${id}/roles`, data)
}

export function changePassword(data: ChangePasswordRequest) {
  return request.put<any, { code: number; message: string }>('/user/me/password', data)
}

export function getAllRoles() {
  return request.get<any, { code: number; message: string; data: SysRole[] }>('/role/all')
}

export function getRolePage() {
  return request.get<any, { code: number; message: string; data: PageResult<any> }>('/role/page')
}

export function getRoleDetail(id: number) {
  return request.get<any, { code: number; message: string; data: any }>(`/role/${id}`)
}

export function createRole(data: { roleCode: string; roleName: string; description?: string }) {
  return request.post<any, { code: number; message: string; data: number }>('/role', data)
}

export function updateRole(id: number, data: { roleName: string; description?: string }) {
  return request.put<any, { code: number; message: string }>(`/role/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete<any, { code: number; message: string }>(`/role/${id}`)
}

export function assignRolePermissions(id: number, data: { permissionIds: number[] }) {
  return request.put<any, { code: number; message: string }>(`/role/${id}/permissions`, data)
}
