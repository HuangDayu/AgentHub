import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'

export interface SystemTool {
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

export function listSystemTools(selection: SelectionState) {
  return requestJson<SystemTool[]>(
    `/api/v1/workspaces/${selection.workspaceId}/system-tools`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function listEnabledSystemTools(selection: SelectionState) {
  return requestJson<SystemTool[]>(
    `/api/v1/workspaces/${selection.workspaceId}/system-tools/enabled`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function getSystemTool(selection: SelectionState, id: string) {
  return requestJson<SystemTool>(
    `/api/v1/workspaces/${selection.workspaceId}/system-tools/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function syncSystemTools(selection: SelectionState) {
  return requestJson<string>(
    `/api/v1/workspaces/${selection.workspaceId}/system-tools/sync`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: scopedHeaders(selection) }
  )
}

export function enableSystemTool(selection: SelectionState, id: string) {
  return requestJson<SystemTool>(
    `/api/v1/workspaces/${selection.workspaceId}/system-tools/${id}/enable`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: scopedHeaders(selection) }
  )
}

export function disableSystemTool(selection: SelectionState, id: string) {
  return requestJson<SystemTool>(
    `/api/v1/workspaces/${selection.workspaceId}/system-tools/${id}/disable`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: scopedHeaders(selection) }
  )
}

export function deleteSystemTool(selection: SelectionState, id: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/system-tools/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'DELETE', headers: scopedHeaders(selection) }
  )
}
