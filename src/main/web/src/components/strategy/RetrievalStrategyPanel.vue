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
          <select v-model="newStrategy.retrievalType">
            <option value="HYBRID">混合检索</option>
            <option value="VECTOR">向量检索</option>
            <option value="KEYWORD">关键词检索</option>
          </select>
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
          <select v-model="newStrategy.enableQueryRewrite">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
        </label>
        <label class="field">
          <span>启用重排序</span>
          <select v-model="newStrategy.enableRerank">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
        </label>
        <label class="field">
          <span>启用文本搜索</span>
          <select v-model="newStrategy.enableTextSearch">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
        </label>
        <label class="field">
          <span>启用向量搜索</span>
          <select v-model="newStrategy.enableVectorSearch">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
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
          <select v-model="editStrategyData.retrievalType">
            <option value="HYBRID">混合检索</option>
            <option value="VECTOR">向量检索</option>
            <option value="KEYWORD">关键词检索</option>
          </select>
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
          <select v-model="editStrategyData.enableQueryRewrite">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
        </label>
        <label class="field">
          <span>启用重排序</span>
          <select v-model="editStrategyData.enableRerank">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
        </label>
        <label class="field">
          <span>启用文本搜索</span>
          <select v-model="editStrategyData.enableTextSearch">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
        </label>
        <label class="field">
          <span>启用向量搜索</span>
          <select v-model="editStrategyData.enableVectorSearch">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
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
import ModalDialog from '@/components/ModalDialog.vue'
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
  await createRetrievalStrategy(
    { tenantId: store.tenantId, workspaceId: store.workspaceId },
    {
      name: newStrategy.name,
      description: newStrategy.description,
      retrievalType: newStrategy.retrievalType,
      topK: newStrategy.topK,
      scoreThreshold: newStrategy.scoreThreshold,
      enableQueryRewrite: newStrategy.enableQueryRewrite,
      enableRerank: newStrategy.enableRerank,
      enableTextSearch: newStrategy.enableTextSearch,
      enableVectorSearch: newStrategy.enableVectorSearch,
      rerankModel: newStrategy.rerankModel || undefined,
      vectorWeight: newStrategy.vectorWeight,
      keywordWeight: newStrategy.keywordWeight,
    }
  )
  showCreateForm.value = false
  resetNewStrategy()
  await loadStrategies()
}

function resetNewStrategy() {
  newStrategy.name = ''
  newStrategy.description = ''
  newStrategy.retrievalType = 'HYBRID'
  newStrategy.topK = 10
  newStrategy.scoreThreshold = 0.75
  newStrategy.enableQueryRewrite = false
  newStrategy.enableRerank = false
  newStrategy.enableTextSearch = false
  newStrategy.enableVectorSearch = true
  newStrategy.rerankModel = ''
  newStrategy.vectorWeight = 0.7
  newStrategy.keywordWeight = 0.3
}

function editStrategyHandler(strategy: RetrievalStrategy) {
  editingId.value = strategy.id
  editStrategyData.name = strategy.name
  editStrategyData.description = strategy.description || ''
  editStrategyData.retrievalType = strategy.retrievalType || 'HYBRID'
  editStrategyData.topK = strategy.topK || 10
  editStrategyData.scoreThreshold = strategy.scoreThreshold || 0.75
  editStrategyData.vectorWeight = strategy.vectorWeight || 0.7
  editStrategyData.keywordWeight = strategy.keywordWeight || 0.3
  editStrategyData.enableQueryRewrite = strategy.enableQueryRewrite || false
  editStrategyData.enableRerank = strategy.enableRerank || false
  editStrategyData.enableTextSearch = strategy.enableTextSearch || false
  editStrategyData.enableVectorSearch = strategy.enableVectorSearch || false
  editStrategyData.rerankModel = strategy.rerankModel || ''
  showEditForm.value = true
}

function closeEditForm() {
  showEditForm.value = false
  editingId.value = null
}

async function updateStrategy() {
  if (!store.tenantId || !store.workspaceId || !editingId.value) return
  await updateRetrievalStrategy(
    { tenantId: store.tenantId, workspaceId: store.workspaceId },
    editingId.value,
    {
      name: editStrategyData.name,
      description: editStrategyData.description,
    }
  )
  closeEditForm()
  await loadStrategies()
}

async function deleteStrategyHandler(id: string) {
  if (!store.tenantId || !store.workspaceId) return
  if (!confirm('确定要删除这个策略吗？')) return
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
  background: rgba(248, 250, 255, 0.5);
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
  color: #264266;
}
.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid rgba(38, 66, 102, 0.2);
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
  color: #5f6878;
}
.strategy-table {
  width: 100%;
  border-collapse: collapse;
}
.strategy-table th,
.strategy-table td {
  border: 1px solid rgba(22, 33, 50, 0.08);
  padding: 0.75rem;
  text-align: left;
}
.strategy-table th {
  background: rgba(248, 250, 255, 0.95);
  font-weight: 600;
  color: #264266;
}
.strategy-table tr:hover {
  background: rgba(248, 250, 255, 0.5);
}
.action-buttons {
  display: flex;
  gap: 0.5rem;
}
.primary {
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}
.ghost {
  background: transparent;
  color: #264266;
  border: 1px solid rgba(38, 66, 102, 0.2);
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}
.danger {
  background: transparent;
  color: #dc3545;
  border: 1px solid #dc3545;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}
</style>
