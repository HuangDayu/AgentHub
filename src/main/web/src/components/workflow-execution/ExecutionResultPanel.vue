<template>
  <div class="execution-result-panel">
    <!-- 基本信息 -->
    <div class="result-section">
      <div class="section-title">基本信息</div>
      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">节点ID:</span>
          <span class="info-value">{{ result.node_id }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">状态:</span>
          <NodeStatusIndicator :status="result.status" :node-id="result.node_id" />
        </div>
        <div class="info-item">
          <span class="info-label">开始时间:</span>
          <span class="info-value">{{ formatTime(result.start_time) }}</span>
        </div>
        <div v-if="result.end_time" class="info-item">
          <span class="info-label">结束时间:</span>
          <span class="info-value">{{ formatTime(result.end_time) }}</span>
        </div>
        <div v-if="result.duration" class="info-item">
          <span class="info-label">执行时长:</span>
          <span class="info-value">{{ formatDuration(result.duration) }}</span>
        </div>
      </div>
    </div>
    
    <!-- 输入数据 -->
    <div class="result-section">
      <div class="section-title">输入数据</div>
      <div class="json-viewer">
        <pre>{{ formatJson(result.input) }}</pre>
      </div>
    </div>
    
    <!-- 输出数据 -->
    <div v-if="result.output" class="result-section">
      <div class="section-title">输出数据</div>
      <div class="json-viewer">
        <pre>{{ formatJson(result.output) }}</pre>
      </div>
    </div>
    
    <!-- 错误信息 -->
    <div v-if="result.error" class="result-section error-section">
      <div class="section-title">错误信息</div>
      <div class="error-content">{{ result.error }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import NodeStatusIndicator from './NodeStatusIndicator.vue'
import type { NodeResult } from '@/types/workflow'

const props = defineProps<{
  result: NodeResult
}>()

function formatTime(time: string): string {
  return new Date(time).toLocaleString('zh-CN')
}

function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`
  if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`
  return `${(ms / 60000).toFixed(2)}min`
}

function formatJson(data: any): string {
  return JSON.stringify(data, null, 2)
}
</script>

<style scoped>
.execution-result-panel {
  padding: 16px;
}

.result-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e1e5eb;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-size: 13px;
  color: #909399;
}

.info-value {
  font-size: 13px;
  color: #606266;
  font-family: 'Monaco', 'Menlo', monospace;
}

.json-viewer {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 12px;
  overflow-x: auto;
}

.json-viewer pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  line-height: 1.6;
  color: #606266;
}

.error-section {
  background: #fef0f0;
  padding: 16px;
  border-radius: 4px;
}

.error-content {
  font-size: 13px;
  color: #f56c6c;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
