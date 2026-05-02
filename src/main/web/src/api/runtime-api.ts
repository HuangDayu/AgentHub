import { runtimeConfig } from '../common/runtime-config'
import type { ChatMessage, ChatSession, SelectionState } from '../domain/types'
import { scopedHeaders } from '../services/workspace-service'
import { requestJson } from './http'

// ── Sessions ─────────────────────────────────────────────

/**
 * Create a new session for a specific agent.
 * Backend endpoint: POST /api/v1/workspaces/${selection.workspaceId}/agents/{agentId}/sessions
 * Backend returns { id, agentId, createdAt } → we map id → sessionId for frontend type.
 */
export function createSession(selection: SelectionState, agentId: string) {
  return requestJson<ChatSession>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions`, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
  }).then((raw) => ({
    sessionId: (raw as any).id ?? raw.sessionId,
    agentId: raw.agentId ?? agentId,
    createdAt: raw.createdAt,
  }))
}

/**
 * List all sessions for a specific agent.
 * Backend endpoint: GET /api/v1/workspaces/${selection.workspaceId}/agents/{agentId}/sessions
 */
export function listSessions(selection: SelectionState, agentId: string) {
  return requestJson<ChatSession[]>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions`, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  }).then((items) =>
    items.map((raw: any) => ({
      sessionId: raw.id ?? raw.sessionId,
      agentId: raw.agentId ?? agentId,
      createdAt: raw.createdAt,
    })),
  )
}

// ── Messages ─────────────────────────────────────────────

/**
 * List messages for a session.
 * Backend endpoint: GET /api/v1/workspaces/${selection.workspaceId}/agents/{agentId}/sessions/{sessionId}/messages
 */
export function listMessages(selection: SelectionState, agentId: string, sessionId: string) {
  return requestJson<ChatMessage[]>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions/${sessionId}/messages`, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  }).then((items) =>
    items.map((raw: any) => ({
      messageId: raw.id ?? raw.messageId,
      sessionId: raw.sessionId,
      role: raw.role as 'user' | 'assistant' | 'system',
      content: raw.content,
      createdAt: raw.createdAt,
    })),
  )
}

/**
 * Send a message to a session.
 * Backend endpoint: POST /api/v1/workspaces/${selection.workspaceId}/agents/{agentId}/sessions/{sessionId}/messages
 */
export function sendMessage(selection: SelectionState, agentId: string, sessionId: string, content: string) {
  return requestJson<ChatMessage>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions/${sessionId}/messages`, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: { content },
  }).then((raw: any) => ({
    messageId: raw.id ?? raw.messageId,
    sessionId: raw.sessionId,
    role: raw.role as 'user' | 'assistant' | 'system',
    content: raw.content,
    createdAt: raw.createdAt,
  }))
}

// ── SSE Streaming ────────────────────────────────────────

/**
 * 流式发送消息并通过 EventSource / ReadableStream 接收 SSE。
 * Backend endpoint: POST /api/v1/workspaces/${selection.workspaceId}/agents/{agentId}/sessions/{sessionId}/messages/stream
 * 回调 onToken 接收每个增量 token，onDone 在流结束时调用。
 */
export async function sendMessageStream(
  selection: SelectionState,
  agentId: string,
  sessionId: string,
  content: string,
  callbacks: {
    onToken: (token: string) => void
    onDone: () => void
    onError: (error: Error) => void
  },
): Promise<void> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    Accept: 'text/event-stream',
  }
  if (selection.tenantId) headers['X-Tenant-Id'] = selection.tenantId
  if (selection.workspaceId) headers['X-Workspace-Id'] = selection.workspaceId

  // Auth
  try {
    const token = localStorage.getItem('agenthub_access_token')
    if (token) headers['Authorization'] = `Bearer ${token}`
  } catch {
    // localStorage unavailable
  }

  const url = new URL(
    `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions/${sessionId}/messages/stream`,
    runtimeConfig.runtimeApiBase,
  )

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify({ content: content }),
    })

    if (!response.ok) {
      const text = await response.text()
      throw new Error(text || `流式请求失败：${response.status}`)
    }

    if (!response.body) {
      throw new Error('浏览器不支持 ReadableStream')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() ?? ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed) continue
        if (trimmed.startsWith('data:')) {
          const data = trimmed.slice(5).trim()
          if (data === '[DONE]') {
            callbacks.onDone()
            return
          }
          callbacks.onToken(data)
        } else if (trimmed.startsWith('event: done')) {
          callbacks.onDone()
          return
        }
      }
    }

    callbacks.onDone()
  } catch (error) {
    callbacks.onError(error instanceof Error ? error : new Error(String(error)))
  }
}
