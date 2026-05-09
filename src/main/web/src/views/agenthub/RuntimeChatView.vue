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
            <label class="section-label">选择 Agent</label>
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
              <button class="new-session-btn" :disabled="!selectedAgentId" @click="createNewSession">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 5v14M5 12h14"/>
                </svg>
                新建
              </button>
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
                  <div class="session-id">{{ session.sessionId.slice(0, 8) }}...</div>
                  <div class="session-time">{{ formatDateTime(session.createdAt) }}</div>
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
                <p class="hint">点击"新建"创建会话</p>
              </div>
            </div>
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
            :class="['message', msg.role, {'fade-in': index === messages.length - 1}]"
          >
            <div class="message-avatar" :class="msg.role">
              <svg v-if="msg.role === 'user'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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
                <span class="message-role">{{ msg.role === 'user' ? '用户' : '助手' }}</span>
                <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <div class="message-content">
                <MarkdownRenderer v-if="msg.role === 'assistant'" :content="msg.content" />
                <template v-else>{{ msg.content }}</template>
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

    <!-- 左下角：会话管理展开/收起按钮 -->
    <button class="toggle-sidebar-fab" @click="toggleSidebar" :title="sidebarExpanded ? '收起会话管理' : '展开会话管理'">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M15 18l-6-6 6-6"/>
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
import type { Agent, ChatMessage, ChatSession } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const router = useRouter()
const store = useWorkspaceStore()
const error = ref('')

// Sidebar state
const sidebarExpanded = ref(true)

// Agent
const agents = ref<Agent[]>([])
const selectedAgentId = ref('')
const loadingAgents = ref(false)

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
  } catch (e: any) {
    error.value = e.message || '加载会话失败'
  }
}

// Create new session
async function createNewSession() {
  if (!selectedAgentId.value) return
  error.value = ''
  try {
    const session = await createSession(getSelection(), selectedAgentId.value)
    sessions.value.unshift(session)
    selectedSessionId.value = session.sessionId
    messages.value = []
  } catch (e: any) {
    error.value = e.message || '创建会话失败'
  }
}

// Delete session
async function handleDeleteSession(sessionId: string) {
  if (!selectedAgentId.value) return
  if (!confirm('确定要删除这个会话吗？')) return
  
  error.value = ''
  try {
    await deleteSession(getSelection(), selectedAgentId.value, sessionId)
    sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
    if (selectedSessionId.value === sessionId) {
      selectedSessionId.value = ''
      messages.value = []
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
    messages.value = await listMessages(getSelection(), selectedAgentId.value, selectedSessionId.value)
    scrollToBottom()
  } catch (e: any) {
    error.value = e.message || '加载消息失败'
  }
}

// Send message
async function handleSend() {
  if (!selectedSessionId.value || !selectedAgentId.value || !inputContent.value.trim() || sending.value) return
  
  const content = inputContent.value.trim()
  inputContent.value = ''
  sending.value = true
  error.value = ''
  
  // 立即添加用户消息
  const userMessage: ChatMessage = {
    messageId: Date.now().toString(),
    sessionId: selectedSessionId.value,
    role: 'user',
    content,
    createdAt: new Date().toISOString()
  }
  messages.value.push(userMessage)
  scrollToBottom()
  
  try {
    if (useStream.value) {
      streamingContent.value = ''
      
      await sendMessageStream(
        getSelection(),
        selectedAgentId.value,
        selectedSessionId.value,
        content,
        {
          onToken: (chunk) => {
            streamingContent.value += chunk
            scrollToBottom()
          },
          onDone: () => {
            // 流式完成后，将内容添加为助手消息
            if (streamingContent.value) {
              const assistantMessage: ChatMessage = {
                messageId: (Date.now() + 1).toString(),
                sessionId: selectedSessionId.value,
                role: 'assistant',
                content: streamingContent.value,
                createdAt: new Date().toISOString()
              }
              messages.value.push(assistantMessage)
              streamingContent.value = ''
              scrollToBottom()
            }
          },
          onError: (err) => {
            error.value = err.message || '发送消息失败'
            streamingContent.value = ''
          }
        }
      )
    } else {
      const response = await sendMessage(
        getSelection(),
        selectedAgentId.value,
        selectedSessionId.value,
        content
      )
      messages.value.push(response)
      scrollToBottom()
    }
  } catch (e: any) {
    error.value = e.message || '发送消息失败'
    // 移除失败的用户消息
    messages.value = messages.value.filter(m => m.messageId !== userMessage.messageId)
  } finally {
    sending.value = false
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

.session-id {
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
  text-transform: uppercase;
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
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.4;
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

.sidebar.collapsed ~ .toggle-sidebar-fab svg {
  transform: rotate(180deg);
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
