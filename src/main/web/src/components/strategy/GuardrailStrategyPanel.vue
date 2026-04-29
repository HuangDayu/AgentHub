<template>
  <div class="strategy-panel">
    <div class="panel-header">
      <h3>护栏策略配置</h3>
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
          <label>输入验证</label>
          <select v-model="newStrategy.inputValidationEnabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </div>
        <div class="form-group">
          <label>输出验证</label>
          <select v-model="newStrategy.outputValidationEnabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>PII检测</label>
          <select v-model="newStrategy.piiDetectionEnabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </div>
        <div class="form-group">
          <label>PII脱敏</label>
          <select v-model="newStrategy.piiMaskingEnabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>提示注入检测</label>
          <select v-model="newStrategy.promptInjectionDetection">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>最大输入长度</label>
          <input type="number" v-model.number="newStrategy.maxInputLength" placeholder="10000" />
        </div>
        <div class="form-group">
          <label>最大输出长度</label>
          <input type="number" v-model.number="newStrategy.maxOutputLength" placeholder="4096" />
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
          <label>输入验证</label>
          <select v-model="editStrategyData.inputValidationEnabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </div>
        <div class="form-group">
          <label>输出验证</label>
          <select v-model="editStrategyData.outputValidationEnabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>PII检测</label>
          <select v-model="editStrategyData.piiDetectionEnabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </div>
        <div class="form-group">
          <label>PII脱敏</label>
          <select v-model="editStrategyData.piiMaskingEnabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>提示注入检测</label>
          <select v-model="editStrategyData.promptInjectionDetection">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>最大输入长度</label>
          <input type="number" v-model.number="editStrategyData.maxInputLength" />
        </div>
        <div class="form-group">
          <label>最大输出长度</label>
          <input type="number" v-model.number="editStrategyData.maxOutputLength" />
        </div>
      </div>
      <div class="form-actions">
        <button type="button" class="ghost" @click="closeEditForm">取消</button>
        <button type="submit" class="primary">保存</button>
      </div>
    </form>

    <!-- 策略列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="strategies.length === 0" class="empty-state">暂无护栏策略</div>
    <table v-else class="strategy-table">
      <thead>
        <tr>
          <th>名称</th>
          <th>输入验证</th>
          <th>输出验证</th>
          <th>PII检测</th>
          <th>注入检测</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="strategy in strategies" :key="strategy.id">
          <td>{{ strategy.name }}</td>
          <td>{{ strategy.inputValidationEnabled ? '是' : '否' }}</td>
          <td>{{ strategy.outputValidationEnabled ? '是' : '否' }}</td>
          <td>{{ strategy.piiDetectionEnabled ? '是' : '否' }}</td>
          <td>{{ strategy.promptInjectionDetection ? '是' : '否' }}</td>
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
  listGuardrailStrategies,
  createGuardrailStrategy,
  updateGuardrailStrategy,
  deleteGuardrailStrategy,
  type GuardrailStrategy,
} from '@/api/strategy-api'

const store = useWorkspaceStore()
const strategies = ref<GuardrailStrategy[]>([])
const loading = ref(false)
const showCreateForm = ref(false)
const showEditForm = ref(false)
const editingId = ref<string | null>(null)

const newStrategy = reactive({
  name: '',
  description: '',
  inputValidationEnabled: true,
  outputValidationEnabled: true,
  piiDetectionEnabled: false,
  piiMaskingEnabled: false,
  promptInjectionDetection: true,
  maxInputLength: 10000,
  maxOutputLength: 4096,
})

const editStrategyData = reactive({
  name: '',
  description: '',
  inputValidationEnabled: true,
  outputValidationEnabled: true,
  piiDetectionEnabled: false,
  piiMaskingEnabled: false,
  promptInjectionDetection: true,
  maxInputLength: 10000,
  maxOutputLength: 4096,
})

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

async function loadStrategies() {
  if (!store.tenantId || !store.workspaceId) return
  loading.value = true
  try {
    strategies.value = await listGuardrailStrategies({ tenantId: store.tenantId, workspaceId: store.workspaceId })
  } finally {
    loading.value = false
  }
}

async function createStrategy() {
  if (!store.tenantId || !store.workspaceId) return
  await createGuardrailStrategy(
    { tenantId: store.tenantId, workspaceId: store.workspaceId },
    {
      name: newStrategy.name,
      description: newStrategy.description,
      inputValidationEnabled: newStrategy.inputValidationEnabled,
      outputValidationEnabled: newStrategy.outputValidationEnabled,
      piiDetectionEnabled: newStrategy.piiDetectionEnabled,
      piiMaskingEnabled: newStrategy.piiMaskingEnabled,
      promptInjectionDetection: newStrategy.promptInjectionDetection,
      maxInputLength: newStrategy.maxInputLength,
      maxOutputLength: newStrategy.maxOutputLength,
    }
  )
  showCreateForm.value = false
  resetNewStrategy()
  await loadStrategies()
}

function resetNewStrategy() {
  newStrategy.name = ''
  newStrategy.description = ''
  newStrategy.inputValidationEnabled = true
  newStrategy.outputValidationEnabled = true
  newStrategy.piiDetectionEnabled = false
  newStrategy.piiMaskingEnabled = false
  newStrategy.promptInjectionDetection = true
  newStrategy.maxInputLength = 10000
  newStrategy.maxOutputLength = 4096
}

function editStrategyHandler(strategy: GuardrailStrategy) {
  editingId.value = strategy.id
  editStrategyData.name = strategy.name
  editStrategyData.description = strategy.description || ''
  editStrategyData.inputValidationEnabled = strategy.inputValidationEnabled
  editStrategyData.outputValidationEnabled = strategy.outputValidationEnabled
  editStrategyData.piiDetectionEnabled = strategy.piiDetectionEnabled
  editStrategyData.piiMaskingEnabled = strategy.piiMaskingEnabled
  editStrategyData.promptInjectionDetection = strategy.promptInjectionDetection
  editStrategyData.maxInputLength = strategy.maxInputLength
  editStrategyData.maxOutputLength = strategy.maxOutputLength
  showEditForm.value = true
}

function closeEditForm() {
  showEditForm.value = false
  editingId.value = null
}

async function updateStrategy() {
  if (!store.tenantId || !store.workspaceId || !editingId.value) return
  await updateGuardrailStrategy(
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
  await deleteGuardrailStrategy({ tenantId: store.tenantId, workspaceId: store.workspaceId }, id)
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
