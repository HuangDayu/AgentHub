import { runtimeConfig } from '@/common/runtime-config'
import { requestJson } from './http'

export interface Agent {
  id: string
  name: string
  description?: string
}

export interface Session {
  id: string
  agentId: string
  agentName: string
  title: string
  createdAt: string
  updatedAt: string
}

export interface Message {
  id: string
  sessionId: string
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt: string
}

export interface CreateSessionRequest {
  agentId: string
  title?: string
}

export interface SendMessageRequest {
  content: string
}

/**
 * List all agents.
 * Backend endpoint: GET /api/v1/agents
 */
export function listAgents() {
  return requestJson<Agent[]>('/api/v1/agents', {
    baseUrl: runtimeConfig.userApiBase,
  })
}

/**
 * List all sessions for a specific agent.
 * Backend endpoint: GET /api/v1/agents/{agentId}/sessions
 * Note: This function now requires agentId parameter.
 */
export function listSessions(agentId: string) {
  return requestJson<Session[]>(`/api/v1/agents/${agentId}/sessions`, {
    baseUrl: runtimeConfig.userApiBase,
  })
}

/**
 * Create a new session for a specific agent.
 * Backend endpoint: POST /api/v1/agents/{agentId}/sessions
 */
export function createSession(req: CreateSessionRequest) {
  return requestJson<Session>(`/api/v1/agents/${req.agentId}/sessions`, {
    baseUrl: runtimeConfig.userApiBase,
    method: 'POST',
  })
}

/**
 * List messages for a session.
 * Backend endpoint: GET /api/v1/agents/{agentId}/sessions/{sessionId}/messages
 * Note: This function now requires agentId parameter.
 */
export function listMessages(agentId: string, sessionId: string) {
  return requestJson<Message[]>(`/api/v1/agents/${agentId}/sessions/${sessionId}/messages`, {
    baseUrl: runtimeConfig.userApiBase,
  })
}

/**
 * Delete a session.
 * Note: Backend SessionController does not have a delete endpoint.
 * This function is kept for compatibility but may need backend implementation.
 */
export function deleteSession(agentId: string, sessionId: string) {
  return requestJson<void>(`/api/v1/agents/${agentId}/sessions/${sessionId}`, {
    baseUrl: runtimeConfig.userApiBase,
    method: 'DELETE',
  })
}

/**
 * Build stream URL for fetch-based streaming.
 * Backend endpoint: POST /api/v1/agents/{agentId}/sessions/{sessionId}/messages/stream
 */
export function buildStreamUrl(agentId: string, sessionId: string, token: string | null): string {
  const base = (runtimeConfig.userApiBase || '').replace(/\/$/, '')
  return `${base}/api/v1/agents/${agentId}/sessions/${sessionId}/messages/stream`
}
