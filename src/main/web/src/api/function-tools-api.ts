import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'

export interface FunctionTool {
  id: string
  tenantId: string
  toolClassName: string
  toolName: string
  description: string
  category: string
  methodCount: number
  enabled: boolean
  systemTool: boolean
  createdAt: string
  updatedAt: string
}

export function listFunctionTools(selection: SelectionState) {
  return requestJson<FunctionTool[]>(
    `/api/v1/workspaces/${selection.workspaceId}/function-tools`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function listEnabledFunctionTools(selection: SelectionState) {
  return requestJson<FunctionTool[]>(
    `/api/v1/workspaces/${selection.workspaceId}/function-tools/enabled`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function getFunctionTool(selection: SelectionState, id: string) {
  return requestJson<FunctionTool>(
    `/api/v1/workspaces/${selection.workspaceId}/function-tools/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function syncFunctionTools(selection: SelectionState) {
  return requestJson<string>(
    `/api/v1/workspaces/${selection.workspaceId}/function-tools/sync`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: scopedHeaders(selection) }
  )
}

export function enableFunctionTool(selection: SelectionState, id: string) {
  return requestJson<FunctionTool>(
    `/api/v1/workspaces/${selection.workspaceId}/function-tools/${id}/enable`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: scopedHeaders(selection) }
  )
}

export function disableFunctionTool(selection: SelectionState, id: string) {
  return requestJson<FunctionTool>(
    `/api/v1/workspaces/${selection.workspaceId}/function-tools/${id}/disable`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: scopedHeaders(selection) }
  )
}

export function deleteFunctionTool(selection: SelectionState, id: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/function-tools/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'DELETE', headers: scopedHeaders(selection) }
  )
}
