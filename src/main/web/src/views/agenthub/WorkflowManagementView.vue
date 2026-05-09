<template>
  <section class="workflow-management glass-float">
    <div class="page-header">
      <h2>工作流管理</h2>
      <p class="muted">管理Agent的工作流图定义</p>
    </div>

    <div class="toolbar">
      <button @click="showCreateDialog = true" class="btn-primary">新建工作流</button>
    </div>

    <div class="workflow-list">
      <table v-if="workflows.length > 0">
        <thead>
          <tr>
            <th>编码</th>
            <th>名称</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="workflow in workflows" :key="workflow.id">
            <td>{{ workflow.workflowCode }}</td>
            <td>{{ workflow.name }}</td>
            <td>
              <span :class="workflow.status === 'PUBLISHED' ? 'status-published' : 'status-draft'">
                {{ workflow.status }}
              </span>
            </td>
            <td>{{ formatDate(workflow.createdAt) }}</td>
            <td>
              <button @click="editWorkflow(workflow)" class="btn-small">编辑</button>
              <button @click="togglePublish(workflow)" class="btn-small">
                {{ workflow.status === 'PUBLISHED' ? '取消发布' : '发布' }}
              </button>
              <button @click="deleteWorkflowHandler(workflow.id)" class="btn-small btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">
        <p>暂无工作流数据</p>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <div v-if="showCreateDialog || showEditDialog" class="dialog-overlay">
      <div class="dialog">
        <h3>{{ showEditDialog ? '编辑工作流' : '新建工作流' }}</h3>
        <form @submit.prevent="showEditDialog ? updateWorkflowHandler() : createWorkflowHandler()">
          <div class="form-group">
            <label>工作流编码</label>
            <input v-model="form.workflowCode" :disabled="showEditDialog" required />
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
            <label>图定义 (JSON/DAG)</label>
            <textarea v-model="form.graphDefinition" rows="8"></textarea>
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
import { ref, onMounted } from 'vue'
import { useWorkspaceStore } from '@/store/workspace-store'
import { listWorkflows, createWorkflow, updateWorkflow, publishWorkflow, unpublishWorkflow, deleteWorkflow } from '@/api/workflow-api'
import type { Workflow } from '@/types/memory'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const workflows = ref<Workflow[]>([])
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingWorkflowId = ref('')

const form = ref({
  workflowCode: '',
  name: '',
  description: '',
  graphDefinition: '{}'
})

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

onMounted(async () => {

// 监听全局新增事件
onMounted(() => {
  window.addEventListener('global-add', () => {
    showCreateForm.value = true
  })
})
  await loadWorkflows()
})

async function loadWorkflows() {
  try {
    workflows.value = await listWorkflows(selection())
  } catch (e) {
    console.error('Failed to load workflows', e)
  }
}

async function createWorkflowHandler() {
  try {
    await createWorkflow(selection(), form.value.workflowCode, form.value.name, form.value.description, form.value.graphDefinition)
    await loadWorkflows()
    closeDialog()
  } catch (e) {
    console.error('Failed to create workflow', e)
  }
}

async function updateWorkflowHandler() {
  try {
    await updateWorkflow(selection(), editingWorkflowId.value, form.value.name, form.value.description, form.value.graphDefinition)
    await loadWorkflows()
    closeDialog()
  } catch (e) {
    console.error('Failed to update workflow', e)
  }
}

async function togglePublish(workflow: Workflow) {
  try {
    if (workflow.status === 'PUBLISHED') {
      await unpublishWorkflow(selection(), workflow.id)
    } else {
      await publishWorkflow(selection(), workflow.id)
    }
    await loadWorkflows()
  } catch (e) {
    console.error('Failed to toggle publish', e)
  }
}

async function deleteWorkflowHandler(id: string) {
  if (confirm('确定删除此工作流？')) {
    try {
      await deleteWorkflow(selection(), id)
      await loadWorkflows()
    } catch (e) {
      console.error('Failed to delete workflow', e)
    }
  }
}

function editWorkflow(workflow: Workflow) {
  editingWorkflowId.value = workflow.id
  form.value = {
    workflowCode: workflow.workflowCode,
    name: workflow.name,
    description: workflow.description,
    graphDefinition: workflow.graphDefinition
  }
  showEditDialog.value = true
}

function closeDialog() {
  showCreateDialog.value = false
  showEditDialog.value = false
  form.value = { workflowCode: '', name: '', description: '', graphDefinition: '{}' }
}

function formatDate(date: string): string {
  return new Date(date).toLocaleString()
}
</script>

<style scoped>
.workflow-management {
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.toolbar {
  margin-bottom: 1.5rem;
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

.status-published {
  color: #28a745;
}

.status-draft {
  color: #6c757d;
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
