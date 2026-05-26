<template>
  <section class="workflow-management glass-float">
    <div class="page-header">
      <div>
      <h2>工作流</h2>
      <p class="muted">管理Agent的工作流图定义</p>
      </div>
    </div>

    <div class="workflow-list float-effect">
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
              <CustomButton type="ghost" @click="editWorkflow(workflow)">编辑</CustomButton>
              <CustomButton type="ghost" @click="openWorkflowEditor(workflow)">工作流</CustomButton>
              <CustomButton type="ghost" @click="togglePublish(workflow)">
                {{ workflow.status === 'PUBLISHED' ? '取消发布' : '发布' }}
              </CustomButton>
              <CustomButton type="ghost" @click="deleteWorkflowHandler(workflow.id)">删除</CustomButton>
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
              <textarea v-model="form.description"></textarea>
            </label>
          </div>
        </form>
      </div>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '@/store/workspace-store'
import { listWorkflows, createWorkflow, updateWorkflow, publishWorkflow, unpublishWorkflow, deleteWorkflow } from '@/api/workflow-api'
import type { Workflow } from '@/types/workflow'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const router = useRouter()

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

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

watch([() => store.tenantId, () => store.workspaceId], () => {
  if (selectionReady.value) {
    loadWorkflows()
  }
})

onMounted(() => {
  // 监听全局新增事件
  window.addEventListener('global-add', () => {
    editingWorkflowId.value = ''
    resetForm()
    showCreateDialog.value = true
  })
  if (selectionReady.value) {
    loadWorkflows()
  }
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
  // 打开编辑对话框，填充表单
  editingWorkflowId.value = workflow.id
  form.value = {
    workflowCode: workflow.workflowCode,
    name: workflow.name,
    description: workflow.description,
    graphDefinition: workflow.graphDefinition || '{}'
  }
  showCreateDialog.value = true
}

function openWorkflowEditor(workflow: Workflow) {
  router.push(`/agenthub/workflows/${workflow.id}`)
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
  background: var(--color-primary); color: var(--color-text-inverse);
  border: none;
  cursor: pointer;
}

.btn-secondary {
  padding: 0.5rem 1rem;
  background: var(--color-text-muted); color: var(--color-text-inverse);
  border: none;
  cursor: pointer;
}

.status-published {
  color: var(--color-success);
}

.status-draft {
  color: var(--color-text-muted);
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
  color: var(--color-text-muted);
}
</style>
