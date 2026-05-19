<template>
  <div class="parallel-node">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">⚡</div>
      <div class="node-label">并行</div>
    </div>
    <Handle type="source" :position="Position.Right" id="branch1" />
    <Handle type="source" :position="Position.Bottom" id="branch2" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
</script>

<style scoped>
.parallel-node {
  background: #fd7e14;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  border: 2px solid #dc6502;
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
