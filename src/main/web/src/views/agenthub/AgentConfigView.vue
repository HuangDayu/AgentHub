<template>
  <section class="agent-config-page">
    <div class="page-header">
      <div>
        <h2>Agent配置管理</h2>
        <p class="muted">管理Agent的配置关联关系</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    
    <template v-else>
      <!-- Agent选择 -->
      <article class="panel">
        <div class="panel-header">
          <h3>选择Agent</h3>
        </div>
        <div class="agent-grid">
          <div 
            v-for="agent in agents" 
            :key="agent.id" 
            :class="['agent-card', { active: selectedAgentId === agent.id }]"
            @click="selectAgent(agent.id)"
          >
            <div class="agent-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2a3 3 0 00-3 3v4a3 3 0 006 0V5a3 3 0 00-3-3z"/>
                <path d="M19 10v2a7 7 0 01-14 0v-2"/>
                <line x1="12" y1="19" x2="12" y2="22"/>
              </svg>
            </div>
            <div class="agent-info">
              <div class="agent-name">{{ agent.name }}</div>
              <div class="agent-id muted">{{ agent.id }}</div>
            </div>
            <div class="agent-status">
              <span :class="['status-badge', agent.published ? 'published' : 'unpublished']">
                {{ agent.published ? '已发布' : '未发布' }}
              </span>
            </div>
          </div>
        </div>
      </article>

      <!-- 配置列表 -->
      <article v-if="selectedAgentId" class="panel">
        <div class="panel-header">
          <h3>{{ selectedAgentName }} - 配置列表</h3>
          <button class="primary" @click="showAddForm = true">添加配置</button>
        </div>
        
        <!-- 配置方块网格 -->
        <div v-if="configs.length === 0" class="empty-hint">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
          </svg>
          <span>暂无配置，请点击"添加配置"按钮</span>
        </div>
        
        <div v-else class="config-grid">
          <div v-for="config in configs" :key="config.id" class="config-card">
            <div class="config-header">
              <span class="config-category">{{ getCategoryLabel(config.category) }}</span>
              <span :class="['config-status', config.enabled ? 'enabled' : 'disabled']">
                {{ config.enabled ? '启用' : '禁用' }}
              </span>
            </div>
            <div class="config-body">
              <div class="config-type">{{ getTypeLabel(config.category, config.type) }}</div>
              <div class="config-name">{{ getConfigName(config) }}</div>
              <div class="config-desc">{{ config.description || '无描述' }}</div>
            </div>
            <div class="config-footer">
              <div class="config-meta">
                <span>优先级: {{ config.priority }}</span>
              </div>
              <div class="config-actions">
                <button 
                  :class="['btn-toggle', config.enabled ? 'btn-disable' : 'btn-enable']"
                  @click="toggleEnabled(config)"
                  :disabled="loading"
                >
                  {{ config.enabled ? '禁用' : '启用' }}
                </button>
                <button class="btn-delete" @click="handleDelete(config.id)" :disabled="loading">
                  删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </article>

      <!-- 添加配置表单 -->
      <article v-if="showAddForm" class="panel form-panel">
        <div class="panel-header">
          <h3>添加配置</h3>
          <button class="ghost" @click="cancelAdd">取消</button>
        </div>
        <form class="config-form" @submit.prevent="handleAdd">
          <div class="form-row">
            <label class="form-field">
              <span>分类 *</span>
              <select v-model="form.category" @change="onCategoryChange" required>
                <option value="">请选择分类</option>
                <option v-for="ct in configTypes" :key="ct.category" :value="ct.category">
                  {{ ct.displayName }}
                </option>
              </select>
            </label>
            <label class="form-field">
              <span>类型 *</span>
              <select v-model="form.type" @change="onTypeChange" :disabled="!form.category" required>
                <option value="">请选择类型</option>
                <option v-for="t in typesForCategory" :key="t.type" :value="t.type">
                  {{ t.displayName }}
                </option>
              </select>
            </label>
          </div>
          <div class="form-row">
            <label class="form-field">
              <span>配置项 *</span>
              <select v-model="form.configId" :disabled="!form.type" required>
                <option value="">请选择配置</option>
                <option v-for="c in availableConfigsForType" :key="c.id" :value="c.id">
                  {{ c.name }}
                </option>
              </select>
            </label>
            <label class="form-field">
              <span>优先级</span>
              <input v-model.number="form.priority" type="number" min="1" />
            </label>
          </div>
          <label class="form-field full-width">
            <span>描述</span>
            <input v-model="form.description" type="text" placeholder="输入配置描述..." />
          </label>
          <label class="form-field checkbox-field">
            <input v-model="form.enabled" type="checkbox" />
            <span>启用此配置</span>
          </label>
          <div class="form-actions">
            <button type="submit" class="primary" :disabled="loading || !isFormValid">
              {{ loading ? '添加中...' : '添加' }}
            </button>
          </div>
        </form>
      </article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useWorkspaceStore } from '@/store/workspace-store'
