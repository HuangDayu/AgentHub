<template>
  <section class="grid">
    <article class="panel">
      <div class="toolbar">
        <h2>平台概览</h2>
        <button class="secondary" @click="loadStats" :disabled="loading">
          {{ loading ? '加载中…' : '刷新数据' }}
        </button>
        <span class="status" v-if="error">{{ error }}</span>
      </div>
    </article>
    <div class="stats">
      <article class="stat">
        <h3>活跃租户数</h3>
        <strong class="stat-value">{{ stats?.activeTenants ?? 0 }}</strong>
        <p class="muted">当前活跃的租户数量</p>
      </article>
      <article class="stat">
        <h3>活跃工作区数</h3>
        <strong class="stat-value">{{ stats?.activeWorkspaces ?? 0 }}</strong>
        <p class="muted">当前活跃的工作区数量</p>
      </article>
      <article class="stat">
        <h3>总知识库数</h3>
        <strong class="stat-value">{{ stats?.totalKnowledgeBases ?? 0 }}</strong>
        <p class="muted">平台知识库总数</p>
      </article>
      <article class="stat">
        <h3>总 Agent 数</h3>
        <strong class="stat-value">{{ stats?.totalAgents ?? 0 }}</strong>
        <p class="muted">平台 Agent 总数</p>
      </article>
      <article class="stat">
        <h3>当前计费总额</h3>
        <strong class="stat-value">
          {{ stats ? formatCurrency(stats.totalBillingCents, stats.currency) : '未加载' }}
        </strong>
        <p class="muted">所有租户累计计费</p>
      </article>
      <article class="stat">
        <h3>审计事件总数</h3>
        <strong class="stat-value">{{ stats?.totalAuditEvents ?? 0 }}</strong>
        <p class="muted">审计日志总条数</p>
      </article>
    </div>

  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getPlatformStats } from '@/api/admin-api'
import { formatCurrency } from '@/common/format'
import type { PlatformStats } from '@/domain/types'

const stats = ref<PlatformStats | null>(null)
const loading = ref(false)
const error = ref('')

onMounted(loadStats)

async function loadStats() {
  loading.value = true
  error.value = ''
  try {
    stats.value = await getPlatformStats()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载概览数据失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #27415d;
}
</style>

