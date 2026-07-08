import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'
import { getMyPermissions } from '@/api/permission'
import { setToken, removeToken, getToken } from '@/utils/token'
import type { LoginRequest, LoginResponse } from '@/types/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<LoginResponse | null>(null)
  const isLoggedIn = ref(!!token.value)
  const permissions = ref<string[]>([])

  function hasPermission(perm: string): boolean {
    return permissions.value.includes(perm)
  }

  async function login(credentials: LoginRequest) {
    const res = await loginApi(credentials)
    if (res.code === 200) {
      const data = res.data
      setToken(data.token)
      token.value = data.token
      userInfo.value = data
      permissions.value = data.permissions || []
      isLoggedIn.value = true
      return data
    }
    throw new Error(res.message)
  }

  async function fetchCurrentUser() {
    if (!token.value) return null
    try {
      const res = await getCurrentUser()
      if (res.code === 200) {
        userInfo.value = res.data
        permissions.value = res.data.permissions || []
        return res.data
      }
    } catch {
      logout()
    }
    return null
  }

  async function refreshPermissions() {
    try {
      const res = await getMyPermissions()
      if (res.code === 200) {
        permissions.value = res.data
      }
    } catch {
      // ignore
    }
  }

  function logout() {
    removeToken()
    token.value = null
    userInfo.value = null
    permissions.value = []
    isLoggedIn.value = false
  }

  return { token, userInfo, isLoggedIn, permissions, hasPermission, login, fetchCurrentUser, refreshPermissions, logout }
})
