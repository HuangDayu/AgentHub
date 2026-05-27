import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
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
  errorSpanCount?: number
  slowestSpanId?: string
  slowestSpanName?: string
  slowestLatencyNs?: number
}

export interface RuntimeSpanSummary {
  spanId: string
  parentSpanId?: string
  name?: string
  latencyNs?: number
  statusCode?: number
  status?: string
  model?: string
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

/**
 * Span 树节点.
 * 包含 Span 所有字段 + 嵌套子节点列表。
 */
export interface SpanTreeNode {
  id: string
  spanId: string
  traceId: string
  parentSpanId?: string
  name: string
  kind: string
  startTimeUnixNano: string
  endTimeUnixNano: string
  latencyNs: number
  attributes?: Record<string, any>
  events?: Array<{ name: string; timeUnixNano: string; attributes?: Record<string, any> }>
  statusCode?: number
  statusMessage?: string
  model?: string
  inputTokens?: number
  outputTokens?: number
  totalTokens?: number
  runId?: string
  agentId?: string
  createdAt: string
  children: SpanTreeNode[]
}

export interface RuntimeDataView {
  runs: RuntimeRun[]
  selectedRun?: RuntimeRun
  trace: RuntimeTrace
  spanTree: SpanTreeNode[]
  errorSpans: RuntimeSpanSummary[]
  slowSpans: RuntimeSpanSummary[]
  modelInvocationData: ModelInvocationData
}

export function emptyRuntimeDataView(): RuntimeDataView {
  return {
    runs: [],
    spanTree: [],
    errorSpans: [],
    slowSpans: [],
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
