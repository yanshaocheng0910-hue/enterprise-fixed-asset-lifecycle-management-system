export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  userId: number
  username: string
  realName: string
  department: string
  roles: string[]
  permissions: string[]
}

export interface SysUser {
  id: number
  username: string
  realName: string
  department: string
  phone: string
  email: string
  status: number
  roleIds: number[]
  roleNames: string[]
  createdAt: string
  updatedAt: string
}

export interface UserPageRequest {
  pageNum: number
  pageSize: number
  username?: string
  realName?: string
  status?: number
}

export interface UserCreateRequest {
  username: string
  password: string
  realName: string
  department?: string
  phone?: string
  email?: string
}

export interface UserUpdateRequest {
  realName: string
  department?: string
  phone?: string
  email?: string
}

export interface UserStatusRequest {
  status: number
}

export interface UserRoleRequest {
  roleIds: number[]
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface SysRole {
  id: number
  roleCode: string
  roleName: string
  description: string
  createdAt: string
}

export interface RoleVO {
  id: number
  roleCode: string
  roleName: string
  description: string
  permissionIds: number[]
  userCount: number
  createdAt: string
}

export interface RoleCreateRequest {
  roleCode: string
  roleName: string
  description?: string
}

export interface RoleUpdateRequest {
  roleName: string
  description?: string
}

export interface RolePermissionRequest {
  permissionIds: number[]
}

export interface PermissionItem {
  id: number
  permissionCode: string
  permissionName: string
}

export interface PermissionTree {
  module: string
  permissions: PermissionItem[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}
