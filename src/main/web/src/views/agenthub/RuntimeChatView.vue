<template>
  <section class="chat-page">
    <!-- 错误提示 -->
    <div v-if="error" class="error-toast fade-in">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <line x1="15" y1="9" x2="9" y2="15"/>
        <line x1="9" y1="9" x2="15" y2="15"/>
      </svg>
      <span>{{ error }}</span>
      <button @click="error = ''">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12"/>
        </svg>
      </button>
    </div>

    <div v-if="!selectionReady" class="empty-state scale-in">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
      </div>
      <p>请先选择工作区</p>
      <button class="primary" @click="goToWorkspace">前往设置</button>
    </div>
    
    <div v-else class="chat-layout">
      <!-- 左侧：Agent和会话管理 -->
      <aside :class="['sidebar', { 'collapsed': !sidebarExpanded }]">
        <div v-if="sidebarExpanded" class="sidebar-content">
          <div class="sidebar-header">
            <h3>会话管理</h3>
          </div>

          <!-- Agent 选择 -->
          <div class="sidebar-section">
            <div class="agent-selector">
              <select v-model="selectedAgentId" @change="onAgentChange">
                <option disabled value="">请选择 Agent</option>
                <option v-for="agent in agents" :key="agent.id" :value="agent.id">
                  {{ agent.name }}
                </option>
              </select>
              <button class="refresh-btn" @click="loadAgents" title="刷新Agent列表">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M23 4v6h-6M1 20v-6h6"/>
                  <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
                </svg>
              </button>
            </div>
            <div v-if="agents.length === 0 && !loadingAgents" class="no-agents">
              <p>暂无Agent</p>
              <button class="link-btn" @click="goToAgents">创建Agent</button>
            </div>
          </div>

          <!-- 会话列表 -->
          <div class="sidebar-section flex-grow">
            <div class="section-header">
              <label class="section-label">会话列表</label>
            </div>
            <div class="session-list">
              <div
                v-for="session in sessions"
                :key="session.sessionId"
                :class="['session-item', { 'active': session.sessionId === selectedSessionId }]"
              >
                <div class="session-icon" @click="selectSession(session.sessionId)">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                </div>
                <div class="session-info" @click="selectSession(session.sessionId)">
                  <div class="session-name">{{ session.name || session.sessionId.slice(0, 8) + '...' }}</div>
                  <div class="session-time">{{ formatDateTime(session.createdAt) }}</div>
                </div>
                <!-- 正在处理的图标 -->
                <div v-if="isSessionStreaming(session.sessionId)" class="streaming-indicator" title="正在处理中...">
                  <svg class="spinner-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
                  </svg>
                </div>
                <button class="delete-btn" @click.stop="handleDeleteSession(session.sessionId)" title="删除会话">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                </button>
              </div>
              <div v-if="!sessions.length && selectedAgentId" class="empty-sessions">
                <p>暂无会话</p>
                <p class="hint">发送消息将自动创建新会话</p>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 左侧：运行时管理 -->
      <aside :class="['runtime-sidebar', { 'collapsed': !runtimeExpanded }]">
        <div v-if="runtimeExpanded" class="runtime-content">
          <div class="sidebar-header">
            <h3>运行时管理</h3>
          </div>

          <!-- 运行时管理内容 - 待实现 -->
          <div class="runtime-placeholder">
            <div class="placeholder-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M12 6v6l4 2"/>
              </svg>
            </div>
            <p>运行时管理功能</p>
            <p class="hint">待后端接口实现</p>
          </div>
        </div>
      </aside>

      <!-- 右侧：对话区 -->
      <article class="chat-panel">
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="!messages.length && !selectedSessionId" class="empty-chat">
            <div class="empty-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
                <line x1="9" y1="9" x2="9.01" y2="9"/>
                <line x1="15" y1="9" x2="15.01" y2="9"/>
              </svg>
            </div>
            <p>选择或创建会话开始对话</p>
          </div>
          <div v-else-if="!messages.length" class="empty-chat">
            <div class="empty-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
              </svg>
            </div>
            <p>发送一条消息开始对话</p>
          </div>
          <div
            v-for="(msg, index) in messages"
            :key="msg.messageId"
            :class="['message', msg.role.toLowerCase(), msg.messageType?.toLowerCase(), {'fade-in': index === messages.length - 1}]"
          >
            <div class="message-avatar" :class="msg.role.toLowerCase()">
              <svg v-if="msg.role === 'USER'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </div>
            <div class="message-body">
              <div class="message-header">
                <span class="message-role">{{ getMessageRoleLabel(msg) }}</span>
                <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <div class="message-content">
                <!-- 用户消息 -->
                <template v-if="msg.role === 'USER'">{{ msg.content }}</template>
                
                <!-- 系统消息（错误消息） -->
                <template v-else-if="msg.role === 'SYSTEM'">
                  <div class="system-message">{{ msg.content }}</div>
                </template>
                
                <!-- 助手文本消息 -->
                <template v-else-if="msg.role === 'ASSISTANT' && (!msg.messageType || msg.messageType === 'ASSISTANT')">
                  <MarkdownRenderer v-if="msg.content" :content="msg.content" />
                  <!-- 工具调用 -->
                  <div v-if="msg.toolCalls && msg.toolCalls.length > 0" class="tool-calls-container">
                    <ToolCallMessage
                      v-for="toolCall in msg.toolCalls"
                      :key="toolCall.id"
                      :tool-call="toolCall"
                    />
                  </div>
                </template>
                
                <!-- 工具结果消息 -->
                <template v-else-if="msg.role === 'TOOL' || msg.messageType === 'TOOL'">
                  <div v-if="msg.toolResponses && msg.toolResponses.length > 0" class="tool-results-container">
                    <ToolResultMessage
                      v-for="response in msg.toolResponses"
                      :key="response.id"
                      :response="response"
                    />
                  </div>
                </template>
                
                <!-- 技能消息 -->
                <template v-else-if="msg.messageType === 'SKILL'">
                  <div v-if="msg.toolResponses && msg.toolResponses.length > 0" class="skill-container">
                    <SkillMessage
                      v-for="response in msg.toolResponses"
                      :key="response.id"
                      :response="response"
                    />
                  </div>
                </template>
              </div>
            </div>
          </div>
          <!-- 流式输出实时显示 -->
          <div v-if="streamingContent" class="message assistant fade-in">
            <div class="message-avatar assistant">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </div>
            <div class="message-body">
              <div class="message-header">
                <span class="message-role">助手</span>
                <span class="typing-indicator">正在输入...</span>
              </div>
              <div class="message-content">
                <MarkdownRenderer :content="streamingContent" />
                <span class="cursor">▊</span>
              </div>
            </div>
          </div>
        </div>
        <!-- 输入区 -->
        <form class="chat-input" @submit.prevent="handleSend">
          <div class="input-container">
            <textarea
              v-model="inputContent"
              rows="2"
              placeholder="输入消息... (Shift+Enter 换行)"
              :disabled="!selectedSessionId || sending"
              @keydown="handleKeydown"
            ></textarea>
            <div class="input-sidebar">
              <label class="toggle-label">
                <input type="checkbox" v-model="useStream" />
                <span class="toggle-slider"></span>
                <span class="toggle-text">流式</span>
              </label>
              <button class="primary send-btn" type="submit" :disabled="!selectedSessionId || !inputContent.trim() || sending">
                <svg v-if="!sending" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="btn-icon">
                  <line x1="22" y1="2" x2="11" y2="13"/>
                  <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                </svg>
                <span v-else class="spinner"></span>
                <span>{{ sending ? '发送中' : '发送' }}</span>
              </button>
            </div>
          </div>
        </form>
      </article>
    </div>

    <!-- 左下角：运行时管理展开/收起按钮 -->
    <button class="toggle-runtime-fab" @click="toggleRuntime" :title="runtimeExpanded ? '收起运行时管理' : '展开运行时管理'">
      <svg v-if="runtimeExpanded" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M15 18l-6-6 6-6"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M9 18l6-6-6-6"/>
      </svg>
    </button>

    <!-- 左下角：会话管理展开/收起按钮 -->
    <button class="toggle-sidebar-fab" @click="toggleSidebar" :title="sidebarExpanded ? '收起会话管理' : '展开会话管理'">
      <svg v-if="sidebarExpanded" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M15 18l-6-6 6-6"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M9 18l6-6-6-6"/>
      </svg>
    </button>

    <!-- 右下角：新增会话按钮 -->
    <button class="new-session-fab" @click="createNewSession" :disabled="!selectedAgentId" title="新建会话">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 5v14M5 12h14"/>
      </svg>
    </button>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { listAgents } from '@/api/agent-api'
