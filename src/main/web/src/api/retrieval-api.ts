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
export async function retrieve(payload: {
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
}): Promise<RetrievalResponse> {
  const resp = await requestJson<BackendSearchResponse>(
    `/api/v1/workspaces/${payload.selection.workspaceId}/knowledge-bases/${payload.kbId}/search`,
    {
      baseUrl: runtimeConfig.retrievalApiBase,
      method: 'POST',
      headers: scopedHeaders(payload.selection),
      bodyJson: {
        query: payload.query,
        topK: payload.topK ?? 5,
        scoreThreshold: payload.scoreThreshold ?? 0.0,
        enableQueryRewrite: payload.enableQueryRewrite ?? false,
        enableRerank: payload.enableRerank ?? false,
        enableTextSearch: payload.enableTextSearch ?? false,
        enableVectorSearch: payload.enableVectorSearch ?? true,
        rerankModel: payload.rerankModel,
        vectorWeight: payload.vectorWeight ?? 0.7,
        keywordWeight: payload.keywordWeight ?? 0.3,
      },
    },
  )
  const results = resp.results || []
  return {
    rewrittenQuery: resp.rewrittenQuery || '',
    chunks: results.map((r) => ({
      docId: r.documentId,
      chunkIndex: r.chunkId,
      content: r.content,
      score: r.score,
    })),
    citations: resp.citations || [],
  }
}
