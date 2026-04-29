import { runtimeConfig } from '@/common/runtime-config'
import type {
  AuditEvent,
  AuditExportJob,
  Budget,
  Connector,
  Invoice,
  PlatformStats,
  Policy,
  SyncJob,
  TenantInfo,
  Tool,
  ToolInvocationResult,
  Usage,
} from '@/domain/types'
import { requestJson } from './http'

// ─── Platform Overview ────────────────────────────────────────────────
export function getPlatformStats() {
  return requestJson<PlatformStats>('/api/v1/platform/stats', {
    baseUrl: runtimeConfig.platformApiBase,
    method: 'GET',
  })
}

export function listTenants(size = 100) {
  return requestJson<TenantInfo[]>(`/api/v1/platform/tenants?size=${size}`, {
    baseUrl: runtimeConfig.platformApiBase,
    method: 'GET',
  })
}

// ─── Billing ──────────────────────────────────────────────────────────
export function getUsage(tenantId?: string) {
  const query = tenantId ? `?tenantId=${encodeURIComponent(tenantId)}` : ''
  return requestJson<Usage>(`/api/v1/billing/usage${query}`, {
    baseUrl: runtimeConfig.billingApiBase,
    method: 'GET',
  })
}

export async function listInvoices() {
  const response = await requestJson<{ items: Invoice[] }>('/api/v1/billing/invoices', {
    baseUrl: runtimeConfig.billingApiBase,
    method: 'GET',
  })
  return response.items
}

export function updateBudget(tenantId: string, limitCents: number, currency: string) {
  return requestJson<Budget>('/api/v1/billing/budgets', {
    baseUrl: runtimeConfig.billingApiBase,
    method: 'PUT',
    bodyJson: { tenantId, limitCents, currency },
  })
}

// ─── Audit ────────────────────────────────────────────────────────────
export async function listAuditEvents(actorId = '') {
  const query = actorId ? `?actorId=${encodeURIComponent(actorId)}` : ''
  const response = await requestJson<{ items: AuditEvent[] }>(`/api/v1/audit/events${query}`, {
    baseUrl: runtimeConfig.auditApiBase,
    method: 'GET',
  })
  return response.items
}

export function createExportJob(format: string, requestedBy: string) {
  return requestJson<AuditExportJob>('/api/v1/audit/export-jobs', {
    baseUrl: runtimeConfig.auditApiBase,
    method: 'POST',
    bodyJson: { format, requestedBy },
  })
}

// ─── Connectors ───────────────────────────────────────────────────────
export function listConnectors() {
  return requestJson<Connector[]>('/api/v1/connectors', {
    baseUrl: runtimeConfig.connectorApiBase,
    method: 'GET',
  })
}

export function getConnector(id: string) {
  return requestJson<Connector>(`/api/v1/connectors/${id}`, {
    baseUrl: runtimeConfig.connectorApiBase,
    method: 'GET',
  })
}

export function createConnector(name: string, type: string, config: Record<string, string>) {
  return requestJson<Connector>('/api/v1/connectors', {
    baseUrl: runtimeConfig.connectorApiBase,
    method: 'POST',
    bodyJson: { name, type, config },
  })
}

export function updateConnector(id: string, name: string, type: string, config: Record<string, string>) {
  return requestJson<Connector>(`/api/v1/connectors/${id}`, {
    baseUrl: runtimeConfig.connectorApiBase,
    method: 'PUT',
    bodyJson: { name, type, config },
  })
}

export function deleteConnector(id: string) {
  return requestJson<void>(`/api/v1/connectors/${id}`, {
    baseUrl: runtimeConfig.connectorApiBase,
    method: 'DELETE',
  })
}

export function setConnectorEnabled(id: string, enabled: boolean) {
  return requestJson<Connector>(`/api/v1/connectors/${id}/enabled`, {
    baseUrl: runtimeConfig.connectorApiBase,
    method: 'PATCH',
    bodyJson: { enabled },
  })
}

export function triggerSync(connectorId: string) {
  return requestJson<SyncJob>(`/api/v1/connectors/${connectorId}/sync`, {
    baseUrl: runtimeConfig.connectorApiBase,
    method: 'POST',
  })
}

export async function listSyncJobs(connectorId: string) {
  const response = await requestJson<{ items: SyncJob[] }>(`/api/v1/connectors/${connectorId}/sync-jobs`, {
    baseUrl: runtimeConfig.connectorApiBase,
    method: 'GET',
  })
  return response.items
}

// ─── Policies ─────────────────────────────────────────────────────────
export async function listPolicies() {
  const response = await requestJson<{ items: Policy[]; total: number }>('/api/v1/policies', {
    baseUrl: runtimeConfig.policyApiBase,
    method: 'GET',
  })
  return response.items
}

export function getPolicy(id: string) {
  return requestJson<Policy>(`/api/v1/policies/${id}`, {
    baseUrl: runtimeConfig.policyApiBase,
    method: 'GET',
  })
}

export function createPolicy(name: string, description: string, rules: Record<string, unknown>) {
  return requestJson<Policy>('/api/v1/policies', {
    baseUrl: runtimeConfig.policyApiBase,
    method: 'POST',
    bodyJson: { name, description, rules },
  })
}

export function updatePolicy(id: string, name: string, description: string, rules: Record<string, unknown>) {
  return requestJson<Policy>(`/api/v1/policies/${id}`, {
    baseUrl: runtimeConfig.policyApiBase,
    method: 'PUT',
    bodyJson: { name, description, rules },
  })
}

export function deletePolicy(id: string) {
  return requestJson<void>(`/api/v1/policies/${id}`, {
    baseUrl: runtimeConfig.policyApiBase,
    method: 'DELETE',
  })
}

// ─── Tools ────────────────────────────────────────────────────────────
export function listTools() {
  return requestJson<Tool[]>('/api/v1/tools', {
    baseUrl: runtimeConfig.toolApiBase,
    method: 'GET',
  })
}

export function getTool(id: string) {
  return requestJson<Tool>(`/api/v1/tools/${id}`, {
    baseUrl: runtimeConfig.toolApiBase,
    method: 'GET',
  })
}

export function createTool(name: string, description: string, parameters: Record<string, unknown>) {
  return requestJson<Tool>('/api/v1/tools', {
    baseUrl: runtimeConfig.toolApiBase,
    method: 'POST',
    bodyJson: { name, description, parameters },
  })
}

export function updateTool(id: string, name: string, description: string, parameters: Record<string, unknown>, enabled: boolean) {
  return requestJson<Tool>(`/api/v1/tools/${id}`, {
    baseUrl: runtimeConfig.toolApiBase,
    method: 'PUT',
    bodyJson: { name, description, parameters, enabled },
  })
}

export function deleteTool(id: string) {
  return requestJson<void>(`/api/v1/tools/${id}`, {
    baseUrl: runtimeConfig.toolApiBase,
    method: 'DELETE',
  })
}

export function invokeTool(id: string, params: Record<string, unknown>) {
  return requestJson<ToolInvocationResult>(`/api/v1/tools/${id}/invoke`, {
    baseUrl: runtimeConfig.toolApiBase,
    method: 'POST',
    bodyJson: params,
  })
}

