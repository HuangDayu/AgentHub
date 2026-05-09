<template>
  <section class="grid glass-float">
    <div class="page-header">
      <div>
        <h2>知识库</h2>
        <p class="muted">基于当前租户/工作区上下文管理知识库与文档。</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <!-- 创建知识库 -->
      <ModalDialog
      v-model:visible="showCreateForm"
      title="创建知识库"
      @confirm="submitKnowledgeBase"
      @close="showCreateForm = false"
      :confirm-disabled="loading"
      confirm-text="创建"
    >
      <form>
        <label class="field">
            <span>名称</span>
            <input v-model="form.name" required placeholder="知识库名称" />
          </label>
          <label class="field">
            <span>编码</span>
            <input v-model="form.kbCode" required placeholder="唯一标识" />
          </label>
          <label class="field">
            <span>向量数据库配置</span>
            <select v-model="form.vectorStoreConfigId">
              <option value="">不绑定</option>
              <option v-for="config in vectorStoreConfigs" :key="config.id" :value="config.id">
                {{ config.name }} ({{ config.type }})
              </option>
            </select>
          </label>
          <label class="field">
            <span>嵌入模型配置</span>
            <select v-model="form.embeddingModelConfigId">
              <option value="">不绑定</option>
              <option v-for="config in modelConfigs.filter(c => c.type === 'EMBEDDING')" :key="config.id" :value="config.id">
                {{ config.name }} ({{ config.supplier }} - {{ config.model }})
              </option>
            </select>
          </label>
          <label class="field">
            <span>对话模型配置</span>
            <select v-model="form.chatModelConfigId">
              <option value="">不绑定</option>
              <option v-for="config in modelConfigs.filter(c => c.type === 'CHAT')" :key="config.id" :value="config.id">
                {{ config.name }} ({{ config.supplier }} - {{ config.model }})
              </option>
            </select>
          </label>
          <label class="field" style="grid-column: 1 / -1">
            <span>描述</span>
            <textarea v-model="form.description" rows="3" placeholder="描述该知识库的数据范围与检索策略"></textarea>
          </label>
          
          <CustomButton type="secondary" @click="loadKnowledgeBases">刷新列表</CustomButton>
      </form>
    </ModalDialog>

      <!-- 编辑知识库弹窗 -->
      <ModalDialog
        v-model:visible="showEditForm"
        title="编辑知识库"
        @confirm="submitEditKnowledgeBase"
        @close="showEditForm = false"
        :confirm-disabled="loading"
        confirm-text="保存"
      >
        <form>
          <label class="field">
            <span>名称</span>
            <input v-model="editForm.name" required placeholder="知识库名称" />
          </label>
          <label class="field">
            <span>向量数据库配置</span>
            <select v-model="editForm.vectorStoreConfigId">
              <option value="">不绑定</option>
              <option v-for="config in vectorStoreConfigs" :key="config.id" :value="config.id">
                {{ config.name }} ({{ config.type }})
              </option>
            </select>
          </label>
          <label class="field">
            <span>嵌入模型配置</span>
            <select v-model="editForm.embeddingModelConfigId">
              <option value="">不绑定</option>
              <option v-for="config in modelConfigs.filter(c => c.type === 'EMBEDDING')" :key="config.id" :value="config.id">
                {{ config.name }} ({{ config.supplier }} - {{ config.model }})
              </option>
            </select>
          </label>
          <label class="field">
            <span>对话模型配置</span>
            <select v-model="editForm.chatModelConfigId">
              <option value="">不绑定</option>
              <option v-for="config in modelConfigs.filter(c => c.type === 'CHAT')" :key="config.id" :value="config.id">
                {{ config.name }} ({{ config.supplier }} - {{ config.model }})
              </option>
            </select>
          </label>
          <label class="field" style="grid-column: 1 / -1">
            <span>描述</span>
            <textarea v-model="editForm.description" rows="3" placeholder="描述该知识库的数据范围与检索策略"></textarea>
          </label>
        </form>
      </ModalDialog>

      <!-- 知识库列表 -->
      <article class="table-card">
        <table>
          <thead>
            <tr>
              <th>知识库</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in knowledgeBases" :key="item.id">
              <td>
                <strong>{{ item.name }}</strong>
                <div class="muted">{{ item.id }} · {{ item.description || '无描述' }}</div>
              </td>
              <td>{{ formatDateTime(item.createdAt) }}</td>
              <td>
                <div class="chip-row">
                  <CustomButton type="secondary" @click="openEditForm(item)">编辑</CustomButton>
                  <CustomButton type="secondary" @click="openDocPanel(item.id)">文档管理</CustomButton>
                  <CustomButton type="ghost" @click="handleDeleteKb(item.id)">删除</CustomButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </article>

      <!-- 文档管理面板 -->
      <article v-if="activeDocKbId" class="panel stack">
        <div class="page-header">
          <div>
            <h3 style="margin: 0">文档管理 · {{ activeDocKbId }}</h3>
            <p class="muted">上传文档到知识库，查看已有文档列表。</p>
          </div>
          <CustomButton type="ghost" @click="activeDocKbId = ''">关闭</CustomButton>
        </div>
        <!-- 上传 -->
        <form class="field-grid" @submit.prevent>
          <label class="field" style="grid-column: 1 / -1">
            <span>选择文件上传</span>
            <input type="file" @change="onFileSelected" />
          </label>
          <button
            class="primary"
            type="button"
            :disabled="!selectedFile || uploading"
            @click="handleUpload"
          >
            {{ uploading ? '上传中...' : '上传文档' }}
          </button>
        </form>
        <!-- 文档列表 -->
        <div v-if="documents && documents.length" class="table-card">
          <table>
            <thead>
              <tr>
                <th>文件名</th>
                <th>类型</th>
                <th>大小</th>
                <th>状态</th>
                <th>上传时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="doc in documents" :key="doc.docId">
                <td>{{ doc.fileName }}</td>
                <td>{{ doc.contentType }}</td>
                <td>{{ formatSize(doc.size) }}</td>
                <td><span class="tag">{{ doc.status }}</span></td>
                <td>{{ formatDateTime(doc.createdAt) }}</td>
                <td>
                  <CustomButton type="secondary" @click="handleVectorizeDoc(doc.docId)" :disabled="vectorizingDocId === doc.docId">{{ vectorizingDocId === doc.docId ? "向量化中..." : "向量化" }}</CustomButton>
                  <CustomButton type="ghost" @click="handleDeleteDoc(doc.docId)">删除</CustomButton>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="empty-state">暂无文档，请上传文件。</div>
      </article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  createKnowledgeBase,
  deleteDocument,
  deleteKnowledgeBase,
  listDocuments,
  listKnowledgeBases,
  updateKnowledgeBase,
  uploadDocument,
  vectorizeDocument,
} from '@/api/knowledge-api'
import { listModelConfigs, listVectorStoreConfigs } from '@/api/config-api'
import type { KnowledgeBase, ModelConfig, VectorStoreConfig } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const selectionReady = computed(() => !!store.tenantId && !!store.workspaceId)

