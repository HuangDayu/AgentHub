import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'

/**
 * 检索策略（与后端RetrievalStrategyResponse一致）
 */
export interface RetrievalStrategy {
  id: string
  name: string
  description?: string
  retrievalType?: string
  topK: number
  scoreThreshold: number
  enableRerank: boolean
  enableQueryRewrite: boolean
  enableTextSearch: boolean
  enableVectorSearch: boolean
  rerankModel?: string
  vectorWeight: number
  keywordWeight: number
  createdAt?: string
  updatedAt?: string
}

/**
 * 模型策略（与后端ModelStrategyResponse一致）
 */
export interface ModelStrategy {
  id: string
  name: string
  description?: string
  temperature: number
  maxTokens: number
  topP: number
  frequencyPenalty: number
  presencePenalty: number
  createdAt?: string
  updatedAt?: string
}

export interface ToolStrategy {
  id: string
  name: string
  description?: string
  maxConcurrentCalls: number
  timeoutSeconds: number
  retryCount: number
  fallbackEnabled: boolean
  allowedTools?: string[]
  createdAt?: string
  updatedAt?: string
}

export interface GuardrailStrategy {
  id: string
  name: string
  description?: string
  inputValidationEnabled: boolean
  outputValidationEnabled: boolean
  piiDetectionEnabled: boolean
  piiMaskingEnabled: boolean
  promptInjectionDetection: boolean
  maxInputLength: number
  maxOutputLength: number
  createdAt?: string
  updatedAt?: string
}

// ── Retrieval Strategy ───────────────────────────────────

export function listRetrievalStrategies(selection: SelectionState) {
  return requestJson<RetrievalStrategy[]>(
    `/api/v1/workspaces/${selection.workspaceId}/retrieval-strategies`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: scopedHeaders(selection),
    }
  )
}

export function createRetrievalStrategy(
  selection: SelectionState,
  data: {
    name: string
    description?: string
    retrievalType?: string
    topK?: number
    scoreThreshold?: number
    enableRerank?: boolean
    enableQueryRewrite?: boolean
    enableTextSearch?: boolean
    enableVectorSearch?: boolean
    rerankModel?: string
    vectorWeight?: number
    keywordWeight?: number
  }
) {
  return requestJson<RetrievalStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/retrieval-strategies`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: scopedHeaders(selection),
      bodyJson: data,
    }
  )
}

export function getRetrievalStrategy(selection: SelectionState, id: string) {
  return requestJson<RetrievalStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/retrieval-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: scopedHeaders(selection),
    }
  )
}

export function updateRetrievalStrategy(
  selection: SelectionState,
  id: string,
  data: {
    name: string
    description?: string
  }
) {
  return requestJson<RetrievalStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/retrieval-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'PUT',
      headers: scopedHeaders(selection),
      bodyJson: data,
    }
  )
}

export function deleteRetrievalStrategy(selection: SelectionState, id: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/retrieval-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'DELETE',
      headers: scopedHeaders(selection),
    }
  )
}

// ── Model Strategy ───────────────────────────────────────

export function listModelStrategies(selection: SelectionState) {
  return requestJson<ModelStrategy[]>(
    `/api/v1/workspaces/${selection.workspaceId}/model-strategies`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: scopedHeaders(selection),
    }
  )
}

export function createModelStrategy(
  selection: SelectionState,
  data: {
    name: string
    description?: string
    temperature?: number
    maxTokens?: number
    topP?: number
    frequencyPenalty?: number
    presencePenalty?: number
  }
) {
  return requestJson<ModelStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/model-strategies`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: scopedHeaders(selection),
      bodyJson: data,
    }
  )
}

export function getModelStrategy(selection: SelectionState, id: string) {
  return requestJson<ModelStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/model-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: scopedHeaders(selection),
    }
  )
}

export function updateModelStrategy(
  selection: SelectionState,
  id: string,
  data: {
    name: string
    description?: string
    temperature?: number
    maxTokens?: number
    topP?: number
    frequencyPenalty?: number
    presencePenalty?: number
  }
) {
  return requestJson<ModelStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/model-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'PUT',
      headers: scopedHeaders(selection),
      bodyJson: data,
    }
  )
}

export function deleteModelStrategy(selection: SelectionState, id: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/model-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'DELETE',
      headers: scopedHeaders(selection),
    }
  )
}

// ── Tool Strategy ────────────────────────────────────────

export function listToolStrategies(selection: SelectionState) {
  return requestJson<ToolStrategy[]>(
    `/api/v1/workspaces/${selection.workspaceId}/tool-strategies`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: scopedHeaders(selection),
    }
  )
}

export function createToolStrategy(
  selection: SelectionState,
  data: {
    name: string
    description?: string
    maxConcurrentCalls?: number
    timeoutSeconds?: number
    retryCount?: number
    fallbackEnabled?: boolean
    allowedTools?: string[]
  }
) {
  return requestJson<ToolStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/tool-strategies`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: scopedHeaders(selection),
      bodyJson: data,
    }
  )
}

export function getToolStrategy(selection: SelectionState, id: string) {
  return requestJson<ToolStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/tool-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: scopedHeaders(selection),
    }
  )
}

export function updateToolStrategy(
  selection: SelectionState,
  id: string,
  data: {
    name: string
    description?: string
  }
) {
  return requestJson<ToolStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/tool-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'PUT',
      headers: scopedHeaders(selection),
      bodyJson: data,
    }
  )
}

export function deleteToolStrategy(selection: SelectionState, id: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/tool-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'DELETE',
      headers: scopedHeaders(selection),
    }
  )
}

// ── Guardrail Strategy ───────────────────────────────────

export function listGuardrailStrategies(selection: SelectionState) {
  return requestJson<GuardrailStrategy[]>(
    `/api/v1/workspaces/${selection.workspaceId}/guardrail-strategies`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: scopedHeaders(selection),
    }
  )
}

export function createGuardrailStrategy(
  selection: SelectionState,
  data: {
    name: string
    description?: string
    inputValidationEnabled?: boolean
    outputValidationEnabled?: boolean
    piiDetectionEnabled?: boolean
    piiMaskingEnabled?: boolean
    promptInjectionDetection?: boolean
    maxInputLength?: number
    maxOutputLength?: number
  }
) {
  return requestJson<GuardrailStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/guardrail-strategies`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: scopedHeaders(selection),
      bodyJson: data,
    }
  )
}

export function getGuardrailStrategy(selection: SelectionState, id: string) {
  return requestJson<GuardrailStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/guardrail-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: scopedHeaders(selection),
    }
  )
}

export function updateGuardrailStrategy(
  selection: SelectionState,
  id: string,
  data: {
    name: string
    description?: string
  }
) {
  return requestJson<GuardrailStrategy>(
    `/api/v1/workspaces/${selection.workspaceId}/guardrail-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'PUT',
      headers: scopedHeaders(selection),
      bodyJson: data,
    }
  )
}

export function deleteGuardrailStrategy(selection: SelectionState, id: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/guardrail-strategies/${id}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'DELETE',
      headers: scopedHeaders(selection),
    }
  )
}
