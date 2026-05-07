<template>
  <section class="grid">
    <div class="page-header">
      <div>
        <h2>Agent 对话</h2>
        <p class="muted">与 Agent 进行实时对话，支持流式响应。</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <!-- Agent 选择与会话管理 -->
      <article class="panel stack">
        <div class="toolbar">
          <select v-model="selectedAgentId" @change="onAgentChange">
            <option disabled value="">选择 Agent</option>
            <option v-for="agent in agents" :key="agent.id" :value="agent.id">
              {{ agent.name }}
            </option>
          </select>
          <button class="secondary" :disabled="!selectedAgentId" @click="createNewSession">新建会话</button>
          <select v-if="sessions.length" v-model="selectedSessionId" @change="onSessionChange">
            <option disabled value="">选择会话</option>
            <option v-for="session in sessions" :key="session.sessionId" :value="session.sessionId">
              {{ session.sessionId.slice(0, 8) }}...（{{ formatDateTime(session.createdAt) }}）
            </option>
          </select>
          <button class="ghost" type="button" @click="loadAgents">刷新 Agent</button>
        </div>
      </article>

      <!-- 对话区 -->
      <article class="chat-panel">
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="!messages.length && !selectedSessionId" class="empty-state">
            选择 Agent 并创建会话开始对话。
          </div>
          <div v-else-if="!messages.length" class="empty-state">
            会话暂无消息，发送一条消息试试。
          </div>
          <div
            v-for="msg in messages"
            :key="msg.messageId"
            :class="['message', msg.role]"
          >
            <div class="message-role">{{ msg.role === 'user' ? '用户' : '助手' }}</div>
            <div class="message-content">
              <MarkdownRenderer v-if="msg.role === 'assistant'" :content="msg.content" />
              <template v-else>{{ msg.content }}</template>
            </div>
          </div>
          <!-- 流式输出实时显示 -->
          <div v-if="streamingContent" class="message assistant">
            <div class="message-role">助手</div>
            <div class="message-content">
              <MarkdownRenderer :content="streamingContent" />
              <span class="cursor">▊</span>
            </div>
          </div>
        </div>
        <!-- 输入区 -->
        <form class="chat-input" @submit.prevent="handleSend">
          <textarea
            v-model="inputContent"
            rows="2"
            placeholder="输入消息..."
            :disabled="!selectedSessionId || sending"
          ></textarea>
          <button class="primary" type="submit" :disabled="!selectedSessionId || !inputContent.trim() || sending">
            {{ sending ? '发送中...' : '发送' }}
          </button>
          <label class="toggle-label">
            <input type="checkbox" v-model="useStream" />
            流式响应
          </label>
        </form>
      </article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { listAgents } from '@/api/agent-api'
import { createSession, listMessages, listSessions, sendMessage, sendMessageStream } from '@/api/runtime-api'
import { formatDateTime } from '@/common/format'
import type { Agent, ChatMessage, ChatSession } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const route = useRoute()
const store = useWorkspaceStore()
const error = ref('')

// Agent
const agents = ref<Agent[]>([])
const selectedAgentId = ref('')

// Session
const sessions = ref<ChatSession[]>([])
const selectedSessionId = ref('')

// Messages
const messages = ref<ChatMessage[]>([])
const messagesContainer = ref<HTMLElement | null>(null)

// Input
const inputContent = ref('')
const sending = ref(false)
const useStream = ref(true)
const streamingContent = ref('')

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

onMounted(async () => {
  await loadAgents()
  // 从 query 参数自动选择 Agent
  const agentId = route.query.agentId as string
  if (agentId && agents.value.some((a) => a.id === agentId)) {
    selectedAgentId.value = agentId
    await onAgentChange()
  }
})

watch(() => [store.tenantId, store.workspaceId], loadAgents)

async function loadAgents() {
  if (!selectionReady.value) {
    agents.value = []
    return
  }
  try {
    agents.value = await listAgents({ tenantId: store.tenantId, workspaceId: store.workspaceId })
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载 Agent 失败'
  }
}

