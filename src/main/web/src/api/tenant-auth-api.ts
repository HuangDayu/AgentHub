import { runtimeConfig } from '../common/runtime-config'
import { requestJson } from './http'

export interface TokenEnvelope {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export function login(username: string, password: string) {
  return requestJson<TokenEnvelope>('/api/v1/auth/login', {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'POST',
    bodyJson: { username, password },
  })
}

export function refreshToken(refreshTokenValue: string) {
  return requestJson<TokenEnvelope>('/api/v1/auth/refresh', {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'POST',
    bodyJson: { refreshToken: refreshTokenValue },
  })
}
