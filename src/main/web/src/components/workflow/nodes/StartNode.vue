<template>
  <div class="start-node" :class="{ selected: selected }">
    <div class="node-header">
      <div class="node-icon">▶</div>
      <div class="node-title">{{ data.label || '开始' }}</div>
    </div>
    <Handle type="source" :position="Position.Right" id="output" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps<{
  data: any
  selected?: boolean
}>()
</script>

<style scoped>
.start-node {
  background: white;
  border: 2px solid #28a745;
  border-radius: 8px;
  min-width: 100px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.start-node.selected {
  border-color: #1e7e34;
  box-shadow: 0 4px 8px rgba(40, 167, 69, 0.3);
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #28a745;
  color: white;
  border-radius: 6px;
}

.node-icon {
  font-size: 16px;
}

.node-title {
  font-weight: bold;
  font-size: 14px;
}
</style>
