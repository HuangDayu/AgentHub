<template>
  <div class="start-node">
    <Handle type="source" :position="Position.Right" />
    <div class="node-content">
      <div class="node-icon">▶</div>
      <div class="node-label">开始</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
</script>

<style scoped>
.start-node {
  background: #28a745;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  border: 2px solid #1e7e34;
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