import { createSession, deleteSession, listMessages, listSessions, sendMessage, sendMessageStream } from '@/api/runtime-api'
import { formatDateTime } from '@/common/format'
import type { Agent, ChatMessage, ChatSession, StreamMessage } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import ToolCallMessage from '@/components/ToolCallMessage.vue'
import ToolResultMessage from '@/components/ToolResultMessage.vue'
import SkillMessage from '@/components/SkillMessage.vue'

const router = useRouter()
const store = useWorkspaceStore()
const error = ref('')

// Sidebar state
const sidebarExpanded = ref(true)
const runtimeExpanded = ref(false)

// Agent
const agents = ref<Agent[]>([])
const selectedAgentId = ref('')
const loadingAgents = ref(false)

// Session
const sessions = ref<ChatSession[]>([])
const selectedSessionId = ref('')
const pendingSessionName = ref('') // 临时会话的名称，用于创建时使用

// Messages - 为每个会话维护独立的消息列表
const sessionMessages = new Map<string, ChatMessage[]>()
const messages = ref<ChatMessage[]>([])
const messagesContainer = ref<HTMLElement | null>(null)

// Input
const inputContent = ref('')
const sending = ref(false)
const useStream = ref(true)
const streamingContent = ref('')

// 流消息状态管理 - 为每个会话维护独立的流消息状态
const sessionStreamingStates = new Map<string, {
  content: string
  isStreaming: boolean
  pendingMessages: ChatMessage[] // 暂存正在进行的stream的消息（用户消息 + 助手消息）
  abortController?: AbortController
}>()

