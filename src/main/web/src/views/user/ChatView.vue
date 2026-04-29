<template>
  <section class="chat-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h3>对话列表</h3>
        <button class="ghost" type="button" @click="showNewSession = true">+ 新建</button>
      </div>

      <div v-if="showNewSession" class="new-session-form">
        <select v-model="newAgentId">
          <option value="" disabled>选择 Agent</option>
          <option v-for="agent in agents" :key="agent.id" :value="agent.id">
            {{ agent.name }}
          </option>
        </select>
        <input v-model="newTitle" placeholder="会话标题（可选）" />
        <div class="new-session-actions">
          <button class="primary" type="button" :disabled="!newAgentId" @click="createNewSession">创建</button>
          <button class="ghost" type="button" @click="showNewSession = false">取消</button>
        </div>
      </div>

      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: session.id === activeSessionId }"
          @click="selectSession(session.id)"
        >
          <div class="session-item-info">
            <strong>{{ session.title || '未命名对话' }}</strong>
            <span class="muted">{{ session.agentName }}</span>
          </div>
          <button class="ghost session-delete" type="button" title="删除会话" @click.stop="removeSession(session.id)">×</button>
        </div>
        <p v-if="sessions.length === 0" class="muted" style="text-align: center; padding: 20px;">暂无对话</p>
      </div>
    </aside>

    <main class="chat-main">
      <template v-if="activeSessionId">
        <div class="chat-messages" ref="messagesContainer">
          <div v-for="msg in messages" :key="msg.id" class="message" :class="msg.role">
            <div class="message-avatar">
              <span v-if="msg.role === 'user'">👤</span>
              <span v-else>🤖</span>
            </div>
            <div class="message-bubble">
              <div class="message-role">{{ msg.role === 'user' ? '你' : 'AI' }}</div>
              <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
              <div class="message-time muted">{{ formatTime(msg.createdAt) }}</div>
            </div>
          </div>
          <div v-if="streamingContent" class="message assistant">
            <div class="message-avatar"><span>🤖</span></div>
            <div class="message-bubble">
              <div class="message-role">AI</div>
              <div class="message-content" v-html="renderMarkdown(streamingContent)"></div>
              <span class="loading-spinner"></span>
            </div>
          </div>
        </div>

        <form class="chat-input-form" @submit.prevent="sendMessage">
          <div class="chat-input-row">
            <textarea
              v-model="inputContent"
              placeholder="输入你的问题..."
              rows="1"
              :disabled="isSending"
              @keydown.enter.exact.prevent="sendMessage"
            ></textarea>
            <button class="primary send-btn" type="submit" :disabled="isSending || !inputContent.trim()">
              {{ isSending ? '发送中' : '发送' }}
            </button>
          </div>
        </form>
      </template>

      <div v-else class="empty-state">
        <p>请选择一个对话或创建新对话开始聊天。</p>
      </div>
    </main>

    <p v-if="error" class="status chat-error">{{ error }}</p>
  </section>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import { runtimeConfig } from '@/common/runtime-config'
import {
  listAgents,
  listSessions,
  createSession,
  listMessages,
  deleteSession,
  type Agent,
  type Session,
  type Message,
} from '@/api/user-runtime-api'
import { relativeTime } from '@/common/format'

const route = useRoute()
const router = useRouter()

const agents = ref<Agent[]>([])
const sessions = ref<Session[]>([])
const activeSessionId = ref<string>('')
const activeAgentId = ref<string>('')
const messages = ref<Message[]>([])
const inputContent = ref('')
const isSending = ref(false)
const streamingContent = ref('')
const error = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const showNewSession = ref(false)
const newAgentId = ref('')
const newTitle = ref('')
let eventSource: EventSource | null = null

onMounted(async () => {
  try {
    agents.value = await listAgents()
  } catch {
    // ignore
  }
  // Load sessions for the first agent if available
  if (agents.value.length > 0) {
    activeAgentId.value = agents.value[0].id
    await loadSessions()
  }
  const sid = route.params.sessionId as string
  if (sid) {
    await selectSession(sid)
  }
})

watch(() => route.params.sessionId, async (sid) => {
  if (sid && typeof sid === 'string' && sid !== activeSessionId.value) {
    await selectSession(sid)
  }
})

