<template>
  <section class="grid glass-float">
    <div class="page-header">
      <div>
        <h2>大模型配置</h2>
        <p class="muted">管理大模型API配置，用于知识库嵌入和Agent对话。</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    
    <article v-if="!selectionReady" class="empty-state glass-effect">请先在"租户空间"页选择租户与工作区。</article>
    
    <template v-else>
      <!-- 创建/编辑大模型配置弹窗 -->
      <ModalDialog
        v-model:visible="showFormModal"
        :title="editingId ? '编辑大模型配置' : '创建大模型配置'"
        @confirm="submitConfig"
        :confirm-disabled="!isFormValid || loading"
        :confirm-text="loading ? '提交中...' : '确定'"
      >
        <form class="field-grid">
          <label class="field">
            <span>名称 *</span>
            <input v-model="form.name" type="text" required placeholder="配置名称" />
          </label>
          
          <label class="field">
            <span>类型 *</span>
            <CustomSelect
              v-model="form.type"
              :options="typeOptions"
              placeholder="请选择类型"
              required
            />
          </label>
          
          <label class="field">
            <span>供应商 *</span>
            <CustomSelect
              v-model="form.supplier"
              :options="supplierOptions"
              placeholder="请选择供应商"
              required
            />
          </label>
          
          <label class="field">
            <span>模型 *</span>
            <input v-model="form.model" type="text" required placeholder="模型名称" />
          </label>
          
          <label class="field">
            <span>API密钥 *</span>
            <input v-model="form.apiKey" type="password" required placeholder="API密钥" />
          </label>
          
          <label class="field">
            <span>API地址</span>
            <input v-model="form.baseUrl" type="url" placeholder="自定义API地址（可选）" />
          </label>
          
          <label class="field checkbox-field">
            <input v-model="form.enabled" type="checkbox" />
            <span>启用此配置</span>
          </label>
        </form>
      </ModalDialog>

      <!-- 配置列表 -->
      <article class="panel stack glass-effect">
        <div class="page-header">
          <h3 style="margin: 0">配置列表</h3>
          <CustomButton type="primary" @click="startCreate">
            创建配置
          </CustomButton>
        </div>
        
        <table class="data-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>类型</th>
              <th>供应商</th>
              <th>模型</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="config in configs" :key="config.id" class="float-effect">
              <td>
                <strong>{{ config.name }}</strong>
                <div class="muted">{{ config.id }}</div>
              </td>
              <td><span class="tag">{{ config.type }}</span></td>
              <td>{{ config.supplier }}</td>
              <td>{{ config.model }}</td>
              <td>
                <span :class="['tag', config.enabled ? 'tag-success' : 'tag-error']">
                  {{ config.enabled ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDateTime(config.createdAt) }}</td>
              <td>
                <div class="chip-row">
                  <CustomButton type="ghost" size="small" @click="startEdit(config)">编辑</CustomButton>
                  <CustomButton type="info" size="small" @click="testModel(config)">测试</CustomButton>
                  <CustomButton type="danger" size="small" @click="handleDelete(config.id)">删除</CustomButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </article>

      <!-- 测试结果弹窗 -->
      <ModalDialog
        :visible="showTestModal"
        :title="testing ? '正在测试模型...' : '测试结果'"
        :show-footer="!testing"
        :show-close="!testing"
        @close="closeTestModal"
      >
        <div v-if="testing" class="test-loading">
          <div class="loading-spinner"></div>
          <p>正在测试大模型连接，请稍候...</p>
          <CustomButton type="secondary" @click="cancelTest">中断测试</CustomButton>
        </div>
        <div v-else :class="['test-result', testResult?.success ? 'test-success' : 'test-error']">
          <p><strong>状态：</strong>{{ testResult?.success ? '成功' : '失败' }}</p>
          <p><strong>消息：</strong>{{ testResult?.message }}</p>
          <p v-if="testResult?.details"><strong>详情：</strong></p>
          <pre v-if="testResult?.details" class="test-details">{{ testResult?.details }}</pre>
        </div>
      </ModalDialog>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'
import {
  listModelConfigs,
  createModelConfig,
  updateModelConfig,
  deleteModelConfig,
  testModelConfig,
} from '@/api/config-api'
import { formatDateTime } from '@/common/format'
import type { ModelConfig } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'

const store = useWorkspaceStore()
const error = ref('')
const configs = ref<ModelConfig[]>([])
const editingId = ref<string | null>(null)
const loading = ref(false)

// 弹窗控制
const showFormModal = ref(false)
const showTestModal = ref(false)
const testing = ref(false)
const testResult = ref<{ success: boolean; message: string; details?: string } | null>(null)

// 表单数据
const form = reactive({
  name: '',
  type: '',
  supplier: '',
  model: '',
  apiKey: '',
  baseUrl: '',
  enabled: true,
})

// 选项数据
const typeOptions = computed(() => [
  { value: 'EMBEDDING', label: '嵌入模型' },
  { value: 'CHAT', label: '对话模型' },
  { value: 'COMPLETION', label: '补全模型' },
])