// 获取当前会话的流消息状态
function getCurrentStreamingState() {
  const sessionId = selectedSessionId.value
  if (!sessionId) return null

  if (!sessionStreamingStates.has(sessionId)) {
    sessionStreamingStates.set(sessionId, {
      content: '',
      isStreaming: false
    })
  }
  return sessionStreamingStates.get(sessionId)!
}

// 检查某个会话是否正在流式处理
function isSessionStreaming(sessionId: string): boolean {
  const state = sessionStreamingStates.get(sessionId)
  return state?.isStreaming || false
}

// 监听selectedSessionId变化，恢复流消息状态和消息列表
watch(selectedSessionId, (newSessionId) => {
  if (newSessionId) {
    // 恢复消息列表（所有消息都在sessionMessages中）
    const msgs = sessionMessages.get(newSessionId) || []
    messages.value = [...msgs]

    // 恢复流消息状态
    const state = sessionStreamingStates.get(newSessionId)
    if (state) {
      streamingContent.value = state.content
      sending.value = state.isStreaming
    } else {
      streamingContent.value = ''
      sending.value = false
    }
  } else {
    messages.value = []
    streamingContent.value = ''
    sending.value = false
  }
})

// Selection state
const selectionReady = computed(() => store.tenantId && store.workspaceId)

// Get selection object
function getSelection() {
  return {
    tenantId: store.tenantId!,
    workspaceId: store.workspaceId!
  }
}

// Toggle sidebar
function toggleSidebar() {
  sidebarExpanded.value = !sidebarExpanded.value
  // 如果展开会话管理，则收起运行时管理
  if (sidebarExpanded.value) {
    runtimeExpanded.value = false
  }
}

// Toggle runtime panel
function toggleRuntime() {
  runtimeExpanded.value = !runtimeExpanded.value
  // 如果展开运行时管理，则收起会话管理
  if (runtimeExpanded.value) {
    sidebarExpanded.value = false
  }
}

// Load agents
async function loadAgents() {
  if (!selectionReady.value) return
  error.value = ''
  loadingAgents.value = true
  try {
    agents.value = await listAgents(getSelection())
    console.log('Loaded agents:', agents.value)
    if (agents.value.length && !selectedAgentId.value) {
      selectedAgentId.value = agents.value[0].id
      await loadSessions()
    }
  } catch (e: any) {
    error.value = e.message || '加载 Agent 失败'
  } finally {
    loadingAgents.value = false
  }
}

// Load sessions
async function loadSessions() {
  if (!selectedAgentId.value) return
  error.value = ''
  try {
    sessions.value = await listSessions(getSelection(), selectedAgentId.value)
    // 默认选中第一个会话
    if (sessions.value.length > 0 && !selectedSessionId.value) {
      selectedSessionId.value = sessions.value[0].sessionId
      await loadMessages()
    }
  } catch (e: any) {
    error.value = e.message || '加载会话失败'
  }
}

// Create new session
function createNewSession() {
  if (!selectedAgentId.value) return
  error.value = ''

  // 创建临时会话（不发送请求）
  const tempSessionId = 'temp-' + Date.now()
  const tempSession: ChatSession = {
    sessionId: tempSessionId,
    agentId: selectedAgentId.value,
    name: '新会话',
    createdAt: new Date().toISOString()
  }

  sessions.value.unshift(tempSession)
  selectedSessionId.value = tempSessionId
  pendingSessionName.value = '新会话'
  messages.value = []
}

// Delete session
async function handleDeleteSession(sessionId: string) {
  if (!selectedAgentId.value) return
  if (!confirm('确定要删除这个会话吗？')) return

  error.value = ''
  try {
    // 如果是临时会话，直接从列表中移除
    if (sessionId.startsWith('temp-')) {
      sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
      if (selectedSessionId.value === sessionId) {
        selectedSessionId.value = ''
        pendingSessionName.value = ''
        messages.value = []
      }
    } else {
      await deleteSession(getSelection(), selectedAgentId.value, sessionId)
      sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
      if (selectedSessionId.value === sessionId) {
        selectedSessionId.value = ''
        messages.value = []
      }
    }
  } catch (e: any) {
    error.value = e.message || '删除会话失败'
  }
}

// Select session
function selectSession(sessionId: string) {
  selectedSessionId.value = sessionId
  loadMessages()
}

