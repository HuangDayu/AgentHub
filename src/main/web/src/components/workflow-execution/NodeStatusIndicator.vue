<template>
  <div :class="['status-indicator', status]">
    <span class="status-icon">{{ statusIcon }}</span>
    <span class="status-text">{{ statusText }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { NodeStatus } from '@/types/workflow'

const props = defineProps<{
  status: NodeStatus
  nodeId: string
}>()

const statusIcon = computed(() => {
  const icons: Record<NodeStatus, string> = {
    idle: '○',
    pending: '◔',
    running: '◑',
    success: '✓',
    failed: '✗',
    timeout: '⏱',
    skipped: '⊘'
  }
  return icons[props.status] || '○'
})

const statusText = computed(() => {
  const texts: Record<NodeStatus, string> = {
    idle: '空闲',
    pending: '等待',
    running: '执行中',
    success: '成功',
    failed: '失败',
    timeout: '超时',
    skipped: '跳过'
  }
  return texts[props.status] || props.status
})
</script>

<style scoped>
.status-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-icon {
  font-size: 14px;
}

.status-text {
  font-weight: 500;
}

.status-indicator.idle {
  background: #f4f4f5;
  color: #909399;
}

.status-indicator.pending {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-indicator.running {
  background: #ecf5ff;
  color: #409eff;
  animation: pulse 1s infinite;
}

.status-indicator.success {
  background: #f0f9eb;
  color: #67c23a;
}

.status-indicator.failed {
  background: #fef0f0;
  color: #f56c6c;
}

.status-indicator.timeout {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-indicator.skipped {
  background: #f4f4f5;
  color: #c0c4cc;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
</style>
