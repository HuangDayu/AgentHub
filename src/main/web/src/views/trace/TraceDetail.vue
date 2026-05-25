<template>
  <div class="trace-detail">
    <el-page-header @back="goBack" content="追踪详情" />

    <el-card class="mt-4" v-loading="loading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Trace ID">
          {{ trace?.traceId }}
        </el-descriptions-item>
        <el-descriptions-item label="Run ID">
          {{ trace?.runId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="根 Span ID">
          {{ trace?.rootSpanId }}
        </el-descriptions-item>
        <el-descriptions-item label="Span 数量">
          {{ trace?.spanCount }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ trace?.startTimeUnixNano }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ trace?.endTimeUnixNano }}
        </el-descriptions-item>
        <el-descriptions-item label="持续时间">
          {{ formatDuration(trace?.durationNs) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="trace?.statusCode === 0 ? 'success' : 'danger'">
            {{ trace?.statusCode === 0 ? '成功' : '错误' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总 Token" v-if="trace?.totalTokens">
          {{ trace.totalTokens }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- Span 列表 -->
    <el-card class="mt-4">
      <template #header>
        <span>Span 列表</span>
      </template>
      <el-table :data="spans" stripe border>
        <el-table-column prop="spanId" label="Span ID" width="200" />
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column prop="kind" label="类型" width="100" />
        <el-table-column label="延迟" width="120">
          <template #default="{ row }">
            {{ formatLatency(row.latencyNs) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.statusCode === 0 ? 'success' : 'danger'">
              {{ row.statusCode === 0 ? '成功' : '错误' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewSpan(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Span 详情对话框 -->
    <el-dialog v-model="spanDetailVisible" title="Span 详情" width="70%">
      <SpanDetail v-if="selectedSpan" :span="selectedSpan" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { spanApi } from '@/api/span';
import SpanDetail from '@/components/trace/SpanDetail.vue';
import type { Span, Trace } from '@/types/span';

const route = useRoute();
const router = useRouter();

const trace = ref<Trace | null>(null);
const spans = ref<Span[]>([]);
const loading = ref(false);
const spanDetailVisible = ref(false);
const selectedSpan = ref<Span | null>(null);

const loadTrace = async () => {
  const traceId = route.params.traceId as string;
  loading.value = true;
  try {
    spans.value = await spanApi.listByTrace(traceId);
    // 从 spans 构建 trace 信息
    if (spans.value.length > 0) {
      const rootSpan = spans.value.find(s => !s.parentSpanId);
      trace.value = {
        traceId,
        runId: spans.value[0].runId,
        rootSpanId: rootSpan?.spanId,
        spanCount: spans.value.length,
        startTimeUnixNano: rootSpan?.startTimeUnixNano || '',
        endTimeUnixNano: rootSpan?.endTimeUnixNano || '',
        durationNs: rootSpan?.latencyNs || 0,
        statusCode: spans.value.some(s => s.statusCode !== 0) ? 2 : 0,
        totalTokens: spans.value.reduce((sum, s) => sum + (s.totalTokens || 0), 0),
      };
    }
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.push('/agenthub/traces');
};

const handleViewSpan = (span: Span) => {
  selectedSpan.value = span;
  spanDetailVisible.value = true;
};

const formatDuration = (durationNs?: number): string => {
  if (!durationNs) return '-';
  const ms = durationNs / 1000000;
  return `${ms.toFixed(2)} ms`;
};

const formatLatency = (latencyNs: number): string => {
  if (!latencyNs) return '-';
  const ms = latencyNs / 1000000;
  return `${ms.toFixed(2)} ms`;
};

onMounted(() => {
  loadTrace();
});
</script>

<style scoped>
.trace-detail {
  padding: 20px;
}

.mt-4 {
  margin-top: 16px;
}
</style>