// Load messages
async function loadMessages() {
  if (!selectedSessionId.value || !selectedAgentId.value) return
  error.value = ''
  try {
    const rawMessages = await listMessages(getSelection(), selectedAgentId.value, selectedSessionId.value)
    // 解析消息内容
    const parsedMessages = rawMessages.map(msg => {
      const parsedMsg = { ...msg }

      // 解析工具调用
      if (msg.role === 'ASSISTANT' && msg.content && msg.content.startsWith('[{')) {
        try {
          parsedMsg.toolCalls = JSON.parse(msg.content)
          parsedMsg.content = '' // 清空content，避免重复显示
        } catch (e) {
          console.error('Failed to parse tool calls:', e)
        }
      }

      // 解析工具响应
      if (msg.role === 'TOOL' && msg.content && msg.content.startsWith('[{')) {
        try {
          parsedMsg.toolResponses = JSON.parse(msg.content)
          parsedMsg.content = '' // 清空content，避免重复显示
        } catch (e) {
          console.error('Failed to parse tool responses:', e)
        }
      }

      return parsedMsg
    })

    // 保存到该会话的消息列表
    // 注意：不要覆盖，而是合并暂存的消息
    const existingMsgs = sessionMessages.get(selectedSessionId.value) || []
    const existingIds = new Set(existingMsgs.map(m => m.messageId))

    // 只添加后端返回的新消息（不在现有消息中的）
    const newMsgs = parsedMessages.filter(m => !existingIds.has(m.messageId))

    // 合并消息：后端消息 + 暂存消息
    const allMsgs = [...parsedMessages]

    // 添加暂存的消息（不在后端返回的消息中的）
    const state = sessionStreamingStates.get(selectedSessionId.value)
    if (state && state.pendingMessages.length > 0) {
      const backendIds = new Set(parsedMessages.map(m => m.messageId))
      const pendingMsgs = state.pendingMessages.filter(m => !backendIds.has(m.messageId))
      allMsgs.push(...pendingMsgs)
    }

    sessionMessages.set(selectedSessionId.value, allMsgs)
    messages.value = [...allMsgs]
    scrollToBottom()
  } catch (e: any) {
    error.value = e.message || '加载消息失败'
  }
}

// Send message
async function handleSend() {
  if (!selectedAgentId.value || !inputContent.value.trim() || sending.value) return

  const content = inputContent.value.trim()
  inputContent.value = ''
  sending.value = true
  error.value = ''

  let currentSessionId = selectedSessionId.value

  // 如果是临时会话，先创建真正的会话
  if (currentSessionId.startsWith('temp-')) {
    try {
      // 使用用户输入的前20个字符作为会话名称
      const sessionName = content.slice(0, 20) + (content.length > 20 ? '...' : '')
      const session = await createSession(getSelection(), selectedAgentId.value, sessionName)

      // 替换临时会话
      const tempIndex = sessions.value.findIndex(s => s.sessionId === currentSessionId)
      if (tempIndex !== -1) {
        sessions.value[tempIndex] = session
      } else {
        sessions.value.unshift(session)
      }

      currentSessionId = session.sessionId
      selectedSessionId.value = session.sessionId
      pendingSessionName.value = ''
    } catch (e: any) {
      error.value = e.message || '创建会话失败'
      sending.value = false
      return
    }
  } else if (!currentSessionId) {
    // 如果没有选中的会话，先创建一个新会话
    try {
      // 使用用户输入的前20个字符作为会话名称
      const sessionName = content.slice(0, 20) + (content.length > 20 ? '...' : '')
      const session = await createSession(getSelection(), selectedAgentId.value, sessionName)
      sessions.value.unshift(session)
      currentSessionId = session.sessionId
      selectedSessionId.value = session.sessionId
      messages.value = []
    } catch (e: any) {
      error.value = e.message || '创建会话失败'
      sending.value = false
      return
    }
  }

  // 立即添加用户消息
  const userMessage: ChatMessage = {
    messageId: Date.now().toString(),
    sessionId: currentSessionId,
    role: 'USER',
    content,
    createdAt: new Date().toISOString()
  }

  // 确保该会话的消息列表存在
  if (!sessionMessages.has(currentSessionId)) {
    sessionMessages.set(currentSessionId, [])
  }

  // 添加用户消息到sessionMessages
  const sessionMsgs = sessionMessages.get(currentSessionId)!
  sessionMsgs.push(userMessage)

  try {
    if (useStream.value) {
      // 初始化该会话的流消息状态
      const streamState = sessionStreamingStates.get(currentSessionId) || {
        content: '',
        isStreaming: false,
        pendingMessages: []
      }
      streamState.content = ''
      streamState.isStreaming = true
      streamState.pendingMessages = [userMessage] // 标记正在进行的消息
      sessionStreamingStates.set(currentSessionId, streamState)

      // 如果是当前会话，更新显示
      if (selectedSessionId.value === currentSessionId) {
        messages.value = [...sessionMsgs]
        streamingContent.value = ''
      }
      scrollToBottom()

      await sendMessageStream(
        getSelection(),
        selectedAgentId.value,
        currentSessionId,
        content,
        {
          onMessage: (streamMsg: StreamMessage) => {
            // 只有当前会话才处理流消息显示
            if (selectedSessionId.value === currentSessionId) {
              handleStreamMessage(streamMsg)
            }
            // 更新该会话的流消息状态
            const state = sessionStreamingStates.get(currentSessionId)
            if (state) {
              state.content = streamingContent.value
            }
            scrollToBottom()
          },
          onDone: () => {
            // 流式完成后，如果有剩余内容，添加为助手消息
            const state = sessionStreamingStates.get(currentSessionId)
            const finalContent = state?.content || streamingContent.value

            if (finalContent.trim()) {
              const assistantMessage: ChatMessage = {
                messageId: (Date.now() + 1).toString(),
                sessionId: currentSessionId,
                role: 'ASSISTANT',
                content: finalContent,
                createdAt: new Date().toISOString(),
                messageType: 'ASSISTANT'
              }

              // 确保sessionMessages存在
              if (!sessionMessages.has(currentSessionId)) {
                sessionMessages.set(currentSessionId, [])
              }

              // 添加到sessionMessages
              const sessionMsgs = sessionMessages.get(currentSessionId)!
              sessionMsgs.push(assistantMessage)

              // 添加到暂存消息列表（用于标记）
              if (state && state.pendingMessages) {
                state.pendingMessages.push(assistantMessage)
              }

              // 如果是当前会话，更新显示
              if (selectedSessionId.value === currentSessionId) {
                messages.value = [...sessionMsgs]
              }
            }

            // 清空该会话的流消息状态
            if (state) {
              state.content = ''
              state.isStreaming = false
              state.pendingMessages = [] // 清空暂存标记
            }
            if (selectedSessionId.value === currentSessionId) {
              streamingContent.value = ''
            }
            scrollToBottom()
          },
          onError: (err) => {
            error.value = err.message || '发送消息失败'
            // 清空该会话的流消息状态
            const state = sessionStreamingStates.get(currentSessionId)
            if (state) {
              state.content = ''
              state.isStreaming = false
              state.pendingMessages = []
            }

            // 从sessionMessages中移除失败的用户消息
            const sessionMsgs = sessionMessages.get(currentSessionId)
            if (sessionMsgs) {
              const index = sessionMsgs.findIndex(m => m.messageId === userMessage.messageId)
              if (index !== -1) {
                sessionMsgs.splice(index, 1)
              }
            }

            if (selectedSessionId.value === currentSessionId) {
              streamingContent.value = ''
              messages.value = [...(sessionMsgs || [])]
            }
          }
        }
      )
    } else {
      // 非流式消息：直接添加到历史消息
      const response = await sendMessage(
        getSelection(),
        selectedAgentId.value,
        currentSessionId,
        content
      )

      // 添加到该会话的消息列表
      const sessionMsgs = sessionMessages.get(currentSessionId)
      if (sessionMsgs) {
        sessionMsgs.push(userMessage)
        sessionMsgs.push(response)
        // 如果是当前会话，更新显示
        if (selectedSessionId.value === currentSessionId) {
          messages.value = [...sessionMsgs]
        }
      }
      scrollToBottom()
    }
  } catch (e: any) {
    error.value = e.message || '发送消息失败'
    // 流式消息失败时，清空暂存消息
    if (useStream.value) {
      const state = sessionStreamingStates.get(currentSessionId)
      if (state) {
        state.pendingMessages = []
      }
      if (selectedSessionId.value === currentSessionId) {
        const historyMsgs = sessionMessages.get(currentSessionId) || []
        messages.value = historyMsgs
      }
    }
  } finally {
    sending.value = false
  }
}

