import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

const TOKEN_KEY = 'user_console_access_token'
const REFRESH_KEY = 'user_console_refresh_token'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(loadFromStorage(TOKEN_KEY))
  const refreshToken = ref(loadFromStorage(REFRESH_KEY))

  const isAuthenticated = computed(() => accessToken.value.length > 0)

  function setTokens(access: string, refresh: string) {
    accessToken.value = access
    refreshToken.value = refresh
    persistToStorage(TOKEN_KEY, access)
    persistToStorage(REFRESH_KEY, refresh)
  }

  function clearTokens() {
    accessToken.value = ''
    refreshToken.value = ''
    removeFromStorage(TOKEN_KEY)
    removeFromStorage(REFRESH_KEY)
  }

  return {
    accessToken,
    refreshToken,
    isAuthenticated,
    setTokens,
    clearTokens,
  }
})

function loadFromStorage(key: string): string {
  try {
    return localStorage.getItem(key) ?? ''
  } catch {
    return ''
  }
}

function persistToStorage(key: string, value: string) {
  try {
    localStorage.setItem(key, value)
  } catch {
    // storage unavailable
  }
}

function removeFromStorage(key: string) {
  try {
    localStorage.removeItem(key)
  } catch {
    // storage unavailable
  }
}