import { listAgents, type Agent } from '@/api/agent-api'
import { listAgentConfigs, setAgentConfig, updateAgentConfig, deleteAgentConfig, type AgentConfig } from '@/api/agent-config-api'
import { getConfigTypes, getAvailableConfigs, type ConfigTypeDefinition, type AvailableConfig } from '@/api/agent-config-type-api'

const store = useWorkspaceStore()
const error = ref('')
const loading = ref(false)
const agents = ref<Agent[]>([])
const selectedAgentId = ref<string | null>(null)
const configs = ref<AgentConfig[]>([])
const configTypes = ref<ConfigTypeDefinition[]>([])
const availableConfigs = ref<Map<string, AvailableConfig[]>>(new Map())
const showAddForm = ref(false)

const form = ref({
  category: '',
  type: '',
  configId: '',
  description: '',
  priority: 1,
  enabled: true,
})

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

const selectedAgentName = computed(() => {
  const agent = agents.value.find(a => a.id === selectedAgentId.value)
  return agent?.name || ''
})

const typesForCategory = computed(() => {
  const ct = configTypes.value.find(c => c.category === form.value.category)
  return ct?.types || []
})

const availableConfigsForType = computed(() => {
  return availableConfigs.value.get(form.value.type) || []
})

const isFormValid = computed(() => {
  return form.value.category && form.value.type && form.value.configId
})

onMounted(() => {
  if (selectionReady.value) {
    loadAgents()
    loadConfigTypes()
  }
})

watch(() => [store.tenantId, store.workspaceId], () => {
  if (selectionReady.value) {
    loadAgents()
    loadConfigTypes()
  }
})

watch(selectedAgentId, () => {
  if (selectedAgentId.value) {
    loadConfigs()
  }
})

watch(() => form.value.type, (newType) => {
  if (newType && form.value.category) {
    loadAvailableConfigs(form.value.category, newType)
  }
})

function selectAgent(agentId: string) {
  selectedAgentId.value = agentId
  showAddForm.value = false
}

function getCategoryLabel(category: string): string {
  const ct = configTypes.value.find(c => c.category === category)
  return ct?.displayName || category
}

function getTypeLabel(category: string, type: string): string {
  const ct = configTypes.value.find(c => c.category === category)
  const t = ct?.types.find(t => t.type === type)
  return t?.displayName || type
}

function getConfigName(config: AgentConfig): string {
  const configsList = availableConfigs.value.get(config.type)
  const found = configsList?.find(c => c.id === config.configId)
  return found?.name || config.configId
}

function onCategoryChange() {
  form.value.type = ''
  form.value.configId = ''
}

function onTypeChange() {
  form.value.configId = ''
}

function cancelAdd() {
  showAddForm.value = false
  resetForm()
}

function resetForm() {
  form.value = {
    category: '',
    type: '',
    configId: '',
    description: '',
    priority: 1,
    enabled: true,
  }
}

async function loadAgents() {
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    agents.value = await listAgents(selection)
  } catch (e: any) {
    error.value = e.message
  }
}

async function loadConfigTypes() {
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    configTypes.value = await getConfigTypes(selection)
  } catch (e: any) {
    error.value = e.message
  }
}

async function loadConfigs() {
  if (!selectedAgentId.value) return
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    configs.value = await listAgentConfigs(selection, selectedAgentId.value)
  } catch (e: any) {
    error.value = e.message
  }
}

async function loadAvailableConfigs(category: string, type: string) {
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    const configs = await getAvailableConfigs(selection, category, type, store.workspaceId)
    availableConfigs.value.set(type, configs)
  } catch (e: any) {
    error.value = e.message
  }
}

