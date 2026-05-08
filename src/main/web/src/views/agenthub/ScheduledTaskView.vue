<template>
  <section class="scheduled-task-page">
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    
    <template v-else>
      <!-- Header -->
      <article class="panel header-panel">
        <div class="header-row">
          <div class="header-left">
            <h2>定时任务管理</h2>
            <p class="muted">管理定时执行的任务调度</p>
          </div>
          <div class="header-right">
            <button class="primary" @click="showCreateDialog = true">新建任务</button>
          </div>
        </div>
      </article>

      <!-- Task List -->
      <article class="panel">
        <div v-if="loading" class="loading">加载中...</div>
        
        <div v-else-if="tasks.length === 0" class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          <p>暂无定时任务</p>
        </div>

        <table v-else>
          <thead>
            <tr>
              <th>任务编码</th>
              <th>名称</th>
              <th>类型</th>
              <th>Cron表达式</th>
              <th>状态</th>
              <th>下次执行</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in tasks" :key="task.id">
              <td><code>{{ task.taskCode }}</code></td>
              <td>{{ task.name }}</td>
              <td><span class="tag">{{ getTaskTypeLabel(task.taskType) }}</span></td>
              <td><code>{{ task.cronExpression }}</code></td>
              <td>
                <span :class="['status-badge', task.enabled ? 'enabled' : 'disabled']">
                  {{ task.enabled ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ task.nextExecuteTime ? formatDateTime(task.nextExecuteTime) : '-' }}</td>
              <td>{{ formatDateTime(task.createdAt) }}</td>
              <td>
                <div class="action-buttons">
                  <button class="btn-small" @click="editTask(task)">编辑</button>
                  <button 
                    :class="['btn-small', task.enabled ? 'btn-warning' : 'btn-success']"
                    @click="toggleEnabled(task)"
                  >
                    {{ task.enabled ? '禁用' : '启用' }}
                  </button>
                  <button class="btn-small btn-info" @click="executeTask(task)">执行</button>
                  <button class="btn-small btn-danger" @click="deleteTaskHandler(task.id)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </article>

      <!-- Create/Edit Dialog -->
      <div v-if="showCreateDialog || showEditDialog" class="dialog-overlay" @click.self="closeDialog">
        <div class="dialog">
          <h3>{{ showEditDialog ? '编辑任务' : '新建任务' }}</h3>
          <form @submit.prevent="showEditDialog ? updateTaskHandler() : createTaskHandler()">
            <div class="form-group">
              <label>任务编码</label>
              <input v-model="form.taskCode" :disabled="showEditDialog" required />
            </div>
            <div class="form-group">
              <label>名称</label>
              <input v-model="form.name" required />
            </div>
            <div class="form-group">
              <label>描述</label>
              <textarea v-model="form.description" rows="3"></textarea>
            </div>
            <div class="form-group">
              <label>任务类型</label>
              <select v-model="form.taskType" required :disabled="showEditDialog">
                <option value="AGENT_CALL">Agent调用</option>
                <option value="WORKFLOW">工作流</option>
                <option value="DATA_SYNC">数据同步</option>
                <option value="CLEANUP">清理任务</option>
              </select>
            </div>
            <div class="form-group">
              <label>Cron表达式</label>
              <input v-model="form.cronExpression" required placeholder="0 0 2 * * ?" />
              <small class="hint">示例: 0 0 2 * * ? (每天凌晨2点执行)</small>
            </div>
            <div class="form-group">
              <label>执行器配置 (JSON)</label>
              <textarea v-model="form.executorConfig" rows="3" placeholder='{"agentId": "xxx"}'></textarea>
            </div>
            <div class="form-group">
              <label>Prompt提示词</label>
              <textarea v-model="form.prompt" rows="5" placeholder="输入任务执行的提示词..."></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn-secondary" @click="closeDialog">取消</button>
              <button type="submit" class="btn-primary">{{ showEditDialog ? '更新' : '创建' }}</button>
            </div>
          </form>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useWorkspaceStore } from '@/store/workspace-store'
import { 
  listScheduledTasks, 
  createScheduledTask, 
  updateScheduledTask, 
  enableScheduledTask, 
  disableScheduledTask, 
  deleteScheduledTask,
  executeScheduledTask
} from '@/api/scheduled-task-api'
import type { ScheduledTask } from '@/types/scheduled-task'

const store = useWorkspaceStore()
const tasks = ref<ScheduledTask[]>([])
const loading = ref(false)
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingTaskId = ref('')

const form = ref({
  taskCode: '',
  name: '',
  description: '',
  taskType: 'AGENT_CALL',
  cronExpression: '',
  executorConfig: '{}',
  prompt: ''
})

const selectionReady = computed(() => !!store.tenantId && !!store.workspaceId)

function getSelection() {
  return {
    tenantId: store.tenantId!,
    workspaceId: store.workspaceId!
  }
}

