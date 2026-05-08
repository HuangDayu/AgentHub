import type { ModelConfig, SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'
import { runtimeConfig } from '@/common/runtime-config'

export type { ModelConfig }

export async function listModelConfigs(selection: SelectionState): Promise<ModelConfig[]> {
  const response = await requestJson<ModelConfig[]>(`/api/v1/workspaces/${selection.workspaceId}/models`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
  return response
}

export async function listModelConfigsByType(selection: SelectionState, type: string): Promise<ModelConfig[]> {
  const response = await requestJson<ModelConfig[]>(`/api/v1/workspaces/${selection.workspaceId}/models?type=${type}`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
  return response
}

export async function createModelConfig(
  selection: SelectionState,
  config: {
    name: string
    type: string
    supplier: string
    apiKey?: string
    baseUrl?: string
    model: string
    enabled: boolean
  }
): Promise<ModelConfig> {
  return requestJson<ModelConfig>(`/api/v1/workspaces/${selection.workspaceId}/models`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: config,
  })
}

export async function updateModelConfig(
  selection: SelectionState,
  configId: string,
  config: {
    name: string
    type: string
    supplier: string
    apiKey?: string
    baseUrl?: string
    model: string
    enabled: boolean
  }
): Promise<ModelConfig> {
  return requestJson<ModelConfig>(`/api/v1/workspaces/${selection.workspaceId}/models/${configId}`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'PUT',
    headers: scopedHeaders(selection),
    bodyJson: config,
  })
}

export async function deleteModelConfig(selection: SelectionState, configId: string): Promise<void> {
  await requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/models/${configId}`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'DELETE',
    headers: scopedHeaders(selection),
  })
}

export async function testModelConfig(
  selection: SelectionState,
  configId: string
): Promise<{ success: boolean; message: string; details?: string }> {
  return requestJson<{ success: boolean; message: string; details?: string }>(
    `/api/v1/workspaces/${selection.workspaceId}/models/${configId}/test`,
    {
      baseUrl: runtimeConfig.knowledgeApiBase,
      method: 'POST',
      headers: scopedHeaders(selection),
    }
  )
}
