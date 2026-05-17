<template>
  <section class="memory-management glass-float">
    <div class="page-header">
      <div>
        <h2>记忆管理</h2>
        <p class="muted">管理Agent的长期记忆存储</p>
      </div>
      <div class="header-right">
          <CustomSelect v-model="selectedAgentId"  :options="agentsOptions" placeholder="请选择Agent" />
      </div>
    </div>

    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>

    <div class="memory-list" v-else>
      <table v-if="memories.length > 0">
        <thead>
          <tr>
            <th>名称</th>
            <th>类型</th>
            <th>内容</th>
            <th>重要性</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="memory in memories" :key="memory.id">
            <td>{{ memory.name || '-' }}</td>
            <td>{{ memory.memoryType }}</td>
            <td>{{ truncateContent(memory.content) }}</td>
            <td>{{ memory.importance.toFixed(2) }}</td>
            <td>{{ formatDate(memory.createdAt) }}</td>
            <td>
              <button @click="editMemory(memory)" class="btn-small">编辑</button>
              <button @click="deleteMemoryHandler(memory.id)" class="btn-small btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">
        <p>暂无记忆数据</p>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <ModalDialog
      v-model:visible="showCreateDialog"
      :title="showEditDialog ? '编辑记忆' : '新建记忆'"
      @confirm="showEditDialog ? updateMemoryHandler() : createMemoryHandler()"
      @close="closeDialog"
      :confirm-text="showEditDialog ? '更新' : '创建'"
    >
      <form>
        <div class="form-group">
          <label>名称</label>
          <input type="text" v-model="form.name" placeholder="请输入记忆名称" />
        </div>
        <div class="form-group">
          <label>记忆类型</label>
          <select v-model="form.memoryType" required>
            <option value="EPISODIC">情景记忆</option>
            <option value="SEMANTIC">语义记忆</option>
            <option value="PROCEDURAL">程序记忆</option>
          </select>
        </div>
        <div class="form-group">
          <label>内容</label>
          <textarea v-model="form.content" rows="5" required></textarea>
        </div>
        <div class="form-group">
          <label>重要性 (0-1)</label>
          <input type="number" v-model.number="form.importance" min="0" max="1" step="0.1" required />
        </div>
      </form>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useWorkspaceStore } from '@/store/workspace-store'
import { listMemoriesByAgent, createMemory, updateMemory, deleteMemory } from '@/api/memory-api'
import { listAgents } from '@/api/agent-api'
import type { Memory } from '@/types/memory'
import type { Agent } from '@/types/agent'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const agents = ref<Agent[]>([])
const memories = ref<Memory[]>([])
const selectedAgentId = ref('')
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingMemoryId = ref('')
const agentsOptions = computed(() => agents.value.map(agent => ({ value: agent.id, label: agent.name })))

const form = ref({
  name: '',
  memoryType: 'EPISODIC',
  content: '',
  importance: 0.5
})

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

onMounted(async () => {
  // 监听全局新增事件
  window.addEventListener('global-add', () => {
    if (!selectedAgentId.value) {
      alert('请先选择一个Agent')
      return
    }
    openCreateDialog()
  })

  // 加载Agent列表
  await loadAgents()
})

// 监听租户和工作区变化
watch(() => [store.tenantId, store.workspaceId], async () => {
  await loadAgents()
})

async function loadAgents() {
  if (!selectionReady.value) {
    agents.value = []
    return
  }

  try {
    agents.value = await listAgents(selection())
    // 默认选中第一个Agent
    if (agents.value.length > 0 && !selectedAgentId.value) {
      selectedAgentId.value = agents.value[0].id
    }
  } catch (e) {
    console.error('Failed to load agents', e)
  }
}

function openCreateDialog() {
  editingMemoryId.value = ''
  showEditDialog.value = false
  form.value = {
    name: '',
    memoryType: 'EPISODIC',
    content: '',
    importance: 0.5
  }
  showCreateDialog.value = true
}

watch(selectedAgentId, async (newId) => {
  if (newId) {
    try {
      memories.value = await listMemoriesByAgent(selection(), newId)
    } catch (e) {
      console.error('Failed to load memories', e)
    }
  } else {
    memories.value = []
  }
})

async function createMemoryHandler() {
  if (!selectionReady.value) return
  if (!selectedAgentId.value) {
    alert('请先选择一个Agent')
    return
  }
  try {
    await createMemory(selection(), selectedAgentId.value, form.value.name, form.value.memoryType, form.value.content, '{}', form.value.importance)
    memories.value = await listMemoriesByAgent(selection(), selectedAgentId.value)
    closeDialog()
  } catch (e) {
    console.error('Failed to create memory', e)
  }
}

async function updateMemoryHandler() {
  if (!selectionReady.value) return
  try {
    await updateMemory(selection(), editingMemoryId.value, form.value.name, form.value.content, '{}', form.value.importance)
    memories.value = await listMemoriesByAgent(selection(), selectedAgentId.value)
    closeDialog()
  } catch (e) {
    console.error('Failed to update memory', e)
  }
}

async function deleteMemoryHandler(id: string) {
  if (!selectionReady.value) return
  if (confirm('确定删除此记忆？')) {
    try {
      await deleteMemory(selection(), id)
      memories.value = await listMemoriesByAgent(selection(), selectedAgentId.value)
    } catch (e) {
      console.error('Failed to delete memory', e)
    }
  }
}function editMemory(memory: Memory) {
  editingMemoryId.value = memory.id
  form.value = {
    name: memory.name || '',
    memoryType: memory.memoryType,
    content: memory.content,
    importance: memory.importance
  }
  showCreateDialog.value = true
  showEditDialog.value = true
}

function closeDialog() {
  showCreateDialog.value = false
  showEditDialog.value = false
  form.value = { memoryType: 'EPISODIC', content: '', importance: 0.5 }
}

function truncateContent(content: string): string {
  return content.length > 50 ? content.substring(0, 50) + '...' : content
}

function formatDate(date: string): string {
  return new Date(date).toLocaleString()
}
</script>

<style scoped>
.memory-management {
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.agent-select {
  width: auto;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: 1px solid #ddd;
  background: white;
  font-size: 0.9rem;
  min-width: 200px;
}

.memory-list {
  background: white;
  border-radius: 8px;
  padding: 1rem;
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary {
  background: #007bff;
  color: white;
  border: none;
}

.btn-secondary {
  background: #6c757d;
  color: white;
  border: none;
  cursor: pointer;
}

.btn-small {
  padding: 0.25rem 0.5rem;
  margin-right: 0.5rem;
  cursor: pointer;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  min-width: 400px;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
}
</style>
