<template>
  <section class="grid">
    <div class="page-header">
      <div>
        <h2>工作区管理</h2>
        <p class="muted">管理您的工作区，选择一个工作区开始使用知识库和Agent功能。</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else>


      <!-- 工作区列表 -->
      <article class="panel stack">
        <div class="page-header">
          <h3 style="margin: 0">工作区列表</h3>
          <button class="primary" type="button" @click="showCreateForm = true">新建工作区</button>
        </div>

        <!-- 创建/编辑表单 -->
        <form v-if="showCreateForm || editingWorkspace" class="field-grid" @submit.prevent="submitWorkspace">
          <label class="field">
            <span>工作区代码 *</span>
            <input v-model="workspaceForm.workspaceCode" :disabled="!!editingWorkspace" required placeholder="workspace-001" />
          </label>
          <label class="field">
            <span>名称 *</span>
            <input v-model="workspaceForm.name" required placeholder="我的工作区" />
          </label>
          <label class="field">
            <span>区域</span>
            <input v-model="workspaceForm.region" placeholder="cn-north-1" />
          </label>
          <div class="form-actions">
            <button class="ghost" type="button" @click="cancelForm">取消</button>
            <button class="primary" type="submit">{{ editingWorkspace ? '更新' : '创建' }}</button>
          </div>
        </form>

        <!-- 工作区表格 -->
        <div v-if="workspaces.length === 0" class="empty-state">暂无工作区，请创建一个工作区。</div>
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>代码</th>
              <th>区域</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="ws in workspaces" :key="ws.id" :class="{ 'selected-row': ws.id === store.workspaceId }">
              <td>{{ ws.name }}</td>
              <td>{{ ws.workspaceCode }}</td>
              <td>{{ ws.region || '-' }}</td>
              <td>{{ formatDateTime(ws.createdAt) }}</td>
              <td>
                <div class="action-buttons">
                  <button class="ghost" @click="startEdit(ws)">编辑</button>
                  <button class="danger" @click="handleDelete(ws.id)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </article>


    </template>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { formatDateTime } from '@/common/format'
import {
  getCurrentUser,
  listWorkspaces,
  createWorkspace,
  updateWorkspace,
  deleteWorkspace,
} from '@/api/tenant-api'
import type { Workspace } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'

const store = useWorkspaceStore()
const error = ref('')
const loading = ref(true)
const workspaces = ref<Workspace[]>([])

// 表单状态
const showCreateForm = ref(false)
const editingWorkspace = ref<Workspace | null>(null)
const workspaceForm = reactive({
  workspaceCode: '',
  name: '',
  region: '',
})

onMounted(init)

async function init() {
  await execute(async () => {
    // 设置租户ID（从store获取或从API获取）
    if (!store.tenantId) {
      const user = await getCurrentUser()
      store.selectTenant(user.tenantId)
    }
    // 加载工作区列表
    await loadWorkspaces()
  })
  loading.value = false
}

async function loadWorkspaces() {
  if (!store.tenantId) return
  await execute(async () => {
    workspaces.value = await listWorkspaces(store.tenantId)
    // 如果没有选择工作区，自动选择第一个
    if (workspaces.value.length && !workspaces.value.some(w => w.id === store.workspaceId)) {
      store.selectWorkspace(workspaces.value[0].id)
    }
    // 触发工作区列表更新事件
    window.dispatchEvent(new CustomEvent('workspace-list-updated'))
  })
}

function startEdit(ws: Workspace) {
  editingWorkspace.value = ws
  workspaceForm.workspaceCode = ws.workspaceCode
  workspaceForm.name = ws.name
  workspaceForm.region = ws.region || ''
}

function cancelForm() {
  showCreateForm.value = false
  editingWorkspace.value = null
  workspaceForm.workspaceCode = ''
  workspaceForm.name = ''
  workspaceForm.region = ''
}

async function submitWorkspace() {
  if (!store.tenantId) return
  await execute(async () => {
    if (editingWorkspace.value) {
      // 更新工作区
      await updateWorkspace(editingWorkspace.value.id, {
        name: workspaceForm.name,
      })
    } else {
      // 创建工作区
      await createWorkspace(store.tenantId!, {
        workspaceCode: workspaceForm.workspaceCode,
        name: workspaceForm.name,
        region: workspaceForm.region || undefined,
      })
    }
    cancelForm()
    await loadWorkspaces()
  })
}

async function handleDelete(workspaceId: string) {
  if (!confirm('确定要删除这个工作区吗？')) return
  await execute(async () => {
    await deleteWorkspace(workspaceId)
    // 如果删除的是当前选择的工作区，清除选择
    if (store.workspaceId === workspaceId) {
      store.selectWorkspace('')
    }
    await loadWorkspaces()
  })
}

async function execute(action: () => Promise<void>) {
  error.value = ''
  try {
    await action()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '请求失败'
  }
}
</script>

<style scoped>
.loading-state {
  text-align: center;
  padding: 2rem;
  color: #5f6878;
}

.user-info {
  display: grid;
  gap: 0.5rem;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field span {
  font-weight: 500;
  color: #264266;
}

.field input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid rgba(38, 66, 102, 0.2);
  border-radius: 4px;
}

.form-actions {
  grid-column: 1 / -1;
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  border: 1px solid rgba(22, 33, 50, 0.08);
  padding: 0.75rem;
  text-align: left;
}

.data-table th {
  background: rgba(248, 250, 255, 0.95);
  font-weight: 600;
  color: #264266;
}

.data-table tr:hover {
  background: rgba(248, 250, 255, 0.5);
}

.selected-row {
  background: rgba(58, 138, 214, 0.1) !important;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.action-buttons .active {
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: white;
}

.info-card {
  background: rgba(58, 138, 214, 0.05);
  border: 1px solid rgba(58, 138, 214, 0.2);
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #5f6878;
}
</style>