async function onAgentChange() {
  sessions.value = []
  selectedSessionId.value = ''
  messages.value = []
  if (!selectedAgentId.value) return
  try {
    const allSessions = await listSessions(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      selectedAgentId.value,
    )
    sessions.value = allSessions
    if (allSessions.length) {
      selectedSessionId.value = allSessions[0].sessionId
      await loadMessages()
    }
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载会话失败'
  }
}

async function onSessionChange() {
  await loadMessages()
}

async function createNewSession() {
  if (!selectedAgentId.value) return
  try {
    const session = await createSession(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      selectedAgentId.value,
    )
    sessions.value.unshift(session)
    selectedSessionId.value = session.sessionId
    messages.value = []
    // 新会话默认无消息，不需要加载消息列表
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '创建会话失败'
    // 确保不会卡在 loading 状态
    selectedSessionId.value = ''
    messages.value = []
  }
}

async function loadMessages() {
  if (!selectedSessionId.value || !selectedAgentId.value) {
    messages.value = []
    return
  }
  try {
    messages.value = await listMessages(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      selectedAgentId.value,
      selectedSessionId.value,
    )
    scrollToBottom()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载消息失败'
  }
}

async function handleSend() {
  if (!selectedSessionId.value || !selectedAgentId.value || !inputContent.value.trim() || sending.value) return

  const content = inputContent.value.trim()
  inputContent.value = ''
  sending.value = true
  error.value = ''

  // 先显示用户消息
  const userMsg: ChatMessage = {
    messageId: `temp-${Date.now()}`,
    sessionId: selectedSessionId.value,
    role: 'user',
    content,
    createdAt: new Date().toISOString(),
  }
  messages.value.push(userMsg)
  scrollToBottom()

  try {
    if (useStream.value) {
      streamingContent.value = ''
      // 添加超时保护，防止 SSE 永远挂起
      const streamPromise = sendMessageStream(
        { tenantId: store.tenantId, workspaceId: store.workspaceId },
        selectedAgentId.value,
        selectedSessionId.value,
        content,
        {
          onToken(token) {
            streamingContent.value += token
            scrollToBottom()
          },
          onDone() {
            if (streamingContent.value) {
              messages.value.push({
                messageId: `stream-${Date.now()}`,
                sessionId: selectedSessionId.value,
                role: 'assistant',
                content: streamingContent.value,
                createdAt: new Date().toISOString(),
              })
            }
            streamingContent.value = ''
            scrollToBottom()
          },
          onError(err) {
            error.value = err.message
            streamingContent.value = ''
          },
        },
      )
      // 30秒超时兜底
      await Promise.race([
        streamPromise,
        new Promise((_, reject) => setTimeout(() => reject(new Error('流式响应超时')), 300_000)),
      ])
    } else {
      const reply = await sendMessage(
        { tenantId: store.tenantId, workspaceId: store.workspaceId },
        selectedAgentId.value,
        selectedSessionId.value,
        content,
      )
      messages.value.push(reply)
      scrollToBottom()
    }
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '发送失败'
    streamingContent.value = ''
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.chat-panel {
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(22, 33, 50, 0.08);
  box-shadow: 0 24px 40px rgba(40, 54, 79, 0.08);
  backdrop-filter: blur(14px);
  border-radius: 22px;
  display: grid;
  grid-template-rows: 1fr auto;
  height: 60vh;
  overflow: hidden;
}

.chat-messages {
  overflow-y: auto;
  padding: 20px;
  display: grid;
  gap: 12px;
  align-content: start;
}

.message {
  border-radius: 16px;
  padding: 14px 18px;
  max-width: 80%;
}

.message.user {
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: #fff;
  justify-self: end;
}

.message.assistant {
  background: rgba(248, 250, 255, 0.95);
  border: 1px solid rgba(38, 66, 102, 0.1);
  justify-self: start;
}

.message-role {
  font-size: 0.75em;
  opacity: 0.7;
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.message-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

.cursor {
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid rgba(22, 33, 50, 0.08);
  align-items: end;
}

.chat-input textarea {
  flex: 1;
  resize: none;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(38, 66, 102, 0.14);
  background: rgba(248, 250, 255, 0.92);
  font: inherit;
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  font-size: 0.85em;
  color: #5f6878;
}
</style>


