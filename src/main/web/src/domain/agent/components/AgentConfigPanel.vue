<template>
  <div class="config-panel">
    <h3 class="panel-title">Agent 配置关联</h3>
    
    <!-- 已有配置列表 -->
    <div class="config-section">
      <div class="section-header">
        <h4>已关联配置</h4>
        <span class="badge">{{ existingConfigs.length }}</span>
      </div>
      <div v-if="existingConfigs.length === 0" class="empty-hint">
        <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
        </svg>
        <span>暂无关联配置，请在下方添加</span>
      </div>
      <div v-else class="config-list">
        <div v-for="config in existingConfigs" :key="config.id" class="config-item">
          <div class="config-info">
            <span class="config-category">{{ getCategoryDisplayName(config.category) }}</span>
            <span class="config-arrow">→</span>
            <span class="config-type">{{ getTypeDisplayName(config.category, config.type) }}</span>
            <span class="config-arrow">→</span>
            <span class="config-id">{{ config.configId }}</span>
          </div>
          <div class="config-actions">
            <span class="config-status" :class="config.enabled ? 'enabled' : 'disabled'">
              {{ config.enabled ? '已启用' : '已禁用' }}
            </span>
            <button @click="handleDeleteConfig(config.id)" :disabled="loading" class="btn-icon btn-danger" title="删除">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加新配置 -->
    <div class="config-section add-section">
      <div class="section-header">
        <h4>添加配置关联</h4>
      </div>
      <div class="form-grid">
        <div class="form-group">
          <label>分类</label>
          <select v-model="selectedCategory" @change="onCategoryChange" class="form-select">
            <option value="">请选择分类</option>
            <option v-for="ct in configTypes" :key="ct.category" :value="ct.category">
              {{ ct.displayName }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>类型</label>
          <select v-model="selectedType" @change="onTypeChange" :disabled="!selectedCategory" class="form-select">
            <option value="">请选择类型</option>
            <option v-for="t in typesForCategory" :key="t.type" :value="t.type">
              {{ t.displayName }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>配置项</label>
          <select v-model="selectedConfigId" :disabled="!selectedType" class="form-select">
            <option value="">请选择配置</option>
            <option v-for="c in availableConfigsForType" :key="c.id" :value="c.id">
              {{ c.name }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>优先级</label>
          <input v-model.number="priority" type="number" min="1" class="form-input" />
        </div>

        <div class="form-group form-group-full">
          <label>描述（可选）</label>
          <input v-model="description" type="text" placeholder="输入配置描述..." class="form-input" />
        </div>

        <div class="form-group form-group-full form-checkbox">
          <label class="checkbox-label">
            <input v-model="enabled" type="checkbox" class="form-checkbox-input" />
            <span class="checkbox-text">启用此配置</span>
          </label>
        </div>
      </div>

      <button
        @click="handleAddConfig"
        :disabled="loading || !selectedCategory || !selectedType || !selectedConfigId"
        class="btn-primary"
      >
        <svg v-if="loading" class="spinner" viewBox="0 0 24 24">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" fill="none" stroke-dasharray="31.4" stroke-dashoffset="10"/>
        </svg>
        <svg v-else class="btn-icon-left" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
        </svg>
        <span>{{ loading ? '处理中...' : '添加配置' }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getConfigTypes, getAvailableConfigs, type ConfigTypeDefinition, type AvailableConfig } from '@/api/agent-config-type-api'
import { setAgentConfig, listAgentConfigs, deleteAgentConfig, type AgentConfigResponse } from '@/api/agent-config-api'

interface Props {
  selection: { tenantId: string; workspaceId: string }
  agentId: string
  workspaceId: string
}

const props = defineProps<Props>()

const configTypes = ref<ConfigTypeDefinition[]>([])
const existingConfigs = ref<AgentConfigResponse[]>([])
const availableConfigs = ref<Map<string, AvailableConfig[]>>(new Map())
const selectedCategory = ref('')
const selectedType = ref('')
const selectedConfigId = ref('')
const description = ref('')
const priority = ref(1)
const enabled = ref(true)
const loading = ref(false)

const typesForCategory = computed(() => {
  const ct = configTypes.value.find(c => c.category === selectedCategory.value)
  return ct?.types || []
})

const availableConfigsForType = computed(() => {
  return availableConfigs.value.get(selectedType.value) || []
})

function getCategoryDisplayName(category: string): string {
  const ct = configTypes.value.find(c => c.category === category)
  return ct?.displayName || category
}

function getTypeDisplayName(category: string, type: string): string {
  const ct = configTypes.value.find(c => c.category === category)
  const t = ct?.types.find(t => t.type === type)
  return t?.displayName || type
}

onMounted(() => {
  loadConfigTypes()
  loadExistingConfigs()
})

watch(selectedType, (newType) => {
  if (newType && selectedCategory.value) loadAvailableConfigs(selectedCategory.value, newType)
})

async function loadConfigTypes() {
  const types = await getConfigTypes(props.selection)
  configTypes.value = types
}

async function loadExistingConfigs() {
  const configs = await listAgentConfigs(props.selection, props.agentId)
  existingConfigs.value = configs
}

async function loadAvailableConfigs(category: string, type: string) {
  const configs = await getAvailableConfigs(props.selection, category, type, props.workspaceId)
  availableConfigs.value.set(type, configs)
}

async function handleAddConfig() {
  if (!selectedCategory.value || !selectedType.value || !selectedConfigId.value) return
  loading.value = true
  try {
    await setAgentConfig(props.selection, props.agentId, {
      category: selectedCategory.value,
      type: selectedType.value,
      configId: selectedConfigId.value,
      description: description.value,
      priority: priority.value,
      enabled: enabled.value,
    })
    await loadExistingConfigs()
    resetForm()
  } finally {
    loading.value = false
  }
}

async function handleDeleteConfig(configId: string) {
  loading.value = true
  try {
    await deleteAgentConfig(props.selection, props.agentId, configId)
    await loadExistingConfigs()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  selectedCategory.value = ''
  selectedType.value = ''
  selectedConfigId.value = ''
  description.value = ''
  priority.value = 1
  enabled.value = true
}

function onCategoryChange() {
  selectedType.value = ''
  selectedConfigId.value = ''
}

function onTypeChange() {
  selectedConfigId.value = ''
}
</script>

<style scoped>
.config-panel {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.panel-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 24px 0;
  padding-bottom: 16px;
  border-bottom: 2px solid #e2e8f0;
}

.config-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.add-section {
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
  border: 2px dashed #cbd5e1;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.section-header h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #334155;
  margin: 0;
}

.badge {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
}

.empty-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  color: #64748b;
  font-size: 0.875rem;
}

.empty-hint .icon {
  width: 20px;
  height: 20px;
  color: #94a3b8;
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.config-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.config-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.config-category {
  font-weight: 600;
  color: #3b82f6;
  background: #eff6ff;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 0.875rem;
}

.config-type {
  font-weight: 500;
  color: #1e293b;
  font-size: 0.875rem;
}

.config-arrow {
  color: #94a3b8;
  font-size: 0.75rem;
}

.config-id {
  font-family: 'Monaco', 'Menlo', monospace;
  color: #64748b;
  font-size: 0.8rem;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
}

.config-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.config-status {
  font-size: 0.75rem;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 12px;
}

.config-status.enabled {
  background: #dcfce7;
  color: #166534;
}

.config-status.disabled {
  background: #fef2f2;
  color: #991b1b;
}

.btn-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-icon svg {
  width: 16px;
  height: 16px;
}

.btn-danger {
  background: #fef2f2;
  color: #dc2626;
}

.btn-danger:hover:not(:disabled) {
  background: #fee2e2;
  color: #b91c1c;
}

.btn-icon:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group-full {
  grid-column: 1 / -1;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #475569;
}

.form-select,
.form-input {
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  color: #1e293b;
  background: white;
  transition: all 0.2s ease;
}

.form-select:focus,
.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-select:disabled {
  background: #f8fafc;
  color: #94a3b8;
  cursor: not-allowed;
}

.form-checkbox {
  flex-direction: row;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.form-checkbox-input {
  width: 18px;
  height: 18px;
  accent-color: #3b82f6;
}

.checkbox-text {
  font-size: 0.875rem;
  color: #475569;
}

.btn-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3);
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  box-shadow: 0 4px 8px rgba(59, 130, 246, 0.4);
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-icon-left {
  width: 18px;
  height: 18px;
}

.spinner {
  width: 18px;
  height: 18px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
