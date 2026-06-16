<template>
  <div class="strategy-panel">
    <div class="panel-header">
      <h3>检索策略配置</h3>
      
    </div>

    <!-- 创建弹窗 -->
    <ModalDialog
      v-model:visible="showCreateForm"
      title="创建检索策略"
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
          <span>检索类型</span>
          <CustomSelect v-model="newStrategy.retrievalType" :options="retrievalTypeOptions" />
        </label>
        <label class="field">
          <span>描述</span>
          <textarea v-model="newStrategy.description" placeholder="输入策略描述"></textarea>
        </label>
        <label class="field">
          <span>Top K</span>
          <input type="number" v-model.number="newStrategy.topK" placeholder="10" />
        </label>
        <label class="field">
          <span>分数阈值</span>
          <input type="number" step="0.01" v-model.number="newStrategy.scoreThreshold" placeholder="0.75" />
        </label>
        <label class="field">
          <span>向量权重</span>
          <input type="number" step="0.1" v-model.number="newStrategy.vectorWeight" placeholder="0.7" />
        </label>
        <label class="field">
          <span>关键词权重</span>
          <input type="number" step="0.1" v-model.number="newStrategy.keywordWeight" placeholder="0.3" />
        </label>
        <label class="field">
          <span>启用查询改写</span>
          <CustomSelect v-model="newStrategy.enableQueryRewrite" :options="booleanOptions" />
        </label>
        <label class="field">
          <span>启用重排序</span>
          <CustomSelect v-model="newStrategy.enableRerank" :options="booleanOptions" />
        </label>
        <label class="field">
          <span>启用文本搜索</span>
          <CustomSelect v-model="newStrategy.enableTextSearch" :options="booleanOptions" />
        </label>
        <label class="field">
          <span>启用向量搜索</span>
          <CustomSelect v-model="newStrategy.enableVectorSearch" :options="booleanOptions" />
        </label>
        <label class="field">
          <span>重排序模型</span>
          <input v-model="newStrategy.rerankModel" placeholder="重排序模型" />
        </label>
      </form>
    </ModalDialog>

    <!-- 编辑弹窗 -->
    <ModalDialog
      v-model:visible="showEditForm"
      title="编辑检索策略"
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
          <span>检索类型</span>
          <CustomSelect v-model="editStrategyData.retrievalType" :options="retrievalTypeOptions" />
        </label>
        <label class="field">
          <span>描述</span>
          <textarea v-model="editStrategyData.description" placeholder="输入策略描述"></textarea>
        </label>
        <label class="field">
          <span>Top K</span>
          <input type="number" v-model.number="editStrategyData.topK" placeholder="10" />
        </label>
        <label class="field">
          <span>分数阈值</span>
          <input type="number" step="0.01" v-model.number="editStrategyData.scoreThreshold" placeholder="0.75" />
        </label>
        <label class="field">
          <span>向量权重</span>
          <input type="number" step="0.1" v-model.number="editStrategyData.vectorWeight" placeholder="0.7" />
        </label>
        <label class="field">
          <span>关键词权重</span>
          <input type="number" step="0.1" v-model.number="editStrategyData.keywordWeight" placeholder="0.3" />
        </label>
        <label class="field">
          <span>启用查询改写</span>
          <CustomSelect v-model="editStrategyData.enableQueryRewrite" :options="booleanOptions" />
        </label>
        <label class="field">
          <span>启用重排序</span>
          <CustomSelect v-model="editStrategyData.enableRerank" :options="booleanOptions" />
        </label>
        <label class="field">
          <span>启用文本搜索</span>
          <CustomSelect v-model="editStrategyData.enableTextSearch" :options="booleanOptions" />
        </label>
        <label class="field">
          <span>启用向量搜索</span>
          <CustomSelect v-model="editStrategyData.enableVectorSearch" :options="booleanOptions" />
        </label>
        <label class="field">
          <span>重排序模型</span>
          <input v-model="editStrategyData.rerankModel" placeholder="重排序模型" />
        </label>
      </form>
    </ModalDialog>

    <!-- 策略列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="strategies.length === 0" class="empty-state">暂无检索策略</div>
    <table v-else class="strategy-table">
      <thead>
        <tr>
          <th>名称</th>
          <th>检索类型</th>
          <th>Top K</th>
          <th>分数阈值</th>
          <th>查询改写</th>
          <th>重排序</th>
          <th>文本搜索</th>
          <th>向量搜索</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="strategy in strategies" :key="strategy.id">
          <td>{{ strategy.name }}</td>
          <td>{{ strategy.retrievalType || 'HYBRID' }}</td>
          <td>{{ strategy.topK }}</td>
          <td>{{ strategy.scoreThreshold }}</td>
          <td>{{ strategy.enableQueryRewrite ? '是' : '否' }}</td>
          <td>{{ strategy.enableRerank ? '是' : '否' }}</td>
          <td>{{ strategy.enableTextSearch ? '是' : '否' }}</td>
          <td>{{ strategy.enableVectorSearch ? '是' : '否' }}</td>
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

const retrievalTypeOptions = [
  { value: 'HYBRID', label: '混合检索' },
  { value: 'VECTOR', label: '向量检索' },
  { value: 'KEYWORD', label: '关键词检索' },
]
const booleanOptions = [
  { value: true, label: '是' },
  { value: false, label: '否' },
]
import { useWorkspaceStore } from '@/store/workspace-store'
import {
  listRetrievalStrategies,
  createRetrievalStrategy,
  updateRetrievalStrategy,
  deleteRetrievalStrategy,
  type RetrievalStrategy,
} from '@/api/strategy-api'