async function loadSessions() {
  if (!activeAgentId.value) return
  try {
    sessions.value = await listSessions(activeAgentId.value)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载对话列表失败'
  }
}

async function selectSession(sessionId: string) {
  activeSessionId.value = sessionId
  error.value = ''
  streamingContent.value = ''
  abortStream()
  try {
    // Find the session to get its agentId
    const session = sessions.value.find(s => s.id === sessionId)
    if (session) {
      activeAgentId.value = session.agentId
    }
    if (!activeAgentId.value) {
      throw new Error('无法确定Agent ID')
    }
    messages.value = await listMessages(activeAgentId.value, sessionId)
    scrollToBottom()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载消息失败'
  }
}

async function createNewSession() {
  if (!newAgentId.value) return
  try {
    const session = await createSession({
      agentId: newAgentId.value,
      title: newTitle.value.trim() || undefined,
    })
    showNewSession.value = false
    newAgentId.value = ''
    newTitle.value = ''
    // Switch to the new agent and load its sessions
    activeAgentId.value = session.agentId
    await loadSessions()
    await selectSession(session.id)
    router.replace(`/user/chat/${session.id}`)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '创建会话失败'
  }
}

async function removeSession(sessionId: string) {
  try {
    if (!activeAgentId.value) return
    await deleteSession(activeAgentId.value, sessionId)
    if (activeSessionId.value === sessionId) {
      activeSessionId.value = ''
      messages.value = []
      router.replace('/chat')
    }
    await loadSessions()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '删除会话失败'
  }
}

async function sendMessage() {
  const content = inputContent.value.trim()
  if (!content || !activeSessionId.value || isSending.value) return

  error.value = ''
  isSending.value = true
  streamingContent.value = ''

  // Add user message optimistically
  const userMsg: Message = {
    id: 'temp-user-' + Date.now(),
    sessionId: activeSessionId.value,
    role: 'user',
    content,
    createdAt: new Date().toISOString(),
  }
  messages.value.push(userMsg)
  inputContent.value = ''
  scrollToBottom()

  // Use fetch + ReadableStream instead of EventSource (which only supports GET)
  try {
    const token = localStorage.getItem('things_knowledge_access_token')
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
    }
    if (token) headers['Authorization'] = `Bearer ${token}`

    const streamPromise = (async () => {
      const response = await fetch(
        `${runtimeConfig.userApiBase}/api/v1/agents/${activeAgentId.value}/sessions/${activeSessionId.value}/messages/stream`,
        {
          method: 'POST',
          headers,
          body: JSON.stringify({ content }),
        }
      )
      if (!response.ok) {
        const text = await response.text()
        throw new Error(text || `请求失败: ${response.status}`)
      }
      if (!response.body) {
        throw new Error('浏览器不支持流式响应')
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
              finishStream()
              return
            }
            streamingContent.value += data
            scrollToBottom()
          } else if (trimmed.startsWith('event: done')) {
            finishStream()
            return
          }
        }
      }
      finishStream()
    })()

    // 30秒超时兜底
    await Promise.race([
      streamPromise,
      new Promise((_, reject) => setTimeout(() => reject(new Error('流式响应超时')), 30_000)),
    ])
  } catch (reason) {
    if (streamingContent.value) {
      finishStream()
    } else {
      error.value = reason instanceof Error ? reason.message : '发送失败'
      isSending.value = false
      abortStream()
    }
  }
}

function finishStream() {
  abortStream()
  if (streamingContent.value) {
    const aiMsg: Message = {
      id: 'temp-ai-' + Date.now(),
      sessionId: activeSessionId.value,
      role: 'assistant',
      content: streamingContent.value,
      createdAt: new Date().toISOString(),
    }
    messages.value.push(aiMsg)
    streamingContent.value = ''
  }
  isSending.value = false
  // Reload to get proper message IDs
  if (activeAgentId.value && activeSessionId.value) {
    listMessages(activeAgentId.value, activeSessionId.value).then((msgs) => {
      messages.value = msgs
      scrollToBottom()
    }).catch(() => {
      // ignore reload errors
    })
  }
}

