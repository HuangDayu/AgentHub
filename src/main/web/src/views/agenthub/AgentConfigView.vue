<template>
  <section class="agent-config-page glass-float">
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>

    <template v-else>
      <!-- Agent选择 -->
      <article class="panel glass-effect header-panel">
        <div class="header-row">
          <div class="header-left">
            <h2>Agent配置</h2>
            <p class="muted">管理Agent的配置关联关系</p>
          </div>
          <div class="header-right">
            <CustomSelect v-model="selectedAgentId" :options="agentsOptions" placeholder="请选择Agent" />
            <CustomSelect v-model="selectedCategory" :options="configTypesOptionsWithAll" placeholder="全部类别" />
            <CustomSelect v-model="selectedType" :options="typesForFilterOptionsWithAll" placeholder="全部类型" />
            <CustomSelect v-model="cardSize" :options="cardSizeOptions" placeholder="卡片大小" />
          </div>
        </div>
        <p v-if="error" class="status">{{ error }}</p>
      </article>

      <!-- 配置列表 -->
      <article v-if="selectedAgentId" class="panel float-effect">

        <!-- 配置方块网格 -->
        <div v-if="filteredConfigs.length === 0" class="empty-hint">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
          </svg>
          <span>暂无配置，请点击"添加配置"按钮</span>
        </div>

        <div v-else class="config-grid" :class="`size-${cardSize}`">
          <div v-for="config in filteredConfigs" :key="config.id" class="config-card" :class="`category-${config.category.toLowerCase()}`">
            <div class="config-header">
              <span class="config-category">{{ getCategoryLabel(config.category) }}/{{ getTypeLabel(config.category, config.type) }}</span>
              <span :class="['config-status', config.enabled ? 'enabled' : 'disabled']">
                {{ config.enabled ? '启用' : '禁用' }}
              </span>
            </div>
            <div class="config-body">
              <div class="config-name">{{ config.name || '无名称' }}</div>
              <div class="config-desc">
                <span class="desc-text">{{ truncateText(config.description, 30) }}</span>
              </div>
            </div>
            <div class="config-footer">
              <div class="config-meta">
                优先级: {{ config.priority }}
              </div>
              <div class="config-actions">
                <CustomButton type="ghost" size="small" @click="showDetail(config)">详情</CustomButton>
                <CustomButton type="ghost" size="small" @click="toggleEnabled(config)" :disabled="loading">
                  {{ config.enabled ? '禁用' : '启用' }}
                </CustomButton>
                <CustomButton type="ghost" size="small" @click="handleDelete(config.id)" :disabled="loading">
                  删除
                </CustomButton>
              </div>
            </div>
          </div>
        </div>
      </article>

      <!-- 详情弹窗 -->
      <ModalDialog
        v-model:visible="showDetailModal"
        title="配置详情"
        :show-footer="false"
        @close="closeDetail"
      >
        <div class="detail-row">
          <span class="detail-label">类别/类型:</span>
          <span class="detail-value">{{ getCategoryLabel(detailConfig?.category || '') }}/{{ getTypeLabel(detailConfig?.category || '', detailConfig?.type || '') }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">名称:</span>
          <span class="detail-value">{{ detailConfig?.name || '无名称' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">描述:</span>
          <span class="detail-value">{{ detailConfig?.description || '无描述' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">优先级:</span>
          <span class="detail-value">{{ detailConfig?.priority }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态:</span>
          <span class="detail-value">{{ detailConfig?.enabled ? '启用' : '禁用' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">配置ID:</span>
          <span class="detail-value">{{ detailConfig?.configId }}</span>
        </div>
      </ModalDialog>

      <!-- 添加配置表单 -->
      <ModalDialog
      v-model:visible="showAddForm"
      title="添加配置"
      @confirm="handleAdd"
      @close="showAddForm = false"
      :confirm-disabled="!isFormValid || loading"
      confirm-text="添加"
    >
      <form>
        <div class="form-row">
            <label class="form-field">
              <span>分类 *</span>
              <CustomSelect v-model="form.category" :options="configTypesOptions" placeholder="请选择分类" required />
            </label>
            <label class="form-field">
              <span>类型 *</span>
              <CustomSelect v-model="form.type" :options="typesForCategoryOptions" placeholder="请选择类型" :disabled="!form.category" required />
            </label>
          </div>
          <div class="form-row">
            <label class="form-field">
              <span>配置项 *</span>
              <CustomSelect v-model="form.configId" :options="availableConfigsForTypeOptions" placeholder="请选择配置" :disabled="!form.type" required />
            </label>
            <label class="form-field">
              <span>优先级</span>
              <input v-model.number="form.priority" type="number" min="1" />
            </label>
          </div>
          <label class="form-field full-width">
            <span>名称</span>
            <input v-model="form.name" type="text" placeholder="输入配置名称..." />
          </label>
          <label class="form-field full-width">
            <span>描述</span>
            <input v-model="form.description" type="text" placeholder="输入配置描述..." />
          </label>
          <label class="form-field">
            <span>启用此配置</span>
            <CustomSelect v-model="form.enabled" :options="enabledOptions" placeholder="请选择" />
          </label>
      </form>
    </ModalDialog>
    </template>
  </section>
</template>

<script setup lang="ts">
import {computed, onMounted, onUnmounted, ref, watch} from 'vue'
import { showConfirm } from '@/utils/confirm'
import {useRoute} from 'vue-router'
import {useWorkspaceStore} from '@/store/workspace-store'
import {type Agent, listAgents} from '@/api/agent-api'
import {
  type AgentConfig,
  deleteAgentConfig,
  listAgentConfigs,
  setAgentConfig,
  syncAgentConfigs,
  updateAgentConfig
} from '@/api/agent-config-api'
import {
  type AvailableConfig,
  type ConfigTypeDefinition,
  getAvailableConfigs,
  getConfigTypes
} from '@/api/agent-config-type-api'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const route = useRoute()
const error = ref('')
const loading = ref(false)
const agents = ref<Agent[]>([])
const selectedAgentId = ref<string | null>(null)
const configs = ref<AgentConfig[]>([])
const configTypes = ref<ConfigTypeDefinition[]>([])
const availableConfigs = ref<Map<string, AvailableConfig[]>>(new Map())
const showAddForm = ref(false)
const selectedCategory = ref('')
const selectedType = ref('')
const cardSize = ref(3)
const showDetailModal = ref(false)
const detailConfig = ref<AgentConfig | null>(null)
const agentsOptions = computed(() => agents.value.map(agent => ({ value: agent.id, label: agent.name })))
const configTypesOptions = computed(() => configTypes.value.map(ct => ({ value: ct.category, label: ct.displayName })))
const configTypesOptionsWithAll = computed(() => [
  { value: '', label: '全部类别' },
  ...configTypesOptions.value
])
const typesForFilterOptions = computed(() => typesForFilter.value.map(t => ({ value: t.type, label: t.displayName })))
const typesForFilterOptionsWithAll = computed(() => [
  { value: '', label: '全部类型' },
  ...typesForFilterOptions.value
])
const cardSizeOptions = computed(() => [
  { value: 1, label: '大小: 1级' },
  { value: 2, label: '大小: 2级' },
  { value: 3, label: '大小: 3级' },
  { value: 4, label: '大小: 4级' },
  { value: 5, label: '大小: 5级' }
])

const enabledOptions = computed(() => [
  { value: true, label: '是' },
  { value: false, label: '否' }
])

const availableConfigsForTypeOptions = computed(() => {
  const ac = availableConfigs.value.get(form.value.type)
  return ac?.map(c => ({ value: c.id, label: c.name })) || []
})

const form = ref({
  category: '',
  type: '',
  configId: '',
  name: '',
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

const typesForCategoryOptions = computed(() => {
  return typesForCategory.value.map(t => ({ value: t.type, label: t.displayName }))
})

const typesForFilter = computed(() => {
  if (!selectedCategory.value) {
    // 返回所有类型
    const allTypes: { type: string; displayName: string }[] = []
    for (const ct of configTypes.value) {
      for (const t of ct.types) {
        if (!allTypes.some(at => at.type === t.type)) {
          allTypes.push({ type: t.type, displayName: t.displayName })
        }
      }
    }
    return allTypes
  }
  const ct = configTypes.value.find(c => c.category === selectedCategory.value)
  return ct?.types || []
})

const availableConfigsForType = computed(() => {
  return availableConfigs.value.get(form.value.type) || []
})

const isFormValid = computed(() => {
  return form.value.category && form.value.type && form.value.configId
})

const filteredConfigs = computed(() => {
  let result = configs.value
  if (selectedCategory.value) {
    result = result.filter(c => c.category === selectedCategory.value)
  }
  if (selectedType.value) {
    result = result.filter(c => c.type === selectedType.value)
  }
  return result
})

// 监听全局新增事件
const handleGlobalAdd = () => {
  showAddForm.value = true
}

const handleGlobalSync = () => {
  if (window.location.pathname.includes('agent-configs') && selectedAgentId.value) {
    handleSync()
  }
}

onMounted(() => {
  window.addEventListener('global-add', handleGlobalAdd)
  window.addEventListener('global-sync', handleGlobalSync)

  if (selectionReady.value) {
    loadAgents()
    loadConfigTypes()
  }
})

onUnmounted(() => {
  window.removeEventListener('global-add', handleGlobalAdd)
  window.removeEventListener('global-sync', handleGlobalSync)
})

// 从URL参数读取agentId
const agentIdFromQuery = route.query.agentId as string
if (agentIdFromQuery) {
  selectedAgentId.value = agentIdFromQuery
}

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

// 当类别变化时重置类型筛选
watch(selectedCategory, () => {
  selectedType.value = ''
})

watch(() => form.value.type, (newType) => {
  if (newType && form.value.category) {
    loadAvailableConfigs(form.value.category, newType)
  }
})

// 监听配置项选择，自动填充描述
watch(() => form.value.configId, (newConfigId) => {
  if (newConfigId) {
    const config = availableConfigsForType.value.find(c => c.id === newConfigId)
    if (config) {
      form.value.name = config.name
      form.value.description = config.description || ''
    }
  }
})

function onAgentChange() {
  showAddForm.value = false
  if (selectedAgentId.value) {
    loadConfigs()
  }
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

function truncateText(text: string | undefined, maxLength: number): string {
  if (!text) return '无描述'
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

function showDetail(config: AgentConfig) {
  detailConfig.value = config
  showDetailModal.value = true
}

function closeDetail() {
  showDetailModal.value = false
  detailConfig.value = null
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
    name: '',
    description: '',
    priority: 1,
    enabled: true,
  }
}

async function loadAgents() {
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    agents.value = await listAgents(selection)
    // 默认选择第一个Agent（如果URL没有指定agentId）
    if (agents.value.length > 0 && !selectedAgentId.value) {
      selectedAgentId.value = agents.value[0].id
      loadConfigs()
    }
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
    // 获取配置名称
    const configName = getConfigNameFromSelection()
    const name = form.value.name || configName
    const description = form.value.description || configName

    await setAgentConfig(selection, selectedAgentId.value, {
      category: form.value.category,
      type: form.value.type,
      configId: form.value.configId,
      name: name,
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
  if (!await showConfirm('确定要删除这个配置吗？')) return
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

async function handleSync() {
  if (!selectedAgentId.value) return
  loading.value = true
  error.value = ''
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    await syncAgentConfigs(selection, selectedAgentId.value)
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
  gap: 1rem;
}

.muted {
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

.status {
  color: var(--color-error);
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--color-text-muted);
}

.panel {
  background: var(--bg-card-solid);
  border-radius: 0.5rem;
  border: 1px solid var(--color-border);
  padding: 1rem;
}

.header-panel {
  padding: 1.25rem;
  position: relative;
  z-index: 10;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.header-left h2 {
  margin: 0 0 0.25rem 0;
  font-size: 1.25rem;
  color: var(--color-heading);
}

.header-right {
  display: flex;
  align-items: center;
}

.agent-select {
  min-width: 200px;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border-strong);
  border-radius: 0.25rem;
  font-size: 0.875rem;
}

.agent-select:focus {
  outline: none;
  border-color: var(--color-primary);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.panel-header h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--color-heading);
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.filter-select, .size-select {
  padding: 0.375rem 0.5rem;
  border: 1px solid var(--color-border-strong);
  border-radius: 0.25rem;
  font-size: 0.8rem;
  background: var(--bg-card-solid);
}

.filter-select:focus, .size-select:focus {
  outline: none;
  border-color: var(--color-primary);
}

/* Config Grid - 大小调整 */
.config-grid {
  display: grid;
  gap: 0.75rem;
}

.config-grid.size-1 {
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.config-grid.size-2 {
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
}

.config-grid.size-3 {
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
}

.config-grid.size-4 {
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
}

.config-grid.size-5 {
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
}

.config-card {
  border: 1px solid var(--color-border);
  border-radius: 0.375rem;
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.config-card:hover {
  box-shadow: 0 2px 4px -1px rgba(0, 0, 0, 0.1);
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0.75rem;
  background: var(--bg-elevated);
  border-bottom: 1px solid var(--color-border);
}

.config-category {
  font-size: 0.7rem;
  font-weight: 600;
  color: var(--color-heading);
  text-transform: uppercase;
}

.config-status {
  font-size: 0.7rem;
  padding: 0.125rem 0.375rem;
  border-radius: 0.125rem;
}

.config-status.enabled {
  background: var(--color-success-subtle);
  color: var(--color-success-dark);
}

.config-status.disabled {
  background: var(--color-error-subtle);
  color: var(--color-error-dark);
}

.config-body {
  padding: 0.625rem 0.75rem;
}

.config-name {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.25rem;
}

.config-desc {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  line-height: 1.3;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.desc-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.config-footer {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  padding: 0.5rem 0.75rem;
  background: var(--bg-elevated);
  border-top: 1px solid var(--color-border);
}

.detail-row {
  display: flex;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--bg-stripe);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  width: 100px;
  font-weight: 500;
  color: var(--color-heading);
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: var(--color-heading);
  word-break: break-all;
}

.config-meta {
  font-size: 0.7rem;
  color: var(--color-text-muted);
}

.config-actions {
  display: flex;
  gap: 0.375rem;
}

/* Form */
.form-panel {
  position: relative;
}

.config-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.form-field span {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--color-heading);
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
  border: 1px solid var(--color-border-strong);
  border-radius: 0.25rem;
  font-size: 0.875rem;
}

.form-field input:focus,
.form-field select:focus {
  outline: none;
  border-color: var(--color-primary);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

/* Buttons */
button.primary {
  background: var(--color-primary); color: var(--color-text-inverse);
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
}

button.primary:hover {
  background: var(--color-primary-dark);
}

button.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

button.secondary {
  background: var(--bg-stripe);
  color: var(--color-heading);
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: 1px solid var(--color-border-strong);
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
}

button.secondary:hover {
  background: var(--color-border);
}

button.secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

button.ghost {
  background: transparent;
  color: var(--color-text-muted);
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  border: 1px solid var(--color-border-strong);
  cursor: pointer;
  font-size: 0.875rem;
}

button.ghost:hover {
  background: var(--bg-stripe);
}

.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  color: var(--color-text-muted);
  gap: 0.5rem;
}

.empty-hint .icon {
  width: 1.5rem;
  height: 1.5rem;
}
</style>
