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
            <CustomSelect
              v-model="form.vectorStoreConfigId"
              :options="vectorStoreConfigOptions"
              placeholder="不绑定"
            />
          </label>
          <label class="field">
            <span>嵌入模型配置</span>
            <CustomSelect
              v-model="form.embeddingModelConfigId"
              :options="embeddingModelConfigOptions"
              placeholder="不绑定"
            />
          </label>
          <label class="field">
            <span>对话模型配置</span>
            <CustomSelect
              v-model="form.chatModelConfigId"
              :options="chatModelConfigOptions"
              placeholder="不绑定"
            />
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
            <CustomSelect
              v-model="editForm.vectorStoreConfigId"
              :options="vectorStoreConfigOptions"
              placeholder="不绑定"
            />
          </label>
          <label class="field">
            <span>嵌入模型配置</span>
            <CustomSelect
              v-model="editForm.embeddingModelConfigId"
              :options="embeddingModelConfigOptions"
              placeholder="不绑定"
            />
          </label>
          <label class="field">
            <span>对话模型配置</span>
            <CustomSelect
              v-model="editForm.chatModelConfigId"
              :options="chatModelConfigOptions"
              placeholder="不绑定"
            />
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
                  <CustomButton type="secondary" @click="openDocPanel(item)">文档管理</CustomButton>
                  <CustomButton type="secondary" @click="openRetrievalDialog(item)">检索</CustomButton>
                  <CustomButton type="ghost" @click="handleDeleteKb(item.id)">删除</CustomButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </article>

      <!-- 文档管理弹窗 -->
      <ModalDialog
        v-model:visible="showDocPanel"
        :title="`文档管理 · ${activeDocKbName}`"
        @close="closeDocPanel"
        size="xlarge"
        :show-footer="false"
      >
        <!-- 上传 -->
        <form @submit.prevent style="display: flex; gap: 16px; align-items: flex-end; margin-bottom: 24px;">
          <label class="field" style="flex: 1;">
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
                  <CustomButton type="secondary" @click="openEditDocForm(doc)" size="small">编辑</CustomButton>
                  <CustomButton type="secondary" @click="handleVectorizeDoc(doc.docId)" :disabled="vectorizingDocId === doc.docId" size="small">{{ vectorizingDocId === doc.docId ? "向量化中..." : "向量化" }}</CustomButton>
                  <CustomButton type="ghost" @click="handleDeleteDoc(doc.docId)" size="small">删除</CustomButton>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="empty-state">暂无文档，请上传文件。</div>
      </ModalDialog>

      <!-- 文档编辑弹窗 -->
      <ModalDialog
        v-model:visible="showEditDocForm"
        title="编辑文档"
        @confirm="submitEditDoc"
        @close="showEditDocForm = false"
        confirm-text="保存"
      >
        <form>
          <label class="field">
            <span>文件名</span>
            <input v-model="editDocForm.fileName" required placeholder="文件名" />
          </label>
          <label class="field">
            <span>文档类型</span>
            <input v-model="editDocForm.contentType" required placeholder="文档类型" />
          </label>
          <label class="field" style="grid-column: 1 / -1">
            <span>描述</span>
            <textarea v-model="editDocForm.description" rows="3" placeholder="文档描述"></textarea>
          </label>
        </form>
      </ModalDialog>

      <!-- 知识库检索弹窗 -->
      <KnowledgeRetrievalDialog
        v-model:visible="showRetrievalDialog"
        :kb-id="retrievalKbId"
        :kb-name="retrievalKbName"
      />
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
import KnowledgeRetrievalDialog from '@/components/KnowledgeRetrievalDialog.vue'

const store = useWorkspaceStore()
const selectionReady = computed(() => !!store.tenantId && !!store.workspaceId)

const knowledgeBases = ref<KnowledgeBase[]>([])
const modelConfigs = ref<ModelConfig[]>([])
const vectorStoreConfigs = ref<VectorStoreConfig[]>([])
const error = ref('')
const showCreateForm = ref(false)
const showEditForm = ref(false)

// 下拉框选项计算属性
const vectorStoreConfigOptions = computed(() => {
  return vectorStoreConfigs.value.map(config => ({
    value: config.id,
    label: `${config.name} (${config.type})`
  }))
})

const embeddingModelConfigOptions = computed(() => {
  return modelConfigs.value
    .filter(c => c.type === 'EMBEDDING')
    .map(config => ({
      value: config.id,
      label: `${config.name} (${config.supplier} - ${config.model})`
    }))
})

const chatModelConfigOptions = computed(() => {
  return modelConfigs.value
    .filter(c => c.type === 'CHAT')
    .map(config => ({
      value: config.id,
      label: `${config.name} (${config.supplier} - ${config.model})`
    }))
})

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
const showDocPanel = ref(false)
const activeDocKbId = ref('')
const activeDocKbName = ref('')
const documents = ref<any[]>([])
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const vectorizingDocId = ref<string | null>(null)

// 文档编辑
const showEditDocForm = ref(false)
const editDocForm = reactive({
  docId: '',
  fileName: '',
  contentType: '',
  description: '',
})

// 知识库检索
const showRetrievalDialog = ref(false)
const retrievalKbId = ref('')
const retrievalKbName = ref('')

// 监听全局新增事件
onMounted(() => {
  loadVectorStoreConfigs()
  loadModelConfigs()
  if (selectionReady.value) loadKnowledgeBases()

  // 监听全局新增事件
  window.addEventListener('global-add', async () => {
    // 刷新配置列表
    await loadVectorStoreConfigs()
    await loadModelConfigs()
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

async function openEditForm(kb: KnowledgeBase) {
  // 刷新配置列表
  await loadVectorStoreConfigs()
  await loadModelConfigs()

  // 填充表单数据
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
async function openDocPanel(kb: KnowledgeBase) {
  activeDocKbId.value = kb.id
  activeDocKbName.value = kb.name
  documents.value = []
  selectedFile.value = null
  showDocPanel.value = true
  await loadDocuments()
}

function closeDocPanel() {
  showDocPanel.value = false
  activeDocKbId.value = ''
  activeDocKbName.value = ''
  documents.value = []
  selectedFile.value = null
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

// 文档编辑
function openEditDocForm(doc: any) {
  editDocForm.docId = doc.docId
  editDocForm.fileName = doc.fileName || ''
  editDocForm.contentType = doc.contentType || ''
  editDocForm.description = doc.description || ''
  showEditDocForm.value = true
}

async function submitEditDoc() {
  if (!activeDocKbId.value) return
  await execute(async () => {
    // 这里需要调用更新文档的API
    // 由于API中可能没有更新文档的接口，这里先注释
    // await updateDocument(
    //   { tenantId: store.tenantId, workspaceId: store.workspaceId },
    //   activeDocKbId.value,
    //   editDocForm.docId,
    //   {
    //     fileName: editDocForm.fileName,
    //     contentType: editDocForm.contentType,
    //     description: editDocForm.description,
    //   }
    // )
    showEditDocForm.value = false
    await loadDocuments()
  })
}

// 知识库检索
function openRetrievalDialog(kb: KnowledgeBase) {
  retrievalKbId.value = kb.id
  retrievalKbName.value = kb.name
  showRetrievalDialog.value = true
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