const store = useWorkspaceStore()
const strategies = ref<RetrievalStrategy[]>([])
const loading = ref(false)
const showCreateForm = ref(false)
const showEditForm = ref(false)
const editingId = ref<string | null>(null)

const newStrategy = reactive({
  name: '',
  description: '',
  retrievalType: 'HYBRID',
  topK: 10,
  scoreThreshold: 0.75,
  enableQueryRewrite: false,
  enableRerank: false,
  enableTextSearch: false,
  enableVectorSearch: true,
  rerankModel: '',
  vectorWeight: 0.7,
  keywordWeight: 0.3,
})

const editStrategyData = reactive({
  name: '',
  description: '',
  retrievalType: 'HYBRID',
  topK: 10,
  scoreThreshold: 0.75,
  vectorWeight: 0.7,
  keywordWeight: 0.3,
  enableQueryRewrite: false,
  enableRerank: false,
  enableTextSearch: false,
  enableVectorSearch: false,
  rerankModel: '',
})

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

async function loadStrategies() {
  if (!store.tenantId || !store.workspaceId) return
  loading.value = true
  try {
    strategies.value = await listRetrievalStrategies({ tenantId: store.tenantId, workspaceId: store.workspaceId })
  } finally {
    loading.value = false
  }
}

async function createStrategy() {
  if (!store.tenantId || !store.workspaceId) return
  await createRetrievalStrategy(getSelection(), buildNewStrategyPayload())
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
  return {
    name: newStrategy.name,
    description: newStrategy.description,
    retrievalType: newStrategy.retrievalType,
  }
}

function buildNewStrategyOptions() {
  return { ...buildNewStrategySearchParams(), ...buildNewStrategyFlags() }
}

function buildNewStrategySearchParams() {
  return { topK: newStrategy.topK, scoreThreshold: newStrategy.scoreThreshold, vectorWeight: newStrategy.vectorWeight, keywordWeight: newStrategy.keywordWeight }
}

function buildNewStrategyFlags() {
  return { enableQueryRewrite: newStrategy.enableQueryRewrite, enableRerank: newStrategy.enableRerank, enableTextSearch: newStrategy.enableTextSearch, enableVectorSearch: newStrategy.enableVectorSearch, rerankModel: newStrategy.rerankModel || undefined }
}

const NEW_STRATEGY_DEFAULTS: Partial<typeof newStrategy> = { name: '', description: '', retrievalType: 'HYBRID', topK: 10, scoreThreshold: 0.75, enableQueryRewrite: false, enableRerank: false, enableTextSearch: false, enableVectorSearch: true, rerankModel: '', vectorWeight: 0.7, keywordWeight: 0.3 }

function resetNewStrategy() {
  Object.assign(newStrategy, NEW_STRATEGY_DEFAULTS)
}

function editStrategyHandler(strategy: RetrievalStrategy) {
  editingId.value = strategy.id
  Object.assign(editStrategyData, buildEditStrategyData(strategy))
  showEditForm.value = true
}

function buildEditStrategyData(strategy: RetrievalStrategy) {
  return {
    name: strategy.name, description: strategy.description || '',
    retrievalType: strategy.retrievalType || 'HYBRID', topK: strategy.topK || 10,
    scoreThreshold: strategy.scoreThreshold || 0.75, vectorWeight: strategy.vectorWeight || 0.7, keywordWeight: strategy.keywordWeight || 0.3,
    enableQueryRewrite: strategy.enableQueryRewrite || false, enableRerank: strategy.enableRerank || false,
    enableTextSearch: strategy.enableTextSearch || false, enableVectorSearch: strategy.enableVectorSearch || false,
    rerankModel: strategy.rerankModel || '',
  }
}

function closeEditForm() {
  showEditForm.value = false
  editingId.value = null
}

async function updateStrategy() {
  if (!canUpdateStrategy()) return
  await updateRetrievalStrategy(getSelection(), editingId.value!, { name: editStrategyData.name, description: editStrategyData.description })
  closeEditForm()
  await loadStrategies()
}

function canUpdateStrategy(): boolean {
  return Boolean(store.tenantId && store.workspaceId && editingId.value)
}

async function deleteStrategyHandler(id: string) {
  if (!store.tenantId || !store.workspaceId) return
  if (!await showConfirm('确定要删除这个策略吗？')) return
  await deleteRetrievalStrategy({ tenantId: store.tenantId, workspaceId: store.workspaceId }, id)
  await loadStrategies()
}

onMounted(loadStrategies)
  window.addEventListener('strategy-retrieval-add', () => {
    showCreateForm.value = true
  })
</script>

<style scoped>
.strategy-panel {
  padding: 1rem;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.panel-header h3 {
  margin: 0;
}
.form {
  background: var(--bg-stripe);
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}
.form-group {
  margin-bottom: 1rem;
}
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--color-heading);
}
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid var(--color-border-strong);
  border-radius: 4px;
}
.form-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  margin-top: 1rem;
}
.loading,
.empty-state {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-muted);
}
.strategy-table {
  width: 100%;
  border-collapse: collapse;
}
.strategy-table th,
.strategy-table td {
  border: 1px solid var(--color-border);
  padding: 0.75rem;
  text-align: left;
}
.strategy-table th {
  background: var(--bg-elevated);
  font-weight: 600;
  color: var(--color-heading);
}
.strategy-table tr:hover {
  background: var(--bg-hover);
}
.action-buttons {
  display: flex;
  gap: 0.5rem;
}
.primary {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--color-text-inverse);
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}
</style>
