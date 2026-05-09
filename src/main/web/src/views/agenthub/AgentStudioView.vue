<template>
  <section class="grid">
    <div class="page-header">
      <div>
        <h2>Agent Studio</h2>
        <p class="muted">创建 Agent、配置关联关系并一键发布。</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <!-- 创建/编辑 Agent 弹窗 -->
      <ModalDialog
        v-model:visible="showCreateForm"
        :title="editingAgent ? '编辑 Agent' : '创建 Agent'"
        @confirm="submitAgent"
        @close="showCreateForm = false"
        :confirm-disabled="loading"
        :confirm-text="editingAgent ? '更新' : '创建'"
      >
        <form>
          <label class="field">
            <span>Agent 名称</span>
            <input v-model="agentName" placeholder="销售教练" />
          </label>
          <label class="field">
            <span>描述</span>
            <input v-model="agentDescription" placeholder="Agent 的目标与交互风格" />
          </label>
        </form>
      </ModalDialog>

      <!-- Agent 列表 -->
      <article class="table-card">
        <table>
          <thead>
            <tr>
              <th>Agent</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="agent in agents" :key="agent.id">
              <td>
                <strong>{{ agent.name }}</strong>
                <p class="muted">{{ agent.description || '无描述' }}</p>
              </td>
              <td>
                <span class="tag" :class="agent.status === 'PUBLISHED' ? 'success' : 'default'">
                  {{ agent.status || 'DRAFT' }}
                </span>
              </td>
              <td>
                <div class="toolbar">
                  <button class="secondary" type="button" @click="handleEdit(agent)">编辑</button>
                  <button v-if="agent.status !== 'PUBLISHED'" class="secondary" type="button" @click="handlePublish(agent)">发布</button>
                  <button v-else class="secondary" type="button" @click="handleUnpublish(agent)">取消发布</button>
                  <button class="ghost" type="button" @click="handleDelete(agent)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <article v-if="!agents.length" class="empty-state">暂无 Agent，点击上方按钮创建。</article>
      </article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '@/store/workspace-store'
import { listAgents, createAgent, updateAgent, deleteAgent, publishAgent, unpublishAgent } from '@/api/agent-api'
import type { Agent } from '@/types/agent'

const store = useWorkspaceStore()
const router = useRouter()
const agents = ref<Agent[]>([])
const error = ref('')
const showCreateForm = ref(false)
const agentName = ref('')
const agentDescription = ref('')
const editingAgent = ref<Agent | null>(null)

const selectionReady = computed(() => store.tenantId && store.workspaceId)

onMounted(loadAgents)

// 监听全局新增事件
onMounted(() => {
  window.addEventListener('global-add', () => {
    editingAgent.value = null
    showCreateForm.value = true
  })
})

// 监听workspaceId变化，重新加载数据
watch(() => store.workspaceId, (newId, oldId) => {
  if (newId && oldId && newId !== oldId) {
    loadAgents()
  }
})

async function loadAgents() {
  if (!selectionReady.value) return
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    agents.value = await listAgents(selection)
  } catch (e: any) {
    error.value = e.message
  }
}

async function submitAgent() {
  if (!selectionReady.value || !agentName.value.trim()) return
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    if (editingAgent.value) {
      await updateAgent(selection, editingAgent.value.id, agentName.value.trim(), agentDescription.value.trim())
    } else {
      await createAgent(selection, agentName.value.trim(), agentDescription.value.trim())
    }
    cancelForm()
    await loadAgents()
  } catch (e: any) {
    error.value = e.message
  }
}

function cancelForm() {
  agentName.value = ''
  agentDescription.value = ''
  showCreateForm.value = false
  editingAgent.value = null
}

function handleEdit(agent: Agent) {
  editingAgent.value = agent
  agentName.value = agent.name
  agentDescription.value = agent.description || ''
  showCreateForm.value = true
}

async function handlePublish(agent: Agent) {
  if (!selectionReady.value) return
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    await publishAgent(selection, agent.id)
    await loadAgents()
  } catch (e: any) {
    error.value = e.message
  }
}

async function handleUnpublish(agent: Agent) {
  if (!selectionReady.value) return
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    await unpublishAgent(selection, agent.id)
    await loadAgents()
  } catch (e: any) {
    error.value = e.message
  }
}

async function handleDelete(agent: Agent) {
  if (!selectionReady.value) return
  if (!confirm(`确定删除 Agent "${agent.name}" 吗？`)) return
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    await deleteAgent(selection, agent.id)
    await loadAgents()
  } catch (e: any) {
    error.value = e.message
  }
}

function openConfigPanel(agent: Agent) {
  selectedAgent.value = agent
}
</script>

<style scoped>

.status {
  color: var(--color-error, #dc2626);
}

.empty-state {
  padding: 2rem;
  text-align: center;
  color: var(--color-muted, #6b7280);
}

.panel {
  background: white;
  border-radius: 0.5rem;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.table-card {
  background: white;
  border-radius: 0.5rem;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.field-grid {
  display: grid;
  gap: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.toolbar {
  display: flex;
  gap: 0.5rem;
}

.muted {
  color: var(--color-muted, #6b7280);
  font-size: 0.875rem;
}

.tag {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.tag.success {
  background: #dcfce7;
  color: #166534;
}

.tag.default {
  background: #f3f4f6;
  color: #374151;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

th {
  font-weight: 600;
  color: #374151;
}

button.primary {
  background: #3b82f6;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: none;
  cursor: pointer;
}

button.primary:hover {
  background: #2563eb;
}

button.secondary {
  background: #f3f4f6;
  color: #374151;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: none;
  cursor: pointer;
}

button.secondary:hover {
  background: #e5e7eb;
}

button.ghost {
  background: transparent;
  color: #6b7280;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: 1px solid #d1d5db;
  cursor: pointer;
}

button.ghost:hover {
  background: #f3f4f6;
}

input, textarea {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 0.25rem;
}

input:focus, textarea:focus {
  outline: none;
  border-color: #3b82f6;
}
</style>
