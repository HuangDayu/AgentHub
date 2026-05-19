<template>
  <div class="end-node">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">⏹</div>
      <div class="node-label">结束</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
</script>

<style scoped>
.end-node {
  background: #dc3545;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  border: 2px solid #bd2130;
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