function getTaskTypeLabel(type: string) {
  const labels: Record<string, string> = {
    'AGENT_CALL': 'Agent调用',
    'WORKFLOW': '工作流',
    'DATA_SYNC': '数据同步',
    'CLEANUP': '清理任务'
  }
  return labels[type] || type
}

async function loadTasks() {
  if (!selectionReady.value) return
  loading.value = true
  try {
    tasks.value = await listScheduledTasks(getSelection())
  } catch (error) {
    console.error('Failed to load tasks:', error)
  } finally {
    loading.value = false
  }
}

async function createTaskHandler() {
  try {
    await createScheduledTask(getSelection().workspaceId, {
      ...getSelection(),
      taskCode: form.value.taskCode,
      name: form.value.name,
      description: form.value.description,
      taskType: form.value.taskType,
      cronExpression: form.value.cronExpression,
      executorConfig: form.value.executorConfig,
      prompt: form.value.prompt
    })
    await loadTasks()
    closeDialog()
  } catch (error) {
    console.error('Failed to create task:', error)
  }
}

function editTask(task: ScheduledTask) {
  editingTaskId.value = task.id
  form.value = {
    taskCode: task.taskCode,
    name: task.name,
    description: task.description,
    taskType: task.taskType,
    cronExpression: task.cronExpression,
    executorConfig: task.executorConfig,
    prompt: task.prompt
  }
  showEditDialog.value = true
}

async function updateTaskHandler() {
  try {
    await updateScheduledTask(getSelection().workspaceId, editingTaskId.value, {
      name: form.value.name,
      description: form.value.description,
      cronExpression: form.value.cronExpression,
      executorConfig: form.value.executorConfig,
      prompt: form.value.prompt
    })
    await loadTasks()
    closeDialog()
  } catch (error) {
    console.error('Failed to update task:', error)
  }
}

async function toggleEnabled(task: ScheduledTask) {
  try {
    if (task.enabled) {
      await disableScheduledTask(getSelection().workspaceId, task.id)
    } else {
      await enableScheduledTask(getSelection().workspaceId, task.id)
    }
    await loadTasks()
  } catch (error) {
    console.error('Failed to toggle enabled:', error)
  }
}

async function executeTask(task: ScheduledTask) {
  try {
    await executeScheduledTask(getSelection().workspaceId, task.id)
    await loadTasks()
  } catch (error) {
    console.error('Failed to execute task:', error)
  }
}

async function deleteTaskHandler(taskId: string) {
  if (!confirm('确定要删除这个任务吗？')) return
  try {
    await deleteScheduledTask(getSelection().workspaceId, taskId)
    await loadTasks()
  } catch (error) {
    console.error('Failed to delete task:', error)
  }
}

function closeDialog() {
  showCreateDialog.value = false
  showEditDialog.value = false
  editingTaskId.value = ''
  form.value = {
    taskCode: '',
    name: '',
    description: '',
    taskType: 'AGENT_CALL',
    cronExpression: '',
    executorConfig: '{}',
    prompt: ''
  }
}

function formatDateTime(date: string) {
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(loadTasks)
watch(() => [store.tenantId, store.workspaceId], loadTasks)
</script>

<style scoped>
.scheduled-task-page {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.panel {
  background: white;
  border-radius: 0.5rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-panel {
  border-bottom: 1px solid #e5e7eb;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #111827;
}

.header-left .muted {
  margin: 0.25rem 0 0 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.header-right {
  display: flex;
  gap: 0.75rem;
}

button.primary {
  padding: 0.5rem 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  font-weight: 500;
}

button.primary:hover {
  background: #2563eb;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #9ca3af;
}

.empty-state svg {
  margin-bottom: 1rem;
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
  background: #f9fafb;
}

code {
  background: #f3f4f6;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.875rem;
}

.tag {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  background: #dbeafe;
  color: #1e40af;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-badge.enabled {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.disabled {
  background: #fee2e2;
  color: #991b1b;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn-small {
  padding: 0.25rem 0.5rem;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 0.75rem;
}

.btn-small:hover {
  background: #f3f4f6;
}

.btn-warning {
  background: #fef3c7;
  color: #92400e;
  border-color: #fcd34d;
}

.btn-success {
  background: #d1fae5;
  color: #065f46;
  border-color: #6ee7b7;
}

.btn-info {
  background: #dbeafe;
  color: #1e40af;
  border-color: #93c5fd;
}

.btn-danger {
  background: #fee2e2;
  color: #991b1b;
  border-color: #fca5a5;
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
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 0.5rem;
  padding: 1.5rem;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.dialog h3 {
  margin: 0 0 1rem 0;
  font-size: 1.25rem;
  color: #111827;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 0.25rem;
  font-size: 0.875rem;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group .hint {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #6b7280;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.btn-primary {
  padding: 0.5rem 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-secondary {
  padding: 0.5rem 1rem;
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
}

.btn-secondary:hover {
  background: #e5e7eb;
}
</style>
