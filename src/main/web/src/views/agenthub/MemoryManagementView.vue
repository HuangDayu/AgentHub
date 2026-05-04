<template>
  <section class="memory-management">
    <div class="page-header">
      <h2>记忆管理</h2>
      <p class="muted">管理Agent的长期记忆存储</p>
    </div>

    <div class="toolbar">
      <select v-model="selectedAgentId" class="agent-select">
        <option value="">选择Agent</option>
        <option v-for="agent in agents" :key="agent.id" :value="agent.id">
          {{ agent.name }}
        </option>
      </select>
      <button @click="showCreateDialog = true" :disabled="!selectedAgentId" class="btn-primary">
        新建记忆
      </button>
    </div>

    <div class="memory-list">
      <table v-if="memories.length > 0">
        <thead>
          <tr>
            <th>类型</th>
            <th>内容</th>
            <th>重要性</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="memory in memories" :key="memory.id">
            <td>{{ memory.memoryType }}</td>
            <td>{{ truncateContent(memory.content) }}</td>
            <td>{{ memory.importance.toFixed(2) }}</td>
            <td>{{ formatDate(memory.createdAt) }}</td>
            <td>
              <button @click="editMemory(memory)" class="btn-small">编辑</button>
              <button @click="deleteMemory(memory.id)" class="btn-small btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">
        <p>暂无记忆数据</p>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <div v-if="showCreateDialog || showEditDialog" class="dialog-overlay">
      <div class="dialog">
        <h3>{{ showEditDialog ? '编辑记忆' : '新建记忆' }}</h3>
        <form @submit.prevent="showEditDialog ? updateMemoryHandler() : createMemoryHandler()">
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
          <div class="form-actions">
            <button type="button" @click="closeDialog" class="btn-secondary">取消</button>
            <button type="submit" class="btn-primary">{{ showEditDialog ? '更新' : '创建' }}</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useWorkspaceStore } from '@/store/workspace-store'
import { listMemoriesByAgent, createMemory, updateMemory, deleteMemory } from '@/api/memory-api'
import { listAgents } from '@/api/agent-api'
import type { Memory } from '@/types/memory'
import type { Agent } from '@/types/agent'

const store = useWorkspaceStore()
const agents = ref<Agent[]>([])
const memories = ref<Memory[]>([])
const selectedAgentId = ref('')
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingMemoryId = ref('')

const form = ref({
  memoryType: 'EPISODIC',
  content: '',
  importance: 0.5
})

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

onMounted(async () => {
  try {
    agents.value = await listAgents(selection())
  } catch (e) {
    console.error('Failed to load agents', e)
  }
})

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
  try {
    await createMemory(selection(), selectedAgentId.value, form.value.memoryType, form.value.content, '{}', form.value.importance)
    memories.value = await listMemoriesByAgent(selection(), selectedAgentId.value)
    closeDialog()
  } catch (e) {
    console.error('Failed to create memory', e)
  }
}

async function updateMemoryHandler() {
  try {
    await updateMemory(selection(), editingMemoryId.value, form.value.content, '{}', form.value.importance)
    memories.value = await listMemoriesByAgent(selection(), selectedAgentId.value)
    closeDialog()
  } catch (e) {
    console.error('Failed to update memory', e)
  }
}

async function deleteMemoryHandler(id: string) {
  if (confirm('确定删除此记忆？')) {
    try {
      await deleteMemory(selection(), id)
      memories.value = await listMemoriesByAgent(selection(), selectedAgentId.value)
    } catch (e) {
      console.error('Failed to delete memory', e)
    }
  }
}

function editMemory(memory: Memory) {
  editingMemoryId.value = memory.id
  form.value = {
    memoryType: memory.memoryType,
    content: memory.content,
    importance: memory.importance
  }
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
  margin-bottom: 2rem;
}

.toolbar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.agent-select {
  padding: 0.5rem;
  min-width: 200px;
}

.btn-primary {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: white;
  border: none;
  cursor: pointer;
}

.btn-secondary {
  padding: 0.5rem 1rem;
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
