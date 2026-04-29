import { runtimeConfig } from '@/common/runtime-config'
import { requestJson } from './http'

export interface TokenEnvelope {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export function login(username: string, password: string) {
  return requestJson<TokenEnvelope>('/api/v1/auth/login', {
    baseUrl: runtimeConfig.userApiBase,
    method: 'POST',
    bodyJson: { username, password },
  })
}

export function logout() {
  return requestJson<void>('/api/v1/auth/logout', {
    baseUrl: runtimeConfig.userApiBase,
    method: 'POST',
  })
}

export function refreshToken(refreshTokenValue: string) {
  return requestJson<TokenEnvelope>('/api/v1/auth/refresh', {
    baseUrl: runtimeConfig.userApiBase,
    method: 'POST',
    bodyJson: { refreshToken: refreshTokenValue },
  })
}

export interface UserInfo {
  id: string
  username: string
  email?: string
  displayName?: string
  createdAt?: string
}

export function getCurrentUser() {
  return requestJson<UserInfo>('/api/v1/auth/me', {
    baseUrl: runtimeConfig.userApiBase,
  })
}

export function changePassword(oldPassword: string, newPassword: string) {
  return requestJson<void>('/api/v1/auth/password', {
    baseUrl: runtimeConfig.userApiBase,
    method: 'PUT',
    bodyJson: { oldPassword, newPassword },
  })
}

