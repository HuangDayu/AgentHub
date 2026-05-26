<template>
  <section class="grid glass-float">
    <div class="page-header">
      <div>
      <h2>大模型配置</h2>
      <p class="muted">管理大模型API配置，用于知识库嵌入和Agent对话。</p>
      </div>
    </div>
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <!-- 创建/编辑大模型配置 -->
      <ModalDialog
      v-model:visible="showCreateForm"
      :title="editingId ? '编辑大模型配置' : '创建大模型配置'"
      @confirm="submitConfig"
      @close="showCreateForm = false"
      :confirm-disabled="loading"
      :confirm-text="editingId ? '更新' : '创建'"
    >
      <form>
        <label class="field">
            <span>名称</span>
            <input v-model="form.name" placeholder="GPT-4 Production" required />
          </label>
          <label class="field">
            <span>类型</span>
            <select v-model="form.type" required :disabled="!!editingId">
              <option value="">选择类型</option>
              <option value="CHAT">对话模型</option>
              <option value="EMBEDDING">嵌入模型</option>
            </select>
          </label>
          <label class="field">
            <span>供应商</span>
            <select v-model="form.supplier" required :disabled="!!editingId">
              <option value="">选择供应商</option>
              <option value="OPENAI">OpenAI</option>
              <option value="DEEPSEEK">DeepSeek</option>
              <option value="OLLAMA">Ollama</option>
              <option value="OPENROUTER">OpenRouter</option>
            </select>
          </label>
          <label class="field">
            <span>模型名称</span>
            <input v-model="form.model" placeholder="gpt-4" required />
          </label>
          <label class="field">
            <span>API Key</span>
            <input v-model="form.apiKey" type="password" placeholder="sk-..." />
          </label>
          <label class="field">
            <span>Base URL（可选）</span>
            <input v-model="form.baseUrl" placeholder="https://api.openai.com/v1" />
          </label>
          <label class="field">
            <span>启用状态</span>
            <select v-model="form.enabled">
              <option :value="true">启用</option>
              <option :value="false">禁用</option>
            </select>
          </label>
      </form>
    </ModalDialog>

      <!-- 配置列表 -->
      <article class="table-card float-effect">

        <table>
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
            <tr v-for="config in configs" :key="config.id">
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
                  <CustomButton type="ghost" @click="startEdit(config)">编辑</CustomButton>
                  <CustomButton type="ghost" @click="testModel(config)">测试模型</CustomButton>
                  <CustomButton type="ghost" @click="handleDelete(config.id)">删除</CustomButton>
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
        <div v-else :class="['test-chunkResult', testResult?.success ? 'test-success' : 'test-error']">
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
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const error = ref('')
const configs = ref<ModelConfig[]>([])
const editingId = ref<string | null>(null)
const testResult = ref<{ success: boolean; message: string; details?: string } | null>(null)
const showTestModal = ref(false)
const testing = ref(false)
const testAbortController = ref<AbortController | null>(null)
const showCreateForm = ref(false)
const form = reactive({
  name: '',
  type: 'CHAT',
  supplier: 'OPENAI',
  model: 'gpt-4',
  apiKey: '',
  baseUrl: '',
  enabled: true,
})

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

onMounted(loadConfigs)

// 监听全局新增事件
onMounted(() => {
  window.addEventListener('global-add', () => {
    showCreateForm.value = true
  })
})
watch(() => [store.tenantId, store.workspaceId], loadConfigs)

async function loadConfigs() {
  if (!selectionReady.value) {
    configs.value = []
    return
  }
  await execute(async () => {
    configs.value = await listModelConfigs({ tenantId: store.tenantId, workspaceId: store.workspaceId })
  })
}

function startEdit(config: ModelConfig) {
  editingId.value = config.id
  form.name = config.name
  form.type = config.type
  form.supplier = config.supplier
  form.model = config.model
  form.apiKey = config.apiKey || ''
  form.baseUrl = config.baseUrl || ''
  form.enabled = config.enabled
  showCreateForm.value = true
}