// 处理流式消息
function handleStreamMessage(streamMsg: StreamMessage) {
  const currentSessionId = selectedSessionId.value
  if (!currentSessionId) return

  // 确保sessionMessages存在
  if (!sessionMessages.has(currentSessionId)) {
    sessionMessages.set(currentSessionId, [])
  }
  const sessionMsgs = sessionMessages.get(currentSessionId)!

  // 获取流状态
  const state = sessionStreamingStates.get(currentSessionId)

  if (streamMsg.messageType === 'ASSISTANT') {
    // 助手消息：累积文本内容
    if (streamMsg.text) {
      streamingContent.value += streamMsg.text
      // 同步更新到流状态
      if (state) {
        state.content = streamingContent.value
      }
    }
    // 如果有工具调用，先保存当前的助手消息，然后添加工具调用消息
    if (streamMsg.toolCalls && streamMsg.toolCalls.length > 0) {
      // 先保存累积的助手消息
      if (streamingContent.value.trim()) {
        const assistantMessage: ChatMessage = {
          messageId: `assistant-${Date.now()}`,
          sessionId: currentSessionId,
          role: 'ASSISTANT',
          content: streamingContent.value,
          createdAt: new Date().toISOString(),
          messageType: 'ASSISTANT'
        }
        sessionMsgs.push(assistantMessage)
        messages.value.push(assistantMessage)

        // 添加到pendingMessages
        if (state && state.pendingMessages) {
          state.pendingMessages.push(assistantMessage)
        }

        streamingContent.value = '' // 清空累积内容
        // 同步更新到流状态
        if (state) {
          state.content = ''
        }
      }

      // 然后保存工具调用消息
      const toolCallMessage: ChatMessage = {
        messageId: `toolcall-${Date.now()}`,
        sessionId: currentSessionId,
        role: 'ASSISTANT',
        content: '',
        createdAt: new Date().toISOString(),
        messageType: 'ASSISTANT',
        toolCalls: streamMsg.toolCalls
      }
      sessionMsgs.push(toolCallMessage)
      messages.value.push(toolCallMessage)

      // 添加到pendingMessages
      if (state && state.pendingMessages) {
        state.pendingMessages.push(toolCallMessage)
      }
    }
  } else if (streamMsg.messageType === 'TOOL') {
    // 工具消息：处理工具响应
    if (streamMsg.responses && streamMsg.responses.length > 0) {
      for (const response of streamMsg.responses) {
        // 判断是否是技能读取
        const isSkill = response.name === 'read_skill' || response.name === 'apply_skill'

        const toolResultMessage: ChatMessage = {
          messageId: `toolresult-${Date.now()}-${response.id}`,
          sessionId: currentSessionId,
          role: 'TOOL',
          content: '',
          createdAt: new Date().toISOString(),
          messageType: isSkill ? 'SKILL' : 'TOOL',
          toolResponses: [response]
        }
        sessionMsgs.push(toolResultMessage)
        messages.value.push(toolResultMessage)

        // 添加到pendingMessages
        if (state && state.pendingMessages) {
          state.pendingMessages.push(toolResultMessage)
        }
      }
    }
  } else if (streamMsg.messageType === 'SYSTEM') {
    // 系统消息：通常是错误消息
    const systemMessage: ChatMessage = {
      messageId: `system-${Date.now()}`,
      sessionId: currentSessionId,
      role: 'SYSTEM',
      content: streamMsg.text || '',
      createdAt: new Date().toISOString(),
      messageType: 'SYSTEM'
    }
    sessionMsgs.push(systemMessage)
    messages.value.push(systemMessage)

    // 添加到pendingMessages
    if (state && state.pendingMessages) {
      state.pendingMessages.push(systemMessage)
    }
  }
}

