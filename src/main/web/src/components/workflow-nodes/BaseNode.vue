<template>
  <div 
    :class="['base-node', nodeType, { selected: selected, disabled: disabled }]"
    @click="$emit('select')"
  >
    <!-- 输入连接点 -->
    <Handle 
      v-if="showInputHandle"
      type="target" 
      :position="Position.Left"
      class="handle-input"
    />
    
    <!-- 节点头部 -->
    <div class="node-header">
      <span class="node-icon">{{ icon }}</span>
      <span class="node-title">{{ label }}</span>
      <span v-if="status" class="status-indicator" :class="status">
        {{ getStatusIcon(status) }}
      </span>
    </div>
    
    <!-- 节点内容 -->
    <div class="node-content">
      <slot></slot>
    </div>
    
    <!-- 输出连接点 -->
    <Handle 
      v-if="showOutputHandle"
      type="source" 
      :position="Position.Right"
      class="handle-output"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import type { NodeStatus } from '@/types/workflow'

const props = withDefaults(defineProps<{
  nodeType: string
  label: string
  icon: string
  selected?: boolean
  disabled?: boolean
  status?: NodeStatus
  showInputHandle?: boolean
  showOutputHandle?: boolean
}>(), {
  selected: false,
  disabled: false,
  showInputHandle: true,
  showOutputHandle: true
})

defineEmits<{
  'select': []
}>()

// 方法
function getStatusIcon(status: NodeStatus): string {
  const icons: Record<NodeStatus, string> = {
    idle: '○',
    pending: '◔',
    running: '◑',
    success: '✓',
    failed: '✗',
    timeout: '⏱',
    skipped: '⊘'
  }
  return icons[status] || '○'
}
</script>

<style scoped>
.base-node {
  background: white;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  padding: 12px;
  min-width: 180px;
  max-width: 280px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
  cursor: pointer;
}

.base-node:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.base-node.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.base-node.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 节点类型样式 */
.base-node.start {
  border-color: #67c23a;
}

.base-node.end {
  border-color: #f56c6c;
}

.base-node.llm {
  border-color: #409eff;
}

.base-node.condition {
  border-color: #e6a23c;
}

.base-node.parallel {
  border-color: #909399;
}

.base-node.loop {
  border-color: #909399;
}

.base-node.tool {
  border-color: #67c23a;
}

.base-node.retrieval {
  border-color: #409eff;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-indicator {
  font-size: 14px;
}

.status-indicator.idle {
  color: #909399;
}

.status-indicator.pending {
  color: #e6a23c;
}

.status-indicator.running {
  color: #409eff;
  animation: pulse 1s infinite;
}

.status-indicator.success {
  color: #67c23a;
}

.status-indicator.failed {
  color: #f56c6c;
}

.status-indicator.timeout {
  color: #e6a23c;
}

.status-indicator.skipped {
  color: #909399;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.node-content {
  font-size: 12px;
  color: #606266;
}

.handle-input,
.handle-output {
  width: 12px;
  height: 12px;
  background: #409eff;
  border: 2px solid white;
}

.handle-input:hover,
.handle-output:hover {
  background: #66b1ff;
}
</style>
