<template>
  <div class="strategy-panel">
    <div class="panel-header">
      <h3>工具策略配置</h3>
      
    </div>

    <!-- 创建弹窗 -->
    <ModalDialog
      v-model:visible="showCreateForm"
      title="创建工具策略"
      @confirm="createStrategy"
      @close="showCreateForm = false"
      :confirm-disabled="loading"
      confirm-text="创建"
    >
      <form>
        <label class="field">
          <span>策略名称 *</span>
          <input v-model="newStrategy.name" required placeholder="输入策略名称" />
        </label>
        <label class="field">
          <span>描述</span>
          <textarea v-model="newStrategy.description" placeholder="输入策略描述"></textarea>
        </label>
        <label class="field">
          <span>最大并发调用数</span>
          <input type="number" v-model.number="newStrategy.maxConcurrentCalls" placeholder="5" />
        </label>
        <label class="field">
          <span>超时时间(秒)</span>
          <input type="number" v-model.number="newStrategy.timeoutSeconds" placeholder="30" />
        </label>
        <label class="field">
          <span>重试次数</span>
          <input type="number" v-model.number="newStrategy.retryCount" placeholder="3" />
        </label>
        <label class="field">
          <span>启用降级</span>
          <CustomSelect v-model="newStrategy.fallbackEnabled" :options="booleanOptions" />
        </label>
      </form>
    </ModalDialog>

    <!-- 编辑弹窗 -->
    <ModalDialog
      v-model:visible="showEditForm"
      title="编辑工具策略"
      @confirm="updateStrategy"
      @close="showEditForm = false"
      :confirm-disabled="loading"
      confirm-text="更新"
    >
      <form>
        <label class="field">
          <span>策略名称 *</span>
          <input v-model="editStrategyData.name" required placeholder="输入策略名称" />
        </label>
        <label class="field">
          <span>描述</span>
          <textarea v-model="editStrategyData.description" placeholder="输入策略描述"></textarea>
        </label>
        <label class="field">
          <span>最大并发调用数</span>
          <input type="number" v-model.number="editStrategyData.maxConcurrentCalls" />
        </label>
        <label class="field">
          <span>超时时间(秒)</span>
          <input type="number" v-model.number="editStrategyData.timeoutSeconds" />
        </label>
        <label class="field">
          <span>重试次数</span>
          <input type="number" v-model.number="editStrategyData.retryCount" />
        </label>
        <label class="field">
          <span>启用降级</span>
          <CustomSelect v-model="editStrategyData.fallbackEnabled" :options="booleanOptions" />
        </label>
      </form>
    </ModalDialog>

    <!-- 策略列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="strategies.length === 0" class="empty-state">暂无工具策略</div>
    <table v-else class="strategy-table">
      <thead>
        <tr>
          <th>名称</th>
          <th>最大并发</th>
          <th>超时(秒)</th>
          <th>重试次数</th>
          <th>降级</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="strategy in strategies" :key="strategy.id">
          <td>{{ strategy.name }}</td>
          <td>{{ strategy.maxConcurrentCalls }}</td>
          <td>{{ strategy.timeoutSeconds }}</td>
          <td>{{ strategy.retryCount }}</td>
          <td>{{ strategy.fallbackEnabled ? '是' : '否' }}</td>
          <td>{{ formatDate(strategy.createdAt) }}</td>
          <td>
            <div class="action-buttons">
              <CustomButton type="ghost" @click="editStrategyHandler(strategy)">编辑</CustomButton>
              <CustomButton type="ghost" @click="deleteStrategyHandler(strategy.id)">删除</CustomButton>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { showConfirm } from '@/utils/confirm'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'
import CustomSelect from '@/components/CustomSelect.vue'

const booleanOptions = [
  { value: true, label: '是' },
  { value: false, label: '否' },
]
import { useWorkspaceStore } from '@/store/workspace-store'
import {
  listToolStrategies,
  createToolStrategy,
  updateToolStrategy,
  deleteToolStrategy,
  type ToolStrategy,
} from '@/api/strategy-api'

const store = useWorkspaceStore()
const strategies = ref<ToolStrategy[]>([])
const loading = ref(false)
const showCreateForm = ref(false)
const showEditForm = ref(false)
const editingId = ref<string | null>(null)

const newStrategy = reactive({
  name: '',
  description: '',
  maxConcurrentCalls: 5,
  timeoutSeconds: 30,
  retryCount: 3,
  fallbackEnabled: false,
})

