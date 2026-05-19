<template>
  <div class="variable-node">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">📝</div>
      <div class="node-label">变量</div>
    </div>
    <Handle type="source" :position="Position.Right" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
</script>

<style scoped>
.variable-node {
  background: #20c997;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  border: 2px solid #1aa179;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-icon {
  font-size: 16px;
}

.node-label {
  font-weight: bold;
}
</style>
