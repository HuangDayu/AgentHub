<template>
  <div class="loop-node">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">🔄</div>
      <div class="node-label">循环</div>
    </div>
    <Handle type="source" :position="Position.Right" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
</script>

<style scoped>
.loop-node {
  background: #6610f2;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  border: 2px solid #520dc2;
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
