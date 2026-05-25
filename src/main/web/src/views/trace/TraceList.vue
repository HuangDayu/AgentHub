<template>
  <div class="trace-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>追踪列表</span>
          <el-input
            v-model="searchTraceId"
            placeholder="输入 Trace ID 搜索"
            style="width: 300px"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
      </template>

      <el-table
        :data="spans"
        v-loading="loading"
        stripe
        border
      >
        <el-table-column prop="spanId" label="Span ID" width="200" />
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column prop="kind" label="类型" width="100" />
        <el-table-column label="延迟" width="120">
          <template #default="{ row }">
            {{ formatLatency(row.latencyNs) }}
          </template>
        </el-table-column>
        <el-table-column prop="model" label="模型" width="150" />
        <el-table-column label="Token" width="120">
          <template #default="{ row }">
            {{ row.totalTokens || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.statusCode === 0 ? 'success' : 'danger'">
              {{ row.statusCode === 0 ? '成功' : '错误' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Span 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="Span 详情"
      width="70%"
    >
      <SpanDetail v-if="selectedSpan" :span="selectedSpan" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { spanApi } from '@/api/span';
import SpanDetail from '@/components/trace/SpanDetail.vue';
import type { Span } from '@/types/span';

const spans = ref<Span[]>([]);
const loading = ref(false);
const searchTraceId = ref('');
const detailVisible = ref(false);
const selectedSpan = ref<Span | null>(null);

/**
 * 加载 Span 列表
 */
const loadSpans = async () => {
  loading.value = true;
  try {
    spans.value = await spanApi.list();
  } finally {
    loading.value = false;
  }
};

/**
 * 搜索处理
 */
const handleSearch = async () => {
  if (!searchTraceId.value) {
    await loadSpans();
    return;
  }

  loading.value = true;
  try {
    spans.value = await spanApi.listByTrace(searchTraceId.value);
  } finally {
    loading.value = false;
  }
};

/**
 * 查看详情
 */
const handleViewDetail = (span: Span) => {
  selectedSpan.value = span;
  detailVisible.value = true;
};

/**
 * 格式化延迟时间
 */
const formatLatency = (latencyNs: number): string => {
  if (!latencyNs) return '-';
  const ms = latencyNs / 1000000;
  return `${ms.toFixed(2)} ms`;
};

/**
 * 格式化时间
 */
const formatTime = (time: string): string => {
  return new Date(time).toLocaleString();
};

onMounted(() => {
  loadSpans();
});
</script>

<style scoped>
.trace-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
