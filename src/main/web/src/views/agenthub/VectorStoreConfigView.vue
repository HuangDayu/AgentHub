<template>
  <section class="grid glass-float">
    <div class="page-header">
      <div>
        <h2>向量数据库</h2>
        <p class="muted">管理向量数据库连接配置，用于知识库的向量存储。</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <!-- 创建/编辑向量数据库配置 -->
      <ModalDialog
        v-model:visible="showCreateForm"
        :title="editingId ? '编辑向量数据库配置' : '创建向量数据库配置'"
        @confirm="submitConfig"
        @close="showCreateForm = false"
        :confirm-disabled="loading"
        :confirm-text="editingId ? '更新' : '创建'"
      >
        <form>
          <label class="field">
            <span>名称</span>
            <input v-model="form.name" placeholder="Qdrant Production" required />
          </label>
          <label class="field">
            <span>类型</span>
            <select v-model="form.type" required :disabled="!!editingId">
              <option value="">选择类型</option>
              <option value="QDRANT">Qdrant</option>
              <option value="CHROMA">Chroma</option>
              <option value="MILVUS">Milvus</option>
              <option value="WEAVIATE">Weaviate</option>
              <option value="PINECONE">Pinecone</option>
            </select>
          </label>
          <label class="field">
            <span>主机地址</span>
            <input v-model="form.host" placeholder="localhost" required />
          </label>
          <label class="field">
            <span>端口</span>
            <input v-model.number="form.port" type="number" placeholder="6333" required />
          </label>
          <label class="field">
            <span>API Key（可选）</span>
            <input v-model="form.apiKey" type="password" placeholder="API密钥" />
          </label>
          <label class="field">
            <span>集合名称</span>
            <input v-model="form.collectionName" placeholder="knowledge_vectors" required />
          </label>
          <label class="field" style="grid-column: 1 / -1">
            <span>额外参数（JSON格式，可选）</span>
            <textarea v-model="form.extraParams" rows="2" placeholder='{"timeout": 5000}'></textarea>
          </label>
          <label class="field" v-if="editingId">
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
              <th>地址</th>
              <th>集合</th>
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
              <td>{{ config.host }}:{{ config.port }}</td>
              <td>{{ config.collectionName }}</td>
              <td>
                <span :class="['tag', config.enabled ? 'tag-success' : 'tag-error']">
                  {{ config.enabled ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDateTime(config.createdAt) }}</td>
              <td>
                <div class="chip-row">
                  <CustomButton type="ghost" @click="startEdit(config)">编辑</CustomButton>
                  <CustomButton type="ghost" @click="testConnection(config)">测试连接</CustomButton>
                  <CustomButton type="ghost" @click="refreshInstance(config)">刷新实例</CustomButton>
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
        :title="testing ? '正在测试连接...' : '测试结果'"
        :show-footer="!testing"
        :show-close="!testing"
        @close="closeTestModal"
      >
        <div v-if="testing" class="test-loading">
          <div class="loading-spinner"></div>
          <p>正在测试向量数据库连接，请稍候...</p>
          <CustomButton type="secondary" @click="cancelTest">中断测试</CustomButton>
        </div>
        <div v-else :class="['test-chunkResult', testResult?.success ? 'test-success' : 'test-error']">
          <p><strong>状态：</strong>{{ testResult?.success ? '成功' : '失败' }}</p>
          <p><strong>消息：</strong>{{ testResult?.message }}</p>
          <p v-if="testResult?.details"><strong>详情：</strong>{{ testResult?.details }}</p>
        </div>
      </ModalDialog>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { showConfirm } from '@/utils/confirm'
import ModalDialog from '@/components/ModalDialog.vue'
import {
  listVectorStoreConfigs,
  createVectorStoreConfig,
  updateVectorStoreConfig,
  deleteVectorStoreConfig,
  testVectorStoreConfig,
  refreshVectorStoreInstance,
} from '@/api/config-api'
import { formatDateTime } from '@/common/format'
import type { VectorStoreConfig } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const error = ref('')
const configs = ref<VectorStoreConfig[]>([])
const editingId = ref<string | null>(null)
const testResult = ref<{ success: boolean; message: string; details?: string } | null>(null)
const showTestModal = ref(false)
const testing = ref(false)
const testAbortController = ref<AbortController | null>(null)
const showCreateForm = ref(false)
const form = reactive({
  name: '',
  type: 'QDRANT',
  host: 'localhost',
  port: 6333,
  apiKey: '',
  collectionName: 'knowledge_vectors',
  extraParams: '',
  enabled: true,
})

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

onMounted(loadConfigs)

// 监听全局新增事件
onMounted(() => {
  window.addEventListener('global-add', () => {
    editingId.value = null
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
    configs.value = await listVectorStoreConfigs({ tenantId: store.tenantId, workspaceId: store.workspaceId })
  })
}

function startEdit(config: VectorStoreConfig) {
  editingId.value = config.id
  form.name = config.name
  form.type = config.type
  form.host = config.host
  form.port = config.port
  form.apiKey = config.apiKey || ''
  form.collectionName = config.collectionName
  form.extraParams = config.extraParams || ''
  form.enabled = config.enabled
  showCreateForm.value = true
}

function cancelEdit() {
  editingId.value = null
      showCreateForm.value = false
  form.name = ''
  form.type = 'QDRANT'
  form.host = 'localhost'
  form.port = 6333
  form.apiKey = ''
  form.collectionName = 'knowledge_vectors'
  form.extraParams = ''
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
      await updateVectorStoreConfig(
        { tenantId: store.tenantId, workspaceId: store.workspaceId },
        editingId.value,
        {
          name: form.name.trim(),
          host: form.host.trim(),
          port: form.port,
          apiKey: form.apiKey.trim() || undefined,
          collectionName: form.collectionName.trim(),
          extraParams: form.extraParams.trim() || undefined,
          enabled: form.enabled,
        }
      )
      showCreateForm.value = false
      editingId.value = null
      showCreateForm.value = false
    } else {
      // 创建配置
      await createVectorStoreConfig(
        { tenantId: store.tenantId, workspaceId: store.workspaceId },
        {
          name: form.name.trim(),
          type: form.type,
          host: form.host.trim(),
          port: form.port,
          apiKey: form.apiKey.trim() || undefined,
          collectionName: form.collectionName.trim(),
          extraParams: form.extraParams.trim() || undefined,
        }
      )
      showCreateForm.value = false
    }
    // Reset form
    cancelForm()
    await loadConfigs()
  })
}

async function testConnection(config: VectorStoreConfig) {
  if (!selectionReady.value) return
  testResult.value = null
  showTestModal.value = true
  testing.value = true
  testAbortController.value = new AbortController()

  try {
    const chunkResult = await testVectorStoreConfig(
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

async function refreshInstance(config: VectorStoreConfig) {
  if (!selectionReady.value) return
  await execute(async () => {
    await refreshVectorStoreInstance(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      config.id
    )
    error.value = '实例已刷新'
    setTimeout(() => (error.value = ''), 3000)
  })
}

async function handleDelete(configId: string) {
  if (!selectionReady.value) return
  if (!await showConfirm('确定要删除这个向量数据库配置吗？')) return
  await execute(async () => {
    await deleteVectorStoreConfig({ tenantId: store.tenantId, workspaceId: store.workspaceId }, configId)
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
