import request from './request'
import type { PermissionTree } from '@/types/user'

export function getPermissionTree() {
  return request.get<any, { code: number; message: string; data: PermissionTree[] }>('/permission/tree')
}

export function getMyPermissions() {
  return request.get<any, { code: number; message: string; data: string[] }>('/permission/my')
}
