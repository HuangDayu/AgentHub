import { runtimeConfig } from '@/common/runtime-config'
import type { VectorStoreConfig, ModelConfig, SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'

// ── Vector Store Config ─────────────────────────────────

export async function listVectorStoreConfigs(selection: SelectionState): Promise<VectorStoreConfig[]> {
  const response = await requestJson<VectorStoreConfig[]>(`/api/v1/workspaces/${selection.workspaceId}/vector-stores`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
  return response
}

export async function createVectorStoreConfig(
  selection: SelectionState,
  config: {
    name: string
    type: string
    host: string
    port: number
    apiKey?: string
    collectionName: string
    extraParams?: string
  }
): Promise<VectorStoreConfig> {
  return requestJson<VectorStoreConfig>(`/api/v1/workspaces/${selection.workspaceId}/vector-stores`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: config,
  })
}

export async function updateVectorStoreConfig(
  selection: SelectionState,
  configId: string,
  config: {
    name: string
    host: string
    port: number
    apiKey?: string
    collectionName: string
    extraParams?: string
    enabled: boolean
  }
): Promise<VectorStoreConfig> {
  return requestJson<VectorStoreConfig>(`/api/v1/workspaces/${selection.workspaceId}/vector-stores/${configId}`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'PATCH',
    headers: scopedHeaders(selection),
    bodyJson: config,
  })
}

export async function deleteVectorStoreConfig(selection: SelectionState, configId: string): Promise<void> {
  await requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/vector-stores/${configId}`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'DELETE',
    headers: scopedHeaders(selection),
  })
}

export async function testVectorStoreConfig(
  selection: SelectionState,
  configId: string
): Promise<{ success: boolean; message: string; details?: string }> {
  return requestJson<{ success: boolean; message: string; details?: string }>(
    `/api/v1/workspaces/${selection.workspaceId}/vector-stores/${configId}/test`,
    {
      baseUrl: runtimeConfig.knowledgeApiBase,
      method: 'POST',
      headers: scopedHeaders(selection),
    }
  )
}

export async function refreshVectorStoreInstance(selection: SelectionState, configId: string): Promise<void> {
  await requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/vector-stores/${configId}/refresh`, {
    baseUrl: runtimeConfig.knowledgeApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
  })
}

// ── Model Config ────────────────────────────────────────

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
