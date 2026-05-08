import type { RetrievalConfig, SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'
import { runtimeConfig } from '@/common/runtime-config'

export type { RetrievalConfig }

export async function listRetrievalConfigs(selection: SelectionState): Promise<RetrievalConfig[]> {
  const response = await requestJson<RetrievalConfig[]>(`/api/v1/workspaces/${selection.workspaceId}/retrieval-configs`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
  return response
}

export async function createRetrievalConfig(
  selection: SelectionState,
  config: Partial<RetrievalConfig>
): Promise<RetrievalConfig> {
  return requestJson<RetrievalConfig>(`/api/v1/workspaces/${selection.workspaceId}/retrieval-configs`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: config,
  })
}

export async function updateRetrievalConfig(
  selection: SelectionState,
  configId: string,
  config: Partial<RetrievalConfig>
): Promise<RetrievalConfig> {
  return requestJson<RetrievalConfig>(`/api/v1/workspaces/${selection.workspaceId}/retrieval-configs/${configId}`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'PUT',
    headers: scopedHeaders(selection),
    bodyJson: config,
  })
}

export async function deleteRetrievalConfig(selection: SelectionState, configId: string): Promise<void> {
  await requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/retrieval-configs/${configId}`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'DELETE',
    headers: scopedHeaders(selection),
  })
}
