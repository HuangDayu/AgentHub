import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import type { Span } from '@/types/span'
import { requestJson } from './http'

export interface RuntimeRun {
  id: string
  agentId: string
  project?: string
  name?: string
  timestamp?: string
  pid?: number
  status?: string
  runDir?: string
}

export interface RuntimeTrace {
  runId: string
  startTimeUnixNano?: string
  endTimeUnixNano?: string
  latencyNs: number
  status: string
  spanCount: number
  totalTokens: number
}

export interface TokenStats {
  promptTokens: number
  completionTokens: number
  totalTokens: number
}

export interface ModelInvocationByModel {
  modelName: string
  invocations: number
}

export interface ModelTokenStats {
  modelName: string
  promptTokens: number
  completionTokens: number
  totalTokens: number
}

export interface ChatInvocationStats {
  modelInvocations: number
  avgTokens: TokenStats
  totalTokens: TokenStats
  modelInvocationsByModel: ModelInvocationByModel[]
  avgTokensByModel: ModelTokenStats[]
  totalTokensByModel: ModelTokenStats[]
}

export interface ModelInvocationData {
  modelInvocations: number
  chat: ChatInvocationStats
}

export interface RuntimeDataView {
  runs: RuntimeRun[]
  selectedRun?: RuntimeRun
  trace: RuntimeTrace
  spans: Span[]
  modelInvocationData: ModelInvocationData
}

export function emptyRuntimeDataView(): RuntimeDataView {
  return {
    runs: [],
    spans: [],
    trace: { runId: '', latencyNs: 0, status: 'PENDING', spanCount: 0, totalTokens: 0 },
    modelInvocationData: { modelInvocations: 0, chat: emptyChatStats() },
  }
}

export function loadRuntimeDataView(selection: SelectionState, agentId: string, sessionId: string) {
  const path = `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions/${sessionId}/data-view`
  return requestJson<RuntimeDataView>(path, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
}

function emptyChatStats(): ChatInvocationStats {
  return {
    modelInvocations: 0,
    avgTokens: emptyTokens(),
    totalTokens: emptyTokens(),
    modelInvocationsByModel: [],
    avgTokensByModel: [],
    totalTokensByModel: [],
  }
}

function emptyTokens(): TokenStats {
  return { promptTokens: 0, completionTokens: 0, totalTokens: 0 }
}
