import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'
import { setToken, removeToken, getToken } from '@/utils/token'
import type { LoginRequest, LoginResponse } from '@/types/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<LoginResponse | null>(null)
  const isLoggedIn = ref(!!token.value)

  async function login(credentials: LoginRequest) {
    const res = await loginApi(credentials)
    if (res.code === 200) {
      const data = res.data
      setToken(data.token)
      token.value = data.token
      userInfo.value = data
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
        return res.data
      }
    } catch {
      logout()
    }
    return null
  }

  function logout() {
    removeToken()
    token.value = null
    userInfo.value = null
    isLoggedIn.value = false
  }

  return { token, userInfo, isLoggedIn, login, fetchCurrentUser, logout }
})
