import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'
import type {
  AgentDataSource,
  AgentDataSourceInvokeResult,
  AgentDataSourceTestResult,
} from '@/types/agent-data-source'

const BASE = (w: string) => `/api/v1/workspaces/${w}/agent-data-sources`

export function listAgentDataSources(selection: SelectionState) {
  return requestJson<AgentDataSource[]>(BASE(selection.workspaceId), {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
}

export function getAgentDataSource(selection: SelectionState, id: string) {
  return requestJson<AgentDataSource>(`${BASE(selection.workspaceId)}/${id}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
}

export function createAgentDataSource(selection: SelectionState, data: Partial<AgentDataSource>) {
  return requestJson<AgentDataSource>(BASE(selection.workspaceId), {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: data,
  })
}

export function updateAgentDataSource(selection: SelectionState, id: string, data: Partial<AgentDataSource>) {
  return requestJson<AgentDataSource>(`${BASE(selection.workspaceId)}/${id}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PATCH',
    headers: scopedHeaders(selection),
    bodyJson: data,
  })
}

export function enableAgentDataSource(selection: SelectionState, id: string) {
  return requestJson<AgentDataSource>(`${BASE(selection.workspaceId)}/${id}/enable`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
  })
}

export function disableAgentDataSource(selection: SelectionState, id: string) {
  return requestJson<AgentDataSource>(`${BASE(selection.workspaceId)}/${id}/disable`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
  })
}

export function deleteAgentDataSource(selection: SelectionState, id: string) {
  return requestJson<void>(`${BASE(selection.workspaceId)}/${id}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: scopedHeaders(selection),
  })
}

export function testAgentDataSource(selection: SelectionState, id: string) {
  return requestJson<AgentDataSourceTestResult>(`${BASE(selection.workspaceId)}/${id}/test`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
  })
}

export function invokeAgentDataSource(
  selection: SelectionState,
  id: string,
  data: { userId: string; agentId: string; sessionId: string; body: string; headers: Record<string, string> }
) {
  return requestJson<AgentDataSourceInvokeResult>(`${BASE(selection.workspaceId)}/${id}/invoke`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: data,
  })
}