function abortStream() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

function renderMarkdown(content: string): string {
  try {
    return marked.parse(content, { async: false, breaks: true }) as string
  } catch {
    return content.replace(/\n/g, '<br>')
  }
}

function formatTime(iso: string): string {
  return relativeTime(iso)
}
</script>

<style scoped>
.chat-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 0;
  min-height: calc(100vh - 200px);
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(22, 33, 50, 0.08);
  border-radius: 24px;
  box-shadow: 0 24px 40px rgba(40, 54, 79, 0.08);
  backdrop-filter: blur(14px);
  overflow: hidden;
  position: relative;
}

.sidebar {
  border-right: 1px solid rgba(22, 33, 50, 0.08);
  display: flex;
  flex-direction: column;
  background: rgba(248, 250, 255, 0.5);
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 12px;
  border-bottom: 1px solid rgba(22, 33, 50, 0.06);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 15px;
}

.new-session-form {
  padding: 12px 16px;
  display: grid;
  gap: 8px;
  border-bottom: 1px solid rgba(22, 33, 50, 0.06);
}

.new-session-form select,
.new-session-form input {
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(38, 66, 102, 0.14);
  background: rgba(255, 255, 255, 0.9);
  font-size: 13px;
}

.new-session-actions {
  display: flex;
  gap: 8px;
}

.new-session-actions .primary,
.new-session-actions .ghost {
  padding: 6px 12px;
  font-size: 13px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
}

.session-item:hover {
  background: rgba(38, 66, 102, 0.06);
}

.session-item.active {
  background: rgba(38, 66, 102, 0.1);
}

.session-item-info {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.session-item-info strong {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-item-info .muted {
  font-size: 12px;
}

.session-delete {
  padding: 4px 8px;
  font-size: 16px;
  opacity: 0;
  transition: opacity 0.15s;
}

.session-item:hover .session-delete {
  opacity: 1;
}

.chat-main {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(38, 66, 102, 0.08);
  display: grid;
  place-items: center;
  font-size: 16px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #264266, #3a8ad6);
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  background: rgba(248, 250, 255, 0.9);
  border: 1px solid rgba(22, 33, 50, 0.06);
  display: grid;
  gap: 4px;
}

.message.user .message-bubble {
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: #fff;
  border: none;
}

.message.user .message-bubble .muted {
  color: rgba(255, 255, 255, 0.7);
}

.message-role {
  font-size: 12px;
  font-weight: 600;
  color: #7a5f2b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.message.user .message-role {
  color: rgba(255, 255, 255, 0.8);
}

.message-content {
  font-size: 14px;
  line-height: 1.7;
}

.message-content :deep(p) {
  margin: 0 0 8px;
}

.message-content :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content :deep(code) {
  background: rgba(0, 0, 0, 0.06);
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 13px;
}

.message.user .message-content :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.message-content :deep(pre) {
  background: rgba(0, 0, 0, 0.05);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-content :deep(pre code) {
  background: none;
  padding: 0;
}

.message-time {
  font-size: 11px;
}

.chat-input-form {
  padding: 16px 20px;
  border-top: 1px solid rgba(22, 33, 50, 0.08);
}

.chat-input-row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input-row textarea {
  flex: 1;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(38, 66, 102, 0.14);
  background: rgba(248, 250, 255, 0.92);
  resize: none;
  max-height: 120px;
  line-height: 1.5;
}

.chat-input-row textarea:focus {
  outline: none;
  border-color: #3a8ad6;
  box-shadow: 0 0 0 3px rgba(58, 138, 214, 0.15);
}

.send-btn {
  flex-shrink: 0;
  padding: 12px 20px;
}

.chat-error {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(138, 59, 47, 0.1);
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
}

.loading-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(22, 33, 50, 0.08);
  border-top-color: #3a8ad6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  vertical-align: middle;
  margin-left: 8px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .chat-layout {
    grid-template-columns: 1fr;
    min-height: calc(100vh - 160px);
  }

  .sidebar {
    border-right: none;
    border-bottom: 1px solid rgba(22, 33, 50, 0.08);
    max-height: 200px;
  }

  .message {
    max-width: 90%;
  }
}
</style>


