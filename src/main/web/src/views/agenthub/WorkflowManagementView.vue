<template>
  <section class="workflow-management glass-float">
    <div class="page-header">
      <h2>工作流管理</h2>
      <p class="muted">管理Agent的工作流图定义</p>
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
    <ModalDialog
      v-model:visible="showCreateDialog"
      :title="editingWorkflowId ? '编辑工作流' : '新建工作流'"
      @close="closeDialog"
      @confirm="editingWorkflowId ? updateWorkflowHandler() : createWorkflowHandler()"
      :confirm-text="editingWorkflowId ? '更新' : '创建'"
      width="1200px"
    >
      <div class="workflow-form-container">
        <!-- 表单部分（上） -->
        <form class="workflow-form">
          <div class="form-row">
            <label class="field">
              <span>工作流编码 *</span>
              <input v-model="form.workflowCode" :disabled="!!editingWorkflowId" required />
            </label>
            <label class="field">
              <span>名称 *</span>
              <input v-model="form.name" required />
            </label>
            <label class="field">
              <span>描述</span>
              <textarea v-model="form.description" rows="2"></textarea>
            </label>
          </div>
        </form>

        <!-- DAG编辑器部分（下） -->
        <div class="dag-section">
          <div class="dag-title">工作流DAG图</div>
          <DagEditor v-model="form.graphDefinition" />
        </div>
      </div>
    </ModalDialog>
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
import DagEditor from '@/components/workflow/DagEditor.vue'

const store = useWorkspaceStore()
const workflows = ref<Workflow[]>([])
const showCreateDialog = ref(false)
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
  window.addEventListener('global-add', () => {
    editingWorkflowId.value = ''
    resetForm()
    showCreateDialog.value = true
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
  showCreateDialog.value = true
}

function resetForm() {
  form.value = { workflowCode: '', name: '', description: '', graphDefinition: '{}' }
}

function closeDialog() {
  showCreateDialog.value = false
  editingWorkflowId.value = ''
  resetForm()
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

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
}

.workflow-form-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.workflow-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: flex;
  gap: 1rem;
}

.form-row .field {
  flex: 1;
}

.dag-section {
  border-top: 1px solid #ddd;
  padding-top: 1rem;
}

.dag-title {
  font-weight: bold;
  margin-bottom: 0.5rem;
  color: #333;
}
</style>
