<template>
  <div class="strategy-panel">
    <div class="panel-header">
      <h3>模型策略配置</h3>
      
    </div>

    <!-- 创建弹窗 -->
    <ModalDialog
      v-model:visible="showCreateForm"
      title="创建模型策略"
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
          <span>温度 (Temperature)</span>
          <input type="number" step="0.1" v-model.number="newStrategy.temperature" placeholder="0.7" />
        </label>
        <label class="field">
          <span>最大Token数</span>
          <input type="number" v-model.number="newStrategy.maxTokens" placeholder="2048" />
        </label>
        <label class="field">
          <span>Top P</span>
          <input type="number" step="0.1" v-model.number="newStrategy.topP" placeholder="1.0" />
        </label>
        <label class="field">
          <span>频率惩罚</span>
          <input type="number" step="0.1" v-model.number="newStrategy.frequencyPenalty" placeholder="0.0" />
        </label>
        <label class="field">
          <span>存在惩罚</span>
          <input type="number" step="0.1" v-model.number="newStrategy.presencePenalty" placeholder="0.0" />
        </label>
      </form>
    </ModalDialog>

    <!-- 编辑弹窗 -->
    <ModalDialog
      v-model:visible="showEditForm"
      title="编辑模型策略"
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
          <span>温度 (Temperature)</span>
          <input type="number" step="0.1" v-model.number="editStrategyData.temperature" />
        </label>
        <label class="field">
          <span>最大Token数</span>
          <input type="number" v-model.number="editStrategyData.maxTokens" />
        </label>
        <label class="field">
          <span>Top P</span>
          <input type="number" step="0.1" v-model.number="editStrategyData.topP" />
        </label>
        <label class="field">
          <span>频率惩罚</span>
          <input type="number" step="0.1" v-model.number="editStrategyData.frequencyPenalty" />
        </label>
        <label class="field">
          <span>存在惩罚</span>
          <input type="number" step="0.1" v-model.number="editStrategyData.presencePenalty" />
        </label>
      </form>
    </ModalDialog>

    <!-- 策略列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="strategies.length === 0" class="empty-state">暂无模型策略</div>
    <table v-else class="strategy-table">
      <thead>
        <tr>
          <th>名称</th>
          <th>温度</th>
          <th>最大Token</th>
          <th>Top P</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="strategy in strategies" :key="strategy.id">
          <td>{{ strategy.name }}</td>
          <td>{{ strategy.temperature }}</td>
          <td>{{ strategy.maxTokens }}</td>
          <td>{{ strategy.topP }}</td>
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
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'
import { useWorkspaceStore } from '@/store/workspace-store'
import {
  listModelStrategies,
  createModelStrategy,
  updateModelStrategy,
  deleteModelStrategy,
  type ModelStrategy,
} from '@/api/strategy-api'

const store = useWorkspaceStore()
const strategies = ref<ModelStrategy[]>([])
const loading = ref(false)
const showCreateForm = ref(false)
const showEditForm = ref(false)
const editingId = ref<string | null>(null)

const newStrategy = reactive({
  name: '',
  description: '',
  temperature: 0.7,
  maxTokens: 2048,
  topP: 1.0,
  frequencyPenalty: 0.0,
  presencePenalty: 0.0,
})

const editStrategyData = reactive({
  name: '',
  description: '',
  temperature: 0.7,
  maxTokens: 2048,
  topP: 1.0,
  frequencyPenalty: 0.0,
  presencePenalty: 0.0,
})

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

async function loadStrategies() {
  if (!store.tenantId || !store.workspaceId) return
  loading.value = true
  try {
    strategies.value = await listModelStrategies({ tenantId: store.tenantId, workspaceId: store.workspaceId })
  } finally {
    loading.value = false
  }
}

async function createStrategy() {
  if (!store.tenantId || !store.workspaceId) return
  await createModelStrategy(
    { tenantId: store.tenantId, workspaceId: store.workspaceId },
    {
      name: newStrategy.name,
      description: newStrategy.description,
      temperature: newStrategy.temperature,
      maxTokens: newStrategy.maxTokens,
      topP: newStrategy.topP,
      frequencyPenalty: newStrategy.frequencyPenalty,
      presencePenalty: newStrategy.presencePenalty,
    }
  )
  showCreateForm.value = false
  resetNewStrategy()
  await loadStrategies()
}

function resetNewStrategy() {
  newStrategy.name = ''
  newStrategy.description = ''
  newStrategy.temperature = 0.7
  newStrategy.maxTokens = 2048
  newStrategy.topP = 1.0
  newStrategy.frequencyPenalty = 0.0
  newStrategy.presencePenalty = 0.0
}

function editStrategyHandler(strategy: ModelStrategy) {
  editingId.value = strategy.id
  editStrategyData.name = strategy.name
  editStrategyData.description = strategy.description || ''
  editStrategyData.temperature = strategy.temperature
  editStrategyData.maxTokens = strategy.maxTokens
  editStrategyData.topP = strategy.topP
  editStrategyData.frequencyPenalty = strategy.frequencyPenalty
  editStrategyData.presencePenalty = strategy.presencePenalty
  showEditForm.value = true
}

function closeEditForm() {
  showEditForm.value = false
  editingId.value = null
}

async function updateStrategy() {
  if (!store.tenantId || !store.workspaceId || !editingId.value) return
  await updateModelStrategy(
    { tenantId: store.tenantId, workspaceId: store.workspaceId },
    editingId.value,
    {
      name: editStrategyData.name,
      description: editStrategyData.description,
      temperature: editStrategyData.temperature,
      maxTokens: editStrategyData.maxTokens,
      topP: editStrategyData.topP,
      frequencyPenalty: editStrategyData.frequencyPenalty,
      presencePenalty: editStrategyData.presencePenalty,
    }
  )
  closeEditForm()
  await loadStrategies()
}

async function deleteStrategyHandler(id: string) {
  if (!store.tenantId || !store.workspaceId) return
  if (!confirm('确定要删除这个策略吗？')) return
  await deleteModelStrategy({ tenantId: store.tenantId, workspaceId: store.workspaceId }, id)
  await loadStrategies()
}

onMounted(loadStrategies)
  window.addEventListener('strategy-model-add', () => {
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
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 0.5rem; border: 1px solid var(--color-border-strong); border-radius: 4px; }
.form-actions { display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 1rem; }
.loading, .empty-state { text-align: center; padding: 2rem; color: var(--color-text-muted); }
.strategy-table { width: 100%; border-collapse: collapse; }
.strategy-table th, .strategy-table td { border: 1px solid var(--color-border); padding: 0.75rem; text-align: left; }
.strategy-table th { background: var(--bg-elevated); font-weight: 600; color: var(--color-heading); }
.strategy-table tr:hover { background: var(--bg-hover); }
.action-buttons { display: flex; gap: 0.5rem; }
.primary { background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse); border: none; padding: 0.5rem 1rem; border-radius: 4px; cursor: pointer; font-weight: 500; }
</style>
