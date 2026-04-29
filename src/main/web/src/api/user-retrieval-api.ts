import { runtimeConfig } from '@/common/runtime-config'
import { requestJson } from './http'

export interface KnowledgeBase {
  id: string
  name: string
  description?: string
}

export interface RetrievalResult {
  id: string
  content: string
  score: number
  source?: string
  documentName?: string
  metadata?: Record<string>
}

export interface RetrievalResponse {
  results: RetrievalResult[]
  query: string
  totalResults: number
}

export function listKnowledgeBases() {
  return requestJson<KnowledgeBase[]>('/api/v1/user/knowledge-bases', {
    baseUrl: runtimeConfig.userApiBase,
  })
}

export function searchKnowledge(knowledgeBaseId: string, query: string, topK = 10) {
  return requestJson<RetrievalResponse>('/api/v1/user/retrieval/search', {
    baseUrl: runtimeConfig.userApiBase,
    method: 'POST',
    bodyJson: { knowledgeBaseId, query, topK },
  })
}