async function handleAdd() {
  if (!selectedAgentId.value || !isFormValid.value) return
  loading.value = true
  error.value = ''
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    // 获取配置名称作为描述
    const configName = getConfigNameFromSelection()
    const description = form.value.description || configName
    
    await setAgentConfig(selection, selectedAgentId.value, {
      category: form.value.category,
      type: form.value.type,
      configId: form.value.configId,
      description: description,
      priority: form.value.priority,
      enabled: form.value.enabled,
    })
    await loadConfigs()
    cancelAdd()
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function getConfigNameFromSelection(): string {
  const configsList = availableConfigs.value.get(form.value.type)
  const found = configsList?.find(c => c.id === form.value.configId)
  return found?.name || ''
}

async function toggleEnabled(config: AgentConfig) {
  if (!selectedAgentId.value) return
  loading.value = true
  error.value = ''
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    await updateAgentConfig(selection, selectedAgentId.value, config.id, {
      ...config,
      enabled: !config.enabled,
    })
    await loadConfigs()
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function handleDelete(configId: string) {
  if (!selectedAgentId.value) return
  if (!confirm('确定要删除这个配置吗？')) return
  loading.value = true
  error.value = ''
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    await deleteAgentConfig(selection, selectedAgentId.value, configId)
    await loadConfigs()
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.agent-config-page {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #1f2937;
}

.muted {
  color: #6b7280;
  font-size: 0.875rem;
}

.status {
  color: #dc2626;
  font-size: 0.875rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
}

.panel {
  background: white;
  border-radius: 0.5rem;
  border: 1px solid #e5e7eb;
  padding: 1.5rem;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.panel-header h3 {
  margin: 0;
  font-size: 1.125rem;
  color: #374151;
}

/* Agent Grid */
.agent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.agent-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.agent-card:hover {
  border-color: #3b82f6;
  background: #f8fafc;
}

.agent-card.active {
  border-color: #3b82f6;
  background: #eff6ff;
}

.agent-icon {
  width: 2.5rem;
  height: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  border-radius: 0.5rem;
}

.agent-icon svg {
  width: 1.5rem;
  height: 1.5rem;
  color: #6b7280;
}

.agent-info {
  flex: 1;
  min-width: 0;
}

.agent-name {
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.agent-id {
  font-size: 0.75rem;
}

.status-badge {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
}

.status-badge.published {
  background: #dcfce7;
  color: #166534;
}

.status-badge.unpublished {
  background: #f3f4f6;
  color: #6b7280;
}

/* Config Grid */
.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.config-card {
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.config-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.config-category {
  font-size: 0.75rem;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
}

.config-status {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
}

.config-status.enabled {
  background: #dcfce7;
  color: #166534;
}

.config-status.disabled {
  background: #fee2e2;
  color: #991b1b;
}

.config-body {
  padding: 1rem;
}

.config-type {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.config-name {
  font-size: 1rem;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.config-desc {
  font-size: 0.875rem;
  color: #6b7280;
}

.config-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

.config-meta {
  font-size: 0.75rem;
  color: #6b7280;
}

.config-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-toggle, .btn-delete {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-enable {
  background: #dcfce7;
  color: #166534;
}

.btn-enable:hover {
  background: #bbf7d0;
}

.btn-disable {
  background: #fee2e2;
  color: #991b1b;
}

.btn-disable:hover {
  background: #fecaca;
}

.btn-delete {
  background: #f3f4f6;
  color: #6b7280;
}

.btn-delete:hover {
  background: #e5e7eb;
}

/* Form */
.form-panel {
  position: relative;
}

.config-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-field span {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.form-field.full-width {
  grid-column: 1 / -1;
}

.form-field.checkbox-field {
  flex-direction: row;
  align-items: center;
}

.form-field input,
.form-field select {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 0.25rem;
  font-size: 0.875rem;
}

.form-field input:focus,
.form-field select:focus {
  outline: none;
  border-color: #3b82f6;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

/* Buttons */
button.primary {
  background: #3b82f6;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
}

button.primary:hover {
  background: #2563eb;
}

button.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

button.ghost {
  background: transparent;
  color: #6b7280;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: 1px solid #d1d5db;
  cursor: pointer;
  font-size: 0.875rem;
}

button.ghost:hover {
  background: #f3f4f6;
}

.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: #6b7280;
  gap: 0.5rem;
}

.empty-hint .icon {
  width: 2rem;
  height: 2rem;
}
</style>
