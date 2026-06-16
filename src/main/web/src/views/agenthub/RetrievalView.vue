<template>
  <section class="grid glass-float">
    <div class="page-header">
      <div>
        <h2>知识检索</h2>
        <p class="muted">选择知识库并输入查询文本，检索相关文档片段。</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <!-- 检索表单 -->
      <article class="panel glass-effect stack float-effect">
        <form class="field-grid" @submit.prevent="handleRetrieve">
          <!-- 第一行：知识库选择 -->
          <label class="field" style="grid-column: 1 / -1">
            <span>知识库</span>
            <CustomSelect v-model="selectedKbId" :options="kbOptions" placeholder="选择知识库" />
          </label>
          <!-- 第二行：返回数量、分数阈值、向量权重 -->
          <label class="field">
            <span>返回数量</span>
            <input v-model.number="topK" type="number" min="1" max="20" placeholder="5" />
          </label>
          <label class="field">
            <span>分数阈值</span>
            <input v-model.number="scoreThreshold" type="number" min="0" max="1" step="0.1" placeholder="0.0" />
          </label>
          <label class="field">
            <span>向量权重</span>
            <input v-model.number="vectorWeight" type="number" min="0" max="1" step="0.1" placeholder="0.7" />
          </label>
          <!-- 第三行：关键词权重、查询改写、重排序 -->
          <label class="field">
            <span>关键词权重</span>
            <input v-model.number="keywordWeight" type="number" min="0" max="1" step="0.1" placeholder="0.3" />
          </label>
          <label class="field">
            <span>启用查询改写</span>
            <CustomSelect v-model="enableQueryRewrite" :options="booleanOptions" />
          </label>
          <label class="field">
            <span>启用重排序</span>
            <CustomSelect v-model="enableRerank" :options="booleanOptions" />
          </label>
          <!-- 第四行：文本搜索、向量搜索 -->
          <label class="field">
            <span>启用文本搜索</span>
            <CustomSelect v-model="enableTextSearch" :options="booleanOptions" />
          </label>
          <label class="field">
            <span>启用向量搜索</span>
            <CustomSelect v-model="enableVectorSearch" :options="booleanOptions" />
          </label>
          <!-- 查询文本 -->
          <label class="field" style="grid-column: 1 / -1">
            <span>查询文本</span>
            <textarea v-model="query" rows="3" placeholder="输入要检索的问题或关键词"></textarea>
          </label>
          <CustomButton type="primary" native-type="submit" :disabled="!selectedKbId || !query.trim() || searching">
            {{ searching ? '检索中...' : '开始检索' }}
          </CustomButton>
          <CustomButton type="secondary" @click="loadKnowledgeBases">刷新知识库</CustomButton>
        </form>
      </article>

      <!-- 检索结果 -->
      <article v-if="rewrittenQuery || chunks.length" class="panel stack float-effect">
        <h3 style="margin: 0">检索结果</h3>
        <p v-if="rewrittenQuery" class="muted">重写查询: {{ rewrittenQuery }}</p>
        <p v-if="chunks.length" class="muted">找到 {{ chunks.length }} 条相关结果</p>
        <div v-for="(chunk, index) in chunks" :key="index" class="chunkResult-card">
          <div class="chunkResult-header">
            <span class="tag">相关度 {{ (chunk.score * 100).toFixed(1) }}%</span>
            <span class="muted">文档 {{ chunk.docId }} · 分块 #{{ chunk.chunkIndex }}</span>
          </div>
          <pre class="chunkResult-content">{{ chunk.content }}</pre>
        </div>
      </article>
      <article v-else-if="searched" class="empty-state">未检索到相关结果，试试其他关键词。</article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { listKnowledgeBases } from '@/api/knowledge-api'
import { retrieve, type RetrievePayload, type RetrievalResponse } from '@/api/retrieval-api'
import type { KnowledgeBase, RetrievalChunk } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const booleanOptions = [
  { value: true, label: '是' },
  { value: false, label: '否' },
]

