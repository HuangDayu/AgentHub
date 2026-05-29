import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'

export interface ConfigTypeDefinition {
  category: string
  displayName: string
  description: string
  types: TypeInfo[]
}

export interface TypeInfo {
  type: string
  displayName: string
  description: string
}

export interface AvailableConfig {
  id: string
  name: string
  description?: string
}

export function getConfigTypes(selection: SelectionState) {
  return requestJson<ConfigTypeDefinition[]>(
      `/api/v1/workspaces/${selection.workspaceId}/agent-config-types`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function getAvailableConfigs(selection: SelectionState, category: string, type: string, workspaceId?: string) {
  const params = new URLSearchParams({ category, type })
  if (workspaceId) params.append('workspaceId', workspaceId)
  return requestJson<AvailableConfig[]>(
    `/api/v1/workspaces/${workspaceId}/agent-config-types/available?${params.toString()}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}