// Handle keydown
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// Scroll to bottom
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// Format time
function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// Get message role label
function getMessageRoleLabel(msg: ChatMessage): string {
  if (msg.role === 'USER') return '用户'
  if (msg.role === 'SYSTEM') return '系统'
  
  // 根据消息类型返回不同的标签
  switch (msg.role) {
    case 'TOOL':
      // 显示工具名称
      if (msg.toolResponses && msg.toolResponses.length > 0) {
        return `工具: ${msg.toolResponses[0].name}`
      }
      return '工具'
    case 'ASSISTANT':
      // 如果有工具调用，显示工具名称
      if (msg.toolCalls && msg.toolCalls.length > 0) {
        return `调用: ${msg.toolCalls[0].name}`
      }
      return '助手'
    default:
      // 检查messageType
      if (msg.messageType === 'SKILL') {
        if (msg.toolResponses && msg.toolResponses.length > 0) {
          const skillName = msg.toolResponses[0].name === 'read_skill' ? '读取技能' : 
                           msg.toolResponses[0].name === 'apply_skill' ? '应用技能' : 
                           msg.toolResponses[0].name
          return `技能: ${skillName}`
        }
        return '技能'
      }
      return '助手'
  }
}
// Agent change
function onAgentChange() {
  selectedSessionId.value = ''
  messages.value = []
  loadSessions()
}

// Go to workspace
function goToWorkspace() {
  router.push('/agenthub/workspace')
}

// Go to agents
function goToAgents() {
  router.push('/agenthub/agents')
}

// Watch selection
watch(() => [store.tenantId, store.workspaceId], () => {
  selectedAgentId.value = ''
  selectedSessionId.value = ''
  messages.value = []
  sessions.value = []
  loadAgents()
})

// Initialize
onMounted(() => {
  if (selectionReady.value) {
    loadAgents()
  }
})
</script>

<style scoped>
.chat-page {
  max-width: 1400px;
  margin: 0 auto;
  height: calc(100vh - 120px);
  position: relative;
}

/* Error Toast */
.error-toast {
  position: fixed;
  top: 80px;
  right: 24px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(201, 74, 53, 0.95);
  color: white;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(201, 74, 53, 0.3);
  backdrop-filter: blur(12px);
  animation: slide-in-right 0.3s ease;
}

.error-toast svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.error-toast button {
  padding: 4px;
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.error-toast button:hover {
  opacity: 1;
}

.error-toast button svg {
  width: 16px;
  height: 16px;
}

@keyframes slide-in-right {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Empty State */
.empty-state {
  border-radius: 20px;
  padding: 60px 40px;
  background: rgba(255, 255, 255, 0.85);
  color: #5d6678;
  text-align: center;
  box-shadow: 0 12px 24px rgba(32, 44, 68, 0.08);
  backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  width: 80px;
  height: 80px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(58, 138, 214, 0.1), rgba(58, 138, 214, 0.05));
  border-radius: 20px;
  color: #3a8ad6;
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

/* Chat Layout */
.chat-layout {
  display: flex;
  height: 100%;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 320px;
  min-width: 320px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(26, 30, 41, 0.08);
  border-radius: 20px;
  box-shadow: 0 12px 24px rgba(32, 44, 68, 0.08);
  backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1), 
              min-width 0.25s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar.collapsed {
  width: 0;
  min-width: 0;
  border: none;
  box-shadow: none;
  opacity: 0;
}

.sidebar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(22, 33, 50, 0.08);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #264266;
}

.sidebar-section {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(22, 33, 50, 0.06);
}

.sidebar-section.flex-grow {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-bottom: none;
  overflow: hidden;
}

.section-label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #5d6678;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.section-header .section-label {
  margin-bottom: 0;
}