function cancelEdit() {
  editingId.value = null
  form.name = ''
  form.type = 'CHAT'
  form.supplier = 'OPENAI'
  form.model = 'gpt-4'
  form.apiKey = ''
  form.baseUrl = ''
  form.enabled = true
}

function cancelForm() {
  showCreateForm.value = false
  cancelEdit()
}

async function submitConfig() {
  if (!selectionReady.value) return
  await execute(async () => {
    if (editingId.value) {
      // 更新配置
      await updateModelConfig(
        { tenantId: store.tenantId, workspaceId: store.workspaceId },
        editingId.value,
        {
          name: form.name.trim(),
          type: form.type,
          supplier: form.supplier,
          model: form.model.trim(),
          apiKey: form.apiKey.trim() || undefined,
          baseUrl: form.baseUrl.trim() || undefined,
          enabled: form.enabled,
        }
      )
      editingId.value = null
    } else {
      // 创建配置
      await createModelConfig(
        { tenantId: store.tenantId, workspaceId: store.workspaceId },
        {
          name: form.name.trim(),
          type: form.type,
          supplier: form.supplier,
          model: form.model.trim(),
          apiKey: form.apiKey.trim() || undefined,
          baseUrl: form.baseUrl.trim() || undefined,
          enabled: form.enabled,
        }
      )
    }
    // Reset form
    cancelForm()
    await loadConfigs()
  })
}

async function testModel(config: ModelConfig) {
  if (!selectionReady.value) return
  testResult.value = null
  showTestModal.value = true
  testing.value = true
  testAbortController.value = new AbortController()

  try {
    const chunkResult = await testModelConfig(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      config.id
    )
    testResult.value = chunkResult
  } catch (e) {
    if (e instanceof Error && e.name === 'AbortError') {
      testResult.value = {
        success: false,
        message: '测试已中断',
        details: '用户取消了测试操作',
      }
    } else {
      testResult.value = {
        success: false,
        message: '测试失败',
        details: e instanceof Error ? e.message : '未知错误',
      }
    }
  } finally {
    testing.value = false
    testAbortController.value = null
  }
}

function cancelTest() {
  if (testAbortController.value) {
    testAbortController.value.abort()
  }
}

function closeTestModal() {
  showTestModal.value = false
  if (testing.value) {
    cancelTest()
  }
}

function getDefaultBaseUrl(supplier: string): string {
  switch (supplier) {
    case 'OPENAI':
      return 'https://api.openai.com/v1'
    case 'DEEPSEEK':
      return 'https://api.deepseek.com/v1'
    case 'OLLAMA':
      return 'http://localhost:11434/v1'
    case 'OPENROUTER':
      return 'https://openrouter.ai/api/v1'
    default:
      return ''
  }
}

async function handleDelete(configId: string) {
  if (!selectionReady.value) return
  if (!confirm('确定要删除这个大模型配置吗？')) return
  await execute(async () => {
    await deleteModelConfig({ tenantId: store.tenantId, workspaceId: store.workspaceId }, configId)
    await loadConfigs()
  })
}

async function execute(action: () => Promise<void>) {
  error.value = ''
  try {
    await action()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '请求失败'
  }
}
</script>

<style scoped>
.tag-success {
  background: rgba(34, 197, 94, 0.14);
  color: var(--color-success);
}
.tag-error {
  background: rgba(239, 68, 68, 0.14);
  color: var(--color-error);
}
.test-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
}
.test-loading p {
  margin: 0;
  color: #666;
}
.test-chunkResult {
  padding: 16px;
  border-radius: 8px;
}
.test-success {
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.3);
}
.test-error {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
}
.test-details {
  background: rgba(0, 0, 0, 0.05);
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 0.85rem;
  margin-top: 8px;
}
.loading-spinner {
  display: inline-block;
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
