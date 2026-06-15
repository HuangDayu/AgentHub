import { runtimeConfig } from '@/common/runtime-config'
import type { Agent } from '@/types/agent'
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

// Agent CRUD
export async function listAgents(selection: Selection): Promise<Agent[]> {
  return requestJson<Agent[]>(`/api/v1/workspaces/${selection.workspaceId}/agents`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function createAgent(
  selection: Selection,
  name: string,
  description: string,
  type?: string,
  runtimeCategory?: string
): Promise<Agent> {
  return requestJson<Agent>(`/api/v1/workspaces/${selection.workspaceId}/agents`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
    bodyJson: { name, description, type, runtimeCategory },
  })
}

export async function getAgent(selection: Selection, agentId: string): Promise<Agent> {
  return requestJson<Agent>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function updateAgent(
  selection: Selection,
  agentId: string,
  name: string,
  description: string,
  type?: string,
  runtimeCategory?: string
): Promise<Agent> {
  return requestJson<Agent>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PUT',
    headers: buildHeaders(selection),
    bodyJson: { name, description, type, runtimeCategory },
  })
}

export async function deleteAgent(selection: Selection, agentId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: buildHeaders(selection),
  })
}

// Agent Publish
export async function publishAgent(selection: Selection, agentId: string): Promise<Agent> {
  return requestJson<Agent>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/enabled`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}

export async function unpublishAgent(selection: Selection, agentId: string): Promise<Agent> {
  return requestJson<Agent>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/unenabled`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}