/* Agent Selector */
.agent-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.agent-selector select {
  flex: 1;
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid rgba(38, 66, 102, 0.14);
  background: white;
  font: inherit;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s ease;
  color: #1a1e29;
}

.agent-selector select:focus {
  outline: none;
  border-color: #3a8ad6;
  box-shadow: 0 0 0 3px rgba(58, 138, 214, 0.15);
}

.agent-selector select option {
  color: #1a1e29;
  background: white;
  padding: 10px;
}

.refresh-btn {
  width: 40px;
  height: 40px;
  border: 1px solid rgba(38, 66, 102, 0.14);
  background: white;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s ease;
  color: #264266;
  flex-shrink: 0;
}

.refresh-btn svg {
  width: 18px;
  height: 18px;
}

.refresh-btn:hover {
  border-color: #3a8ad6;
  color: #3a8ad6;
  background: rgba(248, 250, 255, 0.5);
}

.no-agents {
  text-align: center;
  padding: 12px;
  background: rgba(248, 250, 255, 0.5);
  border-radius: 10px;
}

.no-agents p {
  margin: 0 0 8px;
  font-size: 0.85rem;
  color: #8a94a6;
}

.link-btn {
  padding: 6px 12px;
  background: transparent;
  border: none;
  color: #3a8ad6;
  font: inherit;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
}

.link-btn:hover {
  color: #264266;
}

.new-session-btn {
  padding: 6px 12px;
  border: none;
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: white;
  border-radius: 8px;
  font: inherit;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.25s ease;
}

.new-session-btn svg {
  width: 14px;
  height: 14px;
}

.new-session-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
}

.new-session-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Session List */
.session-list {
  flex: 1;
  overflow-y: auto;
  margin-top: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 4px;
}

.session-item:hover {
  background: rgba(58, 138, 214, 0.08);
}

.session-item.active {
  background: rgba(58, 138, 214, 0.12);
  box-shadow: inset 0 0 0 1px rgba(58, 138, 214, 0.2);
}

.session-icon {
  width: 28px;
  height: 28px;
  padding: 6px;
  background: rgba(58, 138, 214, 0.1);
  border-radius: 8px;
  color: #3a8ad6;
  flex-shrink: 0;
}

.session-icon svg {
  width: 100%;
  height: 100%;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 0.85rem;
  font-weight: 500;
  color: #1a1e29;
  margin-bottom: 2px;
}

.session-time {
  font-size: 0.7rem;
  color: #8a94a6;
}