const supplierOptions = computed(() => [
  { value: 'OPENAI', label: 'OpenAI' },
  { value: 'AZURE', label: 'Azure OpenAI' },
  { value: 'ANTHROPIC', label: 'Anthropic' },
  { value: 'ZHIPU', label: '智谱AI' },
  { value: 'BAIDU', label: '百度文心' },
  { value: 'ALIBABA', label: '阿里通义' },
])

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

const isFormValid = computed(() => {
  return form.name && form.type && form.supplier && form.model && form.apiKey
})

// 开始创建
const startCreate = () => {
  editingId.value = null
  Object.assign(form, {
    name: '',
    type: '',
    supplier: '',
    model: '',
    apiKey: '',
    baseUrl: '',
    enabled: true,
  })
  showFormModal.value = true
}

// 开始编辑
const startEdit = (config: ModelConfig) => {
  editingId.value = config.id
  Object.assign(form, {
    name: config.name,
    type: config.type,
    supplier: config.supplier,
    model: config.model,
    apiKey: config.apiKey,
    baseUrl: config.baseUrl || '',
    enabled: config.enabled,
  })
  showFormModal.value = true
}

// 提交配置
const submitConfig = async () => {
  loading.value = true
  try {
    if (editingId.value) {
      await updateModelConfig(store.tenantId!, store.workspaceId!, editingId.value, form)
    } else {
      await createModelConfig(store.tenantId!, store.workspaceId!, form)
    }
    showFormModal.value = false
    await loadConfigs()
  } catch (e: any) {
    error.value = e.message || '操作失败'
  } finally {
    loading.value = false
  }
}

// 删除配置
const handleDelete = async (id: string) => {
  if (!confirm('确定要删除此配置吗？')) return
  try {
    await deleteModelConfig(store.tenantId!, store.workspaceId!, id)
    await loadConfigs()
  } catch (e: any) {
    error.value = e.message || '删除失败'
  }
}

// 测试模型
const testModel = async (config: ModelConfig) => {
  showTestModal.value = true
  testing.value = true
  testResult.value = null
  
  try {
    const result = await testModelConfig(store.tenantId!, store.workspaceId!, config.id)
    testResult.value = result
  } catch (e: any) {
    testResult.value = {
      success: false,
      message: e.message || '测试失败',
    }
  } finally {
    testing.value = false
  }
}

// 关闭测试弹窗
const closeTestModal = () => {
  showTestModal.value = false
  testResult.value = null
}

// 中断测试
const cancelTest = () => {
  testing.value = false
  showTestModal.value = false
}

// 加载配置列表
const loadConfigs = async () => {
  try {
    configs.value = await listModelConfigs(store.tenantId!, store.workspaceId!)
  } catch (e: any) {
    error.value = e.message || '加载失败'
  }
}

onMounted(() => {
  if (selectionReady.value) {
    loadConfigs()
  }
})

watch(() => [store.tenantId, store.workspaceId], () => {
  if (selectionReady.value) {
    loadConfigs()
  }
})
</script>

<style scoped>
.grid {
  //display: flex;
  //flex-direction: column;
  //gap: 20px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #27415d;
}

.page-header h3 {
  margin: 0;
  font-size: 18px;
  color: #27415d;
}

.muted {
  color: rgba(38, 66, 102, 0.6);
  font-size: 14px;
}

.status {
  color: #c94a35;
  font-size: 14px;
}

.empty-state {
  padding: 40px;
  text-align: center;
  color: rgba(38, 66, 102, 0.6);
  font-size: 16px;
}

.panel {
  padding: 20px;
}

.stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field span {
  font-size: 14px;
  font-weight: 500;
  color: #27415d;
}

.field input,
.field select {
  padding: 10px 14px;
  border: 1px solid rgba(38, 66, 102, 0.15);
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.field input:focus,
.field select:focus {
  outline: none;
  border-color: #3a8ad6;
}

.checkbox-field {
  flex-direction: row;
  align-items: center;
  gap: 10px;
}

.checkbox-field input[type="checkbox"] {
  width: 18px;
  height: 18px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid rgba(38, 66, 102, 0.08);
}

.data-table th {
  font-weight: 600;
  color: #27415d;
  background: rgba(58, 138, 214, 0.05);
}

.data-table tr:hover {
  background: rgba(58, 138, 214, 0.02);
}

.tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(58, 138, 214, 0.1);
  color: #3a8ad6;
}

.tag-success {
  background: rgba(45, 157, 120, 0.1);
  color: #2d9d78;
}

.tag-error {
  background: rgba(201, 74, 53, 0.1);
  color: #c94a35;
}

.chip-row {
  display: flex;
  gap: 8px;
}

.test-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(58, 138, 214, 0.2);
  border-top-color: #3a8ad6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.test-result {
  padding: 20px;
  border-radius: 8px;
}

.test-success {
  background: rgba(45, 157, 120, 0.1);
  border: 1px solid rgba(45, 157, 120, 0.2);
}

.test-error {
  background: rgba(201, 74, 53, 0.1);
  border: 1px solid rgba(201, 74, 53, 0.2);
}

.test-details {
  margin-top: 10px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 6px;
  font-size: 12px;
  overflow-x: auto;
}
</style>
