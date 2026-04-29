import { runtimeConfig } from '@/common/runtime-config'
import type { Tenant, Workspace, Member } from '@/domain/types'
import { requestJson } from './http'

// ── User Info ─────────────────────────────────────────────

export interface UserInfo {
  id: string
  username: string
  tenantId: string
  roles: string[]
}

/**
 * 获取当前登录用户信息（包括租户ID）
 */
export function getCurrentUser() {
  return requestJson<UserInfo>('/api/v1/auth/me', {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'GET',
  })
}

// ── Tenants ──────────────────────────────────────────────

export function listTenants() {
  return requestJson<Tenant[]>('/api/v1/tenants', { baseUrl: runtimeConfig.tenantApiBase, method: 'GET' })
}

export function createTenant(payload: {
  tenantCode: string
  name: string
  planCode: string
  region: string
}) {
  return requestJson<Tenant>('/api/v1/tenants', {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'POST',
    bodyJson: payload,
  })
}

// ── Workspaces ───────────────────────────────────────────

export function listWorkspaces(tenantId: string) {
  return requestJson<Workspace[]>(`/api/v1/tenants/${tenantId}/workspaces`, {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'GET',
  })
}

export function createWorkspace(tenantId: string, payload: { workspaceCode: string; name: string; region?: string }) {
  return requestJson<Workspace>(`/api/v1/tenants/${tenantId}/workspaces`, {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'POST',
    bodyJson: payload,
  })
}

export function getWorkspace(tenantId: string, workspaceId: string) {
  return requestJson<Workspace>(`/api/v1/workspaces/${workspaceId}`, {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'GET',
  })
}

export function updateWorkspace(workspaceId: string, payload: { name?: string }) {
  return requestJson<Workspace>(`/api/v1/workspaces/${workspaceId}`, {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'PATCH',
    bodyJson: payload,
  })
}

export function deleteWorkspace(workspaceId: string) {
  return requestJson<void>(`/api/v1/workspaces/${workspaceId}`, {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'DELETE',
  })
}

// ── Members ──────────────────────────────────────────────

export function listMembers(workspaceId: string) {
  return requestJson<Member[]>(`/api/v1/workspaces/${workspaceId}/members`, {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'GET',
  })
}

export function createMember(workspaceId: string, payload: {
  userId: string
  roleCode: string
  scopeType: string
}) {
  return requestJson<Member>(`/api/v1/workspaces/${workspaceId}/members`, {
    baseUrl: runtimeConfig.tenantApiBase,
    method: 'POST',
    bodyJson: payload,
  })
}

