<template>
  <section class="subagent-chat-page">
    <div v-if="error" class="error-toast fade-in">
      <span>{{ error }}</span>
      <button @click="error = ''">&times;</button>
    </div>

    <div class="chat-layout">
      <!-- 左侧：子会话列表 -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <h3>子会话</h3>
        </div>
        <div class="sidebar-section">
          <label>子Agent</label>
          <select v-model="selectedSubagentId" @change="loadSubsessions">
            <option disabled value="">请选择</option>
            <option v-for="sub in subagents" :key="sub.id" :value="sub.id">{{ sub.name }}</option>
          </select>
        </div>
        <div class="sidebar-section">
          <button class="primary" :disabled="!selectedSubagentId" @click="createNewSubsession">新建子会话</button>
        </div>
        <div class="subsession-list">
          <div
            v-for="ss in subsessions"
            :key="ss.id"
            :class="['subsession-item', { active: ss.id === selectedSubsessionId }]"
            @click="selectSubsession(ss.id)"
          >
            <div class="subsession-name">{{ ss.name || ss.id.slice(0, 8) + '...' }}</div>
            <div class="subsession-status">{{ ss.status }}</div>
          </div>
          <div v-if="!subsessions.length && selectedSubagentId" class="empty-hint">
            <p>暂无子会话，点击上方按钮创建</p>
          </div>
        </div>
      </aside>

      <!-- 右侧：对话区 -->
      <article class="chat-panel">
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="!selectedSubsessionId" class="empty-chat">
            <p>选择一个子会话开始对话</p>
          </div>
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role.toLowerCase()]">
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
                <span class="message-role">{{ msg.role === 'USER' ? '用户' : '子Agent' }}</span>
              </div>
              <div class="message-content">{{ msg.content }}</div>
            </div>
          </div>
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
                <span class="message-role">子Agent</span>
                <span class="typing-indicator">正在输入...</span>
              </div>
              <div class="message-content">{{ streamingContent }}<span class="cursor">▊</span></div>
            </div>
          </div>
        </div>

        <form class="chat-input" @submit.prevent="handleSend">
          <textarea
            v-model="inputContent"
            rows="3"
            placeholder="输入消息..."
            :disabled="!selectedSubsessionId || sending"
            @keydown.enter.prevent="handleSend"
          ></textarea>
          <div class="input-actions">
            <button class="primary send-btn" type="submit" :disabled="!canSend">
              <span>{{ sending ? '发送中...' : '发送' }}</span>
            </button>
          </div>
        </form>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { listAgents } from '@/api/agent-api'
import {
  createSubsession,
  listSubagents,
  listSubsessions,
  startSubagentStream,
} from '@/api/subagent-api'
import { useWorkspaceStore } from '@/store/workspace-store'
import type { Subagent, Subsession } from '@/types/subagent'
import type { Agent } from '@/types/agent'

const store = useWorkspaceStore()
const error = ref('')
const agents = ref<Agent[]>([])
const selectedAgentId = ref('')
const subagents = ref<Subagent[]>([])
const selectedSubagentId = ref('')
const subsessions = ref<Subsession[]>([])
const selectedSubsessionId = ref('')
const messages = ref<{ role: string; content: string }[]>([])
const inputContent = ref('')
const sending = ref(false)
const streamingContent = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

onMounted(async () => {
  try {
    agents.value = await listAgents(getSelection())
    if (agents.value.length) {
      selectedAgentId.value = agents.value[0].id
      await loadSubagents()
    }
  } catch (e: any) {
    error.value = e.message || '加载失败'
  }
})

function getSelection() {
  return { tenantId: store.tenantId!, workspaceId: store.workspaceId! }
}

async function loadSubagents() {
  try {
    subagents.value = await listSubagents(getSelection(), selectedAgentId.value)
  } catch (e: any) {
    error.value = e.message || '加载子Agent失败'
  }
}

async function loadSubsessions() {
  if (!selectedSubagentId.value) return
  try {
    const parentSessionId = selectedSubagentId.value
    subsessions.value = await listSubsessions(getSelection(), selectedAgentId.value, parentSessionId)
  } catch (e: any) {
    error.value = e.message || '加载子会话失败'
  }
}

