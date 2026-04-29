import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'

export interface PromptTemplate {
  id: string
  name: string
  description?: string
  category: string
  content: string
  variables?: Variable[]
  isActive: boolean
  createdAt?: string
  updatedAt?: string
}

export interface Variable {
  name: string
  description?: string
  defaultValue?: string
  required: boolean
}

export function listPromptTemplates(selection: SelectionState, category?: string) {
  const url = category
    ? `/api/v1/workspaces/${selection.workspaceId}/prompt-templates?category=${category}`
    : `/api/v1/workspaces/${selection.workspaceId}/prompt-templates`
  return requestJson<PromptTemplate[]>(
    url,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function createPromptTemplate(selection: SelectionState, data: Partial<PromptTemplate>) {
  return requestJson<PromptTemplate>(
    `/api/v1/workspaces/${selection.workspaceId}/prompt-templates`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: scopedHeaders(selection), bodyJson: data }
  )
}

export function getPromptTemplate(selection: SelectionState, id: string) {
  return requestJson<PromptTemplate>(
    `/api/v1/workspaces/${selection.workspaceId}/prompt-templates/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: scopedHeaders(selection) }
  )
}

export function updatePromptTemplate(selection: SelectionState, id: string, data: Partial<PromptTemplate>) {
  return requestJson<PromptTemplate>(
    `/api/v1/workspaces/${selection.workspaceId}/prompt-templates/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'PUT', headers: scopedHeaders(selection), bodyJson: data }
  )
}

export function deletePromptTemplate(selection: SelectionState, id: string) {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/prompt-templates/${id}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'DELETE', headers: scopedHeaders(selection) }
  )
}
