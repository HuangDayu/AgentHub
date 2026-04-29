import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'

export interface McpTool {
  id: string
  name: string
  description?: string
  serverUrl: string
  serverType: 'STDIO' | 'HTTP' | 'SSE'
  command?: string
  args?: string[]
  env?: Record<string, string>
  enabled: boolean
  createdAt?: string
  updatedAt?: string
}

export function listMcpTools(selection: SelectionState) {
  return requestJson<McpTool[]>(
    `/api/v1/workspaces/${selection.workspaceId}/mcp-tools`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function createMcpTool(selection: SelectionState, data: Partial<McpTool>) {
  return requestJson<McpTool>(
    `/api/v1/workspaces/${selection.workspaceId}/mcp-tools`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: scopedHeaders(selection), bodyJson: data }
  )
}

export function getMcpTool(selection: SelectionState, id: string) {
  return requestJson<McpTool>(
    `/api/v1/workspaces/${selection.workspaceId}/mcp-tools/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function updateMcpTool(selection: SelectionState, id: string, data: Partial<McpTool>) {
  return requestJson<McpTool>(
    `/api/v1/workspaces/${selection.workspaceId}/mcp-tools/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'PUT', headers: scopedHeaders(selection), bodyJson: data }
  )
}

export function deleteMcpTool(selection: SelectionState, id: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/mcp-tools/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'DELETE', headers: scopedHeaders(selection) }
  )
}
