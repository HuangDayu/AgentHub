<template>
  <div class="condition-node">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">❓</div>
      <div class="node-label">条件</div>
    </div>
    <Handle type="source" :position="Position.Right" id="true" />
    <Handle type="source" :position="Position.Bottom" id="false" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
</script>

<style scoped>
.condition-node {
  background: #ffc107;
  color: #333;
  padding: 10px 20px;
  border-radius: 8px;
  border: 2px solid #d39e00;
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