const knowledgeBases = ref<KnowledgeBase[]>([])
const modelConfigs = ref<ModelConfig[]>([])
const vectorStoreConfigs = ref<VectorStoreConfig[]>([])
const error = ref('')
const showCreateForm = ref(false)
const showEditForm = ref(false)

const form = reactive({
  name: '',
  kbCode: '',
  description: '',
  vectorStoreConfigId: '',
  embeddingModelConfigId: '',
  chatModelConfigId: '',
})

const editForm = reactive({
  kbId: '',
  name: '',
  description: '',
  vectorStoreConfigId: '',
  embeddingModelConfigId: '',
  chatModelConfigId: '',
})

// 文档管理
const activeDocKbId = ref('')
const documents = ref<any[]>([])
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const vectorizingDocId = ref<string | null>(null)

// 监听全局新增事件
onMounted(() => {
  loadVectorStoreConfigs()
  loadModelConfigs()
  if (selectionReady.value) loadKnowledgeBases()
  
  // 监听全局新增事件
  window.addEventListener('global-add', () => {
    showCreateForm.value = true
  })
})

watch(selectionReady, (ready) => {
  if (ready) loadKnowledgeBases()
})

// 监听workspaceId变化，重新加载数据
watch(() => store.workspaceId, (newId, oldId) => {
  if (newId && oldId && newId !== oldId) {
    loadKnowledgeBases()
  }
})

