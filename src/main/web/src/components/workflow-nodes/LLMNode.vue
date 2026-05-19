<template>
  <div class="llm-node" :class="{ selected: selected }">
    <Handle type="target" :position="Position.Left" />
    
    <div class="node-header">
      <span class="node-icon">🤖</span>
      <span class="node-title">{{ data.label }}</span>
      <span v-if="executionStatus" class="status-badge" :class="executionStatus">
        {{ getStatusIcon(executionStatus) }}
      </span>
    </div>
    
    <div class="node-body">
      <div class="info-row">
        <span class="label">模型:</span>
        <span class="value">{{ modelName || '未选择' }}</span>
      </div>
      <div class="info-row">
        <span class="label">温度:</span>
        <span class="value">{{ temperature }}</span>
      </div>
    </div>
    
    <Handle type="source" :position="Position.Right" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'

interface NodeData {
  label: string
  node_param: {
    model_id: string
    model_config: {
      temperature: number
      max_tokens: number
    }
    sys_prompt: string
    user_prompt: string
  }
}

const props = defineProps<NodeProps<NodeData>>()

const executionStatus = computed(() => {
  // 从store获取执行状态
  return null
})

const modelName = computed(() => {
  return props.data.node_param?.model_id || ''
})

const temperature = computed(() => {
  return props.data.node_param?.model_config?.temperature || 0.7
})

function getStatusIcon(status: string) {
  const icons: Record<string, string> = {
    running: '⏳',
    success: '✓',
    failed: '✗',
    pending: '○'
  }
  return icons[status] || ''
}
</script>

<style scoped>
.llm-node {
  background: white;
  border: 2px solid #67c23a;
  border-radius: 8px;
  padding: 12px;
  min-width: 200px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.llm-node:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.llm-node.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.node-icon {
  font-size: 18px;
}

.node-title {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.status-badge {
  font-size: 14px;
}

.status-badge.running {
  color: #e6a23c;
}

.status-badge.success {
  color: #67c23a;
}

.status-badge.failed {
  color: #f56c6c;
}

.node-body {
  font-size: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.label {
  color: #909399;
}

.value {
  color: #606266;
  font-weight: 500;
}
</style>
