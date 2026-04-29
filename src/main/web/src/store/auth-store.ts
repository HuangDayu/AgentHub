import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

function loadFromStorage(key: string): string {
  try { return localStorage.getItem(key) ?? '' } catch { return '' }
}
function persistToStorage(key: string, value: string) {
  try { localStorage.setItem(key, value) } catch { /* no-op */ }
}
function removeFromStorage(key: string) {
  try { localStorage.removeItem(key) } catch { /* no-op */ }
}

// Tenant auth
const TENANT_TOKEN_KEY = 'tenant_console_access_token'
const TENANT_REFRESH_KEY = 'tenant_console_refresh_token'

export const useTenantAuthStore = defineStore('tenant-auth', () => {
  const accessToken = ref(loadFromStorage(TENANT_TOKEN_KEY))
  const refreshToken = ref(loadFromStorage(TENANT_REFRESH_KEY))
  const isAuthenticated = computed(() => accessToken.value.length > 0)
  function setTokens(access: string, refresh: string) {
    accessToken.value = access; refreshToken.value = refresh
    persistToStorage(TENANT_TOKEN_KEY, access); persistToStorage(TENANT_REFRESH_KEY, refresh)
  }
  function clearTokens() {
    accessToken.value = ''; refreshToken.value = ''
    removeFromStorage(TENANT_TOKEN_KEY); removeFromStorage(TENANT_REFRESH_KEY)
  }
  return { accessToken, refreshToken, isAuthenticated, setTokens, clearTokens }
})

// User auth
const USER_TOKEN_KEY = 'user_console_access_token'
const USER_REFRESH_KEY = 'user_console_refresh_token'

export const useUserAuthStore = defineStore('user-auth', () => {
  const accessToken = ref(loadFromStorage(USER_TOKEN_KEY))
  const refreshToken = ref(loadFromStorage(USER_REFRESH_KEY))
  const isAuthenticated = computed(() => accessToken.value.length > 0)
  function setTokens(access: string, refresh: string) {
    accessToken.value = access; refreshToken.value = refresh
    persistToStorage(USER_TOKEN_KEY, access); persistToStorage(USER_REFRESH_KEY, refresh)
  }
  function clearTokens() {
    accessToken.value = ''; refreshToken.value = ''
    removeFromStorage(USER_TOKEN_KEY); removeFromStorage(USER_REFRESH_KEY)
  }
  return { accessToken, refreshToken, isAuthenticated, setTokens, clearTokens }
})

==============================
// Backward-compatible alias (used by layouts and views)
// IMPORTANT: User Console uses useUserAuthStore, Tenant uses useTenantAuthStore
// Do NOT alias both to the same store!
==============================
export const useAuthStore = useTenantAuthStore
