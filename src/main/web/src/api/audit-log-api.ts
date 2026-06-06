import { runtimeConfig } from '@/common/runtime-config'
import { requestJson } from './http'
import type { AuditLogPage, AuditLogQuery } from '@/types/audit-log'

function buildQueryString(q: AuditLogQuery): string {
  const parts: string[] = []
  for (const [key, value] of Object.entries(q)) {
    if (value === undefined || value === null || value === '') continue
    parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
  }
  return parts.length === 0 ? '' : `?${parts.join('&')}`
}

export function queryAuditLogs(tenantId: string, query: AuditLogQuery) {
  const qs = buildQueryString(query)
  return requestJson<AuditLogPage>(`/api/v1/audit-logs${qs}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: { 'X-Tenant-Id': tenantId },
  })
}

export function listAuditLogResourceTypes() {
  return requestJson<string[]>('/api/v1/audit-logs/resource-types', {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
  })
}

export function listAuditLogActions() {
  return requestJson<string[]>('/api/v1/audit-logs/actions', {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
  })
}
