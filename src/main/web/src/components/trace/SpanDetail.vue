<template>
  <div class="span-detail">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="Span ID">
        {{ span.spanId }}
      </el-descriptions-item>
      <el-descriptions-item label="Trace ID">
        {{ span.traceId }}
      </el-descriptions-item>
      <el-descriptions-item label="Parent Span ID">
        {{ span.parentSpanId || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="名称">
        {{ span.name }}
      </el-descriptions-item>
      <el-descriptions-item label="类型">
        {{ span.kind }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="span.statusCode === 0 ? 'success' : 'danger'">
          {{ span.statusCode === 0 ? '成功' : '错误' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="开始时间">
        {{ span.startTimeUnixNano }}
      </el-descriptions-item>
      <el-descriptions-item label="结束时间">
        {{ span.endTimeUnixNano }}
      </el-descriptions-item>
      <el-descriptions-item label="延迟">
        {{ formatLatency(span.latencyNs) }}
      </el-descriptions-item>
      <el-descriptions-item label="模型">
        {{ span.model || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="输入 Token">
        {{ span.inputTokens || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="输出 Token">
        {{ span.outputTokens || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="总 Token">
        {{ span.totalTokens || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="Run ID">
        {{ span.runId || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="Agent ID">
        {{ span.agentId || '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <!-- 属性 -->
    <el-card v-if="span.attributes" class="mt-4">
      <template #header>
        <span>属性</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item
          v-for="(value, key) in span.attributes"
          :key="key"
          :label="key"
        >
          {{ JSON.stringify(value, null, 2) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 事件 -->
    <el-card v-if="span.events && span.events.length > 0" class="mt-4">
      <template #header>
        <span>事件</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(event, index) in span.events"
          :key="index"
          :timestamp="event.timeUnixNano"
        >
          <el-card>
            <h4>{{ event.name }}</h4>
            <pre v-if="event.attributes">{{
              JSON.stringify(event.attributes, null, 2)
            }}</pre>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { Span } from '@/types/span';

const props = defineProps<{
  span: Span;
}>();

/**
 * 格式化延迟时间
 */
const formatLatency = (latencyNs: number): string => {
  if (!latencyNs) return '-';
  const ms = latencyNs / 1000000;
  return `${ms.toFixed(2)} ms`;
};
</script>

<style scoped>
.span-detail {
  padding: 20px;
}

.mt-4 {
  margin-top: 16px;
}

pre {
  background: var(--bg-stripe);
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
}
</style>
