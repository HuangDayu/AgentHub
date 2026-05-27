<template>
  <section class="workspace-page glass-float">
    <div class="page-header fade-in">
      <div>
        <h2>工作区管理</h2>
        <p class="muted">管理您的工作区，选择一个工作区开始使用知识库和Agent功能。</p>
      </div>
      <p v-if="error" class="status">{{ error }}</p>
    </div>

    <div v-if="loading" class="loading-state scale-in">
      <div class="spinner-large"></div>
      <p>加载中...</p>
    </div>

    <template v-else>
      <!-- 工作区列表 -->
      <article class="panel workspace-panel slide-in float-effect">
        <div class="panel-header">
          <h3>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="header-icon">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
            工作区列表
          </h3>
          
        </div>

        <!-- 创建/编辑弹窗 -->
        <ModalDialog
          v-model:visible="showCreateForm"
          :title="editingWorkspace ? '编辑工作区' : '创建新工作区'"
          @confirm="submitWorkspace"
          @close="showCreateForm = false"
          :confirm-disabled="loading"
          :confirm-text="editingWorkspace ? '更新' : '创建'"
        >
          <form>
            <div class="form-grid">
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
            </div>
          </form>
        </ModalDialog>

        <!-- 工作区卡片列表 -->
        <div v-if="workspaces.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
              <line x1="12" y1="11" x2="12" y2="17"/>
              <line x1="9" y1="14" x2="15" y2="14"/>
            </svg>
          </div>
          <p>暂无工作区</p>
          <p class="muted">点击上方按钮创建您的第一个工作区</p>
        </div>
        
        <div v-else class="workspace-grid">
          <div 
            v-for="(ws, index) in workspaces" 
            :key="ws.id" 
            :class="['workspace-card', { 'selected': ws.id === store.workspaceId, 'fade-in': index < 3 }]"
            @click="store.selectWorkspace(ws.id)"
          >
            <div class="card-header">
              <div class="card-icon" :class="{ 'active': ws.id === store.workspaceId }">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                </svg>
              </div>
              <div class="card-actions">
                <button class="icon-btn" @click.stop="startEdit(ws)" title="编辑">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
                <button class="icon-btn danger" @click.stop="handleDelete(ws.id)" title="删除">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                </button>
              </div>
            </div>
            <div class="card-body">
              <h4>{{ ws.name }}</h4>
              <div class="card-meta">
                <span class="meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                    <line x1="3" y1="9" x2="21" y2="9"/>
                    <line x1="9" y1="21" x2="9" y2="9"/>
                  </svg>
                  {{ ws.workspaceCode }}
                </span>
                <span v-if="ws.region" class="meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="2" y1="12" x2="22" y2="12"/>
                    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                  </svg>
                  {{ ws.region }}
                </span>
              </div>
              <div class="card-footer">
                <span class="timestamp">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12 6 12 12 16 14"/>
                  </svg>
                  {{ formatDateTime(ws.createdAt) }}
                </span>
                <span v-if="ws.id === store.workspaceId" class="status-badge">当前选择</span>
              </div>
            </div>
          </div>
        </div>
      </article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { showConfirm } from '@/utils/confirm'
import ModalDialog from '@/components/ModalDialog.vue'
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

onMounted(async () => {
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
  
  // 监听全局新增事件
  window.addEventListener('global-add', () => {
    editingWorkspace.value = null
    showCreateForm.value = true
  })
})

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
  showCreateForm.value = true
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
  if (!await showConfirm('确定要删除这个工作区吗？')) return
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
.workspace-page {
  display: grid;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
}

