<template>
  <div class="strategy-panel">
    <div class="panel-header">
      <h3>工具策略配置</h3>
      <button class="primary" @click="showCreateForm = true">创建策略</button>
    </div>

    <!-- 创建表单 -->
    <form v-if="showCreateForm" @submit.prevent="createStrategy" class="form">
      <div class="form-group">
        <label>策略名称 *</label>
        <input v-model="newStrategy.name" required placeholder="输入策略名称" />
      </div>
      <div class="form-group">
        <label>描述</label>
        <textarea v-model="newStrategy.description" placeholder="输入策略描述"></textarea>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>最大并发调用数</label>
          <input type="number" v-model.number="newStrategy.maxConcurrentCalls" placeholder="5" />
        </div>
        <div class="form-group">
          <label>超时时间(秒)</label>
          <input type="number" v-model.number="newStrategy.timeoutSeconds" placeholder="30" />
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>重试次数</label>
          <input type="number" v-model.number="newStrategy.retryCount" placeholder="3" />
        </div>
        <div class="form-group">
          <label>启用降级</label>
          <select v-model="newStrategy.fallbackEnabled">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
        </div>
      </div>
      <div class="form-actions">
        <button type="button" class="ghost" @click="showCreateForm = false">取消</button>
        <button type="submit" class="primary">创建</button>
      </div>
    </form>

    <!-- 编辑表单 -->
    <form v-if="showEditForm" @submit.prevent="updateStrategy" class="form">
      <div class="form-group">
        <label>策略名称 *</label>
        <input v-model="editStrategyData.name" required placeholder="输入策略名称" />
      </div>
      <div class="form-group">
        <label>描述</label>
        <textarea v-model="editStrategyData.description" placeholder="输入策略描述"></textarea>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>最大并发调用数</label>
          <input type="number" v-model.number="editStrategyData.maxConcurrentCalls" />
        </div>
        <div class="form-group">
          <label>超时时间(秒)</label>
          <input type="number" v-model.number="editStrategyData.timeoutSeconds" />
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>重试次数</label>
          <input type="number" v-model.number="editStrategyData.retryCount" />
        </div>
        <div class="form-group">
          <label>启用降级</label>
          <select v-model="editStrategyData.fallbackEnabled">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
        </div>
      </div>
      <div class="form-actions">
        <button type="button" class="ghost" @click="closeEditForm">取消</button>
        <button type="submit" class="primary">保存</button>
      </div>
    </form>

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
              <button class="ghost" @click="editStrategyHandler(strategy)">编辑</button>
              <button class="danger" @click="deleteStrategyHandler(strategy.id)">删除</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
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
  await createToolStrategy(
    { tenantId: store.tenantId, workspaceId: store.workspaceId },
    {
      name: newStrategy.name,
      description: newStrategy.description,
      maxConcurrentCalls: newStrategy.maxConcurrentCalls,
      timeoutSeconds: newStrategy.timeoutSeconds,
      retryCount: newStrategy.retryCount,
      fallbackEnabled: newStrategy.fallbackEnabled,
    }
  )
  showCreateForm.value = false
  resetNewStrategy()
  await loadStrategies()
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
  if (!store.tenantId || !store.workspaceId || !editingId.value) return
  await updateToolStrategy(
    { tenantId: store.tenantId, workspaceId: store.workspaceId },
    editingId.value,
    {
      name: editStrategyData.name,
      description: editStrategyData.description,
      maxConcurrentCalls: editStrategyData.maxConcurrentCalls,
      timeoutSeconds: editStrategyData.timeoutSeconds,
      retryCount: editStrategyData.retryCount,
      fallbackEnabled: editStrategyData.fallbackEnabled,
    }
  )
  closeEditForm()
  await loadStrategies()
}

async function deleteStrategyHandler(id: string) {
  if (!store.tenantId || !store.workspaceId) return
  if (!confirm('确定要删除这个策略吗？')) return
  await deleteToolStrategy({ tenantId: store.tenantId, workspaceId: store.workspaceId }, id)
  await loadStrategies()
}

onMounted(loadStrategies)
</script>

<style scoped>
.strategy-panel { padding: 1rem; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.panel-header h3 { margin: 0; }
.form { background: rgba(248, 250, 255, 0.5); padding: 1.5rem; border-radius: 8px; margin-bottom: 1.5rem; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.5rem; font-weight: 500; color: #264266; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 0.5rem; border: 1px solid rgba(38, 66, 102, 0.2); border-radius: 4px; }
.form-actions { display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 1rem; }
.loading, .empty-state { text-align: center; padding: 2rem; color: #5f6878; }
.strategy-table { width: 100%; border-collapse: collapse; }
.strategy-table th, .strategy-table td { border: 1px solid rgba(22, 33, 50, 0.08); padding: 0.75rem; text-align: left; }
.strategy-table th { background: rgba(248, 250, 255, 0.95); font-weight: 600; color: #264266; }
.strategy-table tr:hover { background: rgba(248, 250, 255, 0.5); }
.action-buttons { display: flex; gap: 0.5rem; }
.primary { background: linear-gradient(135deg, #264266, #3a8ad6); color: white; border: none; padding: 0.5rem 1rem; border-radius: 4px; cursor: pointer; font-weight: 500; }
.ghost { background: transparent; color: #264266; border: 1px solid rgba(38, 66, 102, 0.2); padding: 0.5rem 1rem; border-radius: 4px; cursor: pointer; }
.danger { background: transparent; color: #dc3545; border: 1px solid #dc3545; padding: 0.5rem 1rem; border-radius: 4px; cursor: pointer; }
</style>
