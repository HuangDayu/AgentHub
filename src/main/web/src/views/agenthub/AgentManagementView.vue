<template>
  <section class="grid glass-float">
    <div class="page-header">
      <div>
        <h2>Agent管理</h2>
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
          <label class="field">
            <span>类型</span>
            <select v-model="agentType">
              <option value="">-- 请选择 --</option>
              <option value="MAIN_AGENT">主 Agent</option>
              <option value="SUB_AGENT">子 Agent</option>
            </select>
          </label>
          <label class="field">
            <span>运行类别</span>
            <select v-model="agentRuntimeCategory">
              <option value="">-- 请选择 --</option>
              <option value="SPRING_AGENT">Spring Agent</option>
              <option value="AGENT_SCOPE">AgentScope</option>
              <option value="ALIBABA_AGENT">阿里 Agent</option>
            </select>
          </label>
        </form>
      </ModalDialog>

      <!-- Agent 列表 -->
      <article class="table-card float-effect">
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
import { showConfirm } from '@/utils/confirm'
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
const agentType = ref('')
const agentRuntimeCategory = ref('')
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
  if (!canSubmitAgent()) return
  try { await performSubmitAgent() } catch (e: any) { error.value = e.message }
}

function canSubmitAgent(): boolean {
  return Boolean(selectionReady.value && agentName.value.trim())
}

async function performSubmitAgent(): Promise<void> {
  const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
  const name = agentName.value.trim()
  const description = agentDescription.value.trim()
  const type = agentType.value || undefined
  const runtimeCategory = agentRuntimeCategory.value || undefined
  if (editingAgent.value) { await updateAgent(selection, editingAgent.value.id, name, description, type, runtimeCategory) }
  else { await createAgent(selection, name, description, type, runtimeCategory) }
  cancelForm()
  await loadAgents()
}

function cancelForm() {
  agentName.value = ''
  agentDescription.value = ''
  agentType.value = ''
  agentRuntimeCategory.value = ''
  showCreateForm.value = false
  editingAgent.value = null
}

function handleEdit(agent: Agent) {
  editingAgent.value = agent
  agentName.value = agent.name
  agentDescription.value = agent.description || ''
  agentType.value = agent.type || ''
  agentRuntimeCategory.value = agent.runtimeCategory || ''
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
  if (!selectionReady.value || !await showConfirm(`确定删除 Agent "${agent.name}" 吗？`)) return
  try { await deleteAgent(getSelection(), agent.id); await loadAgents() } catch (e: any) { error.value = e.message }
}

function getSelection() {
  return { tenantId: store.tenantId, workspaceId: store.workspaceId }
}

function openConfigPanel(agent: Agent) {
  selectedAgent.value = agent
}
</script>

<style scoped>

.status {
  color: var(--color-error);
}

.empty-state {
  padding: 2rem;
  text-align: center;
  color: var(--color-muted, var(--color-text-muted));
}

.panel {
  background: var(--bg-card-solid);
  border-radius: 0.5rem;
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
}

.table-card {
  background: var(--bg-card-solid);
  border-radius: 0.5rem;
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
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
  color: var(--color-muted, var(--color-text-muted));
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
  background: var(--color-success-subtle);
  color: var(--color-success-dark);
}

.tag.default {
  background: var(--bg-stripe);
  color: var(--color-heading);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
}

th {
  font-weight: 600;
  color: var(--color-heading);
}

button.primary {
  background: var(--color-primary); color: var(--color-text-inverse);
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: none;
  cursor: pointer;
}

button.primary:hover {
  background: var(--color-primary-dark);
}

button.secondary {
  background: var(--bg-stripe);
  color: var(--color-heading);
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: none;
  cursor: pointer;
}

button.secondary:hover {
  background: var(--color-border);
}

button.ghost {
  background: transparent;
  color: var(--color-text-muted);
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: 1px solid var(--color-border-strong);
  cursor: pointer;
}

button.ghost:hover {
  background: var(--bg-stripe);
}

input, textarea, select {
  padding: 0.5rem;
  border: 1px solid var(--color-border-strong);
  border-radius: 0.25rem;
}

input:focus, textarea:focus, select:focus {
  outline: none;
  border-color: var(--color-primary);
}
</style>
