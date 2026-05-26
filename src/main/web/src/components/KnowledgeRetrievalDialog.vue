<template>
  <ModalDialog
    :visible="visible"
    @update:visible="emit('update:visible', $event)"
    :title="`知识检索 · ${kbName}`"
    @close="handleClose"
    size="xlarge"
    :show-footer="false"
  >
    <div class="retrieval-content">
      <!-- 检索表单 -->
      <div class="retrieval-form">
        <form class="field-grid" @submit.prevent="handleRetrieve">
          <!-- 第一行：返回数量、分数阈值 -->
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
          <!-- 第二行：关键词权重、查询改写、重排序 -->
          <label class="field">
            <span>关键词权重</span>
            <input v-model.number="keywordWeight" type="number" min="0" max="1" step="0.1" placeholder="0.3" />
          </label>
          <label class="field">
            <span>启用查询改写</span>
            <select v-model="enableQueryRewrite">
              <option :value="true">是</option>
              <option :value="false">否</option>
            </select>
          </label>
          <label class="field">
            <span>启用重排序</span>
            <select v-model="enableRerank">
              <option :value="true">是</option>
              <option :value="false">否</option>
            </select>
          </label>
          <!-- 第三行：文本搜索、向量搜索 -->
          <label class="field">
            <span>启用文本搜索</span>
            <select v-model="enableTextSearch">
              <option :value="true">是</option>
              <option :value="false">否</option>
            </select>
          </label>
          <label class="field">
            <span>启用向量搜索</span>
            <select v-model="enableVectorSearch">
              <option :value="true">是</option>
              <option :value="false">否</option>
            </select>
          </label>
          <!-- 查询文本 -->
          <label class="field" style="grid-column: 1 / -1">
            <span>查询文本</span>
            <textarea v-model="query" rows="3" placeholder="输入要检索的问题或关键词"></textarea>
          </label>
          <CustomButton type="primary" native-type="submit" :disabled="!query.trim() || searching">
            {{ searching ? '检索中...' : '开始检索' }}
          </CustomButton>
        </form>
      </div>

      <!-- 检索结果 -->
      <div v-if="error" class="error-message">
        <p style="color: var(--color-error); font-weight: 500;">{{ error }}</p>
      </div>
      <div v-else-if="rewrittenQuery || chunks.length" class="retrieval-results">
        <h3 style="margin: 0 0 16px 0">检索结果</h3>
        <p v-if="rewrittenQuery" class="muted">重写查询: {{ rewrittenQuery }}</p>
        <p v-if="chunks.length" class="muted">找到 {{ chunks.length }} 条相关结果</p>
        <div v-for="(chunk, index) in chunks" :key="index" class="chunk-result-card">
          <div class="chunk-result-header">
            <span class="tag">相关度 {{ (chunk.score * 100).toFixed(1) }}%</span>
            <span class="muted">文档 {{ chunk.docId }} · 分块 #{{ chunk.chunkIndex }}</span>
          </div>
          <pre class="chunk-result-content">{{ chunk.content }}</pre>
        </div>
      </div>
      <div v-else-if="searched" class="empty-state">未检索到相关结果，试试其他关键词。</div>
    </div>
  </ModalDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { retrieve } from '@/api/retrieval-api'
import type { RetrievalChunk } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'

const props = defineProps<{
  visible: boolean
  kbId: string
  kbName: string
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const store = useWorkspaceStore()

const query = ref('')
const topK = ref(5)
const scoreThreshold = ref(0.0)
const vectorWeight = ref(0.7)
const keywordWeight = ref(0.3)
const enableQueryRewrite = ref(true)
const enableRerank = ref(false)
const enableTextSearch = ref(true)
const enableVectorSearch = ref(true)
const rewrittenQuery = ref('')
const chunks = ref<RetrievalChunk[]>([])
const error = ref('')
const searching = ref(false)
const searched = ref(false)

// 监听 visible 变化，重置状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 打开弹窗时重置状态
    query.value = ''
    rewrittenQuery.value = ''
    chunks.value = []
    error.value = ''
    searched.value = false
  }
})

async function handleRetrieve() {
  console.log('handleRetrieve called', {
    kbId: props.kbId,
    query: query.value.trim(),
    tenantId: store.tenantId,
    workspaceId: store.workspaceId
  })

  if (!props.kbId || !query.value.trim()) {
    console.log('Early return: missing kbId or query')
    return
  }

  searching.value = true
  searched.value = true
  error.value = ''
  try {
    console.log('Calling retrieve API...')
    const response = await retrieve({
      selection: { tenantId: store.tenantId, workspaceId: store.workspaceId },
      kbId: props.kbId,
      query: query.value.trim(),
      topK: topK.value,
      scoreThreshold: scoreThreshold.value,
      enableQueryRewrite: enableQueryRewrite.value,
      enableRerank: enableRerank.value,
      enableTextSearch: enableTextSearch.value,
      enableVectorSearch: enableVectorSearch.value,
      vectorWeight: vectorWeight.value,
      keywordWeight: keywordWeight.value,
    })
    console.log('Retrieve response:', response)
    rewrittenQuery.value = response.rewrittenQuery
    chunks.value = response.chunks
  } catch (reason) {
    console.error('Retrieve error:', reason)
    error.value = reason instanceof Error ? reason.message : '检索失败'
    chunks.value = []
  } finally {
    searching.value = false
  }
}

function handleClose() {
  emit('update:visible', false)
}
</script>

<style scoped>
.retrieval-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.retrieval-form {
  background: var(--bg-card-hover);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid transparent;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

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
  padding: 0.5rem;
  border: 1px solid var(--color-border-strong);
  border-radius: 8px;
  font: inherit;
  font-size: 0.9rem;
  transition: border-color 0.2s ease;
}

.field input:focus,
.field select:focus,
.field textarea:focus {
  outline: none;
  border-color: var(--color-primary);
}

.field textarea {
  resize: vertical;
  min-height: 80px;
}

.retrieval-results {
  background: var(--bg-card-hover);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid transparent;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.chunk-result-card {
  margin-top: 16px;
  padding: 16px;
  background: rgba(58, 138, 214, 0.05);
  border-radius: 12px;
  border: 1px solid var(--color-primary-subtle);
}

.chunk-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.tag {
  display: inline-block;
  padding: 4px 12px;
  background: var(--color-primary-subtle);
  color: var(--color-primary);
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
}

.muted {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.chunk-result-content {
  margin: 0;
  padding: 12px;
  background: var(--bg-card-solid);
  border-radius: 8px;
  font-family: inherit;
  font-size: 0.9rem;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  color: var(--color-primary-dark);
}

.empty-state {
  text-align: center;
  padding: 48px 24px;
  color: var(--color-text-muted);
  font-size: 0.95rem;
}
</style>
