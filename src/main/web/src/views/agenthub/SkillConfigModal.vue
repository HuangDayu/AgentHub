<template>
  <ModalDialog
    v-model:visible="visible"
    title="技能配置管理"
    @close="$emit('close')"
    :show-footer="false"
    size="xlarge"
  >
    <div class="config-content">
      <div class="config-header">
        <CustomButton type="primary" size="small" @click="showCreateConfig = true">
          <span class="btn-icon">+</span> 新建配置
        </CustomButton>
      </div>

      <div v-if="loading" class="loading-state">
        <p>加载中...</p>
      </div>

      <div v-else-if="configs.length === 0" class="empty-state">
        <div class="empty-icon">⚙️</div>
        <p>暂无技能配置</p>
        <p class="empty-hint">创建配置来管理本地技能目录的同步</p>
      </div>

      <div v-else class="config-list">
        <div v-for="config in configs" :key="config.id" class="config-card">
          <div class="config-card-body">
            <div class="config-card-header">
              <div class="config-title-row">
                <h4 class="config-name">{{ config.name }}</h4>
                <span class="config-status" :class="config.enabled ? 'status-enabled' : 'status-disabled'">
                  {{ config.enabled ? '启用' : '禁用' }}
                </span>
              </div>
              <p v-if="config.description" class="config-desc">{{ config.description }}</p>
            </div>

            <div class="config-card-meta">
              <div class="meta-item">
                <span class="meta-icon">⏱️</span>
                <span class="meta-label">同步间隔</span>
                <span class="meta-value">{{ formatInterval(config.syncInterval) }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-icon">🔄</span>
                <span class="meta-label">自动同步</span>
                <span class="meta-value">{{ config.autoSync ? '是' : '否' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-icon">✅</span>
                <span class="meta-label">同步启用</span>
                <span class="meta-value">{{ config.syncEnabled ? '是' : '否' }}</span>
              </div>
            </div>

            <div v-if="config.skillPaths.length > 0" class="config-paths">
              <span class="paths-label">监控路径</span>
              <div class="paths-list">
                <span v-for="(path, idx) in config.skillPaths" :key="idx" class="path-tag">
                  📁 {{ path }}
                </span>
              </div>
            </div>
          </div>

          <div class="config-card-actions">
            <CustomButton type="ghost" size="small" @click="editConfig(config)">
              <span class="action-icon">✏️</span> 编辑
            </CustomButton>
            <CustomButton type="ghost" size="small" @click="syncWithConfig(config.id)">
              <span class="action-icon">🔄</span> 同步
            </CustomButton>
            <CustomButton type="ghost" size="small" @click="deleteConfigHandler(config.id)">
              <span class="action-icon">🗑️</span> 删除
            </CustomButton>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建/编辑配置对话框 -->
    <ModalDialog
      v-model:visible="showCreateConfig"
      :title="editingConfig ? '编辑配置' : '新建配置'"
      @confirm="saveConfig"
      @close="closeCreateConfig"
      confirm-text="保存"
      size="medium"
    >
      <form class="config-form">
        <div class="form-section">
          <div class="form-section-header">
            <span class="section-icon">📝</span>
            <span>基本信息</span>
          </div>
          <div class="form-group">
            <label>配置名称 <span class="required">*</span></label>
            <input
              v-model="configForm.name"
              required
              placeholder="例如: 本地技能目录"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea
              v-model="configForm.description"
              rows="2"
              placeholder="可选，简要说明该配置的用途"
              class="form-input"
            ></textarea>
          </div>
        </div>

        <div class="form-section">
          <div class="form-section-header">
            <span class="section-icon">⚙️</span>
            <span>同步设置</span>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>同步间隔</label>
              <div class="interval-input">
                <input
                  v-model.number="configForm.syncInterval"
                  type="number"
                  min="60"
                  class="form-input"
                />
                <span class="interval-unit">秒</span>
              </div>
              <span class="form-hint">最小 60 秒，默认 3600 秒（1小时）</span>
            </div>
            <div class="form-group">
              <label>启用同步</label>
              <div class="toggle-wrapper">
                <label class="toggle">
                  <input v-model="configForm.syncEnabled" type="checkbox" />
                  <span class="toggle-slider"></span>
                </label>
                <span class="toggle-label">{{ configForm.syncEnabled ? '已开启' : '已关闭' }}</span>
              </div>
              <span class="form-hint">关闭后将不同步此配置下的技能文件</span>
            </div>
            <div class="form-group">
              <label>自动同步</label>
              <div class="toggle-wrapper">
                <label class="toggle">
                  <input v-model="configForm.autoSync" type="checkbox" />
                  <span class="toggle-slider"></span>
                </label>
                <span class="toggle-label">{{ configForm.autoSync ? '已开启' : '已关闭' }}</span>
              </div>
              <span class="form-hint">开启后将按间隔自动同步技能文件</span>
            </div>
          </div>
        </div>

        <div class="form-section">
          <div class="form-section-header">
            <span class="section-icon">📁</span>
            <span>监控路径</span>
            <span class="required-tag">至少一个</span>
          </div>
          <div class="path-list">
            <div v-for="(path, idx) in configForm.skillPaths" :key="idx" class="path-item">
              <span class="path-index">{{ idx + 1 }}</span>
              <input
                v-model="configForm.skillPaths[idx]"
                placeholder="例如: C:\Users\xxx\.agents\skills"
                class="form-input path-input"
              />
              <button
                type="button"
                class="path-remove"
                :disabled="configForm.skillPaths.length <= 1"
                @click="configForm.skillPaths.splice(idx, 1)"
              >
                ✕
              </button>
            </div>
          </div>
          <CustomButton type="ghost" size="small" @click="configForm.skillPaths.push('')">
            <span class="btn-icon">+</span> 添加路径
          </CustomButton>
        </div>
      </form>
    </ModalDialog>
  </ModalDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useWorkspaceStore } from '@/store/workspace-store'
import { showConfirm } from '@/utils/confirm'
import {
  listSkillConfigs,
  createSkillConfig,
  updateSkillConfig,
  deleteSkillConfig,
  syncSkillWithConfig
} from '@/api/skill-api'
import type { SkillConfig } from '@/types/memory'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'

const visible = defineModel<boolean>('visible', { default: false })
const emit = defineEmits<{ close: [] }>()

const store = useWorkspaceStore()
const configs = ref<SkillConfig[]>([])
const loading = ref(false)
const showCreateConfig = ref(false)
const editingConfig = ref<SkillConfig | null>(null)

const configForm = ref({
  name: '',
  description: '',
  syncEnabled: true,
  syncInterval: 3600,
  autoSync: false,
  skillPaths: [''] as string[]
})

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

watch(visible, async (isVisible) => {
  if (isVisible) {
    await loadConfigs()
  }
})

async function loadConfigs() {
  loading.value = true
  try {
    configs.value = await listSkillConfigs(selection())
  } catch (e) {
    console.error('Failed to load configs', e)
  } finally {
    loading.value = false
  }
}

function editConfig(config: SkillConfig) {
  editingConfig.value = config
  configForm.value = { name: config.name, description: config.description || '', syncEnabled: config.syncEnabled, syncInterval: config.syncInterval, autoSync: config.autoSync, skillPaths: [...config.skillPaths] }
  showCreateConfig.value = true
}

async function saveConfig() {
  if (!canSaveConfig()) return
  await performSaveConfig()
}

function canSaveConfig(): boolean {
  return Boolean(configForm.value.name && validSkillPaths().length > 0)
}

function validSkillPaths(): string[] {
  return configForm.value.skillPaths.filter(p => p.trim())
}

async function performSaveConfig(): Promise<void> {
  try {
    await saveConfigPayload(buildSaveConfigPayload())
    await loadConfigs()
    closeCreateConfig()
  } catch (e) {
    console.error('Failed to save config', e)
  }
}

function buildSaveConfigPayload() {
  return {
    name: configForm.value.name,
    description: configForm.value.description,
    syncEnabled: configForm.value.syncEnabled,
    syncInterval: configForm.value.syncInterval,
    autoSync: configForm.value.autoSync,
    skillPaths: validSkillPaths(),
  }
}

async function saveConfigPayload(payload: any): Promise<void> {
  if (editingConfig.value) {
    await updateSkillConfig(selection(), editingConfig.value.id, payload)
  } else {
    await createSkillConfig(selection(), payload)
  }
}

async function deleteConfigHandler(id: string) {
  if (await showConfirm('确定删除此配置？')) {
    try {
      await deleteSkillConfig(selection(), id)
      await loadConfigs()
    } catch (e) {
      console.error('Failed to delete config', e)
    }
  }
}

async function syncWithConfig(configId: string) {
  try {
    await syncSkillWithConfig(selection(), configId)
    await loadConfigs()
  } catch (e) {
    console.error('Failed to sync with config', e)
  }
}

const EMPTY_CONFIG_FORM: SkillConfigForm = { name: '', description: '', syncEnabled: true, syncInterval: 3600, autoSync: false, skillPaths: [''] }

function closeCreateConfig() {
  showCreateConfig.value = false; editingConfig.value = null
  configForm.value = { ...EMPTY_CONFIG_FORM }
}

function formatInterval(seconds: number): string {
  if (seconds < 60) return `${seconds}秒`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}小时`
  return `${Math.floor(seconds / 86400)}天`
}
</script>

<style scoped>
.config-content {
  max-height: 70vh;
  overflow-y: auto;
}

.config-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 1.25rem;
}

.btn-icon {
  margin-right: 0.25rem;
}

/* Config list */
.config-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.config-card {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s;
  background: var(--bg-input);
}

.config-card:hover {
  border-color: color-mix(in srgb, var(--color-primary) 40%, var(--border-color));
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.config-card-body {
  padding: 1.25rem;
}

.config-card-header {
  margin-bottom: 0.75rem;
}

.config-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.config-name {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.config-status {
  font-size: 0.65rem;
  font-weight: 600;
  padding: 0.15rem 0.5rem;
  border-radius: 10px;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.config-desc {
  margin: 0.25rem 0 0;
  font-size: 0.8rem;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.config-card-meta {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 0.75rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.75rem;
}

.meta-icon {
  font-size: 0.8rem;
}

.meta-label {
  color: var(--color-text-muted);
}

.meta-value {
  font-weight: 500;
  color: var(--color-text);
}

.config-paths {
  margin-top: 0.5rem;
}

.paths-label {
  display: block;
  font-size: 0.7rem;
  font-weight: 500;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.375rem;
}

.paths-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
}

.path-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.625rem;
  background: var(--bg-hover);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 0.7rem;
  font-family: monospace;
  color: var(--color-text);
}

.config-card-actions {
  display: flex;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  border-top: 1px solid var(--border-color);
  background: var(--bg-hover);
}

.action-icon {
  margin-right: 0.2rem;
}

.status-enabled {
  color: var(--color-success);
  background: rgba(34, 197, 94, 0.1);
}

.status-disabled {
  color: var(--color-text-muted);
  background: var(--bg-hover);
}

/* Form */
.config-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-section {
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 1.25rem;
}

.form-section-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text);
}

.form-section-header .section-icon {
  font-size: 1rem;
}

.form-section-header .required-tag {
  margin-left: auto;
  font-size: 0.65rem;
  font-weight: 600;
  padding: 0.125rem 0.5rem;
  background: rgba(239, 68, 68, 0.1);
  color: var(--color-error);
  border-radius: 4px;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 0.375rem;
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--color-text);
}

.form-group label .required {
  color: var(--color-error);
  margin-left: 0.125rem;
}

.form-input {
  width: 100%;
  padding: 0.625rem 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-input);
  color: var(--color-text);
  font-size: 0.875rem;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input::placeholder {
  color: var(--color-text-muted);
  opacity: 0.6;
}

textarea.form-input {
  resize: vertical;
  min-height: 60px;
}

.form-hint {
  display: block;
  margin-top: 0.375rem;
  font-size: 0.7rem;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

/* Interval input */
.interval-input {
  display: flex;
  align-items: center;
  gap: 0;
}

.interval-input .form-input {
  border-radius: 6px 0 0 6px;
}

.interval-unit {
  padding: 0.625rem 0.75rem;
  border: 1px solid var(--border-color);
  border-left: none;
  border-radius: 0 6px 6px 0;
  background: var(--bg-hover);
  color: var(--color-text-muted);
  font-size: 0.8rem;
  white-space: nowrap;
}

/* Toggle switch */
.toggle-wrapper {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.toggle {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  cursor: pointer;
}

.toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  inset: 0;
  background: var(--border-color);
  border-radius: 24px;
  transition: all 0.25s;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: all 0.25s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.toggle input:checked + .toggle-slider {
  background: var(--color-primary);
}

.toggle input:checked + .toggle-slider::before {
  transform: translateX(20px);
}

.toggle-label {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  font-weight: 500;
}

/* Path list */
.path-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.path-item {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.path-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--bg-hover);
  color: var(--color-text-muted);
  font-size: 0.7rem;
  font-weight: 600;
  flex-shrink: 0;
}

.path-input {
  flex: 1;
}

.path-remove {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: none;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  font-size: 0.8rem;
}

.path-remove:hover:not(:disabled) {
  color: var(--color-error);
  border-color: var(--color-error);
  background: rgba(239, 68, 68, 0.05);
}

.path-remove:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

/* States */
.loading-state,
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: var(--color-text-muted);
}

.empty-icon {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
  opacity: 0.5;
}

.empty-state p {
  margin: 0.25rem 0;
}

.empty-hint {
  font-size: 0.8rem;
  opacity: 0.7;
}
</style>
