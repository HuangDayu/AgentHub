import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'

export interface AgentConfig {
  id: string
  agentId: string
  category: string
  type: string
  configId: string
  description?: string
  priority: number
  enabled: boolean
  createdAt?: string
  updatedAt?: string
}

export type AgentConfigResponse = AgentConfig

export function listAgentConfigs(selection: SelectionState, agentId: string, category?: string) {
  const url = category
    ? `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/configs?category=${category}`
    : `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/configs`
  return requestJson<AgentConfig[]>(
    url,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function setAgentConfig(selection: SelectionState, agentId: string, data: Partial<AgentConfig>) {
  return requestJson<AgentConfig>(
    `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/configs`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: scopedHeaders(selection), bodyJson: data }
  )
}

export function updateAgentConfig(selection: SelectionState, agentId: string, id: string, data: Partial<AgentConfig>) {
  return requestJson<AgentConfig>(
    `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/configs/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'PUT', headers: scopedHeaders(selection), bodyJson: data }
  )
}

export function getAgentConfig(selection: SelectionState, agentId: string, id: string) {
  return requestJson<AgentConfig>(
    `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/configs/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function deleteAgentConfig(selection: SelectionState, agentId: string, id: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/configs/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'DELETE', headers: scopedHeaders(selection) }
  )
}

export function deleteAllAgentConfigs(selection: SelectionState, agentId: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/configs`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'DELETE', headers: scopedHeaders(selection) }
  )
}

export function syncAgentConfigs(selection: SelectionState, agentId: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/configs/sync`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: scopedHeaders(selection) }
  )
}
