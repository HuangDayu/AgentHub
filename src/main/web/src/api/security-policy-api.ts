import { runtimeConfig } from '@/common/runtime-config'
import type { SecurityPolicy } from '@/types/memory'
import { requestJson } from './http'

export interface Selection {
  tenantId: string
  workspaceId: string
}

function buildHeaders(selection: Selection) {
  return {
    'X-Tenant-Id': selection.tenantId,
    'X-Workspace-Id': selection.workspaceId,
  }
}

export async function listSecurityPolicies(selection: Selection): Promise<SecurityPolicy[]> {
  return requestJson<SecurityPolicy[]>(`/api/v1/workspaces/${selection.workspaceId}/security-policies`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function createSecurityPolicy(
  selection: Selection,
  name: string,
  description: string
): Promise<SecurityPolicy> {
  return requestJson<SecurityPolicy>(`/api/v1/workspaces/${selection.workspaceId}/security-policies`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, workspaceId: selection.workspaceId, name, description },
  })
}

export async function getSecurityPolicy(selection: Selection, policyId: string): Promise<SecurityPolicy> {
  return requestJson<SecurityPolicy>(`/api/v1/workspaces/${selection.workspaceId}/security-policies/${policyId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function updateSecurityPolicy(
  selection: Selection,
  policyId: string,
  name: string,
  description: string
): Promise<SecurityPolicy> {
  return requestJson<SecurityPolicy>(`/api/v1/workspaces/${selection.workspaceId}/security-policies/${policyId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PUT',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, workspaceId: selection.workspaceId, name, description },
  })
}

export async function deleteSecurityPolicy(selection: Selection, policyId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/security-policies/${policyId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: buildHeaders(selection),
  })
}
