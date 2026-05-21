<template>
  <div class="llm-node" @dblclick="$emit('edit', id)">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">🧠</div>
      <div class="node-info">
        <div class="node-label">{{ data.label }}</div>
        <div v-if="data.node_param?.model" class="node-detail">
          {{ data.node_param.model }}
        </div>
      </div>
      <div v-if="selected" class="node-status executing">●</div>
    </div>
    <Handle type="source" :position="Position.Right" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps<{
  data: any
  id: string
  selected?: boolean
}>()

defineEmits<{
  (e: 'edit', id: string): void
}>()
</script>

<style scoped>
.llm-node {
  background: linear-gradient(135deg, #e6f4ff, #bae0ff);
  border: 2px solid #1677ff;
  border-radius: 8px;
  padding: 12px 16px;
  min-width: 180px;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.15);
  cursor: pointer;
  transition: all 0.2s;
}
.llm-node:hover {
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.25);
  transform: translateY(-2px);
}
.node-content {
  display: flex;
  align-items: center;
  gap: 10px;
}
.node-icon {
  font-size: 20px;
}
.node-info {
  flex: 1;
}
.node-label {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}
.node-detail {
  font-size: 11px;
  color: #666;
  margin-top: 2px;
}
.node-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  font-size: 8px;
  line-height: 8px;
  text-align: center;
}
.node-status.executing {
  color: #1677ff;
  animation: pulse 1s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