.page-header h2 {
  margin: 0;
  font-size: 1.75rem;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.muted {
  color: var(--color-text-muted);
  margin: 4px 0 0;
}

.status {
  color: var(--color-error);
  font-weight: 500;
  padding: 8px 16px;
  background: rgba(201, 74, 53, 0.08);
  border-radius: 12px;
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  gap: 16px;
  color: var(--color-text-muted);
}

.spinner-large {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(58, 123, 213, 0.2);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Panel */
.workspace-panel {
  padding: 24px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.panel-header h3 {
  margin: 0;
  font-size: 1.25rem;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-primary-dark);
}

.header-icon {
  width: 24px;
  height: 24px;
  color: var(--color-primary);
}

.btn-icon {
  width: 18px;
  height: 18px;
}

/* Form */
.workspace-form {
  background: var(--bg-stripe);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.form-header h4 {
  margin: 0;
  color: var(--color-primary-dark);
}

.close-btn {
  width: 32px;
  height: 32px;
  padding: 6px;
  border: none;
  background: rgba(38, 66, 102, 0.08);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(201, 74, 53, 0.1);
  color: var(--color-error);
}

.close-btn svg {
  width: 100%;
  height: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field span {
  font-weight: 600;
  color: var(--color-primary-dark);
  font-size: 0.9rem;
}

.field input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: var(--bg-card-solid);
  font: inherit;
  font-size: 0.95rem;
  transition: all 0.25s ease;
}

.field input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(58, 123, 213, 0.15);
}

.field input:disabled {
  background: var(--bg-hover);
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* Buttons */
.primary, .secondary {
  padding: 10px 20px;
  border-radius: 12px;
  font: inherit;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.25s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.primary {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  border: none;
  box-shadow: 0 4px 12px rgba(58, 123, 213, 0.2);
}

.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(58, 138, 214, 0.3);
}

.secondary {
  background: var(--bg-card-solid);
  color: var(--color-primary-dark);
  border: 1px solid var(--color-border);
}

.secondary:hover {
  border-color: var(--color-primary);
  background: var(--bg-hover);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 48px 24px;
  color: var(--color-text-muted);
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  padding: 16px;
  background: linear-gradient(135deg, var(--color-primary-subtle), rgba(58, 138, 214, 0.05));
  border-radius: 16px;
  color: var(--color-primary);
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

/* Workspace Grid */
.workspace-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.workspace-card {
  background: var(--bg-card-solid);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.workspace-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, transparent);
  transition: background 0.3s ease;
}

.workspace-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(26, 30, 43, 0.12);
  border-color: rgba(58, 123, 213, 0.2);
}

.workspace-card.selected {
  border-color: var(--color-primary);
  box-shadow: 0 8px 20px rgba(58, 123, 213, 0.15);
}

.workspace-card.selected::before {
  background: linear-gradient(90deg, var(--color-primary-dark), var(--color-primary));
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-icon {
  width: 40px;
  height: 40px;
  padding: 10px;
  background: var(--color-primary-subtle);
  border-radius: 10px;
  color: var(--color-primary);
  transition: all 0.3s ease;
}

.card-icon.active {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
}

.card-icon svg {
  width: 100%;
  height: 100%;
}

.card-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.workspace-card:hover .card-actions {
  opacity: 1;
}

.icon-btn {
  width: 32px;
  height: 32px;
  padding: 6px;
  border: none;
  background: rgba(38, 66, 102, 0.08);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary-dark);
}

.icon-btn:hover {
  background: rgba(58, 123, 213, 0.15);
  color: var(--color-primary);
}

.icon-btn.danger:hover {
  background: rgba(201, 74, 53, 0.1);
  color: var(--color-error);
}

.icon-btn svg {
  width: 100%;
  height: 100%;
}

.card-body h4 {
  margin: 0 0 12px;
  font-size: 1.1rem;
  color: var(--color-text);
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

.meta-item svg {
  width: 14px;
  height: 14px;
  opacity: 0.6;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid rgba(22, 33, 50, 0.06);
}

.timestamp {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: var(--color-text-light);
}

.timestamp svg {
  width: 14px;
  height: 14px;
}

.status-badge {
  padding: 4px 10px;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Animations */
.fade-in {
  animation: fade-in 0.4s ease forwards;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.slide-in {
  animation: slide-in 0.4s ease forwards;
}

@keyframes slide-in {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.scale-in {
  animation: scale-in 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes scale-in {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

/* Responsive */
@media (max-width: 768px) {
  .workspace-page {
    gap: 16px;
  }
  
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .workspace-grid {
    grid-template-columns: 1fr;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