.delete-btn {
  width: 28px;
  height: 28px;
  padding: 6px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8a94a6;
  opacity: 0;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.delete-btn svg {
  width: 100%;
  height: 100%;
}

.session-item:hover .delete-btn {
  opacity: 1;
}

.streaming-indicator {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 4px;
  flex-shrink: 0;
}

.spinner-icon {
  width: 18px;
  height: 18px;
  color: #3a8ad6;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.delete-btn:hover {
  background: rgba(201, 74, 53, 0.1);
  color: #c94a35;
}

.empty-sessions {
  text-align: center;
  padding: 20px;
  color: #8a94a6;
}

.empty-sessions p {
  margin: 0;
  font-size: 0.9rem;
}

.empty-sessions .hint {
  margin-top: 4px;
  font-size: 0.8rem;
}

/* Chat Panel */
.chat-panel {
  flex: 1;
  min-width: 0;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(26, 30, 41, 0.08);
  border-radius: 20px;
  box-shadow: 0 12px 24px rgba(32, 44, 68, 0.08);
  backdrop-filter: blur(12px);
  display: grid;
  grid-template-rows: 1fr auto;
  overflow: hidden;
}

.chat-messages {
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Empty Chat */
.empty-chat {
  text-align: center;
  padding: 60px 20px;
  color: #5d6678;
}

.empty-chat .empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  padding: 16px;
}

/* Messages */
.message {
  display: flex;
  gap: 10px;
  max-width: 85%;
  animation: message-in 0.3s ease;
}

@keyframes message-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.assistant {
  align-self: flex-start;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-avatar svg {
  width: 16px;
  height: 16px;
}

.message-avatar.user {
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: white;
  box-shadow: 0 3px 8px rgba(58, 138, 214, 0.3);
}

.message-avatar.assistant {
  background: linear-gradient(135deg, rgba(247, 203, 110, 0.2), rgba(247, 203, 110, 0.1));
  color: #7a5f2b;
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.message-role {
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  color: #264266;
}

.message-time {
  font-size: 0.65rem;
  color: #8a94a6;
}

.typing-indicator {
  font-size: 0.65rem;
  color: #3a8ad6;
  font-style: italic;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.message-content {
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-word;
  font-size: 0.85rem;
  display: inline-block;
  width: fit-content;
  max-width: 100%;
}

.message.user .message-content {
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: white;
  box-shadow: 0 3px 8px rgba(58, 138, 214, 0.2);
}

.message.assistant .message-content {
  background: rgba(248, 250, 255, 0.95);
  border: 1px solid rgba(38, 66, 102, 0.1);
}

.message.system .message-content {
  background: rgba(201, 74, 53, 0.1);
  border: 1px solid rgba(201, 74, 53, 0.3);
  color: #c94a35;
}

.system-message {
  font-weight: 500;
}

/* Tool and Skill message styles */
.message.tool .message-content,
.message.skill .message-content {
  background: transparent;
  border: none;
  padding: 0;
  width: 100%;
}

.tool-calls-container,
.tool-results-container,
.skill-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.cursor {
  animation: blink 1s step-end infinite;
  color: #3a8ad6;
  font-weight: bold;
}

@keyframes blink {
  50% { opacity: 0; }
}

/* Chat Input */
.chat-input {
  padding: 16px 20px;
  border-top: 1px solid rgba(22, 33, 50, 0.08);
  background: rgba(248, 250, 255, 0.5);
  backdrop-filter: blur(8px);
}

.input-container {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
}

.chat-input textarea {
  width: 100%;
  resize: none;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(38, 66, 102, 0.14);
  background: white;
  font: inherit;
  font-size: 0.9rem;
  line-height: 1.5;
  transition: all 0.25s ease;
}

.chat-input textarea:focus {
  outline: none;
  border-color: #3a8ad6;
  box-shadow: 0 0 0 3px rgba(58, 138, 214, 0.15);
}

.chat-input textarea::placeholder {
  color: #8a94a6;
}

.input-sidebar {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: space-between;
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: #5d6678;
  cursor: pointer;
}

.toggle-label input {
  display: none;
}

.toggle-slider {
  width: 32px;
  height: 18px;
  background: rgba(38, 66, 102, 0.14);
  border-radius: 9px;
  position: relative;
  transition: all 0.25s ease;
}

.toggle-slider::after {
  content: '';
  position: absolute;
  width: 14px;
  height: 14px;
  background: white;
  border-radius: 50%;
  top: 2px;
  left: 2px;
  transition: all 0.25s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-label input:checked + .toggle-slider {
  background: linear-gradient(135deg, #264266, #3a8ad6);
}

.toggle-label input:checked + .toggle-slider::after {
  left: 16px;
}

.toggle-text {
  font-size: 0.75rem;
}

.primary {
  padding: 10px 16px;
  border-radius: 10px;
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: white;
  border: none;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 3px 8px rgba(58, 138, 214, 0.2);
  transition: all 0.25s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 0.85rem;
}

.primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
}

.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn .btn-icon {
  width: 16px;
  height: 16px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Toggle Sidebar FAB */
.toggle-runtime-fab {
  position: fixed;
  bottom: 80px;
  left: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #6c757d, #7a8288);
  color: white;
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 100;
}

.toggle-runtime-fab svg {
  width: 24px;
  height: 24px;
  transition: transform 0.3s ease;
}

.toggle-runtime-fab:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(108, 117, 125, 0.4);
}

.toggle-runtime-fab:active {
  transform: translateY(0);
}

.toggle-sidebar-fab {
  position: fixed;
  bottom: 24px;
  left: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: white;
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 100;
}

.toggle-sidebar-fab svg {
  width: 24px;
  height: 24px;
  transition: transform 0.3s ease;
}

.toggle-sidebar-fab:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(58, 138, 214, 0.4);
}

.toggle-sidebar-fab:active {
  transform: translateY(0);
}

.new-session-fab {
  position: fixed;
  bottom: 140px;
  right: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 100;
}

.new-session-fab svg {
  width: 24px;
  height: 24px;
  transition: transform 0.3s ease;
}

.new-session-fab:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
}

.new-session-fab:active:not(:disabled) {
  transform: translateY(0);
}

.new-session-fab:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.runtime-sidebar {
  width: 320px;
  background: var(--bg-color, #ffffff);
  border-right: 1px solid var(--border-color, #e5e7eb);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
}

.runtime-sidebar.collapsed {
  width: 0;
  border-right: none;
}

.runtime-content {
  width: 320px;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 1rem;
}

.runtime-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--text-muted, #6b7280);
}

.runtime-placeholder .placeholder-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.runtime-placeholder .placeholder-icon svg {
  width: 100%;
  height: 100%;
}

.runtime-placeholder p {
  margin: 0.5rem 0;
}

.runtime-placeholder .hint {
  font-size: 0.875rem;
  opacity: 0.7;
}

/* Animations */
.fade-in {
  animation: fade-in 0.4s ease forwards;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* Scrollbar */
.chat-messages::-webkit-scrollbar,
.session-list::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track,
.session-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb,
.session-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb:hover,
.session-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.25);
}

/* Responsive */
@media (max-width: 900px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: calc(100vh - 120px);
    z-index: 50;
  }
  
  .sidebar.collapsed {
    left: -320px;
  }
}

@media (max-width: 768px) {
  .chat-page {
    height: calc(100vh - 100px);
  }
  
  .message {
    max-width: 95%;
  }
  
  .input-container {
    grid-template-columns: 1fr;
  }
  
  .input-sidebar {
    flex-direction: row;
  }
  
  .toggle-sidebar-fab {
    width: 48px;
    height: 48px;
    bottom: 20px;
    left: 20px;
  }
  
  .toggle-sidebar-fab svg {
    width: 20px;
    height: 20px;
  }
}
</style>