const editStrategyData = reactive({
  name: '',
  description: '',
  maxConcurrentCalls: 5,
  timeoutSeconds: 30,
  retryCount: 3,
  fallbackEnabled: false,
})

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

async function loadStrategies() {
  if (!store.tenantId || !store.workspaceId) return
  loading.value = true
  try {
    strategies.value = await listToolStrategies({ tenantId: store.tenantId, workspaceId: store.workspaceId })
  } finally {
    loading.value = false
  }
}

async function createStrategy() {
  if (!store.tenantId || !store.workspaceId) return
  await createToolStrategy(getSelection(), buildNewStrategyPayload())
  showCreateForm.value = false
  resetNewStrategy()
  await loadStrategies()
}

function getSelection() {
  return { tenantId: store.tenantId!, workspaceId: store.workspaceId! }
}

function buildNewStrategyPayload() {
  return { ...buildNewStrategyIdentity(), ...buildNewStrategyOptions() }
}

function buildNewStrategyIdentity() {
  return { name: newStrategy.name, description: newStrategy.description }
}

function buildNewStrategyOptions() {
  return { maxConcurrentCalls: newStrategy.maxConcurrentCalls, timeoutSeconds: newStrategy.timeoutSeconds, retryCount: newStrategy.retryCount, fallbackEnabled: newStrategy.fallbackEnabled }
}

function resetNewStrategy() {
  newStrategy.name = ''
  newStrategy.description = ''
  newStrategy.maxConcurrentCalls = 5
  newStrategy.timeoutSeconds = 30
  newStrategy.retryCount = 3
  newStrategy.fallbackEnabled = false
}

function editStrategyHandler(strategy: ToolStrategy) {
  editingId.value = strategy.id
  editStrategyData.name = strategy.name
  editStrategyData.description = strategy.description || ''
  editStrategyData.maxConcurrentCalls = strategy.maxConcurrentCalls
  editStrategyData.timeoutSeconds = strategy.timeoutSeconds
  editStrategyData.retryCount = strategy.retryCount
  editStrategyData.fallbackEnabled = strategy.fallbackEnabled
  showEditForm.value = true
}

function closeEditForm() {
  showEditForm.value = false
  editingId.value = null
}

async function updateStrategy() {
  if (!canUpdateStrategy()) return
  await updateToolStrategy(getSelection(), editingId.value!, buildEditStrategyPayload())
  closeEditForm()
  await loadStrategies()
}

function canUpdateStrategy(): boolean {
  return Boolean(store.tenantId && store.workspaceId && editingId.value)
}

function buildEditStrategyPayload() {
  return { ...buildEditStrategyIdentity(), ...buildEditStrategyOptions() }
}

function buildEditStrategyIdentity() {
  return { name: editStrategyData.name, description: editStrategyData.description }
}

function buildEditStrategyOptions() {
  return { maxConcurrentCalls: editStrategyData.maxConcurrentCalls, timeoutSeconds: editStrategyData.timeoutSeconds, retryCount: editStrategyData.retryCount, fallbackEnabled: editStrategyData.fallbackEnabled }
}

async function deleteStrategyHandler(id: string) {
  if (!store.tenantId || !store.workspaceId) return
  if (!await showConfirm('确定要删除这个策略吗？')) return
  await deleteToolStrategy({ tenantId: store.tenantId, workspaceId: store.workspaceId }, id)
  await loadStrategies()
}

onMounted(loadStrategies)
  window.addEventListener('strategy-tool-add', () => {
    showCreateForm.value = true
  })
</script>

<style scoped>
.strategy-panel { padding: 1rem; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.panel-header h3 { margin: 0; }
.form { background: var(--bg-stripe); padding: 1.5rem; border-radius: 8px; margin-bottom: 1.5rem; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.5rem; font-weight: 500; color: var(--color-heading); }
.form-group input, .form-group textarea { width: 100%; padding: 0.5rem; border: 1px solid var(--color-border-strong); border-radius: 4px; }
.form-actions { display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 1rem; }
.loading, .empty-state { text-align: center; padding: 2rem; color: var(--color-text-muted); }
.strategy-table { width: 100%; border-collapse: collapse; }
.strategy-table th, .strategy-table td { border: 1px solid var(--color-border); padding: 0.75rem; text-align: left; }
.strategy-table th { background: var(--bg-elevated); font-weight: 600; color: var(--color-heading); }
.strategy-table tr:hover { background: var(--bg-hover); }
.action-buttons { display: flex; gap: 0.5rem; }
.primary { background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse); border: none; padding: 0.5rem 1rem; border-radius: 4px; cursor: pointer; font-weight: 500; }
</style>