const store = useWorkspaceStore()
const knowledgeBases = ref<KnowledgeBase[]>([])
const selectedKbId = ref('')
const query = ref('')
const topK = ref(5)
const scoreThreshold = ref(0.0)
const vectorWeight = ref(0.7)
const keywordWeight = ref(0.3)
const enableQueryRewrite = ref(false)
const enableRerank = ref(false)
const enableTextSearch = ref(false)
const enableVectorSearch = ref(true)
const chunks = ref<RetrievalChunk[]>([])
const rewrittenQuery = ref('')
const error = ref('')
const searching = ref(false)
const searched = ref(false)

const kbOptions = computed(() => knowledgeBases.value.map(kb => ({ value: kb.id, label: `${kb.name}（${kb.id}）` })))

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

onMounted(loadKnowledgeBases)
watch(() => [store.tenantId, store.workspaceId], loadKnowledgeBases)

async function loadKnowledgeBases() {
  if (!selectionReady.value) { knowledgeBases.value = []; return }
  try { knowledgeBases.value = await listKnowledgeBases(getSelection()) } catch (reason) { error.value = reason instanceof Error ? reason.message : '加载知识库失败' }
}

function getSelection() {
  return { tenantId: store.tenantId, workspaceId: store.workspaceId }
}

async function handleRetrieve() {
  if (!canRetrieve()) return
  await runRetrieve()
}

function canRetrieve(): boolean {
  return Boolean(selectedKbId.value && query.value.trim())
}

async function runRetrieve(): Promise<void> {
  beginSearch()
  await tryPerformSearch()
}

function beginSearch(): void {
  searching.value = true
  searched.value = true
  error.value = ''
}

async function tryPerformSearch(): Promise<void> {
  try {
    await performSearch()
  } catch (reason) {
    handleSearchError(reason)
  } finally {
    searching.value = false
  }
}

async function performSearch(): Promise<void> {
  const response = await retrieve(buildRetrievePayload())
  applyRetrieveResponse(response)
}

function buildRetrievePayload(): RetrievePayload {
  const payload = createRetrieveContext()
  applyRetrieveWeights(payload)
  applyRetrieveFeatures(payload)
  return payload
}

function createRetrieveContext(): RetrievePayload {
  return {
    selection: { tenantId: store.tenantId, workspaceId: store.workspaceId },
    kbId: selectedKbId.value,
    query: query.value.trim(),
  }
}

function applyRetrieveWeights(payload: RetrievePayload): void {
  payload.topK = topK.value
  payload.scoreThreshold = scoreThreshold.value
  payload.vectorWeight = vectorWeight.value
  payload.keywordWeight = keywordWeight.value
}

function applyRetrieveFeatures(payload: RetrievePayload): void {
  payload.enableQueryRewrite = enableQueryRewrite.value
  payload.enableRerank = enableRerank.value
  payload.enableTextSearch = enableTextSearch.value
  payload.enableVectorSearch = enableVectorSearch.value
}

function applyRetrieveResponse(response: RetrievalResponse): void {
  rewrittenQuery.value = response.rewrittenQuery
  chunks.value = response.chunks
}

function handleSearchError(reason: unknown): void {
  error.value = reason instanceof Error ? reason.message : '检索失败'
  chunks.value = []
}
</script>

<style scoped>
.field-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field span {
  font-weight: 500;
  color: var(--color-primary-dark);
}

.field input,
.field select,
.field textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid var(--color-border-strong);
  border-radius: 4px;
  font-size: 0.9rem;
}

.chunkResult-card {
  background: var(--bg-card-solid);
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 16px;
  display: grid;
  gap: 8px;
}

.chunkResult-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.chunkResult-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 0.95em;
  line-height: 1.6;
  color: var(--color-text);
}
</style>
