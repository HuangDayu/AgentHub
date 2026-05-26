import { runtimeConfig } from '../common/runtime-config'
import type { ChatMessage, ChatSession, MessageRole, SelectionState, StreamMessage } from '../domain/types'
import { scopedHeaders } from '../services/workspace-service'
import { requestJson } from './http'

export interface ChatAttachment {
  fileName: string
  path: string
  contentType?: string
  size: number
}

// ── Sessions ─────────────────────────────────────────────

/**
 * Create a new session for a specific agent.
 * Backend endpoint: POST /api/v1/workspaces/${selection.workspaceId}/agents/{agentId}/sessions
 * Backend returns { id, agentId, name, createdAt } → we map id → sessionId for frontend type.
 */
export function createSession(selection: SelectionState, agentId: string, name?: string) {
  return requestJson<ChatSession>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions`, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: { name: name || null },
  }).then((raw) => ({
    sessionId: (raw as any).id ?? raw.sessionId,
    agentId: raw.agentId ?? agentId,
    name: (raw as any).name,
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
      name: raw.name,
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
      role: normalizeRole(raw.role),
      content: raw.content,
      createdAt: raw.createdAt,
    })),
  )
}

function normalizeRole(role: string): MessageRole {
  const upperRole = role?.toUpperCase()
  if (upperRole === 'USER' || upperRole === 'SYSTEM' || upperRole === 'TOOL') return upperRole
  return 'ASSISTANT'
}

/**
 * Send a message to a session.
 * Backend endpoint: POST /api/v1/workspaces/${selection.workspaceId}/agents/{agentId}/sessions/{sessionId}/messages
 */
export function sendMessage(selection: SelectionState, agentId: string, sessionId: string, content: string, filePaths: string[] = []) {
  return requestJson<any>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions/${sessionId}/messages`, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: { content, filePaths },
  }).then((raw: any) => {
    // 适配AssistantMessage对象
    const message: ChatMessage = {
      messageId: raw.id ?? `msg-${Date.now()}`,
      sessionId: sessionId,
      role: 'ASSISTANT',
      content: raw.text || raw.content || '',
      createdAt: new Date().toISOString(),
    }
    return message
  })
}

// ── SSE Streaming ────────────────────────────────────────

/**
 * 流式发送消息并通过 EventSource / ReadableStream 接收 SSE。
 * Backend endpoint: POST /api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions/${sessionId}/messages/stream
 * 回调 onMessage 接收每个消息对象，onDone 在流结束时调用。
 */
export async function sendMessageStream(
  selection: SelectionState,
  agentId: string,
  sessionId: string,
  content: string,
  filePaths: string[],
  callbacks: {
    onMessage: (message: StreamMessage) => void
    onDone: () => void
    onError: (error: Error) => void
  },
): Promise<void> {  const headers: Record<string, string> = {
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
    // 创建AbortController用于超时控制
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 600000) // 10分钟超时

    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify({ content, filePaths }),
      signal: controller.signal,
    })

    clearTimeout(timeoutId)

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
          // 解析Message对象并提取内容
          const streamMessage = parseStreamMessage(data)
          if (streamMessage) {
            callbacks.onMessage(streamMessage)
          }
        } else if (trimmed.startsWith('event: done')) {
          callbacks.onDone()
          return
        }
      }
    }

    callbacks.onDone()
  } catch (error) {
    if (error instanceof Error && error.name === 'AbortError') {
      callbacks.onError(new Error('请求超时，请稍后重试'))
    } else {
      callbacks.onError(error instanceof Error ? error : new Error(String(error)))
    }
  }
}

export async function uploadChatAttachments(
  selection: SelectionState,
  agentId: string,
  sessionId: string,
  files: File[],
): Promise<ChatAttachment[]> {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  const response = await fetch(buildAttachmentUrl(selection, agentId, sessionId), {
    method: 'POST',
    headers: scopedHeaders(selection),
    body: formData,
  })
  if (!response.ok) throw new Error(await response.text())
  return response.json()
}

function buildAttachmentUrl(selection: SelectionState, agentId: string, sessionId: string) {
  return new URL(
    `/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions/${sessionId}/attachments`,
    runtimeConfig.runtimeApiBase,
  ).toString()
}

/**
 * 解析流式消息对象
 */
function parseStreamMessage(data: string): StreamMessage | null {
  try {
    const message = JSON.parse(data)
    // 返回完整的消息对象
    return {
      messageType: message.messageType,
      text: message.text || message.content || '',
      toolCalls: message.toolCalls,
      responses: message.responses,
      metadata: message.metadata,
    }
  } catch {
    // 如果不是JSON，返回null
    return null
  }
}

/**
 * Delete a session.
 * Backend endpoint: DELETE /api/v1/workspaces/${selection.workspaceId}/agents/{agentId}/sessions/{sessionId}
 */
export function deleteSession(selection: SelectionState, agentId: string, sessionId: string) {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/agents/${agentId}/sessions/${sessionId}`, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'DELETE',
    headers: scopedHeaders(selection),
  })
}
