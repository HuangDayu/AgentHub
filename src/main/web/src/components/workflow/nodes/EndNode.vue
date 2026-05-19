<template>
  <div class="end-node" :class="{ selected: selected }">
    <Handle type="target" :position="Position.Left" id="input" />
    <div class="node-header">
      <div class="node-icon">⏹</div>
      <div class="node-title">{{ data.label || '结束' }}</div>
    </div>
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
.end-node {
  background: white;
  border: 2px solid #dc3545;
  border-radius: 8px;
  min-width: 100px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.end-node.selected {
  border-color: #bd2130;
  box-shadow: 0 4px 8px rgba(220, 53, 69, 0.3);
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #dc3545;
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
