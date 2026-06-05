import { runtimeConfig } from '@/common/runtime-config'
import type { RetrievalChunk, SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'

/**
 * 知识库检索请求参数（与后端SearchRequest一致）
 */
export interface RetrievalRequest {
  kbId: string
  query: string
  topK?: number
  scoreThreshold?: number
  enableQueryRewrite?: boolean
  enableRerank?: boolean
  enableTextSearch?: boolean
  enableVectorSearch?: boolean
  rerankModel?: string
  vectorWeight?: number
  keywordWeight?: number
}

/**
 * 检索结果项（与后端RetrievalResultItem一致）
 */
interface RetrievalResultItem {
  documentId: string
  chunkId: string
  content: string
  score: number
}

/**
 * 引用项（与后端CitationItem一致）
 */
interface CitationItem {
  index: number
  documentId: string
  chunkId: string
  excerpt: string
}

/**
 * 后端搜索响应（与后端SearchResponse一致）
 */
interface BackendSearchResponse {
  rewrittenQuery?: string
  results?: RetrievalResultItem[]
  citations?: CitationItem[]
}

/**
 * 前端检索响应
 */
export interface RetrievalResponse {
  rewrittenQuery: string
  chunks: RetrievalChunk[]
  citations: CitationItem[]
}

/**
 * 检索知识库（tenant console）。
 * 后端端点：POST /api/v1/workspaces/${selection.workspaceId}/knowledge-bases/{kbId}/search
 */
export interface RetrievePayload {
  selection: { tenantId: string; workspaceId: string }
  kbId: string
  query: string
  topK?: number
  scoreThreshold?: number
  enableQueryRewrite?: boolean
  enableRerank?: boolean
  enableTextSearch?: boolean
  enableVectorSearch?: boolean
  rerankModel?: string
  vectorWeight?: number
  keywordWeight?: number
}

export async function retrieve(payload: RetrievePayload): Promise<RetrievalResponse> {
  const resp = await postSearchRequest(payload)
  return toRetrievalResponse(resp)
}

function searchUrl(payload: RetrievePayload): string {
  return `/api/v1/workspaces/${payload.selection.workspaceId}/knowledge-bases/${payload.kbId}/search`
}

function postSearchRequest(payload: RetrievePayload): Promise<BackendSearchResponse> {
  return requestJson<BackendSearchResponse>(searchUrl(payload), {
    baseUrl: runtimeConfig.retrievalApiBase,
    method: 'POST',
    headers: scopedHeaders(payload.selection),
    bodyJson: buildRequestBody(payload),
  })
}

function buildRequestBody(payload: RetrievePayload): RetrievalRequest {
  const body: RetrievalRequest = { query: payload.query, rerankModel: payload.rerankModel }
  applyNumericDefaults(body, payload)
  applyFeatureFlags(body, payload)
  return body
}

function applyNumericDefaults(body: RetrievalRequest, payload: RetrievePayload): void {
  body.topK = payload.topK ?? 5
  body.scoreThreshold = payload.scoreThreshold ?? 0.0
  body.vectorWeight = payload.vectorWeight ?? 0.7
  body.keywordWeight = payload.keywordWeight ?? 0.3
}

function applyFeatureFlags(body: RetrievalRequest, payload: RetrievePayload): void {
  body.enableQueryRewrite = payload.enableQueryRewrite ?? false
  body.enableRerank = payload.enableRerank ?? false
  body.enableTextSearch = payload.enableTextSearch ?? false
  body.enableVectorSearch = payload.enableVectorSearch ?? true
}

function toRetrievalResponse(resp: BackendSearchResponse): RetrievalResponse {
  const results = resp.results || []
  return {
    rewrittenQuery: resp.rewrittenQuery || '',
    chunks: results.map(toRetrievalChunk),
    citations: resp.citations || [],
  }
}

function toRetrievalChunk(r: RetrievalResultItem): RetrievalChunk {
  return {
    docId: r.documentId,
    chunkIndex: r.chunkId,
    content: r.content,
    score: r.score,
  }
}
