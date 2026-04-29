<template>
  <section class="grid">
    <div class="page-header">
      <div>
        <h2>知识检索</h2>
        <p class="muted">在知识库中搜索你需要的文档和信息。</p>
      </div>
    </div>

    <article class="panel search-panel">
      <form class="search-form" @submit.prevent="executeSearch">
        <label class="field">
          <span>知识库</span>
          <select v-model="selectedKbId">
            <option value="" disabled>请选择知识库</option>
            <option v-for="kb in knowledgeBases" :key="kb.id" :value="kb.id">
              {{ kb.name }}
            </option>
          </select>
        </label>
        <label class="field search-field">
          <span>搜索内容</span>
          <div class="search-input-row">
            <input
              v-model="query"
              placeholder="输入关键词或问题..."
              @keydown.enter.prevent="executeSearch"
            />
            <button class="primary" type="submit" :disabled="searching || !selectedKbId || !query.trim()">
              {{ searching ? '搜索中...' : '搜索' }}
            </button>
          </div>
        </label>
      </form>
    </article>

    <p v-if="error" class="status">{{ error }}</p>

    <template v-if="results.length > 0">
      <div class="chunkResult-summary muted">
        找到 {{ results.length }} 条结果，共 {{ totalResults }} 条匹配
      </div>
      <article v-for="chunkResult in results" :key="chunkResult.id" class="panel chunkResult-card">
        <div class="chunkResult-header">
          <strong>{{ chunkResult.documentName ?? '未知文档' }}</strong>
          <span class="tag">相关度 {{ formatScore(chunkResult.score) }}</span>
        </div>
        <div class="chunkResult-content" v-html="highlightContent(chunkResult.content)"></div>
        <div v-if="chunkResult.source" class="chunkResult-source muted">
          来源：{{ chunkResult.source }}
        </div>
      </article>
    </template>

    <article v-else-if="searched && results.length === 0" class="empty-state">
      <p>没有找到相关结果，请尝试更换关键词或知识库。</p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  listKnowledgeBases,
  searchKnowledge,
  type KnowledgeBase,
  type RetrievalResult,
} from '@/api/user-retrieval-api'

const knowledgeBases = ref<KnowledgeBase[]>([])
const selectedKbId = ref('')
const query = ref('')
const results = ref<RetrievalResult[]>([])
const totalResults = ref(0)
const searching = ref(false)
const searched = ref(false)
const error = ref('')

onMounted(async () => {
  try {
    knowledgeBases.value = await listKnowledgeBases()
    if (knowledgeBases.value.length > 0) {
      selectedKbId.value = knowledgeBases.value[0].id
    }
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载知识库失败'
  }
})

async function executeSearch() {
  if (!selectedKbId.value || !query.value.trim()) return
  searching.value = true
  searched.value = true
  error.value = ''
  try {
    const resp = await searchKnowledge(selectedKbId.value, query.value.trim())
    results.value = resp.results
    totalResults.value = resp.totalResults
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '搜索失败'
    results.value = []
  } finally {
    searching.value = false
  }
}

function formatScore(score: number): string {
  return (score * 100).toFixed(1) + '%'
}

function highlightContent(content: string): string {
  if (!query.value.trim()) return escapeHtml(content)
  const escaped = escapeHtml(content)
  const queryEscaped = escapeRegex(query.value.trim())
  const regex = new RegExp(`(${queryEscaped})`, 'gi')
  return escaped.replace(regex, '<mark>$1</mark>')
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/\n/g, '<br>')
}

function escapeRegex(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
</script>

<style scoped>
.search-panel {
  padding: 24px;
}

.search-form {
  display: grid;
  gap: 16px;
}

.search-field {
  grid-column: 1 / -1;
}

.search-input-row {
  display: flex;
  gap: 12px;
}

.search-input-row input {
  flex: 1;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(38, 66, 102, 0.14);
  background: rgba(248, 250, 255, 0.92);
}

.search-input-row input:focus {
  outline: none;
  border-color: #3a8ad6;
  box-shadow: 0 0 0 3px rgba(58, 138, 214, 0.15);
}

.search-input-row .primary {
  flex-shrink: 0;
  padding: 12px 24px;
}

.chunkResult-summary {
  font-size: 14px;
}

.chunkResult-card {
  display: grid;
  gap: 12px;
}

.chunkResult-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chunkResult-content {
  font-size: 14px;
  line-height: 1.7;
  color: #2a2a2a;
}

.chunkResult-content :deep(mark) {
  background: rgba(255, 220, 100, 0.45);
  padding: 1px 3px;
  border-radius: 3px;
}

.chunkResult-source {
  font-size: 13px;
}

.status {
  color: #8a3b2f;
}
</style>