async function loadModelConfigs() {
  if (selectionReady.value) loadKnowledgeBases()
  try {
    modelConfigs.value = await listModelConfigs({ tenantId: store.tenantId, workspaceId: store.workspaceId })
  } catch { /* ignore */ }
}

async function loadVectorStoreConfigs() {
  loadModelConfigs()
  if (selectionReady.value) loadKnowledgeBases()
  try {
    vectorStoreConfigs.value = await listVectorStoreConfigs({ tenantId: store.tenantId, workspaceId: store.workspaceId })
  } catch { /* ignore */ }
}

async function loadKnowledgeBases() {
  await execute(async () => {
    knowledgeBases.value = await listKnowledgeBases({ tenantId: store.tenantId, workspaceId: store.workspaceId })
  })
}

async function submitKnowledgeBase() {
  await execute(async () => {
    await createKnowledgeBase({
      selection: { tenantId: store.tenantId, workspaceId: store.workspaceId },
      kbCode: form.kbCode,
      name: form.name,
      description: form.description,
      indexVersions: ['v1'],
      activeIndexVersion: 'v1',
      vectorStoreConfigId: form.vectorStoreConfigId || undefined,
      embeddingModelConfigId: form.embeddingModelConfigId || undefined,
      chatModelConfigId: form.chatModelConfigId || undefined,
    })
    showCreateForm.value = false
    resetForm()
    await loadKnowledgeBases()
  })
}

function resetForm() {
  form.name = ''
  form.kbCode = ''
  form.description = ''
  form.vectorStoreConfigId = ''
  form.embeddingModelConfigId = ''
  form.chatModelConfigId = ''
}

function openEditForm(kb: KnowledgeBase) {
  editForm.kbId = kb.id
  editForm.name = kb.name
  editForm.description = kb.description || ''
  editForm.vectorStoreConfigId = kb.vectorStoreConfigId || ''
  editForm.embeddingModelConfigId = kb.embeddingModelConfigId || ''
  editForm.chatModelConfigId = kb.chatModelConfigId || ''
  showEditForm.value = true
}

async function submitEditKnowledgeBase() {
  await execute(async () => {
    await updateKnowledgeBase({
      selection: { tenantId: store.tenantId, workspaceId: store.workspaceId },
      kbId: editForm.kbId,
      name: editForm.name,
      description: editForm.description,
      vectorStoreConfigId: editForm.vectorStoreConfigId || undefined,
      embeddingModelConfigId: editForm.embeddingModelConfigId || undefined,
      chatModelConfigId: editForm.chatModelConfigId || undefined,
    })
    showEditForm.value = false
    await loadKnowledgeBases()
  })
}

async function handleDeleteKb(kbId: string) {
  if (!confirm('确定要删除该知识库吗？此操作不可恢复。')) return
  await execute(async () => {
    await deleteKnowledgeBase({ tenantId: store.tenantId, workspaceId: store.workspaceId }, kbId)
    await loadKnowledgeBases()
  })
}

// 文档管理
async function openDocPanel(kbId: string) {
  activeDocKbId.value = kbId
  documents.value = []
  selectedFile.value = null
  await loadDocuments()
}

function onFileSelected(event: Event) {
  const input = event.target as HTMLInputElement
  selectedFile.value = input.files?.[0] ?? null
}

async function loadDocuments() {
  if (!activeDocKbId.value) return
  await execute(async () => {
    documents.value = await listDocuments(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      activeDocKbId.value,
    )
  })
}

async function handleUpload() {
  if (!selectedFile.value || !activeDocKbId.value) return
  uploading.value = true
  try {
    await uploadDocument(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      activeDocKbId.value,
      selectedFile.value,
    )
    selectedFile.value = null
    await loadDocuments()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '上传失败'
  } finally {
    uploading.value = false
  }
}

async function handleVectorizeDoc(docId: string) {
  if (!activeDocKbId.value) return
  vectorizingDocId.value = docId
  try {
    await vectorizeDocument(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      activeDocKbId.value,
      docId,
    )
    await loadDocuments()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '向量化失败'
  } finally {
    vectorizingDocId.value = null
  }
}

async function handleDeleteDoc(docId: string) {
  if (!activeDocKbId.value) return
  await execute(async () => {
    await deleteDocument(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      activeDocKbId.value,
      docId,
    )
    await loadDocuments()
  })
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('zh-CN')
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