async function createNewSubsession() {
  if (!selectedSubagentId.value) return
  try {
    const parentSessionId = selectedSubagentId.value
    const ss = await createSubsession(getSelection(), selectedAgentId.value, parentSessionId, {
      subagentId: selectedSubagentId.value,
      name: `会话-${new Date().toLocaleTimeString('zh-CN')}`,
    })
    subsessions.value.unshift(ss)
    selectedSubsessionId.value = ss.id
    messages.value = []
  } catch (e: any) {
    error.value = e.message || '创建子会话失败'
  }
}

function selectSubsession(id: string) {
  selectedSubsessionId.value = id
  messages.value = []
  streamingContent.value = ''
}

const canSend = computed(() =>
  Boolean(selectedSubsessionId.value && inputContent.value.trim() && !sending.value)
)

async function handleSend() {
  if (!canSend.value) return
  const content = inputContent.value.trim()
  inputContent.value = ''

  messages.value.push({ role: 'USER', content })
  sending.value = true
  streamingContent.value = ''

  try {
    await startSubagentStream(
      getSelection(),
      selectedSubsessionId.value,
      selectedSubagentId.value,
      content,
      {
        onMessage: (event) => {
          if (event.eventType === 'assistant' && event.content) {
            streamingContent.value += event.content
          }
          nextTick(() => scrollToBottom())
        },
        onDone: () => {
          if (streamingContent.value) {
            messages.value.push({ role: 'ASSISTANT', content: streamingContent.value })
          }
          streamingContent.value = ''
          sending.value = false
          nextTick(() => scrollToBottom())
        },
        onError: (err) => {
          error.value = err.message
          sending.value = false
        },
      }
    )
  } catch (e: any) {
    error.value = e.message || '发送失败'
    sending.value = false
  }
}

function scrollToBottom() {
  const el = messagesContainer.value
  if (el) el.scrollTop = el.scrollHeight
}
</script>

<style scoped>
.subagent-chat-page { height: calc(100vh - 60px); }
.chat-layout { display: flex; height: 100%; }
.sidebar { width: 280px; border-right: 1px solid #e0e0e0; padding: 16px; display: flex; flex-direction: column; }
.sidebar-header { margin-bottom: 16px; }
.sidebar-header h3 { margin: 0; }
.sidebar-section { margin-bottom: 12px; }
.sidebar-section label { display: block; font-size: 12px; color: #666; margin-bottom: 4px; }
.sidebar-section select { width: 100%; padding: 6px 8px; border: 1px solid #ccc; border-radius: 4px; }
.subsession-list { flex: 1; overflow-y: auto; }
.subsession-item { padding: 8px 12px; cursor: pointer; border-radius: 4px; margin-bottom: 4px; }
.subsession-item:hover { background: #f5f5f5; }
.subsession-item.active { background: #e3f2fd; }
.subsession-name { font-size: 14px; }
.subsession-status { font-size: 11px; color: #999; }
.empty-hint { text-align: center; padding: 20px; color: #999; font-size: 13px; }
.chat-panel { flex: 1; display: flex; flex-direction: column; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; }
.empty-chat { text-align: center; padding: 60px 20px; color: #999; }
.message { display: flex; gap: 12px; margin-bottom: 16px; }
.message-avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.message-avatar svg { width: 16px; height: 16px; }
.message-avatar.user { background: #e3f2fd; }
.message-avatar.assistant { background: #f3e5f5; }
.message-body { flex: 1; }
.message-header { font-size: 12px; color: #666; margin-bottom: 4px; }
.message-content { font-size: 14px; line-height: 1.6; white-space: pre-wrap; }
.typing-indicator { font-size: 11px; color: #999; margin-left: 8px; }
.cursor { animation: blink 1s step-end infinite; }
@keyframes blink { 50% { opacity: 0; } }
.chat-input { padding: 16px; border-top: 1px solid #e0e0e0; }
.chat-input textarea { width: 100%; padding: 8px 12px; border: 1px solid #ccc; border-radius: 4px; resize: none; }
.input-actions { display: flex; justify-content: flex-end; margin-top: 8px; }
.error-toast { background: #f44336; color: #fff; padding: 10px 16px; display: flex; justify-content: space-between; }
.fade-in { animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
</style>
