import { runtimeConfig } from '@/common/runtime-config'
import type { Memory } from '@/types/memory'
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

export async function listMemoriesByAgent(selection: Selection, agentId: string): Promise<Memory[]> {
  return requestJson<Memory[]>(`/api/v1/workspaces/${selection.workspaceId}/memories/agents/${agentId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function createMemory(
  selection: Selection,
  agentId: string,
  name: string,
  memoryType: string,
  content: string,
  metadata: string,
  importance: number,
  expiresAt?: string
): Promise<Memory> {
  return requestJson<Memory>(`/api/v1/workspaces/${selection.workspaceId}/memories`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, workspaceId: selection.workspaceId, agentId, name, memoryType, content, metadata, importance, expiresAt },
  })
}

export async function getMemory(selection: Selection, memoryId: string): Promise<Memory> {
  return requestJson<Memory>(`/api/v1/workspaces/${selection.workspaceId}/memories/${memoryId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function updateMemory(
  selection: Selection,
  memoryId: string,
  content: string,
  metadata: string,
  importance: number,
  expiresAt?: string
): Promise<Memory> {
  return requestJson<Memory>(`/api/v1/workspaces/${selection.workspaceId}/memories/${memoryId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PUT',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, workspaceId: selection.workspaceId, content, metadata, importance, expiresAt },
  })
}

export async function deleteMemory(selection: Selection, memoryId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/memories/${memoryId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: buildHeaders(selection),
  })
}

export async function deleteMemoriesByAgent(selection: Selection, agentId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/memories/agents/${agentId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: buildHeaders(selection),
  })
}
