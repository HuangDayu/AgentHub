<template>
  <div class="retrieval-node" :class="{ selected: selected, disabled: data.disabled }">
    <!-- 输入Handle -->
    <Handle type="target" :position="Position.Left" id="input" />

    <!-- 节点内容 -->
    <div class="node-header">
      <div class="node-icon">📚</div>
      <div class="node-title">{{ data.label || '知识检索' }}</div>
      <button v-if="!data.disabled" class="config-btn" @click="openConfig">
        ⚙️
      </button>
    </div>

    <div class="node-body">
      <div class="config-info">
        <div v-if="data.config?.knowledgeBase" class="info-item">
          <span class="label">知识库:</span>
          <span class="value">{{ data.config.knowledgeBase }}</span>
        </div>
        <div v-if="data.config?.topK" class="info-item">
          <span class="label">TopK:</span>
          <span class="value">{{ data.config.topK }}</span>
        </div>
        <div v-if="data.config?.scoreThreshold" class="info-item">
          <span class="label">阈值:</span>
          <span class="value">{{ data.config.scoreThreshold }}</span>
        </div>
      </div>
    </div>

    <!-- 输出Handle -->
    <Handle type="source" :position="Position.Right" id="output" />

    <!-- 状态显示 -->
    <div v-if="data.status" class="node-status" :class="data.status">
      {{ getStatusText(data.status) }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
import { computed } from 'vue'

const props = defineProps<{
  id: string
  data: any
  selected?: boolean
}>()

const emit = defineEmits<{
  config: [nodeId: string]
}>()

const openConfig = () => {
  emit('config', props.id)
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    waiting: '等待中',
    running: '检索中...',
    success: '检索完成',
    error: '检索失败'
  }
  return statusMap[status] || status
}
</script>

<style scoped>
.retrieval-node {
  background: #f0f8ff;
  border: 2px solid #4169e1;
  border-radius: 8px;
  min-width: 200px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.retrieval-node:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.retrieval-node.selected {
  border-color: #1e90ff;
  box-shadow: 0 0 0 2px rgba(30, 144, 255, 0.3);
}

.retrieval-node.disabled {
  opacity: 0.6;
  pointer-events: none;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #e6f2ff;
  border-radius: 6px 6px 0 0;
}

.node-icon {
  font-size: 16px;
}

.node-title {
  flex: 1;
  font-weight: bold;
  font-size: 14px;
}

.config-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 2px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.config-btn:hover {
  opacity: 1;
}

.node-body {
  padding: 8px 12px;
  background: white;
}

.config-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item {
  display: flex;
  gap: 4px;
  font-size: 12px;
}

.info-item .label {
  color: #666;
}

.info-item .value {
  color: #333;
  font-weight: 500;
}

.node-status {
  padding: 4px 12px;
  font-size: 12px;
  text-align: center;
  border-radius: 0 0 6px 6px;
}

.node-status.running {
  background: #fff3cd;
  color: #856404;
}

.node-status.success {
  background: #d4edda;
  color: #155724;
}

.node-status.error {
  background: #f8d7da;
  color: #721c24;
}

.node-status.waiting {
  background: #e2e3e5;
  color: #383d41;
}
</style>
