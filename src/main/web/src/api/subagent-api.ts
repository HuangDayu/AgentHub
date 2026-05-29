import { runtimeConfig } from '@/common/runtime-config'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'
import type { Subagent, Subsession } from '@/types/subagent'

export interface Selection {
  tenantId: string
  workspaceId: string
}

// ── Subagent API ─────────────────────────────────────────

export async function listSubagents(selection: Selection, agentId: string): Promise<Subagent[]> {
  return requestJson<Subagent[]>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/subagents`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
}

export async function createSubagent(
  selection: Selection,
  agentId: string,
  data: { name: string; description?: string; systemPrompt?: string; modelConfigId?: string; sessionId?: string; parentSubagentId?: string }
): Promise<Subagent> {
  return requestJson<Subagent>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/subagents`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: data,
  })
}

export async function getSubagent(selection: Selection, agentId: string, subagentId: string): Promise<Subagent> {
  return requestJson<Subagent>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/subagents/${subagentId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
}

export async function deleteSubagent(selection: Selection, agentId: string, subagentId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/subagents/${subagentId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: scopedHeaders(selection),
  })
}

// ── Subsession API ───────────────────────────────────────

export async function listSubsessions(selection: Selection, agentId: string, sessionId: string): Promise<Subsession[]> {
  return requestJson<Subsession[]>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions/${sessionId}/subsessions`, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
}

export async function createSubsession(
  selection: Selection,
  agentId: string,
  sessionId: string,
  data: { subagentId: string; name?: string }
): Promise<Subsession> {
  return requestJson<Subsession>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions/${sessionId}/subsessions`, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: data,
  })
}

export async function closeSubsession(selection: Selection, agentId: string, subsessionId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/subsessions/${subsessionId}/close`, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
  })
}
